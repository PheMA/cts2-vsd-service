package edu.mayo.cts2.framework.plugin.service.mat.umls.dao;

import scala.collection.JavaConversions._
import org.springframework.stereotype.Component
import gov.nih.nlm.umls.uts.webservice.UtsWsSecurityControllerImplService
import gov.nih.nlm.umls.uts.webservice.UtsWsContentControllerImplService
import gov.nih.nlm.umls.uts.webservice.Psf
import java.util.Date
import org.springframework.beans.factory.annotation.Value

@Component
class UtsDao {

  val EIGHT_HOURS = 1000 * 60 * 60 * 8;

  @scala.reflect.BeanProperty
  @Value("${utsUsername}")
  var username:String = _

  @scala.reflect.BeanProperty
  @Value("${utsPassword}")
  var password:String = _

  @scala.reflect.BeanProperty
  @Value("${utsUmlsRelease}")
  var umlsRelease:String = _

  @scala.reflect.BeanProperty
  @Value("${utsServiceName}")
  var serviceName:String = _

  private val securityService = (new UtsWsSecurityControllerImplService()).getUtsWsSecurityControllerImplPort();
  val utsContentService = (new UtsWsContentControllerImplService()).getUtsWsContentControllerImplPort();

  private var ticketGrantingTicket: String = _
  private var ticketCreationDate: Date = _

  def getSecurityTicket(): String = {

    val eightHoursAgo = new Date(new Date().getTime() - EIGHT_HOURS);

    if (ticketCreationDate == null || ticketCreationDate.before(eightHoursAgo)) {
      ticketCreationDate = new Date()
      ticketGrantingTicket = securityService.getProxyGrantTicket(username, password);
    }

    securityService.getProxyTicket(ticketGrantingTicket, serviceName);
  }

  private def callSecurely[R](fn: (String, String) => R): R = {
    fn(getSecurityTicket, umlsRelease)
  }
}
