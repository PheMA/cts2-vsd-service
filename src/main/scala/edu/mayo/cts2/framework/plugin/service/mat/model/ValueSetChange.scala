package edu.mayo.cts2.framework.plugin.service.mat.model

import java.util.{Date, UUID}
import reflect.BeanProperty
import javax.persistence.{Column, Entity, Id}
import edu.mayo.cts2.framework.model.core.types.{FinalizableState, ChangeCommitted, ChangeType}
import edu.mayo.cts2.framework.model.core.{SourceReference, ChangeSetElementGroup}
import edu.mayo.cts2.framework.model.updates.ChangeSet

@Entity
class ValueSetChange(uuid: String) {

  def this() = this(UUID.randomUUID().toString)

  @Id
  @BeanProperty
  var id: String = uuid

  @BeanProperty
  @Column(nullable = false)
  var changeType: ChangeType = _

  @BeanProperty
  @Column(nullable = false)
  var changeCommitted: ChangeCommitted = ChangeCommitted.PENDING

  @BeanProperty
  @Column(nullable = false)
  var state: FinalizableState = FinalizableState.OPEN

  @BeanProperty
  var prevChange: String = _

  @BeanProperty
  var author: String = _

  @BeanProperty
  var date: Date = new Date

  @BeanProperty
  var name: String = _

  @BeanProperty
  var description: String = _

  def convertToChangeSet: ChangeSet = {
    val changeSet = new ChangeSet
    changeSet.setChangeSetURI(id)
    changeSet.setCreationDate(date)
    val eg = new ChangeSetElementGroup
    val creator = new SourceReference()
    creator.setContent(author)
    eg.setCreator(creator)
    changeSet.setChangeSetElementGroup(eg)
    changeSet.setState(state)
    changeSet
  }

}
