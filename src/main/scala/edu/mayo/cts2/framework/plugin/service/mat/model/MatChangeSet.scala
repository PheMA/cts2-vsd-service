package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence._

import java.util.{UUID, ArrayList, Date, List}

import edu.mayo.cts2.framework.model.core.types.FinalizableState
import reflect.BeanProperty
import edu.mayo.cts2.framework.model.core.{SourceReference, ChangeSetElementGroup}
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.model.updates.ChangeableResource

@Entity
class MatChangeSet(changeSetUri: String) extends Equals {

  def this() = this(UUID.randomUUID().toString)

  @Id
  @BeanProperty
  var id: String = changeSetUri

  @BeanProperty
  @Column(nullable = false)
  var creationDate: Date = new Date

  @BeanProperty
  var officialEffectiveDate: Date = _

  @BeanProperty
  var closeDate: Date = _

  @BeanProperty
  var creator: String = _

  @BeanProperty
  var changeInstructions: String = _

  @BeanProperty
  var state: FinalizableState = FinalizableState.OPEN

  @BeanProperty
  var entryCount: Long = 0

  @OneToMany(cascade = Array { CascadeType.ALL}, fetch = FetchType.LAZY)
  var memberList: List[MatChangeSetMember] = new ArrayList[MatChangeSetMember]()

  def addMember(member: MatChangeSetMember) = memberList.add(member)

  def addMembers(members: Seq[MatChangeSetMember]) {
    members.foreach(addMember(_))
  }

  def removeMember(member: MatChangeSetMember) = memberList.remove(member)

  def removeMembers(members: Seq[MatChangeSetMember]) {
    members.foreach(removeMember(_))
  }

  def getMembers = new ArrayList[MatChangeSetMember](memberList)

  def getChangeSetElementGroup: ChangeSetElementGroup = {
    val eg = new ChangeSetElementGroup
    if (creator != null) {
      eg.setCreator(createSourceReference(creator))
    }
    if (changeInstructions != null) {
      eg.setChangeInstructions(ModelUtils.createOpaqueData(changeInstructions))
    }
    eg
  }

  def createSourceReference(content: String): SourceReference = {
    val sr = new SourceReference
    sr.setContent(content)
    sr
  }

  override def hashCode() = this.id.hashCode

  override def equals(other: Any) = other match {
    case that: MatChangeSet => this.id == that.id
    case _ => false
  }

  def canEqual(other: Any) = other.isInstanceOf[MatChangeSet]

  def update(changeSet: MatChangeSet) {
    this.officialEffectiveDate = changeSet.officialEffectiveDate
    this.closeDate = changeSet.closeDate
    this.creator = changeSet.creator
    this.changeInstructions = changeSet.changeInstructions
    this.state = changeSet.state
    this.memberList = changeSet.memberList
  }

}
