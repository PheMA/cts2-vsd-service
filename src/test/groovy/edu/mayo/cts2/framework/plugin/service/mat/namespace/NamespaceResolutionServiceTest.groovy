package edu.mayo.cts2.framework.plugin.service.mat.namespace

import org.junit.Ignore

import static org.junit.Assert.*

import org.junit.Test

import clojure.lang.RT


public class NamespaceResolutionServiceTest {
	
	@Test
    @Ignore
	void TestGetUri() {
		def svc = new NamespaceResolutionService()
		svc.namespaceServiceUrl = "http://informatics.mayo.edu/cts2/services/bioportal-rdf"
		
		assertEquals "http://schema.omg.org/spec/CTS2/1.0/", svc.prefixToUri("cts2").get()
	}
	
	@Test
    @Ignore
	void TestGetPrefix() {
		def svc = new NamespaceResolutionService()
		svc.namespaceServiceUrl = "http://informatics.mayo.edu/cts2/services/bioportal-rdf"
		
		assertEquals "cts2", svc.uriToPrefix("http://schema.omg.org/spec/CTS2/1.0/")
	}
	
}
