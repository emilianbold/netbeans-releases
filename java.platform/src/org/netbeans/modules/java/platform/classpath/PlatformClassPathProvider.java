/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.platform.classpath;

import java.util.List;
import org.openide.filesystems.FileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
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
        JavaPlatformManager manager = JavaPlatformManager.getDefault();
        JavaPlatform[] platforms = manager.getInstalledPlatforms();
        for (int i=0; i<platforms.length; i++) {
            ClassPath bootClassPath = platforms[i].getBootstrapLibraries();
            ClassPath libraryPath = platforms[i].getStandardLibraries();
            ClassPath sourcePath = null;
            List sourceFolder = platforms[i].getSourceFolders();
            if (sourceFolder.size()>0) {
                sourcePath = ClassPathSupport.createClassPath((FileObject[])
                sourceFolder.toArray (new FileObject[sourceFolder.size()]));
            }

            if (ClassPath.SOURCE.equals(type) && sourcePath != null && sourcePath.contains(fo)) {
                return sourcePath;
            }
            else if (ClassPath.BOOT.equals(type) &&
                    ((bootClassPath != null && bootClassPath.contains (fo)) ||
                    (sourcePath != null && sourcePath.contains(fo)) ||
                    (libraryPath != null && libraryPath.contains(fo)))) {
                return bootClassPath;
            }
            else if (ClassPath.COMPILE.equals(type) &&
                    libraryPath != null && libraryPath.contains(fo)) {
                return libraryPath;
            }
        }
        return null;
    }
}
