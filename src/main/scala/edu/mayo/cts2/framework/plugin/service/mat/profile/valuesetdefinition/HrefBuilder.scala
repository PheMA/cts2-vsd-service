package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import scala.collection.JavaConversions._
import scala.collection.JavaConversions.iterableAsScalaIterable
import org.apache.commons.lang.StringUtils
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.core.url.UrlConstructor
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import javax.annotation.Resource
import org.springframework.beans.factory.annotation.Value

@Component
class HrefBuilder {

  @Resource
  var urlConstructor: UrlConstructor = _

  @scala.reflect.BeanProperty
  @Value("${snomedCtUrlBase}")
  var snomedCtUrlBase: String = _

  val SNOMEDCT = "SNOMED-CT"
  val UMLS_CODE_SYSTEMS = Set("CPT", "ICD-10-CM", "ICD-9-CM", "RxNorm")

  def createEntityHref(entry: ValueSetEntry) = {
    
    def createUmlsUtsUrl = {
      urlConstructor.createEntityUrl(
        csNameToSab(entry.codeSystem),
        csNameAndVersionToCsVersionName(entry.codeSystem, entry.codeSystemVersion),
        entry.code)
    }
    
    val cs = entry.codeSystem
    
    if (UMLS_CODE_SYSTEMS.contains(cs)) {
      createUmlsUtsUrl
    } else if (cs.equals(SNOMEDCT)) {
      if (StringUtils.isNotBlank(snomedCtUrlBase)) {
        snomedCtUrlBase + "/" + entry.code
      } else {
        createUmlsUtsUrl
      }
    } else {
      null
    }
  }

  def csNameAndVersionToCsVersionName(csName: String, versionId: String) = {
    val version =
      if (StringUtils.isBlank(versionId)) {
        "unknown"
      } else {
        versionId
      }
    csNameToSab(csName) + "-" + StringUtils.replaceChars(version, "/", "-")
  }

  def csNameToSab(csName: String) = {
    StringUtils.upperCase(StringUtils.remove(csName, '-'))
  }: String

}