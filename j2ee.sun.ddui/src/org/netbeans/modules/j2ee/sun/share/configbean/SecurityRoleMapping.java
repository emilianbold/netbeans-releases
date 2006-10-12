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
import java.util.Iterator;
import java.util.List;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathEvent;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp;

import org.netbeans.modules.j2ee.sun.share.PrincipalNameMapping;
import org.netbeans.modules.j2ee.sun.share.configbean.Base.DefaultSnippet;


/** Property structure of SecurityRoleMapping from DTD:
 *
 *		securityRoleMapping <security-role-mapping> : SecurityRoleMapping[0,n]
 *			roleName <role-name> : String
 *			(
 *			  | principalName <principal-name> : String
 *			  | 	[attr: class-name CDATA #IMPLIED ]
 *			  | groupName <group-name> : String
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
    private ArrayList/*PrincipalNameMapping*/ principalNames;
    
    /** Holds value of property groupName. */
    private ArrayList/*String*/ groupNames;
    
    /** Our parent if this security-role-mapping is inside a root module (EAR, WAR, EJB) */
    private BaseRoot rootParent;
    
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

        // IZ 78686, 84549 - if inside an EAR, WAR, or EJB, remove this mapping from the loaded mappings.
        if(parent instanceof BaseRoot) {
            rootParent = (BaseRoot) parent;
            updateRootMappings();
        }
        
		loadFromPlanFile(getConfig());
	}
    
    private void updateRootMappings() {
		if(rootParent != null) {
            org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping mapping = 
                    rootParent.removeSavedRoleMapping(getRoleName());
        }
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

            // IZ 78686, 84549 - if inside an EAR, WAR, or EJB, remove from loaded mappings on name change.
            updateRootMappings();
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
    public List/*PrincipalNameMapping*/ getPrincipalNames() {
        return principalNames;
    }
    
	public PrincipalNameMapping getPrincipalName(int index) {
		return (PrincipalNameMapping) principalNames.get(index);
	}
	
    /** Setter for property principalNames.
     * @param newPrincipalNames New value of property principalNames.
     *
     * @throws PropertyVetoException
     *
     */
    public void setPrincipalNames(ArrayList/*PrincipalNameMapping*/ newPrincipalNames) throws java.beans.PropertyVetoException {
        List oldPrincipalNames = principalNames;
        getVCS().fireVetoableChange("principalNames", oldPrincipalNames, newPrincipalNames);	// NOI18N
        principalNames = newPrincipalNames;
        getPCS().firePropertyChange("principalNames", oldPrincipalNames, principalNames);	// NOI18N
    }
    
	public void addPrincipalName(PrincipalNameMapping newPrincipalName) throws java.beans.PropertyVetoException {
		getVCS().fireVetoableChange("principalName", null, newPrincipalName);	// NOI18N
		principalNames.add(newPrincipalName);
		getPCS().firePropertyChange("principalName", null, newPrincipalName );	// NOI18N
	}
	
	public void removePrincipalName(PrincipalNameMapping oldPrincipalName) throws java.beans.PropertyVetoException {
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
					getConfig().getStorageFactory().createSecurityRoleMapping();

				// write properties into SecurityRoleMapping bean
				srm.setRoleName(getRoleName());
				
				if(principalNames.size() > 0) {
					Iterator principalIter = principalNames.iterator();
					while(principalIter.hasNext()) {
						PrincipalNameMapping nameMap = (PrincipalNameMapping) principalIter.next();
						int index = srm.addPrincipalName(nameMap.getPrincipalName());
						if(Utils.notEmpty(nameMap.getClassName())) {
							try {
								srm.setPrincipalNameClassName(index, nameMap.getClassName());
							} catch(VersionNotSupportedException ex) {
								// Should not happen at runtime.
							}
						}
					}
				}
				
				if(groupNames.size() > 0) {
					String [] names = (String []) groupNames.toArray(new String[groupNames.size()]);
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
			uriText, constructFileName(), getParser(), new SecurityRoleMappingFinder(getRoleName()));
		
		clearProperties();
		
		if(beanGraph != null) {
			String [] names = beanGraph.getPrincipalName();
			if(names != null && names.length > 0) {
				principalNames = new ArrayList(names.length+3);
				for(int i = 0; i < names.length; i++) {
                    String className = null;
                    try {
                        className = beanGraph.getPrincipalNameClassName(i);
                    } catch(VersionNotSupportedException ex) {
                        // Should not happen at runtime.
                    }
					principalNames.add(new PrincipalNameMapping(names[i], className));
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
