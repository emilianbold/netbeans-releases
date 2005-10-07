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
 * Installer.java -- synopsis
 *
 */

package org.netbeans.modules.j2ee.sun.ide;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PluginProperties;
import org.openide.modules.ModuleInstall;


public class Installer extends ModuleInstall {
    
    static DeploymentFactory facadeDF=null;
    public static final String 	ideHomeLocation = System.getProperty("netbeans.home");
    
    
    
    
    
    /** Factory method to create DeploymentFactory for s1as.
     */
    public static synchronized Object create() {
        if (facadeDF != null)
            return facadeDF;
               
        PluginProperties.configureDefaultServerInstance();
        //register the panel that will ask username password. Global IDE level...
        facadeDF =  new org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory();
        
        return facadeDF;
    }
       
    
    public void close() {
        
    }
    public void uninstalled() {
        
    }
    
    
    
}
