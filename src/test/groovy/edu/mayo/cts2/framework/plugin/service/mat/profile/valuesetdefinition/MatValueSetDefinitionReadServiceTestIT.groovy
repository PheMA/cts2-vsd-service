package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId

class MatValueSetDefinitionReadServiceTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetDefinitionReadService service

	def marshaller = new DelegatingMarshaller()

	@Test
	void TestSetUp() {
		assertNotNull service
	}
	
	@Test
	void TestRead() {
		def id = new ValueSetDefinitionReadId("1", ModelUtils.nameOrUriFromName("2.16.840.1.113883.1.11.1"))
		
		def result = service.read(id, null)
		
		assertNotNull result
	}
	
	@Test
	void TestHasSourceAndNotation() {
		def id = new ValueSetDefinitionReadId("1", ModelUtils.nameOrUriFromName("2.16.840.1.113883.1.11.1"))
		
		def result = service.read(id, null).getResource()
		
		assertNotNull result.sourceAndNotation
	}
	
	@Test
	void TestHasEntries() {
		def id = new ValueSetDefinitionReadId("1", ModelUtils.nameOrUriFromName("2.16.840.1.113883.1.11.1"))
		
		def result = service.read(id, null).getResource()
		
		assertTrue result.entryCount > 0
	}
	
	@Test
	void TestReadValidXml() {
		def id = new ValueSetDefinitionReadId("1", ModelUtils.nameOrUriFromName("2.16.840.1.113883.1.11.1"))
		
		def result = service.read(id, null)
		
		assertNotNull result.getResource()
		
		marshaller.marshal(result.getResource(), new StreamResult(new StringWriter()))
	}

}