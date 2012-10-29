package edu.mayo.cts2.framework.plugin.service.mat.profile;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specifications;

public final class ProfileUtils {

	private ProfileUtils() {
		super();
	}

	public static <T> CriteriaQuery<T> addSpecifications(
			Specifications<T> specifications, 
			Root<T> root,
			CriteriaQuery<T> query, 
			CriteriaBuilder builder) {
		return query.where(specifications.toPredicate(root, query, builder));
	}
	

	public static Predicate and(CriteriaBuilder builder, Predicate... predicates) {
		return builder.and(predicates);
	}

}
