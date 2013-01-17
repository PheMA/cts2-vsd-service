package edu.mayo.cts2.framework.plugin.service.mat.model

import java.util.ArrayList
import java.util.UUID
import scala.collection.JavaConversions._
import scala.reflect.BeanProperty
import javax.persistence._
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import java.util

@Entity
class ValueSetVersion extends Equals {

  @Id
  @BeanProperty
  var id: String = UUID.randomUUID.toString

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable=false)
  var valueSet: ValueSet = _

  @BeanProperty
  var versionId: String = UUID.randomUUID.toString

  @BeanProperty
  var valueSetDeveloper: String = _

  @BeanProperty
  var notes: String = _

  @BeanProperty
  var status: String = "active"

  @BeanProperty
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

  @ManyToOne(cascade = Array{CascadeType.ALL})
  var changeDescription: ValueSetChangeDescription = _
  def setChangeDescription(description: ValueSetChangeDescription) {
    if (Option(changeDescription).isDefined) {
      successor.add(changeDescription)
    }
    changeDescription = description
  }
  def getChangeDescription = changeDescription

  @BeanProperty
  @ElementCollection
  var successor: util.List[ValueSetChangeDescription] = new util.ArrayList[ValueSetChangeDescription]()

  @OneToMany(mappedBy="valueSetVersion", fetch = FetchType.LAZY, cascade = Array{CascadeType.ALL})
  private var _entries: util.List[ValueSetEntry] = new ArrayList[ValueSetEntry]()
  
  def addEntry(entry:ValueSetEntry) = {
    entry.valueSetVersion = this
    _entries.add(entry)
  }
  
  def addEntries(entries:Seq[ValueSetEntry]) {
    entries.foreach(addEntry(_))
  }

  @ElementCollection
  var includesValueSets: util.List[String] = new util.ArrayList[String]()

  override def hashCode() = this.id.hashCode
      
  override def equals(other: Any) = other match {
    case that: ValueSetVersion => this.id == that.id && this.id == that.id
    case _ => false
  }
  
  def canEqual(other: Any) = {
    other.isInstanceOf[edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion]
  }

}
