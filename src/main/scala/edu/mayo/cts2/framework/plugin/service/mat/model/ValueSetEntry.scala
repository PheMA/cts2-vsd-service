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
import org.apache.commons.lang.builder.{EqualsBuilder, HashCodeBuilder}

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
  @Column(length = 1024)
  var description: String = _
  
  override def hashCode() = new HashCodeBuilder()
    .append(codeSystem)
    .append(codeSystemVersion)
    .append(code)
    .toHashCode
      
  override def equals(other: Any) = other match {
    case that: ValueSetEntry => new EqualsBuilder()
      .append(codeSystem, that.codeSystem)
      .append(codeSystemVersion, that.codeSystemVersion)
      .append(code, that.code)
      .isEquals
    case _ => false
  }
  
  def canEqual(other: Any) = classOf[ValueSetEntry].eq(other.getClass)

}