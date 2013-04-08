package edu.mayo.cts2.framework.plugin.service.mat.loader

import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import org.junit.Test
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class Cts2SpreadSheetLoaderTest extends AbstractTestBase {

	@Resource
	def Cts2SpreadSheetLoader loader

	@Resource
	def ValueSetRepository repo

	@Resource
	def ValueSetVersionRepository vrepo

	def ssPath = "src/test/resources/exampleCts2SpreadSheet/CTS2_Spreadsheet.xlsx"

	@Test
	void TestSetUp() {
		assertNotNull loader
	}

	@Test
	@Transactional
	void TestLoadValueSets() {
		def ss = new File(ssPath)

		def result = loader.loadSpreadSheet(ss)

		assertEquals 1, repo.count()
		def valueSet = repo.findOne("SNOMEDCT_Allergy_Branch")
		assertNotNull valueSet
		assertEquals 1, valueSet.versions().size()
		assertEquals "SNOMEDCT_Allergy_Branch_20130131", valueSet.currentVersion().version
		assertEquals 1646, valueSet.currentVersion().entries.size()
	}

}
