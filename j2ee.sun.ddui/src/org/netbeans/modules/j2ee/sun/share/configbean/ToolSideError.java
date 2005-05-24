/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.deploy.model.DDBean;
/**
 *
 * @author  vkraemer
 */
public class ToolSideError extends BaseRoot {

	/** Holds value of property message. */
	private String message;
	
	/** Creates a new instance of ToolSideError */
	public ToolSideError(String message) { // DDBean ddbean, Base parent) {
		this.message = message;
	}

	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		return snippets;
	}

	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		return false;
	}
	
	/** Getter for property message.
	 * @return Value of property message.
	 *
	 */
	public String getMessage() {
		return message; // "there is a tool side error"; //this.message;
	}
	
	/** Getter for property ddbeanXpath.
	 * @return Value of property ddbeanXpath.
	 *
	 */
	public String getDdbeanXpath() {
		return getDDBean().getXpath(); // this.ddbeanXpath;
	}
	
	/** Getter for property ddbeanText.
	 * @return Value of property ddbeanText.
	 *
	 */
	public String getDdbeanText() {
		return getDDBean().getText(); // this.ddbeanText;
	}

        public String getHelpId() {
            return "AS_CFG_ToolSideError";                              //NOI18N
        }
}
