package edu.mayo.cts2.framework.plugin.service.mat.model

import java.util.ArrayList
import java.util.Calendar
import java.util.UUID
import scala.collection.JavaConversions._
import scala.reflect.BeanProperty
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.LazyCollection
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import org.hibernate.annotations.LazyCollectionOption
import javax.persistence.FetchType
import javax.persistence.CascadeType
import org.hibernate.annotations.IndexColumn

@Entity
class ValueSetVersion extends Equals {
  
  val DEFAULT_VERSION_ID = "1"

  @Id
  @BeanProperty
  var id: String = UUID.randomUUID.toString

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(nullable=false)
  var valueSet: ValueSet = _
  
  @BeanProperty
  var versionId: String = DEFAULT_VERSION_ID

  @BeanProperty
  var valueSetDeveloper: String = _

  @BeanProperty
  var source: String = _

  @BeanProperty
  var valueSetType: String = _

  @BeanProperty
  var binding: String = _

  @BeanProperty
  var status: String = _

  @BeanProperty
  var revisionDate: Calendar = _

  @BeanProperty
  var qdmCategory: String = _

  @OneToMany(mappedBy="valueSetVersion", fetch = FetchType.LAZY, cascade = Array{CascadeType.ALL})
  private var _entries: java.util.List[ValueSetEntry] = new ArrayList[ValueSetEntry]()
  
  def addEntry(entry:ValueSetEntry) = {
    entry.valueSetVersion = this
    _entries.add(entry)
  }
  
  def addEntries(entries:Seq[ValueSetEntry]) = {
    entries.foreach(addEntry(_))
  }

  @ElementCollection
  var includesValueSets: java.util.List[String] = new ArrayList[String]()

  override def hashCode() = this.id.hashCode
      
  override def equals(other: Any) = other match {
    case that: ValueSetVersion => this.id == that.id && this.id == that.id
    case _ => false
  }
  
  def canEqual(other: Any) = {
    other.isInstanceOf[edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion]
  }

}
