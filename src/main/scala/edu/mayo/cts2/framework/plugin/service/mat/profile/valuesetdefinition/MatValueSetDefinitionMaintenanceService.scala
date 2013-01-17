package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import scala.collection.JavaConversions._
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionMaintenanceService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ValueSetChangeDescriptionRepository, ValueSetRepository, ValueSetVersionRepository, ChangeSetRepository}
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetChangeDescription, ValueSetEntry, ValueSetVersion}
import edu.mayo.cts2.framework.model.core.types.{FinalizableState, ChangeType}
import java.util.UUID
import edu.mayo.cts2.framework.model.service.exception.{ResourceIsNotOpen, ChangeSetIsNotOpen, UnknownChangeSet}
import java.util

@Component
class MatValueSetDefinitionMaintenanceService extends AbstractService with ValueSetDefinitionMaintenanceService {

  @Resource
  var versionRepo: ValueSetVersionRepository = _

  @Resource
  var changeDescRepo: ValueSetChangeDescriptionRepository = _

  @Resource
  var valueSetRepo: ValueSetRepository = _

  @Resource
  var changeSetRepo: ChangeSetRepository = _

  /* TODO: implement */
  override def updateChangeableMetadata(readId: ValueSetDefinitionReadId, request: UpdateChangeableMetadataRequest) {
//    val changeSet = changeSetRepo.findOne(request.getChangeSetUri)
//
//    if (changeSet.eq(null))
//      throw new UnknownChangeSet
//
//    if (changeSet.getFinalizableState.ne(FinalizableState.OPEN))
//      throw new ChangeSetIsNotOpen
//
//    Option(versionRepo.findOne(readId.getUri)) match {
//      case Some(version) => {
//        /* update desc */
//        val changeDesc = new ValueSetChangeDescription
//        changeDesc.setChangeType(ChangeType.METADATA)
//        changeDesc.setContainingChangeSet(changeSet.getId)
//        changeDescRepo.save(changeDesc)
//
//        version.setChangeDescription(changeDesc)
//        versionRepo.save(version)
//      }
//      case None => null
//    }
  }

  override def updateResource(resource: LocalIdValueSetDefinition) {
    val changeSet = changeSetRepo.findOne(resource.getChangeableElementGroup.getChangeDescription.getContainingChangeSet)
    if (changeSet.eq(null))
      throw new UnknownChangeSet

    if (changeSet.getFinalizableState.ne(FinalizableState.OPEN))
      throw new ChangeSetIsNotOpen

    Option(versionRepo.findOne(resource.getResource.getDocumentURI)) match {
      case Some(version) => {
        val valueSetDefinition = resource.getResource
        val version: ValueSetVersion = toValueSetVersion(valueSetDefinition)
        /* update desc */
        val changeDesc = new ValueSetChangeDescription
        changeDesc.setChangeType(ChangeType.UPDATE)
        changeDesc.setContainingChangeSet(changeSet.getId)
        changeDesc.setAuthor(valueSetDefinition.getSourceAndRole(0).getSource.getContent)
        changeDescRepo.save(changeDesc)

        version.setChangeDescription(changeDesc)
        versionRepo.save(version)
      }
      case None => null
    }
  }

  override def createResource(valueSetDefinition: ValueSetDefinition): LocalIdValueSetDefinition = {
    val changeSet = changeSetRepo.findOne(valueSetDefinition.getChangeableElementGroup.getChangeDescription.getContainingChangeSet)
    if (changeSet.eq(null))
      throw new UnknownChangeSet

    val version: ValueSetVersion = toValueSetVersion(valueSetDefinition)
    /* update desc */
    val changeDesc = new ValueSetChangeDescription
    changeDesc.setChangeType(ChangeType.CREATE)
    changeDesc.setContainingChangeSet(changeSet.getId)
    changeDesc.setAuthor(valueSetDefinition.getSourceAndRole(0).getSource.getContent)
    changeDescRepo.save(changeDesc)

    version.setChangeDescription(changeDesc)
    versionRepo.save(version)

    changeSet.addVersion(version)
    changeSetRepo.save(changeSet)

    new LocalIdValueSetDefinition(version.getId, valueSetDefinition)
  }

  override def deleteResource(valueSetDefinitionReadId: ValueSetDefinitionReadId, changeSetUri: String) {

    val changeSet = changeSetRepo.findOne(changeSetUri)
    if (changeSet.eq(null)) {
      throw new UnknownChangeSet
    }

    if (changeSet.getFinalizableState.ne(FinalizableState.OPEN)) {
      throw new ChangeSetIsNotOpen
    }

    Option(versionRepo.findOne(valueSetDefinitionReadId.getUri)) match {
      case Some(version) => {
        if (version.getState.eq(FinalizableState.FINAL)) {
          throw new ResourceIsNotOpen
        } else {
          val desc = new ValueSetChangeDescription
          desc.setContainingChangeSet(changeSetUri)
          desc.setChangeType(ChangeType.DELETE)
          desc.setAuthor(version.getValueSetDeveloper)
          changeDescRepo.save(desc)

          version.setChangeDescription(desc)
          versionRepo.save(version)
        }
      }
      case None => null
    }

  }

  private def toValueSetVersion(vsd: ValueSetDefinition): ValueSetVersion = {
    val version = new ValueSetVersion
    version.setId(vsd.getDocumentURI)
    version.valueSet = valueSetRepo.findOne(vsd.getDefinedValueSet.getContent)
    version.setValueSetDeveloper(vsd.getSourceAndRole(0).getSource.getContent)
    version.setState(FinalizableState.OPEN)

    if (vsd.getVersionTagCount > 0)
      version.setVersionId(vsd.getVersionTag(0).getContent)

    val page = new edu.mayo.cts2.framework.model.command.Page()
    page.setMaxToReturn(1000)

    val entries = versionRepo.findValueSetEntriesByValueSetVersionId(version.getId, toPageable(Option(page))).getContent

    if (versionRepo.findVersionByIdOrVersionIdAndValueSetName(version.valueSet.getOid, version.getVersionId) == null) {
      vsd.getEntry.foreach(entry => {
        var codeSystemVersion = ""
        if (entry.getCompleteCodeSystem != null && entry.getCompleteCodeSystem.getCodeSystemVersion != null && entry.getCompleteCodeSystem.getCodeSystemVersion.getVersion != null)
          Option(entry.getCompleteCodeSystem.getCodeSystemVersion.getVersion.getContent) match {
            case Some(entryVersion) => codeSystemVersion = entryVersion
            case None => codeSystemVersion = ""
          }
        entry.getEntityList.getReferencedEntity.foreach(entity => {
          val vsEntry = new ValueSetEntry
          vsEntry.setId(UUID.randomUUID.toString)
          vsEntry.setCodeSystem(entity.getNamespace)
          vsEntry.setCodeSystemVersion(codeSystemVersion)
          vsEntry.setValueSetVersion(version)
          vsEntry.setCode(entity.getName)
          if (!entries.contains(vsEntry))
            version.addEntry(vsEntry)
        })
      })

    }
    version
  }

}
