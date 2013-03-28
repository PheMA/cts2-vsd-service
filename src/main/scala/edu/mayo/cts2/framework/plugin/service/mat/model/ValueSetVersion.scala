package edu.mayo.cts2.framework.plugin.service.mat.model

import scala.collection.JavaConversions._
import scala.reflect.BeanProperty
import javax.persistence._
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, ChangeType, FinalizableState}
import java.util

@Entity
class ValueSetVersion extends Equals {

  /*************************/
  /* Definition Properties */
  /*************************/
  @Id
  @BeanProperty
  var documentUri: String = util.UUID.randomUUID.toString

  @BeanProperty
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable=false)
  var valueSet: ValueSet = _

  @BeanProperty
  var version: String = _

  @BeanProperty
  var creator: String = _

  @BeanProperty
  var notes: String = _

  @BeanProperty
  var status: String = "active"

  @BeanProperty
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  var state: FinalizableState = FinalizableState.OPEN

  @BeanProperty
  var qdmCategory: String = _

  @BeanProperty
  var valueSetType: String = _

  @BeanProperty
  var source: String = _

  @BeanProperty
  var binding: String = _

  @BeanProperty
  var synopsis: String = _

  @BeanProperty
  var successor: String = _

  @BeanProperty
  @OneToMany(mappedBy="valueSetVersion", fetch = FetchType.LAZY, cascade = Array{CascadeType.ALL})
  val entries: util.List[ValueSetEntry] = new util.ArrayList[ValueSetEntry]()
  
  def addEntry(entry:ValueSetEntry) = {
    entry.valueSetVersion = this
    entries.add(entry)
  }
  
  def addEntries(entries:Seq[ValueSetEntry]) {
    entries.foreach(addEntry(_))
  }

  @ElementCollection
  var includesValueSets: util.List[String] = new util.ArrayList[String]()

  /*********************************/
  /* Change Description Properties */
  /*********************************/
  @BeanProperty
  var changeSetUri: String = _

  @BeanProperty
  var prevChangeSetUri: String = _

  @Enumerated(EnumType.STRING)
  @BeanProperty
  var changeType: ChangeType = _

  @Enumerated(EnumType.STRING)
  @BeanProperty
  var changeCommitted: ChangeCommitted = ChangeCommitted.PENDING

  @BeanProperty
  var revisionDate = util.Calendar.getInstance

  override def hashCode() = this.documentUri.hashCode
      
  override def equals(other: Any) = other match {
    case that: ValueSetVersion => this.documentUri == that.documentUri
    case _ => false
  }
  
  def canEqual(other: Any) = {
    other.isInstanceOf[edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion]
  }

}
