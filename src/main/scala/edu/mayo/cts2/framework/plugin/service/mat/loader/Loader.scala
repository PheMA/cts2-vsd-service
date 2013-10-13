package edu.mayo.cts2.framework.plugin.service.mat.loader

import org.apache.poi.ss.usermodel.Cell
import scala.xml
//import scala.collection.Seq
import scala.collection._

trait Loader {
  def getCellValue(cell:Cell): Option[String] = {
    Option(cell) match {
      case Some(c) => {
        cell.getCellType match {
          case Cell.CELL_TYPE_STRING => Option(cell.getStringCellValue)
          case Cell.CELL_TYPE_NUMERIC => Option(cell.toString)
          case _ => None
        }
      }
      case _ => None
    }
  }
}

class ValueSetsResult {
  var valueSets = mutable.ArrayBuffer.empty[(String, String, Int)]
  var errors = false
  var messages = mutable.ArrayBuffer.empty[String]
}

object Loader {

  def getXmlResult(result: ValueSetsResult): xml.Elem = {
    <ValueSetImportResult><Status>{
      if (result.errors) {
        "error"
      } else {
        "success"
      }
    }</Status><ValueSets>{
      for (vs <- result.valueSets) yield <ValueSet name={vs._1} version={vs._2} entries={vs._3.toString} />
    }</ValueSets><Messages>{
      for (msg <- result.messages) yield <Message>{msg}</Message>
    }</Messages></ValueSetImportResult>
  }

}