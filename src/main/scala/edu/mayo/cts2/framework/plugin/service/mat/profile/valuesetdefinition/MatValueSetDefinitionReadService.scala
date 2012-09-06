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
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry
import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.core.types.SetOperator
import edu.mayo.cts2.framework.plugin.service.mat.uri.IdType
import edu.mayo.cts2.framework.model.core.SourceAndNotation
import edu.mayo.cts2.framework.model.core.ValueSetReference

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

  def valueSetToDefinition(valueSet: ValueSet): ValueSetDefinition = {
    val valueSetDef = new ValueSetDefinition()
    valueSetDef.setAbout("urn:oid:" + valueSet.oid)
    valueSetDef.setDocumentURI("urn:oid:" + valueSet.oid + ":1")
    valueSetDef.setSourceAndNotation(buildSourceAndNotation())
    valueSetDef.setDefinedValueSet(buildValueSetReference(valueSet))

    val list = valueSet.entries.foldLeft(new SpecificEntityList())((list, entry) => {
      val entity = new URIAndEntityName()
      val prefix = uriResolver.idToName(entry.codeSystem, IdType.CODE_SYSTEM)
      entity.setNamespace(prefix)
      entity.setName(entry.code)
      
      val baseUri = uriResolver.idToBaseUri(entry.codeSystem)

      entity.setUri(baseUri + entry.code)

      list.addReferencedEntity(entity)

      list
    })

    val vsdEntry = new ValueSetDefinitionEntry()
    vsdEntry.setEntryOrder(1)
    vsdEntry.setOperator(SetOperator.UNION)
    vsdEntry.setEntityList(list)

    valueSetDef.addEntry(vsdEntry)

    valueSetDef
  }

  private def buildValueSetReference(valueSet: ValueSet): ValueSetReference = {
	val ref = new ValueSetReference()
	ref.setContent(valueSet.name)
	ref.setUri("urn:oid:" + valueSet.oid)
	ref.setHref(urlConstructor.createValueSetUrl(valueSet.name))
	
	ref
  }

  private def buildSourceAndNotation(): SourceAndNotation = {
    val sourceAndNotation = new SourceAndNotation()
    sourceAndNotation.setSourceAndNotationDescription("MAT Authoring Tool Output Zip.")

    sourceAndNotation
  }

  @Override
  def exists(identifier: ValueSetDefinitionReadId, readContext: ResolvedReadContext): Boolean = {
    throw new UnsupportedOperationException()
  }

  def getSupportedTags: java.util.List[VersionTagReference] =
    List[VersionTagReference](CURRENT_TAG)

}