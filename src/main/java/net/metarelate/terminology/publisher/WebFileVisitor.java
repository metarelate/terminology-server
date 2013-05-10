package net.metarelate.terminology.publisher;

import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;

public class WebFileVisitor extends PublisherVisitor {

	public WebFileVisitor(TemplateManager myTm) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void crawl(TerminologySet root) {
		//TODO
		//root.accept(this);
		//root.getIndividuals;
		//for all individuals, ind.accept(this);
		//root.getChildrend();
		//for all children, child.accept(this);
	}

	@Override
	public void visit(TerminologySet set) {
		// TODO
		//print files for set (get text from template)
		//get versions. Print files for versioned set (get text from template)
		
		// TODO Auto-generated method stub

	}

	@Override
	public void visit(TerminologyIndividual ind) {
		// TODO Auto-generated method stub
		//print files for code (get text from template)
		//get versions. Print files for versioned ind (get text from template)
	}

}
