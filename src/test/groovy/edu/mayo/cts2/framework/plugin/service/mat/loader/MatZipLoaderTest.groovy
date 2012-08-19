package edu.mayo.cts2.framework.plugin.service.mat.loader

import static org.junit.Assert.*

import org.junit.Test

class MatZipLoaderTest {

	@Test
	void TestValueSetFormalNameToName() {
		def loader = new MatZipLoader()
		
		def name = loader.valueSetFormalNameToName("American Medical Association - Physician Consortium for Performance Improvement")
	
		assertEquals "AMA-PCFPI", name	
	}
	
}
