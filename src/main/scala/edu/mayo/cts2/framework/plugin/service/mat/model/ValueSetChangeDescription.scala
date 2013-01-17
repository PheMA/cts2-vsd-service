package edu.mayo.cts2.framework.plugin.service.mat.model

import java.util.{Calendar, UUID}
import javax.persistence._
import reflect.BeanProperty
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, ChangeType}

@Entity
class ValueSetChangeDescription {

  @Id
  val id = UUID.randomUUID.toString

  @BeanProperty
  @Column(nullable = false)
  var changeType: ChangeType = _

  @BeanProperty
  @Column(nullable = false)
  var committed: ChangeCommitted = ChangeCommitted.PENDING

  @BeanProperty
  @Column(nullable = false)
  var containingChangeSet: String = _

  @BeanProperty
  var prevChangeSet: String = _

  @BeanProperty
  var date: Calendar = Calendar.getInstance

  @BeanProperty
  var author: String = _

}
