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
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
import edu.mayo.cts2.framework.plugin.service.mat.profile.valueset.MatValueSetUtils

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
    
    set
  }

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = { new java.util.HashSet[PropertyReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  @Transactional
  def getResourceSummaries(query: ValueSetDefinitionQuery, sort: SortCriteria, page: Page = new Page()): DirectoryResult[ValueSetDefinitionDirectoryEntry] = {
    val fn =
    if(query == null || query.getRestrictions == null || query.getRestrictions.getValueSet == null){
      valueSetVersionRepository.findAll(_:Pageable)
    } else {
      val name = query.getRestrictions.getValueSet.getName
      valueSetVersionRepository.findByValueSetName(name, _:Pageable)
    }
    
    val valueSets = fn(toPageable(Option(page)))

    val entries = valueSets.foldLeft(Seq[ValueSetDefinitionDirectoryEntry]())(transformValueSetVersion)

    val totalElements = valueSets.getTotalElements
    
    new DirectoryResult(entries, entries.size == totalElements)
  }

  def transformValueSetVersion = (seq:Seq[ValueSetDefinitionDirectoryEntry], valueSetVersion:ValueSetVersion) => {
    val summary = new ValueSetDefinitionDirectoryEntry()
    summary.setResourceName(valueSetVersion.id)
    summary.setAbout(UriUtils.oidToUri(valueSetVersion.valueSet.oid))
    summary.setDocumentURI(UriUtils.uuidToUri(valueSetVersion.id))
    summary.setFormalName(valueSetVersion.valueSet.formalName)
    summary.setHref(urlConstructor.createValueSetDefinitionUrl(valueSetVersion.valueSet.name, valueSetVersion.id))

    summary.setDefinedValueSet(MatValueSetUtils.buildValueSetReference(valueSetVersion, urlConstructor))
    
    seq ++ Seq(summary)
  }:Seq[ValueSetDefinitionDirectoryEntry]

  def getResourceList(p1: ValueSetDefinitionQuery, p2: SortCriteria, p3: Page): DirectoryResult[ValueSetDefinition] = null

  def count(p1: ValueSetDefinitionQuery): Int = 0

}