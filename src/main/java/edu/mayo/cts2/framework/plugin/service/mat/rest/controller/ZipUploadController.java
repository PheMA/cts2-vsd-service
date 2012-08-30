package edu.mayo.cts2.framework.plugin.service.mat.rest.controller;

import java.io.File;
import java.util.UUID;
import java.util.zip.ZipFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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

import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader;
import edu.mayo.cts2.framework.webapp.rest.extensions.controller.ControllerProvider;

@Controller("zipUploadControllerProvider")
public class ZipUploadController implements ControllerProvider, InitializingBean {
	
	@Resource
	private MatZipLoader matZipLoader;
	
	@Autowired(required=false)
	private HttpService httpService;

	@Override
	public void afterPropertiesSet() throws Exception {
	
		if(this.httpService != null){
			httpService.registerResources(
					"/mat",
					"/WEB-INF", 
					null);
		}
	}

	@RequestMapping(value = "/mat/zips", method = RequestMethod.POST)
	public String loadZip(HttpServletRequest request)
			throws Exception {

		MultipartResolver multipartResolver = 
			new CommonsMultipartResolver(request.getSession().getServletContext());
		
		if (! multipartResolver.isMultipart(request)) {
			throw new IllegalStateException(
					"ServletRequest expected to be of type MultipartHttpServletRequest");
		}

		MultipartHttpServletRequest multipartRequest = multipartResolver.resolveMultipart(request);

		MultipartFile multipartFile = multipartRequest.getFile("file");

		final File zip = File.createTempFile(UUID.randomUUID().toString(),
				".zip");

		multipartFile.transferTo(zip);

		ZipFile zipFile = new ZipFile(zip);

		matZipLoader.loadMatZip(zipFile);
		
		return "uploadComplete";
	}

	@Override
	public Object getController() {
		return this;
	}
}