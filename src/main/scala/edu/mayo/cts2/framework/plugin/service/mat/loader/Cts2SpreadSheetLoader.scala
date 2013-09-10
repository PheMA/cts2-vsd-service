package edu.mayo.cts2.framework.plugin.service.mat.loader

import java.io.File
import scala.collection.JavaConversions._
import collection.mutable
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.Row
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.plugin.service.mat.model._
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ValueSetVersionRepository, ChangeSetRepository, ValueSetRepository}
import javax.annotation
import collection.mutable.ArrayBuffer
import edu.mayo.cts2.framework.model.core.types.{ChangeType, ChangeCommitted, FinalizableState}
import edu.mayo.cts2.framework.plugin.service.mat.uri.{IdType, UriResolver}

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

  @annotation.Resource
  var uriResolver: UriResolver = _

  class Resource {
    var domain: String = _
    var name: String = _
    var uri: String = _
    var uriPrefix: String = _
    var href: String = _
    var description: String = _
  }

  case class ResourcesResult(workbook: Workbook) {
    var resourceTypes: mutable.Map[String, mutable.Buffer[Resource]] = mutable.HashMap[String, mutable.Buffer[Resource]]()
    val status = new ValueSetsResult

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

    val result = loadValueSets(
      loadResources(
        loadReferenceTypes(wb)))

    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + Loader.getXmlResult(result).toString()
  }

  /*******************************************************/
  /*              Load ReferenceTypes Sheet              */
  /*******************************************************/
  private def loadReferenceTypes(wb: Workbook): ResourcesResult = {
    val resourcesResult = new ResourcesResult(wb)
    val refTypeSheet = wb.getSheet(REFERENCE_TYPE_SHEET)
    if (refTypeSheet != null) {
      val iter = wb.getSheet(REFERENCE_TYPE_SHEET).rowIterator()
      var domain = ""
      iter.foldLeft(resourcesResult)(
      (result, row) => {
        if (row.getRowNum > 0) {
          val resource = refTypeRowToReferenceType(row, domain)
          domain = resource.domain
          result.addResourceType(resource)
        }
        result
      })
      resourcesResult
    } else {
      resourcesResult.status.errors = true
      resourcesResult.status.messages += "Error: %s sheet was not found, canceled load.".format(REFERENCE_TYPE_SHEET)
      resourcesResult
    }
  }

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

  private def getCodeSystem(codeSystem: String): String = uriResolver.idToName(codeSystem, IdType.CODE_SYSTEM)

  /*******************************************************/
  /*                Load Resources Sheet                 */
  /*******************************************************/
  private def loadResources(resourceResults: ResourcesResult): ResourcesResult = {
    if (!resourceResults.status.errors) {
      val resSheet = resourceResults.workbook.getSheet(RESOURCES_SHEET)
      if (resSheet != null) {
        val iter = resSheet.rowIterator()
        var domain: String = ""
        iter.foldLeft(resourceResults)(
          (result, row) => {
            if (row.getRowNum > 0) {
              val resource = resourceRowToReferenceType(row, domain)
              domain = resource.domain
              result.addResourceType(resource)
            }
            result
          })
      } else {
        resourceResults.status.errors = true
        resourceResults.status.messages += "Error: %s sheet was not found, canceled load.".format(RESOURCES_SHEET)
        resourceResults
      }
    } else {
      resourceResults.status.messages += "Error: Could not load the resources because of previous errors."
      resourceResults
    }
  }

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
    var valueSetName: String = null
    var valueSetDefinition: String = null
    var valueSet: Option[ValueSet] = None
  }

  case class ValueSetRow(
    valueSet: String,
    valueSetDefinition: String,
    codeSystem: String,
    codeSystemVersion: String,
    concept: String,
    description: String
  )

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

  private def loadValueSets(resourcesResult: ResourcesResult): ValueSetsResult = {
    if (resourcesResult.status.errors) {
      resourcesResult.status.messages+= "Error: Could not load the value sets because of previous errors."
      resourcesResult.status
    } else {
      val defMap = buildValueSetMap(resourcesResult)

      if (!resourcesResult.status.errors) {
        defMap.entrySet().foldLeft(resourcesResult.status)((valueSetsResult, entry) => {
          // check if valueset exisits in service
          var valueSet = valueSetRepository.findOne(entry.getValue.valueSet.name)
          val version = entry.getValue.definition

          if (valueSet == null) {
            try {
              valueSetRepository.save(entry.getValue.valueSet)
              valueSet = valueSetRepository.findOne(entry.getValue.valueSet.name)
            } catch {
              case e: Exception => {
                valueSetsResult.errors = true
                valueSetsResult.messages += "An error occurred while saving the value set. Error: %s".format(e.getMessage)
              }
            }
          }

          // check if value set def exists in service
          val definition = valueSetVersionRepository.findByValueSetNameAndValueSetVersion(valueSet.name, version.version)
          if (definition == null) {
            val changeSet = new ValueSetChange
            changeSet.setCreator("CTS2 Spreadsheet Loader")
            changeSet.addVersion(version)
            version.setChangeType(ChangeType.CREATE)
            version.setChangeSetUri(changeSet.getChangeSetUri)
            valueSet.addVersion(version)

            try {
              valueSetVersionRepository.save(entry.getValue.definition)
              changeSetRepository.save(changeSet)
              valueSetRepository.save(valueSet)
            } catch {
              case e: Exception => {
                valueSetsResult.errors = true
                valueSetsResult.messages += "An error occurred while saving the value set definition. Error: %s".format(e.getMessage)
              }
            }
          } else {
            valueSetsResult.messages += "Duplicate definition, %s/%s already exists in the service and was not recreated.".format(valueSet.name, version.version)
          }
          val vs = (valueSet.name, version.version, version.entries.size)
          valueSetsResult.valueSets += vs
          valueSetsResult
        })
      } else {
        resourcesResult.status.messages += "Error: Could not load the value sets because of previous errors."
        resourcesResult.status
      }
    }
  }

  private def buildValueSetMap(resourcesResults: ResourcesResult):
  Map[String, DefinitionResult] = {
    var context = new EntryContext

    val valueSetSheet = resourcesResults.workbook.getSheet(VALUE_SET_SHEET)
    if (valueSetSheet != null) {
      valueSetSheet.rowIterator().foldLeft(mutable.Map.empty[String, DefinitionResult])((definitionResultMap, row) => {
        if (row.getRowNum > 0) {
          val valueSetRowTuple = rowToValueSetRow(row, context)
          context = valueSetRowTuple._2

          val valueSet = context.valueSet
            .getOrElse(createValueSet(valueSetRowTuple._1.valueSet, resourcesResults, context))

          val version = definitionResultMap.get(valueSet.name) match {
            case Some(v) => v.definition
            case _ => createValueSetVersion(valueSetRowTuple._1, valueSet, resourcesResults)
          }
          val entry = createValueSetEntry(valueSetRowTuple._1)
          version.addEntry(entry)
          definitionResultMap += (valueSet.name -> new DefinitionResult(valueSet, version))

        }
        definitionResultMap
      }).toMap
    } else {
      resourcesResults.status.errors = true
      resourcesResults.status.messages += "Error: %s sheet was not found, canceled load.".format(VALUE_SET_SHEET)
      Map.empty
    }
  }

  def createValueSet(name: String, resources: ResourcesResult, context: EntryContext) = {
    val res = resources.resourceTypes.get("VALUE_SET").get.find(_.name.equalsIgnoreCase(name)).get
    val vs = new ValueSet
    vs.setName(name)
    vs.setFormalName(name)
    vs.setHref(res href)
    vs.setUri(res uri)
    context.valueSet = Option(vs)
    vs
  }

  def rowToValueSetRow(row: Row, context: EntryContext): (ValueSetRow, EntryContext) = {
    val newContext = context

    var valueSetName = getCellValue(row.getCell(VALUE_SET_VALUE_SET_CELL))
    if (valueSetName == null) {
      valueSetName = newContext.valueSetName
    } else {
      newContext.valueSetName = valueSetName
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
      newContext.codeSystem = getCodeSystem(codeSystem)
    }

    var codeSystemVersion = getCellValue(row.getCell(VALUE_SET_CODE_SYSTEM_VERSION_CELL))
    if (codeSystemVersion == null) {
      codeSystemVersion = newContext.codeSystemVersion
    } else {
      newContext.codeSystemVersion = codeSystemVersion
    }

    if (context!=newContext) {
      val vsExisting = valueSetRepository.findOneByUri(newContext.valueSetName)
      if (vsExisting != null)
        newContext.valueSet = Option(vsExisting)
    }

    (new ValueSetRow(
      valueSetName,
      valueSetDef,
      codeSystem,
      codeSystemVersion,
      getCellValue(row.getCell(VALUE_SET_CONCEPT_CELL)),
      getCellValue(row.getCell(VALUE_SET_DESCRIPTION_CELL))),
      newContext)
  }

}

class DefinitionResultMap(var definitions: Map[String, Option[DefinitionResult]]) {
  def addDefinition(id: String, definition: Option[DefinitionResult]) {
    definitions += (id -> definition)
  }
}

case class DefinitionResult(valueSet: ValueSet, definition: ValueSetVersion)