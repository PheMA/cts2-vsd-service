package edu.mayo.cts2.framework.plugin.service.mat.model

import scala.reflect.BeanProperty
import javax.persistence._
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import org.apache.commons.lang.builder.{EqualsBuilder, HashCodeBuilder}
import scala.collection.JavaConversions._
import java.util

@Entity
class ValueSet(valueSetOid: String) extends Equals {

  def this() = this(null)

  @Id
  @BeanProperty
  var name: String = valueSetOid

  @BeanProperty
  var formalName: String = _

  @BeanProperty
  var uri: String = _

  @BeanProperty
  var href: String = _

  def addVersion(version: ValueSetVersion) {
    if (!versions.contains(version)) {
      if(currentVersion == null || version.getState.eq(FinalizableState.FINAL)){
        currentVersion = version
      }
      versions :+ version
      version.valueSet = this
    }
  }

  @OneToOne(cascade=Array{CascadeType.ALL}, fetch = FetchType.EAGER)
  var currentVersion: ValueSetVersion = _

  @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true)
  var versions: java.util.List[ValueSetVersion] = new java.util.ArrayList[ValueSetVersion]

  @OneToMany(cascade=Array{CascadeType.ALL})
  var properties: java.util.List[ValueSetProperty] = new util.ArrayList[ValueSetProperty]()

  override def hashCode() = new HashCodeBuilder()
    .append(name)
    .toHashCode

  override def equals(other: Any) = other match {
    case that: ValueSet => new EqualsBuilder()
      .append(name, that.name)
      .isEquals
    case _ => false
  }

  def canEqual(that: Any) = classOf[ValueSet].eq(that.getClass)

}
