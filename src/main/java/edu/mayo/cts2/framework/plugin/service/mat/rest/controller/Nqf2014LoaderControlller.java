package edu.mayo.cts2.framework.plugin.service.mat.rest.controller;

import edu.mayo.cts2.framework.webapp.rest.extensions.controller.ControllerProvider;
import edu.mayo.cts2.framework.plugin.service.mat.loader.Nqf2014Loader;
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
import java.util.UUID;

@Controller("nqf2014LoaderController")
public class Nqf2014LoaderControlller implements ControllerProvider {

	@Resource
	private Nqf2014Loader loader;

	@Override
	public Object getController() {
		return this;
	}

	@RequestMapping(value="/upload/nqf2014", method= RequestMethod.POST)
	public void loadNqf2014SpreadSheet(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
