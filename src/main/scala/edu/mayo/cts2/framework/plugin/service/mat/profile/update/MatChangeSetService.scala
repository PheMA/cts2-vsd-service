package edu.mayo.cts2.framework.plugin.service.mat.profile.update

import javax.annotation.Resource

import java.net.URI
import java.util.{Calendar, UUID, Date}

import edu.mayo.cts2.framework.model.core.{OpaqueData, SourceReference}
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetChange}
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ValueSetVersionRepository, ChangeSetRepository}
import edu.mayo.cts2.framework.service.profile.update.ChangeSetService
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, ChangeType, FinalizableState}
import edu.mayo.cts2.framework.model.updates.ChangeSet
import edu.mayo.cts2.framework.model.service.exception.{UnknownValueSetDefinition, ChangeSetIsNotOpen, UnknownChangeSet}

@Component
class MatChangeSetService extends AbstractService with ChangeSetService {

  @Resource
  var changeSetRepository: ChangeSetRepository = _

  @Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  def readChangeSet(changeSetUri: String) = {
    val changeSet = Option[ValueSetChange](changeSetRepository.findOne(changeSetUri))
    changeSet match {
      case None => null
      case Some(cs) => cs.convertToChangeSet
    }
  }

  def createChangeSet() = {
    val valueSetChange = new ValueSetChange
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
        changeSet.setCreator(creator.getContent)
      }

      if (changeInstructions != null) {
        changeSet.setInstructions(changeInstructions.getValue.getContent)
      }

      if (officialEffectiveDate != null) {
        val calendar = Calendar.getInstance()
        calendar.setTime(officialEffectiveDate)
        changeSet.setDate(calendar)
      }

      changeSetRepository.save(changeSet)
    }
  }

  def rollbackChangeSet(changeSetId: String) {
    changeSetRepository.delete(changeSetId)
  }

  def commitChangeSet(changeSetId: String) {
    Option(changeSetRepository.findOne(changeSetId)) match {
      case Some(change) => {
        if (change.getState.eq(FinalizableState.OPEN)) {
          commitChangeSet(change)
        } else {
          throw new ChangeSetIsNotOpen
        }
      }
      case None => throw new UnknownChangeSet
    }
  }

  private def commitChangeSet(change: ValueSetChange) {
    throw new RuntimeException
//    change.getChangeType match {
//      case ChangeType.DELETE => {
//        Option(valueSetVersionRepository.findByChangeSetUri(change.getId)) match {
//          case Some(valueSetVersion) => valueSetVersionRepository.delete(valueSetVersion)
//          case None => throw new UnknownValueSetDefinition
//        }
//      }
//      case ChangeType.UPDATE => {}
//      case ChangeType.CREATE => {}
//      case default => throw new UnsupportedOperationException
//    }
//    change.setChangeCommitted(ChangeCommitted.COMMITTED)
//    change.setFinalizableState(FinalizableState.FINAL)
//    changeSetRepository.save(change)
  }

  def importChangeSet(changeSetUri: URI): String = {
    throw new RuntimeException
  }

}
