/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

    protected ConfigParser getParser() {
        return null;
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
    
    public String generateDocType(ASDDVersion version) {
        return "";
    }

    public String getHelpId() {
        return "AS_CFG_ToolSideError";                              //NOI18N
    }

}
