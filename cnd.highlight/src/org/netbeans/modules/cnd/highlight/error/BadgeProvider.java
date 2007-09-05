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

package org.netbeans.modules.cnd.highlight.error;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class BadgeProvider {
    private static BadgeProvider myInstance = new BadgeProvider();
    
    private Storage storage = new Storage();
    private Object listLock = new Object();
    
    private BadgeProvider() {
    }
    
    public static BadgeProvider getInstance(){
        return myInstance;
    }
    
    public void addInvalidFile(CsmFile file){
        synchronized (listLock){
            for (CsmInclude incl : file.getIncludes()){
                if (incl.getIncludeFile() == null) {
                    if (!storage.contains(file)){
                        storage.add(file);
                        setProjectbadge(file);
                    }
                    return;
                }
            }
            if (storage.contains(file)){
                storage.remove(file);
                setProjectbadge(file);
            }
        }
    }
    
    private void setProjectbadge(CsmFile file){
        (new BrokenProjectService()).stateChanged(null);
    }
    
    private void setProjectbadge(CsmProject csmProject){
        (new BrokenProjectService()).stateChanged(null);
    }
    
    private void setProjectbadge(){
        (new BrokenProjectService()).stateChanged(null);
    }
    
    public void removeInvalidFile(CsmFile file) {
        synchronized (listLock){
            if (storage.contains(file)){
                storage.remove(file);
                setProjectbadge(file);
            }
        }
    }
    
    public void removeAllProjects(){
        synchronized (listLock){
            storage.clear();
            setProjectbadge();
        }
    }
    
    public void removeProject(CsmProject project){
        synchronized (listLock){
            boolean stateChanged = storage.contains(project);
            storage.remove(project);
            if (stateChanged) {
                setProjectbadge(project);
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(BadgeProvider.class,id);
    }
    
    boolean isBroken(NativeProject project) {
        synchronized (listLock){
            return storage.contains(project);
        }
    }
    
    public Set<CsmUID<CsmFile>> getFailedFiles(NativeProject nativeProject) {
        synchronized (listLock) {
            return new HashSet<CsmUID<CsmFile>>(storage.getFiles(nativeProject));
        }
    }
    
    public boolean hasFailedFiles(NativeProject nativeProject) {
        synchronized (listLock){
            return storage.contains(nativeProject);
        }
    }
    
    private static class Storage {
	
        private Map<CsmProject,Set<CsmUID<CsmFile>>> wrongFiles = new HashMap<CsmProject,Set<CsmUID<CsmFile>>>();
        private Map<CsmProject,NativeProject> nativeProjects = new HashMap<CsmProject, NativeProject>();
        
        public Set<CsmUID<CsmFile>> getFiles(CsmProject project){
            return wrongFiles.get(project);
        }
        
        public Set<CsmUID<CsmFile>> getFiles(NativeProject project) {
            for(Map.Entry<CsmProject,NativeProject> entry : nativeProjects.entrySet()){
                if (project == entry.getValue()){
                    return getFiles(entry.getKey());
                }
            }
            return null;
        }
        
        public void clear(){
            wrongFiles.clear();
            nativeProjects.clear();
        }
        
        public void remove(CsmProject project){
            wrongFiles.remove(project);
            nativeProjects.remove(project);
        }
        
        public void remove(CsmFile file) {
            CsmProject project = file.getProject();
            if (project != null) {
                Set<CsmUID<CsmFile>> set = getFiles(project);
                if (set != null){
                    set.remove(file.getUID());
                }
            }
        }
        
        public void add(CsmFile file) {
            CsmProject project = file.getProject();
            if (project != null) {
                Set<CsmUID<CsmFile>> set = getFiles(project);
                if (set == null){
                    Object id = project.getPlatformProject();
                    if (id instanceof NativeProject) {
                        set = new HashSet<CsmUID<CsmFile>>();
                        wrongFiles.put(project,set);
                        nativeProjects.put(project, (NativeProject) id);
                    }
                }
                if (set != null) {
                    set.add(file.getUID());
                }
            }
        }
        
        public boolean contains(CsmProject project){
            Set<CsmUID<CsmFile>> set = getFiles(project);
            return set != null && set.size() > 0;
        }
        
        public boolean contains(NativeProject project){
            Set<CsmUID<CsmFile>> set = getFiles(project);
            return set != null && set.size() > 0;
        }
        
        public boolean contains(CsmFile file){
            CsmProject project = file.getProject();
            if (project != null) {
                Set<CsmUID<CsmFile>> set = getFiles(project);
                if (set != null){
                    return set.contains(file.getUID());
                }
            }
            return false;
        }
    }
}
