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
package org.netbeans.modules.java.j2seplatform.libraries;


import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;

import java.util.List;
import java.net.URL;




public class J2SELibraryClassPathProvider implements ClassPathProvider {

    public ClassPath findClassPath(FileObject file, String type) {
        assert file != null;
        Library[] libraries;
        Library ll = this.getLastUsedLibrary(file);
        if (ll != null) {
            libraries = new Library[] {ll};
        }
        else {
            libraries = LibraryManager.getDefault().getLibraries();
        }
        for (int i=0; i< libraries.length; i++) {
            if (J2SELibraryTypeProvider.LIBRARY_TYPE.equalsIgnoreCase(libraries[i].getType())) {
                List resources = libraries[i].getContent (J2SELibraryTypeProvider.VOLUME_TYPE_SRC);
                ClassPath sourcePath = ClassPathSupport.createClassPath((URL[]) resources.toArray(new URL[resources.size()]));
                FileObject root;
                if ((root = sourcePath.findOwnerRoot(file))!=null) {
                    this.setLastUsedLibrary(root,libraries[i]);
                    if (ClassPath.SOURCE.equals(type)) {
                        return sourcePath;
                    }
                    else if (ClassPath.COMPILE.equals(type)) {
                        resources = libraries[i].getContent(J2SELibraryTypeProvider.VOLUME_TYPE_CLASSPATH);
                        return ClassPathSupport.createClassPath((URL[]) resources.toArray(new URL[resources.size()]));
                    }
                    else if (ClassPath.BOOT.equals(type)) {
                        return JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
                    }
                    else {
                        break;
                    }
                }
            }
        }
        return null;
    }


    private synchronized Library getLastUsedLibrary (FileObject fo) {
        if (this.lastUsedRoot != null && FileUtil.isParentOf(this.lastUsedRoot,fo)) {
            return this.lastUsedLibrary;
        }
        else {
            return null;
        }
    }

    private synchronized void setLastUsedLibrary (FileObject root, Library lib) {
        this.lastUsedRoot = root;
        this.lastUsedLibrary = lib;
    }

    private FileObject lastUsedRoot;
    private Library lastUsedLibrary;
}
