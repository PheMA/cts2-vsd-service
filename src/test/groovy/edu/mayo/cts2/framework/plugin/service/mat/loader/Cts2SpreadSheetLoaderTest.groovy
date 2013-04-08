package edu.mayo.cts2.framework.plugin.service.mat.loader

import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import org.junit.Ignore
import org.junit.Test
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class Cts2SpreadSheetLoaderTest extends AbstractTestBase {

	@Resource
	def Cts2SpreadSheetLoader loader

	def ssPath = "src/test/resources/exampleCts2SpreadSheet/CTS2_Spreadsheet.xlsx"

	@Test
	void TestSetUp() {
		assertNotNull loader
	}

	@Test
	@Ignore
	@Transactional
	void TestLoadresources() {
		def ss = new File(ssPath)

		def result = loader.loadSpreadSheet(ss)
		assertTrue result.resourceTypes().contains("REFERENCETYPE")
		def resultRefType = result.resourceTypes().get("REFERENCETYPE").get()
		assertEquals 35, resultRefType.length()

		assertTrue result.resourceTypes().contains("CODE_SYSTEM")
		def resultCodeSys = result.resourceTypes().get("CODE_SYSTEM").get()
		assertEquals 1, resultCodeSys.length()
		def type = resultCodeSys.head()
		assertEquals "SNOMEDCT", type.name
		assertEquals "http://snomed.info/sct/900000000000207008", type.uri
		assertEquals "SNOMED CT International Edition", type.description
		assertEquals "http://snomed.info/id/", type.uriPrefix

		assertTrue result.resourceTypes().contains("CODE_SYSTEM_VERSION")
		def resultCodeSysVer = result.resourceTypes().get("CODE_SYSTEM_VERSION").get()
		assertEquals 1, resultCodeSysVer.length()
		type = resultCodeSysVer.head()
		assertEquals "SNOMEDCT_20130131", type.name
		assertEquals "http://snomed.info/sct/900000000000207008/version/20120131", type.uri
		assertEquals "SNOMED CT International Edition January 2013 Release", type.description

		assertTrue result.resourceTypes().contains("VALUE_SET_DEFINITION")
		def resultValueSetDef = result.resourceTypes().get("VALUE_SET_DEFINITION").get()
		assertEquals 1, resultValueSetDef.length()
		type = resultValueSetDef.head()
		assertEquals "SNOMEDCT_Allergy_Branch_20130131", type.name
		assertEquals "http://snomed.info/id/420134006/version/20130131", type.uri
		assertEquals "Descendants of Propensity to adverse reactions 2013 Release", type.description

		assertTrue result.resourceTypes().contains("VALUE_SET")
		def resultValueSet = result.resourceTypes().get("VALUE_SET").get()
		assertEquals 1, resultValueSet.length()
		type = resultValueSet.head()
		assertEquals "SNOMEDCT_Allergy_Branch", type.name
		assertEquals "http://snomed.info/id/420134006", type.uri
		assertEquals "Descendants of Propensity to adverse reactions", type.description

	}

	@Test
	@Transactional
	void TestLoadValueSets() {
		def ss = new File(ssPath)

		def result = loader.loadSpreadSheet(ss)
		println result
	}

}
