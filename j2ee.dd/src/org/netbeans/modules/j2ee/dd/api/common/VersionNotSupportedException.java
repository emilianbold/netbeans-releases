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

package org.netbeans.modules.j2ee.dd.api.common;

import org.openide.util.NbBundle;
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
        super(NbBundle.getMessage(VersionNotSupportedException.class,"MSG_versionNotSupported",version));
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
