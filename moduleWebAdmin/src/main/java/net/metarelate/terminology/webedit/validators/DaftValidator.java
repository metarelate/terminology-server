package net.metarelate.terminology.webedit.validators;

import java.io.Serializable;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * We sidestep WicketValidator with this object in place of AbstractFormValidator,
 *  as this seems to be too form field specific for what we need!
 * @author andreasplendiani
 *
 */
public interface DaftValidator extends Serializable{
	boolean validate(Model m);
	String getMessage();
}
