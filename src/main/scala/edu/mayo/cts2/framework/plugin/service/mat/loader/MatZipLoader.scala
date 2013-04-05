package edu.mayo.cts2.framework.plugin.service.mat.loader

import java.io.BufferedInputStream
import java.util.zip.ZipFile
import scala.collection.JavaConversions._
import scala.collection.Seq
import org.apache.commons.lang.StringUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.model._
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ChangeSetRepository, ValueSetRepository}
import javax.annotation.Resource
import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, ChangeType, FinalizableState}

@Component
class MatZipLoader extends Loader {

  def GROUPING_CODE_SYSTEM = "GROUPING"

  @scala.reflect.BeanProperty
  var fetchCptDescriptions: java.lang.Boolean = false
    
  @Resource  
  var loaderUts: MatZipLoaderUTS = _  

  /* This seems to be the new version of the excel
  def DEVELOPER_CELL = 0
  def OID_CELL = 1
  def LAST_MODIFIED = 2
  def NAME_CELL = 3
  def QDM_CATEGORY_CELL = 4
  def CODE_SYSTEM_CELL = 5
  def CODE_SYSTEM_VERSION_CELL = 6
  def CODE_CELL = 7
  def DESCRIPTOR_CELL = 8
  */

  def DEVELOPER_CELL = 0
  def OID_CELL = 1
  def NAME_CELL = 2
  def QDM_CATEGORY_CELL = 3
  def CODE_SYSTEM_CELL = 4
  def CODE_SYSTEM_VERSION_CELL = 5
  def CODE_CELL = 6
  def DESCRIPTOR_CELL = 7

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Resource
  var changeSetRepository: ChangeSetRepository = _


  def loadCombinedMatZip(zip: ZipFile) {
    MatZipLoaderUtils.doWithCombinedMatZip(zip, processSpreadSheet)
  }

  def loadMatZip(zip: ZipFile) {
    MatZipLoaderUtils.doWithMatZip(zip, processSpreadSheet)
  }

  class SpreadSheetResult {
    var valueSets: Map[String, ValueSet] = Map[String, ValueSet]()
    var valueSetEntries: Map[String, Seq[ValueSetEntry]] = Map[String, Seq[ValueSetEntry]]()
  }

  private def processSpreadSheet = (zip: MatZip) => {
    val result = loadSheets(zip)

    val valueSets = result.valueSets.foldLeft(Set[ValueSet]())((set, mapEntry) => {
      val valueSet = mapEntry._2

      val foundEntries = result.valueSetEntries.get(mapEntry._1)
      if(foundEntries.isDefined){
        valueSet.currentVersion.addEntries(foundEntries.get)
      }
      if(!valueSetRepository.exists(valueSet.name)){
        set + valueSet
      }
      else {
        set
      }
    })

    valueSets.foreach(vs => {
      val change = new ValueSetChange
      change.setCreator("MAT Authoring Tool")
      change.addVersion(vs.currentVersion)
      vs.currentVersion.setChangeType(ChangeType.CREATE)
      vs.currentVersion.setChangeSetUri(change.getChangeSetUri)
      valueSetRepository.save(vs)
      changeSetRepository.save(change)
    })
  }: Unit

  private def getSheetRowIterator(wb:HSSFWorkbook) = {
    var sheetIterator:Iterator[Row] = Iterator()
    for(i <- 1 until wb.getNumberOfSheets) {
      val itr = wb.getSheetAt(i).rowIterator
      itr.next
      sheetIterator = sheetIterator ++ itr
    }

    sheetIterator
  } : Iterator[Row]

  private def loadSheets(zip: MatZip) = {

    val fs = new POIFSFileSystem(zip.spreadSheet)
    val wb = new HSSFWorkbook(fs)

    val sheetIterator = getSheetRowIterator(wb)

    val spreadSheetResult = sheetIterator.foldLeft(new SpreadSheetResult())(
      (result, row) => {

        val valueSet = rowToValueSet(row)
        if (valueSet != null) {

          val name = valueSet.name
          val valueSetEntry = rowToValueSetEntry(row)

          if (!result.valueSets.contains(name)) {
            result.valueSets += (name -> valueSet)
          }


         if (!valueSetEntry.codeSystem.equalsIgnoreCase(GROUPING_CODE_SYSTEM)) {
            if (!result.valueSetEntries.contains(name)) {
              result.valueSetEntries += (name -> Seq(valueSetEntry))
            } else {
              result.valueSetEntries = result.valueSetEntries updated (name, (result.valueSetEntries.get(name).get ++ Seq(valueSetEntry)))
            }
          } else {
            result.valueSets.get(name).get.currentVersion.includesValueSets += valueSetEntry.code
          }

        }
        
        result
      })

    spreadSheetResult
  }: SpreadSheetResult

  def rowToValueSet(row: Row): ValueSet = {
    val valueSet = new ValueSet()
    if (!validateCell(row,OID_CELL) || !validateCell(row,NAME_CELL)) {
      return null
    }
    valueSet.name = getCellValue(row.getCell(OID_CELL))
    valueSet.formalName = getCellValue(row.getCell(NAME_CELL))
    valueSet.uri = "urn:oid:" + valueSet.name
    valueSet.addVersion(new ValueSetVersion())
    valueSet.currentVersion.setVersion("1")
    valueSet.currentVersion.setState(FinalizableState.FINAL)
    valueSet.currentVersion.setChangeCommitted(ChangeCommitted.COMMITTED)
    valueSet.currentVersion.creator = getCellValue(row.getCell(DEVELOPER_CELL))
    valueSet.currentVersion.qdmCategory = getCellValue(row.getCell(QDM_CATEGORY_CELL))

    valueSet
  }
  
  private def validateCell(row:Row, cell:Int) = {
    row.getCell(OID_CELL) != null && StringUtils.isNotBlank(getCellValue(row.getCell(OID_CELL)))
  } :Boolean

  def rowToValueSetEntry(row: Row): ValueSetEntry = {
    val valueSetEntry = new ValueSetEntry()

    valueSetEntry.code = getCellValue(row.getCell(CODE_CELL))

    valueSetEntry.codeSystem = getCellValue(row.getCell(CODE_SYSTEM_CELL))
    valueSetEntry.codeSystemVersion = getCellValue(row.getCell(CODE_SYSTEM_VERSION_CELL))
    
    val description = getCellValue(row.getCell(DESCRIPTOR_CELL))
    if(StringUtils.isBlank(description) &&
        fetchCptDescriptions && 
        valueSetEntry.codeSystem.equals("CPT")){
    	valueSetEntry.description = loaderUts.getDescriptionFromUTS(valueSetEntry.codeSystem, valueSetEntry.code)
    }
    else{
          valueSetEntry.description = description
    }

    valueSetEntry
  }
}

class MatZip(val spreadSheet: BufferedInputStream, val xml: BufferedInputStream)