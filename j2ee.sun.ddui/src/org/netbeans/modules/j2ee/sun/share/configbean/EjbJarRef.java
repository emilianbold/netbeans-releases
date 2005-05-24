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
/*
 * EjbJarRef.java
 *
 * Created on June 27, 2003, 1:32 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.ArrayList;
import java.util.Collection;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;


/**
 *
 * @author Peter Williams
 */
public class EjbJarRef extends BaseModuleRef {

	/** -----------------------------------------------------------------------
	 * Initialization
	 */
	
	/** Creates new EjbJarRef 
	 */
	public EjbJarRef() {
	}

        public String getHelpId() {
            return "AS_CFG_ModuleRef";                                  //NOI18N
        }

	Collection getSnippets() {
		// FIXME - create & initialize S2B representative here - may be nothing to do
		return new ArrayList();
	}
	
	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		return false;
	}
	
}
