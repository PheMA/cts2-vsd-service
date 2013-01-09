package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import scala.collection.JavaConversions._
import edu.mayo.cts2.framework.plugin.service.mat.profile.{StateChangeCallback, AbstractService}
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionMaintenanceService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import edu.mayo.cts2.framework.model.valuesetdefinition.{ValueSetDefinitionEntry, SpecificEntityList, ValueSetDefinition}
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ValueSetRepository, ValueSetVersionRepository, ChangeSetRepository}
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetEntry, ValueSetVersion, ValueSetChange}
import edu.mayo.cts2.framework.model.core.{URIAndEntityName, ChangeDescription}
import edu.mayo.cts2.framework.model.core.types.{FinalizableState, ChangeCommitted, ChangeType}
import java.util.{UUID, Date}
import edu.mayo.cts2.framework.model.updates.ChangeableResource
import edu.mayo.cts2.framework.model.service.core.{EntityNameOrURI, EntityNameOrURIList}

@Component
class MatValueSetDefinitionMaintenanceService extends AbstractService with ValueSetDefinitionMaintenanceService {

  @Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Resource
  var changeSetRepository: ChangeSetRepository = _

  @Resource
  var stateChangeCallback: StateChangeCallback = _

  override def updateChangeableMetadata(readId: ValueSetDefinitionReadId, request: UpdateChangeableMetadataRequest) {}

  override def updateResource(resource: LocalIdValueSetDefinition) {
    /* TODO: implement */
    val choice : ChangeableResource = new ChangeableResource
    choice.setValueSetDefinition(resource.getResource)
    stateChangeCallback.resourceUpdated(choice)
  }

  override def createResource(valueSetDefinition: ValueSetDefinition): LocalIdValueSetDefinition = {
    saveValueSetDefinition(valueSetDefinition)
    val choice: ChangeableResource = new ChangeableResource
    choice.setValueSetDefinition(valueSetDefinition)
    stateChangeCallback.resourceAdded(choice)
    new LocalIdValueSetDefinition(UUID.randomUUID().toString, valueSetDefinition)
  }

  override def deleteResource(valueSetDefinitionReadId: ValueSetDefinitionReadId, changeSetUri: String) {
    val changeSet = getChangeSet(changeSetUri)
    if (isChangeSetWritable(changeSet)) {
      val vsd = getValueSetDefinition(valueSetDefinitionReadId)
      if (isValueSetDefinitionWritable(vsd)) {

        val group = vsd.getChangeableElementGroup

        val changeDesc = new ChangeDescription
        changeDesc.setChangeDate(new Date())
        changeDesc.setChangeType(ChangeType.DELETE)
        changeDesc.setCommitted(ChangeCommitted.PENDING)
        changeDesc.setContainingChangeSet(changeSetUri)

        group.setChangeDescription(changeDesc)
        saveValueSetDefinition(vsd)

        val choice = new ChangeableResource
        choice.setValueSetDefinition(vsd)
        stateChangeCallback.resourceDeleted(choice, changeSetUri)
      }
    }

  }

  def createSpecificEntityListDefinition(changeSetUri: String, valueSetId: String, entities: EntityNameOrURIList) {
    val changeSet = changeSetRepository.findOne(changeSetUri)
    if (isChangeSetWritable(changeSet)) {
      val specificList: SpecificEntityList = new SpecificEntityList()
      val vsd = new ValueSetDefinition
      val entry = new ValueSetDefinitionEntry
      entry.setEntityList(specificList)
      vsd.addEntry(entry)
    }

  }

  private def resolveEntity(nameOrUri: EntityNameOrURI): URIAndEntityName = {
    null
  }

  private def isChangeSetWritable(changeSet: ValueSetChange) = {
    changeSet != null && changeSet.getState.eq(FinalizableState.OPEN)
  }

  private def isValueSetDefinitionWritable(valueSetDef: ValueSetDefinition) = {
    valueSetDef != null && valueSetDef.getState != null && valueSetDef.getState.eq(FinalizableState.OPEN)
  }

  private def saveValueSetDefinition(vsd: ValueSetDefinition) {
    val version = new ValueSetVersion
    version.setId(vsd.getDocumentURI)
    version.valueSet = valueSetRepository.findOne(vsd.getDefinedValueSet.getContent)
    version.setValueSetDeveloper(vsd.getSourceAndRole(0).getSource.getContent)
    if (vsd.getVersionTagCount > 0)
      version.setVersionId(vsd.getVersionTag(0).getContent)

    if (valueSetVersionRepository.findVersionByIdOrVersionIdAndValueSetName(version.valueSet.getOid, version.getVersionId) == null) {
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
          //        vsEntry.setUri(entity.getUri)
          //        vsEntry.setHref(entity.getHref)
          version.addEntry(vsEntry)
        })
      })

      valueSetVersionRepository.save(version)
    }
  }

  private def removeDefinitionEntry(changeSetUri: String, valueSetDefinitionUri: String, entryToRemove: Int): ValueSetDefinition = {
    //    val vsd = valueSetDefinitionRepository.findOne(valueSetDefinitionUri)
    //    vsd.removeEntry(entryToRemove)
    //    valueSetDefinitionRepository.save(vsd)
    //    MatValueSetDefinitionUtils.transformToValueSetDefinition(vsd)
    null
  }

  private def getChangeSet(changeSetUri: String): ValueSetChange = {
    changeSetRepository.findOne(changeSetUri)
  }

  private def getValueSetDefinition(vsdReadId: ValueSetDefinitionReadId, changeSetUri: String): ValueSetChange = {
    null
  }

  /* TODO: implement */
  private def getValueSetDefinition(vsdReadId: ValueSetDefinitionReadId): ValueSetDefinition = {
//    MatValueSetDefinitionUtils.transformToValueSetDefinition(valueSetDefinitionRepository.findOne(vsdReadId.getUri))
    null
  }

}
