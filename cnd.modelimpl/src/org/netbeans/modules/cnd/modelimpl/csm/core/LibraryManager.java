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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.openide.filesystems.FileUtil;

/**
 * Artificial libraries manager.
 * Manage auto ctrated libraries (artificial libraries) for included files.
 *
 *
 * @author Alexander Simon
 */
public final class LibraryManager {
    private static LibraryManager instance = new LibraryManager();
    
    public static LibraryManager getInsatnce(){
        return instance;
    }
    
    private LibraryManager() {
    }
    
    private final Map<String, LibraryEntry> librariesEntries = new ConcurrentHashMap<String, LibraryEntry>();
    private Object lock = new Object();
    
    /**
     * Returns collection of artificial libraries used in project
     */
    public Collection<LibProjectImpl> getLiraries(ProjectImpl project){
        List<LibProjectImpl> res = new ArrayList<LibProjectImpl>();
        CsmUID<CsmProject> projectUid = project.getUID();
        for(LibraryEntry entry : librariesEntries.values()){
            if (entry.containsProject(projectUid)){
                res.add((LibProjectImpl)entry.getLibrary().getObject());
            }
        }
        return res;
    }
    
    /**
     * Returns collection uids of artificial libraries used in project
     */
    public Collection<CsmUID<CsmProject>> getLirariesKeys(CsmUID<CsmProject> projectUid){
        List<CsmUID<CsmProject>> res = new ArrayList<CsmUID<CsmProject>>();
        for(LibraryEntry entry : librariesEntries.values()){
            if (entry.containsProject(projectUid)){
                res.add(entry.getLibrary());
            }
        }
        return res;
    }
    
    /**
     * Find project for resolved file.
     * Search for project in proroject, dependancies, artificial libraries.
     * If search is false then method creates artificial library or returns base project.
     */
    public ProjectBase resolveFileProjectOnInclude(ProjectBase baseProject, FileImpl curFile, ResolvedPath resolvedPath) {
        String absPath = resolvedPath.getPath();
        File searchFor = new File(absPath);
        ProjectBase res = searchInProjectFiles(baseProject, searchFor);
        if (res != null) {
            return res;
        }
        String folder = FileUtil.normalizeFile(new File(resolvedPath.getFolder())).getAbsolutePath();
        res = searchInProjectRoots(baseProject, getPathToFolder(folder, resolvedPath.getPath()));
        if (res == null) {
            if (resolvedPath.isDefaultSearchPath()) {
                res = baseProject;
            } else if (!baseProject.isArtificial()) {
                res = getLibrary((ProjectImpl)baseProject, folder);
            } else {
                res = baseProject;
            }
        }
        return res;
    }
    
    private List<String> getPathToFolder(String folder, String path){
        List<String> res = new ArrayList<String>(3);
        res.add(folder);
        if (path.startsWith(folder)){
            File file = FileUtil.normalizeFile(new File(path));
            while(file != null){
                String dir = file.getParent();
                if(folder.equals(dir) || !dir.startsWith(folder)){
                    break;
                }
                res.add(dir);
                if (res.size()==3){
                    break;
                }
                file = file.getParentFile();
            }
        }
        return res;
    }
    
    private ProjectBase searchInProjectFiles(ProjectBase baseProject, File searchFor){
        baseProject.ensureFilesCreated();
        FileImpl file = baseProject.getFile(searchFor);
        if (file != null) {
            return baseProject;
        }
        for (CsmProject prj : baseProject.getLibraries()) {
            ((ProjectBase)prj).ensureFilesCreated();
            ProjectBase res = searchInProjectFiles((ProjectBase)prj, searchFor);
            if (res != null) {
                return res;
            }
        }
        return null;
    }
    
    private ProjectBase searchInProjectRoots(ProjectBase baseProject, List<String> folders){
        for(String folder : folders) {
            if (baseProject.isMySource(folder)) {
                return baseProject;
            }
        }
        for (CsmProject prj : baseProject.getLibraries()) {
            ProjectBase res = searchInProjectRoots((ProjectBase)prj, folders);
            if (res != null) {
                return res;
            }
        }
        return null;
    }
    
    private LibProjectImpl getLibrary(ProjectImpl project, String folder){
        CsmUID<CsmProject> projectUid = project.getUID();
        LibraryEntry entry = librariesEntries.get(folder);
        if (entry == null) {
            entry = getOrCreateLibrary(project.getModel(), folder);
        }
        if (!entry.containsProject(projectUid)){
            entry.addProject(projectUid);
        }
        return (LibProjectImpl)entry.getLibrary().getObject();
    }
    
    private LibraryEntry getOrCreateLibrary(ModelImpl model, String includeFolder) {
        assert TraceFlags.USE_REPOSITORY;
        LibraryEntry entry = librariesEntries.get(includeFolder);
        if (entry == null) {
            boolean needFire = false;
            synchronized (lock) {
                entry = librariesEntries.get(includeFolder);
                if( entry == null ) {
                    entry = new LibraryEntry(includeFolder);
                    librariesEntries.put(includeFolder, entry);
                    needFire = true;
                }
            }
            if (needFire){
                model.fireProjectOpened((ProjectBase)entry.getLibrary().getObject());
            }
        }
        return entry;
    }
    
    /**
     * Close unused artificial libraries.
     */
    /*package-local*/ void onProjectClose(CsmUID<CsmProject> project){
        List<LibraryEntry> toClose = new ArrayList<LibraryEntry>();
        for(LibraryEntry entry : librariesEntries.values()){
            entry.removeProject(project);
            if (entry.isEmpty()){
                toClose.add(entry);
            }
        }
        if (toClose.size()>0){
            for (LibraryEntry entry : toClose){
                librariesEntries.remove(entry.getFolder());
            }
        }
        closeLibraries(toClose);
    }
    
    private void closeLibraries(Collection<LibraryEntry> entries) {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        assert TraceFlags.USE_REPOSITORY;
        for (LibraryEntry entry : entries) {
            CsmUID<CsmProject> uid = entry.getLibrary();
            ProjectBase lib = (ProjectBase) uid.getObject();
            assert lib != null : "Null project for UID " + uid;
            model.disposeProject(lib);
        }
    }
    
    /**
     * Write artificial libraries for project
     */
    /*package-local*/ void writeProjectLibraries(CsmUID<CsmProject> project, DataOutput aStream) throws IOException {
        assert aStream != null;
        Set<String> res = new HashSet<String>();
        for(LibraryEntry entry : librariesEntries.values()){
            if (entry.containsProject(project)){
                res.add(entry.getFolder());
            }
        }
        PersistentUtils.writeCollectionStrings(res, aStream);
    }
    
    /**
     * Read artificial libraries for project
     */
    /*package-local*/ void readProjectLibraries(CsmUID<CsmProject> project, DataInput aStream) throws IOException {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        assert aStream != null;
        Collection<String> res = PersistentUtils.readCollectionStrings(aStream,null);
        for(String folder : res){
            LibraryEntry entry = getOrCreateLibrary(model, folder);
            entry.addProject(project);
        }
    }
    
    private static class LibraryEntry {
        private String folder;
        private CsmUID<CsmProject> libraryUID;
        private Set<CsmUID<CsmProject>> dependentProjects;

        private LibraryEntry(String folder){
            this.folder = folder;
            dependentProjects = Collections.synchronizedSet(new HashSet<CsmUID<CsmProject>>());
        }
        private String getFolder(){
            return folder;
        }
        private CsmUID<CsmProject> getLibrary(){
            if (libraryUID == null){
                createUID();
            }
            return libraryUID;
        }
        private synchronized void createUID(){
            if (libraryUID == null){
                ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
                LibProjectImpl library = LibProjectImpl.createInstance(model, folder);
                libraryUID = library.getUID();
            }
        }
        private boolean isEmpty(){
            return dependentProjects.size() == 0;
        }
        private boolean containsProject(CsmUID<CsmProject> project){
            return dependentProjects.contains(project);
        }
        private void addProject(CsmUID<CsmProject> project){
            dependentProjects.add(project);
        }
        private void removeProject(CsmUID<CsmProject> project){
            dependentProjects.remove(project);
        }
    }
}
