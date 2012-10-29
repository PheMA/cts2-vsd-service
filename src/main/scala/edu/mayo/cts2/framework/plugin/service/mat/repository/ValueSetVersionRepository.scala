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
trait ValueSetVersionRepository extends CrudRepository[ValueSetVersion, String] {

  def findAll(pageable: Pageable): Page[ValueSetVersion]
  
  @Query("select vsv from ValueSetVersion vsv where vsv.valueSet.name = :name")
  def findByValueSetName(@Param("name") name: String, pageable: Pageable): Page[ValueSetVersion]

  @Query("select vsv from ValueSetVersion vsv where vsv.valueSet.name = :name and (vsv.id = :id or vsv.versionId = :id)")
  def findVersionByIdOrVersionIdAndValueSetName(@Param("name") name:String, @Param("id") id:String): ValueSetVersion

  @Query("select distinct entries.codeSystem, entries.codeSystemVersion from ValueSetVersion valueSet " +
  		"inner join valueSet._entries entries where valueSet.id = :id")
  def findCodeSystemVersionsByValueSetVersion(@Param("id") oid:String): java.util.Collection[Array[String]]

  @Query("select vse from ValueSetEntry vse where vse.valueSetVersion.id = :id")
  def findValueSetEntriesByValueSetVersionId(@Param("id") id:String, pageable: Pageable): Page[ValueSetEntry]

  @Query("select vse from ValueSetEntry vse where vse.valueSetVersion.id in (:ids)")
  def findValueSetEntriesByValueSetVersionIds(@Param("ids") id:java.util.List[String], pageable: Pageable): Page[ValueSetEntry]

}