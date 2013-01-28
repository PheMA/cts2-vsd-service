package edu.mayo.cts2.framework.plugin.service.mat.repository

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.plugin.service.mat.model.{ValueSetChange}
import org.springframework.data.repository.CrudRepository
import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.jpa.repository
import repository.Query
import org.springframework.data.repository.query.Param

@Repository
@Transactional
trait ChangeSetRepository extends CrudRepository[ValueSetChange, String] {

  def findAll(pageable: Pageable): Page[ValueSetChange]

  @Query("select c from ValueSetChange c where c.creator=:creator")
  def findChangeSetsByCreator(@Param("creator")creator: String, pageable: Pageable): Page[ValueSetChange]

}
