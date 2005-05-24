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

import org.netbeans.modules.schema2beans.BaseBean;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;
import org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb;


/** Property structure of SecurityRoleMapping from DTD:
 *
 *		security-role-mapping : SecurityRoleMapping[0,n]
 *			role-name : String
 *			(
 *			  | principal-name : String
 *			  | group-name : String
 *			)[1,n]
 *
 * Master list of principal and group names are stored in ---
 *
 * @author  Peter Williams
 * @version %I%, %G%
 */
public class SecurityRoleMapping extends Base {
    
	/** property event names
	 */
	public static final String ROLE_NAME = "roleName"; // NOI18N
	
	private DDBean securityRoleNameDD;
	
    /** Holds value of property principalName. */
    private ArrayList principalNames;
    
    /** Holds value of property groupName. */
    private ArrayList groupNames;
    
    /** Creates a new instance of SunONESRMDConfigBean */
	public SecurityRoleMapping() {
		setDescriptorElement(bundle.getString("BDN_SecurityRoleMapping"));	// NOI18N	
	}

	/** Override init to enable grouping support for this bean
	 * @param dDBean DDBean matching this bean
	 * @param parent Parent DConfigBean in the tree
	 */
	protected void init(DDBean dDBean, Base parent) throws ConfigurationException {
		super.init(dDBean, parent);
// !PW Disable grouping code for now, spec non-compliance.
//		initGroup(dDBean, parent);
		
		securityRoleNameDD = getNameDD("role-name"); // NOI18N
		
		loadFromPlanFile(getConfig());
	}
	
	protected String getComponentName() {
		return getRoleName();
	}
	
	/** Getter for helpId property
	 * @return Help context ID for this DConfigBean
	 */
	public String getHelpId() {
		return "AS_CFG_SecurityRoleAssignment";
	}	
	
	/** The DDBean (or one of it's children) that this DConfigBean is bound to
	 *  has changed.
	 *
	 * @param xpathEvent
	 */    
	public void notifyDDChange(XpathEvent xpathEvent) {
		super.notifyDDChange(xpathEvent);

		if(securityRoleNameDD == xpathEvent.getBean()) {
			// name changed...
			getPCS().firePropertyChange(ROLE_NAME, "", getRoleName());
			getPCS().firePropertyChange(DISPLAY_NAME, "", getDisplayName());
		}
	}

	/** Getter for property roleName.
     * @return Value of property roleName.
     *
     */
    public String getRoleName() {
        return cleanDDBeanText(securityRoleNameDD); // .getText();
    }
    
    /** Getter for property principalNames.
     * @return Value of property principalNames.
     *
     */
    public List getPrincipalNames() {
        return principalNames;
    }
    
	public String getPrincipalName(int index) {
		return (String) principalNames.get(index);
	}
	
    /** Setter for property principalNames.
     * @param newPrincipalNames New value of property principalNames.
     *
     * @throws PropertyVetoException
     *
     */
    public void setPrincipalNames(ArrayList newPrincipalNames) throws java.beans.PropertyVetoException {
        List oldPrincipalNames = principalNames;
        getVCS().fireVetoableChange("principalNames", oldPrincipalNames, newPrincipalNames);	// NOI18N
        principalNames = newPrincipalNames;
        getPCS().firePropertyChange("principalNames", oldPrincipalNames, principalNames);	// NOI18N
    }
    
	public void addPrincipalName(String newPrincipalName) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("principalName", null, newPrincipalName);	// NOI18N
		principalNames.add(newPrincipalName);
		getPCS().firePropertyChange("principalName", null, newPrincipalName );	// NOI18N
	}
	
	public void removePrincipalName(String oldPrincipalName) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("principalName", oldPrincipalName, null);	// NOI18N
		principalNames.remove(oldPrincipalName);
		getPCS().firePropertyChange("principalName", oldPrincipalName, null );	// NOI18N
	}
	
	/** Getter for property groupNames.
     * @return Value of property groupNames.
     *
     */
    public List getGroupNames() {
        return this.groupNames;
    }
    
	public String getGroupName(int index) {
		return (String) groupNames.get(index);
	}

	/** Setter for property groupName.
     * @param newGroupNames New value of property groupNames.
     *
     * @throws PropertyVetoException
     *
     */
    public void setGroupNames(ArrayList newGroupNames) throws java.beans.PropertyVetoException {
        List oldGroupNames = groupNames;
        getVCS().fireVetoableChange("groupNames", oldGroupNames, newGroupNames);	// NOI18N
        groupNames = newGroupNames;
        getPCS().firePropertyChange("groupNames", oldGroupNames, groupNames);	// NOI18N
    }
	
	public void addGroupName(String newGroupName) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("groupName", null, newGroupName);	// NOI18N
		groupNames.add(newGroupName);
		getPCS().firePropertyChange("groupName", null, newGroupName );	// NOI18N
	}
	
	public void removeGroupName(String oldGroupName) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("groupName", oldGroupName, null);	// NOI18N
		groupNames.remove(oldGroupName);
		getPCS().firePropertyChange("groupName", oldGroupName, null );	// NOI18N
	}
	
	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved deployment
	 * plan file.
	 */
	Collection getSnippets() {
		Collection snippets = new ArrayList();
		Snippet snipOne = new DefaultSnippet() {
			public CommonDDBean getDDSnippet() {
				org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping srm = 
					StorageBeanFactory.getDefault().createSecurityRoleMapping();

				// write properties into SecurityRoleMapping bean
				srm.setRoleName(getRoleName());
				
				if(principalNames.size() > 0) {
					String [] names = (String []) principalNames.toArray(new String[0]);
					srm.setPrincipalName(names);
				}
				
				if(groupNames.size() > 0) {
					String [] names = (String []) groupNames.toArray(new String[0]);
					srm.setGroupName(names);
				}

				return srm;
			}
			
			public boolean hasDDSnippet() {
				if(principalNames != null && principalNames.size() > 0) {
					return true;
				}
				
				if(groupNames != null && groupNames.size() > 0) {
					return true;
				}
				
				return false;
			}	
			
			public String getPropertyName() {
				return SunWebApp.SECURITY_ROLE_MAPPING;
			}
		};
		
		snippets.add(snipOne);
		return snippets;
	}
	
	private class SecurityRoleMappingFinder extends NameBasedFinder {
		public SecurityRoleMappingFinder(String beanName) {
			super(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping.ROLE_NAME,
				beanName, org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping.class);
		}
	}	
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		String uriText = getUriText();

		org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping beanGraph = 
			(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping) config.getBeans(
			uriText, constructFileName(), null, new SecurityRoleMappingFinder(getRoleName()));
		
		clearProperties();
		
		if(beanGraph != null) {
			String [] names = beanGraph.getPrincipalName();
			if(names != null && names.length > 0) {
				principalNames = new ArrayList(names.length+3);
				for(int i = 0; i < names.length; i++) {
					principalNames.add(names[i]);
				}
			}
			
			names = beanGraph.getGroupName();
			if(names != null && names.length > 0) {
				groupNames = new ArrayList(names.length+3);
				for(int i = 0; i < names.length; i++) {
					groupNames.add(names[i]);
				}
			}
		} else {
			setDefaultProperties();
		}
		
		return (beanGraph != null);
	}
	
	protected void clearProperties() {
		principalNames = new ArrayList(3);
		groupNames = new ArrayList(3);
	}
	
	protected void setDefaultProperties() {
		// no defaults
	}
}
