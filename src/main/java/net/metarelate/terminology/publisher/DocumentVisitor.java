package net.metarelate.terminology.publisher;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.metarelate.terminology.coreModel.TerminologyIndividual;
import net.metarelate.terminology.coreModel.TerminologySet;
import net.metarelate.terminology.exceptions.ConfigurationException;
import net.metarelate.terminology.exceptions.ModelException;
import net.metarelate.terminology.exceptions.WebWriterException;
import net.metarelate.terminology.instanceManager.Initializer;

public class DocumentVisitor extends PublisherVisitor {
	Initializer myInitializer=null;
	TemplateManager tm=null;
	StringBuilder myDoc=null;
	String language=null;
	String tag=null;
	private int level=0;
	
	public DocumentVisitor(Initializer myInitializer, TemplateManager myTemplate) {
		super();
		this.myInitializer = myInitializer;
		this.tm = myTemplate;
		myDoc=new StringBuilder();
	}

	public void bind(String tag, String language) throws ConfigurationException, ModelException, WebWriterException, IOException {
		this.language=language;
		this.tag=tag;
		myDoc.append(tm.getIntroForLang(language, tag,myInitializer.myFactory));
		TerminologySet[] roots=myInitializer.myFactory.getRootCollections(); //TODO note, we need some sorting here!
		Set<TerminologySet> rootsSet= new HashSet<TerminologySet>();		//TODO we should homogenize collections and have only Sets returned, where that's the semantics (no repetitions, no guaranteed order)
		for(TerminologySet root:roots) rootsSet.add(root);
		crawl(rootsSet,tag,language);
		myDoc.append(tm.getClosingForLang(language, tag,myInitializer.myFactory));
		
	}

	public void crawl(Set<TerminologySet> currentSent, String tag, String language) throws ConfigurationException, ModelException, WebWriterException, IOException {
		for(TerminologySet set:currentSent) {
			String[] versions=set.getVersionsForTag(tag);
			if(versions!=null && versions.length>0) {	//TODO to check. If a node is not tagged, we don't go through the subtree. Seems plausible.
				set.accept(this);
				level+=1;
				Set<TerminologySet> childrenToConsider=new HashSet<TerminologySet>();
				for(String version:versions) {
					childrenToConsider.addAll(set.getCollections(version));
				}
				crawl(childrenToConsider,tag,language);
				level-=1;
			}
			
			
			
		}
	}
	
	@Override
	public void visit(TerminologySet set) throws WebWriterException,
			IOException, ConfigurationException, ModelException {
			String[] versions=set.getVersionsForTag(tag);
			for(String version:versions) {
				myDoc.append(tm.getPageForLang(language, set, version, level,"",myInitializer.myCache,myInitializer.myFactory.getLabelManager(),myInitializer.myFactory.getBackgroundKnowledgeManager(), "")); //TODO some of these argumnents (baseURL, cache) don't matter for docs. This should be implied by design.
			}
	}

	@Override
	public void visit(TerminologyIndividual ind) throws WebWriterException,
			IOException, ConfigurationException, ModelException {
		String[] versions=ind.getVersionsForTag(tag);
		for(String version:versions) {
			myDoc.append(tm.getPageForLang(language, ind, version, level,"",myInitializer.myCache,myInitializer.myFactory.getLabelManager(),myInitializer.myFactory.getBackgroundKnowledgeManager(), "")); //TODO some of these argumnents (baseURL, cache) don't matter for docs. This should be implied by design.
		}

	}

	public void writeToFile(String fileName) throws IOException {
		FileWriter fWriter = new FileWriter(fileName);
		BufferedWriter fBWriter = new BufferedWriter(fWriter);
		fBWriter.write(myDoc.toString());
		fBWriter.close();
		
	}

}
