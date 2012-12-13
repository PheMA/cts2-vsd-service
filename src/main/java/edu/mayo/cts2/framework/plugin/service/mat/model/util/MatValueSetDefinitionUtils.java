package edu.mayo.cts2.framework.plugin.service.mat.model.util;

import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinition;
import edu.mayo.cts2.framework.model.valuesetdefinition.ValueSetDefinitionEntry;
import edu.mayo.cts2.framework.plugin.service.mat.model.MatValueSetDefinition;
import edu.mayo.cts2.framework.plugin.service.mat.model.MatValueSetDefinitionEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MatValueSetDefinitionUtils {

	public static ValueSetDefinition transformToValueSetDefinition(MatValueSetDefinition matValueSetDefinition) {
		ValueSetDefinition vsd = new ValueSetDefinition();
		vsd.setDocumentURI(matValueSetDefinition.getDocumentURI());
		vsd.setDefinedValueSet(matValueSetDefinition.getDefinedValueSet());
		vsd.setVersionTag(matValueSetDefinition.getVerisonTag());
//    vsd.setEntry(MatValueSetDescriptionUtils.transformToValueSetDefinitionEntry(matValueSetDefinition.getEntries))
		return vsd;
	}

	public static List<ValueSetDefinitionEntry> transformToValueSetDefinitionEntries(MatValueSetDefinitionEntry[] matEntries) {
		return transformToValueSetDefinitionEntries(Arrays.asList(matEntries));
	}

	public static List<ValueSetDefinitionEntry> transformToValueSetDefinitionEntries(List<MatValueSetDefinitionEntry> matEntries) {
		List<ValueSetDefinitionEntry> entries = new ArrayList<ValueSetDefinitionEntry>(matEntries.size());
		for (MatValueSetDefinitionEntry matEntry : matEntries) {
			entries.add(transformToValueSetDefinitionEntry(matEntry));
		}
		return entries;
	}

	public static ValueSetDefinitionEntry transformToValueSetDefinitionEntry(MatValueSetDefinitionEntry matEntry) {
		ValueSetDefinitionEntry entry = new ValueSetDefinitionEntry();
		entry.setOperator(matEntry.getOperator());
//		entry.setEntityList(matEntry.referencedEntities());
		return entry;
	}

	public static MatValueSetDefinition transformToMatValueSetDefinition(ValueSetDefinition valueSetDefinition) {
		MatValueSetDefinition matValueSetDefinition = new MatValueSetDefinition();
		matValueSetDefinition.setDocumentURI(valueSetDefinition.getDocumentURI());
		matValueSetDefinition.setDefinedValueSet(valueSetDefinition.getDefinedValueSet());
		matValueSetDefinition.setEntries(transformToMatValueSetDefinitionEntries(valueSetDefinition.getEntry()));
		matValueSetDefinition.setVerisonTag(valueSetDefinition.getVersionTag());
		matValueSetDefinition.setChangeableElementGroup(valueSetDefinition.getChangeableElementGroup());
		return matValueSetDefinition;
	}

	public static List<MatValueSetDefinitionEntry> transformToMatValueSetDefinitionEntries(ValueSetDefinitionEntry[] entries) {
		return transformToMatValueSetDefinitionEntries(Arrays.asList(entries));
	}

	public static List<MatValueSetDefinitionEntry> transformToMatValueSetDefinitionEntries(List<ValueSetDefinitionEntry> entries) {
		List<MatValueSetDefinitionEntry> matEntries = new ArrayList<MatValueSetDefinitionEntry>(entries.size());
		for (ValueSetDefinitionEntry entry : entries) {
			matEntries.add(transformMatToValueSetDefinitionEntry(entry));
		}
		return matEntries;
	}



	public static MatValueSetDefinitionEntry transformMatToValueSetDefinitionEntry(ValueSetDefinitionEntry entry) {
		MatValueSetDefinitionEntry matEntry = new MatValueSetDefinitionEntry();
		matEntry.setOperator(entry.getOperator());
		return matEntry;
	}

}
