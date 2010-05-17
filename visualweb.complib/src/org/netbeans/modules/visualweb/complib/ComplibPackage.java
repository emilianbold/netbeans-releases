/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.visualweb.complib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.netbeans.modules.visualweb.complib.Complib.InitialPaletteFolder;
import org.netbeans.modules.visualweb.complib.api.ComplibException;

/**
 * Represents an unexpanded complib package file.
 * 
 * @author Edwin Goei
 */
public class ComplibPackage {

    /**
     * ClassLoader used to load l10n resources from a *.complib file
     * 
     * @author Edwin Goei
     */
    private static class ResourceClassLoader extends URLClassLoader {

        public ResourceClassLoader(URL packageFileUrl) {
            super(new URL[0], ResourceClassLoader.class.getClassLoader());
            addURL(packageFileUrl);
        }
    }

    /**
     * ClassLoader used to load resources referenced in package file metadata such as the manifest
     * and initial-palette config file. These resources can be accessed before or after a package is
     * expanded.
     */
    private ResourceClassLoader resourceClassLoader;

    private ComplibManifest complibManifest;

    /** Points to jar file */
    private File packageFile;

    /**
     * This constructor partially validates the complib package file. A successful return indicates
     * a valid package file.
     * 
     * @param packageFile
     * @throws ComplibException
     *             if complib file is not valid
     * @throws IOException
     */
    public ComplibPackage(File packageFile) throws ComplibException, IOException {
        File freedJarFile = IdeUtil.freeJarFile(packageFile);

        JarFile jarFile = new JarFile(freedJarFile);
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            // Construct an appropriate error message
            String message = freedJarFile.equals(packageFile) ? "" : "' copied from '"
                    + packageFile; // NOI18N
            throw new ComplibException("File '" + freedJarFile + message
                    + "' must contain a complib manifest."); // NOI18N
        }

        // Init resource ClassLoader to get localized complib package resources
        URL packageFileUrl = freedJarFile.toURI().toURL();
        resourceClassLoader = new ResourceClassLoader(packageFileUrl);

        complibManifest = ComplibManifest.getInstance(manifest, resourceClassLoader);
        jarFile.close();

        this.packageFile = freedJarFile;
    }

    public ComplibManifest getManifest() {
        return complibManifest;
    }

    /**
     * Method does not throw a checked exception. Used by UI.
     * 
     * @return
     */
    public List<InitialPaletteFolder> getInitialPaletteFolders() {
        return complibManifest.getInitialPalette();
    }

    /**
     * Returns the localized Title if it has been localized
     * 
     * @return
     */
    public String getTitle() {
        return complibManifest.getTitle();
    }

    public Complib.Identifier getIdentifer() {
        return complibManifest.getIdentifier();
    }

    File getPackageFile() {
        return packageFile;
    }
}
