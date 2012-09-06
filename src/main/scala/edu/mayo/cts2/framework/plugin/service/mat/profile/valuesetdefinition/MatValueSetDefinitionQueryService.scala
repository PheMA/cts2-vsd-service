package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import scala.collection.JavaConversions._
import scala.collection.JavaConversions.iterableAsScalaIterable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core.MatchAlgorithmReference
import edu.mayo.cts2.framework.model.core.PredicateReference
import edu.mayo.cts2.framework.model.core.PropertyReference
import edu.mayo.cts2.framework.model.core.SortCriteria
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
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils

@Component
class MatValueSetDefinitionQueryService
  extends AbstractService
  with ValueSetDefinitionQueryService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

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
    
    set
  }

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = { new java.util.HashSet[PropertyReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  @Transactional
  def getResourceSummaries(query: ValueSetDefinitionQuery, sort: SortCriteria, page: Page = new Page()): DirectoryResult[ValueSetDefinitionDirectoryEntry] = {
    val fn =
    if(query == null || query.getRestrictions == null || query.getRestrictions.getValueSet == null){
      valueSetRepository.findAll(_:Pageable)
    } else {
      val name = query.getRestrictions.getValueSet.getName
      valueSetRepository.findByNameLikeIgnoreCase(name, _:Pageable)
    }
    
    val valueSets = fn(toPageable(Option(page)))

    val entries = valueSets.foldLeft(Seq[ValueSetDefinitionDirectoryEntry]())(transformValueSet)

    val totalElements = valueSets.getTotalElements
    
    new DirectoryResult(entries, entries.size == totalElements)
  }

  def transformValueSet = (seq:Seq[ValueSetDefinitionDirectoryEntry], valueSet:ValueSet) => {
    val summary = new ValueSetDefinitionDirectoryEntry()
    summary.setResourceName("1")
    summary.setAbout(UriUtils.oidToUri(valueSet.oid))
    summary.setDocumentURI(summary.getAbout + ":1")
    summary.setFormalName(valueSet.formalName)
    summary.setHref(urlConstructor.createValueSetDefinitionUrl(valueSet.name, "1"))
    
    val valueSetRef = new ValueSetReference(valueSet.name)
    valueSetRef.setUri(UriUtils.oidToUri(valueSet.oid))
    valueSetRef.setHref(urlConstructor.createValueSetUrl(valueSet.name))

    summary.setDefinedValueSet(valueSetRef)
    
    seq ++ Seq(summary)
  }:Seq[ValueSetDefinitionDirectoryEntry]

  def getResourceList(p1: ValueSetDefinitionQuery, p2: SortCriteria, p3: Page): DirectoryResult[ValueSetDefinition] = null

  def count(p1: ValueSetDefinitionQuery): Int = 0

}