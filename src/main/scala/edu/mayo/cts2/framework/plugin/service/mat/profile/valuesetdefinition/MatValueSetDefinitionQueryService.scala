package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import scala.collection.JavaConversions._
import scala.collection.JavaConversions.iterableAsScalaIterable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core._
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionDirectoryEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQuery
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQueryService
import javax.annotation.Resource
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
import edu.mayo.cts2.framework.plugin.service.mat.profile.valueset.MatValueSetUtils
import edu.mayo.cts2.framework.filter.`match`.StateAdjustingPropertyReference
import org.springframework.data.jpa.domain.Specification
import edu.mayo.cts2.framework.filter.`match`.StateAdjustingPropertyReference.StateUpdater
import javax.persistence.criteria._
import edu.mayo.cts2.framework.model.core.types.TargetReferenceType

@Component
class MatValueSetDefinitionQueryService
  extends AbstractService
  with ValueSetDefinitionQueryService {

  @Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  def getSupportedMatchAlgorithms: java.util.Set[_ <: MatchAlgorithmReference] = { 
    val set = new java.util.HashSet[MatchAlgorithmReference]() 
    set.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference)
    set.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference)
    set
  }

  def getSupportedSearchReferences: java.util.Set[_ <: PropertyReference] = {
    val set = new java.util.HashSet[PropertyReference]()
    set.add(StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference)
    set.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference)
    set.add(createAttributeReference("creator", "http://purl.org/dc/elements/1.1/creator", "valueSetDeveloper"))
    set
  }

  def createAttributeReference(name: String, uri: String, path: String): StateAdjustingPropertyReference[Seq[Specification[ValueSet]]] = {
    val stateUpdater = new StateUpdater[Seq[Specification[ValueSet]]]() {
      def updateState(currentState: Seq[Specification[ValueSet]], matchAlgorithm: MatchAlgorithmReference, queryString: String): Seq[Specification[ValueSet]] = {
        val specification = new Specification[ValueSet]() {
          def toPredicate(root: Root[ValueSet], query: CriteriaQuery[_], cb: CriteriaBuilder): Predicate = {
            val fn = filterFn(matchAlgorithm)
            fn(root.get(path).asInstanceOf[Path[String]], queryString)
          }
        }
        currentState :+ specification
      }
    }

    val ref = new StateAdjustingPropertyReference[Seq[Specification[ValueSet]]](stateUpdater)
    ref.setReferenceType(TargetReferenceType.ATTRIBUTE);
    ref.setReferenceTarget(new URIAndEntityName())
    ref.getReferenceTarget.setName(name)
    ref.getReferenceTarget.setUri(uri)

    ref
  }

  private def filterFn(ref: MatchAlgorithmReference) = {
    val matchAlgorithm = ref.getContent
    val contains = StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference.getContent
    val startsWith = StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference.getContent
    val exact = StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference.getContent
    val cb = entityManager.getCriteriaBuilder
    val cbLikeFn = cb.like(_:Expression[String],_:String)
    val cbEqualsFn = cb.equal(_:Expression[String],_:String)
    val likeFn = (ex:Expression[String], formatter:(String) => String, q:String) => {
      cbLikeFn(cb.lower(ex),formatter(q))
    }

    val equalsFn = (ex:Expression[String], formatter:(String) => String, q:String) => {
      cbEqualsFn(ex,formatter(q))
    }

    matchAlgorithm match {
      case `contains` => ( likeFn(_:Expression[String], (s:String) => '%'+s.toLowerCase+'%', _:String ) )
      case `startsWith` => ( likeFn(_:Expression[String], (s:String) => s.toLowerCase+'%', _:String ) )
      case `exact` => ( equalsFn(_:Expression[String], (s:String) => s, _:String ) )
    }

  }: (Expression[String], String) => Predicate

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = { new java.util.HashSet[PropertyReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  @Transactional
  def getResourceSummaries(query: ValueSetDefinitionQuery, sort: SortCriteria, page: Page = new Page()): DirectoryResult[ValueSetDefinitionDirectoryEntry] = {
    val fn =
    if(query == null || query.getRestrictions == null || query.getRestrictions.getValueSet == null){
      valueSetVersionRepository.findAll(_:Pageable)
    } else {
      val name = query.getRestrictions.getValueSet.getName
      var creator: String = null
      query.getFilterComponent.foreach(filter => {
        if (filter.getPropertyReference.getReferenceTarget.getName.equals("creator")) {
          creator = filter.getMatchValue
        }
      })
      if (name != null && creator != null)
        valueSetVersionRepository.findByValueSetNameAndCreator(name, creator, _:Pageable)
      else if (name == null && creator != null)
        valueSetVersionRepository.findByCreator(creator, _:Pageable)
      else
        valueSetVersionRepository.findByValueSetName(name, _:Pageable)
    }
    
    val valueSets = fn(toPageable(Option(page)))

    val entries = valueSets.foldLeft(Seq[ValueSetDefinitionDirectoryEntry]())(transformValueSetVersion)

    val totalElements = valueSets.getTotalElements
    
    new DirectoryResult(entries, entries.size == totalElements)
  }

  def transformValueSetVersion = (seq:Seq[ValueSetDefinitionDirectoryEntry], valueSetVersion:ValueSetVersion) => {
    val valueSetDefName = MatValueSetUtils.getValueSetDefName(valueSetVersion)
    
    val summary = new ValueSetDefinitionDirectoryEntry()
    summary.setResourceName(valueSetDefName)
    summary.setAbout(UriUtils.oidToUri(valueSetVersion.valueSet.oid))
    summary.setDocumentURI(UriUtils.uuidToUri(valueSetVersion.id))
    summary.setFormalName(valueSetVersion.valueSet.formalName)
    summary.setHref(urlConstructor.createValueSetDefinitionUrl(valueSetVersion.valueSet.name, valueSetDefName))

    summary.setDefinedValueSet(MatValueSetUtils.buildValueSetReference(valueSetVersion, urlConstructor))
    
    seq ++ Seq(summary)
  }:Seq[ValueSetDefinitionDirectoryEntry]

  def getResourceList(p1: ValueSetDefinitionQuery, p2: SortCriteria, p3: Page): DirectoryResult[ValueSetDefinition] = null

  def count(p1: ValueSetDefinitionQuery): Int = 0

}