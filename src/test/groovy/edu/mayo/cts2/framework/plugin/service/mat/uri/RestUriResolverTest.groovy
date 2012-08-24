package edu.mayo.cts2.framework.plugin.service.mat.uri

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

class RestUriResolverTest {
	
	def resolver

	@Before
	void SetUp(){
		resolver = new RestUriResolver()
		resolver.setUriResolutionServiceUrl("http://bmidev4:8000/")
	}

	@Test
	void TestIdToName() {
		assertEquals "LNC", resolver.idToName("http://id.nlm.nih.gov/cui/C1136323", IdType.CODE_SYSTEM())
	}
	
	@Test
	void TestIdToUri() {
		assertEquals "http://id.nlm.nih.gov/cui/C1136323", resolver.idToUri("LNC", IdType.CODE_SYSTEM())
	}
	
	@Test
	void TestIdToBaseUri() {
		assertEquals "http://id.nlm.nih.gov/cui/C1136323/", resolver.idToBaseUri("LNC")
	}
	
	@Test
	void TestIdAndVersionToUri() {
		assertEquals "http://id.nlm.nih.gov/cui/C3260726", resolver.idAndVersionToVersionUri("LNC", "238", IdType.CODE_SYSTEM())
	}
	
	@Test
	void TestIdAndVersionToName() {
		assertEquals "LNC238", resolver.idAndVersionToVersionName("LNC", "238", IdType.CODE_SYSTEM())
	}
}
