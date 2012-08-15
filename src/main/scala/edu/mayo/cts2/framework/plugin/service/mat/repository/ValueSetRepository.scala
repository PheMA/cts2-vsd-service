package edu.mayo.cts2.framework.plugin.service.mat.repository;

import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Repository
import org.springframework.data.repository.CrudRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Repository
@Transactional
trait ValueSetRepository extends CrudRepository[ValueSet, String] {

  def findAll(pageable: Pageable): Page[ValueSet]
  
}
