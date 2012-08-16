package edu.mayo.cts2.framework.plugin.service.mat.loader

import static org.junit.Assert.*

import java.util.zip.ZipFile

import javax.annotation.Resource

import org.junit.Test

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
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/Pharyngitis_Artifacts.zip"))
		
		loader.loadMatZip(zip)
		
		assertTrue repo.count() > 10
	}
	
}
