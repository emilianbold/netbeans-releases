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

import java.util.Collection;
import java.util.ArrayList;
import java.text.MessageFormat;

import org.netbeans.modules.schema2beans.BaseBean;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal;


/**
 *
 * @author Peter Williams
 */
public class ResourceRef extends Base {
    
	/** property event names
	 */
	public static final String RES_REF_NAME = "resRefName"; // NOI18N
	
	/** Holds value of property resourceRefName. */
	private DDBean resRefNameDD;
	
	/** Holds value of property jndiName. */
	private String jndiName;
	
	/** Holds value of property principalName. */
	private String principalName;
	
	/** Holds value of property principalPassword. */
	private String principalPassword;
	
    /** Creates a new instance of ResourceRef */
	public ResourceRef() {
		setDescriptorElement(bundle.getString("BDN_ResourceRef"));	// NOI18N	
	}

	/** Override init to enable grouping support for this bean
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		resRefNameDD = getNameDD("res-ref-name");
                jndiName = getResRefName();

		loadFromPlanFile(getConfig());
	}
	
	protected String getComponentName() {
		return getResRefName();
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
		return "AS_CFG_ResourceRef";
	}	
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		if(resRefNameDD == xpathEvent.getBean()) {
			// name changed...
			getPCS().firePropertyChange(RES_REF_NAME, "", getResRefName());
			getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());
		}
	}	

	/** Getter for property resRefName.
	 * @return Value of property resRefName.
	 *
	 */
	public String getResRefName() {
		return cleanDDBeanText(resRefNameDD);
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
	
	/** Getter for property principalName.
	 * @return Value of property principalName.
	 *
	 */
	public String getPrincipalName() {
		return this.principalName;
	}
	
	/** Setter for property principalName.
	 * @param principalName New value of property principalName.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPrincipalName(String principalName) throws java.beans.PropertyVetoException {
		String oldPrincipalName = this.principalName;
		getVCS().fireVetoableChange("principalName", oldPrincipalName, principalName);
		this.principalName = principalName;
		getPCS().firePropertyChange("principalName", oldPrincipalName, principalName);
	}
	
	/** Getter for property principalPassword.
	 * @return Value of property principalPassword.
	 *
	 */
	public String getPrincipalPassword() {
		return this.principalPassword;
	}
	
	/** Setter for property principalPassword.
	 * @param principalPassword New value of property principalPassword.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPrincipalPassword(String principalPassword) throws java.beans.PropertyVetoException {
		String oldPrincipalPassword = this.principalPassword;
		getVCS().fireVetoableChange("principalPassword", oldPrincipalPassword, principalPassword);
		this.principalPassword = principalPassword;
		getPCS().firePropertyChange("principalPassword", oldPrincipalPassword, principalPassword);
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 *
	BaseBean getDDSnippet() {
		return null;
	}
	
	String getFileName() {
		return getParent().getFileName();
	}
	*/
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			
			public CommonDDBean getDDSnippet() {
				org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef ref = 
					StorageBeanFactory.getDefault().createResourceRef();

				// write properties into Servlet bean
				String resRefName = getResRefName();
				if(resRefName != null) {
					ref.setResRefName(resRefName);
				}

				if(jndiName != null && jndiName.length() > 0) {
					ref.setJndiName(jndiName);
				}

				boolean hasPrincipalName = (principalName != null && principalName.length() > 0);
				boolean hasPrincipalPassword = (principalPassword != null && principalPassword.length() > 0);
				
				if(hasPrincipalName || hasPrincipalPassword) {
					DefaultResourcePrincipal drp = ref.getDefaultResourcePrincipal();
					if(drp == null) {
						drp = ref.newDefaultResourcePrincipal();
					}

					if(hasPrincipalName) {
						drp.setName(principalName);
					}

					if(hasPrincipalPassword) {
						drp.setPassword(principalPassword);
					}

					ref.setDefaultResourcePrincipal(drp);
				}
				
				return ref;
			}
			
			public boolean hasDDSnippet() {
				if(jndiName != null && jndiName.length() > 0) {
					return true;
				}
				
				if(principalName != null && principalName.length() > 0) {
					return true;
				}
				
				if(principalPassword != null && principalPassword.length() > 0) {
					return true;
				}
				
				return false;
			}
			

			public String getPropertyName() {
				return Ejb.RESOURCE_REF;
			}
			
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
/*
	private class ResourceRefFinder implements ConfigFinder {
		private String beanName;

		public ResourceRefFinder(String beanName) {
			this.beanName = beanName;
		}

		public Object find(Object obj) {
			org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef resEnvRef = null;			
			CommonDDBean root = (CommonDDBean) obj;
			
			String[] props = root.findPropertyValue(
				org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef.RES_REF_NAME, beanName);
			
			for(int i = 0; i < props.length; i++) {
				CommonDDBean candidate = root.graphManager().getPropertyParent(props[i]);
				if(candidate instanceof org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef) {
					resEnvRef = (org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef) candidate;
					break;
				}
			}
			
			return resEnvRef;
		}
	}	
*/

	private class ResourceRefFinder extends NameBasedFinder {
		public ResourceRefFinder(String beanName) {
			super(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef.RES_REF_NAME,
				beanName, org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef beanGraph = 
			(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef) config.getBeans(uriText, 
			constructFileName(), null, new ResourceRefFinder(getResRefName()));
		
		clearProperties();
		
		if(beanGraph != null) {
			jndiName = beanGraph.getJndiName();
			
			DefaultResourcePrincipal drp = beanGraph.getDefaultResourcePrincipal();
			if(drp != null) {
				principalName = drp.getName();
				principalPassword = drp.getPassword();
			} 
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
		jndiName = null;
		principalName = null;
		principalPassword = null;
	}
	
	protected void setDefaultProperties() {
		jndiName = getResRefName();
	}
}
