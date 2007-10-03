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

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;
import java.text.MessageFormat;

import org.netbeans.modules.schema2beans.BaseBean;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;


/**
 *
 * @author Peter Williams
 */
public class ResourceEnvRef extends Base {
    
	/** property event names
	 */
	public static final String RESOURCE_ENV_REF_NAME = "resourceEnvRefName"; // NOI18N
	
	/** Holds value of property resourceEnvRef. */
	private DDBean resourceEnvRefNameDD;
	
	/** Holds value of property jndiName. */
	private String jndiName;
	
    /** Creates a new instance of ResourceEnvRef */
	public ResourceEnvRef() {
		setDescriptorElement(bundle.getString("BDN_ResourceEnvRef"));	// NOI18N	
	}

	/** Override init to enable grouping support for this bean
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		resourceEnvRefNameDD = getNameDD("resource-env-ref-name");
		
        updateNamedBeanCache(SunWebApp.RESOURCE_ENV_REF);
        
		loadFromPlanFile(getConfig());
	}
	
	protected String getComponentName() {
		return getResourceEnvRefName();
	}
	
	/** -----------------------------------------------------------------------
	 *  Validation implementation
	 */
	
	// relative xpaths (double as field id's)
	public static final String FIELD_JNDI_NAME="jndi-name";
	
	protected void updateValidationFieldList() {
		super.updateValidationFieldList();
		validationFieldList.add(FIELD_JNDI_NAME);
	}
	
	public boolean validateField(String fieldId) {
		ValidationError error = null;
		boolean result = true;

		if(fieldId.equals(FIELD_JNDI_NAME)) {
			// validation version will be:
			//   expand relative field id to full xpath id based on current context
			//   lookup validator for this field in field validator DB
			//   execute validator
			J2EEBaseVersion moduleVersion = getJ2EEModuleVersion();
            if(moduleVersion.compareSpecification(J2EEVersion.JAVAEE_5_0) < 0) {
                String absoluteFieldXpath = getAbsoluteXpath(fieldId);
                if(!Utils.notEmpty(jndiName)) {
                    Object [] args = new Object[1];
                    args[0] = FIELD_JNDI_NAME;
                    String message = MessageFormat.format(bundle.getString("ERR_SpecifiedFieldIsEmpty"), args); // NOI18N
                    error = ValidationError.getValidationError(absoluteFieldXpath, message);
                } else {
                    error = ValidationError.getValidationErrorMask(absoluteFieldXpath);
                }
            }
		}
		
		if(error != null) {
			getMessageDB().updateError(error);
		}
		
		// return true if there was no error added
		return (error == null || !Utils.notEmpty(error.getMessage()));
	}
	
	/** Getter for helpId property
	 * @return Help context ID for this DConfigBean
	 */
	public String getHelpId() {
		return "AS_CFG_ResourceEnvRef";
	}
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		if(resourceEnvRefNameDD == xpathEvent.getBean()) {
			// name changed...
			getPCS().firePropertyChange(RESOURCE_ENV_REF_NAME, "", getResourceEnvRefName());
			getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());

            updateNamedBeanCache(SunWebApp.RESOURCE_ENV_REF);
		}
	}
	
	/** Getter for property resourceEnvRefName.
	 * @return Value of property resourceEnvRefName.
	 *
	 */
	public String getResourceEnvRefName() {
		return cleanDDBeanText(resourceEnvRefNameDD);
	}
	
	/** Getter for property jndiName.
	 * @return Value of property jndiName.
	 *
	 */
	public String getJndiName() {
		return this.jndiName;
	}
	
	/** Setter for property jndiName.
	 * @param jndiName New value of property jndiName.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setJndiName(String jndiName) throws java.beans.PropertyVetoException {
		String oldJndiName = this.jndiName;
		getVCS().fireVetoableChange("jndiName", oldJndiName, jndiName);
		this.jndiName = jndiName;
		getPCS().firePropertyChange("jndiName", oldJndiName, jndiName);
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			
			public CommonDDBean getDDSnippet() {
				org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef ref = 
					getConfig().getStorageFactory().createResourceEnvRef();

				// write properties into Servlet bean
				String resourceEnvRefName = getResourceEnvRefName();
				if(resourceEnvRefName != null) {
					ref.setResourceEnvRefName(resourceEnvRefName);
				}

				if(jndiName != null && jndiName.length() > 0) {
					ref.setJndiName(jndiName);
				}

				return ref;
			}
			
			public boolean hasDDSnippet() {
				if(jndiName != null && jndiName.length() > 0) {
					return true;
				}
				
				return false;
			}
			
			public String getPropertyName() {
				return SunWebApp.RESOURCE_ENV_REF;
			}
			
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
/*
	private class ResourceEnvRefFinder implements ConfigFinder {
		private String beanName;

		public ResourceEnvRefFinder(String beanName) {
			this.beanName = beanName;
		}

		public Object find(Object obj) {
			org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef resEnvRef = null;			
			CommonDDBean root = (CommonDDBean) obj;
			
			String[] props = root.findPropertyValue(
				org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef.RESOURCE_ENV_REF_NAME, beanName);
			
			for (int i = 0; i < props.length; i++) {
				CommonDDBean candidate = root.graphManager().getPropertyParent(props[i]);
				if (candidate instanceof org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef) {
					resEnvRef = (org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef) candidate;
					break;
				}
			}
			
			return resEnvRef;
		}
	}
 */
	
	private static class ResourceEnvRefFinder extends NameBasedFinder {
		public ResourceEnvRefFinder(String beanName) {
			super(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef.RESOURCE_ENV_REF_NAME,
				beanName, org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef beanGraph = 
			(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef) config.getBeans(uriText, 
			constructFileName(), getParser(), new ResourceEnvRefFinder(getResourceEnvRefName()));

		clearProperties();
		
		if(beanGraph != null) {
			jndiName = beanGraph.getJndiName();
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
		jndiName = null;
	}
	
	protected void setDefaultProperties() {
		// no defaults
	}
}
