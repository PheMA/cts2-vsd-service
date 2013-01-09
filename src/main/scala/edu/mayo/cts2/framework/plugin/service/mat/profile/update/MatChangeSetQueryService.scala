package edu.mayo.cts2.framework.plugin.service.mat.profile.update

import scala.collection.JavaConversions._
import scala.collection.JavaConversions.iterableAsScalaIterable
import edu.mayo.cts2.framework.service.profile.update.{ChangeSetQueryExtension, ChangeSetQuery}
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.model.service.core.Query
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.service.command.restriction.ChangeSetQueryExtensionRestrictions
import edu.mayo.cts2.framework.model.core._
import edu.mayo.cts2.framework.service.meta.{StandardModelAttributeReference, StandardMatchAlgorithmReference}
import edu.mayo.cts2.framework.plugin.service.mat.repository.ChangeSetRepository
import javax.annotation.Resource
import edu.mayo.cts2.framework.model.command.Page
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.model.updates.ChangeSetDirectoryEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetChange}

@Component
class MatChangeSetQueryService extends AbstractService with ChangeSetQuery with ChangeSetQueryExtension {

  val CREATOR_PROP: String = "creator"
  val VALUESET_OID_PROP: String = "valuesetoid"

  @Resource
  var changeSetRepository: ChangeSetRepository = _

  def getQuery = null

  def getFilterComponent = null

  def getChangeSetQueryExtensionRestrictions = null

  @Transactional
  def getResourceSummaries(query: ChangeSetQuery,
                           sort: SortCriteria,
                           page: Page): DirectoryResult[ChangeSetDirectoryEntry] = {
    var changeSets: org.springframework.data.domain.Page[ValueSetChange] = null
    val filters = query.getFilterComponent

    var creator: String = null
    var valuesetoid: String = null

    filters.foreach((filter: ResolvedFilter) =>
      if (filter.getPropertyReference.getReferenceTarget.getName.equals("creator")) {
        creator = filter.getMatchValue
      }
    else if (filter.getPropertyReference.getReferenceTarget.getName.equals("valuesetoid")) {
        valuesetoid = filter.getMatchValue
      }
    )

//    if (valuesetoid != null && creator != null) {
//      changeSets = changeSetRepository.findChangeSetsByValueSetIdAndCreator(valuesetoid, creator, toPageable(Option(page)))
//    }
//    else if (creator != null) {
//      changeSets = changeSetRepository.findChangeSetsByCreator(creator, toPageable(Option(page)))
//    }
//    else if (valuesetoid != null) {
//      changeSets = changeSetRepository.findChangeSetsByValueSetId(valuesetoid, toPageable(Option(page)))
//    }
//    else {
      changeSets = changeSetRepository.findAll(toPageable(Option(page)))
//    }

    val entries = changeSets.foldLeft(Seq[ChangeSetDirectoryEntry]())(transformChangeSet)
    val totalElements = changeSets.getTotalElements

    new DirectoryResult(entries, entries.size == totalElements)
  }

  def transformChangeSet = (seq:Seq[ChangeSetDirectoryEntry], changeSet:ValueSetChange) => {
    seq :+ transformSingleChangeSet(changeSet)
  }:Seq[ChangeSetDirectoryEntry]

  def transformSingleChangeSet = (changeSet: ValueSetChange) => {
    val entry = new ChangeSetDirectoryEntry
    entry.setChangeSetURI(changeSet.getId)
    entry.setResourceName(changeSet.getId)
    entry.setCreationDate(changeSet.getDate)
    entry.setState(changeSet.getState)

    val group = new ChangeSetElementGroup
    val sourceRef = new SourceReference
    sourceRef.setContent(changeSet.getAuthor)
    group.setCreator(sourceRef)
    entry.setChangeSetElementGroup(group)
    entry
  }: ChangeSetDirectoryEntry

  def count(query: Query,
            filter: java.util.Set[ResolvedFilter],
            restrictions: ChangeSetQueryExtensionRestrictions) = 0

  def getSupportedMatchAlgorithms: java.util.Set[MatchAlgorithmReference] = {
    val set = new java.util.HashSet[MatchAlgorithmReference]()
    set.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference)
    set.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference)
    set.add(StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference)
    set
  }

  def getSupportedSearchReferences: java.util.Set[_ <: PropertyReference] = {
    val set = new java.util.HashSet[PropertyReference]()
    set.add(StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference)
    set.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference)

    set
  }

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = {
    new java.util.HashSet[PropertyReference]()
  }

  def getKnownProperties: java.util.Set[PredicateReference] = {
    new java.util.HashSet[PredicateReference]()
  }
}
