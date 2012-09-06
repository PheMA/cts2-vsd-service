package edu.mayo.cts2.framework.plugin.service.mat.repository;

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import scala.reflect.BeanProperty
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

@Repository
@Transactional
trait ValueSetRepository extends CrudRepository[ValueSet, String] {

  def findAll(pageable: Pageable): Page[ValueSet]
  
  @Query("select vs from ValueSet vs where upper(vs.name) = upper(:query) or upper(vs.oid) = upper(:query)")
  def findByAnyLikeIgnoreCase(@Param("query") query:String, pageable: Pageable): Page[ValueSet]

  def findByNameLikeIgnoreCase(query:String, pageable: Pageable): Page[ValueSet]
  
  def findByFormalNameLikeIgnoreCase(query:String, pageable: Pageable): Page[ValueSet]
  
  def findOneByName(query:String): ValueSet
  
  @Query("select distinct entries.codeSystem, entries.codeSystemVersion from ValueSet valueSet " +
  		"inner join valueSet.entries entries where valueSet.oid = :oid")
  def findCodeSystemVersionsByOid(@Param("oid") oid:String): java.util.Collection[Array[String]]
  
    @Query("select distinct entries.codeSystem, entries.codeSystemVersion from ValueSet valueSet " +
  		"inner join valueSet.entries entries where valueSet.name = :name")
  def findCodeSystemVersionsByName(@Param("name") name:String): java.util.Collection[Array[Any]]
  
}