package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence.Entity
import javax.persistence.Embeddable
import scala.reflect.BeanProperty

@Embeddable
class ValueSetEntry {

  @BeanProperty
  var code: String = _

  @BeanProperty
  var codeSystem: String = _

  @BeanProperty
  var codeSystemVersion: String = _

  @BeanProperty
  var description: String = _

}