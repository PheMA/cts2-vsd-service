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
  var id: String = uuid

  @BeanProperty
  var author: String = _

  @BeanProperty
  var date = Calendar.getInstance

  @BeanProperty
  var name: String = _

  @BeanProperty
  var description: String = _

  @BeanProperty
  var finalizableState: FinalizableState = FinalizableState.OPEN

  @BeanProperty
  @OneToMany(cascade=Array{CascadeType.ALL}, fetch = FetchType.EAGER)
  var versions: util.List[ValueSetVersion] = new util.ArrayList[ValueSetVersion]()

  def addVersion(version: ValueSetVersion) {
    versions.add(version)
  }

  def convertToChangeSet: ChangeSet = {
    val changeSet = new ChangeSet
    changeSet.setChangeSetURI(id)
    changeSet.setCreationDate(date.getTime)
    val eg = new ChangeSetElementGroup
    val creator = new SourceReference()
    creator.setContent(author)
    eg.setCreator(creator)
    changeSet.setChangeSetElementGroup(eg)
    changeSet.setState(finalizableState)
    changeSet.setMember(convertMembers)
    changeSet
  }

  private def convertMembers: Array[ChangeableResource] = {
    val members = Array[ChangeableResource]()
    versions.foreach(version => null)
    members
  }

}
