/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.platform.queries;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.event.ChangeListener;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.ErrorManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceForBinaryQuery;

/**
 * This implementation of the SourceForBinaryQueryImplementation
 * provides sources for the active platform and project libraries
 */

public class PlatformSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    public PlatformSourceForBinaryQuery () {
    }

    /**
     * Tries to locate the source root for given classpath root.
     * @param binaryRoot the URL of a classpath root (platform supports file and jar protocol)
     * @return FileObject[], never returns null
     */
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        JavaPlatformManager mgr = JavaPlatformManager.getDefault();
        JavaPlatform[] platforms = mgr.getInstalledPlatforms();
        for (int i=0; i< platforms.length; i++) {
            ClassPath cp = platforms[i].getBootstrapLibraries();
            for (Iterator it = cp.entries().iterator(); it.hasNext();) {
                ClassPath.Entry entry = (ClassPath.Entry) it.next();
                if (entry.getURL().equals (binaryRoot)) {
                    return new Result (platforms[i]);                    
                }
            }
        }
        return null;
    }
    
    private static class Result implements SourceForBinaryQuery.Result {
                        
        private JavaPlatform platform;
                        
        public Result (JavaPlatform platform) {
            this.platform = platform;
        }
                        
        public FileObject[] getRoots () {
            ClassPath sources = this.platform.getSourceFolders();
            return sources.getRoots();
        }
                        
        public void addChangeListener (ChangeListener l) {
            //TODO: Implement this
        }
                        
        public void removeChangeListener (ChangeListener l) {
            //TODO: Implement this
        }
    }
}
