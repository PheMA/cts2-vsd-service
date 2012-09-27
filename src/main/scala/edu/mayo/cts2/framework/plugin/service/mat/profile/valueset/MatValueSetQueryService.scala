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
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import org.springframework.data.domain.Pageable
import org.apache.commons.collections.CollectionUtils
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils
import org.apache.commons.lang.StringUtils
import edu.mayo.cts2.framework.model.core.types.TargetReferenceType
import edu.mayo.cts2.framework.model.core.URIAndEntityName

@Component
class MatValueSetQueryService
  extends AbstractService
  with ValueSetQueryService {

  val NQF_NUMBER_PROP:String = "nqfnumber"
  val EMEASURE_ID_PROP:String = "emeasureid"
  
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
    set.add(createPropertyReference(NQF_NUMBER_PROP))
    set.add(createPropertyReference(EMEASURE_ID_PROP))
    
    set
  }
  
  def createPropertyReference(name:String): PropertyReference = { 
    val ref = new PropertyReference()
    ref.setReferenceType(TargetReferenceType.PROPERTY);
    ref.setReferenceTarget(new URIAndEntityName())
    ref.getReferenceTarget.setName(name)
    
    ref
  }

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = { new java.util.HashSet[PropertyReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  @Transactional
  def getResourceSummaries(query: ValueSetQuery, sort: SortCriteria, page: Page = new Page()): DirectoryResult[ValueSetCatalogEntrySummary] = {
    val valueSets = getValueSetQueryFunction(query)(toPageable(Option(page)))

    val entries = valueSets.foldLeft(Seq[ValueSetCatalogEntrySummary]())(transformValueSet)

    val totalElements = valueSets.getTotalElements
    
    new DirectoryResult(entries, entries.size == totalElements)
  }
  
  def getValueSetQueryFunction(query: ValueSetQuery) = {
    if(query == null || CollectionUtils.isEmpty(query.getFilterComponent) ){
    	valueSetRepository.findAll _ 
    } else {
      val filters = query.getFilterComponent
      if(filters.size()	> 1){
        throw new IllegalStateException("Only one Filter per request is allowed.")
      } else {
        val filter = filters.iterator.next
        
        filterToQueryFunction(filter)
      }
    }
  }: (Pageable) => org.springframework.data.domain.Page[ValueSet]
  
  def filterToQueryFunction(filter:ResolvedFilter) = {
    val propertyReference = filter.getPropertyReference
    
    val resourceName = StandardModelAttributeReference.RESOURCE_NAME.getPropertyReference.getReferenceTarget.getName
    val resourceSynopsis = StandardModelAttributeReference.RESOURCE_SYNOPSIS.getPropertyReference.getReferenceTarget.getName
   
    val matchAlgorithm = filter.getMatchAlgorithmReference.getContent
    val contains = StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference.getContent
    val startsWith = StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference.getContent

    var fn = propertyReference.getReferenceTarget.getName match {
      case `resourceName` => valueSetRepository.findByNameLikeIgnoreCase(_:String, _:Pageable)
      case `resourceSynopsis` => valueSetRepository.findByAnyLikeIgnoreCase(_:String, _:Pageable)
      case `NQF_NUMBER_PROP` => valueSetRepository.findByPropertyLikeIgnoreCase("NQF Number", _:String, _:Pageable)
      case `EMEASURE_ID_PROP` => valueSetRepository.findByPropertyLikeIgnoreCase("eMeasure Identifier", _:String, _:Pageable)
    }
    
    val matchValue = filter.getMatchValue
    
    matchAlgorithm match {
      case `contains` => fn('%'+matchValue+'%', _:Pageable)
      case `startsWith` => fn(matchValue+'%', _:Pageable)
    }

  }: (Pageable) => org.springframework.data.domain.Page[ValueSet]
  
  
  def transformValueSet = (seq:Seq[ValueSetCatalogEntrySummary], valueSet:ValueSet) => {
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
    
    seq ++ Seq(summary)
  }:Seq[ValueSetCatalogEntrySummary]

  def getResourceList(p1: ValueSetQuery, p2: SortCriteria, p3: Page): DirectoryResult[ValueSetCatalogEntry] = null

  def count(p1: ValueSetQuery): Int = 0

}