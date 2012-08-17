package edu.mayo.cts2.framework.plugin.service.mat.repository

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.springframework.transaction.annotation.Transactional

import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
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
	void TestInsertAndRetrieveByName() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		repos.save(valueSet)
		
		assertNotNull repos.findOneByName("testName")
	}
	
	@Test
	void TestInsertAndRetrieveByWrongName() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		repos.save(valueSet)
		
		assertNull repos.findOneByName("__INVALID__")
	}
	
	@Test
	void TestInsertAndRetrieveWrongId() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		repos.save(valueSet)
		
		assertNull repos.findOne("__INVALID__")
	}
	
	@Test
	@Transactional
	void TestInsertWithValueSetEntry() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		valueSet.entries.add(new ValueSetEntry(code:"123"))
		repos.save(valueSet)
		
		assertEquals 1, repos.findOne("1.23.45").entries.size()
	}
	
	@Test
	@Transactional
	void TestInsertWithTwoValueSetEntry() {
		def valueSet1 = new ValueSet(oid:"1.23.45", name:"testName")
		valueSet1.entries.add(new ValueSetEntry(code:"123"))
		repos.save(valueSet1)
		
		def valueSet2 = repos.findOne("1.23.45")
		valueSet2.entries.add(new ValueSetEntry(code:"456"))
		repos.save(valueSet2)
		
		assertEquals 2, repos.findOne("1.23.45").entries.size()
	}
}
