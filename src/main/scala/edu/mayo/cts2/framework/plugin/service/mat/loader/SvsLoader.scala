package edu.mayo.cts2.framework.plugin.service.mat.loader

import scala.collection.JavaConversions.asScalaBuffer
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import ihe.iti.svs._2008.DescribedValueSet
import ihe.iti.svs._2008.RetrieveMultipleValueSetsResponseType
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.svs.SvsTransform

@Component
class SvsLoader {

  @Resource
  var valueSetRepository: ValueSetRepository = _
  
  @Resource
  var svsTransform: SvsTransform = _

  def loadRetrieveMultipleValueSetsResponse(svs: RetrieveMultipleValueSetsResponseType): Unit = {
     svsTransform.transformMultipleValueSetsResponse(svs).foreach(valueSetRepository.save(_))
  }
  
}