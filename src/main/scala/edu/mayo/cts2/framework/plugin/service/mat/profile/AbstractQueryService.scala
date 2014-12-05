package edu.mayo.cts2.framework.plugin.service.mat.profile

import edu.mayo.cts2.framework.filter.`match`.StateAdjustingComponentReference
import edu.mayo.cts2.framework.filter.`match`.StateAdjustingComponentReference.StateUpdater
import edu.mayo.cts2.framework.model.core.{URIAndEntityName, ComponentReference, PredicateReference, MatchAlgorithmReference}
import javax.persistence.criteria._
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference
import org.springframework.data.jpa.domain.Specification

abstract class AbstractQueryService extends AbstractService {

  def getSupportedSortReferences: java.util.Set[_ <: ComponentReference] = { new java.util.HashSet[ComponentReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  def createAttributeReference(name: String,
                               path: String): StateAdjustingComponentReference[Seq[Specification[ValueSet]]] = {
    val stateUpdater = new StateUpdater[Seq[Specification[ValueSet]]]() {
      def updateState(currentState: Seq[Specification[ValueSet]],
                      matchAlgorithm: MatchAlgorithmReference,
                      queryString: String): Seq[Specification[ValueSet]] = {
        val specification = new Specification[ValueSet]() {
          def toPredicate(root: Root[ValueSet], query: CriteriaQuery[_], cb: CriteriaBuilder): Predicate = {
            val fn = filterFn(matchAlgorithm)
            fn(root.get(path).asInstanceOf[Path[String]], queryString)
          }
        }
        currentState :+ specification
      }
    }

    val ref = new StateAdjustingComponentReference[Seq[Specification[ValueSet]]](stateUpdater)
    ref.setAttributeReference(name)
    ref
  }

  def createPropertyReference(name: String,
                              uri: String,
                              propertyName: String): StateAdjustingComponentReference[Seq[Specification[ValueSet]]] = {

    val stateUpdater = new StateUpdater[Seq[Specification[ValueSet]]]() {

      def updateState(currentState: Seq[Specification[ValueSet]],
                      matchAlgorithm: MatchAlgorithmReference,
                      queryString: String): Seq[Specification[ValueSet]] = {

        val specification = new Specification[ValueSet]() {
          def toPredicate(root: Root[ValueSet], query: CriteriaQuery[_], cb: CriteriaBuilder): Predicate = {
            val join = root.join("properties", JoinType.INNER)
            val fn = filterFn(matchAlgorithm)
            val pred1 = fn(join.get("value").asInstanceOf[Path[String]], queryString)
            val pred2 = cb.equal(join.get("name").asInstanceOf[Path[String]], propertyName)
            ProfileUtils.and(cb, pred1, pred2)
          }
        }
        currentState :+ specification
      }
    }

    val ref = new StateAdjustingComponentReference[Seq[Specification[ValueSet]]](stateUpdater)
    ref.setPropertyReference(new URIAndEntityName)
    ref.getPropertyReference.setName(name)
    ref.getPropertyReference.setUri(name)

    ref
  }

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
