package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence.Entity
import scala.reflect.BeanProperty
import java.util.ArrayList
import javax.persistence.OneToMany
import javax.persistence.ElementCollection
import javax.persistence.FetchType
import javax.persistence.Id
import scala.collection.JavaConversions._
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Column
import java.util.Date
import java.util.Calendar
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.ManyToOne
import javax.persistence.JoinColumn
import javax.persistence.Table
import org.hibernate.annotations.GenericGenerator
import java.util.UUID

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

  @ElementCollection
  var entries: java.util.List[ValueSetEntry] = new ArrayList[ValueSetEntry]()

  @ElementCollection
  var includesValueSets: java.util.List[String] = new ArrayList[String]()

  def canEqual(other: Any) = {
    other.isInstanceOf[edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion]
  }

}
