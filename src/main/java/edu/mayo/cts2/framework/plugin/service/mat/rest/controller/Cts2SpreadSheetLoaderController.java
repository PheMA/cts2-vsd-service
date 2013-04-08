package edu.mayo.cts2.framework.plugin.service.mat.rest.controller;

import scala.collection.Iterator;
import scala.collection.JavaConversions.*;
import edu.mayo.cts2.framework.plugin.service.mat.loader.Cts2SpreadSheetLoader;
import edu.mayo.cts2.framework.webapp.rest.extensions.controller.ControllerProvider;
import org.osgi.service.http.HttpService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLEncoder;
import java.util.UUID;

@Controller("cts2SpreadSheetLoaderController")
public class Cts2SpreadSheetLoaderController implements ControllerProvider, InitializingBean {

	@Resource
	private Cts2SpreadSheetLoader loader;

	@Autowired(required = false)
	private HttpService httpService;

	@Override
	public Object getController() {
		return this;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.httpService != null) {
			httpService.registerResources("/upload", "/WEB-INF", null);
		}
	}

	@RequestMapping(value="/upload/cts2spreadsheet", method= RequestMethod.POST)
	public void loadSpreadSheet(HttpServletRequest request, HttpServletResponse response) throws Exception {

		MultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());

		if (!multipartResolver.isMultipart(request))
			throw new IllegalStateException("ServletRequest expected to be of type MultipartHttpServletRequest");

		MultipartHttpServletRequest multipartRequest = multipartResolver.resolveMultipart(request);
		MultipartFile multipartFile = multipartRequest.getFile("file");
		final File ss = File.createTempFile(UUID.randomUUID().toString(), ".xls");
		multipartFile.transferTo(ss);

		String result = loader.loadSpreadSheet(ss);

		response.getWriter().write(result);
		response.setStatus(200);
	}
}
