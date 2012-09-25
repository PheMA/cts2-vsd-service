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

}