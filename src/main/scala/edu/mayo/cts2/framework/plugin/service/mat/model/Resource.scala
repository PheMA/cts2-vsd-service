// Copyright (c) 2014. Mayo Foundation for Medical Education and Research. All rights reserved.

package edu.mayo.cts2.framework.plugin.service.mat.model

import scala.reflect.BeanProperty

class Resource(resourceName: String, resourceUri: String, resourceDomain: String) {

  def this() = this(null, null, null)

  @BeanProperty
  var uri: String = resourceUri

  @BeanProperty
  var domain: String = resourceDomain

  @BeanProperty
  var name: String = resourceName

  @BeanProperty
  var baseUri: String = _

  @BeanProperty
  var href: String = _

  @BeanProperty
  var description: String = _
}
