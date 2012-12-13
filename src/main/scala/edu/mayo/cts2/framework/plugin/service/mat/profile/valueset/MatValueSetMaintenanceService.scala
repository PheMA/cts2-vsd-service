package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetMaintenanceService
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.util.ModelUtils

@Component
class MatValueSetMaintenanceService extends AbstractService with ValueSetMaintenanceService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

  def updateChangeableMetadata(nameOrUri: NameOrURI, updateChangeableMetadataRequest: UpdateChangeableMetadataRequest) {

  }

  def updateResource(valueSetCatalogEntry: ValueSetCatalogEntry) {

  }

  def createResource(valueSetCatalogEntry: ValueSetCatalogEntry): ValueSetCatalogEntry = {

    null
  }

  def deleteResource(nameOrUri: NameOrURI, changeSetUri: String) {

  }
}
