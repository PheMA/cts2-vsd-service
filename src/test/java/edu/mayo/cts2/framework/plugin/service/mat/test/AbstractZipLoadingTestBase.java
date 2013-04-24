package edu.mayo.cts2.framework.plugin.service.mat.test;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipFile;

import javax.annotation.Resource;

import org.junit.Before;

import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader;
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository;

public abstract class AbstractZipLoadingTestBase extends AbstractTestBase {

	protected String zipFilePath = "src/test/resources/exampleMatZips/test.zip";
	protected String svsXmlFilePath = "src/test/resources/exampleSVS/1.3.6.1.4.1.33895.1.3.0.31.xml";

	@Resource
	public MatZipLoader zipLoader;

	@Resource
	public ValueSetRepository repo;

	@Before
	public void Load() throws Exception {
		ZipFile zip = new ZipFile(new File(zipFilePath));

		zipLoader.loadMatZip(zip);
	}
}