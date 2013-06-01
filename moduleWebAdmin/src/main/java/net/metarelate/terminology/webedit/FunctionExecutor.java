package net.metarelate.terminology.webedit;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;

public abstract class FunctionExecutor implements Serializable{

	//public abstract void execute();

	public abstract void execute(AjaxRequestTarget target);

}
