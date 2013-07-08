package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.core.ChangeDescription
import edu.mayo.cts2.framework.model.core.ChangeableElementGroup
import edu.mayo.cts2.framework.model.core.RoleReference
import edu.mayo.cts2.framework.model.core.SourceAndNotation
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference
import edu.mayo.cts2.framework.model.core.SourceReference
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.core.VersionTagReference
import edu.mayo.cts2.framework.model.core.types.ChangeCommitted
import edu.mayo.cts2.framework.model.core.types.ChangeType
import edu.mayo.cts2.framework.model.core.types.EntryState
import edu.mayo.cts2.framework.model.core.types.FinalizableState
import edu.mayo.cts2.framework.model.core.types.SetOperator
import edu.mayo.cts2.framework.model.extension.LocalIdValueSetDefinition
import edu.mayo.cts2.framework.model.service.exception.UnknownChangeSet
import edu.mayo.cts2.framework.model.service.exception.UnknownValueSet
import edu.mayo.cts2.framework.model.updates.ChangeSet
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.model.valueset.ValueSetCatalogEntry
import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.plugin.service.mat.profile.update.MatChangeSetService
import edu.mayo.cts2.framework.plugin.service.mat.profile.valueset.MatValueSetMaintenanceService
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetVersionRepository
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertTrue

class MatValueSetDefinitionMaintenanceServiceTestIT extends AbstractTestBase {

	@Resource
	def PlatformTransactionManager txManager

	@Resource
	def MatChangeSetService changeSetService

	@Resource
	def MatValueSetDefinitionMaintenanceService valueSetDefinitionMaintenanceService

	@Resource
	def MatValueSetMaintenanceService valueSetMaintenanceService

	@Resource
	def ValueSetRepository valueSetRepository

	@Resource
	def ValueSetVersionRepository valueSetVersionRepository

	@Before
	def void setUp() {
		assertNotNull changeSetService
		assertNotNull valueSetDefinitionMaintenanceService
		assertNotNull valueSetMaintenanceService
		assertNotNull valueSetRepository
	}

	@Test
	void createResourceTest() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				def definition = createValueSetDef()

				def createdVersion = valueSetVersionRepository.findOne(definition.getDocumentURI())
				assertNotNull createdVersion
				assertEquals FinalizableState.OPEN, createdVersion.getState()
				assertEquals ChangeCommitted.COMMITTED, createdVersion.getChangeCommitted()
				assertEquals ChangeType.CREATE, createdVersion.getChangeType()

				def valueSet = valueSetRepository.findOne(createdVersion.valueSet.name)
				assertNotNull valueSet
				assertTrue valueSet.versions().contains(createdVersion)
				assertEquals createdVersion.documentUri, valueSet.currentVersion().documentUri
			}
		})

	}

	@Test(expected = UnknownChangeSet.class)
	void createResourceNoChangeSet() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				def definition = createNewValueSetDefinition("i.dont.exist")
				saveAndValidateDefinition(definition)
			}
		})
	}

	@Test(expected = UnknownValueSet.class)
	void createResourceNoValueSet() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				def valueSet = new ValueSet(UUID.randomUUID().toString())
				valueSet.setFormalName("TestValueSet_FORMALNAME")
//				valueSetRepository.save(valueSet)

				def definition = new ValueSetDefinition()

				definition.setState(FinalizableState.OPEN)

				ChangeableElementGroup group = new ChangeableElementGroup()
				ChangeDescription changeDescription = new ChangeDescription()
				changeDescription.setChangeType(ChangeType.CREATE)
				changeDescription.setCommitted(ChangeCommitted.PENDING)
				changeDescription.setContainingChangeSet(changeSetService.createChangeSet().changeSetURI)
				changeDescription.setChangeDate(new Date())
				group.setChangeDescription(changeDescription)
				definition.setChangeableElementGroup(group)

				definition.setDocumentURI(UUID.randomUUID().toString())
				definition.setEntryState(EntryState.ACTIVE)
				definition.setAbout("This is a test value set description.")
				definition.setDefinedValueSet(new ValueSetReference(valueSet.name))

				VersionTagReference[] versionRefs = new VersionTagReference[1]
				versionRefs[0] = new VersionTagReference(UUID.randomUUID().toString().substring(0, 4))
				definition.setVersionTag(versionRefs)

				SourceAndRoleReference sourceAndRole = new SourceAndRoleReference()
				sourceAndRole.setSource(new SourceReference("Dale Suesse"))
				sourceAndRole.setRole(new RoleReference("creator"))
				definition.setSourceAndRole((SourceAndRoleReference[]) [sourceAndRole])

				definition.setSourceAndNotation(new SourceAndNotation())

				SpecificEntityList entityList = new SpecificEntityList()

				URIAndEntityName entity1 = new URIAndEntityName()
				entity1.setName("Test Entity 1")
				entity1.setHref("http://service/testEntity1")
				entity1.setNamespace("TEST")
				entity1.setUri(UUID.randomUUID().toString())

				URIAndEntityName entity2 = new URIAndEntityName()
				entity2.setName("Test Entity 2")
				entity2.setHref("http://service/testEntity2")
				entity2.setNamespace("TEST")
				entity2.setUri(UUID.randomUUID().toString())

				entityList.addReferencedEntity(entity1)
				entityList.addReferencedEntity(entity2)

				ValueSetDefinitionEntry entry = new ValueSetDefinitionEntry()
				entry.setOperator(SetOperator.UNION)
				entry.setEntryOrder(1L)
				entry.setEntityList(entityList)

				definition.setEntry((ValueSetDefinitionEntry[]) [entry])

				saveAndValidateDefinition(definition)
			}
		})
	}

	private ValueSetDefinition createValueSetDef() {
		ChangeSet changeSet = changeSetService.createChangeSet()
		ValueSetDefinition definition = createNewValueSetDefinition(changeSet.getChangeSetURI())
		saveAndValidateDefinition(definition)
		validateValueSetDefinitionWasCreated(definition.getDocumentURI())
		validateChangeSetWasCreated(definition, changeSet.getChangeSetURI())
		definition
	}

	@Test(expected = RuntimeException.class)
	void deleteResourceTest() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult(){

			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				def definition = createValueSetDef()

				/* delete changeset */
				def deleteChangeSet = changeSetService.createChangeSet()
				def changeSetUri = deleteChangeSet.getChangeSetURI()
				valueSetDefinitionMaintenanceService.deleteResource(
						new ValueSetDefinitionReadId(definition.getDocumentURI()),
						changeSetUri)

				def deletedVersion = valueSetVersionRepository.findOne(definition.getDocumentURI())
				assertNotNull deletedVersion
				assertEquals FinalizableState.OPEN, deletedVersion.getState()
				assertEquals ChangeCommitted.PENDING, deletedVersion.getChangeCommitted()
				assertEquals ChangeType.DELETE, deletedVersion.getChangeType()
			}
		})
	}

	@Test
	@Ignore
	void updateChangeableMetadataTest() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult(){
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				def definition = createValueSetDef()

				/* update metadata */
				def updatedChangeSet = changeSetService.createChangeSet()
				def changeSetUri = updatedChangeSet.getChangeSetURI()

				def metadata = new UpdateChangeableMetadataRequest()
				metadata.setChangeSource(new SourceReference("UpdateMetadata Source"))
				metadata.setChangeNotes(ModelUtils.createOpaqueData("UpdateMetadata change notes"))

				def readId = new ValueSetDefinitionReadId()

				valueSetDefinitionMaintenanceService.updateChangeableMetadata(
						new ValueSetDefinitionReadId(definition.getDocumentURI()),
						metadata
				)

				def updatedVersion = valueSetVersionRepository.findOne(definition.getDocumentURI())
				assertNotNull updatedVersion
				assertEquals "UpdateMetadata Source", updatedVersion.getSource()
				assertEquals "UpdateMetadata change notes", updatedVersion.getNotes()

			}
		})
	}

	@Test
	void updateResourceTest() {
		def definition = createValueSetDef()
		def page = new Page()
		def pageable = new PageRequest(page.getPage(), page.getMaxToReturn())
		def changeSetUri = definition.getChangeableElementGroup().getChangeDescription().getContainingChangeSet()

		/* update definition */
		URIAndEntityName entity = new URIAndEntityName()
		entity.setName("Test Entity 3")
		entity.setHref("http://service/testEntity3")
		entity.setNamespace("TEST")
		entity.setUri(UUID.randomUUID().toString())
		definition.getEntry(0).entityList.addReferencedEntity(entity)
		def localDef = new LocalIdValueSetDefinition(definition.getDocumentURI(), definition)
		valueSetDefinitionMaintenanceService.updateResource(localDef)

		/* Check Original Version */
		def originalVersion = valueSetVersionRepository.findOne(definition.getDocumentURI())
		assertNotNull originalVersion
		assertEquals FinalizableState.OPEN, originalVersion.getState()
		def entries = valueSetVersionRepository.findValueSetEntriesByValueSetVersion(originalVersion.getVersion(), pageable)
		assertEquals 2, entries.numberOfElements
		assertEquals ChangeCommitted.PENDING, originalVersion.getChangeCommitted()
		assertEquals ChangeType.CREATE, originalVersion.getChangeType()

		/* Check Updated Version */
		def updatedVersion = valueSetVersionRepository.findByChangeSetUri(changeSetUri)
		assertNotNull updatedVersion
		assertEquals FinalizableState.OPEN, updatedVersion.getState()
		entries = valueSetVersionRepository.findValueSetEntriesByValueSetVersion(updatedVersion.getVersion(), pageable)
		assertEquals 3, entries.numberOfElements
		assertEquals ChangeCommitted.PENDING, updatedVersion.getChangeCommitted()
		assertEquals ChangeType.UPDATE, updatedVersion.getChangeType()

	}

	@Test
	@Ignore
	void removeDefinitionEntryTest() {

	}

	private void saveAndValidateDefinition(ValueSetDefinition definition) {
		valueSetDefinitionMaintenanceService.createResource(definition)
		assertNotNull definition
		assertNotNull definition.getDocumentURI()
	}

	private void validateValueSetDefinitionWasCreated(String documentUri) {
		def vsdResult = valueSetDefinitionMaintenanceService.versionRepo().findOne(documentUri)
		assertNotNull vsdResult
		assertEquals documentUri, vsdResult.getDocumentUri()
//		printResource(vsdResult, "Returned Definition")
	}

	private void validateChangeSetWasCreated(ValueSetDefinition definition, String changeSetUri) {
		def csResult = changeSetService.changeSetRepository().findOne(definition.getChangeableElementGroup().changeDescription.containingChangeSet)
		assertNotNull csResult
		assertEquals changeSetUri, csResult.getChangeSetUri()
//		printResource(csResult, "Returned ChangeSet")
	}

	private static void printResource(Object o, String header) {
		StringWriter sw = new StringWriter();
		DelegatingMarshaller marshaller = new DelegatingMarshaller()
		marshaller.marshal(o, new StreamResult(sw))
		if (header == null) {
			println sw
		}
		else {
			println header + ":\n" + sw
		}
	}

	private static void printResource(Object o) {
		printResource(o, null)
	}

	protected ValueSetDefinition createNewValueSetDefinition(String changeSetUri) {
		def definition = getValueSetDefinition()
		definition.setState(FinalizableState.OPEN)

		ChangeableElementGroup group = new ChangeableElementGroup()
		ChangeDescription changeDescription = new ChangeDescription()
		changeDescription.setChangeType(ChangeType.CREATE)
		changeDescription.setCommitted(ChangeCommitted.PENDING)
		changeDescription.setContainingChangeSet(changeSetUri)
		changeDescription.setChangeDate(new Date())
		group.setChangeDescription(changeDescription)
		definition.setChangeableElementGroup(group)

		definition
	}

	protected ValueSetDefinition getValueSetDefinition() {
		ValueSetCatalogEntry valueSet = createValueSetCatalogEntry()
		valueSetMaintenanceService.createResource(valueSet)

//		def valueSet = new ValueSet(UUID.randomUUID().toString())
//		valueSet.setFormalName("TestValueSet_FORMALNAME")
//		valueSetRepository.save(valueSet)

		def definition = new ValueSetDefinition()

		definition.setDocumentURI(UUID.randomUUID().toString())
		definition.setEntryState(EntryState.ACTIVE)
		definition.setAbout("This is a test value set description.")
		definition.setDefinedValueSet(new ValueSetReference(valueSet.valueSetName))

		VersionTagReference[] versionRefs = new VersionTagReference[1]
		versionRefs[0] = new VersionTagReference(UUID.randomUUID().toString().substring(0, 4))
		definition.setVersionTag(versionRefs)

		SourceAndRoleReference sourceAndRole = new SourceAndRoleReference()
		sourceAndRole.setSource(new SourceReference("Dale Suesse"))
		sourceAndRole.setRole(new RoleReference("creator"))
		definition.setSourceAndRole((SourceAndRoleReference[]) [sourceAndRole])

		definition.setSourceAndNotation(new SourceAndNotation())

		SpecificEntityList entityList = new SpecificEntityList()

		URIAndEntityName entity1 = new URIAndEntityName()
		entity1.setName("Test Entity 1")
		entity1.setHref("http://service/testEntity1")
		entity1.setNamespace("TEST")
		entity1.setUri(UUID.randomUUID().toString())

		URIAndEntityName entity2 = new URIAndEntityName()
		entity2.setName("Test Entity 2")
		entity2.setHref("http://service/testEntity2")
		entity2.setNamespace("TEST")
		entity2.setUri(UUID.randomUUID().toString())

		entityList.addReferencedEntity(entity1)
		entityList.addReferencedEntity(entity2)

		ValueSetDefinitionEntry entry = new ValueSetDefinitionEntry()
		entry.setOperator(SetOperator.UNION)
		entry.setEntryOrder(1L)
		entry.setEntityList(entityList)

		definition.setEntry((ValueSetDefinitionEntry[]) [entry])

		definition
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

		def cs = changeSetService.createChangeSet()

		def changeDescription = new ChangeDescription()
		changeDescription.setChangeDate(new Date())
		changeDescription.setChangeType(ChangeType.CREATE)
		changeDescription.setContainingChangeSet(cs.getChangeSetURI())
		changeDescription.setCommitted(ChangeCommitted.PENDING);

		def ceg = new ChangeableElementGroup()
		ceg.setChangeDescription(changeDescription)

		vs.setChangeableElementGroup(ceg)

		return vs
	}

	public static void main(String[] args) {
		MatValueSetDefinitionMaintenanceServiceTestIT test = new MatValueSetDefinitionMaintenanceServiceTestIT()
		ValueSetDefinition definition = test.createNewValueSetDefinition(UUID.randomUUID().toString())
		StringWriter sw = new StringWriter()
		DelegatingMarshaller marshaller = new DelegatingMarshaller()
		marshaller.marshal(definition, new StreamResult(sw))
		println(sw)
	}

}
