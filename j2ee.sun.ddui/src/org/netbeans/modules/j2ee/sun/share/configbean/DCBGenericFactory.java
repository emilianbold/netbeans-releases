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

import org.netbeans.modules.schema2beans.BaseBean;
/**
 *
 * @author Peter Williams
 */
public class DCBGenericFactory extends AbstractDCBFactory { //implements DCBFactory {

	private Class dcbClass;

	DCBGenericFactory(Class c) {
		dcbClass = c;
	}

	protected Class getClass(DDBean ddBean, Base dcbParent) throws ConfigurationException{
		return dcbClass;
	}

}
