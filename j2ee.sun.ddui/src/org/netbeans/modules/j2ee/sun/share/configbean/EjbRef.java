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
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;


/**
 *
 * @author Peter Williams
 */
public class EjbRef extends Base {
	
	/** property event names
	 */
	public static final String EJB_REF_NAME = "ejbRefName"; // NOI18N
    
	/** Holds value of property ejbRefName. */
	private DDBean ejbRefNameDD;
	
	/** Holds value of property jndiName. */
	private String jndiName;
	
    /** Creates a new instance of EjbRef */
	public EjbRef() {
		setDescriptorElement(bundle.getString("BDN_EjbRef"));	// NOI18N	
	}

	/** Override init to enable grouping support for this bean
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		ejbRefNameDD = getNameDD("ejb-ref-name");
		
        updateNamedBeanCache(SunWebApp.EJB_REF);
		
		loadFromPlanFile(getConfig());
	}
	
	protected String getComponentName() {
		return getEjbRefName();
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
		return "AS_CFG_EjbRef";
	}
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		if(ejbRefNameDD == xpathEvent.getBean()) {
			// name changed...
			getPCS().firePropertyChange(EJB_REF_NAME, "", getEjbRefName());
			getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());
            
            updateNamedBeanCache(SunWebApp.EJB_REF);
		}
	}	

	/** Getter for property ejbRefName.
	 * @return Value of property ejbRefName.
	 *
	 */
	public String getEjbRefName() {
		return cleanDDBeanText(ejbRefNameDD);
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
				org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef ref = 
                    getConfig().getStorageFactory().createEjbRef();

				// write properties into Servlet bean
				String ejbRefName = getEjbRefName();
				if(ejbRefName != null) {
					ref.setEjbRefName(ejbRefName);
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
				return Ejb.EJB_REF;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}

	private static class EjbRefFinder extends NameBasedFinder {
		public EjbRefFinder(String beanName) {
			super(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef.EJB_REF_NAME,
			      beanName, org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef beanGraph = 
			(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef) config.getBeans(uriText, 
			constructFileName(), getParser(), new EjbRefFinder(getEjbRefName()));
		
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
        if(requiresJndiName()) {
            // Set default JNDI name
            jndiName = ejbRefNameDD.getText();

            // Prepend "ejb/" only if, the Ref name does not start with "ejb/"
            // By blue prints convention, Ref name always starts with "ejb/"
            if (!jndiName.startsWith("ejb/")) {	// NOI18N
                jndiName = "ejb/" + jndiName;	// NOI18N
            }

            getConfig().getMasterDCBRoot().setDirty();
        }
	}
}
