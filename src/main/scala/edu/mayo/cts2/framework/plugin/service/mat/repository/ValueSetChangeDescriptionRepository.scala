package edu.mayo.cts2.framework.plugin.service.mat.repository

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.springframework.data.repository.CrudRepository
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSetChangeDescription

@Repository
@Transactional
trait ValueSetChangeDescriptionRepository extends CrudRepository[ValueSetChangeDescription, String] {

}
