package edu.mayo.cts2.framework.plugin.service.mat.uri

object UriUtils {

  val URN_PREFIX = "urn:"
    
  val OID_PREFIX = "oid:"
    
  val UUID_PREFIX = "uuid:"
    
  val OID_URI_PREFIX = URN_PREFIX + OID_PREFIX
  
  val UUID_URI_PREFIX = URN_PREFIX + UUID_PREFIX

  def oidToUri(oid: String) = OID_URI_PREFIX + oid
  
  def uuidToUri(uuid: String) = UUID_URI_PREFIX + uuid

}