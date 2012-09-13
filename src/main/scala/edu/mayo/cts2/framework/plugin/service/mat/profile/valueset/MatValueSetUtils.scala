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
import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.core.url.UrlConstructor
import edu.mayo.cts2.framework.model.core.SourceReference
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference
import edu.mayo.cts2.framework.model.core.RoleReference
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils

object MatValueSetUtils {

  def currentDefintion(valueSet: ValueSetCatalogEntry, urlConstructor: UrlConstructor) = {
    doGetCurrentDefintion(valueSet.getValueSetName, valueSet.getAbout, urlConstructor)
  }: ValueSetDefinitionReference

  def currentDefintion(valueSet: ValueSetCatalogEntrySummary, urlConstructor: UrlConstructor) = {
    doGetCurrentDefintion(valueSet.getValueSetName, valueSet.getAbout, urlConstructor)
  }: ValueSetDefinitionReference

  def buildValueSetReference(valueSet: ValueSet, urlConstructor: UrlConstructor): ValueSetReference = {
    val ref = new ValueSetReference()
    ref.setContent(valueSet.name)
    ref.setUri(UriUtils.oidToUri(valueSet.oid))
    ref.setHref(urlConstructor.createValueSetUrl(valueSet.name))

    ref
  }

  private def doGetCurrentDefintion(name: String, about: String, urlConstructor: UrlConstructor) = {
    val currentDefinition = new ValueSetDefinitionReference()

    val valueSetRef = new ValueSetReference(name)
    valueSetRef.setUri(about)
    valueSetRef.setHref(urlConstructor.createValueSetUrl(name))
    currentDefinition.setValueSet(valueSetRef)

    val valueSetDefRef = new NameAndMeaningReference("1")
    valueSetDefRef.setUri(about + ":1")
    valueSetDefRef.setHref(urlConstructor.createValueSetDefinitionUrl(name, "1"))
    currentDefinition.setValueSetDefinition(valueSetDefRef)

    currentDefinition
  }: ValueSetDefinitionReference

  def sourceAndRole = {
    val sourceAndRoleRef = new SourceAndRoleReference()

    val roleRef = new RoleReference()
    roleRef.setContent("creator")
    roleRef.setUri("http://purl.org/dc/elements/1.1/creator")
    sourceAndRoleRef.setRole(roleRef)

    val sourceRef = new SourceReference("National Committee for Quality Assurance")
    sourceAndRoleRef.setSource(sourceRef)

    sourceAndRoleRef
  }
}