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
        //this is our JSR88 factory lazy init, only when needed via layer.      
         PluginProperties.configureDefaultServerInstance();
         facadeDF =  new org.netbeans.modules.j2ee.sun.ide.dm.SunDeploymentFactory();
        
        return facadeDF;
    }
       
    
    public void close() {
        
    }
    public void uninstalled() {
        
    }
    
    
    
}
