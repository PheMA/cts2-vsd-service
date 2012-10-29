package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.apache.commons.lang.StringUtils
import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core.VersionTagReference
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase.SVS_OR_ZIP
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionReadService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.ValueSetDefinitionResolutionService
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId

class MatValueSetDefinitionResolutionServiceSvsTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetDefinitionResolutionService service

	@Resource
	def ValueSetDefinitionReadService read
	
	def marshaller = new DelegatingMarshaller()
	
	public MatValueSetDefinitionResolutionServiceSvsTestIT(){
		super()
		svsOrZip = SVS_OR_ZIP.SVS
	}

	@Test
	void TestSetUp() {
		assertNotNull service
	}
	
	@Test
	void TestQuerySize() {
		def id = new ValueSetDefinitionReadId("20120917", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.resolveDefinition(id, null, null, null, null, null, new Page())
		
		assertNotNull result
		assertTrue result.entries.size() > 0
	}
	
	@Test
	void TestResolveByTag() {
		def ref = new VersionTagReference("CURRENT")
		def bytag = read.readByTag(ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"), ref, null)
		
		assertNotNull bytag
		
		def id = new ValueSetDefinitionReadId(bytag.localID, ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.resolveDefinition(id, null, null, null, null, null, new Page())
		
		assertNotNull result
		assertTrue result.entries.size() > 0		
	}
	
	@Test
	void TestResolveSNOMEDCTHrefs() {
		def id = new ValueSetDefinitionReadId("20120917", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.resolveDefinition(id, null, null, null, null, null, new Page())
		
		assertNotNull result
		assertTrue result.entries.size() > 0
		
		result.entries.each {
			assertTrue StringUtils.isNotBlank(it.uri)
		}
	}

	@Test
	void TestEntriesHaveUri() {
		def id = new ValueSetDefinitionReadId("20120917", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.resolveDefinition(id, null, null, null, null, null, new Page())
		
		assertNotNull result
		assertTrue result.entries.size() > 0
		
		result.entries.each {
			assertTrue ! it.uri.startsWith("null")
		}
	}
	
	@Test
	void TestQueryGrouping() {
		def id = new ValueSetDefinitionReadId("20120917", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def result = service.resolveDefinition(id, null, null, null, null, null, new Page())
		
		assertNotNull result
		println result.entries.size()
		assertTrue result.entries.size() > 0
	}
	
	@Test
	void TestValidXml() {
		def id = new ValueSetDefinitionReadId("20120917", ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"))
		
		def entries = service.resolveDefinition(id, null, null, null, null, null, new Page()).entries
		
		assertTrue entries.size() > 0
		
		entries.each {
			marshaller.marshal(it, new StreamResult(new StringWriter()))
		}
	}

}