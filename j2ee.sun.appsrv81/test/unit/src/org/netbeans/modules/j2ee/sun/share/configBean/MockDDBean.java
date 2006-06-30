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

import java.util.Map;
import java.util.HashMap;
/**
 *
 * @author  vkraemer
 */
public class MockDDBean implements javax.enterprise.deploy.model.DDBean {
	
	private String xpath = null;
	/** Creates a new instance of MockDDBean */

    private static int instanceCounter = 0;
    private int instanceVal = 0;
	public MockDDBean() {
        instanceVal = instanceCounter++;
	}
	
	public void addXpathListener(String xpath, javax.enterprise.deploy.model.XpathListener xpl) {
	}
	
	public String[] getAttributeNames() {
		return new String[0];
	}
	
	public String getAttributeValue(String attrName) {
		return null;
	}
	
	public javax.enterprise.deploy.model.DDBean[] getChildBean(String xpath) {
        javax.enterprise.deploy.model.DDBean[] retVal =        
            new javax.enterprise.deploy.model.DDBean[1];
        MockDDBean mock = new MockDDBean();
        mock.setXpath(xpath);
        retVal[0] = mock;        
        return retVal;
	}
	
	public String getId() {
		return null;
	}
    
    javax.enterprise.deploy.model.DDBeanRoot ddbr;
    
    public void setRoot(javax.enterprise.deploy.model.DDBeanRoot ddbr) {
        this.ddbr = ddbr;
    }
	
	public javax.enterprise.deploy.model.DDBeanRoot getRoot() {
		return ddbr;
	}
	
	public String getText() {
		return "mockDDBean_"+instanceVal+"_textVal";
	}
	
/*	public String[] getText(String xpath) {
		return new String[0];
	}
*/	
	public void setXpath(String xpath) {
		this.xpath = xpath;
	}
	
	public String getXpath() {
		return xpath;
	}
	
	private Map pairs  = new HashMap();
	
	public void setText(Map pairs) {
		this.pairs = pairs;
	}
	
	public String[] getText(String key) {
		return (String[]) pairs.get(key);
	}
		
	
	public void removeXpathListener(String xpath, javax.enterprise.deploy.model.XpathListener xpl) {
	}
	
}
