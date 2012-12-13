package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence._
import reflect.BeanProperty
import edu.mayo.cts2.framework.model.core.types.SetOperator
import java.util.{UUID, List, ArrayList}

@Entity
class MatValueSetDefinitionEntry(entryId: String) {

  def this() = this(UUID.randomUUID().toString)

  @Id
  @BeanProperty
  var id: String = entryId

  @BeanProperty
  var operator: SetOperator = SetOperator.UNION

//    @BeanProperty
//    var entryType: ValueSetDefinitionEntryType = _

  @OneToMany
  var referencedEntities: List[ValueSetEntry] = new ArrayList[ValueSetEntry]()

}
