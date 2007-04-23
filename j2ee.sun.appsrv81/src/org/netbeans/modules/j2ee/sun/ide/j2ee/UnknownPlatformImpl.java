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

import java.awt.Image;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/** Placeholder for an unknown platform
 *
 * @author vkraemer
 */
class UnknownPlatformImpl extends J2eePlatformImpl {
    
    private final File root;
    
    /** Creates a new instance of UnknownPlatformImpl */
    public UnknownPlatformImpl(File root) {
        this.root = root;
    }

    public LibraryImplementation[] getLibraries() {
        return new LibraryImplementation[0];
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UnknownPlatformImpl.class, "MSG_UNKNOWN_PLATFORM",root);
    }

    public Image getIcon() {
        return Utilities.loadImage("org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.png"); // NOI18N;
    }

    public File[] getPlatformRoots() {
        return new File[] { root } ;
    }

    public File[] getToolClasspathEntries(String toolName) {
        return new File[0];
    }

    public boolean isToolSupported(String toolName) {
        return false;
    }

    public Set getSupportedSpecVersions() {
        return new HashSet();
    }

    public Set getSupportedModuleTypes() {
        return Collections.EMPTY_SET;
    }

    public Set getSupportedJavaPlatformVersions() {
        return Collections.EMPTY_SET;
    }

    public JavaPlatform getJavaPlatform() {
        return null;
    }
    
}
