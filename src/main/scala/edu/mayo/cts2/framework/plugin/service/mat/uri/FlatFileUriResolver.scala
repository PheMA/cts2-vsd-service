package edu.mayo.cts2.framework.plugin.service.mat.uri

import org.springframework.stereotype.Component
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.namespace.NamespaceResolutionService

//TODO:
@Component
class FlatFileUriResolver extends UriResolver {
  
  @Resource
  var namespaceResolutionService: NamespaceResolutionService = _

  def idToUri(id: String, idType: IdType.Value):String = {
    
    val prefix = namespaceResolutionService.prefixToUri(id)
    
    prefix.getOrElse("urn:oid:" + id)
  }
  
  def idToName(id: String, idType: IdType.Value):String = {
    
    id
  }

  def uriToVersionId(id: String, idType: IdType.Value):VersionId = {
    
    null
  }

}
