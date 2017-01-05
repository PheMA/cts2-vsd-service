package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import org.springframework.transaction.annotation.Transactional

import scala.collection.JavaConversions._
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionMaintenanceService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ValueSetRepository, ValueSetVersionRepository, ChangeSetRepository}
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetEntry, ValueSetVersion}
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, FinalizableState, ChangeType}
import edu.mayo.cts2.framework.model.service.exception.{UnknownValueSet, ResourceIsNotOpen, ChangeSetIsNotOpen, UnknownChangeSet}
import java.util
import util.{Calendar, UUID}
import edu.mayo.cts2.framework.model.core.URIAndEntityName

@Component
class MatValueSetDefinitionMaintenanceService extends AbstractService with ValueSetDefinitionMaintenanceService {

  @Resource
  var versionRepo: ValueSetVersionRepository = _

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

  @Transactional
  override def updateResource(resource: LocalIdValueSetDefinition) {
    val changeSet = changeSetRepo.findOne(resource.getChangeableElementGroup.getChangeDescription.getContainingChangeSet)
    if (changeSet.eq(null))
      throw new UnknownChangeSet

    if (changeSet.getState.ne(FinalizableState.OPEN))
      throw new ChangeSetIsNotOpen

    val valueSetName = resource.getResource.getDefinedValueSet.getContent

    Option(versionRepo.findByValueSetNameAndValueSetVersion(valueSetName, resource.getLocalID)) match {
      case Some(origVersion) => {
        val updatedVersion: ValueSetVersion = toValueSetVersion(resource.getResource)
        updatedVersion.setDocumentUri(UUID.randomUUID.toString)
        updatedVersion.setVersion(origVersion.getVersion)
        updatedVersion.setChangeType(ChangeType.UPDATE)
        updatedVersion.setChangeSetUri(changeSet.getChangeSetUri)
        updatedVersion.setPrevChangeSetUri(origVersion.getChangeSetUri)
        updatedVersion.setRevisionDate(Calendar.getInstance)
        updatedVersion.setChangeCommitted(ChangeCommitted.COMMITTED)

        origVersion.setSuccessor(updatedVersion.getDocumentUri)

        changeSet.addVersion(updatedVersion)

        versionRepo.save(origVersion)
        versionRepo.save(updatedVersion)
      }
      case None => null
    }
    changeSetRepo.save(changeSet)
  }

  override def createResource(valueSetDefinition: ValueSetDefinition): LocalIdValueSetDefinition = {
    val changeSet = changeSetRepo.findOne(valueSetDefinition.getChangeableElementGroup.getChangeDescription.getContainingChangeSet)
    if (changeSet.eq(null))
      throw new UnknownChangeSet

    if (changeSet.getState.ne(FinalizableState.OPEN))
      throw new ChangeSetIsNotOpen

    val valueSet = valueSetRepo.findOne(valueSetDefinition.getDefinedValueSet.getContent)
    if (valueSet.eq(null))
      throw new UnknownValueSet

    val version: ValueSetVersion = toValueSetVersion(valueSetDefinition)
    version.setChangeType(ChangeType.CREATE)
    version.setChangeCommitted(ChangeCommitted.COMMITTED)
    version.setChangeSetUri(changeSet.getChangeSetUri)
    valueSet.addVersion(version)
    changeSet.addVersion(version)
    versionRepo.save(version)
    changeSetRepo.save(changeSet)
    valueSetRepo.save(valueSet)
    new LocalIdValueSetDefinition(version.getVersion, valueSetDefinition)
  }

  /* TODO: implement */
  override def deleteResource(valueSetDefinitionReadId: ValueSetDefinitionReadId, changeSetUri: String) {
    throw new RuntimeException

    val changeSet = changeSetRepo.findOne(changeSetUri)
    if (changeSet.eq(null)) {
      throw new UnknownChangeSet
    }

    if (changeSet.getState.ne(FinalizableState.OPEN)) {
      throw new ChangeSetIsNotOpen
    }

    Option(versionRepo.findOne(valueSetDefinitionReadId.getUri)) match {
      case Some(version) => {
        if (version.getState.eq(FinalizableState.FINAL)) {
          throw new ResourceIsNotOpen
        } else {
//          val desc = new ValueSetChangeDescription
//          desc.setContainingChangeSet(changeSetUri)
//          desc.setChangeType(ChangeType.DELETE)
//          desc.setAuthor(version.getCreator)
//          changeDescRepo.save(desc)
//
//          version.setChangeDescription(desc)
          versionRepo.save(version)
        }
      }
      case None => null
    }
    changeSetRepo.save(changeSet)
  }

  private def toValueSetVersion(vsd: ValueSetDefinition): ValueSetVersion = {
    val version = new ValueSetVersion
    version.setDocumentUri(vsd.getDocumentURI)
    version.setValueSet(valueSetRepo.findOne(vsd.getDefinedValueSet.getContent))
    version.setCreator(Option(vsd.getSourceAndRole(0)).map(_.getSource.getContent).getOrElse(""))
    version.setState(vsd.getState)
    version.setChangeSetUri(vsd.getChangeableElementGroup.getChangeDescription.getContainingChangeSet)
    if (vsd.getNoteCount > 0)
      version.setNotes(Option(vsd.getNote(0)).map(_.getValue.getContent).getOrElse(""))

    if (vsd.getVersionTagCount > 0)
      version.setVersion(vsd.getVersionTag(0).getContent)

    vsd.getEntry.foreach(entry => {
      entry.getEntityList.getReferencedEntity.foreach(entity => {
        val vsEntry = new ValueSetEntry
        vsEntry.setId(UUID.randomUUID.toString)
        vsEntry.setCodeSystem(entity.getNamespace)
        vsEntry.setCodeSystemVersion(getCodeSystemVersion(vsd, entity))
        vsEntry.setValueSetVersion(version)
        vsEntry.setCode(entity.getName)
        vsEntry.setDescription(entity.getDesignation)
        version.addEntry(vsEntry)
      })
    })

    version
  }

  private def getCodeSystemVersion(vsd: ValueSetDefinition, entry: URIAndEntityName) = {
    vsd.getProperty.find(p =>
      p.getPredicate.getName.equals("usesCodeSystem") &&
        p.getValue.exists(v => v.getLiteral.getSchema.equals("codeSystem") &&
          v.getLiteral.getValue.getContent.equals(entry.getNamespace))) match {
      case Some(p) =>
        p.getPropertyQualifier.find(_.getPredicate.getName.equals("usesCodeSystemVersion")) match {
          case Some(pq) => pq.getValue.find(_.getLiteral.getSchema.equals("codeSystemVersion")) match {
            case Some(v) => v.getLiteral.getValue.getContent
            case None => ""
          }
          case None => ""
        }
      case None => ""
    }
  }

}
