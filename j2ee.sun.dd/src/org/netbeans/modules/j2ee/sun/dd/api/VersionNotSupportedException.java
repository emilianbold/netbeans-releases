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

package org.netbeans.modules.j2ee.sun.dd.api;

import java.util.ResourceBundle;
/**
 * Exception for cases when specific Servletx.x specification doesn't support the specific method.
 *
 * @author  Milan Kuchtiak
 */
public class VersionNotSupportedException extends java.lang.Exception {
    private String version;
    
    /**
     * Constructor for VersionNotSupportedException
     * 
     * @param version specific version of Servlet Spec. e.g."2.4"
     * @param message exception message
     */
    public VersionNotSupportedException(String version, String message) {
        super(message);
        this.version=version;
    }
    /**
     * Constructor for VersionNotSupportedException
     * 
     * @param version specific version of Servlet Spec. e.g."2.4"
     */
    public VersionNotSupportedException(String version) {
        super(ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/dd/api/Bundle").getString("MSG_versionNotSupported") + version );
        this.version=version;
    }
    /**
     * Returns the version of deployment descriptor that caused this exception.
     * 
     * @return string specifying the DD version e.g. "2.4"
     */    
    public String getVersion() {
        return version;
    }
}
