package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import java.lang.Override
import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference
import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService
import javax.annotation.Resource
import org.apache.commons.lang.StringUtils

@Component
class MatValueSetReadService extends AbstractService with ValueSetReadService {

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Override
  @Transactional
  def read(
    identifier: NameOrURI,
    readContext: ResolvedReadContext): ValueSetCatalogEntry = {
    val valueSet =
      if (identifier.getName != null) {
        valueSetRepository.findOneByName(identifier.getName)
      } else {
        val uri = identifier.getUri

        val lookupKey =
          if (StringUtils.startsWith(uri, UriUtils.OID_URI_PREFIX)) {
            StringUtils.substringAfter(identifier.getUri, UriUtils.OID_URI_PREFIX)
          } else {
            uri
          }
        
        valueSetRepository.findOne(lookupKey)
      }

    if (valueSet == null) null else valueSetToValueSetCatalogEntry(valueSet)
  }

  def valueSetToValueSetCatalogEntry(valueSet: ValueSet): ValueSetCatalogEntry = {
    val valueSetCatalogEntry = new ValueSetCatalogEntry()
    valueSetCatalogEntry.setAbout(UriUtils.oidToUri(valueSet.oid))
    valueSetCatalogEntry.addAlternateID(valueSet.oid)
    valueSetCatalogEntry.setValueSetName(valueSet.getName)
    valueSetCatalogEntry.setFormalName(valueSet.formalName)
    valueSetCatalogEntry.addSourceAndRole(MatValueSetUtils.sourceAndRole)

    valueSetCatalogEntry.setDefinitions(urlConstructor.createDefinitionsOfValueSetUrl(valueSet.getName))
    valueSetCatalogEntry.setCurrentDefinition(MatValueSetUtils.currentDefintion(valueSetCatalogEntry, urlConstructor))

    valueSetCatalogEntry
  }

  @Override
  def exists(identifier: NameOrURI, readContext: ResolvedReadContext): Boolean = {
    throw new UnsupportedOperationException()
  }

}