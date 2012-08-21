package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import java.util.Set
import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
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
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResolutionEntityQuery
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ResolvedValueSetResult
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.namespace.NamespaceResolutionService
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import org.springframework.transaction.annotation.Transactional

@Component
class MatValueSetDefinitionResolutionService extends AbstractService with ValueSetDefinitionResolutionService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

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

    val valueSetName = id.getName()

    val valueSet = valueSetRepository.findOneByName(valueSetName)

    if (valueSet == null) {
      return null
    }

    val entries = valueSet.entries.slice(page.getStart, page.getEnd).
      foldLeft(Seq[EntitySynopsis]())(valueSetToEntitySynopsis)

    new ResolvedValueSetResult(buildHeader(valueSetName), entries, true);
  }

  private def valueSetToEntitySynopsis = (seq: Seq[EntitySynopsis], entry: ValueSetEntry) => {
    val synopsis = new EntitySynopsis()
    synopsis.setName(entry.code)
    synopsis.setNamespace(entry.codeSystem)
    synopsis.setUri("TODO")
    synopsis.setDesignation(entry.description);
    
    seq ++ Seq(synopsis)
  }: Seq[EntitySynopsis]

  private def buildHeader(valueSetName: String): ResolvedValueSetHeader = {
    val header = new ResolvedValueSetHeader()

    val valueDefSetRef = new ValueSetDefinitionReference()
    valueDefSetRef.setValueSet(new ValueSetReference(valueSetName))
    valueDefSetRef.setValueSetDefinition(new NameAndMeaningReference(valueSetName))
    header.setResolutionOf(valueDefSetRef)

    header
  }

  def resolveDefinitionAsEntityDirectory(p1: ValueSetDefinitionReadId, p2: Set[NameOrURI], p3: NameOrURI, p4: ResolvedValueSetResolutionEntityQuery, p5: SortCriteria, p6: ResolvedReadContext, p7: Page): ResolvedValueSetResult[EntityDirectoryEntry] = null

  def resolveDefinitionAsCompleteSet(p1: ValueSetDefinitionReadId, p2: Set[NameOrURI], p3: NameOrURI, p4: ResolvedReadContext): ResolvedValueSet = null
}