package edu.mayo.cts2.framework.plugin.service.mat.model

import reflect.BeanProperty

import java.util.{UUID, ArrayList, List}

import javax.persistence._
import edu.mayo.cts2.framework.model.core.types.EntryState
import edu.mayo.cts2.framework.model.core.StatusReference
import edu.mayo.cts2.framework.model.updates.ChangeableResource

@Entity
class MatChangeSetMember extends ChangeableResource with Equals {

  @Id
  @BeanProperty
  var id: String = UUID.randomUUID.toString

  @BeanProperty
  @Column(nullable = false)
  var state: EntryState = EntryState.ACTIVE

  @BeanProperty
  var status: StatusReference = _

  @BeanProperty
  @OneToOne(cascade = Array{CascadeType.ALL})
  var containingSet: MatChangeSet = _

  @BeanProperty
  @OneToOne(cascade = Array{CascadeType.ALL})
  var changeDescription: MatChangeDescription = _

  @BeanProperty
  @OneToMany(cascade = Array{CascadeType.ALL}, fetch = FetchType.LAZY)
  var successor: List[MatChangeDescription] = new ArrayList[MatChangeDescription]()


  override def hashCode() = super.hashCode()

  override def equals(other: Any) = other match {
    case that: MatChangeSetMember => this.id == that.id
    case _ => false
  }

  def canEqual(that: Any) = that.isInstanceOf[MatChangeSetMember]

}
