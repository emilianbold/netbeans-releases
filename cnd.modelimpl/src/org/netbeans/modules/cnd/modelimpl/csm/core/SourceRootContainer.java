/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alexander Simon
 */
public class SourceRootContainer {
    private Map<String,Integer> projectRoots = new ConcurrentHashMap<String,Integer>();
    
    public SourceRootContainer() {
    }
    
    public boolean isMySource(String includePath){
        return projectRoots.containsKey(includePath);
    }
    
    public void fixFolder(String path){
        projectRoots.put(path,new Integer(Integer.MAX_VALUE/2));
    }
    
    public void addSources(List<NativeFileItem> items){
        for( NativeFileItem nativeFileItem : items ) {
            addFile(nativeFileItem.getFile());
        }
    }
    
    public void addFile(File file){
        File parentFile = FileUtil.normalizeFile(file).getParentFile();
        String path = parentFile.getAbsolutePath();
        addPath(path);
        String canonicalPath;
        try {
            canonicalPath = parentFile.getCanonicalPath();
            if (!path.equals(canonicalPath)) {
                addPath(canonicalPath);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void addPath(final String path) {
        Integer integer = projectRoots.get(path);
        if (integer == null){
            projectRoots.put(path,new Integer(1));
        } else {
            projectRoots.put(path, new Integer(integer.intValue()+1));
        }
    }
    
    public void removeSources(List<NativeFileItem> items){
        for( NativeFileItem nativeFileItem : items ) {
            removeFile(nativeFileItem.getFile());
        }
    }
    
    public void removeFile(File file){
        String path = FileUtil.normalizeFile(file).getParent();
        Integer integer = projectRoots.get(path);
        if (integer != null){
            if (integer.intValue()>1) {
                projectRoots.put(path, new Integer(integer.intValue()-1));
            } else {
                projectRoots.remove(path);
            }
        }
    }
}
