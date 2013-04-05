package edu.mayo.cts2.framework.plugin.service.mat.model

import scala.reflect.BeanProperty
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.CascadeType
import javax.persistence.ElementCollection
import java.util.ArrayList
import javax.persistence.FetchType

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

  def addVersion(version: ValueSetVersion,current: Boolean = false) {
    if(currentVersion == null || current){
      currentVersion = version
    }
    versions.add(version)
    version.valueSet = this
  }

  @OneToOne(cascade=Array{CascadeType.ALL}, fetch = FetchType.LAZY)
  var currentVersion: ValueSetVersion = _

  @OneToMany(cascade=Array{CascadeType.ALL}, fetch = FetchType.LAZY)
  var versions: java.util.List[ValueSetVersion] = new java.util.ArrayList[ValueSetVersion]

  @OneToMany(cascade=Array{CascadeType.ALL})
  var properties: java.util.List[ValueSetProperty] = new ArrayList[ValueSetProperty]()

  override def hashCode() = this.name.hashCode

  override def equals(other: Any) = other match {
    case that: ValueSet => this.name == that.name
    case _ => false
  }

  def canEqual(other: Any) = {
    other.isInstanceOf[edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet]
  }
}
