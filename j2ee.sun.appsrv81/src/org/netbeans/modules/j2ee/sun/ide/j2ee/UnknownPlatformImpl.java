// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.ImageUtilities;
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
        return ImageUtilities.loadImage("org/netbeans/modules/j2ee/sun/ide/resources/ServerInstanceIcon.png"); // NOI18N;
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
