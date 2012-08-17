package edu.mayo.cts2.framework.plugin.service.mat.profile.entity

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId


class UtsEntityReadServiceTestIT extends AbstractTestBase {

	@Resource
	def UtsEntityReadService service

	def marshaller = new DelegatingMarshaller()

	@Test
	void TestSetUp() {
		assertNotNull service
	}
	
	@Test
	void TestRead() {
		def id = new EntityDescriptionReadId("185465003", "SNOMEDCT", ModelUtils.nameOrUriFromName("SNOMEDCT"))
		
		def result = service.read(id, null)
		
		assertNotNull result
	}
	
	@Test
	void TestReadHasDescriptions() {
		def id = new EntityDescriptionReadId("185465003", "SNOMEDCT", ModelUtils.nameOrUriFromName("SNOMEDCT"))
		
		def result = service.read(id, null).namedEntity
		
		assertTrue result.designationCount > 0
	}

}
