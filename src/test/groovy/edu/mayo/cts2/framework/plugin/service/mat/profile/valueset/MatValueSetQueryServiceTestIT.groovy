package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.meta.StandardModelAttributeReference
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
	void TestMaxToReturn() {
		def summaries = service.getResourceSummaries(null as ValueSetQuery,null,new Page(maxToReturn:5,page:0))
		
		assertEquals 5, summaries.entries.size()
	}
	
	@Test
	void TestIsPartialFalse() {
		def summaries = service.getResourceSummaries(null as ValueSetQuery,null,new Page(maxToReturn:5,page:0))
		
		assertTrue summaries.entries.size() < 50
		
		assertFalse summaries.atEnd
	}
	
	@Test
	void TestQueryContainsFilter() {
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
					def filter = new ResolvedFilter(
						matchValue:"02.99",
						propertyReference: StandardModelAttributeReference.RESOURCE_NAME.propertyReference,
						matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference
					)
					[filter] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 1, summaries.entries.size()
	}
	
	@Test
	void TestQueryContainsFilterSynopsis() {
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
					def filter = new ResolvedFilter(
						matchValue:"office",
						propertyReference: StandardModelAttributeReference.RESOURCE_SYNOPSIS.propertyReference,
						matchAlgorithmReference: StandardMatchAlgorithmReference.CONTAINS.matchAlgorithmReference
					)
					[filter] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 1, summaries.entries.size()
	}
	
	@Test
	void TestIsPartialTrue() {
		def summaries = service.getResourceSummaries(null as ValueSetQuery,null,new Page(maxToReturn:50,page:0))
		
		assertTrue summaries.entries.size() < 50
		
		assertTrue summaries.atEnd
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
