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
public final class TomcatModule implements TargetModuleID {
    
    private TomcatTarget target;
    
    private final String path;
    private final String docRoot;
    
    public TomcatModule (Target target, String path) {
        this(target, path, null);
    }
    
    public TomcatModule (Target target, String path, String docRoot) {
        this.target = (TomcatTarget) target;
        // Tomcat ROOT context path bug hack
        this.path = "".equals(path) ? "/" : path; // NOI18N
        this.docRoot = docRoot;
    }
    
    public String getDocRoot () {
        return docRoot;
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
    
//    // PENDING
    public String getWebURL () {
        return target.getServerUri () + path.replaceAll(" ", "%20");
//        try {
//            return new java.net.URL ("http", "localhost", target.getPort (), path).toExternalForm ();
//        }
//        catch (java.net.MalformedURLException ex) {
//            return "http://localhost:8080"+path;    // NOI18N
//        }
    }
    
    public String toString () {
        return getModuleID ();
    }
}
