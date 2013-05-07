package edu.mayo.cts2.framework.plugin.service.mat.loader

import java.io.File
import scala.collection.JavaConversions._
import collection.{mutable, Seq}
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.model._
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ValueSetVersionRepository, ChangeSetRepository, ValueSetRepository}
import javax.annotation
import collection.mutable.ArrayBuffer
import edu.mayo.cts2.framework.model.core.types.{ChangeType, ChangeCommitted, FinalizableState}

@Component
class Cts2SpreadSheetLoader extends Loader {

  def VALUE_SET_SHEET = "ValueSet"
  def VALUE_SET_VALUE_SET_COL = "ValueSet"
  def VALUE_SET_VALUE_SET_DEFINITION_COL = "ValueSetDefinition"
  def VALUE_SET_CODE_SYSTEM_COL = "CodeSystem"
  def VALUE_SET_CODE_SYSTEM_VERSION_COL = "CodeSystemVersion"
  def VALUE_SET_CONCEPT_COL = "Concept"
  def VALUE_SET_DESCRIPTION_COL = "Description"
  def VALUE_SET_VALUE_SET_CELL = 0
  def VALUE_SET_VALUE_SET_DEFINITION_CELL = 1
  def VALUE_SET_CODE_SYSTEM_CELL = 2
  def VALUE_SET_CODE_SYSTEM_VERSION_CELL = 3
  def VALUE_SET_CONCEPT_CELL = 4
  def VALUE_SET_DESCRIPTION_CELL = 5

  def RESOURCES_SHEET = "Resources"
  def RESOURCES_DOMAIN_COL = "Domain"
  def RESOURCES_NAME_COL = "Name"
  def RESOURCES_URI_COL = "URI"
  def RESOURCES_DESCRIPTION_COL = "Description"
  def RESOURCES_HREF_COL = "Href"
  def RESOURCES_URI_PREFIX_COL = "URIPrefix"
  def RESOURCES_DOMAIN_CELL = 0
  def RESOURCES_NAME_CELL = 1
  def RESOURCES_URI_CELL = 2
  def RESOURCES_DESCRIPTION_CELL = 3
  def RESOURCES_HREF_CELL = 4
  def RESOURCES_URI_PREFIX_CELL = 5

  def REFERENCE_TYPE_SHEET = "ReferenceType"
  def REFERENCE_TYPE_DOMAIN_COL = "Domain"
  def REFERENCE_TYPE_NAME_COL = "Name"
  def REFERENCE_TYPE_URI_COL = "URI"
  def REFERENCE_TYPE_DESCRIPTION_COL = "Description"
  def REFERENCE_TYPE_DOMAIN_CELL = 0
  def REFERENCE_TYPE_NAME_CELL = 1
  def REFERENCE_TYPE_URI_CELL = 2
  def REFERENCE_TYPE_DESCRIPTION_CELL = 3

  @annotation.Resource
  var valueSetRepository: ValueSetRepository = _

  @annotation.Resource
  var valueSetVersionRepository: ValueSetVersionRepository = _

  @annotation.Resource
  var changeSetRepository: ChangeSetRepository = _

  class Resource {
    var domain: String = _
    var name: String = _
    var uri: String = _
    var uriPrefix: String = _
    var href: String = _
    var description: String = _
  }

  class ResourcesResult {
    var resourceTypes: mutable.Map[String, mutable.Buffer[Resource]] = mutable.HashMap[String, mutable.Buffer[Resource]]()

    def addResourceType(resource: Resource) {
      resourceTypes.get(resource.domain) match {
        case Some(resources) => resources+=resource
        case None => {
          val buffer = new ArrayBuffer[Resource]
          buffer += resource
          resourceTypes.put(resource.domain, buffer)
        }
      }
    }
  }

  def loadSpreadSheet(file: File) = {
    val wb = WorkbookFactory.create(file)
    val result = loadValueSets(wb, loadResources(wb, loadReferenceTypes(wb)))
    Loader.getXmlResult(result).toString()
  }

  /*******************************************************/
  /*              Load ReferenceTypes Sheet              */
  /*******************************************************/
  private def loadReferenceTypes(wb: Workbook) = {
    val iter = wb.getSheet(REFERENCE_TYPE_SHEET).rowIterator()
    var domain = ""
    val resources = iter.foldLeft(new ResourcesResult)(
    (result, row) => {
      if (row.getRowNum > 0) {
        val resource = refTypeRowToReferenceType(row, domain)
        domain = resource.domain
        result.addResourceType(resource)
      }
      result
    })

    resources
  }: ResourcesResult

  def refTypeRowToReferenceType(row: Row, domain: String): Resource = {
    val resource = new Resource
    resource.domain = getCellValue(row.getCell(REFERENCE_TYPE_DOMAIN_CELL))
    resource.name = getCellValue(row.getCell(REFERENCE_TYPE_NAME_CELL))
    resource.uri = getCellValue(row.getCell(REFERENCE_TYPE_URI_CELL))
    resource.description = getCellValue(row.getCell(REFERENCE_TYPE_DESCRIPTION_CELL))

    if (resource.domain == null || resource.domain.isEmpty)
      resource.domain = domain

    resource
  }

  /*******************************************************/
  /*                Load Resources Sheet                 */
  /*******************************************************/
  private def loadResources(wb: Workbook, resourcesResults: ResourcesResult) = {
    val iter = wb.getSheet(RESOURCES_SHEET).rowIterator()
    var domain: String = ""
    val resources = iter.foldLeft(resourcesResults)(
      (result, row) => {
        if (row.getRowNum() > 0) {
          val resource = resourceRowToReferenceType(row, domain)
          domain = resource.domain
          result.addResourceType(resource)
        }
        result
      })

    resources
  }: ResourcesResult

  def resourceRowToReferenceType(row: Row, domain: String): Resource = {
    val resource = new Resource
    resource.domain = getCellValue(row.getCell(RESOURCES_DOMAIN_CELL))
    resource.name = getCellValue(row.getCell(RESOURCES_NAME_CELL))
    resource.uri = getCellValue(row.getCell(RESOURCES_URI_CELL))
    resource.description = getCellValue(row.getCell(RESOURCES_DESCRIPTION_CELL))
    resource.href = getCellValue(row.getCell(RESOURCES_HREF_CELL))
    resource.uriPrefix = getCellValue(row.getCell(RESOURCES_URI_PREFIX_CELL))

    if (resource.domain == null || resource.domain.isEmpty)
      resource.domain = domain

    resource
  }

  /*******************************************************/
  /*                 Load ValueSets Sheet                */
  /*******************************************************/
  class EntryContext {
    var codeSystem: String = null
    var codeSystemVersion: String = null
    var valueSet: String = null
    var valueSetDefinition: String = null
  }

  class ValueSetRow {
    var valueSet: String = null
    var valueSetDefinition: String = null
    var codeSystem: String = null
    var codeSystemVersion: String = null
    var concept: String = null
    var description: String = null
  }

  private def createValueSetEntry(vsr: ValueSetRow) = {
    val vse = new ValueSetEntry
    vse.setCode(vsr.concept)
    vse.setDescription(vsr.description)
    vse.setCodeSystem(vsr.codeSystem)
    vse.setCodeSystemVersion(vsr.codeSystemVersion)
    vse
  }

  private def createValueSetVersion(vsr: ValueSetRow, vs: ValueSet, resources: ResourcesResult) = {
    val vsv = new ValueSetVersion
    vsv.setValueSet(vs)
    val uri = resources.resourceTypes.get("VALUE_SET_DEFINITION").get.find(vsd => vsd.name.equalsIgnoreCase(vsr.valueSetDefinition)).get.uri
    vsv.setDocumentUri(uri)
    vsv.setVersion(vsr.valueSetDefinition)
    vsv.setState(FinalizableState.FINAL)
    vsv.setChangeCommitted(ChangeCommitted.COMMITTED)
    vsv
  }

  private def loadValueSets(wb: Workbook, resourcesResults: ResourcesResult) = {
    val valueSetSheet = wb.getSheet(VALUE_SET_SHEET)

    var context = new EntryContext

    val valueSetsResult = new ValueSetsResult
    valueSetSheet.rowIterator().foreach(row => {
      if (row.getRowNum > 0) {
        val valueSetRowResult = rowToValueSetRow(row, context)
        val valueSetRow = valueSetRowResult._1
        val valueSetName = valueSetRow.valueSet
        context = valueSetRowResult._2
        val valueSet = valueSetsResult.valueSets.get(valueSetName) match {
          case Some(vs) => vs
          case None => createValueSet(valueSetName, resourcesResults)
        }

        /* if valueset has definition add to it, else create then add to it */
        var currentVersion = valueSet.currentVersion
        if (currentVersion != null && currentVersion.version.equalsIgnoreCase(valueSetRow.valueSetDefinition)) {
          currentVersion.addEntry(createValueSetEntry(valueSetRow))
        } else {
          /* create new version */
          currentVersion = createValueSetVersion(valueSetRow, valueSet, resourcesResults)
          currentVersion.addEntry(createValueSetEntry(valueSetRow))

          val changeSet = new ValueSetChange
          changeSet.setCreator("CTS2 Spreadsheet Loader")
          changeSet.addVersion(currentVersion)
          currentVersion.setChangeType(ChangeType.CREATE)
          currentVersion.setChangeSetUri(changeSet.getChangeSetUri)
          if (valueSetVersionRepository.findOne(currentVersion.getDocumentUri) == null)
            valueSetVersionRepository save currentVersion
          changeSetRepository save changeSet

          valueSet.addVersion(currentVersion)
        }
        valueSetsResult.valueSets += (valueSetName -> valueSet)
      }
    })
    valueSetsResult.valueSets.foreach(vs => valueSetRepository.save(vs._2))

    valueSetsResult
  }: ValueSetsResult

  def createValueSet(name: String, resources: ResourcesResult) = {
    val res = resources.resourceTypes.get("VALUE_SET").get.find(_.name.equalsIgnoreCase(name)).get
    val vs = new ValueSet
    vs.setName(name)
    vs.setFormalName(name)
    vs.setHref(res href)
    vs.setUri(res uri)
    /* todo: namespace? */
    if (valueSetRepository.findOne(vs.name) == null)
      valueSetRepository save vs
    vs
  }

  def rowToValueSetRow(row: Row, context: EntryContext): (ValueSetRow, EntryContext) = {
    val newContext = context
    val valueSetRow = new ValueSetRow

    var valueSetName = getCellValue(row.getCell(VALUE_SET_VALUE_SET_CELL))
    if (valueSetName == null) {
      valueSetName = newContext.valueSet
    } else {
      newContext.valueSet = valueSetName
    }

    var valueSetDef = getCellValue(row.getCell(VALUE_SET_VALUE_SET_DEFINITION_CELL))
    if (valueSetDef == null) {
      valueSetDef = newContext.valueSetDefinition
    } else {
      newContext.valueSetDefinition = valueSetDef
    }

    var codeSystem = getCellValue(row.getCell(VALUE_SET_CODE_SYSTEM_CELL))
    if (codeSystem == null) {
      codeSystem = newContext.codeSystem
    } else {
      newContext.codeSystem = codeSystem
    }

    var codeSystemVersion = getCellValue(row.getCell(VALUE_SET_CODE_SYSTEM_VERSION_CELL))
    if (codeSystemVersion == null) {
      codeSystemVersion = newContext.codeSystemVersion
    } else {
      newContext.codeSystemVersion = codeSystemVersion
    }

    valueSetRow.valueSet = valueSetName
    valueSetRow.valueSetDefinition = valueSetDef
    valueSetRow.codeSystem = codeSystem
    valueSetRow.codeSystemVersion = codeSystemVersion
    valueSetRow.concept = getCellValue(row.getCell(VALUE_SET_CONCEPT_CELL))
    valueSetRow.description = getCellValue(row.getCell(VALUE_SET_DESCRIPTION_CELL))

    (valueSetRow, newContext)
  }

}
