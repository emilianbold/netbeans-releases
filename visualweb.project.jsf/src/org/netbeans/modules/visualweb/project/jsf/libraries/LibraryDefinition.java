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

package org.netbeans.modules.visualweb.project.jsf.libraries;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Po-Ting Wu
 */
public abstract class LibraryDefinition {
    public static URL toResourceURL(URL resource) {
        if (FileUtil.isArchiveFile(resource)) {
            resource = FileUtil.getArchiveRoot(resource);
        } else if (!resource.toExternalForm().endsWith("/")) {
            try {
                resource = new URL(resource.toExternalForm()+"/");
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
            }
        }
        return resource;
    }

    /**
     * Remove an existing library definition
     * @param name Internal name of the existing library from which the library definition file name
     * will be derived.
     * @throws IOException if the library definition does not already exist
     */
    public static void remove(String name) throws IOException {
        LibraryManager manager = LibraryManager.getDefault();
        Library[] libs = manager.getLibraries();
        
        for (int i = 0; i < libs.length; i++) {
            if (libs[i].getName().equals(name)) {
                manager.removeLibrary(libs[i]);
                return;
            }
        }
    }
}
