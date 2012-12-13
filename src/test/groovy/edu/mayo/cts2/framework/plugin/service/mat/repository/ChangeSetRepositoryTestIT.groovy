package edu.mayo.cts2.framework.plugin.service.mat.repository

import static org.junit.Assert.*

import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import org.junit.Test
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSet
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSetMember

class ChangeSetRepositoryTestIT extends AbstractTestBase {

	@Resource
	def ChangeSetRepository changeSetRepository

	@Test
	void TestSetUp() {
		assertNotNull changeSetRepository
	}

	@Test
	void TestInsertAndRetrieve() {
		def changeSet = createChangeSet()
		def returnedChangeSet = changeSetRepository.findOne(changeSet.getId())

		assertNotNull returnedChangeSet
		assertEquals changeSet.getId(), returnedChangeSet.getId()
		assertEquals changeSet.getCreator(), returnedChangeSet.getCreator()
		assertEquals 2, returnedChangeSet.getEntryCount()
	}

	private MatChangeSet createChangeSet() {
		def cs = new MatChangeSet(
				creator: UUID.randomUUID().toString(),
				memberList: createChangeSetMembers())
		changeSetRepository.save(cs)
		return cs
	}

	private List<MatChangeSetMember> createChangeSetMembers() {
		def members = new ArrayList<MatChangeSetMember>(2)
		members.add(new MatChangeSetMember(
				code: "code1",
				codeSystem: "codeSystem1",
				codeSystemVersion: "codeSystemVersion1",
				description: "description1"))
		members.add(new MatChangeSetMember(
				code: "code2",
				codeSystem: "codeSystem2",
				codeSystemVersion: "codeSystemVersion2",
				description: "description2"))
		return members
	}

}
