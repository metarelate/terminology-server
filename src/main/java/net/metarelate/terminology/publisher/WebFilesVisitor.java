package net.metarelate.terminology.publisher;

import java.io.File;

import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;

public class WebFilesVisitor implements TerminologyVisitor {
	File templateDir=null;
	public String visit(TerminologySet set) {
		String uri=set.getURI();
		String[] versions=set.getVersions();
		// TODO Auto-generated method stub
		return null;
	}

	public String visit(TerminologyIndividual ind) {
		// TODO Auto-generated method stub
		return null;
	}

}
