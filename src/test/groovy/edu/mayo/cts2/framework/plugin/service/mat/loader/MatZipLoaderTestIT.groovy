package edu.mayo.cts2.framework.plugin.service.mat.loader

import static org.junit.Assert.*

import java.util.zip.ZipFile

import javax.annotation.Resource

import org.junit.Test
import org.springframework.transaction.annotation.Transactional

import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class MatZipLoaderTestIT extends AbstractTestBase {

	@Resource
	def MatZipLoader loader
	
	@Resource
	def ValueSetRepository repo

	@Test
	void TestSetUp() {
		assertNotNull loader
	}
	
	@Test
	void TestLoad() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/test.zip"))
		
		loader.loadMatZip(zip)
		
		assertTrue repo.count() > 10
	}
	
	@Test
	void TestLoadCombined() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/combined.zip"))
		
		loader.loadCombinedMatZip(zip)
		
		assertTrue repo.count() > 10
	}
	
	@Test
	@Transactional
	void TestLoadNQF_0002() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/NQF_0002_HHS_Updated_Dec_2011.zip"))
		
		loader.loadMatZip(zip)
		
		def valueSet = repo.findOne("2.16.840.1.113883.3.464.0001.372")
		
		assertNotNull valueSet
		
		valueSet.entries.each {
			println it.code
			assertTrue it.codeSystem + " - " + it.code, it.code.length() > 1
		}

	}

    @Test
    @Transactional
    void TestRowToValueSetEntry(){
        def zip = new ZipFile(new File("src/test/resources/exampleMatZips/NQF_0002_HHS_Updated_Dec_2011.zip"))
        loader.loadMatZip(zip)

        def valueSet = repo.findOne("2.16.840.1.113883.3.464.0001.45")
        assertNotNull valueSet

        for (it in valueSet.entries()) {
            println it.description
            assertTrue it.codeSystem + " - " + it.description, it.description.length() > 1
        }
    }
}
