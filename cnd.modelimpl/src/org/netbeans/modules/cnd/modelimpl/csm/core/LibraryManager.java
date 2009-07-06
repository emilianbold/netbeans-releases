/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 * Artificial libraries manager.
 * Manage auto ctrated libraries (artificial libraries) for included files.
 *
 *
 * @author Alexander Simon
 */
public final class LibraryManager {

    private static final LibraryManager instance = new LibraryManager();

    public static LibraryManager getInstance() {
        return instance;
    }

    private LibraryManager() {
    }
    private final Map<String, LibraryEntry> librariesEntries = new ConcurrentHashMap<String, LibraryEntry>();
    private static final class Lock {}
    private final Object lock = new Lock();

    public void shutdown(){
        librariesEntries.clear();
    }
    
    /**
     * Returns collection of artificial libraries used in project
     */
    public List<LibProjectImpl> getLibraries(ProjectImpl project) {
        List<LibProjectImpl> res = new ArrayList<LibProjectImpl>();
        CsmUID<CsmProject> projectUid = project.getUID();
        for (LibraryEntry entry : librariesEntries.values()) {
            if (entry.containsProject(projectUid)) {
                LibProjectImpl lib = (LibProjectImpl) entry.getLibrary().getObject();
                if (lib != null) {
                    res.add(lib);
                }
            }
        }
        return res;
    }

    /**
     * Returns collection uids of artificial libraries used in project
     */
    public Collection<CsmUID<CsmProject>> getLirariesKeys(CsmUID<CsmProject> projectUid) {
        List<CsmUID<CsmProject>> res = new ArrayList<CsmUID<CsmProject>>();
        for (LibraryEntry entry : librariesEntries.values()) {
            if (entry.containsProject(projectUid)) {
                res.add(entry.getLibrary());
            }
        }
        return res;
    }

    private void trace(String where, FileImpl curFile, ResolvedPath resolvedPath, ProjectBase res, ProjectBase start) {
        System.out.println("Resolved Path " + resolvedPath.getPath()); //NOI18N
        System.out.println("    start project " + start); //NOI18N
        System.out.println("    found in " + where + " " + res); //NOI18N
        System.out.println("    included from " + curFile); //NOI18N
        System.out.println("    file from project " + curFile.getProject()); //NOI18N
        for (CsmProject prj : start.getLibraries()) {
            System.out.println("    search lib " + prj); //NOI18N
        }
    }

    /**
     * Find project for resolved file.
     * Search for project in proroject, dependancies, artificial libraries.
     * If search is false then method creates artificial library or returns base project.
     */
    public ProjectBase resolveFileProjectOnInclude(ProjectBase baseProject, FileImpl curFile, ResolvedPath resolvedPath) {
        String absPath = resolvedPath.getPath().toString();
        File searchFor = new File(absPath);
        ProjectBase res = searchInProjectFiles(baseProject, searchFor);
        if (res != null) {
            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                trace("Projects", curFile, resolvedPath, res, baseProject);//NOI18N
            }
            return res;
        }
        String folder = CndFileUtils.normalizeAbsolutePath(resolvedPath.getFolder().toString());
        res = searchInProjectRoots(baseProject, getPathToFolder(folder, absPath));
        if (res != null) {
            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                trace("Projects roots", curFile, resolvedPath, res, baseProject);//NOI18N
            }
            return res;
        }
        res = searchInProjectFilesArtificial(baseProject, searchFor);
        if (res != null) {
            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                trace("Libraries", curFile, resolvedPath, res, baseProject);//NOI18N
            }
            return res;
        }
        res = searchInProjectRootsArtificial(baseProject, getPathToFolder(folder, absPath));
        if (res == null) {
            if (resolvedPath.isDefaultSearchPath()) {
                res = curFile.getProjectImpl(true);
                if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                    trace("Base Project as Default Search Path", curFile, resolvedPath, res, baseProject);//NOI18N
                }
            } else if (!baseProject.isArtificial()) {
                res = getLibrary((ProjectImpl) baseProject, folder);
                if (res == null) {
                    if (CndUtils.isDebugMode()) {
                        trace("Not created library for folder " + folder, curFile, resolvedPath, res, baseProject); //NOI18N
                    }
                    res = baseProject;
                }
                if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                    trace("Library for folder " + folder, curFile, resolvedPath, res, baseProject); //NOI18N
                }
            } else {
                res = baseProject;
                if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                    trace("Base Project", curFile, resolvedPath, res, baseProject);//NOI18N
                }
            }
        } else {
            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                trace("Libraries roots", curFile, resolvedPath, res, baseProject);//NOI18N
            }
        }
        return res;
    }

    private List<String> getPathToFolder(String folder, String path) {
        List<String> res = new ArrayList<String>(3);
        res.add(folder);
        if (path.startsWith(folder)) {
            File file = CndFileUtils.normalizeFile(new File(path));
            while (file != null) {
                String dir = file.getParent();
                if (folder.equals(dir) || !dir.startsWith(folder)) {
                    break;
                }
                res.add(dir);
                if (res.size() == 3) {
                    break;
                }
                file = file.getParentFile();
            }
        }
        return res;
    }

    private ProjectBase searchInProjectFiles(ProjectBase baseProject, File searchFor) {
        return searchInProjectFiles(baseProject, searchFor, new HashSet<ProjectBase>());
    }

    private ProjectBase searchInProjectFiles(ProjectBase baseProject, File searchFor, Set<ProjectBase> set) {
        if (set.contains(baseProject)) {
            return null;
        }
        set.add(baseProject);
        baseProject.ensureFilesCreated();
        FileImpl file = baseProject.getFile(searchFor, true);
        if (file != null) {
            return baseProject;
        }
        List<CsmProject> libraries = baseProject.getLibraries();
        int size = libraries.size();
        for (int i = 0; i < size; i++) {
            CsmProject prj = libraries.get(i);
            if (prj.isArtificial()) {
                break;
            }
            ((ProjectBase) prj).ensureFilesCreated();
            ProjectBase res = searchInProjectFiles((ProjectBase) prj, searchFor, set);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    private ProjectBase searchInProjectFilesArtificial(ProjectBase baseProject, File searchFor) {
        List<CsmProject> libraries = baseProject.getLibraries();
        int size = libraries.size();
        for (int i = 0; i < size; i++) {
            CsmProject prj = libraries.get(i);
            if (prj.isArtificial()) {
                ((ProjectBase) prj).ensureFilesCreated();
                ProjectBase res = searchInProjectFiles((ProjectBase) prj, searchFor);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private ProjectBase searchInProjectRoots(ProjectBase baseProject, List<String> folders) {
        return searchInProjectRoots(baseProject, folders, new HashSet<ProjectBase>());
    }

    private ProjectBase searchInProjectRoots(ProjectBase baseProject, List<String> folders, HashSet<ProjectBase> set) {
        if (set.contains(baseProject)) {
            return null;
        }
        set.add(baseProject);
        int folderSize = folders.size();
        for (int i = 0; i < folderSize; i++) {
            String folder = folders.get(i);
            if (baseProject.isMySource(folder)) {
                return baseProject;
            }
        }
        List<CsmProject> libraries = baseProject.getLibraries();
        int size = libraries.size();
        for (int i = 0; i < size; i++) {
            CsmProject prj = libraries.get(i);
            if (prj.isArtificial()) {
                break;
            }
            ProjectBase res = searchInProjectRoots((ProjectBase) prj, folders, set);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    private ProjectBase searchInProjectRootsArtificial(ProjectBase baseProject, List<String> folders) {
        List<CsmProject> libraries = baseProject.getLibraries();
        int size = libraries.size();
        for (int i = 0; i < size; i++) {
            CsmProject prj = libraries.get(i);
            if (prj.isArtificial()) {
                ProjectBase res = searchInProjectRoots((ProjectBase) prj, folders);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private LibProjectImpl getLibrary(ProjectImpl project, String folder) {
        CsmUID<CsmProject> projectUid = project.getUID();
        LibraryEntry entry = librariesEntries.get(folder);
        if (entry == null) {
            entry = getOrCreateLibrary(project.getModel(), folder);
        }
        if (!entry.containsProject(projectUid)) {
            entry.addProject(projectUid);
        }
        return (LibProjectImpl) entry.getLibrary().getObject();
    }

    private LibraryEntry getOrCreateLibrary(final ModelImpl model, final String includeFolder) {
        LibraryEntry entry = librariesEntries.get(includeFolder);
        if (entry == null) {
            boolean needFire = false;
            synchronized (lock) {
                entry = librariesEntries.get(includeFolder);
                if (entry == null) {
                    entry = new LibraryEntry(includeFolder);
                    librariesEntries.put(includeFolder, entry);
                    needFire = true;
                }
            }
            if (needFire) {
                final LibraryEntry passEntry = entry;
                ModelImpl.instance().enqueueModelTask(new Runnable() {

                    public void run() {
                        ListenersImpl.getImpl().fireProjectOpened((ProjectBase) passEntry.getLibrary().getObject());
                    }
                }, "postponed library opened " + includeFolder); // NOI18N
            }
        }
        return entry;
    }

    public void onProjectPropertyChanged(CsmUID<CsmProject> project) {
        for (LibraryEntry entry : librariesEntries.values()) {
            entry.removeProject(project);
        }
    }

    /**
     * Close unused artificial libraries.
     */
    public void onProjectClose(CsmUID<CsmProject> project) {
        List<LibraryEntry> toClose = new ArrayList<LibraryEntry>();
        for (LibraryEntry entry : librariesEntries.values()) {
            entry.removeProject(project);
            if (entry.isEmpty()) {
                toClose.add(entry);
            }
        }
        if (toClose.size() > 0) {
            for (LibraryEntry entry : toClose) {
                librariesEntries.remove(entry.getFolder());
            }
        }
        closeLibraries(toClose);
    }

    private void closeLibraries(Collection<LibraryEntry> entries) {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
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
        Set<CharSequence> res = new HashSet<CharSequence>();
        for (LibraryEntry entry : librariesEntries.values()) {
            if (entry.containsProject(project)) {
                res.add(FilePathCache.getManager().getString(entry.getFolder()));
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
        Collection<CharSequence> res = PersistentUtils.readCollectionStrings(aStream, FilePathCache.getManager());
        for (CharSequence folder : res) {
            LibraryEntry entry = getOrCreateLibrary(model, folder.toString());
            entry.addProject(project);
        }
    }

    private static class LibraryEntry {

        private String folder;
        private CsmUID<CsmProject> libraryUID;
        private ConcurrentMap<CsmUID<CsmProject>, Boolean> dependentProjects;

        private LibraryEntry(String folder) {
            this.folder = folder;
            dependentProjects = new ConcurrentHashMap<CsmUID<CsmProject>, Boolean>();
        }

        private String getFolder() {
            return folder;
        }

        private CsmUID<CsmProject> getLibrary() {
            if (libraryUID == null) {
                createUID();
            }
            return libraryUID;
        }

        private synchronized void createUID() {
            if (libraryUID == null) {
                ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
                LibProjectImpl library = LibProjectImpl.createInstance(model, folder);
                libraryUID = library.getUID();
            }
        }

        private boolean isEmpty() {
            return dependentProjects.size() == 0;
        }

        private boolean containsProject(CsmUID<CsmProject> project) {
            return dependentProjects.containsKey(project);
        }

        private void addProject(CsmUID<CsmProject> project) {
            dependentProjects.put(project, Boolean.TRUE);
        }

        private void removeProject(CsmUID<CsmProject> project) {
            dependentProjects.remove(project);
        }
    }
}
