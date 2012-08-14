package edu.mayo.cts2.framework.plugin.service.mat.profile.entity

import scala.Option.option2Iterable
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.JavaConverters.asScalaBufferConverter
import scala.collection.JavaConverters.mapAsScalaMapConverter
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

@Component
class UtsEntityReadService extends AbstractService
  with EntityDescriptionReadService {

  @Resource
  var namespaceResolutionService: NamespaceResolutionService = _

  def readEntityDescriptions(p1: EntityNameOrURI, p2: SortCriteria, p3: ResolvedReadContext, p4: Page): DirectoryResult[EntityListEntry] = throw new RuntimeException()

  def availableDescriptions(p1: EntityNameOrURI, p2: ResolvedReadContext): EntityReference = throw new RuntimeException()

  def readEntityDescriptions(p1: EntityNameOrURI, p2: ResolvedReadContext): EntityList = throw new RuntimeException()

  def getKnownCodeSystems: java.util.List[CodeSystemReference] = throw new RuntimeException()

  def getKnownCodeSystemVersions: java.util.List[CodeSystemVersionReference] = throw new RuntimeException()

  def read(id: EntityDescriptionReadId, context: ResolvedReadContext = null): EntityDescription = {

   throw new UnsupportedOperationException()
  }
  
  

  def exists(p1: EntityDescriptionReadId, p2: ResolvedReadContext): Boolean = throw new RuntimeException()

  def getSupportedVersionTags: java.util.List[VersionTagReference] =
    List[VersionTagReference](CURRENT_TAG)
}