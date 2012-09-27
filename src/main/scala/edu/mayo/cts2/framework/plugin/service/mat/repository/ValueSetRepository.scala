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
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion

@Repository
@Transactional
trait ValueSetRepository extends CrudRepository[ValueSet, String] {

  def findAll(pageable: Pageable): Page[ValueSet]
  
  @Query("select vs from ValueSet vs where upper(vs.name) like upper(:query) or upper(vs.formalName) like upper(:query) " +
  		"or upper(vs.id.oid) like upper(:query)")
  def findByAnyLikeIgnoreCase(@Param("query") query:String, pageable: Pageable): Page[ValueSet]
  
  @Query("select vs from ValueSet vs join vs.properties vsp " +
  		"where vsp.name = :propertyName and upper(vsp.value) like upper(:query)" )
  def findByPropertyLikeIgnoreCase(
      @Param("propertyName") propertyName:String, 
      @Param("query") query:String, 
      pageable: Pageable): Page[ValueSet]

  @Query("select vs.currentVersion.id from ValueSet vs where vs.name = :name")
  def findCurrentVersionIdByName(@Param("name") name:String): String
 
  def findByNameLikeIgnoreCase(query:String, pageable: Pageable): Page[ValueSet]
  
  def findByFormalNameLikeIgnoreCase(query:String, pageable: Pageable): Page[ValueSet]
  
  def findOneByName(query:String): ValueSet

}