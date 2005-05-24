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
 * Constants.java
 *
 * Created on October 30, 2003, 3:01 PM
 */

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.util.logging.Logger;
/**
 *
 * @author  vkraemer
 */
public class Constants {
    
    private Constants() {
    }
    
    public static final Logger pluginLogger =
        Logger.getLogger("org.netbeans.modules.j2ee.sun.ide.j2ee.spi"); // NOI18N

    public static final String DEPLOYMENT_PLUGIN_DIR_RESOURCE = 
        "J2EE/DeploymentPlugins/J2EE"; // NOI18N
    
    public static final String DEFAULT_DOMAIN_NAME = "domain1";
}
