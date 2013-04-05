package edu.mayo.cts2.framework.plugin.service.mat.loader

import org.apache.poi.ss.usermodel.Cell

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
