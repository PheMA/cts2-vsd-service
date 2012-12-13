package edu.mayo.cts2.framework.plugin.service.mat.profile.update

import javax.annotation.Resource

import java.net.URI
import java.util.{UUID, Date}

import edu.mayo.cts2.framework.model.core.{OpaqueData, SourceReference}
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSet
import edu.mayo.cts2.framework.plugin.service.mat.repository.ChangeSetRepository
import edu.mayo.cts2.framework.service.profile.update.ChangeSetService
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.plugin.service.mat.model.util.MatChangeSetUtils

@Component
class MatChangeSetService extends AbstractService with ChangeSetService {

  @Resource
  var changeSetRepository: ChangeSetRepository = _

  def readChangeSet(changeSetUri: String) = {
    val changeSet = changeSetRepository.findOne(changeSetUri)
    MatChangeSetUtils.transformToChangeSet(changeSet)
  }

  def createChangeSet() = {
    val changeSet = doCreateChangeSet()
    changeSetRepository.save(changeSet)
    MatChangeSetUtils.transformToChangeSet(changeSet)
  }

  def doCreateChangeSet(): MatChangeSet = {
    val changeSet = new MatChangeSet(createChangeSetUri)
    changeSet.setState(FinalizableState.OPEN)
    changeSet
  }

  def createChangeSetUri = "urn:uuid:" + UUID.randomUUID.toString

  def updateChangeSetMetadata(changeSetId: String,
                              creator: SourceReference,
                              changeInstructions: OpaqueData,
                              officialEffectiveDate: Date) {
    val changeSet = MatChangeSetUtils.transformToMatChangeSet(readChangeSet(changeSetId))

    if (creator != null) changeSet.setCreator(creator.getContent)
    if (changeInstructions != null) changeSet.setChangeInstructions(changeInstructions.getValue.asInstanceOf[String])
    if (officialEffectiveDate != null) changeSet.setOfficialEffectiveDate(officialEffectiveDate)

    changeSetRepository.save(changeSet)
  }

  def rollbackChangeSet(changeSetId: String) {
    changeSetRepository.delete(changeSetId)
  }

  def commitChangeSet(changeSetId: String) {
    val changeSet = MatChangeSetUtils.transformToMatChangeSet(readChangeSet(changeSetId))
    throw new RuntimeException
  }

  def importChangeSet(changeSetURI: URI) = {
    throw new RuntimeException
  }

}
