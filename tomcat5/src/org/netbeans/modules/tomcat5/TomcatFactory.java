/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.tomcat5;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.openide.util.NbBundle;

/** Factory capable to create DeploymentManager that can deploy to 
 * Tomcat 5.
 *
 * @author  Radim Kubacki
 */
public class TomcatFactory implements DeploymentFactory {
    
    private static TomcatFactory instance;
    
    /** Factory method to create DeploymentFactory for Tomcat.
     */
    public static synchronized TomcatFactory create() {
        if (instance == null) {
            instance = new TomcatFactory ();
        }
        return instance;
    }
    
    /** Creates a new instance of TomcatFactory */
    public TomcatFactory() {
    }
    
    public DeploymentManager getDeploymentManager(String str, String str1, String str2) 
    throws DeploymentManagerCreationException {
        // PENDING
        return null;
    }
    
    public DeploymentManager getDisconnectedDeploymentManager(String str) 
    throws DeploymentManagerCreationException {
        // PENDING
        return null;
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage (TomcatFactory.class, "LBL_TomcatFactory");
    }
    
    public String getProductVersion() {
        // PENDING
        return null;
    }
    
    public boolean handlesURI(String str) {
        return false;
    }
    
}
