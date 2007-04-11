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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;

/**
 * Storage for include graph.
 * @author Alexander Simon
 */
/*package-local*/ class GraphContainer {
    
    /** Creates a new instance of GraphContainer */
    public GraphContainer() {
    }
    
    /**
     * save file graph.
     * called after (re)parse.
     */
    public void putFile(CsmFile master){
        for (CsmInclude include : master.getIncludes()){
            if (putInclude(include, master)) {
                putFile(include.getIncludeFile(), master);
            }
        }
    }
    
    /**
     * remove file graph.
     * called before (re)parse, remove, delelete.
     */
    public void removeFile(CsmFile master){
        for (CsmInclude include : master.getIncludes()){
            if (removeInclude(include, master)) {
                removeFile(include.getIncludeFile(), master);
            }
        }
    }
    
    /**
     * gets all included files.
     */
    public Set<CsmUID<CsmFile>> getIncludedFiles(CsmFile start){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(start);
        synchronized (graph){
            getIncludedFiles(res, keyFrom);
        }
        return res;
    }

    /**
     * gets all master files that include this file.
     * Master file is a compilation unit, i.e. source file or free include file.
     */
    public Set<CsmFile> getParentMasterFiles(CsmFile start){
        Set<CsmUID<CsmFile>> res = new HashSet<CsmUID<CsmFile>>();
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(start);
        synchronized (graph){
            getParentMasterFiles(res, keyTo);
        }
        Set<CsmFile> res2= new HashSet<CsmFile>();
        for(CsmUID<CsmFile> uid : res) {
            res2.add(UIDCsmConverter.UIDtoFile(uid));
        }
        return res2;
    }

    
    private void putFile(CsmFile from, CsmFile master){
        for (CsmInclude include : from.getIncludes()){
            if (putInclude(include, master)) {
                putFile(include.getIncludeFile(), master);
            }
        }
    }
    
    private void removeFile(CsmFile from, CsmFile master){
        for (CsmInclude include : master.getIncludes()){
            if (removeInclude(include, master)) {
                removeFile(include.getIncludeFile(), master);
            }
        }
    }
    
    private boolean putInclude(CsmInclude include, CsmFile master){
        CsmFile to = include.getIncludeFile();
        if (to == null) {
            return false;
        }
        CsmFile from = include.getContainingFile();
        CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(from);
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(to);
        CsmUID<CsmFile> keyMaster = UIDCsmConverter.fileToUID(master);
        synchronized (graph){
            Map<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>> map = graph.get(keyFrom);
            if (map == null) {
                map = new HashMap<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>>();
                graph.put(keyFrom, map);
            }
            Set<CsmUID<CsmFile>> set = map.get(keyTo);
            if (set == null) {
                set = new HashSet<CsmUID<CsmFile>>();
                map.put(keyTo,set);
            }
            if (set.contains(keyMaster)){
                return false;
            }
            set.add(keyMaster);
        }
        return true;
    }
    
    private boolean removeInclude(CsmInclude include, CsmFile master){
        CsmFile to = include.getIncludeFile();
        if (to == null) {
            return false;
        }
        CsmFile from = include.getContainingFile();
        CsmUID<CsmFile> keyFrom = UIDCsmConverter.fileToUID(from);
        CsmUID<CsmFile> keyTo = UIDCsmConverter.fileToUID(to);
        CsmUID<CsmFile> keyMaster = UIDCsmConverter.fileToUID(master);
        synchronized (graph){
            Map<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>> map = graph.get(keyFrom);
            if (map == null) {
                return false;
            }
            Set<CsmUID<CsmFile>> set = map.get(keyTo);
            if (set == null) {
                return false;
            }
            if (!set.contains(keyMaster)){
                return false;
            }
            set.remove(keyMaster);
            if (set.size() == 0){
                map.remove(keyTo);
            }
            if (map.size() == 0){
                graph.remove(keyFrom);
            }
        }
        return true;
    }

    /*
     * method called in synchronized block
     */
    private void getIncludedFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyFrom){
        Map<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>> map = graph.get(keyFrom);
        if (map != null) {
            for(Map.Entry<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>> entry : map.entrySet()){
                if (!res.contains(entry.getKey())){
                    res.add(entry.getKey());
                    getIncludedFiles(res, entry.getKey());
                }
            }
        }
    }

    /*
     * method called in synchronized block
     */
    private void getParentMasterFiles(Set<CsmUID<CsmFile>> res, CsmUID<CsmFile> keyTo){
        for (Map.Entry<CsmUID<CsmFile>, Map<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>>> entry : graph.entrySet()){
            Set<CsmUID<CsmFile>> masters = entry.getValue().get(keyTo);
            if (masters != null){
                res.addAll(masters);
            }
        }
    }

    // from -> to (in master)
    private Map<CsmUID<CsmFile>, Map<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>>> graph =
            new HashMap<CsmUID<CsmFile>, Map<CsmUID<CsmFile>, Set<CsmUID<CsmFile>>>>();
}
