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

import org.netbeans.modules.j2ee.sun.share.Constants;


/**
 *
 * @author Peter Williams
 * @author Vince Kraemer
 */
public abstract class AbstractDCBFactory implements DCBFactory, Constants {
    
	abstract protected Class getClass(DDBean ddBean, Base dcbParent) throws ConfigurationException;

	public Base createDCB(DDBean ddBean, Base dcbParent) throws ConfigurationException {
		Class dcbClass = getClass(ddBean, dcbParent);
//		System.out.println(this.getClass().getName()+"('" + dcbClass.getName() + "': createDCB");
//		System.out.println("  dDBean.getXpath()=="+ddBean.getXpath());
//		System.out.println("        .getText()=="+((ddBean.getText() != null) ? ddBean.getText() : "(null)"));
//		System.out.println("  Parent DCB: "+ dcbParent.getClass().getName());

		Base newDCB = null;
//		Throwable cause = null;

		try {
			newDCB = (Base) dcbClass.newInstance();
			newDCB.init(ddBean, dcbParent);
		} catch(InstantiationException ex) {
			Object [] args = new Object [1];
			args[0] = dcbClass.getName();
			throw Utils.makeCE("ERR_UnexpectedInstantiateException", args, ex);	// NOI18N
		} catch(IllegalAccessException ex) {
			Object [] args = new Object [1];
			args[0] = dcbClass.getName();
			throw Utils.makeCE("ERR_UnexpectedIllegalAccessException", args, ex);	// NOI18N
		} catch (RuntimeException ex) {
            throw Utils.makeCE("ERR_UnexpectedRuntimeException", null, ex);	// NOI18N
		}
		
//		if (null == newDCB) {
//			try {
//				//getErrorClass().
//				newDCB = new Error(); 
//				newDCB.init(ddBean, dcbParent); 
//			} catch (Throwable t) {
//				String message = "Could not instantiate the error class bean: Error";
//				jsr88Logger.severe(message);
//				throw new ConfigurationException(message);
//			}
//		} else {
//			if (null != cause)
//				jsr88Logger.throwing("a","b",cause);
//		}

		return newDCB;
	}
}
