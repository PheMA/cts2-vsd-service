package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService

class MatValueSetReadServiceTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetReadService service
	
	def marshaller = new DelegatingMarshaller()
	
	@Test
	void TestSetUp() {
		assertNotNull service
	}	
	
	@Test
	void TestRead() {
		assertNotNull service.read(ModelUtils.nameOrUriFromName("AMA-PCFPI"), null)
	}
	
	@Test
	void TestReadCorrectName() {
		def vs = service.read(ModelUtils.nameOrUriFromName("AMA-PCFPI"), null)
		
		assertEquals "AMA-PCFPI", vs.getValueSetName()
	}
	
	@Test
	void TestReadCorrectAbout() {
		def vs = service.read(ModelUtils.nameOrUriFromName("AMA-PCFPI"), null)
		
		fail("not sure what the about should be")
		assertEquals "???", vs.getAbout()
	}
	
}
