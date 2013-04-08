package edu.mayo.cts2.framework.plugin.service.mat.loader

import org.apache.poi.ss.usermodel.Cell
import scala.xml
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import scala.collection.Seq

trait Loader {
  def getCellValue(cell:Cell) = {
    if (cell != null) {
      val cellType = cell.getCellType

      cellType match {
        case Cell.CELL_TYPE_STRING => cell.getStringCellValue
        case Cell.CELL_TYPE_NUMERIC => cell.getNumericCellValue.asInstanceOf[Int].toString
        case Cell.CELL_TYPE_BLANK => null
        case _ => throw new IllegalStateException("Found a Cell of type: " + cellType)
      }
    } else {
      null
    }
  }
}

class ValueSetsResult {
  var valueSets = Map[String, ValueSet]()
  var errors = false
  var messages: Seq[String] = IndexedSeq[String]()
}

object Loader {

  def getXmlResult(result: ValueSetsResult): xml.Elem = {
    <upload><status>{
      if (result.errors) {
        "error"
      } else {
        "success"
      }
    }</status><valuesets>{
      for (vs <- result.valueSets) yield <valueset name={vs._1} version={vs._2.currentVersion.version} entries={vs._2.currentVersion.getEntries.size.toString} />
    }</valuesets><messages>{
      for (msg <- result.messages) yield <message>{msg}</message>
    }</messages></upload>
  }

}