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

package net.metarelate.terminology.coreModel;

import com.hp.hpl.jena.rdf.model.Model;
/**
 * This class handles "background knowledge". This is intended as information that can
 * be used by the TerminologyServer, but it is not part of its content.
 * One example of usage of this class would be to store (and query) configuration options, or for instance the URLs
 * where a term representation is published.
 * Labels, which could be rightfully handled via this class, are instead handled via a specific class {@link LabelManager}, that
 * can provide more specialized methods (e.g. labels by language, list of supported languages...).
 * The remit of the {@link BackgroundKnowledgeManager} is very broad, and as such, its functionalities are very basic.
 * In the current implementation, it only returns a Jena Model that hold the set of statements that constitute
 * of background knowledge.
 * More specialized methods (add/set/inspect) would be possible, but they bring scarce utility in the current implementation.
 * TODO In the current version of the prototype, this class in not in use (though it should be once the publishing layer is re-factored)
 * 
 * @author andreasplendiani
 *
 */
public abstract class BackgroundKnowledgeManager {
	// TODO we may want to abstract more the level here
	public abstract Model getModel();
}
