package edu.mayo.cts2.framework.plugin.service.mat.loader

import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import javax.annotation
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ChangeSetRepository, ValueSetVersionRepository, ValueSetRepository}
import java.io.File
import org.apache.poi.ss.usermodel.{Row, Workbook, WorkbookFactory}
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetVersion, ValueSetChange, ValueSetEntry, ValueSet}
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, FinalizableState, ChangeType}

@Component
class Nqf2014Loader extends Loader {

//  def MEASURE_ID_COL = "Measure Identifier"
//  def MEASURE_ID_CELL = "0"
//  def MEASURE_TITLE_COL = "Measure Title"
//  def MEASURE_TITLE_CELL = "1"
//  def NQF_ID_COL = "NQF ID"
//  def NQF_ID_CELL = "2"
//  def VERSION_COL = "Version"
//  def VERSION_CELL = "3"
//  def ELIGIBILITY_COL = "Eligibility"
//  def ELIGIBILITY_CELL = "4"
//  def CATEGORY_COL = "Category"
//  def CATEGORY_CELL = "5"
//  def QUALITY_DATE_ELEMENT_COL = "Quality Data Element"
//  def QUALITY_DATE_ELEMENT_CELL = "6"
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

  @annotation.Resource
  var valueSetRepository: ValueSetRepository = _

  @annotation.Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  @annotation.Resource
  var changeSetRepository: ChangeSetRepository = _

  class ValueSetRow {
    var oid: String = _
    var name : String = _
    var version: String = _
    var codeSystem: String = _
    var codeSystemVersion: String = _
    var code: String = _
    var codeDesc: String = _
  }

  def loadSpreadSheet(file: File) = {
    val wb = WorkbookFactory.create(file)
    val result = loadValueSets(wb)
    Loader.getXmlResult(result).toString
  }

  def loadValueSets(wb: Workbook) = {
    val valueSetsResult = new ValueSetsResult
    wb.getSheetAt(0).rowIterator().foreach(row => {
      if (row.getRowNum > 0) {
        val vsRow = rowToValueSetRow(row)
        val valueSet = valueSetsResult.valueSets.get(vsRow.oid) match {
          case Some(vs) => vs
          case None => createValueSet(vsRow)
        }
        var currentVersion = valueSet.currentVersion
        if (currentVersion != null && currentVersion.version.equalsIgnoreCase(vsRow.version)) {
          currentVersion.addEntry(createValueSetEntry(vsRow))
        } else {
          /* create new version */
          currentVersion = createValueSetVersion(vsRow, valueSet)
          currentVersion.addEntry(createValueSetEntry(vsRow))

          val changeSet = new ValueSetChange
          changeSet.setCreator("NQF 2014 Loader")
          changeSet.addVersion(currentVersion)
          currentVersion.setChangeType(ChangeType.CREATE)
          currentVersion.setChangeSetUri(changeSet.getChangeSetUri)
          valueSetVersionRepository save currentVersion
          changeSetRepository save changeSet

          valueSet.addVersion(currentVersion)
        }
        valueSetsResult.valueSets += (vsRow.oid -> valueSet)
      }
    })
    valueSetsResult.valueSets.foreach(vs => valueSetRepository.save(vs._2))

    valueSetsResult
  }: ValueSetsResult


  def rowToValueSetRow(row: Row): ValueSetRow = {
    val vs = new ValueSetRow
    vs.oid = getCellValue(row.getCell(VALUESET_OID_CELL))
    vs.name = getCellValue(row.getCell(VALUESET_NAME_CELL))
    vs.version = getCellValue(row.getCell(VALUESET_VERSION_CELL))
    vs.codeSystem = getCellValue(row.getCell(CODE_SYSTEM_CELL))
    vs.codeSystemVersion = getCellValue(row.getCell(CODE_SYSTEM_VERSION_CELL))
    vs.code = getCellValue(row.getCell(CONCEPT_CELL))
    vs.codeDesc = getCellValue(row.getCell(CONCEPT_DESC_CELL))
    vs
  }

  def createValueSet(vsRow: ValueSetRow): ValueSet = {
    val valueSet = new ValueSet
    valueSet.setName(vsRow.oid)
    valueSet.setFormalName(vsRow.name)
    valueSet.setUri("urn:oid:" + vsRow.oid)
    valueSetRepository.save(valueSet)
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

}
