package edu.mayo.cts2.framework.plugin.service.mat.profile

import edu.mayo.cts2.framework.filter.`match`.StateAdjustingPropertyReference
import org.springframework.data.jpa.domain.Specification
import edu.mayo.cts2.framework.plugin.service.mat.model.ValueSet
import edu.mayo.cts2.framework.filter.`match`.StateAdjustingPropertyReference.StateUpdater
import edu.mayo.cts2.framework.model.core.{PredicateReference, PropertyReference, URIAndEntityName, MatchAlgorithmReference}
import javax.persistence.criteria._
import edu.mayo.cts2.framework.model.core.types.TargetReferenceType
import edu.mayo.cts2.framework.service.meta.StandardMatchAlgorithmReference

abstract class AbstractQueryService extends AbstractService {

  def getSupportedSortReferences: java.util.Set[_ <: PropertyReference] = { new java.util.HashSet[PropertyReference]() }

  def getKnownProperties: java.util.Set[PredicateReference] = { new java.util.HashSet[PredicateReference]() }

  def createAttributeReference(name: String,
                               uri: String,
                               path: String): StateAdjustingPropertyReference[Seq[Specification[ValueSet]]] = {
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

    val ref = new StateAdjustingPropertyReference[Seq[Specification[ValueSet]]](stateUpdater)
    ref.setReferenceType(TargetReferenceType.ATTRIBUTE);
    ref.setReferenceTarget(new URIAndEntityName())
    ref.getReferenceTarget.setName(name)
    ref.getReferenceTarget.setUri(uri)

    ref
  }

  def createPropertyReference(name: String,
                              uri: String,
                              propertyName: String): StateAdjustingPropertyReference[Seq[Specification[ValueSet]]] = {

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

    val ref = new StateAdjustingPropertyReference[Seq[Specification[ValueSet]]](stateUpdater)
    ref.setReferenceType(TargetReferenceType.PROPERTY)
    ref.setReferenceTarget(new URIAndEntityName())
    ref.getReferenceTarget.setName(name)
    ref.getReferenceTarget.setUri(uri)

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
