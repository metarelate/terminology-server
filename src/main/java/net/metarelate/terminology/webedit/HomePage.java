package net.metarelate.terminology.webedit;

import net.metarelate.terminology.coreModel.TerminologySet;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

	public HomePage(final PageParameters parameters) {
		super(parameters);
		add(new Footer("ft"));
		
		String rootsString="";
		TerminologySet[] termRoots=CommandWebConsole.myFactory.getRootCollections();
		for(int i=0;i<termRoots.length;i++) {
			rootsString+=termRoots[i].getLabel(termRoots[i].getDefaultVersion())+"\n";
		}
		add(new MultiLineLabel("body2",rootsString));

		// TODO Add your page's components here

    }
}
