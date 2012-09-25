package edu.mayo.cts2.framework.plugin.service.mat.loader

import org.springframework.stereotype.Component
import javax.annotation.Resource
import edu.mayo.cts2.framework.plugin.service.mat.umls.dao.UtsDao
import gov.nih.nlm.umls.uts.webservice.Psf

@Component
class MatZipLoaderUTS {

  @Resource
  var utsDao: UtsDao = _

  def getDescriptionFromUTS(codeSystem: String, code: String): String = {

    val csv = codeSystem
    val fn = utsDao.utsContentService.getCodeAtoms _

    val atoms = utsDao.callSecurely(fn(_, _, code, codeSystem, new Psf()))

    if (atoms.size > 0) {
      atoms.get(0).getTermString().getDefaultPreferredName()
    } else {
      null
    }
  }

}