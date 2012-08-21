package edu.mayo.cts2.framework.plugin.service.mat.uri

trait UriResolver {

  /*
   * Given an ID ("SNOMEDCT", "123.32.342.23", "http://http://purl.obolibrary.org/obo/zfa.owl")
   * And a Type, return the `canonical` URI of the resource. 
   */
  def idToUri(id: String, idType: IdType.Value):String

  /*
   * Given a `canonical` URI of a resource, return the VersionID information.
   */
  def uriToVersionId(id: String, idType: IdType.Value):VersionId

}

class VersionId(
    val name:String,
    val versionId:String,
    val uri:String
)

object IdType extends Enumeration {
  type IdType = Value
  val CODESYSTEM, VALUESET = Value
}