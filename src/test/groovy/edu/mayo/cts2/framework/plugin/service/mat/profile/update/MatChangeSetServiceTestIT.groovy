package edu.mayo.cts2.framework.plugin.service.mat.profile.update

import static org.junit.Assert.*

import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import javax.annotation.Resource
import org.junit.Test
import edu.mayo.cts2.framework.model.core.SourceReference

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller

class MatChangeSetServiceTestIT extends AbstractTestBase {

	@Resource
	def MatChangeSetService service

	def marshaller = new DelegatingMarshaller()

	void setUp() {

	}

	@Test
	void TestSetUp() {
		assertNotNull service
	}

	@Test
	void TestRead() {
		def changeSet = service.createChangeSet()
		assertNotNull service.readChangeSet(changeSet.getChangeSetURI())
	}

	@Test
	void TestCreate() {
		def changeSet = service.createChangeSet()
		assertNotNull changeSet
		assertNotNull changeSet.getChangeSetURI()
	}

	@Test(expected = RuntimeException.class)
	void TestCommit() {
		service.commitChangeSet("test")
	}

	@Test
	void TestUpdateMetadata() {
		def changeSet = service.createChangeSet()
		def uri = changeSet.getChangeSetURI()
		def creator = new SourceReference()
		creator.setContent("test creator")
		def date = new Date()
		service.updateChangeSetMetadata(uri, creator, null, date)
		changeSet = service.readChangeSet(uri)
		assertEquals "test creator", changeSet.getChangeSetElementGroup().creator.content
		assertEquals date, changeSet.getOfficialEffectiveDate()
	}

	@Test
	void TestRollback() {
		def changeSet = service.createChangeSet()
		def uri = changeSet.getChangeSetURI()
		assertNotNull service.readChangeSet(uri)
		service.rollbackChangeSet(uri)
		assertNull service.readChangeSet(uri)
	}

	@Test(expected = RuntimeException.class)
	void TestImport() {
		service.importChangeSet("test")
	}

}
