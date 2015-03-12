// Copyright (c) 2014. Mayo Foundation for Medical Education and Research. All rights reserved.

package edu.mayo.cts2.framework.plugin.service.mat.loader

import java.io.File
import javax.annotation

import edu.mayo.cts2.framework.model.core.types.{ChangeCommitted, ChangeType, FinalizableState}
import edu.mayo.cts2.framework.plugin.service.mat.model._
import edu.mayo.cts2.framework.plugin.service.mat.repository.{ChangeSetRepository, ResourceRepository, ValueSetRepository, ValueSetVersionRepository}
import edu.mayo.cts2.framework.plugin.service.mat.uri.UriResolver
import org.apache.poi.ss.usermodel.{Row, Workbook, WorkbookFactory}
import org.springframework.stereotype.Component

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

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
  var resourceRepository: ResourceRepository = _

  @annotation.Resource
  var uriResolver: UriResolver = _

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
    val domain = getCellValue(row.getCell(REFERENCE_TYPE_DOMAIN_CELL)).getOrElse("")
    val name = getCellValue(row.getCell(REFERENCE_TYPE_NAME_CELL)).getOrElse("")
    val uri = getCellValue(row.getCell(REFERENCE_TYPE_URI_CELL)).getOrElse("")
    val resource = new Resource(name, uri, domain)
    resource.description = getCellValue(row.getCell(REFERENCE_TYPE_DESCRIPTION_CELL)).getOrElse("")

    if (resource.domain == null || resource.domain.isEmpty)
      resource.domain = domain

    resource
  }

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
              resourceRowToReferenceType(row, domain).foreach(resource => {
                if (resource != null) {
                  domain = resource.domain
                  if (resource.name != null && !resource.name.isEmpty) {
                    resourceRepository.save(resource)
                    result.addResourceType(resource)
                  }
                }
              })
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

  def resourceRowToReferenceType(row: Row, domain: String): Option[Resource] = {
    val name = getCellValue(row.getCell(RESOURCES_NAME_CELL)).getOrElse("")
    val uri = getCellValue(row.getCell(RESOURCES_URI_CELL)).getOrElse("")
    val domain = getCellValue(row.getCell(RESOURCES_DOMAIN_CELL)).getOrElse("")
    val resource = new Resource(name, uri, domain)
    resource.description = getCellValue(row.getCell(RESOURCES_DESCRIPTION_CELL)).getOrElse("")
    resource.href = getCellValue(row.getCell(RESOURCES_HREF_CELL)).getOrElse("")
    resource.baseUri = getCellValue(row.getCell(RESOURCES_URI_PREFIX_CELL)).getOrElse(uri)

    if (resource.name != null && !resource.name.isEmpty) {
      if (resource.domain == null || resource.domain.isEmpty)
        resource.domain = domain
      Option(resource)
    } else
      None
  }

  /*******************************************************/
  /*                 Load ValueSets Sheet                */
  /*******************************************************/
  case class EntryContext(
    var codeSystem: String,
    var codeSystemVersion: String,
    var valueSetName: String,
    var valueSetDefinition: String
  )

  case class Code(code: String, description: Option[String])

  case class ValueSetRow(
    context: EntryContext,
    code: Code
  )

  private def createValueSetEntry(vsr: ValueSetRow) = {
    val vse = new ValueSetEntry
    vse.setCode(vsr.code.code)
    vse.setDescription(vsr.code.description.getOrElse(""))
    vse.setCodeSystem(vsr.context.codeSystem)
    vse.setCodeSystemVersion(vsr.context.codeSystemVersion)
    vse
  }

  private def createValueSetVersion(vsr: ValueSetRow, vs: ValueSet, resources: ResourcesResult) = {
    val vsv = new ValueSetVersion
    vsv.setValueSet(vs)
    val uri = resources.resourceTypes.get("VALUE_SET_DEFINITION").get.find(vsd => vsd.name.equalsIgnoreCase(vsr.context.valueSetDefinition)).get.uri
    vsv.setDocumentUri(uri)
    vsv.setVersion(vsr.context.valueSetDefinition)
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
        defMap.entrySet().foldLeft(resourcesResult.status)((status, entry) => {
          // check if valueset exisits in service
          var valueSet = valueSetRepository.findOne(entry.getValue.valueSet.name)

          if (valueSet == null) {
            try {
              valueSetRepository.save(entry.getValue.valueSet)
              valueSet = valueSetRepository.findOne(entry.getValue.valueSet.name)
            } catch {
              case e: Exception => {
                status.errors = true
                status.messages += "An error occurred while saving the value set. Error: %s".format(e.getMessage)
              }
            }
          }

          val version = entry.getValue.definition
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
                status.errors = true
                status.messages += "An error occurred while saving the value set definition. Error: %s".format(e.getMessage)
              }
            }
          } else {
            status.messages += "Duplicate definition, %s/%s already exists in the service and was not recreated.".format(valueSet.name, version.version)
          }
          val vs = (valueSet.name, version.version, version.entries.size)
          status.valueSets += vs
          status
        })
      } else {
        resourcesResult.status.messages += "Error: Could not load the value sets because of previous errors."
        resourcesResult.status
      }
    }
  }

  private def buildValueSetMap(resourcesResults: ResourcesResult):
  Map[String, DefinitionResult] = {
    var context = new EntryContext(null, null, null, null)

    val valueSetSheet = resourcesResults.workbook.getSheet(VALUE_SET_SHEET)
    if (valueSetSheet != null) {
      valueSetSheet.rowIterator().foldLeft(mutable.Map.empty[String, DefinitionResult])((definitionResultMap, row) => {
        if (row.getRowNum > 0) {
          rowToValueSetRow(row, context).foreach(valueSetRow => {
            if (valueSetRow.code.code != null) {
              definitionResultMap.get(valueSetRow.context.valueSetName + valueSetRow.context.valueSetDefinition) match {
                case Some(defResult) => {
                  defResult.definition.addEntry(createValueSetEntry(valueSetRow))
                }
                case None => {
                  val valueSet = createValueSet(valueSetRow, resourcesResults)
                  val version = createValueSetVersion(valueSetRow, valueSet, resourcesResults)
                  version.addEntry(createValueSetEntry(valueSetRow))
//                  valueSet.addVersion(version)
                  definitionResultMap +=
                    (valueSetRow.context.valueSetName + valueSetRow.context.valueSetDefinition ->
                      new DefinitionResult(valueSet, version))
                }
              }
              context = valueSetRow.context
            }
          })
        }
        definitionResultMap
      }).toMap
    } else {
      resourcesResults.status.errors = true
      resourcesResults.status.messages += "Error: %s sheet was not found, canceled load.".format(VALUE_SET_SHEET)
      Map.empty
    }
  }

  def createValueSet(vsr: ValueSetRow, resources: ResourcesResult) = {
    val res = resources.resourceTypes.get("VALUE_SET").get.find(_.name.equalsIgnoreCase(vsr.context.valueSetName)).get
    val vs = new ValueSet
    vs.setName(vsr.context.valueSetName)
    vs.setFormalName(vsr.context.valueSetName)
    vs.setHref(res href)
    vs.setUri(res uri)
    vs
  }

  def rowToValueSetRow(row: Row, context: EntryContext): Option[ValueSetRow] = {

    getCellValue(row.getCell(VALUE_SET_CONCEPT_CELL)).flatMap(code => {
      val newContext = new EntryContext(context.codeSystem, context.codeSystemVersion, context.valueSetName, context.valueSetDefinition)
      getCellValue(row.getCell(VALUE_SET_VALUE_SET_CELL)).foreach(newContext.valueSetName = _)
      getCellValue(row.getCell(VALUE_SET_VALUE_SET_DEFINITION_CELL)).foreach(newContext.valueSetDefinition = _)
      getCellValue(row.getCell(VALUE_SET_CODE_SYSTEM_CELL)).foreach(newContext.codeSystem = _)
      getCellValue(row.getCell(VALUE_SET_CODE_SYSTEM_VERSION_CELL)).foreach(newContext.codeSystemVersion = _)

      Option(new ValueSetRow(newContext,
        new Code(code, getCellValue(row.getCell(VALUE_SET_DESCRIPTION_CELL)))))
    })
  }

//  private def getCodeSystem(codeSystem: String): String = uriResolver.idToName(codeSystem, IdType.CODE_SYSTEM)

}

case class DefinitionResult(valueSet: ValueSet, definition: ValueSetVersion)