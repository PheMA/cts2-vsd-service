package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core.{ComponentReference, MatchAlgorithmReference, SortCriteria}
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.model.valueset.{ValueSetCatalogEntryListEntry, ValueSetCatalogEntrySummary}
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractQueryService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService
import javax.annotation.Resource

@Component
class MatValueSetQueryService
  extends AbstractQueryService
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

  def getSupportedSearchReferences: java.util.Set[ComponentReference] = {
    Set[ComponentReference](StandardModelAttributeReference.RESOURCE_NAME.getComponentReference,
      StandardModelAttributeReference.RESOURCE_SYNOPSIS.getComponentReference)
  }

  @Transactional
  def getResourceSummaries(query: ValueSetQuery, sort: SortCriteria, requestedPage: Page = new Page()): DirectoryResult[ValueSetCatalogEntrySummary] = {
    val page = if (requestedPage == null) new Page() else requestedPage

    // TODO Implement
//    var directoryBuilder: DirectoryBuilder[ValueSetCatalogEntrySummary] = new HibernateCriteriaDirectoryBuilder[ValueSetCatalogEntrySummary, ValueSet](
//      classOf[ValueSet],
//      entityManager,
//      transformSingleValueSet,
//      getSupportedMatchAlgorithms,
//      getSupportedSearchReferences)
//
//    if (query != null) {
//      directoryBuilder = directoryBuilder.restrict(query.getFilterComponent)
//    }
//
//    directoryBuilder.
//      addStart(page.getStart).
//      addMaxToReturn(page.getMaxToReturn).
//      resolve

    new DirectoryResult[ValueSetCatalogEntrySummary](List.empty[ValueSetCatalogEntrySummary], true)
  }

  def transformSingleValueSet = (valueSet: ValueSet) => {
    val summary = new ValueSetCatalogEntrySummary()
    summary.setValueSetName(valueSet.name)
    summary.setAbout(valueSet.uri)
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

  def getResourceList(p1: ValueSetQuery, p2: SortCriteria, p3: Page): DirectoryResult[ValueSetCatalogEntryListEntry] =
    new DirectoryResult[ValueSetCatalogEntryListEntry](List.empty[ValueSetCatalogEntryListEntry], true)

  def count(p1: ValueSetQuery): Int = 0

}