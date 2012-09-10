package edu.mayo.cts2.framework.plugin.service.mat.test;

import java.io.File;
import java.util.zip.ZipFile;

import javax.annotation.Resource;

import org.junit.Before;

import edu.mayo.cts2.framework.plugin.service.mat.loader.MatZipLoader;
import edu.mayo.cts2.framework.plugin.service.mat.repository.ValueSetRepository;

public abstract class AbstractZipLoadingTestBase extends AbstractTestBase {

	@Resource
	private MatZipLoader loader;
	
	@Resource
	private ValueSetRepository repo;

	@Before
	public void Load() throws Exception {
		ZipFile zip = 
			new ZipFile(new File("src/test/resources/exampleMatZips/test.zip"));
		
		loader.loadMatZip(zip);
	}
}