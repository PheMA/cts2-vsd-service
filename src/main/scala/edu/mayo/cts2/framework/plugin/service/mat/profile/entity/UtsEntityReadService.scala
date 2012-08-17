package edu.mayo.cts2.framework.plugin.service.mat.profile.entity

import scala.Option.option2Iterable
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import org.springframework.stereotype.Component
import edu.mayo.cts2.framework.model.command.Page
import edu.mayo.cts2.framework.model.command.ResolvedReadContext
import edu.mayo.cts2.framework.model.core.CodeSystemReference
import edu.mayo.cts2.framework.model.core.CodeSystemVersionReference
import edu.mayo.cts2.framework.model.core.Definition
import edu.mayo.cts2.framework.model.core.EntityReference
import edu.mayo.cts2.framework.model.core.NameAndMeaningReference
import edu.mayo.cts2.framework.model.core.SortCriteria
import edu.mayo.cts2.framework.model.core.URIAndEntityName
import edu.mayo.cts2.framework.model.core.VersionTagReference
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.model.entity.Designation
import edu.mayo.cts2.framework.model.entity.EntityDescription
import edu.mayo.cts2.framework.model.entity.EntityList
import edu.mayo.cts2.framework.model.entity.EntityListEntry
import edu.mayo.cts2.framework.model.entity.NamedEntityDescription
import edu.mayo.cts2.framework.model.service.core.EntityNameOrURI
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.profile.AbstractService
import edu.mayo.cts2.framework.service.profile.entitydescription.name.EntityDescriptionReadId
import edu.mayo.cts2.framework.service.profile.entitydescription.EntityDescriptionReadService
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.namespace.NamespaceResolutionService
import edu.mayo.cts2.framework.model.core.Property
import edu.mayo.cts2.framework.model.core.StatementTarget
import edu.mayo.cts2.framework.model.core.PredicateReference
import edu.mayo.cts2.framework.plugin.service.mat.umls.dao.UtsDao
import gov.nih.nlm.umls.uts.webservice.Psf
import gov.nih.nlm.umls.uts.webservice.AtomDTO
import edu.mayo.cts2.framework.model.core.ScopedEntityName

@Component
class UtsEntityReadService extends AbstractService
  with EntityDescriptionReadService {

  @Resource
  var namespaceResolutionService: NamespaceResolutionService = _
  
  @Resource
  var utsDao: UtsDao = _

  def readEntityDescriptions(p1: EntityNameOrURI, p2: SortCriteria, p3: ResolvedReadContext, p4: Page): DirectoryResult[EntityListEntry] = throw new RuntimeException()

  def availableDescriptions(p1: EntityNameOrURI, p2: ResolvedReadContext): EntityReference = throw new RuntimeException()

  def readEntityDescriptions(p1: EntityNameOrURI, p2: ResolvedReadContext): EntityList = throw new RuntimeException()

  def getKnownCodeSystems: java.util.List[CodeSystemReference] = throw new RuntimeException()

  def getKnownCodeSystemVersions: java.util.List[CodeSystemVersionReference] = throw new RuntimeException()

  def read(id: EntityDescriptionReadId, context: ResolvedReadContext = null): EntityDescription = {

    val csv = id.getCodeSystemVersion.getName
    val fn = utsDao.utsContentService.getCodeAtoms _
  
    val atoms = utsDao.callSecurely(fn(_, _, id.getEntityName.getName, csv, new Psf()))
    
    if(atoms.size == 0){
      return null;
    }
    
    val namedEntity = atoms.foldLeft(atomToNamedEntityDescription(atoms.get(0)))(
    		(entity, atom) => {
    		  entity
    		}
    )
    
    val ed = new EntityDescription()
    ed.setNamedEntity(namedEntity)
    
    ed
  }
  
  private def atomToNamedEntityDescription(atom:AtomDTO):NamedEntityDescription = {
    val ed = new NamedEntityDescription()
    ed.setAbout("TODO")
    
    val name = new ScopedEntityName()
    name.setName(atom.getSourceUi)
    name.setNamespace(atom.getRootSource)
    ed.setEntityID(name)
    
    ed
  }

  def exists(p1: EntityDescriptionReadId, p2: ResolvedReadContext): Boolean = throw new RuntimeException()

  def getSupportedVersionTags: java.util.List[VersionTagReference] =
    List[VersionTagReference](CURRENT_TAG)
}