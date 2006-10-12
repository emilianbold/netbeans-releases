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
import java.text.MessageFormat;

import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;


/**
 *
 * @author Peter Williams
 */
public class MessageDestinationRef extends Base {
	
	/** property event names
	 */
	public static final String MESSAGE_DESTINATION_REF_NAME = "messageDestinationRefName"; // NOI18N
    
	/** Holds value of property messageDestinationRefName. */
	private DDBean messageDestinationRefNameDD;
	
	/** Holds value of property jndiName. */
	private String jndiName;
	
    /** Creates a new instance of MessageDestinationRef */
	public MessageDestinationRef() {
		setDescriptorElement(bundle.getString("BDN_MessageDestinationRef"));	// NOI18N	
	}

	/** Override init to enable grouping support for this bean
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		messageDestinationRefNameDD = getNameDD("message-destination-ref-name");
		
		loadFromPlanFile(getConfig());
	}
	
	protected String getComponentName() {
		return getMessageDestinationRefName();
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
		return "AS_CFG_MessageDestinationRef";
	}
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		if(messageDestinationRefNameDD == xpathEvent.getBean()) {
			// name changed...
			getPCS().firePropertyChange(MESSAGE_DESTINATION_REF_NAME, "", getMessageDestinationRefName());
			getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());
		}
	}	

	/** Getter for property messageDestinationRefName.
     * @return Value of property messageDestinationRefNameDD.
     *
     */
	public String getMessageDestinationRefName() {
		return cleanDDBeanText(messageDestinationRefNameDD);
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
				org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef mdRef = 
                    getConfig().getStorageFactory().createMessageDestinationRef();

				// write properties into parent bean
				String messageDestinationRefName = getMessageDestinationRefName();
				if(messageDestinationRefName != null) {
					mdRef.setMessageDestinationRefName(messageDestinationRefName);
				}

				if(jndiName != null && jndiName.length() > 0) {
					mdRef.setJndiName(jndiName);
				}

				return mdRef;
			}
			
			public boolean hasDDSnippet() {
				if(jndiName != null && jndiName.length() > 0) {
					return true;
				}
				
				return false;
			}
			
			public String getPropertyName() {
				return Ejb.MESSAGE_DESTINATION_REF;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}

	private class MessageDestinationRefFinder extends NameBasedFinder {
		public MessageDestinationRefFinder(String beanName) {
			super(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef.MESSAGE_DESTINATION_REF_NAME,
			      beanName, org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef beanGraph = 
			(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) config.getBeans(uriText, 
			constructFileName(), getParser(), new MessageDestinationRefFinder(getMessageDestinationRefName()));
		
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
//		// Set default JNDI name
//		jndiName = messageDestinationRefNameDD.getText();
//
//		// Prepend "ejb/" only if, the Ref name does not start with "ejb/"
//		// By blue prints convention, Ref name always starts with "ejb/"
//		if (!jndiName.startsWith("ejb/")) {	// NOI18N
//			jndiName = "ejb/" + jndiName;	// NOI18N
//		}
//        
//        getConfig().getMasterDCBRoot().setDirty();
	}
}
