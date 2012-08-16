package edu.mayo.cts2.framework.plugin.service.mat.repository;

import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Repository
import org.springframework.data.repository.CrudRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

@Repository
@Transactional
trait ValueSetRepository extends CrudRepository[ValueSet, String] {

  def findAll(pageable: Pageable): Page[ValueSet]
  
  @Query("select vs from ValueSet vs where vs.name = :query or vs.oid = :query")
  def findByAny(@Param("query") query:String, pageable: Pageable): Page[ValueSet]
  
}
