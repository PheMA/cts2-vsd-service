package edu.mayo.cts2.framework.plugin.service.mat.umls.dao

import static org.junit.Assert.*

import javax.annotation.Resource

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import scala.Function2

@RunWith(SpringJUnit4ClassRunner)
@ContextConfiguration("/test-mat-context.xml")
class UtsDaoTestIT {

	@Resource
	def UtsDao dao

	@Test
	void TestSetUp() {
		assertNotNull dao
	}

	@Test
	void TestGetCode() {
		assertNotNull dao.callSecurely( 
			{ ticket, version -> 
				dao.utsContentService.getCode(ticket, version, "C12727", "NCI") 
			} as Function2 )
	}

}
