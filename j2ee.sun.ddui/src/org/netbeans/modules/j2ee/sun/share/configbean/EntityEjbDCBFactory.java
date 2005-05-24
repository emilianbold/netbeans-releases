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

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.model.DeployableObject;
import javax.enterprise.deploy.model.J2eeApplicationObject;

/** This factory creates instances of DCB's that depend on the value of the 
 * persistence-type element.
 *
 * Includes attempted default value manipulation for the EjbJarRoot, if the jar 
 * will contain a cmp bean. 
 *
 * This introduces a "stateful"-ness to this factory
 *
 * @author Peter Williams
 * @author vkraemer
 */
public class EntityEjbDCBFactory extends AbstractDCBFactory { // implements DCBFactory {
    
    static final String PERSISTENCE_TYPE_KEY = "persistence-type"; // NOI18N
    static final String CONTAINER = "Container"; // NOI18N
    static final String  BEAN = "Bean"; // NOI18N

	private EjbJarRoot parent;

	/** Create the factory related to a specific EjbJarRoot DCB
	 *
	 * @param parent The EjbJarRoot bean
	 */
	public EntityEjbDCBFactory(EjbJarRoot parent) {
		this.parent = parent;
	}

	protected Class getClass(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		Class dcbClass;
		String testRet[] = ddBean.getText(PERSISTENCE_TYPE_KEY);

		if(null != testRet && 1 == testRet.length && testRet[0].indexOf(CONTAINER) > -1) {
			dcbClass = CmpEntityEjb.class;

			// FIXME !PW is there a better place to put this???
			//
			// if the Jar hasn't had the CmpResourceJndiName property set
			// get a default value and set it.
			//
			// Picking this well should make the server easier to build 
			// cmp beans for.
                        parent.addCmpResourceIfNotPresent();
			/*if(null == parent.getCmpResourceJndiName()) {
				try {
					parent.setCmpResourceJndiName(Utils.getDefaultCmpResourceJndiName(parent));
				}
				catch(java.beans.PropertyVetoException pve) {
					jsr88Logger.severe("bug in Utils.getDefaultCmpResourceJndiName");
				}
			}*/
        } else if(null != testRet && 1 == testRet.length && testRet[0].indexOf(BEAN) > -1) {
            dcbClass = EntityEjb.class;
        } else {
            throw Utils.makeCE("ERR_UnknownPersistenceType", testRet, null);
        }
        
		return dcbClass;
	}
	
	/* 
	public Base createDCB(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		System.out.println("EntityEjbDCBFactory: createDCB");
		System.out.println("dDBean.getXpath()=="+ddBean.getXpath());
		//System.out.println("      .getText()=="+((ddBean.getText() != null) ? ddBean.getText() : "(null)"));

		Base newDCB = null;

		Class dcbClass = Object.class;
		String testRet[] = ddBean.getText("persistence-type");
		if (null != testRet && 1 == testRet.length && testRet[0].indexOf("Container") > -1) {
			newDCB = new CmpEntityEjb();
			dcbClass = CmpEntityEjb.class;
		} else if (null != testRet && 1 == testRet.length && testRet[0].indexOf("Bean") > -1) {
			newDCB = new EntityEjb();
			dcbClass = EntityEjb.class;
		} else {
			System.out.println("Error: Unknown persistence-type element value in deployment descriptor");
			// throw exception?
		}

		if(newDCB != null) {
			newDCB.init(ddBean, dcbParent);
		}
		else {
			newDCB = new Error(this, dcbClass, null);
		}			
		return newDCB;
	}
	**/
}
