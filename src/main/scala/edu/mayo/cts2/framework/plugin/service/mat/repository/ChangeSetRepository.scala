package edu.mayo.cts2.framework.plugin.service.mat.repository

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.plugin.service.mat.model.MatChangeSet
import org.springframework.data.repository.CrudRepository
import org.springframework.data.domain.{Page, Pageable}

@Repository
@Transactional
trait ChangeSetRepository extends CrudRepository[MatChangeSet, String] {

  def findAll(pageable: Pageable): Page[MatChangeSet]

}
