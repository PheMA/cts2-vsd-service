package edu.mayo.cts2.framework.plugin.service.mat.model.util;

import edu.mayo.cts2.framework.model.core.SourceReference;

public class MatModelUtils {

	public static SourceReference createSourceReferece(String content) {
		SourceReference sr = new SourceReference();
		sr.setContent(content);
		return sr;
	}

}
