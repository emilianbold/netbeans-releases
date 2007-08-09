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
    private final String tomcatPath;
    private final String docRoot;

    public TomcatModule (Target target, String path) {
        this(target, path, null);
    }

    public TomcatModule (Target target, String path, String docRoot) {
        this.target = (TomcatTarget) target;
        /*
         * Tomcat ROOT context path bug hack.
         */
        this.path = path;
        this.tomcatPath = "".equals(path) ? "/" : path; // NOI18N
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
        return tomcatPath;
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
