package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence._
import scala.reflect.BeanProperty
import java.util
import org.apache.commons.lang.builder.{EqualsBuilder, HashCodeBuilder}
import scala.collection.JavaConversions._

@Entity
class ValueSetProperty extends Equals {
  
  @Id
  @BeanProperty
  var id: String = util.UUID.randomUUID.toString

  @BeanProperty
  var name: String = _

  @BeanProperty
  @Column(length = 1024)
  var value: String = _
  
  @ElementCollection
  var qualifiers: java.util.List[PropertyQualifier] = new util.ArrayList[PropertyQualifier]()

  override def hashCode() = new HashCodeBuilder()
    .append(id)
    .toHashCode

  override def equals(other: Any) = other match {
    case that: ValueSetProperty => new EqualsBuilder()
      .append(id, that.id)
      .isEquals
    case _ => false
  }

  def canEqual(that: Any) = classOf[ValueSetProperty].eq(that.getClass)

}

@Embeddable
case class PropertyQualifier(qualName:String, @Column(length=1024)qualValue:String) {
  def this() = this(null,null)
}