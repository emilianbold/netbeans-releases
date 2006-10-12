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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;


/** This DConfigBean is a child of SunWebApp.
 *
 * Property structure of ServletRef from sun-web-app DTD:
 *
 *	servlet <servlet> : Servlet[0,n]
 *		servletName <servlet-name> : String
 *		principalName <principal-name> : String[0,1]
 *			[attr: class-name CDATA #IMPLIED ] *
 *
 * @author  Peter Williams
 */
public class ServletRef extends Base {
    
	/** property event names
	 */
	public static final String SERVLET_NAME = "servletName"; // NOI18N
	public static final String RUN_AS_ROLE_NAME = "runAsRoleName"; // NOI18N
	
	
	/** DDBean that refers to "servlet-name" child of bound DDBean. */
	private DDBean servletNameDD;
	
	/** Holds value of property principalName. */
	private String principalName;
	
	/** Holds value of property className. */
	private String className;
	
	/** Creates a new instance of ServletRef */
	public ServletRef() {
		setDescriptorElement(bundle.getString("BDN_Servlet"));	// NOI18N	
	}

	/** Override init to enable grouping support for this bean and load name
	 *  field from related DDBean.
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
		
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		servletNameDD = getNameDD("servlet-name");

		loadFromPlanFile(getConfig());		
	}

	protected String getComponentName() {
		return getServletName();
	}
	
	/** -----------------------------------------------------------------------
	 *  Validation implementation
	 */
	
	// relative xpaths (double as field id's)
	public static final String FIELD_PRINCIPAL_CLASS_NAME=":class-name";
	
	protected void updateValidationFieldList() {
		super.updateValidationFieldList();
		validationFieldList.add(FIELD_PRINCIPAL_CLASS_NAME);
	}
	
	public boolean validateField(String fieldId) {
		ValidationError error = null;
		boolean result = true;

		if(fieldId.equals(FIELD_PRINCIPAL_CLASS_NAME)) {
			// validation version will be:
			//   expand relative field id to full xpath id based on current context
			//   lookup validator for this field in field validator DB
			//   execute validator
			String absoluteFieldXpath = getAbsoluteXpath("principal-name/" + fieldId);
			if(Utils.notEmpty(className) && !Utils.isJavaClass(className)) {
				Object [] args = new Object[1];
				args[0] = FIELD_PRINCIPAL_CLASS_NAME;
				String message = MessageFormat.format(bundle.getString("ERR_InvalidJavaClass"), args); // NOI18N
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
		return "AS_CFG_Servlet";
	}
	
	/** Getter for property servlet-name.
	 * @return Value of property servlet-name.
	 *
	 */
	public String getServletName() {
		return cleanDDBeanText(servletNameDD);
	}
	
	public String getRunAsRoleName() {
		String roleName = null;
		
		DDBean[] beans = getDDBean().getChildBean("run-as/role-name");	// NOI18N
		if(beans.length > 0) {
			// beans[0] is the run-as element & it's role-name DD
			roleName = beans[0].getText();
		}
		
		return roleName;
	}
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		DDBean eventBean = xpathEvent.getBean();
		
		if(eventBean == servletNameDD) {
			// name changed...
			getPCS().firePropertyChange(SERVLET_NAME, GenericOldValue, getServletName());
			getPCS().firePropertyChange(DISPLAY_NAME, GenericOldValue, getDisplayName());
		} else if(eventBean.getXpath().indexOf("run-as") != -1) {
			getPCS().firePropertyChange(RUN_AS_ROLE_NAME, GenericOldValue, getRunAsRoleName());
		}
	}
	
	/** Getter for property principalName.
	 * @return Value of property principalName.
	 *
	 */
	public String getPrincipalName() {
		return principalName;
	}
	
	/** Setter for property principalName.
	 * @param newPrincipalName New value of property principalName.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setPrincipalName(String newPrincipalName) throws java.beans.PropertyVetoException {
		String oldPrincipalName = principalName;
		getVCS().fireVetoableChange("principalName", oldPrincipalName, newPrincipalName);
		principalName = newPrincipalName;
		getPCS().firePropertyChange("principalName", oldPrincipalName, principalName);
	}
	
	/** Getter for property className.
	 * @return Value of property className.
	 *
	 */
	public String getClassName() {
		return className;
	}
	
	/** Setter for property className.
	 * @param newClassName New value of property className.
	 *
	 * @throws PropertyVetoException
	 *
	 */
	public void setClassName(String newClassName) throws java.beans.PropertyVetoException {
		String oldClassName = className;
		getVCS().fireVetoableChange("className", oldClassName, newClassName);
		className = newClassName;
		getPCS().firePropertyChange("className", oldClassName, className);
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			public CommonDDBean getDDSnippet() {
				Servlet sg = getConfig().getStorageFactory().createServlet();

				// write properties into Servlet bean
				String sn = getServletName();
				if(sn != null) {
					sg.setServletName(sn);
				}

				if(Utils.notEmpty(principalName)) {
					sg.setPrincipalName(principalName);
                    if(Utils.notEmpty(className)) {
                        try {
                            sg.setPrincipalNameClassName(className);
                        } catch (VersionNotSupportedException ex) {
                            // Should not happen at runtime.
                        }
                    }
				}
				
				return sg;
			}
			
			public boolean hasDDSnippet() {
                // No need to check className, as principal name must be filled in that case.
				if(principalName != null && principalName.length() > 0) {
					return true;
				}
				
				return false;
			}	
			
/*
			public BaseBean mergeIntoRootDD(BaseBean ddRoot) {
				SunWebApp swa = SunWebApp.createGraph();
				BaseBean newBean = getDDSnippet();
				swa.addValue(SunWebApp.SERVLET, newBean);
				ddRoot.merge(swa, BaseBean.MERGE_UNION);
				return newBean;
			}
 */

/*
			public BaseBean mergeIntoRovingDD(BaseBean ddParent) {
				BaseBean newBean = getDDSnippet();
				ddParent.addValue(SunWebApp.SERVLET, newBean);
				return newBean;
			}
 */
			public String getPropertyName() {
				return SunWebApp.SERVLET;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
	private class ServletRefFinder extends NameBasedFinder {
		public ServletRefFinder(String beanName) {
			super(Servlet.SERVLET_NAME, beanName, Servlet.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		Servlet beanGraph = (Servlet) config.getBeans(uriText, constructFileName(), 
			getParser(), new ServletRefFinder(getServletName()));
		
		clearProperties();
		
		if(beanGraph != null) {
			principalName = beanGraph.getPrincipalName();
            try {
                className = beanGraph.getPrincipalNameClassName();
            } catch(VersionNotSupportedException ex) {
                // Should not happen at runtime.
            }
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
		principalName = null;
        className = null;
	}
	
	protected void setDefaultProperties() {
	}
}
