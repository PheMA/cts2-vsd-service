package edu.mayo.cts2.framework.plugin.service.mat.loader

import static org.junit.Assert.*

import java.util.zip.ZipFile

import org.junit.Test

import scala.Function1


class MatZipLoaderUtilsTest {

	@Test
	void TestDoWithCombinedMatZip() {
		def zip = new ZipFile(new File("src/test/resources/exampleMatZips/combined.zip"))
		
		MatZipLoaderUtils.doWithCombinedMatZip(zip, 
			{ result ->  
				assertNotNull result
				assertNotNull result.xml
				assertNotNull result.spreadSheet
			} as Function1)
	}
	
}
