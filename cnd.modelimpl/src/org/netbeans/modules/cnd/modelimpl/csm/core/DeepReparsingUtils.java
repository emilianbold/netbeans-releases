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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.csm.core.GraphContainer.ParentFiles;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.openide.filesystems.FileObject;

/**
 * Reparse dependant files.
 * @author Alexander Simon
 */
public final class DeepReparsingUtils {

    private DeepReparsingUtils() {
    }

    /**
     * Reparse including/included files at fileImpl content changed.
     */
    public static void reparseOnEdit(FileImpl fileImpl, ProjectBase project) {
        reparseOnEdit(fileImpl, project, true);
    }

    /**
     * Reparse including/included files at fileImpl content changed.
     */
    public static void reparseOnEdit(FileImpl fileImpl, ProjectBase project, boolean scheduleParsing) {
        ParentFiles top = project.getGraph().getTopParentFiles(fileImpl);
        Set<CsmFile> topParents = top.getCompilationUnits();
        Set<CsmFile> parents = top.getParentFiles();
        if (topParents.size() > 0) {
            fileImpl.clearStateCache();
            Set<CsmFile> coherence = project.getGraph().getCoherenceFiles(fileImpl).getCoherenceFiles();
            for (CsmFile file : coherence) {
                if (topParents.contains(file)) {
                    ((FileImpl)file).clearStateCache();
                } else if (parents.contains(file)) {
                    ((FileImpl)file).clearStateCache();
                    invalidateFileAndPreprocState(project, file);
                } else {
                    invalidateFileAndPreprocState(project, file);
                }
            }
            if (scheduleParsing) {
                // coherence already invalidated, pass empty set
                addToReparse(project, topParents, new HashSet<CsmFile>(0), false);
            }
        } else {
            if (scheduleParsing) {
                ParserQueue.instance().add(fileImpl, project.getPreprocHandler(fileImpl.getBuffer().getFile()).getState(), ParserQueue.Position.HEAD);
            }
        }
    }

    static void reparseOnEdit(Collection<FileImpl> toReparse, ProjectBase project, boolean scheduleParsing) {
        Set<CsmUID<CsmFile>> topParents = new HashSet<CsmUID<CsmFile>>();
        Set<CsmUID<CsmFile>> parents = new HashSet<CsmUID<CsmFile>>();
        Set<CsmUID<CsmFile>> coherence = new HashSet<CsmUID<CsmFile>>();
        for(FileImpl fileImpl: toReparse){
            ParentFiles top = project.getGraph().getTopParentFiles(fileImpl);
            Set<CsmUID<CsmFile>> units = top.getCompilationUnitsUids();
            if (units.size() > 0) {
                topParents.addAll(units);
                parents.addAll(top.getParentFilesUids());
                coherence.addAll(project.getGraph().getCoherenceFiles(fileImpl).getCoherenceFilesUids());
            } else {
                if (scheduleParsing) {
                    ParserQueue.instance().add(fileImpl, project.getPreprocHandler(fileImpl.getBuffer().getFile()).getState(), ParserQueue.Position.HEAD);
                }
            }
        }
        if (topParents.size() > 0) {
            Set<CsmFile> topParentsImpl = new HashSet<CsmFile>();
            for (CsmUID<CsmFile> file : coherence) {
                FileImpl fileImpl = (FileImpl) UIDCsmConverter.UIDtoFile(file);
                if (fileImpl != null) {
                    if (topParents.contains(file)) {
                        topParentsImpl.add(fileImpl);
                        fileImpl.clearStateCache();
                    } else if (parents.contains(file)){
                        fileImpl.clearStateCache();
                        invalidateFileAndPreprocState(project, fileImpl);
                    } else {
                        invalidateFileAndPreprocState(project, fileImpl);
                    }
                }
            }
            if (scheduleParsing) {
                // coherence already invalidated, pass empty set
                addToReparse(project, topParentsImpl, new HashSet<CsmFile>(0), false);
            }
        }
    }


    /**
     * Reparse including/included files at file properties changed.
     */
    public static void reparseOnPropertyChanged(NativeFileItem nativeFile, ProjectBase project) {
        FileImpl file = project.getFile(nativeFile.getFile(), false);
        if (file == null) {
            return;
        }
        file.clearStateCache();
        Set<CsmFile> top = project.getGraph().getTopParentFiles(file).getCompilationUnits();
        Set<CsmFile> coherence = project.getGraph().getIncludedFiles(file);
        Set<CsmFile> coherenceLibrary = new HashSet<CsmFile>();
        for (CsmFile parent : coherence) {
            if (!top.contains(parent)) {
                CsmProject parentPoject = parent.getProject();
                if (project.equals(parentPoject)) {
                    invalidateFileAndPreprocState(project, parent);
                } else {
                    coherenceLibrary.add(parent);
                }
            }
        }
        if (!TraceFlags.DEEP_REPARSING_OPTIMISTIC) {
            gatherCoherenceLibrary(coherenceLibrary);
            invalidateFileAndPreprocState(coherenceLibrary);
        }
        addToReparse(project, nativeFile, file);
        top.remove(file);
        // coherence already invalidated, pass empty set
        addToReparse(project, top, new HashSet<CsmFile>(0), false);
    }

    private static void gatherCoherenceLibrary(Set<CsmFile> coherenceLibrary) {
        while (true) {
            Set<CsmFile> newCoherenceLibrary = new HashSet<CsmFile>();
            for (CsmFile coherence : coherenceLibrary) {
                newCoherenceLibrary.add(coherence);
                ProjectBase coherenceProject = (ProjectBase) coherence.getProject();
                newCoherenceLibrary.addAll(coherenceProject.getGraph().getIncludedFiles(coherence));
            }
            if (newCoherenceLibrary.size() == coherenceLibrary.size()) {
                return;
            }
            coherenceLibrary.addAll(newCoherenceLibrary);
        }
    }

    /**
     * Reparse including/included files at file properties changed.
     */
    public static void reparseOnPropertyChanged(Collection<NativeFileItem> items, ProjectBase project) {
        try {
            ParserQueue.instance().onStartAddingProjectFiles(project);
            Map<FileImpl, NativeFileItem> pairs = new HashMap<FileImpl, NativeFileItem>();
            Set<CsmFile> top = new HashSet<CsmFile>();
            Set<CsmFile> coherence = new HashSet<CsmFile>();
            Set<CsmFile> coherenceLibrary = new HashSet<CsmFile>();
            for (NativeFileItem item : items) {
                if (project.acceptNativeItem(item)) {
                    FileImpl file = project.getFile(item.getFile(), false);
                    if (file != null) {
                        file.clearStateCache();
                        pairs.put(file, item);
                        top.addAll(project.getGraph().getTopParentFiles(file).getCompilationUnits());
                        coherence.addAll(project.getGraph().getIncludedFiles(file));
                    }
                }
            }
            for (CsmFile parent : coherence) {
                if (!top.contains(parent)) {
                    CsmProject parentPoject = parent.getProject();
                    if (project.equals(parentPoject)) {
                        invalidateFileAndPreprocState(project, parent);
                    } else {
                        coherenceLibrary.add(parent);
                    }
                }
            }
            if (!TraceFlags.DEEP_REPARSING_OPTIMISTIC) {
                gatherCoherenceLibrary(coherenceLibrary);
                invalidateFileAndPreprocState(coherenceLibrary);
            }
            for (CsmFile parent : top) {
                if (parent.getProject() == project) {
                    FileImpl parentImpl = (FileImpl) parent;
                    if (pairs.containsKey(parentImpl)) {
                        NativeFileItem item = pairs.get(parentImpl);
                        addToReparse(project, item, parentImpl);
                    } else {
                        addToReparse(project, parentImpl, true);
                    }
                }
            }
        } catch (Exception e) {
            DiagnosticExceptoins.register(e);
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(project);
        }
    }

    /**
     * Reparse included files at file added.
     */
    public static void reparseOnAdded(NativeFileItem nativeFile, ProjectBase project) {
        reparseOnAdded(nativeFile.getFile().getName(), project);
    }

    /**
     * Reparse included files at file added.
     */
    public static void reparseOnAdded(FileObject addedFile, ProjectBase project) {
        reparseOnAdded(addedFile.getNameExt(), project);
    }

    private static void reparseOnAdded(String name, ProjectBase project) {
        Set<CsmFile> resolved = new HashSet<CsmFile>();
        for (CsmFile file : project.getAllFiles()) {
            for (CsmInclude incl : file.getIncludes()) {
                if (incl.getIncludeName().toString().endsWith(name)/* && incl.getIncludeFile() == null*/) {
                    resolved.add(file);
                    break;
                }
            }
        }
        if (resolved.size() > 0) {
            Set<CsmFile> top = new HashSet<CsmFile>();
            Set<CsmFile> coherence = new HashSet<CsmFile>();
            for (CsmFile file : resolved) {
                top.addAll(project.getGraph().getTopParentFiles(file).getCompilationUnits());
                coherence.add(file);
                coherence.addAll(project.getGraph().getIncludedFiles(file));
            }
            addToReparse(project, top, coherence, true);
        }
    }

    static void reparseOnAdded(List<NativeFileItem> toReparse, ProjectBase project) {
        Set<String> names = new HashSet<String>();
        for (NativeFileItem item : toReparse) {
            names.add(item.getFile().getName());
        }
        Set<CsmFile> resolved = new HashSet<CsmFile>();
        for (CsmFile file : project.getSourceFiles()) {
            findResolved(names, resolved, file);
        }
        for (CsmFile file : project.getHeaderFiles()) {
            findResolved(names, resolved, file);
        }
        if (resolved.size() > 0) {
            Set<CsmFile> top = new HashSet<CsmFile>();
            Set<CsmFile> coherence = new HashSet<CsmFile>();
            for (CsmFile file : resolved) {
                top.addAll(project.getGraph().getTopParentFiles(file).getCompilationUnits());
                coherence.addAll(project.getGraph().getIncludedFiles(file));
            }
            addToReparse(project, top, coherence, true);
        }
    }

    private static void findResolved(final Set<String> names, final Set<CsmFile> resolved, final CsmFile file) {
        for (CsmInclude incl : file.getIncludes()) {
            String name = incl.getIncludeName().toString();
            int i = Math.max(name.lastIndexOf('\\'), name.lastIndexOf('/'));
            if (i > 0) {
                name = name.substring(i);
            }
            if (names.contains(name)) {
                resolved.add(file);
                break;
            }
        }
    }

    /**
     * Reparse including/included files at file removed.
     */
    public static void reparseOnRemoved(FileImpl impl, ProjectBase project) {
        Set<CsmFile> topParents = project.getGraph().getTopParentFiles(impl).getCompilationUnits();
        Set<CsmFile> coherence = project.getGraph().getCoherenceFiles(impl).getCoherenceFiles();
        project.getGraph().removeFile(impl);
        topParents.remove(impl);
        coherence.remove(impl);
        addToReparse(project, topParents, coherence, false);
    }

    static void reparseOnRemoved(List<FileImpl> toReparse, ProjectBase project) {
        Set<CsmFile> topParents = new HashSet<CsmFile>();
        Set<CsmFile> coherence = new HashSet<CsmFile>();
        for (FileImpl impl : toReparse) {
            topParents.addAll(project.getGraph().getTopParentFiles(impl).getCompilationUnits());
            coherence.addAll(project.getGraph().getCoherenceFiles(impl).getCoherenceFiles());
            project.getGraph().removeFile(impl);
            topParents.remove(impl);
            coherence.remove(impl);
        }
        addToReparse(project, topParents, coherence, false);
    }

    private static void addToReparse(final ProjectBase project, final Set<CsmFile> topParents, final Set<CsmFile> coherence, boolean invalidateCache) {
        for (CsmFile incl : coherence) {
            if (!topParents.contains(incl)) {
                invalidateFileAndPreprocState(project, incl);
            }
        }
        try {
            // send notifications
            ParserQueue.instance().onStartAddingProjectFiles(project);
            for (CsmFile parent : topParents) {
                if (parent.getProject() == project) {
                    FileImpl parentImpl = (FileImpl) parent;
                    addToReparse(project, parentImpl, invalidateCache);
                }
            }
        } catch (Exception e) {
            DiagnosticExceptoins.register(e);
        } finally {
            // send notifications
            ParserQueue.instance().onEndAddingProjectFiles(project);
        }
    }

    private static void addToReparse(final ProjectBase project, final FileImpl parentImpl, final boolean invalidateCache) {
        parentImpl.markReparseNeeded(invalidateCache);
        ParserQueue.instance().add(parentImpl, project.getPreprocHandler(parentImpl.getBuffer().getFile()).getState(), ParserQueue.Position.HEAD);
        if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
            System.out.println("Add file to reparse " + parentImpl.getAbsolutePath()); // NOI18N
        }
    }

    private static void addToReparse(final ProjectBase project, final NativeFileItem nativeFile, final FileImpl file) {
        if (nativeFile.getFile() != null) {
            file.markReparseNeeded(true);
            APTPreprocHandler.State state = project.setChangedFileState(nativeFile);
            if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
                System.out.println("Add file to reparse " + file.getAbsolutePath()); // NOI18N
            }
            ParserQueue.instance().add(file, state, ParserQueue.Position.HEAD);
        } else {
            assert false;
        }
    }

    private static void invalidateFileAndPreprocState(final ProjectBase project, final CsmFile parent) {
        if (parent.getProject() == project) {
            FileImpl parentImpl = (FileImpl) parent;
            parentImpl.clearStateCache();
            project.invalidatePreprocState(parentImpl.getBuffer().getFile());
            parentImpl.markReparseNeeded(false);
            if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
                System.out.println("Invalidate file to reparse " + parent.getAbsolutePath()); // NOI18N
            }
        }
    }

    private static void invalidateFileAndPreprocState(Set<CsmFile> coherenceLibrary) {
        for (CsmFile parent : coherenceLibrary) {
            CsmProject project = parent.getProject();
            if (project instanceof ProjectBase) {
                FileImpl parentImpl = (FileImpl) parent;
                parentImpl.clearStateCache();
                ((ProjectBase) project).invalidatePreprocState(parentImpl.getBuffer().getFile());
                parentImpl.markReparseNeeded(false);
                if (TraceFlags.USE_DEEP_REPARSING_TRACE) {
                    System.out.println("Invalidate file to reparse " + parent.getAbsolutePath()); // NOI18N
                }
            }
        }
    }
}
