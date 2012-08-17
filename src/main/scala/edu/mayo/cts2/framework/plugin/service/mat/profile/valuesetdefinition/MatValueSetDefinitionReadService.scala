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

@Component
class MatValueSetDefinitionReadService extends AbstractService with ValueSetDefinitionReadService {

  @Resource
  var valueSetRepository: ValueSetRepository = _
  
  /**
   * This is incomplete... this is only here to map the 'CURRENT' tag to a CodeSystemVersionName.
   */
  @Override
  def readByTag(
    valueSet: NameOrURI,
    tag: VersionTagReference, readContext: ResolvedReadContext): LocalIdValueSetDefinition = {

    if (tag.getContent() == null || !tag.getContent().equals("CURRENT")) {
      throw new RuntimeException("Only 'CURRENT' tag is supported")
    }

    val valueSetName = valueSet.getName()

    new LocalIdValueSetDefinition(valueSetName, null)
  }

  @Override
  def existsByTag(valueSet: NameOrURI,
    tag: VersionTagReference, readContext: ResolvedReadContext): Boolean = {
    throw new UnsupportedOperationException()
  }

  @Override
  @Transactional
  def read(
      identifier: ValueSetDefinitionReadId,
      readContext: ResolvedReadContext): LocalIdValueSetDefinition = {
    val valueSetName = identifier.getValueSet.getName
    
    val valueSet = valueSetRepository.findOneByName(valueSetName)
    
    val valueSetDef = valueSetToDefinition(valueSet)
    
    new LocalIdValueSetDefinition("1", valueSetDef)
  }
  
  def valueSetToDefinition(valueSet:ValueSet):ValueSetDefinition = {
    val valueSetDef = new ValueSetDefinition()
    valueSetDef.setAbout("TODO")

    valueSetDef
  }

  @Override
  def exists(identifier: ValueSetDefinitionReadId, readContext: ResolvedReadContext): Boolean = {
    throw new UnsupportedOperationException()
  }

  def getSupportedTags: java.util.List[VersionTagReference] =
    List[VersionTagReference](CURRENT_TAG)

}