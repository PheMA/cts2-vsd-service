package edu.mayo.cts2.framework.plugin.service.mat.rest.controller;

import java.io.File;
import java.util.UUID;
import java.util.zip.ZipFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader;

@Controller
public class ZipUploadController {
	
	@Resource
	private MatZipLoader matZipLoader;

	@RequestMapping(value = "/executions", method = RequestMethod.POST)
	public String createExceuction(HttpServletRequest request)
			throws Exception {

		if (!(request instanceof MultipartHttpServletRequest)) {
			throw new IllegalStateException(
					"ServletRequest expected to be of type MultipartHttpServletRequest");
		}

		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

		MultipartFile multipartFile = multipartRequest.getFile("file");

		final File zip = File.createTempFile(UUID.randomUUID().toString(),
				".zip");

		multipartFile.transferTo(zip);

		ZipFile zipFile = new ZipFile(zip);

		matZipLoader.loadMatZip(zipFile);
		
		return "loaded";
	}
}