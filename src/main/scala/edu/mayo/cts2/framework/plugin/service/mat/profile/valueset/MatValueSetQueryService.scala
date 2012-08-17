package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import scala.collection.JavaConversions.iterableAsScalaIterable
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core.PredicateReference
import edu.mayo.cts2.framework.model.core.SortCriteria
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService
import javax.annotation.Resource
import javax.persistence.Entity
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.model.core.PropertyReference
import org.springframework.context.annotation.ScopedProxyMode
import scala.collection.JavaConversions._

@Component
class MatValueSetQueryService
  extends AbstractService
  with ValueSetQueryService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

  def getSupportedMatchAlgorithms: java.util.Set[_ <: MatchAlgorithmReference] = null

  def getSupportedSearchReferences: java.util.Set[_ <: PropertyReference] = null

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = null

  def getKnownProperties: java.util.Set[PredicateReference] = null

  @Transactional
  def getResourceSummaries(query: ValueSetQuery, sort: SortCriteria, page: Page = new Page()): DirectoryResult[ValueSetCatalogEntrySummary] = {
    val valueSets = valueSetRepository.findAll(toPageable(Option(page)))

    val entries = valueSets.foldLeft(Seq[ValueSetCatalogEntrySummary]())(transformValueSet)
    
    new DirectoryResult(entries, valueSets.getSize == valueSets.getTotalElements)
  }
  
  def transformValueSet = (seq:Seq[ValueSetCatalogEntrySummary], valueSet:ValueSet) => {
    val summary = new ValueSetCatalogEntrySummary()
    summary.setValueSetName(valueSet.name)
    summary.setAbout(valueSet.oid)
    summary.setFormalName(valueSet.formalName)
    
    seq ++ Seq(summary)
  }:Seq[ValueSetCatalogEntrySummary]

  def getResourceList(p1: ValueSetQuery, p2: SortCriteria, p3: Page): DirectoryResult[ValueSetCatalogEntry] = null

  def count(p1: ValueSetQuery): Int = 0

}