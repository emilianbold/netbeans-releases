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

package org.netbeans.modules.visualweb.complib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.netbeans.modules.visualweb.complib.Complib.InitialPaletteFolder;

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
     * ClassLoader used to load resources referenced in package file metadata
     * such as the manifest and initial-palette config file. These resources can
     * be accessed before or after a package is expanded.
     */
    private ResourceClassLoader resourceClassLoader;

    private ComplibManifest complibManifest;

    /** Points to jar file */
    private File packageFile;

    /**
     * This constructor partially validates the complib package file. A
     * successful return indicates a valid package file.
     *
     * @param packageFile
     * @throws ComplibException
     *             if complib file is not valid
     * @throws IOException
     */
    public ComplibPackage(File packageFile) throws ComplibException,
            IOException {
        File freedJarFile = IdeUtil.freeJarFile(packageFile);

        JarFile jarFile = new JarFile(freedJarFile);
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            // Construct an appropriate error message
            String message = freedJarFile.equals(packageFile) ? ""
                    : "' copied from '" + packageFile; // NOI18N
            throw new ComplibException("File '" + freedJarFile + message
                    + "' must contain a complib manifest."); // NOI18N
        }

        // Init resource ClassLoader to get localized complib package resources
        URL packageFileUrl = freedJarFile.toURI().toURL();
        resourceClassLoader = new ResourceClassLoader(packageFileUrl);

        complibManifest = ComplibManifest.getInstance(manifest,
                resourceClassLoader);
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
