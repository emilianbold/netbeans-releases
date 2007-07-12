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
import java.util.HashMap;
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
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Alexander Simon
 */
public final class LibraryManager {
    private static LibraryManager instance = new LibraryManager();
    
    public static LibraryManager getInsatnce(){
        return instance;
    }
    
    private final Map<CsmUID<CsmProject>, Set<CsmUID<CsmProject>>> librariesUids = new ConcurrentHashMap<CsmUID<CsmProject>, Set<CsmUID<CsmProject>>>();
    private final Map<String, CsmUID<CsmProject>> librariesNames = new ConcurrentHashMap<String, CsmUID<CsmProject>>();
    private Object lock = new Object();
    
    private LibraryManager() {
    }
    
    public Collection<LibProjectImpl> getLiraries(ProjectImpl project){
        List<LibProjectImpl> res = new ArrayList<LibProjectImpl>();
        for(Map.Entry<CsmUID<CsmProject>, Set<CsmUID<CsmProject>>> entry : librariesUids.entrySet()){
            LibProjectImpl library = (LibProjectImpl)entry.getKey().getObject();
            Set<CsmUID<CsmProject>> set = entry.getValue();
            if (set.contains(project.getUID())) {
                res.add(library);
            }
        }
        return res;
    }
    
    public Collection<CsmUID<CsmProject>> getLirariesKeys(CsmUID<CsmProject> project){
        List<CsmUID<CsmProject>> res = new ArrayList<CsmUID<CsmProject>>();
        for(Map.Entry<CsmUID<CsmProject>, Set<CsmUID<CsmProject>>> entry : librariesUids.entrySet()){
            CsmUID<CsmProject> library = entry.getKey();
            Set<CsmUID<CsmProject>> set = entry.getValue();
            if (set.contains(project)) {
                res.add(library);
            }
        }
        return res;
    }

    public ProjectBase resolveFileProjectOnInclude(ProjectBase baseProject, FileImpl curFile, ResolvedPath resolvedPath) {
        String absPath = resolvedPath.getPath();
        File searchFor = new File(absPath);
        ProjectBase res = searchInProjectFiles(baseProject, searchFor);
        if (res != null) {
            return res;
        }
        String folder = FileUtil.normalizeFile(new File(resolvedPath.getFolder())).getAbsolutePath();
        res = searchInProjectRoots(baseProject, folder);
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
    
    
    private LibProjectImpl getLibrary(ProjectImpl project, String folder){
        for(Map.Entry<CsmUID<CsmProject>, Set<CsmUID<CsmProject>>> entry : librariesUids.entrySet()){
            LibProjectImpl library = (LibProjectImpl)entry.getKey().getObject();
            if (library != null) {
                if (library.getPath().equals(folder)){
                    Set<CsmUID<CsmProject>> set = entry.getValue();
                    if (!set.contains(project.getUID())){
                        set.add(project.getUID());
                    }
                    return library;
                }
            } else {
                System.err.println("Cannot find library by key "+entry.getKey());
            }
        }
        return addLibrary(project, getOrCreateLibrary((ModelImpl)project.getModel(), folder));
    }
    
    private synchronized LibProjectImpl addLibrary(ProjectImpl project, LibProjectImpl library){
        Set<CsmUID<CsmProject>> set = librariesUids.get(library.getUID());
        set.add(project.getUID());
        return library;
    }
    
    /*package-local*/ void onProjectClose(CsmUID<CsmProject> project){
        List<CsmUID<CsmProject>> toClose = new ArrayList<CsmUID<CsmProject>>();
        for(Map.Entry<CsmUID<CsmProject>, Set<CsmUID<CsmProject>>> entry : librariesUids.entrySet()){
            CsmUID<CsmProject> library = entry.getKey();
            Set<CsmUID<CsmProject>> set = entry.getValue();
            if (set.contains(project)) {
                set.remove(project);
            }
            if (set.isEmpty()){
                toClose.add(library);
            }
        }
        if (toClose.size()>0){
            for (CsmUID<CsmProject> library : toClose){
                librariesUids.remove(library);
            }
            List<String> toCloseFolders = new ArrayList<String>();
            for (Map.Entry<String, CsmUID<CsmProject>> entry : librariesNames.entrySet()){
                if (toClose.contains(entry.getValue())) {
                    toCloseFolders.add(entry.getKey());
                }
            }
            for (String folder : toCloseFolders){
                librariesNames.remove(folder);
            }
        }
        closeLibraries(toClose);
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
    
    private ProjectBase searchInProjectRoots(ProjectBase baseProject, String folder){
        if (baseProject.isMySource(folder)) {
            return baseProject;
        }
        for (CsmProject prj : baseProject.getLibraries()) {
            ProjectBase res = searchInProjectRoots((ProjectBase)prj, folder);
            if (res != null) {
                return res;
            }
        }
        return null;
    }
    
    /*package-local*/ LibProjectImpl getOrCreateLibrary(ModelImpl model, String includeFolder) {
        LibProjectImpl library = null;
        assert TraceFlags.USE_REPOSITORY;
        CsmUID<CsmProject> uid = librariesNames.get(includeFolder);
        boolean needFire = false;
        if (uid == null) {
            synchronized (lock) {
                uid = librariesNames.get(includeFolder);
                if( uid == null ) {
                    library = LibProjectImpl.createInstance(model, includeFolder);
                    uid = UIDCsmConverter.projectToUID(library);
                    librariesNames.put(includeFolder,  uid);
                    librariesUids.put(uid, Collections.synchronizedSet(new HashSet<CsmUID<CsmProject>>()));
                    needFire = true;
                }
            }
            if (needFire){
                model.fireProjectOpened(library);
            }
        }
        if (library == null && uid != null) {
            library = (LibProjectImpl)UIDCsmConverter.UIDtoProject(uid);
            assert library != null || uid == null  : "null object for UID " + uid;
        }
        return library;
    }
    

    private void closeLibraries(Collection<CsmUID<CsmProject>> libUIDs) {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        assert TraceFlags.USE_REPOSITORY;
        for (CsmUID<CsmProject> uid : libUIDs) {
            assert uid != null;
            ProjectBase lib = (ProjectBase) UIDCsmConverter.UIDtoProject(uid);
            assert lib != null : "Null project for UID " + uid;
            model.disposeProject(lib, ! TraceFlags.PERSISTENT_REPOSITORY);
        }
    }

    /*package-local*/ void write(CsmUID<CsmProject> project, DataOutput aStream) throws IOException {
        assert aStream != null;
        Set<String> res = new HashSet<String>();
        for(Map.Entry<String, CsmUID<CsmProject>> entry : librariesNames.entrySet()){
            String folder = entry.getKey();
            CsmUID<CsmProject> library = entry.getValue();
            Set<CsmUID<CsmProject>> set = librariesUids.get(library);
            if (set != null && set.contains(project)) {
                res.add(folder);
            }
        }
        PersistentUtils.writeCollectionStrings(res, aStream);
    }

    /*package-local*/ void read(CsmUID<CsmProject> project, DataInput aStream) throws IOException {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        assert aStream != null;
        Collection<String> res = PersistentUtils.readCollectionStrings(aStream,null);
        for(String folder : res){
            LibProjectImpl libImpl = getOrCreateLibrary(model, folder);
            Set<CsmUID<CsmProject>> set = librariesUids.get(libImpl.getUID());
            if (set == null) {
                set = Collections.synchronizedSet(new HashSet<CsmUID<CsmProject>>());
            }
            set.add(project);
        }
    }
}
