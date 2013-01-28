package edu.mayo.cts2.framework.plugin.service.mat.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetEntry
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetVersion

@Repository
@Transactional
trait ValueSetVersionRepository extends CrudRepository[ValueSetVersion, String] {

  def findAll(pageable: Pageable): Page[ValueSetVersion]
  
  @Query("select vsv from ValueSetVersion vsv where vsv.valueSet.name = :name")
  def findByValueSetName(@Param("name") name: String, pageable: Pageable): Page[ValueSetVersion]

  @Query("select vs.currentVersion from ValueSet vs where vs.name = :name")
  def findCurrentVersionByValueSetName(@Param("name") name: String): ValueSetVersion

//  @Query("select vsv from ValueSetVersion vsv where vsv.valueSet.name = :name and (vsv.id = :id or vsv.version = :id)")
//  def findVersionByIdOrVersionIdAndValueSetName(@Param("name") name:String, @Param("id") id:String): ValueSetVersion

  @Query("select distinct entries.codeSystem, entries.codeSystemVersion from ValueSetVersion valueSet " +
  		"inner join valueSet.entries entries where valueSet.version = :version")
  def findCodeSystemVersionsByValueSetVersion(@Param("version") version:String): java.util.Collection[Array[String]]

  @Query("select vse from ValueSetEntry vse where vse.valueSetVersion.changeSetUri = :changeSetUri")
  def findValueSetEntriesByChangeSetUri(@Param("changeSetUri") changeSetUri:String, pageable: Pageable): Page[ValueSetEntry]

  @Query("select vse from ValueSetEntry vse where vse.valueSetVersion.documentUri in (:ids)")
  def findValueSetEntriesByValueSetVersionIds(@Param("ids") id:java.util.List[String], pageable: Pageable): Page[ValueSetEntry]

  @Query("select vsv from ValueSetVersion vsv where vsv.valueSet.name = :name and vsv.creator = :creator")
  def findByValueSetNameAndCreator(@Param("name") name: String, @Param("creator") creator: String, pageable: Pageable): Page[ValueSetVersion]

  @Query("select vsv from ValueSetVersion vsv where vsv.creator = :creator")
  def findByCreator(@Param("creator") creator: String, pageable: Pageable): Page[ValueSetVersion]

  @Query("select c.currentVersion from ValueSetChange c where c.changeSetUri = :changeSetUri")
  def findByChangeSetUri(@Param("changeSetUri") changeSetUri: String): ValueSetVersion

}