package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence.Entity
import scala.reflect.BeanProperty
import java.util.ArrayList
import javax.persistence.OneToMany
import javax.persistence.ElementCollection
import javax.persistence.FetchType
import javax.persistence.Id

@Entity
class ValueSet {
  
  @BeanProperty
  @Id
  var oid:String = _
  
  @BeanProperty
  var name:String = _
  
  @ElementCollection
  var entries:java.util.List[ValueSetEntry] = new ArrayList[ValueSetEntry]()

}