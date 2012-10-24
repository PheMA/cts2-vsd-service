package edu.mayo.cts2.framework.plugin.service.mat.loader

import static org.junit.Assert.*
import ihe.iti.svs._2008.RetrieveMultipleValueSetsResponseType

import java.util.zip.ZipFile

import javax.annotation.Resource
import javax.xml.bind.JAXBContext

import org.junit.Ignore
import org.junit.Test

import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase

class SvsLoaderTestIT extends AbstractTestBase {

	@Resource
	def SvsLoader loader

	@Resource
	def ValueSetRepository repo

	@Test
	void TestSetUp() {
		assertNotNull loader
	}
	
	@Test
	void TestLoadSVS_Zip() {
		def is = new FileInputStream("src/test/resources/exampleSVS/RetrieveMultipleValueSetResponse.xml")
		def jc = JAXBContext.newInstance(RetrieveMultipleValueSetsResponseType)
		def u = jc.createUnmarshaller();
		def o = u.unmarshal( is );

		assertNotNull o
	}
	
	@Test
	@Ignore
	void asdfasdf() {
		//this is internally protected
		def f = new File("src/test/resources/exampleSVS/svsXml/svsXml.zip")
		
			def zip = new ZipFile(f)
			
			loader.loadSvsZip(zip)
		
	}

	@Test
	void TestUnmarshallSVS() {
		//this is internally protected
		def file = File.createTempFile(UUID.randomUUID().toString(), "zip")
		try {
			def url = 'http://bmidev3/cts2/svsXml.zip'
			file << new URL(url).openStream()
			
			
			def zip = new ZipFile(file)
			
			loader.loadSvsZip(zip)
		} finally {
			file.delete()
		}
	}
}
