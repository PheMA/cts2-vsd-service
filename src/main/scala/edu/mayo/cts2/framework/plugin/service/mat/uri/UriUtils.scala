package edu.mayo.cts2.framework.plugin.service.mat.uri

object UriUtils {

  val oidUriPrefix = "urn:oid:"

  def oidToUri(oid: String) = oidUriPrefix + oid

}