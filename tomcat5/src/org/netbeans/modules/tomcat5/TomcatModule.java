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

import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;

/** Dummy implementation of target for Tomcat 5 server
 *
 * @author  Radim Kubacki
 */
public class TomcatModule implements TargetModuleID {
    
    private Target target;
    
    private String path;
    
    public TomcatModule (Target target, String path) {
        this.target = target;
        this.path = path;
    }
    
    public TargetModuleID[] getChildTargetModuleID () {
        return null;
    }
    
    public String getModuleID () {
        return getWebURL ();
    }
    
    public TargetModuleID getParentTargetModuleID () {
        return null;
    }
    
    public Target getTarget () {
        return target;
    }
    
    /** Context root path of this module. */
    public String getPath () {
        return path;
    }
    
    // PENDING
    public String getWebURL () {
        try {
            return new java.net.URL ("http", "localhost", 8080, path).toExternalForm ();
        }
        catch (java.net.MalformedURLException ex) {
            return "http://localhost:8080"+path;    // NOI18N
        }
    }
    
}
