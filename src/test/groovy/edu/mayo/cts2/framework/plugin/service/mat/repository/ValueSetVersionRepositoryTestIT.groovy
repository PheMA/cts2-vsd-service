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
		def valueSet = new ValueSet(name:"1.23.45")
		valueSet.addVersion(new ValueSetVersion(),true)
		
		valueSet.currentVersion.addEntry(new ValueSetEntry(code:"123"))
		valueSetRepos.save(valueSet)
		
		assertEquals 1, repos.count()
	}
	
	@Test
	void TestInsertWithValueSetEntryLarge() {
		def valueSet = new ValueSet(name:"1.23.45")
		valueSet.addVersion(new ValueSetVersion(),true)
		
		for(i in 0..9999){
			def e = new ValueSetEntry(code:"123")
			e.valueSetVersion = valueSet.currentVersion()
			valueSet.currentVersion().addEntry(e)
		}
		
		def txTemplate = new TransactionTemplate(txManager)
		
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				valueSetRepos.save(valueSet)
			}
		})
		
		txTemplate.execute(new TransactionCallbackWithoutResult() {
			public void doInTransactionWithoutResult(TransactionStatus status) {
				def v = valueSetRepos.findOne("1.23.45").currentVersion()
				
			
				assertNotNull repos.findValueSetEntriesByChangeSetUri(v.changeSetUri, new PageRequest(0,100))

			}
		})
	}
	
	@Test
	@Transactional
	void TestInsertWithTwoValueSetEntry() {
		def valueSet1 = new ValueSet(name:"1.23.45")
		valueSet1.addVersion(new ValueSetVersion(),true)
		valueSet1.currentVersion().changeSetUri = UUID.randomUUID().toString()

		valueSet1.currentVersion().addEntry(new ValueSetEntry(code:"123"))
		valueSetRepos.save(valueSet1)
			
		def valueSet2 = valueSetRepos.findOne("1.23.45")
		valueSet2.currentVersion().addEntry(new ValueSetEntry(code:"456"))
		valueSetRepos.save(valueSet2)
			
		def id = valueSetRepos.findOne("1.23.45").currentVersion().changeSetUri
		def entries = repos.findValueSetEntriesByChangeSetUri(id, new PageRequest(0, 100)).content
		
		assertEquals 2, entries.size()
	}
	
	@Test
	@Transactional
	void TestGetDistinctCodeSystemVersionsWithNumberVersion() {
		def valueSet = new ValueSet(name:"1.23.45")
		
		def version = new ValueSetVersion()
		version.setVersion(UUID.randomUUID().toString())
		valueSet.addVersion(version,true)
		def entry = new ValueSetEntry(code:"123")
		entry.setCodeSystem("testcs")
		entry.setCodeSystemVersion("2011")
		valueSet.currentVersion().addEntry(entry)

		valueSetRepos.save(valueSet)
		
		def val = repos.findCodeSystemVersionsByValueSetVersion(version.version)
		
		assertEquals 1, val.size()
		assertEquals "testcs", val[0][0]
		assertEquals "2011", val[0][1]
	}
	
	@Test
	@Transactional
	void TestGetDistinctCodeSystemVersions() {
		def valueSet = new ValueSet(name:"1.23.45")
		
		def version = new ValueSetVersion()
		version.setVersion(UUID.randomUUID().toString())
		valueSet.addVersion(version,true)
		
		def entry = new ValueSetEntry(code:"123")
		entry.setCodeSystem("testcs")
		entry.setCodeSystemVersion("1.0")
		valueSet.currentVersion().addEntry(entry)
		
		entry = new ValueSetEntry(code:"456")
		entry.setCodeSystem("something")
		entry.setCodeSystemVersion("2.0")
		valueSet.currentVersion().addEntry(entry)
		
		valueSetRepos.save(valueSet)
		
		def val = repos.findCodeSystemVersionsByValueSetVersion(version.version)
		
		assertEquals 2, val.size()
	}

	@Test
	@Transactional
	void TestFindByValueSetNameAndCreator() {
		def oid = UUID.randomUUID().toString()
		def creator = UUID.randomUUID().toString()

		def valueSet = new ValueSet(name: oid)
		def version1 = new ValueSetVersion()
		version1.version = UUID.randomUUID().toString()
		version1.creator = creator
		valueSet.addVersion(version1,true)
		def version2 = new ValueSetVersion()
		version2.version = UUID.randomUUID().toString()
		version2.creator = creator
		valueSet.addVersion(version2,true)
		valueSetRepos.save(valueSet)

		def oid2 = UUID.randomUUID().toString()
		def valueSet2 = new ValueSet(name: oid2)
		def version3 = new ValueSetVersion()
		version3.version = UUID.randomUUID().toString()
		version3.creator = creator
		valueSet2.addVersion(version3,true)
		valueSetRepos.save(valueSet2)

		def val = repos.findByValueSetNameAndCreator(oid, creator, new PageRequest(0, 10))
		assertNotNull val
		assertEquals 2, val.numberOfElements
	}

	@Test
	@Transactional
	void TestFindByCreator() {
		def oid = UUID.randomUUID().toString()
		def creator = UUID.randomUUID().toString()

		def valueSet = new ValueSet(name: oid)
		def version1 = new ValueSetVersion()
		version1.version = UUID.randomUUID().toString()
		version1.creator = creator
		valueSet.addVersion(version1,true)
		def version2 = new ValueSetVersion()
		version2.version = UUID.randomUUID().toString()
		version2.creator = creator
		valueSet.addVersion(version2,true)
		valueSetRepos.save(valueSet)

		def valueSet2 = new ValueSet(name: oid)
		def version3 = new ValueSetVersion()
		version3.version = UUID.randomUUID().toString()
		version3.creator = creator
		valueSet2.addVersion(version3,true)
		valueSetRepos.save(valueSet2)

		def val = repos.findByCreator(creator, new PageRequest(0, 10))
		assertNotNull val
		assertEquals 3, val.numberOfElements
	}

}


