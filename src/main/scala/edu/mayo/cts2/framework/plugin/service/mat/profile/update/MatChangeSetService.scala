package edu.mayo.cts2.framework.plugin.service.mat.profile.update

import javax.annotation.Resource

import java.net.URI
import java.util.{UUID, Date}

import edu.mayo.cts2.framework.model.core.{OpaqueData, SourceReference}
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetChange}
import edu.mayo.cts2.framework.plugin.service.mat.repository.ChangeSetRepository
import edu.mayo.cts2.framework.service.profile.update.ChangeSetService
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.core.types.{ChangeType, FinalizableState}
import edu.mayo.cts2.framework.model.updates.ChangeSet

@Component
class MatChangeSetService extends AbstractService with ChangeSetService {

  @Resource
  var changeSetRepository: ChangeSetRepository = _

  def readChangeSet(changeSetUri: String) = {
    val changeSet = Option[ValueSetChange](changeSetRepository.findOne(changeSetUri))
    changeSet match {
      case None => null
      case Some(cs) => cs.convertToChangeSet
    }
  }

  def createChangeSet() = {
    val valueSetChange = new ValueSetChange
    valueSetChange.setChangeType(ChangeType.CREATE)
    changeSetRepository.save(valueSetChange)
    valueSetChange.convertToChangeSet
  }

  def updateChangeSetMetadata(changeSetId: String,
                              creator: SourceReference,
                              changeInstructions: OpaqueData,
                              officialEffectiveDate: Date) {
    val changeSet = changeSetRepository.findOne(changeSetId)
    if (changeSet != null) {
      if (creator != null) {
        changeSet.setAuthor(creator.getContent)
      }

      if (changeInstructions != null) {
        changeSet.setDescription(changeInstructions.getValue.toString)
      }

      changeSetRepository.save(changeSet)
    }
  }

  def rollbackChangeSet(changeSetId: String) {
    changeSetRepository.delete(changeSetId)
  }

  def commitChangeSet(changeSetId: String) {
    throw new RuntimeException
  }

  def importChangeSet(changeSetURI: URI) = {
    throw new RuntimeException
  }

}
