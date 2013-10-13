package edu.mayo.cts2.framework.plugin.service.mat.loader

import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ChangeSetRepository, ValueSetVersionRepository, ValueSetRepository}
import java.io.File
import org.apache.poi.ss.usermodel.{Row, Workbook, WorkbookFactory}
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetVersion, ValueSetChange, ValueSetEntry, ValueSet}
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, FinalizableState, ChangeType}
import edu.mayo.cts2.framework.plugin.service.mat.uri.{UriResolver, IdType}
import javax.annotation.Resource
import scala.collection._

@Component
class Nqf2014Loader extends Loader {

  val VALUESET_OID_COL = "Value Set OID"
  val VALUESET_OID_CELL = 7
  val VALUESET_NAME_COL = "Value Set Name"
  val VALUESET_NAME_CELL = 8
  val VALUESET_VERSION_COL = "Value Set Version"
  val VALUESET_VERSION_CELL = 9
  val CODE_SYSTEM_COL = "Code System"
  val CODE_SYSTEM_CELL = 10
  val CODE_SYSTEM_VERSION_COL = "Code System Version"
  val CODE_SYSTEM_VERSION_CELL = 11
  val CONCEPT_COL = "Concept"
  val CONCEPT_CELL = 12
  val CONCEPT_DESC_COL = "Concept Description"
  val CONCEPT_DESC_CELL = 13

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  @Resource
  var changeSetRepository: ChangeSetRepository = _

  @Resource
  var uriResolver: UriResolver = _

  case class ValueSetRow(
    oid: String,
    name: String,
    version: String,
    codeSystem: String,
    codeSystemVersion: String,
    code: String,
    codeDesc: String)

  class ResourcesResult {
    var valueSets = Map[String, ValueSet]()
    var valueSetVersions = Map[(String, String), ValueSetVersion]()
  }

  def loadSpreadSheet(file: File) = {
    val wb = WorkbookFactory.create(file)
    val result = loadValueSets(wb)
    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + Loader.getXmlResult(result).toString
  }

  def loadValueSets(wb: Workbook) = {
    val resourcesResult = new ResourcesResult
    val valueSetsResult = new ValueSetsResult
    wb.getSheetAt(0).rowIterator().foreach(row => {
      if (row.getRowNum > 0) {
        // create value set add to collection
        val vsRow = rowToValueSetRow(row)

        val valueSet = resourcesResult.valueSets.get(vsRow.oid).getOrElse({
          val vs = createValueSet(vsRow)
          resourcesResult.valueSets += (vsRow.oid -> vs)
          vs
        })

        // create entry add to collection
        resourcesResult.valueSetVersions.get((vsRow.oid, vsRow.version)).getOrElse({
          val version = createValueSetVersion(vsRow, valueSet)
          resourcesResult.valueSetVersions += ((vsRow.oid, vsRow.version) -> version)
          version
        }).addEntry(createValueSetEntry(vsRow))

      }
    })

    // 1. save all valuesets
    resourcesResult.valueSets.foreach(vs => {
      if (valueSetRepository.findOne(vs._1) != null) {
        valueSetsResult.messages += "Duplicate value set, %s already exists in the service and was not recreated.".format(vs._1)
      } else {
        valueSetRepository.save(vs._2)
      }
    })

    // 2. save all definitions
    resourcesResult.valueSetVersions.entrySet.foreach(definition => {
      val version = definition.getValue
      val valueSet = resourcesResult.valueSets.get(definition.getKey._1).get

      val vs = (valueSet.name, version.version, version.entries.size)
      valueSetsResult.valueSets += vs

      if (valueSetVersionRepository.findByValueSetNameAndValueSetVersion(valueSet.name, version.version) != null) {
        valueSetsResult.messages += "Duplicate definition, %s/%s already exists in the service and was not recreated.".format(valueSet.name, version.version)
      } else {
        valueSet.addVersion(version)
        val changeSet = new ValueSetChange
        changeSet.setCreator("NQF 2014 Loader")
        changeSet.addVersion(version)
        version.setChangeType(ChangeType.CREATE)
        version.setChangeSetUri(changeSet.getChangeSetUri)
        valueSetVersionRepository.save(version)
        changeSetRepository.save(changeSet)
        valueSetRepository.save(valueSet)
      }
    })

    valueSetsResult
  }: ValueSetsResult


  def rowToValueSetRow(row: Row): ValueSetRow = {
    new ValueSetRow(getCellValue(row.getCell(VALUESET_OID_CELL)).getOrElse(""),
      getCellValue(row.getCell(VALUESET_NAME_CELL)).getOrElse(""),
      getCellValue(row.getCell(VALUESET_VERSION_CELL)).getOrElse(""),
      getCodeSystem(getCellValue(row.getCell(CODE_SYSTEM_CELL)).getOrElse("")),
      getCellValue(row.getCell(CODE_SYSTEM_VERSION_CELL)).getOrElse(""),
      getCellValue(row.getCell(CONCEPT_CELL)).getOrElse(""),
      getCellValue(row.getCell(CONCEPT_DESC_CELL)).getOrElse(""))
  }

  def createValueSet(vsRow: ValueSetRow): ValueSet = {
    val valueSet = new ValueSet
    valueSet.setName(vsRow.oid)
    valueSet.setFormalName(vsRow.name)
    valueSet.setUri("urn:oid:" + vsRow.oid)
    valueSet
  }

  def createValueSetEntry(vsr: ValueSetRow): ValueSetEntry = {
    val vse = new ValueSetEntry
    vse.setCode(vsr.code)
    vse.setDescription(vsr.codeDesc)
    vse.setCodeSystem(vsr.codeSystem)
    vse.setCodeSystemVersion(vsr.codeSystemVersion)
    vse
  }

  def createValueSetVersion(vsr: ValueSetRow, vs: ValueSet): ValueSetVersion = {
    val vsv = new ValueSetVersion
    vsv.setValueSet(vs)
    vsv.setVersion(vsr.version)
    vsv.setState(FinalizableState.FINAL)
    vsv.setChangeCommitted(ChangeCommitted.COMMITTED)
    vsv
  }

  private def getCodeSystem(codeSystem: String): String = {
   uriResolver.idToName(codeSystem, IdType.CODE_SYSTEM)
  }

}
