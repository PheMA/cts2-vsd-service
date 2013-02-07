package edu.mayo.cts2.framework.plugin.service.mat.profile

import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.repository.ChangeSetRepository
import edu.mayo.cts2.framework.model.core.{StatusReference, ChangeDescription, ChangeableElementGroup}
import edu.mayo.cts2.framework.model.util.ModelUtils

abstract class AbstractReadService extends AbstractService {

  @Resource
  var changeRepository: ChangeSetRepository = _

  def getChangeableElementGroup(changeSetUri: String): ChangeableElementGroup = {
    val group = new ChangeableElementGroup
    if (changeSetUri.ne("")) {
      val change = changeRepository.findOne(changeSetUri)
      if (change != null) {
        val changeDesc = new ChangeDescription
        changeDesc.setContainingChangeSet(change.getChangeSetUri)
        changeDesc.setChangeDate(change.getCurrentVersion.getRevisionDate.getTime)
        changeDesc.setChangeType(change.getCurrentVersion.getChangeType)
        changeDesc.setChangeNotes(ModelUtils.createOpaqueData(change.getInstructions))
        changeDesc.setCommitted(change.getCurrentVersion.getChangeCommitted)

        /* TODO: populate the rest of the change set details */
        //        changeDesc.setChangeSource()
        //        changeDesc.setClonedResource()
        //        changeDesc.setEffectiveDate()
        //        changeDesc.setPrevChangeSet()
        //        changeDesc.setPrevImage()

        group.setChangeDescription(changeDesc)
        group.setStatus(new StatusReference(change.getCurrentVersion.getStatus))
      }
    }
    group
  }

}
