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
import java.util.List;
import java.text.MessageFormat;

import org.netbeans.modules.schema2beans.BaseBean;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;


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
                    StorageBeanFactory.getDefault().createEjbRef();

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

	private class EjbRefFinder extends NameBasedFinder {
		public EjbRefFinder(String beanName) {
			super(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef.EJB_REF_NAME,
			      beanName, org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef beanGraph = 
			(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef) config.getBeans(uriText, 
			constructFileName(), null, new EjbRefFinder(getEjbRefName()));
		
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
		// Set default JNDI name
                jndiName = ejbRefNameDD.getText();

                //Prepend "ejb/" only if, the Ref name does not start with "ejb/"
                //By blue prints convention, Ref name always starts with "ejb/"
                if (!jndiName.startsWith("ejb/")) {	// NOI18N
                    jndiName = "ejb/" + jndiName;	// NOI18N
                }
	}
}
