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
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.model.DDBean;


/**
 *
 * @author  vkraemer
 */
public class StatelessEjb extends SessionEjb {

    /** Creates a new instance of StatelessEjb */
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
