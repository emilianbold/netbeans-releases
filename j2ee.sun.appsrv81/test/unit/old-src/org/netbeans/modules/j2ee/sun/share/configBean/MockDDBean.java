/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
