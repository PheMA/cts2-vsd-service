package edu.mayo.cts2.framework.plugin.service.mat.profile.valuesetdefinition

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.core.ChangeDescription
import edu.mayo.cts2.framework.model.core.ChangeableElementGroup
import edu.mayo.cts2.framework.model.core.RoleReference
import edu.mayo.cts2.framework.model.core.SourceAndNotation
import edu.mayo.cts2.framework.model.core.SourceAndRoleReference
import edu.mayo.cts2.framework.model.core.SourceReference
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.core.ValueSetReference
import edu.mayo.cts2.framework.model.core.types.ChangeCommitted
import edu.mayo.cts2.framework.model.core.types.ChangeType
import edu.mayo.cts2.framework.model.core.types.EntryState
import edu.mayo.cts2.framework.model.core.types.SetOperator
import edu.mayo.cts2.framework.model.updates.ChangeSet
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.model.valuesetdefinition.SpecificEntityList
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.MatValueSetDefinition
import edu.mayo.cts2.framework.plugin.service.mat.profile.update.MatChangeSetService
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import edu.mayo.cts2.framework.service.profile.UpdateChangeableMetadataRequest
import edu.mayo.cts2.framework.service.profile.valuesetdefinition.name.ValueSetDefinitionReadId
import org.junit.Ignore
import org.junit.Test
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

class MatValueSetDefinitionMaintenanceServiceTestIT extends AbstractTestBase {

	@Resource
	def PlatformTransactionManager txManager

	@Resource
	def MatChangeSetService changeSetService

	@Resource
	def MatValueSetDefinitionMaintenanceService valueSetDefinitionMaintenanceService

	@Test
	def void setUp() {
		assertNotNull changeSetService
		assertNotNull valueSetDefinitionMaintenanceService
	}

	@Test
	void createResourceTest() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				ChangeSet changeSet = changeSetService.createChangeSet()
				ValueSetDefinition definition = createNewValueSetDefinition(changeSet.getChangeSetURI())
				saveAndValidateDefinition(definition)
				validateValueSetDefinitionWasCreated(definition.getDocumentURI())
				validateChangeSetWasCreated(definition, changeSet.getChangeSetURI())
			}
		})

	}

	@Test
	void deleteResourceTest() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult(){
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				ChangeSet changeSet = changeSetService.createChangeSet()
				ValueSetDefinition definition = createNewValueSetDefinition(changeSet.getChangeSetURI())
				saveAndValidateDefinition(definition)
				validateValueSetDefinitionWasCreated(definition.getDocumentURI())
				validateChangeSetWasCreated(definition, changeSet.getChangeSetURI())

				valueSetDefinitionMaintenanceService.deleteResource(new ValueSetDefinitionReadId(definition.getDocumentURI()), changeSet.getChangeSetURI())
				def vsdResult = valueSetDefinitionMaintenanceService.valueSetDefinitionRepository().findOne(definition.getDocumentURI())
				assertNull vsdResult
			}
		})
	}

	@Test
	void updateChangeableMetadataTest() {
		def txTemplate = new TransactionTemplate(txManager)

		txTemplate.execute(new TransactionCallbackWithoutResult(){
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				ChangeSet changeSet = changeSetService.createChangeSet()
				ValueSetDefinition definition = createNewValueSetDefinition(changeSet.getChangeSetURI())
				saveAndValidateDefinition(definition)
				validateValueSetDefinitionWasCreated(definition.getDocumentURI())
				validateChangeSetWasCreated(definition, changeSet.getChangeSetURI())

				UpdateChangeableMetadataRequest request = new UpdateChangeableMetadataRequest()
				request.setChangeSource(new SourceReference(content: "Changed Source"))
				request.setChangeNotes(ModelUtils.createOpaqueData("Changed Notes"))

				valueSetDefinitionMaintenanceService.updateChangeableMetadata(new ValueSetDefinitionReadId(definition.getDocumentURI()), request)
				def vsdResult = valueSetDefinitionMaintenanceService.valueSetDefinitionRepository().findOne(definition.getDocumentURI())
				assertNotNull vsdResult
				assertEquals "Changed Source", vsdResult.getSourceAndRole(0).content
				assertEquals "Changed Notes", vsdResult.getNote(0)
			}
		})
	}

	@Test
	@Ignore
	void updateResourceTest() {

	}

	@Test
	@Ignore
	void removeDefinitionEntryTest() {

	}

	private void saveAndValidateDefinition(ValueSetDefinition definition) {
		valueSetDefinitionMaintenanceService.createResource(definition)
		assertNotNull definition
		assertNotNull definition.getDocumentURI()
		printResource(definition, "Created Definition")
	}

	private void validateValueSetDefinitionWasCreated(String documentUri) {
		def vsdResult = valueSetDefinitionMaintenanceService.valueSetDefinitionRepository().findOne(documentUri)
		assertNotNull vsdResult
		assertEquals documentUri, vsdResult.getDocumentURI()
		printResource(vsdResult, "Returned Definition")
	}

	private void validateChangeSetWasCreated(ValueSetDefinition definition, String changeSetUri) {
		def csResult = changeSetService.changeSetRepository().findOne(definition.getChangeableElementGroup().changeDescription.containingChangeSet)
		assertNotNull csResult
		assertEquals changeSetUri, csResult.getId()
		printResource(csResult, "Returned ChangeSet")
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

	protected static ValueSetDefinition createNewValueSetDefinition(String changeSetUri) {
		def definition = getValueSetDefinition()

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

	protected static ValueSetDefinition getValueSetDefinition() {
		def definition = new ValueSetDefinition()

		definition.setDocumentURI(UUID.randomUUID().toString())
		definition.setEntryState(EntryState.ACTIVE)
		definition.setAbout("This is a test value set description.")
		definition.setDefinedValueSet(new ValueSetReference("test.value.set.ref"))

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

	public static void main(String[] args) {
		ValueSetDefinition definition = this.createNewValueSetDefinition()
		StringWriter sw = new StringWriter()
		DelegatingMarshaller marshaller = new DelegatingMarshaller()
		marshaller.marshal(definition, new StreamResult(sw))
		println(sw)
	}

}
