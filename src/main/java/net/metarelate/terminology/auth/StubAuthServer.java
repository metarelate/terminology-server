package net.metarelate.terminology.auth;
/**
 * A stub implementation of an AuthServer, with a few demo permissions hard-coded
 * 
 * 
 * @author andreasplendiani
 *
 */
public class StubAuthServer extends AuthServer {

	private boolean containsTriple(String author, String action, String entity) {
		if(author.equals("http://www.sgtp.net/AndreaSplendiani") && action.equals(AuthConfig.allURI) && entity.equals("http://127.0.0.1:8888/MetOffice2/global/common/c-6")) return true;
		if(author.equals("http://metoffice.gov.uk/wmo/") && action.equals(AuthConfig.allURI) && entity.equals(AuthConfig.allURI)) return true;
		return false;
	}

	@Override
	public boolean contains(String agent, String action, String entity) {
		return containsTriple(agent,action,AuthConfig.allURI) ||
				containsTriple(agent,action,AuthConfig.allURI)	||
				containsTriple(agent,AuthConfig.allURI,entity)	||
				containsTriple(agent,AuthConfig.allURI,AuthConfig.allURI)	||
				containsTriple(AuthConfig.allURI,action,entity)	||
				containsTriple(AuthConfig.allURI,action,AuthConfig.allURI)	||
				containsTriple(AuthConfig.allURI,AuthConfig.allURI,entity)	||
				containsTriple(AuthConfig.allURI,AuthConfig.allURI,AuthConfig.allURI);
	
	}

}
