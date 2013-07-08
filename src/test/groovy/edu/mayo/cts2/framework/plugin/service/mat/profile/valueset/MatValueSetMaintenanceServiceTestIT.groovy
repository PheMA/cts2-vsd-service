package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import edu.mayo.cts2.framework.model.core.ChangeDescription
import edu.mayo.cts2.framework.model.core.ChangeableElementGroup
import edu.mayo.cts2.framework.model.core.RoleReference
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference
import edu.mayo.cts2.framework.model.core.SourceReference
import edu.mayo.cts2.framework.model.core.types.ChangeCommitted
import edu.mayo.cts2.framework.model.core.types.ChangeType
import edu.mayo.cts2.framework.model.service.exception.UnknownChangeSet
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntrySummary
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.service.profile.update.ChangeSetService
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetMaintenanceService
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQuery
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetQueryService
import org.junit.Ignore
import org.junit.Test

import javax.annotation.Resource

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue

class MatValueSetMaintenanceServiceTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetMaintenanceService service

	@Resource
	def ValueSetQueryService queryService

	@Resource
	def ChangeSetService changeSetService

	@Test
	void TestSetUp() {
		assertNotNull service
		assertNotNull queryService
		assertNotNull changeSetService
	}

	@Test
	void TestCreateValueSet() {
		def vs = createValueSetCatalogEntry()

		def cs = changeSetService.createChangeSet()

		def changeDescription = new ChangeDescription()
		changeDescription.setChangeDate(new Date())
		changeDescription.setChangeType(ChangeType.CREATE)
		changeDescription.setContainingChangeSet(cs.getChangeSetURI())
		changeDescription.setCommitted(ChangeCommitted.PENDING);

		def ceg = new ChangeableElementGroup()
		ceg.setChangeDescription(changeDescription)

		vs.setChangeableElementGroup(ceg)

		assertNotNull service.createResource(vs)

		def dr = queryService.getResourceSummaries(null as ValueSetQuery,null,null)
		def vsName = new ArrayList<String>()
		for (ValueSetCatalogEntrySummary entry : dr.entries) {
			vsName.add(entry.getValueSetName())
		}
		assertTrue(vsName.contains(vs.getValueSetName()))
	}

	@Test(expected = UnknownChangeSet.class)
	void TestCreateWithNoChangeSet() {
		service.createResource(createValueSetCatalogEntry())
	}

	@Ignore
	@Test
	void TestUpdateValueSet() {
		/* TODO: implement test */
	}

	@Ignore
	@Test
	void TestDeleteValueSet() {
		/* TODO: implement test */
	}

	private ValueSetCatalogEntry createValueSetCatalogEntry() {
		ValueSetCatalogEntry vs = new ValueSetCatalogEntry()
		def id = UUID.randomUUID().toString()
		vs.setValueSetName(id)
		vs.setFormalName("TestValueSet" + id)
		vs.setAbout("urn:uuid:" + UUID.randomUUID().toString())
		SourceAndRoleReference snrr = new SourceAndRoleReference()
		snrr.setRole(new RoleReference("Author"))
		snrr.setSource(new SourceReference("jUnitTester"))
		vs.addSourceAndRole(snrr)
		return vs
	}

}
