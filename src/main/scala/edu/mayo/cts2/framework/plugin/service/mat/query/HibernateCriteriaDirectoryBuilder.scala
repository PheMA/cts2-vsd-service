package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import scala.collection.JavaConversions._
import org.springframework.data.jpa.domain.Specifications
import edu.mayo.cts2.framework.filter.directory.AbstractStateBuildingDirectoryBuilder.Callback
import edu.mayo.cts2.framework.model.directory.DirectoryResult
import edu.mayo.cts2.framework.plugin.service.mat.profile.ProfileUtils
import javax.persistence.EntityManager
import org.springframework.data.jpa.domain.Specification
import org.apache.commons.collections.CollectionUtils

class CriteriaCallback[T, X](
  entityManager: EntityManager,
  resultClass: Class[X],
  transform: (X) => T) extends Callback[Seq[Specification[X]], T] {

  def executeCount(criteriaQuery: Seq[Specification[X]]): Int = {
    throw new UnsupportedOperationException()
  }

  def execute(specificationList: Seq[Specification[X]],
    start: Int,
    maxResults: Int): DirectoryResult[T] = {

    val cb = entityManager.getCriteriaBuilder()
    val q = cb.createQuery(resultClass).distinct(true)
    val from = q.from(resultClass)

    val query =

      if (CollectionUtils.isNotEmpty(specificationList)) {
        var specifications: Specifications[X] = null

        specificationList.foreach((specification) => {

          specifications =
            if (specifications == null) {
              Specifications.where(specification)
            } else {
              specifications.and(specification)
            }

        })

        entityManager.createQuery(ProfileUtils.addSpecifications(specifications, from, q, cb))

      } else {
        entityManager.createQuery(q.select(from))
      }

    val entries = query.setFirstResult(start).setMaxResults(maxResults + 1).getResultList.foldLeft(Seq[T]())(
      _ :+ transform(_))

    val hasMore: Boolean = entries.size == maxResults + 1

    val finalEntries =
      if (hasMore) {
        entries.slice(0, entries.size - 1)
      } else {
        entries
      }

    new DirectoryResult[T](finalEntries, !hasMore);

  }
}

//class HibernateCriteriaDirectoryBuilder[T, X](
//  resultClass: Class[X],
//  entityManager: EntityManager,
//  transform: (X) => T,
//  matchAlgorithmReferences: java.util.Set[MatchAlgorithmReference],
//  stateAdjustingPropertyReferences: java.util.Set[StateAdjustingPropertyReference[Seq[Specification[X]]]])
//
//  extends AbstractStateBuildingDirectoryBuilder[Seq[Specification[X]], T](
//    new java.util.ArrayList[Specification[X]](),
//    new CriteriaCallback[T, X](entityManager, resultClass, transform),
//    matchAlgorithmReferences,
//    stateAdjustingPropertyReferences) {
//
//}