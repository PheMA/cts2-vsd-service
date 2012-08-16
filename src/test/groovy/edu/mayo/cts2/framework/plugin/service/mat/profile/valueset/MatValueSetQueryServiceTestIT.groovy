package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import static org.junit.Assert.*

import java.util.zip.ZipFile

import javax.annotation.Resource

import org.junit.Before
import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery

class MatValueSetQueryServiceTestIT extends AbstractTestBase {

	@Resource
	def MatValueSetQueryService service
	
	@Resource
	def MatZipLoader loader

	def marshaller = new DelegatingMarshaller()
	
	@Before
	void LoadContent() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/Pharyngitis_Artifacts.zip"))
		
		loader.loadMatZip(zip)
	}

	@Test
	void TestSetUp() {
		assertNotNull service
	}	
	
	@Test
	void TestQuerySize() {
		assertTrue service.getResourceSummaries(null as ValueSetQuery,null,null).entries.size() > 10
	}

}
