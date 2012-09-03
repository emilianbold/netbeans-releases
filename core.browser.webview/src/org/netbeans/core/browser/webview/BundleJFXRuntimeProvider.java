/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.core.browser.webview;

import java.io.File;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provides the location of JavaFX runtime bundled with NetBeans (if any).
 * 
 * @author S. Aubrecht
 */
@ServiceProvider(service=WebBrowserImplProvider.JFXRuntimePathProvider.class, position=11000)
public class BundleJFXRuntimeProvider implements WebBrowserImplProvider.JFXRuntimePathProvider {

    private static final String PATH_32BIT = "i586"; //NOI18N
    private static final String PATH_64BIT = "x64"; //NOI18N

    @Override
    public String getJFXRuntimePath() {
        String nbDir = System.getProperty( "netbeans.home"); //NOI18N
        if( null == nbDir || nbDir.isEmpty() )
            return null;
        File nbInstallDir = new File(nbDir).getParentFile();
        File bundleFolder = new File( new File( new File( nbInstallDir, "ide" ), "javafxrt" ), is32Bit() ? PATH_32BIT : PATH_64BIT ); //NOI18N
        if( bundleFolder.exists() && bundleFolder.isDirectory() ) {
            File rtJar = new File( new File( bundleFolder, "lib" ), "jfxrt.jar" ); //NOI18N
            if( rtJar.exists() && rtJar.isFile() && rtJar.canRead() ) {
                return bundleFolder.getAbsolutePath();
            }
        }
        return null;
    }

    private static boolean is32Bit() {
        String osarch = System.getProperty("os.arch"); //NOI18N
        for (String x : new String[]{"x86", "i386", "i486", "i586", "i686"}) { //NOI18N
            if (x.equals(osarch)) {
                return true;
            }
        }
        return false;
    }
}
