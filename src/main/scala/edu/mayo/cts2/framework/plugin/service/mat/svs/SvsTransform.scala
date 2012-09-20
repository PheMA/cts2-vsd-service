package edu.mayo.cts2.framework.plugin.service.mat.svs

import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import ihe.iti.svs._2008.RetrieveMultipleValueSetsResponseType
import javax.annotation.Resource
import ihe.iti.svs._2008.DescribedValueSet
import scala.collection.JavaConversions._
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import ihe.iti.svs._2008.CE

@Component
class SvsTransform {

  def GROUPING_CODE_SYSTEM = "GROUPING"

  @Resource
  var valueSetRepository: ValueSetRepository = _

  def transformMultipleValueSetsResponse(svs: RetrieveMultipleValueSetsResponseType) = {
    svs.getDescribedValueSet.foldLeft(Seq[ValueSet]())(
      (seq, describedVs) => seq :+ describedValueSetToValueSet(describedVs))

  }

  def describedValueSetToValueSet(svsValueSet: DescribedValueSet) = {
    val valueSet = new ValueSet()
    valueSet.name = svsValueSet.getID
    valueSet.oid = svsValueSet.getID
    valueSet.valueSetType = svsValueSet.getType
    valueSet.source = svsValueSet.getSource
    valueSet.binding = svsValueSet.getBinding
    valueSet.version = svsValueSet.getVersion
    valueSet.revisionDate = svsValueSet.getRevisionDate.toGregorianCalendar

    val entries =
      svsValueSet.getConceptList.getConcept.foldLeft(Seq[ValueSetEntry]())(_ :+ conceptToValueSetEntry(_))

    valueSet.entries = entries
    
    valueSet
  }

  def conceptToValueSetEntry(ce: CE) = {
    val entry = new ValueSetEntry()
    entry.code = ce.getCode
    entry.codeSystem = ce.getCodeSystem
    entry.codeSystemVersion = ce.getCodeSystemVersion
    entry.description = ce.getDisplayName

    entry
  }
}