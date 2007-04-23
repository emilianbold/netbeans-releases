// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
// </editor-fold>

package org.netbeans.modules.j2ee.sun.ide.j2ee;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;

// TODO flesh this class out completely
/**
 *
 * @author vkraemer
 */
class PlatformImpl82 extends PlatformImpl {
    
    /** Creates a new instance of PlatformImpl82 */
    PlatformImpl82(File root, DeploymentManagerProperties dmProps) {
        super(root,dmProps);
    }
    
    public Set/*<String>*/ getSupportedJavaPlatformVersions() {
        Set versions = new HashSet();
        versions.add("1.4"); // NOI18N
        versions.add("1.5"); // NOI18N
        versions.add("1.6"); // NOI18N
        return versions;
    }
    
    /**
     * Specifies whether a tool of the given name is supported by this platform.
     *
     * @param  toolName tool's name e.g. "wscompile".
     * @return <code>true</code> if platform supports tool of the given name,
     *         <code>false</code> otherwise.
     */
    public boolean isToolSupported(String toolName) {
        if (J2eePlatform.TOOL_WSCOMPILE.equals(toolName)
        || J2eePlatform.TOOL_APP_CLIENT_RUNTIME.equals(toolName)) {
            return true;
        }
        return false;
    }
    
    /**
     * Return a list of supported J2EE specification versions. Use J2EE specification
     * versions defined in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE specification versions.
     */
    public Set/*<String>*/ getSupportedSpecVersions() {        
        return SPEC_VERSIONS;
    }
    
    
}
