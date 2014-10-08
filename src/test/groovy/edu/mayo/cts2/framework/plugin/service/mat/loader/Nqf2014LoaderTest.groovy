package edu.mayo.cts2.framework.plugin.service.mat.loader

import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import org.junit.Ignore
import org.junit.Test
import org.springframework.transaction.annotation.Transactional

import javax.annotation.Resource

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

class Nqf2014LoaderTest extends AbstractTestBase {

	@Resource
	def Nqf2014Loader loader

	@Resource
	def ValueSetRepository repo

	@Resource
	def ValueSetVersionRepository vrepo

	def ssPath = "src/test/resources/exampleNqf2014/valuesets.xlsx"

	@Test
    @Ignore
	void TestSetUp() {
		assertNotNull loader
	}

	@Test
	@Transactional
    @Ignore
	void TestLoadValueSets() {
		def ss = new File(ssPath)
		def result = loader.loadSpreadSheet(ss)
		assertEquals 4, repo.count()
		assertEquals 3, repo.findOne("2.16.840.1.113883.3.464.1003.121.12.1006").currentVersion().entries.size()
		assertEquals 3, repo.findOne("2.16.840.1.113762.1.4.1").currentVersion().entries.size()
		assertEquals 16, repo.findOne("2.16.840.1.113883.3.464.1003.101.12.1048").currentVersion().entries.size()
		assertEquals 6, repo.findOne("2.16.840.1.114222.4.11.836").currentVersion().entries.size()
	}

}
