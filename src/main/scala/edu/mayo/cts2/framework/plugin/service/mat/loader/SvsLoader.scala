package edu.mayo.cts2.framework.plugin.service.mat.loader

import scala.collection.JavaConversions._
import scala.collection.JavaConversions.asScalaBuffer
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import ihe.iti.svs._2008.DescribedValueSet
import ihe.iti.svs._2008.RetrieveMultipleValueSetsResponseType
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.svs.SvsTransform
import java.util.zip.ZipFile
import javax.xml.bind.JAXBContext
import java.io.InputStream
import javax.xml.bind.JAXBElement
import java.util.zip.ZipEntry

@Component
class SvsLoader {
  
  val BATCH_SIZE = 10

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Resource
  var svsTransform: SvsTransform = _

  def loadSvsZip(zip: ZipFile): Unit = {
    try {
      val entries = zip.entries();

      var buffer = Seq[ZipEntry]()
      
      while (entries.hasMoreElements()) {
        val zipEntry = entries.nextElement()
        
        buffer = buffer :+ zipEntry

        if(buffer.size >= BATCH_SIZE){
          flushBuffer(buffer,zip)
          buffer = Seq[ZipEntry]()
        }
      }
      
      flushBuffer(buffer,zip)

    } finally {
      zip.close()
    }
  }
  
  def flushBuffer(buffer: Seq[ZipEntry], zip: ZipFile) = {
    loadRetrieveMultipleValueSetsResponse(
        buffer.foldLeft(Seq[RetrieveMultipleValueSetsResponseType]())(
            (seq,entry) => seq :+ unmarshall(zip.getInputStream(entry))))
  }

  def loadSvsXml(inputStream: InputStream): Unit = {
    try {
      loadRetrieveMultipleValueSetsResponse(unmarshall(inputStream))
    } finally {
      inputStream.close()
    }
  }

  def unmarshall(stream: InputStream): RetrieveMultipleValueSetsResponseType = {
    try {
      val jc = JAXBContext.newInstance(classOf[RetrieveMultipleValueSetsResponseType])
      val u = jc.createUnmarshaller()
      val obj = u.unmarshal(stream)

      obj.asInstanceOf[JAXBElement[RetrieveMultipleValueSetsResponseType]].getValue
    } finally {
      stream.close()
    }
  }

  def loadRetrieveMultipleValueSetsResponse(svs: Seq[RetrieveMultipleValueSetsResponseType]): Unit = {
    valueSetRepository.save(svs.foldLeft(Seq[ValueSet]())(_ ++svsTransform.transformMultipleValueSetsResponse(_)))
  }
  
  def loadRetrieveMultipleValueSetsResponse(svs: RetrieveMultipleValueSetsResponseType): Unit = {
    loadRetrieveMultipleValueSetsResponse(Seq(svs))
  }

}