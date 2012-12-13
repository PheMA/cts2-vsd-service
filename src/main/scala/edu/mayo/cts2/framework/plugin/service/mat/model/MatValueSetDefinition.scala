package edu.mayo.cts2.framework.plugin.service.mat.model

import javax.persistence._
import reflect.BeanProperty
import edu.mayo.cts2.framework.model.core._
import java.util.{Date, UUID, List, ArrayList, Map, HashMap}
import edu.mayo.cts2.framework.model.core.types.FinalizableState

@Entity
class MatValueSetDefinition extends ResourceVersionDescription {

  @Id
  var documentURI: String = UUID.randomUUID.toString
  override def getDocumentURI = this.documentURI
  override def setDocumentURI(documentURI: String) { this.documentURI = documentURI }

  var state: FinalizableState = FinalizableState.FINAL
  override def getState = this.state
  override def setState(state: FinalizableState) { this.state = state }

  var predecessorURI: String = _
  override def getPredecessor = {
    val ref = new NameAndMeaningReference()
    ref.setUri(predecessorURI)
    ref
  }
  override def setPredecessor(predecessor: NameAndMeaningReference) { this.predecessorURI = predecessor.getUri }

  var officalReleaseDate: Date = _
  override def getOfficialReleaseDate = officalReleaseDate
  override def setOfficialReleaseDate(date: Date) { this.officalReleaseDate = date }

  var officialActivationDate: Date = _
  override def getOfficialActivationDate = officialActivationDate
  override def setOfficialActivationDate(date: Date) { this.officialActivationDate = date }

  var officialResourceVersionId: String = _
  override def getOfficialResourceVersionId = officialResourceVersionId
  override def setOfficialResourceVersionId(versionId: String) { this.officialResourceVersionId = versionId }

  var about: String = _
  override def getAbout = this.about
  override def setAbout(about: String) { this.about = about }

  var formalName: String = _
  override def getFormalName = this.formalName
  override def setFormalName(name: String) { this.formalName = name }

  @ElementCollection(targetClass = classOf[String])
  var keywordList: List[String] = new ArrayList[String]()

  @ElementCollection(targetClass = classOf[String])
  @MapKeyClass(classOf[String])
  var sourceAndRoleList: Map[String, String] = new HashMap[String, String]()


  @BeanProperty
  @Column(nullable = false)
  var definedValueSet: ValueSetReference = _

  @BeanProperty
  var verisonTag: Array[VersionTagReference] = _

  @BeanProperty
  @OneToMany(cascade = Array { CascadeType.ALL}, fetch = FetchType.LAZY)
  var entries: List[MatValueSetDefinitionEntry] = new ArrayList[MatValueSetDefinitionEntry]()
  def removeEntry(entry: Int) { entries.remove(entry) }

}
