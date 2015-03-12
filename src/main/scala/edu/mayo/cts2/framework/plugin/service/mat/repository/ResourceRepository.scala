// Copyright (c) 2014. Mayo Foundation for Medical Education and Research. All rights reserved.

package edu.mayo.cts2.framework.plugin.service.mat.repository

import java.util.logging.Logger

import dispatch._
import edu.mayo.cts2.framework.plugin.service.mat.model.Resource
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ResourceRepository {

  val logger = Logger.getLogger(classOf[ResourceRepository].toString)

  @scala.reflect.BeanProperty
  @Value("${uriResolutionServiceUrl}")
  var uriResolutionServiceUrl: String = _

  def save(resource: Resource): Unit = {
    val request = url(uriResolutionServiceUrl) / "ids"
    request << "{\"resourceType\":\"CODE_SYSTEM\", \"resourceName\":\""+resource.name+"\", \"resourceURI\":\""+resource.uri+"\", \"baseEntityURI\":\""+resource.baseUri+"\", \"identifiers\":[\""+resource.name.toLowerCase+"\", \""+resource.uri+"\", \""+resource.name.replaceAll("_", " ")+"\"]}"
    request.setHeader("Content-Type", "application/json")
    val response = Http(request OK as.String)
    for (r <- response) {
      logger.info("Created URI resource " + resource.uri)
    }

  }

}
