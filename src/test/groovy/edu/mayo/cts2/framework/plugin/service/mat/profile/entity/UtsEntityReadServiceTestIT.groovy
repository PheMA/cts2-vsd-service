package edu.mayo.cts2.framework.plugin.service.mat.profile.entity

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase


class UtsEntityReadServiceTestIT extends AbstractTestBase {

	@Resource
	def UtsEntityReadService service

	def marshaller = new DelegatingMarshaller()

	@Test
	void TestSetUp() {
		assertNotNull service
	}

}
