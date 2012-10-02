package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import scala.collection.JavaConversions._
import org.apache.commons.collections.CollectionUtils
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.filter.`match`.StateAdjustingPropertyReference
import edu.mayo.cts2.framework.filter.`match`.StateAdjustingPropertyReference.StateUpdater
import edu.mayo.cts2.framework.filter.directory.DirectoryBuilder
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.model.core.PredicateReference
import edu.mayo.cts2.framework.model.core.PropertyReference
import edu.mayo.cts2.framework.model.core.SortCriteria
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.core.types.TargetReferenceType
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService
import javax.annotation.Resource
import javax.persistence.Entity
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root
import javax.persistence.criteria.Predicate
import javax.persistence.criteria.Path
import javax.persistence.criteria.JoinType
import edu.mayo.cts2.framework.plugin.service.mat.profile.ProfileUtils
import javax.persistence.criteria.Expression

@Component
class MatValueSetQueryService
  extends AbstractService
  with ValueSetQueryService {

  val NQF_NUMBER_PROP: String = "nqfnumber"
  val EMEASURE_ID_PROP: String = "emeasureid"

  @Resource
  var valueSetRepository: ValueSetRepository = _

  def getSupportedMatchAlgorithms: java.util.Set[MatchAlgorithmReference] = {
    val set = new java.util.HashSet[MatchAlgorithmReference]()
    set.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference)
    set.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference)
    set.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference)
    set
  }

  def getSupportedSearchReferences: java.util.Set[StateAdjustingPropertyReference[Seq[Specification[ValueSet]]]] = {
    val set = new java.util.HashSet[StateAdjustingPropertyReference[Seq[Specification[ValueSet]]]]()

    val nameRef = StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference
    val synopsisRef = StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference

    set.add(
      createAttributeReference(
        nameRef.getReferenceTarget.getName, nameRef.getReferenceTarget.getUri, "name"))
    set.add(
      createAttributeReference(
        synopsisRef.getReferenceTarget.getName, synopsisRef.getReferenceTarget.getUri, "formalName"))

    set.add(createPropertyReference(NQF_NUMBER_PROP, UriUtils.toSvsUri("NQF Number"), "NQF Number"))
    set.add(createPropertyReference(EMEASURE_ID_PROP, UriUtils.toSvsUri("eMeasure Identifier"), "eMeasure Identifier"))

    set
  }

  def createAttributeReference(
    name: String,
    uri: String,
    path: String): StateAdjustingPropertyReference[Seq[Specification[ValueSet]]] = {

    val stateUpdater = new StateUpdater[Seq[Specification[ValueSet]]]() {

      def updateState(
        currentState: Seq[Specification[ValueSet]],
        matchAlgorithm: MatchAlgorithmReference,
        queryString: String): Seq[Specification[ValueSet]] = {

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

  def createPropertyReference(
    name: String,
    uri: String,
    propertyName: String): StateAdjustingPropertyReference[Seq[Specification[ValueSet]]] = {

    val stateUpdater = new StateUpdater[Seq[Specification[ValueSet]]]() {

      def updateState(
        currentState: Seq[Specification[ValueSet]],
        matchAlgorithm: MatchAlgorithmReference,
        queryString: String): Seq[Specification[ValueSet]] = {

        val specification = new Specification[ValueSet]() {

          def toPredicate(root: Root[ValueSet], query: CriteriaQuery[_], cb: CriteriaBuilder): Predicate = {

            val join = root.join("properties", JoinType.INNER)
            
            val fn = filterFn(matchAlgorithm)
            
            val pred1 = fn(join.get("value").asInstanceOf[Path[String]], queryString)
            val pred2 = cb.equal(join.get("name").asInstanceOf[Path[String]], propertyName)

            ProfileUtils.and(cb, pred1, pred2)
          }

        }
        currentState :+ specification
      }
    }

    val ref = new StateAdjustingPropertyReference[Seq[Specification[ValueSet]]](stateUpdater)
    ref.setReferenceType(TargetReferenceType.PROPERTY)
    ref.setReferenceTarget(new URIAndEntityName())
    ref.getReferenceTarget.setName(name)
    ref.getReferenceTarget.setUri(uri)

    ref
  }

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = { new java.util.HashSet[PropertyReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  @Transactional
  def getResourceSummaries(query: ValueSetQuery, sort: SortCriteria, requestedPage: Page = new Page()): DirectoryResult[ValueSetCatalogEntrySummary] = {
    val page = if (requestedPage == null) new Page() else requestedPage

    var directoryBuilder: DirectoryBuilder[ValueSetCatalogEntrySummary] = new HibernateCriteriaDirectoryBuilder[ValueSetCatalogEntrySummary, ValueSet](
      classOf[ValueSet],
      entityManager,
      transformSingleValueSet,
      getSupportedMatchAlgorithms,
      getSupportedSearchReferences)

    if (query != null) {
      directoryBuilder = directoryBuilder.restrict(query.getFilterComponent)
    }

    directoryBuilder.
      addStart(page.getStart).
      addMaxToReturn(page.getMaxToReturn).
      resolve
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

  def transformSingleValueSet = (valueSet: ValueSet) => {
    val summary = new ValueSetCatalogEntrySummary()
    summary.setValueSetName(valueSet.name)
    summary.setAbout(UriUtils.oidToUri(valueSet.oid))
    summary.setFormalName(valueSet.formalName)
    summary.setHref(urlConstructor.createValueSetUrl((valueSet.name)))

    summary.setCurrentDefinition(
      MatValueSetUtils.buildValueSetDefinitionReference(
        summary.getValueSetName, summary.getAbout,
        valueSet.currentVersion,
        urlConstructor))

    summary
  }: ValueSetCatalogEntrySummary

  def transformValueSet = (seq: Seq[ValueSetCatalogEntrySummary], valueSet: ValueSet) => {
    seq :+ transformSingleValueSet(valueSet)
  }: Seq[ValueSetCatalogEntrySummary]

  def getResourceList(p1: ValueSetQuery, p2: SortCriteria, p3: Page): DirectoryResult[ValueSetCatalogEntry] = null

  def count(p1: ValueSetQuery): Int = 0

}