package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.core.VersionTagReference
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase.SVS_OR_ZIP
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId

class MatValueSetDefinitionReadServiceSvsTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetDefinitionReadService service

	def marshaller = new DelegatingMarshaller()

	public MatValueSetDefinitionReadServiceSvsTestIT(){
		super()
		svsOrZip = SVS_OR_ZIP.SVS
	}
	
	@Test
	void TestSetUp() {
		assertNotNull service
	}
	
	@Test
	void TestRead() {
		def id = new ValueSetDefinitionReadId("20120917", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.read(id, null)
		
		assertNotNull result
	}	
	
	@Test
	void TestReadByTag() {
		def ref = new VersionTagReference("CURRENT")
		def result = service.readByTag(ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"), ref, null)
		
		assertNotNull result
		print result.localID
	}
	
	@Test
	void TestReadBadVersion() {
		def id = new ValueSetDefinitionReadId("__INVALID__", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.read(id, null)
		
		assertNull result
	}
	
	
	@Test
	void TestReadValidXml() {
		def id = new ValueSetDefinitionReadId("20120917", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.read(id, null)
		
		assertNotNull result.getResource()
		
		marshaller.marshal(result.getResource(), new StreamResult(new StringWriter()))
	}

}