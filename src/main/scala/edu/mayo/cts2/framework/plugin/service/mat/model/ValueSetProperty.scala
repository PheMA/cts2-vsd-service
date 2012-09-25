package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence.Entity
import javax.persistence.Embeddable
import scala.reflect.BeanProperty
import javax.persistence.Column

@Embeddable
class ValueSetProperty {

  @BeanProperty
  var name: String = _

  @BeanProperty
  var value: String = _

}