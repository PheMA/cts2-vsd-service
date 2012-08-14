package edu.mayo.cts2.framework.plugin.service.mat.namespace

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import clojure.lang.RT
import javax.annotation.Resource

@Component("namespaceResolutionService")
class NamespaceResolutionService {

  RT.loadResourceScript("cts2/ns/NamespaceService.clj");

  val getNsUriFn = RT.`var`("cts2.ns", "getNsUri")
  val getNsPrefixFn = RT.`var`("cts2.ns", "getNsPrefix")

  @scala.reflect.BeanProperty
  @Value("${namespaceServiceUrl}")
  var namespaceServiceUrl: String = _

  def prefixToUri(prefix: String): String = {

    var uri = getNsUriFn.invoke(namespaceServiceUrl, prefix)

    if (uri != null) {
      println("GOT: " + uri + " from: " + namespaceServiceUrl  + "AND" + prefix);
      uri.toString()
    } else {
      throw new UnsupportedOperationException()
    }
  }

  def uriToPrefix(prefix: String): String = {
    var uri = getNsPrefixFn.invoke(namespaceServiceUrl, prefix)

    if (uri != null) {
      uri.toString()
    } else {
      throw new UnsupportedOperationException()
    }
  }

}