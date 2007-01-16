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

package org.netbeans.modules.refactoring.spi.impl;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author  Jan Becicka
 */

public class Util {

    //copy - paste programming
    //http://ant.netbeans.org/source/browse/ant/src-bridge/org/apache/tools/ant/module/bridge/impl/BridgeImpl.java.diff?r1=1.15&r2=1.16
    //http:/java.netbeans.org/source/browse/java/javacore/src/org/netbeans/modules/javacore/Util.java    
    //http://core.netbeans.org/source/browse/core/ui/src/org/netbeans/core/ui/MenuWarmUpTask.java
    //http://core.netbeans.org/source/browse/core/src/org/netbeans/core/actions/RefreshAllFilesystemsAction.java
    //http://java.netbeans.org/source/browse/java/api/src/org/netbeans/api/java/classpath/ClassPath.java
        
    private static FileSystem[] fileSystems;

    private static FileSystem[] getFileSystems() {
        if (fileSystems != null) {
            return fileSystems;
        }
        File[] roots = File.listRoots();
        Set allRoots = new LinkedHashSet();
        assert roots != null && roots.length > 0 : "Could not list file roots"; // NOI18N
        
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            FileObject random = FileUtil.toFileObject(root);
            if (random == null) continue;
            
            FileSystem fs;
            try {
                fs = random.getFileSystem();
                allRoots.add(fs);
                
                /*Because there is MasterFileSystem impl. that provides conversion to FileObject for all File.listRoots
                (except floppy drives and empty CD). Then there is useless to convert all roots into FileObjects including
                net drives that might cause performance regression.
                */
                
                if (fs != null) {
                    break;
                }
            } catch (FileStateInvalidException e) {
                throw new AssertionError(e);
            }
        }
        FileSystem[] retVal = new FileSystem [allRoots.size()];
        allRoots.toArray(retVal);
        assert retVal.length > 0 : "Could not get any filesystem"; // NOI18N
        
        return fileSystems = retVal;
    }
    
    /**
     * Adds given listener to all FileSystems
     * @param l listener to add
     */    
    public static void addFileSystemsListener(FileChangeListener l) {
        FileSystem[] fileSystems = getFileSystems();
        for (int i=0; i<fileSystems.length; i++) {
            fileSystems[i].addFileChangeListener(l);
        }
    }
    
    /**
     * Removes given listener to all FileSystems
     * @param l listener to remove
     */    
    public static void removeFileSystemsListener(FileChangeListener l) {
        FileSystem[] fileSystems = getFileSystems();
        for (int i=0; i<fileSystems.length; i++) {
            fileSystems[i].removeFileChangeListener(l);
        }
    }
}