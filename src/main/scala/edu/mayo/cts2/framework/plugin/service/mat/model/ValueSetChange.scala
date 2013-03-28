package edu.mayo.cts2.framework.plugin.service.mat.model

import reflect.BeanProperty
import javax.persistence._
import edu.mayo.cts2.framework.model.core.{SourceReference, ChangeSetElementGroup}
import edu.mayo.cts2.framework.model.updates.{ChangeableResource, ChangeSet}
import java.util
import scala.collection.JavaConversions._
import util.{UUID, Calendar}
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, FinalizableState}

@Entity
class ValueSetChange(uuid: String) {

  def this() = this(UUID.randomUUID().toString)

  @Id
  @BeanProperty
  var changeSetUri: String = uuid

  @BeanProperty
  var creator: String = _

  @BeanProperty
  var date = Calendar.getInstance

  @BeanProperty
  var closeDate: Calendar = _

  @BeanProperty
  var state: FinalizableState = FinalizableState.OPEN

  @BeanProperty
  var instructions: String = _

  @BeanProperty
  @OneToOne
  var currentVersion: ValueSetVersion = _

  @BeanProperty
  @OneToMany(cascade=Array{CascadeType.ALL}, fetch = FetchType.EAGER)
  var versions: util.List[ValueSetVersion] = new util.ArrayList[ValueSetVersion]()

  def addVersion(version: ValueSetVersion) {
    currentVersion = version
    versions.add(version)
  }

  def convertToChangeSet: ChangeSet = {
    val changeSet = new ChangeSet
    changeSet.setChangeSetURI(changeSetUri)
    changeSet.setCreationDate(date.getTime)
    val eg = new ChangeSetElementGroup
    val creatorRef = new SourceReference()
    creatorRef.setContent(creator)
    eg.setCreator(creatorRef)
    changeSet.setChangeSetElementGroup(eg)
    changeSet.setState(state)
    changeSet.setMember(convertMembers)
    changeSet
  }

  private def convertMembers: Array[ChangeableResource] = {
    val members = Array[ChangeableResource]()
    versions.foreach(version => null)
    members
  }

}
