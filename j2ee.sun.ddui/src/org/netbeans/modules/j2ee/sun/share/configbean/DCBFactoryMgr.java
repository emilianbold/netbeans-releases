/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
	
	protected final ResourceBundle bundle = ResourceBundle.getBundle(
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
//		} else if(!xpath.startsWith("/")) {	// NOI18N
//			result = xpath;
		} else {
			result = xpath;
		}
		
		return result;
	}
}
