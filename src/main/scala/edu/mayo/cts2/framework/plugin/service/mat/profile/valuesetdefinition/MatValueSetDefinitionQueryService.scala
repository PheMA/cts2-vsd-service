package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import scala.collection.JavaConversions._
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core._
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.model.valuesetdefinition.{ValueSetDefinitionListEntry, ValueSetDefinitionDirectoryEntry}
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractQueryService
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQuery
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionQueryService
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
import edu.mayo.cts2.framework.plugin.service.mat.profile.valueset.MatValueSetUtils
import java.util.Collections
import edu.mayo.cts2.framework.model.util.ModelUtils

@Component
class MatValueSetDefinitionQueryService
  extends AbstractQueryService
  with ValueSetDefinitionQueryService {

  @Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  def getSupportedMatchAlgorithms: java.util.Set[_ <: MatchAlgorithmReference] = { 
    val set = new java.util.HashSet[MatchAlgorithmReference]() 
    set.add(StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference)
    set.add(StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference)
    set
  }

  def getSupportedSearchReferences: java.util.Set[_ <: ComponentReference] = {
    val set = new java.util.HashSet[ComponentReference]()
    set.add(StandardModelAttributeReference.RESOURCE_NAME.getComponentReference)
    set.add(StandardModelAttributeReference.RESOURCE_SYNOPSIS.getComponentReference)
    set
  }

  @Transactional
  def getResourceSummaries(query: ValueSetDefinitionQuery, sort: SortCriteria, page: Page = new Page()): DirectoryResult[ValueSetDefinitionDirectoryEntry] = {
    val fn =
    if(query == null || query.getRestrictions == null || query.getRestrictions.getValueSet == null){
      valueSetVersionRepository.findAllLatest(_:Pageable)
    } else {
      def vs = query.getRestrictions.getValueSet

      def name = if(vs.getName != null) {
        vs.getName
      } else {
        vs.getUri
      }

      valueSetVersionRepository.findByValueSetName(name, _:Pageable)
    }

    val valueSets = fn(toPageable(Option(page)))

    val entries = valueSets.foldLeft(Seq[ValueSetDefinitionDirectoryEntry]())(transformValueSetVersion)

    val totalElements = valueSets.getTotalElements
    
    new DirectoryResult(entries, entries.size == totalElements)
  }

  def transformValueSetVersion = (seq:Seq[ValueSetDefinitionDirectoryEntry], valueSetVersion:ValueSetVersion) => {
    val valueSetDefName = MatValueSetUtils.getValueSetDefName(valueSetVersion)
    
    val summary = new ValueSetDefinitionDirectoryEntry()
    summary.setResourceName(valueSetDefName)
    summary.setAbout(valueSetVersion.valueSet.uri + "/version/" + valueSetVersion.version)
    summary.setDocumentURI(valueSetVersion.documentUri)
    summary.setFormalName(valueSetVersion.valueSet.formalName)
    summary.setHref(urlConstructor.createValueSetDefinitionUrl(valueSetVersion.valueSet.name, valueSetDefName))
    summary.setDefinedValueSet(MatValueSetUtils.buildValueSetReference(valueSetVersion, urlConstructor))
    summary.setVersionTag(Collections.singletonList(new VersionTagReference(valueSetVersion.version)))

    /* TODO: Add to expansion node when available */
    /* The note and changeSetURI do not belong in the resource synopsis, this is a temporary place for them. */
    val sb = new StringBuilder("<note>" + Option(valueSetVersion.getNotes).getOrElse("") + "</note>")
    sb.append("<changeSetUri>" + Option(valueSetVersion.getChangeSetUri).getOrElse("") + "</changeSetUri>")

    val syn = new EntryDescription
    syn.setValue(ModelUtils.toTsAnyType(sb.toString()))
    summary.setResourceSynopsis(syn)

    seq ++ Seq(summary)
  }:Seq[ValueSetDefinitionDirectoryEntry]

  def getResourceList(query: ValueSetDefinitionQuery, sortCriteria: SortCriteria, page: Page): DirectoryResult[ValueSetDefinitionListEntry] =
    new DirectoryResult[ValueSetDefinitionListEntry](List.empty[ValueSetDefinitionListEntry], true)

  def count(p1: ValueSetDefinitionQuery): Int = 0

}