package edu.mayo.cts2.framework.plugin.service.mat.loader

import static org.junit.Assert.*
import ihe.iti.svs._2008.RetrieveMultipleValueSetsResponseType

import javax.annotation.Resource
import javax.xml.bind.JAXBContext

import org.junit.Test

import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class SLoaderTestIT extends AbstractTestBase {

	@Resource
	def SvsLoader loader

	@Resource
	def ValueSetRepository repo

	@Test
	void TestSetUp() {
		assertNotNull loader
	}

	@Test
	void TestUnmarshallSVS() {
		def is = new FileInputStream("src/test/resources/exampleSVS/RetrieveMultipleValueSetResponse.xml")
		def jc = JAXBContext.newInstance(RetrieveMultipleValueSetsResponseType)
		def u = jc.createUnmarshaller();
		def o = u.unmarshal( is );

		assertNotNull o
	}
}
