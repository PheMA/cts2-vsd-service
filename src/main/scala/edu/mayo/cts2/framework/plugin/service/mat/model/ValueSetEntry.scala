package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence.Entity
import javax.persistence.Embeddable
import scala.reflect.BeanProperty
import javax.persistence.Column
import java.util.UUID
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.FetchType
import javax.persistence.CascadeType

@Entity
class ValueSetEntry extends Equals  {

  @Id
  @BeanProperty
  var id: String = UUID.randomUUID.toString

  @BeanProperty
  @ManyToOne(cascade=Array{CascadeType.ALL}, fetch = FetchType.LAZY)
  @JoinColumn(nullable=false)
  var valueSetVersion: ValueSetVersion = _
  
  @BeanProperty
  var code: String = _

  @BeanProperty
  var codeSystem: String = _

  @BeanProperty
  var codeSystemVersion: String = _

  @BeanProperty
  var uri: String = _

  @BeanProperty
  var href: String = _

  @BeanProperty
  @Column(length = 1024)
  var description: String = _
  
  override def hashCode() = this.id.hashCode
      
  override def equals(other: Any) = other match {
    case that: ValueSetEntry => {
      this.codeSystem == that.codeSystem &&
      this.codeSystemVersion == that.codeSystemVersion &&
      this.code == that.code
    }
    case _ => false
  }
  
  def canEqual(other: Any) = {
    other.isInstanceOf[edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry]
  }

}