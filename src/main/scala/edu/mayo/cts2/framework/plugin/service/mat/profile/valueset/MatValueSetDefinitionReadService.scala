package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import java.lang.Override
import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core.VersionTagReference
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import javax.annotation.Resource
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry

@Component
class MatValueSetReadService extends AbstractService with ValueSetReadService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Override
  @Transactional
  def read(
      identifier: NameOrURI,
      readContext: ResolvedReadContext): ValueSetCatalogEntry = {
    val valueSetName = identifier.getName
    
    val valueSet = valueSetRepository.findOneByName(valueSetName)
    
    valueSetToValueSetCatalogEntry(valueSet)
  }
  
  def valueSetToValueSetCatalogEntry(valueSet:ValueSet):ValueSetCatalogEntry = {
    val valueSetDef = new ValueSetCatalogEntry()
    valueSetDef.setAbout("TODO")

    valueSetDef
  }

  @Override
  def exists(identifier: NameOrURI, readContext: ResolvedReadContext): Boolean = {
    throw new UnsupportedOperationException()
  }

}