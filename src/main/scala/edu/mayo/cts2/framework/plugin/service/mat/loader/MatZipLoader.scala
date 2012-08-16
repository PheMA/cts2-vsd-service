package edu.mayo.cts2.framework.plugin.service.mat.loader

import org.springframework.stereotype.Component
import java.io.InputStream
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFSheet
import scala.collection.JavaConversions._
import java.util.zip.ZipFile
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import org.apache.poi.ss.usermodel.Row
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import scala.collection.Seq
import java.io.BufferedInputStream
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import javax.annotation.Resource

@Component
class MatZipLoader {

  def GROUPING_CODE_SYSTEM = "GROUPING"

  def DEVELOPER_CELL = 0
  def OID_CELL = 1
  def NAME_CELL = 2
  def QDM_CATEGORY_CELL = 3
  def CODE_SYSTEM_CELL = 4
  def CODE_SYSTEM_VERSION_CELL = 5
  def CODE_CELL = 6
  def DESCRIPTOR_CELL = 7
  
  @Resource
  var valueSetRepository:ValueSetRepository = _

  def loadMatZip(zip: ZipFile): Unit = {
    MatZipLoaderUtils.doWithMatZip(zip, processSpreadSheet)
  }

  class SpreadSheetResult {
    var valueSets: Map[String, ValueSet] = Map[String, ValueSet]()
    var valueSetEntries: Map[String, Seq[ValueSetEntry]] = Map[String, Seq[ValueSetEntry]]()
    var groupEntries: Map[String, Seq[String]] = Map[String, Seq[String]]()
  }

  private def processSpreadSheet = (zip: MatZip) => {
    val result = loadSheets(zip)

    val valueSets = result.valueSets.foldLeft(Set[ValueSet]())((set, mapEntry) => {
      val valueSet = mapEntry._2
      valueSet.entries = (result.valueSetEntries.get(mapEntry._1).get)

      set + valueSet
    })
    
    valueSetRepository.save(valueSets)
  }:Unit

  private def loadSheets(zip: MatZip) = {

    val fs = new POIFSFileSystem(zip.spreadSheet)
    val wb = new HSSFWorkbook(fs);

    val sheetIterator = wb.getSheetAt(1).rowIterator ++ wb.getSheetAt(2).rowIterator

    val spreadSheetResult = sheetIterator.foldLeft(new SpreadSheetResult())(
      (result, row) => {
        val valueSet = rowToValueSet(row)
        val oid = valueSet.oid
        val valueSetEntry = rowToValueSetEntry(row)

        if (!result.valueSets.contains(oid)) {
          result.valueSets += (oid -> valueSet)
        }

        if (!valueSetEntry.codeSystem.equals(GROUPING_CODE_SYSTEM)) {
          if (!result.valueSetEntries.contains(oid)) {
            result.valueSetEntries += (oid -> Seq(valueSetEntry))
          } else {
            result.valueSetEntries = result.valueSetEntries updated (oid, (result.valueSetEntries.get(oid).get ++ Seq(valueSetEntry)))
          }
        } else {
          if (!result.groupEntries.contains(oid)) {
            result.groupEntries += (oid -> Seq(valueSetEntry.code))
          } else {
            result.groupEntries = result.groupEntries updated (oid, (result.groupEntries.get(oid).get ++ Seq(valueSetEntry.code)))
          }
        }

        result
      })

    spreadSheetResult.groupEntries.foreach((entry) => {
      val entries = entry._2.foldLeft(Seq[ValueSetEntry]())((seq, groupOid) => {
        seq ++ getGroupCodes(groupOid, spreadSheetResult)
      })

      if (!spreadSheetResult.valueSetEntries.contains(entry._1)) {
        spreadSheetResult.valueSetEntries += (entry._1 -> entries)
      } else {
        spreadSheetResult.valueSetEntries = spreadSheetResult.valueSetEntries updated
          (entry._1, (spreadSheetResult.valueSetEntries.get(entry._1).get ++ entries))
      }

    })

    spreadSheetResult
  }: SpreadSheetResult

  def getGroupCodes(oid: String, spreadSheet: SpreadSheetResult): Seq[ValueSetEntry] = {
    val groupOids = spreadSheet.groupEntries.getOrElse(oid, return Seq[ValueSetEntry]())

    groupOids.foldLeft(Seq[ValueSetEntry]())((entries, groupOid) => {
      entries ++ getGroupCodes(groupOid, spreadSheet) ++ spreadSheet.valueSetEntries.getOrElse(groupOid, Seq())
    })
  }

  def rowToValueSet(row: Row): ValueSet = {
    val valueSet = new ValueSet()
    valueSet.oid = row.getCell(OID_CELL).getStringCellValue
    valueSet.name = row.getCell(NAME_CELL).getStringCellValue
    valueSet.valueSetDeveloper = row.getCell(DEVELOPER_CELL).getStringCellValue
    valueSet.qdmCategory = row.getCell(QDM_CATEGORY_CELL).getStringCellValue

    valueSet
  }

  def rowToValueSetEntry(row: Row): ValueSetEntry = {
    val valueSetEntry = new ValueSetEntry()
    valueSetEntry.code = row.getCell(CODE_CELL).getStringCellValue
    valueSetEntry.description = row.getCell(DESCRIPTOR_CELL).getStringCellValue
    valueSetEntry.codeSystem = row.getCell(CODE_SYSTEM_CELL).getStringCellValue
    valueSetEntry.codeSystemVersion = row.getCell(CODE_SYSTEM_VERSION_CELL).getStringCellValue

    valueSetEntry
  }
}

class MatZip(val spreadSheet: BufferedInputStream, val xml: BufferedInputStream)