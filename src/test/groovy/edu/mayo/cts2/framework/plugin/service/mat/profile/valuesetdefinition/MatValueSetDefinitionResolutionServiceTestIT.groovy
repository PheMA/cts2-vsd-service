package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import javax.annotation.Resource

import org.junit.Test
import static org.junit.Assert.*
import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class MatValueSetDefinitionResolutionServiceTestIT extends AbstractTestBase {

	@Resource
	def MatValueSetDefinitionResolutionService service

	def marshaller = new DelegatingMarshaller()

	@Test
	void TestSetUp() {
		assertNotNull service
	}

}