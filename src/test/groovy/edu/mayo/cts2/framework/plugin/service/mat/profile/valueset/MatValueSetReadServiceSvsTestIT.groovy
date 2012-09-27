package edu.mayo.cts2.framework.plugin.service.mat.profile.valueset

import static org.junit.Assert.*

import javax.annotation.Resource
import javax.xml.transform.stream.StreamResult

import org.junit.Test

import edu.mayo.cts2.framework.core.xml.DelegatingMarshaller
import edu.mayo.cts2.framework.model.util.ModelUtils
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase
import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractZipLoadingTestBase.SVS_OR_ZIP
import edu.mayo.cts2.framework.service.profile.valueset.ValueSetReadService

class MatValueSetReadServiceSvsTestIT extends AbstractZipLoadingTestBase {

	@Resource
	def ValueSetReadService service
	
	def marshaller = new DelegatingMarshaller()
	
	public MatValueSetReadServiceSvsTestIT(){
		super()
		svsOrZip = SVS_OR_ZIP.SVS
	}

	@Test
	void TestSetUp() {
		assertNotNull service
	}	
	
	@Test
	void TestRead() {
		assertNotNull service.read(ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"), null)
	}
	
	@Test
	void TestReadValidXml() {	
		def result = service.read(ModelUtils.nameOrUriFromName("1.3.6.1.4.1.33895.1.3.0.31"), null)
		
		assertNotNull result
		
		marshaller.marshal(result, new StreamResult(new StringWriter()))
	}
}
