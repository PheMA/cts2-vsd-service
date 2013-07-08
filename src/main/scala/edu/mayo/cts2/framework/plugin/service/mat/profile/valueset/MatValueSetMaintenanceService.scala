package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetMaintenanceService
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ChangeSetRepository, ValueSetRepository}
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetVersion, ValueSetChange, ValueSet}
import edu.mayo.cts2.framework.model.service.exception.{ChangeSetIsNotOpen, UnknownChangeSet, DuplicateValueSetURI, DuplicateValueSetName}
import edu.mayo.cts2.framework.model.core.types.FinalizableState

@Component
class MatValueSetMaintenanceService extends AbstractService with ValueSetMaintenanceService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Resource
  var changeSetRepo: ChangeSetRepository = _

  override def updateChangeableMetadata(nameOrUri: NameOrURI, updateChangeableMetadataRequest: UpdateChangeableMetadataRequest) {

  }

  override def updateResource(valueSetCatalogEntry: ValueSetCatalogEntry) {
    getChangeSet(valueSetCatalogEntry)
  }

  override def createResource(valueSetCatalogEntry: ValueSetCatalogEntry): ValueSetCatalogEntry = {
    /* TODO: update change set */
    getChangeSet(valueSetCatalogEntry)

    if (valueSetRepository.findOne(valueSetCatalogEntry.getValueSetName).ne(null))
      throw new DuplicateValueSetName
    if (valueSetRepository.findOneByUri(valueSetCatalogEntry.getAbout).ne(null))
      throw new DuplicateValueSetURI

    val vs = new ValueSet
    vs.setName(valueSetCatalogEntry.getValueSetName)
    vs.setFormalName(valueSetCatalogEntry.getFormalName)
    vs.setHref(null)
    vs.setUri(valueSetCatalogEntry.getAbout)
      valueSetRepository save vs
    valueSetCatalogEntry
  }

  override def deleteResource(nameOrUri: NameOrURI, changeSetUri: String) {
    getChangeSet(changeSetUri)
  }

  private def getChangeSet(valueSetCatalogEntry: ValueSetCatalogEntry): ValueSetChange = {
    if (valueSetCatalogEntry.ne(null) && valueSetCatalogEntry.getChangeableElementGroup.ne(null) && valueSetCatalogEntry.getChangeableElementGroup.getChangeDescription.ne(null)) {
      getChangeSet(valueSetCatalogEntry.getChangeableElementGroup.getChangeDescription.getContainingChangeSet)
    } else {
      throw new UnknownChangeSet
    }
  }

  private def getChangeSet(changeSetUri: String): ValueSetChange = {
    val changeSet = changeSetRepo.findOne(changeSetUri)
    if (changeSet.eq(null))
      throw new UnknownChangeSet
    if (changeSet.getState.ne(FinalizableState.OPEN))
      throw new ChangeSetIsNotOpen
    changeSet
  }
}
