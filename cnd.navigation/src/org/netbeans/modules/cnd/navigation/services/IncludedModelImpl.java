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

package org.netbeans.modules.cnd.navigation.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;

/**
 *
 * @author Alexander Simon
 */
/*package-local*/ class IncludedModelImpl implements IncludedModel {
    private Map<CsmFile,Set<CsmFile>> map;
    
    /** Creates a new instance of IncludedModel */
    public IncludedModelImpl(CsmFile file, boolean whoIncludes, boolean plain, boolean recursive) {
        if (whoIncludes) {
            map = buildWhoIncludes(file);
        } else {
            map = buildWhoIsIncluded(file);
        }
        if (!recursive) {
            Set<CsmFile> result = map.get(file);
            if (result == null){
                result = new HashSet<CsmFile>();
            }
            map = new HashMap<CsmFile,Set<CsmFile>>();
            map.put(file,result);
        }
        if (plain) {
            Set<CsmFile> result = new HashSet<CsmFile>();
            gatherList(file, result, map);
            map = new HashMap<CsmFile,Set<CsmFile>>();
            map.put(file,result);
        }
    }
    
    private void gatherList(CsmFile file, Set<CsmFile> result, Map<CsmFile,Set<CsmFile>> map){
        Set<CsmFile> set = map.get(file);
        if (set == null) {
            return;
        }
        for(CsmFile f : set){
            if (!result.contains(f)) {
                result.add(f);
                gatherList(f, result, map);
            }
        }
    }
    
    public Map<CsmFile,Set<CsmFile>> getModel(){
        return map;
    }
    
    private Map<CsmFile,Set<CsmFile>> buildWhoIncludes(CsmFile file){
        HashMap<CsmFile,Set<CsmFile>> aMap = new HashMap<CsmFile,Set<CsmFile>>();
        for(CsmProject prj :CsmModelAccessor.getModel().projects()){
            for(CsmFile f : prj.getSourceFiles()){
                buildWhoIncludes(f, aMap);
            }
            for(CsmFile f : prj.getHeaderFiles()){
                buildWhoIncludes(f, aMap);
            }
            for (CsmProject lib : prj.getLibraries()){
                for(CsmFile f : lib.getSourceFiles()){
                    buildWhoIncludes(f, aMap);
                }
                for(CsmFile f : lib.getHeaderFiles()){
                    buildWhoIncludes(f, aMap);
                }
            }
        }
        return aMap;
    }
    
    private void buildWhoIncludes(CsmFile file, Map<CsmFile,Set<CsmFile>> map){
        for(CsmInclude include : file.getIncludes()){
            CsmFile included = include.getIncludeFile();
            if (included != null){
                Set<CsmFile> back = map.get(included);
                if (back == null){
                    back = new HashSet<CsmFile>();
                    map.put(included,back);
                }
                if (!back.contains(file)) {
                    back.add(file);
                    buildWhoIncludes(included, map);
                }
            }
        }
    }

    private Map<CsmFile, Set<CsmFile>> buildWhoIsIncluded(CsmFile file) {
        HashMap<CsmFile,Set<CsmFile>> aMap = new HashMap<CsmFile,Set<CsmFile>>();
        buildWhoIsIncluded(file, aMap);
        return aMap;
    }

    private void buildWhoIsIncluded(CsmFile file, Map<CsmFile,Set<CsmFile>> map){
        Set<CsmFile> includes = map.get(file);
        if (includes != null){
            return;
        }
        includes = new HashSet<CsmFile>();
        map.put(file, includes);
        for(CsmInclude include : file.getIncludes()){
            CsmFile included = include.getIncludeFile();
            if (included != null){
                includes.add(included);
                buildWhoIsIncluded(included, map);
            }
        }
    }
}
