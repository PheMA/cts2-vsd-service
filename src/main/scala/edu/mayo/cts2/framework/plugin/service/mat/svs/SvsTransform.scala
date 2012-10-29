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
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetProperty
import ihe.iti.svs._2008.GroupType
import org.apache.commons.collections.CollectionUtils
import edu.mayo.cts2.framework.plugin.service.mat.model.PropertyQualifier

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
    val valueSet = new ValueSet(svsValueSet.getID)
    valueSet.name = svsValueSet.getID
    valueSet.formalName = svsValueSet.getDisplayName

    val valueSetVersion = new ValueSetVersion() 
    valueSetVersion.valueSetType = svsValueSet.getType
    valueSetVersion.source = svsValueSet.getSource
    valueSetVersion.binding = svsValueSet.getBinding
    valueSetVersion.revisionDate = svsValueSet.getRevisionDate.toGregorianCalendar
    valueSetVersion.versionId = svsValueSet.getVersion
    valueSetVersion.status = svsValueSet.getStatus
    
    val properties = 
      svsValueSet.getGroup.foldLeft(Seq[ValueSetProperty]())(_ :+ groupToValueSetProperty(_))
      
    valueSet.properties = properties
    
    val entries =
      svsValueSet.getConceptList.getConcept.foldLeft(Seq[ValueSetEntry]())(_ :+ conceptToValueSetEntry(_))

    valueSetVersion.addEntries(entries)
    
    valueSet.addVersion(valueSetVersion)
    
    valueSet
  }

  def groupToValueSetProperty(group: GroupType) = {
    val prop = new ValueSetProperty()
    prop.name = group.getDisplayName
    
    if(CollectionUtils.isNotEmpty(group.getKeyword)){
    	prop.value = group.getKeyword.get(0)
    }
    
    prop.qualifiers.add(new PropertyQualifier("GroupID", group.getID))
    prop.qualifiers.add(new PropertyQualifier("SourceOrganization", group.getSourceOrganization))
    
    prop
  }
  
  def conceptToValueSetEntry(ce: CE) = {
    val entry = new ValueSetEntry()
    entry.code = ce.getCode
    entry.codeSystem = ce.getCodeSystemName
    entry.codeSystemVersion = ce.getCodeSystemVersion
    entry.description = ce.getDisplayName

    entry
  }
}