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

/**
 *
 * @author  vkraemer
 */
public class Error extends Base {
	
	/** Creates a new instance of Error */
	public Error() {
	}

        public String getHelpId() {
            return "AS_CFG_Error";                                      //NOI18N
        }

	boolean loadFromPlanFile(SunONEDeploymentConfiguration config) {
		return false;
	}
	
	Collection getSnippets() {
		return new ArrayList();
	}
	
}
