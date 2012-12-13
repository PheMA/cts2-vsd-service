package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import scala.collection.JavaConversions._
import edu.mayo.cts2.framework.plugin.service.mat.profile.{StateChangeCallback, AbstractService}
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionMaintenanceService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import edu.mayo.cts2.framework.model.valuesetdefinition.{ValueSetDefinitionEntry, SpecificEntityList, ValueSetDefinition}
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ChangeSetRepository, ValueSetDefinitionRepository}
import edu.mayo.cts2.framework.plugin.service.mat.model.util.MatValueSetDefinitionUtils
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSet
import edu.mayo.cts2.framework.model.core.{URIAndEntityName, ChangeDescription}
import edu.mayo.cts2.framework.model.core.types.{FinalizableState, ChangeCommitted, ChangeType}
import java.util.{UUID, Date}
import edu.mayo.cts2.framework.model.updates.ChangeableResource
import edu.mayo.cts2.framework.model.service.core.{EntityNameOrURI, EntityNameOrURIList}

@Component
class MatValueSetDefinitionMaintenanceService extends AbstractService with ValueSetDefinitionMaintenanceService {

  @Resource
  var valueSetDefinitionRepository: ValueSetDefinitionRepository = _

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
      if (isValueSetDefintionWritable(vsd)) {

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

  private def isChangeSetWritable(changeSet: MatChangeSet) = {
    changeSet != null && changeSet.getState.eq(FinalizableState.OPEN)
  }

  private def isValueSetDefintionWritable(valueSetDef: ValueSetDefinition) = {
    valueSetDef == null && valueSetDef.getState.eq(FinalizableState.OPEN)
  }

  private def saveValueSetDefinition(vsd: ValueSetDefinition) {
    valueSetDefinitionRepository.save(MatValueSetDefinitionUtils.transformToMatValueSetDefinition(vsd))
    vsd
  }

  private def removeDefinitionEntry(changeSetUri: String, valueSetDefinitionUri: String, entryToRemove: Int): ValueSetDefinition = {
    //    val vsd = valueSetDefinitionRepository.findOne(valueSetDefinitionUri)
    //    vsd.removeEntry(entryToRemove)
    //    valueSetDefinitionRepository.save(vsd)
    //    MatValueSetDefinitionUtils.transformToValueSetDefinition(vsd)
    null
  }

  private def getChangeSet(changeSetUri: String): MatChangeSet = {
    changeSetRepository.findOne(changeSetUri)
  }

  private def getValueSetDefinition(vsdReadId: ValueSetDefinitionReadId, changeSetUri: String): MatChangeSet = {
    null
  }

  private def getValueSetDefinition(vsdReadId: ValueSetDefinitionReadId): ValueSetDefinition = {
    MatValueSetDefinitionUtils.transformToValueSetDefinition(valueSetDefinitionRepository.findOne(vsdReadId.getUri))
  }

}
