package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import java.lang.Override
import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core._
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import javax.annotation.Resource
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry
import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList
import edu.mayo.cts2.framework.model.core.types.SetOperator
import edu.mayo.cts2.framework.plugin.service.mat.uri.IdType
import edu.mayo.cts2.framework.plugin.service.mat.profile.valueset.MatValueSetUtils
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils
import edu.mayo.cts2.framework.model.valuesetdefinition.CompleteValueSetReference
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
import org.apache.commons.lang.StringUtils
import edu.mayo.cts2.framework.model.core.types.EntryState
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import org.springframework.data.domain.PageRequest

@Component
class MatValueSetDefinitionReadService extends AbstractService with ValueSetDefinitionReadService {

  val SIZE_LIMIT = 100;
  
  @Resource
  var valueSetRepository: ValueSetRepository = _
  
  @Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  /**
   * This is incomplete... this is only here to map the 'CURRENT' tag to a CodeSystemVersionName.
   */
  @Override
  def readByTag(
    valueSet: NameOrURI,
    tag: VersionTagReference, readContext: ResolvedReadContext): LocalIdValueSetDefinition = {

    if (tag.getContent() == null || !tag.getContent().equals("CURRENT")) {
      throw new RuntimeException("Only 'CURRENT' tag is supported")
    }

    val valueSetName = valueSet.getName()

    val versionId = valueSetRepository.findCurrentVersionIdByName(valueSetName)

    if (versionId != null) {
      new LocalIdValueSetDefinition(versionId, null)
    } else {
      null
    }
  }

  @Override
  def existsByTag(valueSet: NameOrURI,
    tag: VersionTagReference, readContext: ResolvedReadContext): Boolean = {
    readByTag(valueSet, tag, readContext) != null
  }

  @Override
  @Transactional
  def read(
    identifier: ValueSetDefinitionReadId,
    readContext: ResolvedReadContext): LocalIdValueSetDefinition = {
    val valueSetName = identifier.getValueSet.getName

    val valueSetVersion =
      valueSetVersionRepository.findVersionByIdOrVersionIdAndValueSetName(valueSetName, identifier.getName)

    if (valueSetVersion != null) {
      val valueSetDef = valueSetVersionToDefinition(valueSetVersion)

      new LocalIdValueSetDefinition(valueSetVersion.getId, valueSetDef)
    } else {
      null
    }
  }

  def valueSetVersionToDefinition(valueSetVersion: ValueSetVersion): ValueSetDefinition = {
    val valueSetDef = new ValueSetDefinition()
    valueSetDef.setAbout(UriUtils.oidToUri(valueSetVersion.valueSet.oid))
    valueSetDef.setDocumentURI(UriUtils.uuidToUri(valueSetVersion.id))
    valueSetDef.setSourceAndNotation(buildSourceAndNotation())
    valueSetDef.setDefinedValueSet(
      MatValueSetUtils.buildValueSetReference(valueSetVersion.valueSet, urlConstructor))

    valueSetDef.setOfficialResourceVersionId(valueSetVersion.versionId)

    Option(valueSetVersion.getChangeDescription) match {
      case Some(changeDesc) => valueSetDef.setOfficialReleaseDate(changeDesc.getDate.getTime)
      case None => /* do nothing */
    }

    val ids = MatValueSetUtils.getIncludedVersionIds(valueSetVersion, valueSetRepository)
    
    val entries = valueSetVersionRepository.
    	findValueSetEntriesByValueSetVersionIds(ids, new PageRequest(0,SIZE_LIMIT)).getContent
    
    val list = entries.foldLeft(new SpecificEntityList())((list, entry) => {
      val entity = new URIAndEntityName()
      val prefix = uriResolver.idToName(entry.codeSystem, IdType.CODE_SYSTEM)
      entity.setNamespace(prefix)
      entity.setName(entry.code)

      val baseUri = uriResolver.idToBaseUri(entry.codeSystem)

      entity.setUri(baseUri + entry.code)

      list.addReferencedEntity(entity)

      list
    })

    if (list.getReferencedEntityCount > 0) {
      val vsdEntry = new ValueSetDefinitionEntry()
      vsdEntry.setEntryOrder(1)
      vsdEntry.setOperator(SetOperator.UNION)
      vsdEntry.setEntityList(list)

      valueSetDef.addEntry(vsdEntry)
    }

    val includesValueSets = valueSetVersion.includesValueSets.foldLeft(Seq[ValueSetDefinitionEntry]())(
      (seq, oid) => {
        val vsdEntry = new ValueSetDefinitionEntry()
        vsdEntry.setEntryOrder(1)
        vsdEntry.setOperator(SetOperator.UNION)

        val valueSet = valueSetRepository.findOne(oid)

        val vsRef = new CompleteValueSetReference()
        vsRef.setValueSet(MatValueSetUtils.buildValueSetReference(valueSet, urlConstructor))

        vsdEntry.setCompleteValueSet(vsRef)

        seq :+ vsdEntry
      })

    includesValueSets.foreach(valueSetDef.addEntry(_))

    if (valueSetVersion.getValueSetDeveloper != null) {
      val snr: SourceAndRoleReference = new SourceAndRoleReference
      snr.setSource(new SourceReference(valueSetVersion.getValueSetDeveloper))
      snr.setRole(MatValueSetUtils.creatorRole)
      valueSetDef.addSourceAndRole(snr)
    }
    else {
      valueSetDef.addSourceAndRole(MatValueSetUtils.sourceAndRole)
    }

    valueSetDef.setEntryState(
      if (StringUtils.equalsIgnoreCase(valueSetVersion.getStatus, "inactive")) {
        EntryState.INACTIVE
      } else {
        EntryState.ACTIVE
      })

    valueSetDef.setState(valueSetVersion.getState)

    valueSetDef
  }

  private def buildSourceAndNotation(): SourceAndNotation = {
    val sourceAndNotation = new SourceAndNotation()
    sourceAndNotation.setSourceAndNotationDescription("MAT Authoring Tool Output Zip.")

    sourceAndNotation
  }

  @Override
  def exists(identifier: ValueSetDefinitionReadId, readContext: ResolvedReadContext): Boolean = {
    throw new UnsupportedOperationException()
  }

  def getSupportedTags: java.util.List[VersionTagReference] =
    List[VersionTagReference](CURRENT_TAG)

}