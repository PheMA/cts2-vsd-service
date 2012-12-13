package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence._

import reflect.BeanProperty
import java.util.{UUID, Date}
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, ChangeType}
import edu.mayo.cts2.framework.model.core.{NameAndMeaningReference, SourceReference}

@Entity
class MatChangeDescription {

  @Id
  @BeanProperty
  var id: String = UUID.randomUUID.toString

  @BeanProperty
  @Column(nullable = false)
  var changeType: ChangeType = _

  @BeanProperty
  @Column(nullable = false)
  var changeCommitted: ChangeCommitted = ChangeCommitted.PENDING

  @OneToOne(cascade = Array{CascadeType.ALL})
  var containingChangeSet: MatChangeSet = _

  @OneToOne(cascade = Array{CascadeType.ALL})
  var prevChangeSet: MatChangeSet = _

  @BeanProperty
  var changeDate: Date = _

  @BeanProperty
  var effectiveDate: Date = _

  @BeanProperty
  @Column(length = 1024)
  var changeNotes: String = _

  @BeanProperty
  var changeSource: SourceReference = _

  @BeanProperty
  var clonedResource: NameAndMeaningReference = _

  @BeanProperty
  @OneToOne
  var describedChange: MatChangeSetMember = _

  @BeanProperty
  @OneToOne
  var prevImage: MatChangeSetMember = _

}
