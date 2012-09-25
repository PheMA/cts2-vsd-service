package edu.mayo.cts2.framework.plugin.service.mat.loader

import edu.mayo.cts2.framework.plugin.service.mat.test.AbstractTestBase
import org.junit.Test
import org.springframework.transaction.annotation.Transactional
import java.util.zip.ZipFile
import javax.annotation.Resource
import static org.junit.Assert.*

class MatZipLoaderUTSTestIT extends AbstractTestBase {
	@Resource
	def MatZipLoaderUTS loaderUts
	
	
	
	
	@Test
	@Transactional
	void getDescriptionFromUTS(){

		def description = loaderUts.getDescriptionFromUTS("CPT", "99281")

			println description
			assertTrue description.length() > 1

	}

}
