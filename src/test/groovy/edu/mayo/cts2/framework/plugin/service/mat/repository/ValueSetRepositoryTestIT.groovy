package edu.mayo.cts2.framework.plugin.service.mat.repository

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional

import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
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
		def valueSet = new ValueSet(name:"1.23.45")
		repos.save(valueSet)
		
		assertNotNull repos.findOne("1.23.45")
	}
	
	@Test
	@Transactional
	void TestInsertAndRetrieveWithVersion() {
		def valueSetVersion = new ValueSetVersion()
		
		def valueSet = new ValueSet("1.23.45")

		def vsver = new ValueSetVersion()
		
		valueSet.addVersion(vsver,false)

		repos.save(valueSet)
		
		def result = repos.findOne("1.23.45")
		
		assertNotNull result
		assertEquals 1, result.versions().size()
		
		print result.versions().get(0).documentUri
		assertNotNull result.versions().get(0).documentUri
	}

//	@Test
//	void TestFindByNameLike() {
//		def valueSet = new ValueSet(name:"1.23.45")
//		repos.save(valueSet)
//
//		def page = repos.findByNameLikeIgnoreCase("%3.45%", new PageRequest(0,100))
//		assertNotNull page
//		assertEquals 1, page.getContent().size()
//	}
//
//	@Test
//	void TestFindByNameLikeCaseInsensitive() {
//		def valueSet = new ValueSet(name:"1.23.45")
//		repos.save(valueSet)
//
//		assertNotNull repos.findByNameLikeIgnoreCase("%estna%",new PageRequest(0,100))
//	}
//
//	@Test
//	void TestInsertAndRetrieveByName() {
//		def valueSet = new ValueSet(name:"1.23.45")
//		repos.save(valueSet)
//
//		assertNotNull repos.findOneByName("testName")
//	}
	
//	@Test
//	void TestInsertAndRetrieveByWrongName() {
//		def valueSet = new ValueSet(name:"1.23.45")
//		repos.save(valueSet)
//
//		assertNull repos.findOneByName("__INVALID__")
//	}
	
	@Test
	void TestInsertAndRetrieveWrongId() {
		def valueSet = new ValueSet(name:"1.23.45")
		repos.save(valueSet)
		
		assertNull repos.findOne("__INVALID__")
	}
	
}
