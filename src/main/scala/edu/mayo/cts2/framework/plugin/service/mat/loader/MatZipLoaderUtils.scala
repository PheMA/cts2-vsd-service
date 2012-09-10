package edu.mayo.cts2.framework.plugin.service.mat.loader

import java.util.zip.ZipFile
import java.io.InputStream
import java.util.Enumeration
import java.util.zip.ZipEntry
import java.io.BufferedInputStream
import org.apache.commons.io.IOUtils
import org.apache.poi.poifs.filesystem.POIFSFileSystem
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import scala.collection.JavaConversions._
import java.io.ByteArrayInputStream
import org.apache.commons.io.FileUtils
import java.io.File

object MatZipLoaderUtils {

  def doWithCombinedMatZip(zip: ZipFile, fn: (MatZip => Unit)) = {
    try {
      var xml: BufferedInputStream = null
      var spreadSheet: BufferedInputStream = null;

      val entries = zip.entries();

      while (entries.hasMoreElements()) {
        val zipEntry = entries.nextElement()

        val name = zipEntry.getName()
        if (!name.startsWith("__MACOSX/")) {

          if (name.endsWith(".xml")) {
            xml = new BufferedInputStream(zip.getInputStream(zipEntry))
          }
          if (name.endsWith(".xls")) {
            spreadSheet = new BufferedInputStream(zip.getInputStream(zipEntry))
          }
        }

        if (xml != null && spreadSheet != null) {
          fn(new MatZip(spreadSheet, xml))
          xml = null
          spreadSheet = null
        }
      }

    } finally {
      zip.close()
    }
  }

  def doWithMatZip(zip: ZipFile, fn: (MatZip => Unit)) = {
    try {
      var xml: BufferedInputStream = null
      var spreadSheet: BufferedInputStream = null;

      val entries = zip.entries();

      while (entries.hasMoreElements()) {
        val zipEntry = entries.nextElement()
        val name = zipEntry.getName()
        if (!name.startsWith("__MACOSX/")) {

          if (name.endsWith(".xml")) {
            xml = new BufferedInputStream(zip.getInputStream(zipEntry))
          }
          if (name.endsWith(".xls")) {
            spreadSheet = new BufferedInputStream(zip.getInputStream(zipEntry))
          }
        }
      }

      fn(new MatZip(spreadSheet, xml))
    } finally {
      zip.close()
    }
  }
}