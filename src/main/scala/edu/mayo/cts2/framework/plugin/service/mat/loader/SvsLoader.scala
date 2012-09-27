package edu.mayo.cts2.framework.plugin.service.mat.loader

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

@Component
class SvsLoader {

  @Resource
  var valueSetRepository: ValueSetRepository = _

  @Resource
  var svsTransform: SvsTransform = _

  def loadSvsZip(zip: ZipFile): Unit = {
    try {
      val entries = zip.entries();

      while (entries.hasMoreElements()) {
        val zipEntry = entries.nextElement()

        loadRetrieveMultipleValueSetsResponse(unmarshall(zip.getInputStream(zipEntry)))
      }

    } finally {
      zip.close()
    }
  }

  def loadSvsXml(inputStream: InputStream): Unit = {
    try {
      loadRetrieveMultipleValueSetsResponse(unmarshall(inputStream))
    } finally {
      inputStream.close()
    }
  }

  def unmarshall(stream: InputStream) = {
    try {
      val jc = JAXBContext.newInstance(classOf[RetrieveMultipleValueSetsResponseType])
      val u = jc.createUnmarshaller()
      val obj = u.unmarshal(stream)

      obj.asInstanceOf[JAXBElement[RetrieveMultipleValueSetsResponseType]].getValue
    } finally {
      stream.close()
    }
  }: RetrieveMultipleValueSetsResponseType

  def loadRetrieveMultipleValueSetsResponse(svs: RetrieveMultipleValueSetsResponseType): Unit = {
    svsTransform.transformMultipleValueSetsResponse(svs).foreach(valueSetRepository.save(_))
  }

}