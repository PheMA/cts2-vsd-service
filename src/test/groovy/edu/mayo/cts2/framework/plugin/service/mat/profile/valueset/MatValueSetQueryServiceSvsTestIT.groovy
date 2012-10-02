package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.springframework.transaction.annotation.Transactional

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.command.ResolvedFilter
import edu.mayo.cts2.framework.model.core.PropertyReference
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.core.types.TargetReferenceType
import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase.SVS_OR_ZIP
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService


class MatValueSetQueryServiceSvsTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetQueryService service
	
	@Resource
	def MatZipLoader loader

	def marshaller = new DelegatingMarshaller()
	
	public MatValueSetQueryServiceSvsTestIT(){
		super()
		svsOrZip = SVS_OR_ZIP.SVS
	}
	
	@Test
	void TestSetUp() {
		assertNotNull service
	}	
	
	@Test
	@Transactional
	void TestQuerySize() {
		assertTrue service.getResourceSummaries(null as ValueSetQuery,null,null).entries.size() > 0
	}
	
	@Test
	void TestQueryContainsPropertyFilterEmeasure() {
		
		def ref = new PropertyReference()
		ref.referenceTarget = new URIAndEntityName(uri:"some uri", name:"emeasureid")
		ref.referenceType = TargetReferenceType.PROPERTY
		
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
					def filter = new ResolvedFilter(
						matchValue:"172",
						propertyReference: ref,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					[filter] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 1, summaries.entries.size()
	}
	
	@Test
	void TestQueryContainsPropertyFilterEmeasureWithDuplicateInGroup() {
		
		def ref = new PropertyReference()
		ref.referenceTarget = new URIAndEntityName(uri:"some uri", name:"emeasureid")
		ref.referenceType = TargetReferenceType.PROPERTY
		
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
					def filter = new ResolvedFilter(
						matchValue:"171",
						propertyReference: ref,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					[filter] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 1, summaries.entries.size()
	}
	
	@Test
	void TestQueryTwoFilters() {
		
		def ref1 = new PropertyReference()
		ref1.referenceTarget = new URIAndEntityName(uri:"some uri", name:"emeasureid")
		ref1.referenceType = TargetReferenceType.PROPERTY
		
		def ref2 = new PropertyReference()
		ref2.referenceTarget = new URIAndEntityName(uri:"some uri", name:"nqfnumber")
		ref2.referenceType = TargetReferenceType.PROPERTY
		
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
					def filter1 = new ResolvedFilter(
						matchValue:"172",
						propertyReference: ref1,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					def filter2 = new ResolvedFilter(
						matchValue:"0453",
						propertyReference: ref2,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					[filter1,filter2] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 1, summaries.entries.size()
	}
	
	@Test
	void TestQueryTwoFiltersOneInvalid() {
		
		def ref1 = new PropertyReference()
		ref1.referenceTarget = new URIAndEntityName(uri:"some uri", name:"emeasureid")
		ref1.referenceType = TargetReferenceType.PROPERTY
		
		def ref2 = new PropertyReference()
		ref2.referenceTarget = new URIAndEntityName(uri:"some uri", name:"nqfnumber")
		ref2.referenceType = TargetReferenceType.PROPERTY
		
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
					def filter1 = new ResolvedFilter(
						matchValue:"172",
						propertyReference: ref1,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					def filter2 = new ResolvedFilter(
						matchValue:"__INVALID__",
						propertyReference: ref2,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					[filter1,filter2] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 0, summaries.entries.size()
	}
	
	@Test
	@Transactional
	void TestQueryContainsPropertyFilter() {
		
		def ref = new PropertyReference()
		ref.referenceTarget = new URIAndEntityName(uri:"some uri", name:"nqfnumber")
		ref.referenceType = TargetReferenceType.PROPERTY
		
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
			
					def filter = new ResolvedFilter(
						matchValue:"0453",
						propertyReference: ref,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					[filter] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 1, summaries.entries.size()
	}
	
	@Test
	@Transactional
	void TestQueryContainsPropertyFilterInvalid() {
		
		def ref = new PropertyReference()
		ref.referenceTarget = new URIAndEntityName(uri:"uri", name:"nqfnumber")
		ref.referenceType = TargetReferenceType.PROPERTY
		
		def summaries = service.getResourceSummaries(
			{
				getFilterComponents : {
					def filter = new ResolvedFilter(
						matchValue:"__INVALID__",
						propertyReference: ref,
						matchAlgorithmReference: StandardMatchAlgorithmReference.EXACT_MATCH.matchAlgorithmReference
					)
					[filter] as Set
				}
			} as ValueSetQuery,null,null)
		
		assertEquals 0, summaries.entries.size()
	}
}
