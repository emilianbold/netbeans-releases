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
/*
 * Factory.java
 *
 * Created on May 2, 2003, 4:17 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.Map;
import java.util.ResourceBundle;
import java.text.MessageFormat;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;

/**
 *
 * @author Peter Williams
 */
public class DCBFactoryMgr {
	
	protected static final ResourceBundle bundle = ResourceBundle.getBundle(
		"org.netbeans.modules.j2ee.sun.share.configbean.Bundle");	// NOI18N
	
	private Map factoryMap;
	private String beanBaseXpath;
	
	/** Creates a new instance of Factory 
	 */
	public DCBFactoryMgr(Map xPathFactoryMap, String baseXpath) {
		factoryMap = xPathFactoryMap;
		beanBaseXpath = baseXpath;
		
		if(!Utils.hasTrailingSlash(beanBaseXpath)) {
			beanBaseXpath += "/"; // NOI18N
		}
	}
	
	public Base createDCB(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		Base dcbResult = null;
		
		// TODO is bean waiting to be loaded from persistent storage?
		// [can this even go here if we use a generic factory manager?]
		
		// If we can't load it, it's a new bean so we create it from scratch.
		if(dcbResult == null) {
			String ddBeanRelativeXPath = makeRelative(ddBean.getXpath());
			DCBFactory factory = (DCBFactory) factoryMap.get(ddBeanRelativeXPath);
			if(factory != null) {
				dcbResult = factory.createDCB(ddBean, dcbParent);
				if (null == dcbResult) {
					dcbResult = new Error();
					dcbResult.init(ddBean, dcbParent);
				}
			} else {
				Object [] args = new Object[1];
				args[0] = ddBean.getXpath();
				throw Utils.makeCE("ERR_BadFactoryMapping", args, null);	// NOI18N
			}
		}

		return dcbResult;
	}
	
	public String [] getFactoryKeys() {
		return (String []) factoryMap.keySet().toArray(new String[factoryMap.size()]);
	}
	
	/** !PW This method works around an issue with NetBean's implementation of
	 *  toolside JSR-88.  We specify many child beans via relative xpaths in
	 *  DConfigBean.getXpaths().  However, when the studio calls getDConfigBean
	 *  to create those beans, they pass us an absolute xpath.  (This is vague
	 *  in JSR-88 1.0, but was clarified as a violation of JSR-88 1.1.)
	 *
	 *  Anyway, this method allows us at runtime to determine what relative path
	 *  we most likely passed them that caused the call to getDConfigBean now
	 *  being processed.  We need the relative xpath because that is how our
	 *  factories are indexed.
	 */
	private String makeRelative(String xpath) {
		String result;
		
		if(xpath.startsWith(beanBaseXpath)) {
			result = xpath.substring(beanBaseXpath.length());
		} else if(!xpath.startsWith("/")) {	// NOI18N
			result = xpath;
		} else {
			result = xpath;
		}
		
		return result;
	}
}
