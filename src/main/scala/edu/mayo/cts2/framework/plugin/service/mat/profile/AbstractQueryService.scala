package edu.mayo.cts2.framework.plugin.service.mat.profile

import edu.mayo.cts2.framework.model.core.{ComponentReference, PredicateReference, MatchAlgorithmReference}
import javax.persistence.criteria._
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference

abstract class AbstractQueryService extends AbstractService {

  def getSupportedSortReferences: java.util.Set[_ <: ComponentReference] = { new java.util.HashSet[ComponentReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  private def filterFn(ref: MatchAlgorithmReference) = {
    val matchAlgorithm = ref.getContent
    val contains = StandardMatchAlgorithmReference.CONTAINS.getMatchAlgorithmReference.getContent
    val startsWith = StandardMatchAlgorithmReference.STARTS_WITH.getMatchAlgorithmReference.getContent
    val exact = StandardMatchAlgorithmReference.EXACT_MATCH.getMatchAlgorithmReference.getContent

    val cb = entityManager.getCriteriaBuilder

    val cbLikeFn = cb.like(_:Expression[String],_:String)
    val cbEqualsFn = cb.equal(_:Expression[String],_:String)

    val likeFn = (ex:Expression[String], formatter:(String) => String, q:String) => {
      cbLikeFn(cb.lower(ex),formatter(q))
    }

    val equalsFn = (ex:Expression[String], formatter:(String) => String, q:String) => {
      cbEqualsFn(ex,formatter(q))
    }

    matchAlgorithm match {
      case `contains` => ( likeFn(_:Expression[String], (s:String) => '%'+s.toLowerCase+'%', _:String ) )
      case `startsWith` => ( likeFn(_:Expression[String], (s:String) => s.toLowerCase+'%', _:String ) )
      case `exact` => ( equalsFn(_:Expression[String], (s:String) => s, _:String ) )
    }

  }: (Expression[String], String) => Predicate

}
