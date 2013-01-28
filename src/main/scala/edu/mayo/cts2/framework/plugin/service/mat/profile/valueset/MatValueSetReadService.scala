package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import java.lang.Override
import scala.collection.JavaConversions._
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core.Property
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference
import edu.mayo.cts2.framework.model.core.ValueSetDefinitionReference
import edu.mayo.cts2.framework.model.service.core.NameOrURI
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetProperty
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriUtils
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService
import javax.annotation.Resource
import edu.mayo.cts2.framework.model.core.PredicateReference
import edu.mayo.cts2.framework.model.core.StatementTarget
import edu.mayo.cts2.framework.model.util.ModelUtils

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
        valueSetRepository.findOne(identifier.getName)
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
    valueSetCatalogEntry.setAbout(UriUtils.oidToUri(valueSet.name))
    valueSetCatalogEntry.addAlternateID(valueSet.name)
    valueSetCatalogEntry.setValueSetName(valueSet.getName)
    valueSetCatalogEntry.setFormalName(valueSet.formalName)
    valueSetCatalogEntry.addSourceAndRole(MatValueSetUtils.sourceAndRole)

    valueSetCatalogEntry.setDefinitions(urlConstructor.createDefinitionsOfValueSetUrl(valueSet.getName))
    
    valueSetCatalogEntry.setCurrentDefinition(
        MatValueSetUtils.buildValueSetDefinitionReference(
            valueSetCatalogEntry.getValueSetName, valueSetCatalogEntry.getAbout,
            valueSet.currentVersion,
            urlConstructor))

    valueSet.properties.foreach( (prop) => valueSetCatalogEntry.addProperty( toProperty(prop) ) )
    
    valueSetCatalogEntry
  }
  
  def toProperty(valueSetProp: ValueSetProperty) = {
     val prop = createProperty(valueSetProp.getName, valueSetProp.getValue)
     
     valueSetProp.qualifiers.foreach( (qual) => {
       prop.addPropertyQualifier( createProperty(qual.getName, qual.getValue) )
     })  
     
     prop
  } : Property
  
  def createProperty(name:String,value:String) = {
    val prop = new Property()
    
     val predicate = new PredicateReference()
     predicate.setName(name)
     predicate.setNamespace(UriUtils.SVS_NS)
     predicate.setUri(UriUtils.toSvsUri(name))
     prop.setPredicate(predicate)
     
     val target = new StatementTarget()
     target.setLiteral( ModelUtils.createOpaqueData(value) )   
     prop.addValue(target)
     
     prop
  }

  @Override
  def exists(identifier: NameOrURI, readContext: ResolvedReadContext): Boolean = {
    throw new UnsupportedOperationException()
  }

}