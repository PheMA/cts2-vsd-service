package edu.mayo.cts2.framework.plugin.service.mat.svs

import static org.junit.Assert.*
import ihe.iti.svs._2008.RetrieveMultipleValueSetsResponseType

import javax.annotation.Resource
import javax.xml.bind.JAXBContext

import org.junit.Test

import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class SvsTransformTestIT extends AbstractTestBase {

	@Resource
	def SvsTransform transform

	@Test
	void TestSetUp() {
		assertNotNull transform
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
