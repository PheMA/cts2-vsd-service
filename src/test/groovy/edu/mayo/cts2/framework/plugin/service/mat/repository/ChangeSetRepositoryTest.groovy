package edu.mayo.cts2.framework.plugin.service.mat.repository

import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetChange

import static org.junit.Assert.*

import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import org.junit.Test
import javax.annotation.Resource

class ChangeSetRepositoryTest extends AbstractTestBase {

	@Resource
	def ChangeSetRepository changeSetRepository

	@Resource
	def ValueSetRepository valueSetRepository

	@Test
	void TestSetUp() {
		assertNotNull changeSetRepository
		assertNotNull valueSetRepository
	}

	@Test
	void TestInsertAndRetrieve() {
		def changeSet = createChangeSet()
		def returnedChangeSet = changeSetRepository.findOne(changeSet.getChangeSetUri())

		assertNotNull returnedChangeSet
		assertEquals changeSet.getChangeSetUri(), returnedChangeSet.getChangeSetUri()
		assertEquals changeSet.getCreator(), returnedChangeSet.getCreator()
	}

	private ValueSetChange createChangeSet() {
		def cs = new ValueSetChange(
				creator: UUID.randomUUID().toString())
		changeSetRepository.save(cs)
		return cs
	}

	@Test
	void TestFindByCreator() {
		def changeSet = createChangeSet()
		def creator = changeSet.getCreator()

		def returnedChangeSet = changeSetRepository.findChangeSetsByCreator(creator, null).getContent().get(0)
		assertNotNull returnedChangeSet
		assertEquals changeSet.getChangeSetUri(), returnedChangeSet.getChangeSetUri()
		assertEquals creator, returnedChangeSet.getCreator()
	}

}
