package edu.mayo.cts2.framework.plugin.service.mat.repository

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test

import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class ValueSetRepositoryTestIT extends AbstractTestBase {

	@Resource
	def ValueSetRepository repos

	@Test
	void TestSetUp() {
		assertNotNull repos
	}
	
	@Test
	void TestInsertAndRetrieve() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		repos.save(valueSet)
		
		assertNotNull repos.findOne("1.23.45")
	}
	
	@Test
	void TestInsertAndRetrieveWrongId() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		repos.save(valueSet)
		
		assertNull repos.findOne("__INVALID__")
	}
}
