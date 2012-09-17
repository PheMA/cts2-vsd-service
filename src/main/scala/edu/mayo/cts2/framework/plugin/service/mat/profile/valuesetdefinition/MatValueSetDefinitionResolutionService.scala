package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import java.util.Set
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.apache.commons.collections.CollectionUtils
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core.CodeSystemReference
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference
import edu.mayo.cts2.framework.model.core.EntitySynopsis
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.model.core.PredicateReference
import edu.mayo.cts2.framework.model.core.PropertyReference
import edu.mayo.cts2.framework.model.core.SortCriteria
import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.entity.EntityDirectoryEntry
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSet
import edu.mayo.cts2.framework.model.valuesetdefinition.ResolvedValueSetHeader
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import edu.mayo.cts2.framework.plugin.service.mat.namespace.NamespaceResolutionService
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.uri.IdType
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriResolver
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import javax.annotation.Resource
import com.google.common.collect.Lists
import org.apache.commons.lang.ObjectUtils

@Component
class MatValueSetDefinitionResolutionService extends AbstractService with ValueSetDefinitionResolutionService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Resource
  var hrefBuilder: HrefBuilder = _

  @Resource
  var namespaceResolutionService: NamespaceResolutionService = _

  def getSupportedMatchAlgorithms: Set[_ <: MatchAlgorithmReference] = null

  def getSupportedSearchReferences: Set[_ <: PropertyReference] = null

  def getSupportedSortReferences: Set[_ <: PropertyReference] = null

  def getKnownProperties: Set[PredicateReference] = null

  @Transactional
  def resolveDefinition(
    id: ValueSetDefinitionReadId,
    codeSystemVersions: Set[NameOrURI],
    codeSystemVersionTag: NameOrURI,
    query: ResolvedValueSetResolutionEntityQuery,
    sort: SortCriteria,
    readContext: ResolvedReadContext,
    page: Page): ResolvedValueSetResult[EntitySynopsis] = {

    //NOTE: Currently not using this variable...
    val valueSetDefinitionName = id.getName

    val valueSetName = id.getValueSet.getName

    val valueSet = valueSetRepository.findOneByName(valueSetName)

    if (valueSet == null) {
      return null
    }

    val synopsises: (Seq[ValueSetEntry], Int) =
      if (CollectionUtils.isNotEmpty(valueSet.includesValueSets)) {
        valueSet.includesValueSets.foldLeft[(Seq[ValueSetEntry], Int)]((valueSet.entries, valueSet.entries.size))(
          (list, oid) => {
            val result = getEntriesFromOid(oid)
            (list._1 ++ result._1, list._2 + result._2)
          })
      } else {
        (valueSet.entries, valueSet.entries.size)
      }

    val entries = synopsises._1.slice(page.getStart, page.getEnd).
      foldLeft(Seq[EntitySynopsis]())(valueSetToEntitySynopsis)

    new ResolvedValueSetResult(buildHeader(valueSet), entries, entries.size == synopsises._2);
  }

  private def getEntriesFromOid(oid: String): (Seq[ValueSetEntry], Int) = {
    val vs = valueSetRepository.findOne(oid)
    val includes = valueSetRepository.findOne(oid).includesValueSets
    if (includes.size == 0) {
      (vs.entries, vs.entries.size)
    } else {
      includes.foldLeft[(Seq[ValueSetEntry], Int)]((vs.entries, vs.entries.size))(
        (list, oid) => {
          val result = getEntriesFromOid(oid)
          val size = list._2 + result._2
          (list._1 ++ result._1, size)
        })
    }
  }

  private def valueSetToEntitySynopsis = (seq: Seq[EntitySynopsis], entry: ValueSetEntry) => {
    val synopsis = new EntitySynopsis()
    synopsis.setName(entry.code)

    val csName = uriResolver.idToName(entry.codeSystem, IdType.CODE_SYSTEM)
    synopsis.setNamespace(csName)

    val baseUri = uriResolver.idToBaseUri(csName)

    synopsis.setUri(baseUri + entry.code)
    synopsis.setDesignation(entry.description);

    synopsis.setHref(hrefBuilder.createEntityHref(entry))

    seq ++ Seq(synopsis)
  }: Seq[EntitySynopsis]

  private def buildHeader(valueSet: ValueSet): ResolvedValueSetHeader = {
    val header = new ResolvedValueSetHeader()

    val valueDefSetRef = new ValueSetDefinitionReference()
    val valueSetRef = new ValueSetReference(valueSet.getName)
    valueSetRef.setUri(UriUtils.oidToUri(valueSet.oid))
    valueSetRef.setHref(urlConstructor.createValueSetUrl(valueSet.getName))
    valueDefSetRef.setValueSet(valueSetRef)

    val nameAndMeaning = new NameAndMeaningReference("1")
    nameAndMeaning.setUri(UriUtils.oidToUri(valueSet.oid) + ":1")
    nameAndMeaning.setHref(urlConstructor.createValueSetDefinitionUrl(valueSet.getName, "1"))
    valueDefSetRef.setValueSetDefinition(nameAndMeaning)

    header.setResolutionOf(valueDefSetRef)

    val codeSystemVersions = valueSetRepository.findCodeSystemVersionsByName(valueSet.name).asInstanceOf[java.util.List[Array[Object]]]

    val itr = codeSystemVersions.asScala

    val versionRefs = itr.foldLeft(Seq[CodeSystemVersionReference]())(
      (seq, entry) => {
        val ref = new CodeSystemVersionReference()

        val csName = entry(0).toString
        var versionId = ObjectUtils.toString(entry(1))
        if (StringUtils.isBlank(versionId)) {
          versionId = "unknown"
        }

        val codeSystemName = uriResolver.idToName(csName, IdType.CODE_SYSTEM)
        val codeSystemUri = uriResolver.idToUri(csName, IdType.CODE_SYSTEM)
        /*
    		  val codeSystemVersionName = uriResolver.idAndVersionToVersionName(
    		        codeSystemName,
    		        versionId, 
    		        IdType.CODE_SYSTEM_VERSION) 
    		  */
        /*     
    		  val codeSystemVersionUri = uriResolver.idAndVersionToVersionUri(
    		        codeSystemName,
    		        versionId, 
    		        IdType.CODE_SYSTEM_VERSION) 
    		  */

        val cs = new CodeSystemReference()
        cs.setContent(codeSystemName)
        cs.setUri(codeSystemUri)

        val csv = new NameAndMeaningReference()
        csv.setContent(csName + "-" + versionId)
        //csv.setUri(codeSystemUri)

        ref.setCodeSystem(cs)
        ref.setVersion(csv)

        seq :+ ref
      })
    header.setResolvedUsingCodeSystem(versionRefs)

    header
  }

  def resolveDefinitionAsEntityDirectory(p1: ValueSetDefinitionReadId, p2: Set[NameOrURI], p3: NameOrURI, p4: ResolvedValueSetResolutionEntityQuery, p5: SortCriteria, p6: ResolvedReadContext, p7: Page): ResolvedValueSetResult[EntityDirectoryEntry] = null

  def resolveDefinitionAsCompleteSet(p1: ValueSetDefinitionReadId, p2: Set[NameOrURI], p3: NameOrURI, p4: ResolvedReadContext): ResolvedValueSet = null
}