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

package org.netbeans.modules.java.platform.classpath;


import java.util.Collections;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;


public class PlatformClassPathProvider implements ClassPathProvider {



    /** Creates a new instance of PlatformClassPathProvider */
    public PlatformClassPathProvider() {
    }
    
    
    public ClassPath findClassPath(FileObject fo, String type) {
        if (fo == null || type == null) {
            throw new IllegalArgumentException();
        }
        JavaPlatform lp = this.getLastUsedPlatform(fo);
        JavaPlatform[] platforms;
        if (lp != null) {
            platforms = new JavaPlatform[] {lp};
        }
        else {
            JavaPlatformManager manager = JavaPlatformManager.getDefault();
            platforms = manager.getInstalledPlatforms();
        }
        for (int i=0; i<platforms.length; i++) {
            ClassPath bootClassPath = platforms[i].getBootstrapLibraries();
            ClassPath libraryPath = platforms[i].getStandardLibraries();
            ClassPath sourcePath = platforms[i].getSourceFolders();
            FileObject root = null;
            if (ClassPath.SOURCE.equals(type) && sourcePath != null &&
                (root = sourcePath.findOwnerRoot(fo))!=null) {
                this.setLastUsedPlatform (root,platforms[i]);
                return sourcePath;
            }
            else if (ClassPath.BOOT.equals(type) &&
                    ((bootClassPath != null && (root = bootClassPath.findOwnerRoot (fo))!=null) ||
                    (sourcePath != null && (root = sourcePath.findOwnerRoot(fo)) != null) ||
                    (libraryPath != null && (root = libraryPath.findOwnerRoot(fo))!=null))) {
                this.setLastUsedPlatform (root,platforms[i]);
                return bootClassPath;
            }
            else if (ClassPath.COMPILE.equals(type)) {
                if (libraryPath != null && (root = libraryPath.findOwnerRoot(fo))!=null) {
                    this.setLastUsedPlatform (root,platforms[i]);
                    return libraryPath;
                }
                else if ((bootClassPath != null && (root = bootClassPath.findOwnerRoot (fo))!=null) ||
                    (sourcePath != null && (root = sourcePath.findOwnerRoot(fo)) != null)) {
                    return this.getEmptyClassPath ();
                }
            }
        }
        return null;
    }

    private synchronized ClassPath getEmptyClassPath () {
        if (this.emptyCp == null ) {
            this.emptyCp = ClassPathSupport.createClassPath(Collections.EMPTY_LIST);
        }
        return this.emptyCp;
    }

    private synchronized void setLastUsedPlatform (FileObject root, JavaPlatform platform) {
        this.lastUsedRoot = root;
        this.lastUsedPlatform = platform;
    }

    private synchronized JavaPlatform getLastUsedPlatform (FileObject file) {
        if (this.lastUsedRoot != null && FileUtil.isParentOf(this.lastUsedRoot,file)) {
            return lastUsedPlatform;
        }
        else {
            return null;
        }
    }

    private FileObject lastUsedRoot;
    private JavaPlatform lastUsedPlatform;
    private ClassPath emptyCp;
}
