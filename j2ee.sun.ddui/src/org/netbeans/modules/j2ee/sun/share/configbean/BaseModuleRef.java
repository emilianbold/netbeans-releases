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
 * BaseModuleRef.java
 *
 * Created on June 27, 2003, 1:32 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.J2eeApplicationObject;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;


/**
 *
 * @author Peter Williams
 */
abstract public class BaseModuleRef extends Base {

	/** -----------------------------------------------------------------------
	 * Initialization
	 */
	
	/** Creates new BaseModuleRef 
	 */
	public BaseModuleRef() {
		setDescriptorElement(bundle.getString("BDN_BaseModuleRef"));	// NOI18N	
	}
	
	/**
	 * @param dDBean
	 * @param parent
	 * @throws ConfigurationException
	 */	
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
		
		// Depending on what actual type of ModuleRef this object is, there are
		// different ways of initializing the URI field.  And we need this to
		// properly patch references, so initialize that field here, then call
		// findRootDCB to handle reference patching correctly.
		//
		initModuleUri(dDBean);
		
		// initialize reference if we can
		findRootDCB(moduleUri);
	}
	
	/** Called from BaseModuleRef.init() to get the correct module URI field
	 *  for the reference object.  Overload it in the derived reference object
	 *  if the module URI field is on a different DDBean (e.g. web modules
	 *  use the child bean "web-uri" to store this information.)
	 */
	protected void initModuleUri(DDBean dDBean) {
		setModuleUri(dDBean);
	}
	
	/** -----------------------------------------------------------------------
	 * Properties
	 */
	
	/** Holds value of property moduleUri. */
	private DDBean moduleUri;
	
	/** Getter for displayName property
	 * @return String suitable for display
	 */
	public String getDisplayName() {
		StringBuffer buf = new StringBuffer(64);
		
		// This adds the name of the particular module, in the case of EARs where
		// there could be multiple sub modules.
		if(moduleUri != null) {
			buf.append(getModuleUri());
		}
		buf.append(" [");
		buf.append(getDescriptorElement());
		buf.append("]");
		
		return buf.toString();
    }
    
    /** Getter for customizer title fragment property
     * @return String fragment for use in customizer title
     *
     */
    public String getTitleFragment() {
        return (J2EEVersion.J2EE_1_4.compareSpecification(getJ2EEModuleVersion()) >= 0) ? 
            bundle.getString("LBL_J2EETitleFragment") : // NOI18N
            bundle.getString("LBL_JavaEETitleFragment"); // NOI18N
    }
	
	/** Getter for property name.
	 * @return Value of property name.
	 *
	 */
	public String getModuleUri() {
		String moduleUriText = null;
		
		if(moduleUri != null) {
			moduleUriText = moduleUri.getText();
		}
		
		if(moduleUriText == null) {
			moduleUriText = "(null)"; // FIXME - needs I18N
		}
		
		return moduleUriText;
	}
	
	protected void setModuleUri(DDBean uriBean) {
		moduleUri = uriBean;
	}
	
	/** Getter for property refIdentity.
	 * @return Value of property refIdentity.
	 *
	 */
	public String getRefIdentity() {
		String result = "(null)"; // FIXME - needs I18N
		if(getReference() != null) {
			result = getReference().getIdentity();
		}
		
		return result;
	}
	
	/** -----------------------------------------------------------------------
	 * Reference support
	 */
	private BaseRoot referencedDCB = null;
	
	protected BaseRoot getReference() {
		return referencedDCB;
	}
	
	protected void setReference(BaseRoot ref) {
		referencedDCB = ref;
	}
	
	protected void findRootDCB(DDBean ddBean) {
		// Locate DDBeanRoot for referenced module via old lookup method
		SunONEDeploymentConfiguration dc = getConfig();
		DDBeanRoot ddbRoot = getDDBeanRoot(ddBean, dc.getDeployableObject());
		
		// Then look in DCBCache for this DDBeanRoot
		if(ddbRoot != null) {
			Base dcb = getDCBInstance(ddbRoot);
			if(dcb != null && dcb instanceof BaseRoot) {
				BaseRoot rootDCB = (BaseRoot) dcb;
				
				if(rootDCB.getReference() == null) {
					// Does this need to be synchronized (or a larger section)?
					// Or is it just my paranoia... -- PW
					setReference(rootDCB);
					rootDCB.setReference(this);
				} else {
					jsr88Logger.finer("ReferencePatcher: " + rootDCB + " already has reference " + rootDCB.getReference());
				}
			}
			
			if(getReference() == null) {
				// if we're still unpatched here, we found a ddbRoot to patch
				// with, but no DCB to match that root has been created yet.
				// So add to "patch list" so we get patched on that DCB's
				// creation step.
				dc.getPatchList().put(ddbRoot, this);
			}
		}
	}
	
	private DDBeanRoot getDDBeanRoot(DDBean dDBean, DeployableObject dplObj) {
		DDBeanRoot innerDDBeanRoot = null;
		
		if(dplObj != null && dplObj instanceof J2eeApplicationObject) {
			J2eeApplicationObject dplObjRoot = (J2eeApplicationObject) dplObj;

			// !PW The uri string is used as an index by 'getDeployableObject' to
			// find the DeployableObject we're looking for.  The studio team has
			// often changed the format of this index, so be wary of failures here
			String uri = dDBean.getText();

			if(uri != null) { // "/connector" still comes through as null
				DeployableObject innerDObj = dplObjRoot.getDeployableObject(uri);
				if(innerDObj != null) {
					innerDDBeanRoot = innerDObj.getDDBeanRoot();
				} else {
					jsr88Logger.finer("ReferencePatcher: no deployable object found for URI='" + uri + "'");
				}
			} else {
				jsr88Logger.finer("ReferencePatcher: no URI found for XPath='" + dDBean.getXpath() + "'");
			}
		}
			
		return innerDDBeanRoot;
	}
	
}
