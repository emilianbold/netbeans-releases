/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.deployment.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import org.netbeans.modules.j2ee.deployment.common.api.SourceFileMap;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public class DefaultSourceMap extends SourceFileMap {
    /**
     * Straight file mapping service.
     * Map a distribution path to a file using distribution path as relative path to a mapping root.
     */
    private String contextName;
    private FileObject[] roots;
    private HashSet rootFiles = new HashSet();
    
    /** Creates a new instance of DefaultFileMapping */
    public DefaultSourceMap(String name, FileObject[] roots) {
        this.contextName = name;
        this.roots = roots;
        for (int i=0; i<roots.length; i++) {
            rootFiles.add(FileUtil.toFile(roots[i]));
        }
    }
    
    public String getContextName() {
        return contextName;
    }

    public FileObject[] getSourceRoots() {
        return roots;
    }
    
    public boolean add(String distributionPath, FileObject sourceFile) {
        for (int i=0; i<roots.length; i++) {
            if (sourceFile.getPath().startsWith(roots[i].getPath()))
                return true;
        }
        return false;
    }
    
    public FileObject remove(String distributionPath) {
        return null;
    }
    
    public FileObject[] findSourceFile(String distributionPath) {
        ArrayList ret = new ArrayList();
        String path = distributionPath.startsWith("/") ? distributionPath.substring(1) : distributionPath; //NOI18N
        for (int i=0; i<roots.length; i++) {
            FileObject fo = roots[i].getFileObject(path);
            if (fo != null)
                ret.add(fo);
        }
        return (FileObject[]) ret.toArray(new FileObject[ret.size()]);
    }
    
    public File getDistributionPath(FileObject sourceFile) {
        File relative = null;
        File absolute = FileUtil.toFile(sourceFile);
        while (absolute.getParentFile() != null) {
            if (relative == null) {
                relative = new File(absolute.getName());
            } else {
                relative = new File(relative, absolute.getName());
            }
            absolute = absolute.getParentFile();
            if (rootFiles.contains(absolute))
                return relative;
        }
        return null;
    }
}


