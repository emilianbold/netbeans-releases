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
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;


/**
 *
 * @author  vkraemer
 */
public class StatelessEjb extends SessionEjb {

    /** Creates a new instance of SunONEStatelessEjbDConfigBean */
    public StatelessEjb() {
    }


    public String getHelpId() {
        return "AS_CFG_StatelessEjb";                                   //NOI18N
    }


    /* ------------------------------------------------------------------------
     * XPath to Factory mapping support
     */
/*
	private HashMap statelessEjbFactoryMap;
	
    protected Map getXPathToFactoryMap() {
        if(statelessEjbFactoryMap == null) {
            statelessEjbFactoryMap = (HashMap) super.getXPathToFactoryMap();

            // add child DCB's specific to Stateless Session Beans
        }

        return statelessEjbFactoryMap;
    }
 */

	/* ------------------------------------------------------------------------
	 * Persistence support.  Loads DConfigBeans from previously saved Deployment
	 * plan file.
	 */
/* 
    Collection getSnippets() {
		Collection snippets = super.getSnippets();
		// add any special stuff here
		
		return snippets;
    }

	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
        return super.loadFromPlanFile(config);
    }
 */
}
