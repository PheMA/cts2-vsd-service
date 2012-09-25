package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence.Entity
import javax.persistence.Embeddable
import scala.reflect.BeanProperty
import javax.persistence.Column
import javax.persistence.ElementCollection
import java.util.ArrayList

@Embeddable
class PropertyQualifier(qualName:String,qualValue:String) {
  
  def this() = this(null,null)

  @BeanProperty
  var name: String = qualName

  @BeanProperty
  @Column(length = 1024)
  var value: String = qualValue

}