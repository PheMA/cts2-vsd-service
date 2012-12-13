package edu.mayo.cts2.framework.plugin.service.mat.repository

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.repository.CrudRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.MatValueSetDefinition
import org.springframework.data.repository.query.Param
import org.springframework.data.jpa.repository.Query

@Repository
@Transactional
trait ValueSetDefinitionRepository extends CrudRepository[MatValueSetDefinition, String] {

  @Query("select vsd from MatValueSetDefinition vsd where vsd.documentURI=:documentURI")
  def getValueSetDefinition(@Param("documentURI") documentURI: String): MatValueSetDefinition

}
