package edu.mayo.cts2.framework.plugin.service.mat.repository

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate

import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class ValueSetVersionRepositoryTestIT extends AbstractTestBase {

	@Resource 
	def PlatformTransactionManager txManager
	
	@Resource
	def ValueSetRepository valueSetRepos

	@Resource
	def ValueSetVersionRepository repos

	@Test
	void TestSetUp() {
		assertNotNull repos
	}
	
	@Test
	@Transactional
	void TestInsertWithValueSetEntry() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		valueSet.addVersion(new ValueSetVersion(),true)
		
		valueSet.currentVersion.addEntry(new ValueSetEntry(code:"123"))
		valueSetRepos.save(valueSet)
		
		assertEquals 1, repos.count()
	}
	
	@Test
	void TestInsertWithValueSetEntryLarge() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		valueSet.addVersion(new ValueSetVersion(),true)
		
		for(i in 0..9999){
			def e = new ValueSetEntry(code:"123")
			e.valueSetVersion = valueSet.currentVersion
			valueSet.currentVersion.addEntry(e)		
		}
		
		def txTemplate = new TransactionTemplate(txManager)
		
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				valueSetRepos.save(valueSet)
			}
		})
		
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				def v = valueSetRepos.findOneByName("testName").currentVersion
				
			
				assertNotNull repos.findValueSetEntriesByValueSetVersionId(v.id, new PageRequest(0,100))

			}
		})
	}
	
	@Test
	@Transactional
	void TestInsertWithTwoValueSetEntry() {
		def valueSet1 = new ValueSet(oid:"1.23.45", name:"testName")
		valueSet1.addVersion(new ValueSetVersion(),true)
		
		valueSet1.currentVersion.addEntry(new ValueSetEntry(code:"123"))
		valueSetRepos.save(valueSet1)
			
		def valueSet2 = valueSetRepos.findOne("1.23.45")
		valueSet2.currentVersion.addEntry(new ValueSetEntry(code:"456"))
		valueSetRepos.save(valueSet2)
			
		def id = valueSetRepos.findOne("1.23.45").currentVersion.id
		def entries = repos.findValueSetEntriesByValueSetVersionId(id, new PageRequest(0, 100)).content
		
		assertEquals 2, entries.size()
	}
	
	@Test
	@Transactional
	void TestGetDistinctCodeSystemVersionsWithNumberVersion() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		
		def version = new ValueSetVersion()
		valueSet.addVersion(version,true)
		def entry = new ValueSetEntry(code:"123")
		entry.setCodeSystem("testcs")
		entry.setCodeSystemVersion("2011")
		valueSet.currentVersion.addEntry(entry)

		valueSetRepos.save(valueSet)
		
		def val = repos.findCodeSystemVersionsByValueSetVersion(version.id)
		
		assertEquals 1, val.size()
		assertEquals "testcs", val[0][0]
		assertEquals "2011", val[0][1]
	}
	
	@Test
	@Transactional
	void TestGetDistinctCodeSystemVersions() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		
		def version = new ValueSetVersion()
		valueSet.addVersion(version,true)
		
		def entry = new ValueSetEntry(code:"123")
		entry.setCodeSystem("testcs")
		entry.setCodeSystemVersion("1.0")
		valueSet.currentVersion.addEntry(entry)
		
		entry = new ValueSetEntry(code:"456")
		entry.setCodeSystem("something")
		entry.setCodeSystemVersion("2.0")
		valueSet.currentVersion.addEntry(entry)
		
		valueSetRepos.save(valueSet)
		
		def val = repos.findCodeSystemVersionsByValueSetVersion(version.id)
		
		assertEquals 2, val.size()
	}
	
	@Test
	@Transactional
	void TestValueSetVersionByVersionId() {
		def valueSet = new ValueSet(oid:"1.23.45", name:"testName")
		def version = new ValueSetVersion()
		version.versionId = "999"
		valueSet.addVersion(version,true)
		
		valueSetRepos.save(valueSet)
		
		def val = repos.findVersionByIdOrVersionIdAndValueSetName("testName", "999")
		
		assertNotNull val
	}
}


