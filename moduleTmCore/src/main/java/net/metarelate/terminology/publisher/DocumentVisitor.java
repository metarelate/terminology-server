/* 
 (C) British Crown Copyright 2011 - 2013, Met Office

 This file is part of terminology-server.

 terminology-server is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 terminology-server is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with terminology-server. If not, see <http://www.gnu.org/licenses/>.
*/

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
import net.metarelate.terminology.utils.Loggers;

public class DocumentVisitor extends PublisherVisitor {
	Initializer myInitializer=null;
	TemplateManager tm=null;
	StringBuilder myDoc=null;
	String language=null;
	String tag=null;
	private int level=0;
	
	/**
	 * Constructor
	 * @param myInitializer
	 * @param myTemplate
	 */
	public DocumentVisitor(Initializer myInitializer, TemplateManager myTemplate) {
		super();
		this.myInitializer = myInitializer;
		this.tm = myTemplate;
		myDoc=new StringBuilder();
	}

	/**
	 * pack visits the terminology and pack results of the visit together, adding pre and post templates
	 * @param tag the tag that identifies the release to be exported
	 * @param language the desired language for the export
	 * @throws ConfigurationException
	 * @throws ModelException
	 * @throws WebWriterException
	 * @throws IOException
	 */
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

	
	void crawl(Set<TerminologySet> currentSent, String tag, String language) throws ConfigurationException, ModelException, WebWriterException, IOException {
		Loggers.publishLogger.trace("Doc visitor crwaling at level: "+level);
		for(TerminologySet set:currentSent) {
			Loggers.publishLogger.trace("Set: "+set.getURI());
			String[] versions=set.getVersionsForTag(tag);
			if(versions!=null && versions.length>0) {	//TODO to check. If a node is not tagged, we don't go through the subtree. Seems plausible.
				Loggers.publishLogger.trace("Tag test passed");
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
				Loggers.publishLogger.trace("Calling rendering for "+set.getURI()+" v. "+version);
				myDoc.append(tm.getPageForLang(language, set, version, level,"",myInitializer.myCache,myInitializer.myFactory.getLabelManager(),myInitializer.myFactory.getBackgroundKnowledgeManager(), "",tag)); //TODO some of these argumnents (baseURL, cache) don't matter for docs. This should be implied by design.
			}
	}

	@Override
	public void visit(TerminologyIndividual ind) throws WebWriterException,
			IOException, ConfigurationException, ModelException {
		String[] versions=ind.getVersionsForTag(tag);
		for(String version:versions) {
			myDoc.append(tm.getPageForLang(language, ind, version, level,"",myInitializer.myCache,myInitializer.myFactory.getLabelManager(),myInitializer.myFactory.getBackgroundKnowledgeManager(), "",tag)); //TODO some of these argumnents (baseURL, cache) don't matter for docs. This should be implied by design.
		}

	}

	/**
	 * Outputs the overall representation of the terminology as a file
	 * @param fileName the absolute path of the file where to write to (including extensions)
	 * @throws IOException
	 */
	public void writeToFile(String fileName) throws IOException {
		FileWriter fWriter = new FileWriter(fileName);
		BufferedWriter fBWriter = new BufferedWriter(fWriter);
		fBWriter.write(myDoc.toString());
		fBWriter.close();
		
	}

}
