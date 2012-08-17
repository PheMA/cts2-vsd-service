package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService

class MatValueSetQueryServiceTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetQueryService service
	
	@Resource
	def MatZipLoader loader

	def marshaller = new DelegatingMarshaller()
	
	@Test
	void TestSetUp() {
		assertNotNull service
	}	
	
	@Test
	void TestQuerySize() {
		assertTrue service.getResourceSummaries(null as ValueSetQuery,null,null).entries.size() > 10
	}
	
	@Test
	void TestValidXml() {
		def entries = service.getResourceSummaries(null as ValueSetQuery,null,null).entries
		
		assertTrue entries.size() > 0
		
		entries.each {
			marshaller.marshal(it, new StreamResult(new StringWriter()))
		}
	}

}
