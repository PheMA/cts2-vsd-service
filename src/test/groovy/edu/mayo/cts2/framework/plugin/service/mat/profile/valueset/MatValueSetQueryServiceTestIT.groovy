package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import javax.annotation.Resource

import org.junit.Test
import static org.junit.Assert.*
import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class MatValueSetQueryServiceTestIT extends AbstractTestBase {

	@Resource
	def MatValueSetQueryService service

	def marshaller = new DelegatingMarshaller()


	@Test
	void TestSetUp() {
		assertNotNull service
	}

}
