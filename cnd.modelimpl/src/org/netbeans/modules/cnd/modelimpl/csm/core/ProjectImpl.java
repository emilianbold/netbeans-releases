/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.ui.NamedOption;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.ServiceProvider;

/**
 * Project implementation
 * @author Vladimir Kvashin
 */
public final class ProjectImpl extends ProjectBase {

    private ProjectImpl(ModelImpl model, FileSystem fs, NativeProject platformProject, String name) {
        super(model, fs, platformProject, name);
    // RepositoryUtils.put(this);
    }

    public static ProjectImpl createInstance(ModelImpl model, NativeProject platformProject, String name) {
        return createInstance(model, platformProject.getFileSystem(), platformProject, name);
    }

    private static ProjectImpl createInstance(ModelImpl model, FileSystem fs, NativeProject platformProject, String name) {
        ProjectBase instance = null;
        if (TraceFlags.PERSISTENT_REPOSITORY) {
            try {
                instance = readInstance(model, fs, platformProject, name);
            } catch (Exception e) {
                // just report to console;
                // the code below will create project "from scratch"
                cleanRepository(fs, platformProject, false);
                DiagnosticExceptoins.register(e);
            }
        }
        if (instance == null) {
            instance = new ProjectImpl(model, fs, platformProject, name);
        }
        CndUtils.assertTrue(instance.getFileSystem() == fs);
        return (ProjectImpl) instance;
    }

    @Override
    protected final ParserQueue.Position getIncludedFileParserQueuePosition() {
        return ParserQueue.Position.HEAD;
    }

    public 
    @Override
    void onFileEditStart(final FileBuffer buf, NativeFileItem nativeFile) {
        if (!Utils.acceptNativeItem(nativeFile)) {
            return;
        }
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("------------------------- onFileEditSTART " + buf.getUrl()); //NOI18N
        }
        final FileImpl impl = createOrFindFileImpl(buf, nativeFile);
        if (impl != null) {
            APTDriver.invalidateAPT(buf);
            APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());
            // listener will be triggered immediately, because editor based buffer
            // will be notifies about editing event exactly after onFileEditStart
            final ChangeListener changeListener = new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    scheduleParseOnEditing(impl);
                }
            };
            synchronized (editedFiles) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("onFileEditStart: edited file " + csmFile);
                    }
                    System.err.println("onFileEditStart: current file " + impl);
                }
                // sync set buffer as well
                impl.setBuffer(buf);
                if (!editedFiles.containsKey(impl)) {
                    // register edited file
                    editedFiles.put(impl, new EditingTask(buf, changeListener));
                }
                scheduleParseOnEditing(impl);
            }
        }
    }

    public 
    @Override
    void onFileEditEnd(FileBuffer buf, NativeFileItem nativeFile, boolean undo) {
        if (!Utils.acceptNativeItem(nativeFile)) {
            return;
        }
        if (TraceFlags.DEBUG) {
            Diagnostic.trace("------------------------- onFileEditEND " + buf.getUrl()); //NOI18N
        }
        FileImpl file = getFile(buf.getAbsolutePath(), false);
        if (file != null) {
            synchronized (editedFiles) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("onFileEditEnd: edited file " + csmFile);
                    }
                    System.err.println("onFileEditEnd: " + (undo ? "undo" : "save") + " current file " + file);
                }
                EditingTask task = editedFiles.remove(file);
                if (task != null) {
                    task.cancelTask();
                } else {
                    // FixUp double file edit end on mounted files
                    return;
                }
                // sync set buffer as well
                file.setBuffer(buf);
            }
//            file.clearStateCache();
            // no need for deep parsing util call here in case of save, because it will be called as external notification change anyway
            if (undo) {
                // but we need to call in case of undo when there are no external modifications
                DeepReparsingUtils.reparseOnUndoEditedFile(this, file);
            }
        }
    }

    @Override
    public void onFileItemsPropertyChanged(List<NativeFileItem> items, boolean invalidateLibs) {
        if (!this.isValid()) {
            return;
        }
        if (items.size() > 0) {
            DeepReparsingUtils.reparseOnPropertyChanged(items, this, invalidateLibs);
        }
    }

    @Override
    public void onFileImplRemoved(Collection<FileImpl> physicallyRemoved, Collection<FileImpl> excluded) {
        try {
            Set<FileImpl> files = new HashSet<FileImpl>(physicallyRemoved);
            files.addAll(excluded);
            synchronized (editedFiles) {
                for (FileImpl impl : files) {
                    EditingTask task = editedFiles.remove(impl);
                    if (task != null) {
                        task.cancelTask();
                    }
                }
            }
            LinkedList<FileImpl> toReparse = new LinkedList<FileImpl>();
            for (FileImpl impl : files) {
                if (impl != null) {
                    NativeFileItem removedNativeFileItem = removeNativeFileItem(impl.getUID());
                    // this is analogue of synchronization if method was called from different threads,
                    // because removeNativeFileItem is thread safe and removes only once
                    if (removedNativeFileItem != null) {
                        toReparse.addLast(impl);
                        impl.dispose();
                        removeFile(impl.getAbsolutePath());
                        final FileBuffer buf = impl.getBuffer();
                        APTDriver.invalidateAPT(buf);
                        APTFileCacheManager.getInstance(buf.getFileSystem()).invalidate(buf.getAbsolutePath());
                        ParserQueue.instance().remove(impl);
                    }
                }
            }
            DeepReparsingUtils.reparseOnRemoved(toReparse, this);
        } finally {
            Notificator.instance().flush();
        }
    }

    @Override
    public void onFileItemsRemoved(List<NativeFileItem> items) {
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
//            List<FileImpl> toReparse = new ArrayList<FileImpl>();
//            for (NativeFileItem item : items) {
//                FileImpl impl = getFile(item.getAbsolutePath(), false);
//                if (impl != null) {
//                    toReparse.add(impl);
//                }
//            }
//            onFileImplRemoved(toReparse);            
            checkForRemoved();
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    @Deprecated
    @Override
    public void onFileExternalAdded(NativeFileItem nativeFile) {
        onFileAddedImpl(nativeFile, true);
    }

    @Override
    public void onFileItemRenamed(String oldPath, NativeFileItem newFileIetm) {
//        FileImpl fileImpl = getFile(oldPath, false);
//        if (fileImpl != null) {
//            onFileImplRemoved(Collections.singletonList(fileImpl));
//        }
        checkForRemoved();
        onFileAddedImpl(newFileIetm, false);
    }
    
    private NativeFileItem onFileAddedImpl(NativeFileItem nativeFile, boolean deepReparse) {
        if (Utils.acceptNativeItem(nativeFile)) {
            CndFileUtils.clearFileExistenceCache();
            try {
                //Notificator.instance().startTransaction();
                createIfNeed(nativeFile, isSourceFile(nativeFile));
                return nativeFile;
            } finally {
                //Notificator.instance().endTransaction();
                Notificator.instance().flush();
                if (deepReparse) {
                    DeepReparsingUtils.reparseOnAdded(Collections.singletonList(nativeFile), this);
                }
            }
        }
        return null;
    }

    @Override
    public void onFileItemsAdded(List<NativeFileItem> items) {
        try {
            ParserQueue.instance().onStartAddingProjectFiles(this);
            List<NativeFileItem> toReparse = new ArrayList<NativeFileItem>();
            for (NativeFileItem item : items) {
                NativeFileItem done = onFileAddedImpl(item, false);
                if (done != null) {
                    toReparse.add(done);
                }
            }
            DeepReparsingUtils.reparseOnAdded(toReparse, this);
        } finally {
            ParserQueue.instance().onEndAddingProjectFiles(this);
        }
    }

    protected 
    @Override
    void ensureChangedFilesEnqueued() {
        List<FileImpl> addToParse = new ArrayList<FileImpl>();
        synchronized (editedFiles) {
            super.ensureChangedFilesEnqueued();
            for (Iterator<CsmFile> iter = editedFiles.keySet().iterator(); iter.hasNext();) {
                FileImpl file = (FileImpl) iter.next();
                if (!file.isParsingOrParsed()) {
                    addToParse.add(file);
                }
            }
        }
        for (FileImpl file : addToParse) {
            ParserQueue.instance().add(file, getPreprocHandlersForParse(file), ParserQueue.Position.TAIL);
        }
    //N.B. don't clear list of editedFiles here.
    }

    protected 
    @Override
    boolean hasChangedFiles(CsmFile skipFile) {
        if (skipFile == null) {
            return false;
        }
        synchronized (editedFiles) {
            for (Iterator iter = editedFiles.keySet().iterator(); iter.hasNext();) {
                FileImpl file = (FileImpl) iter.next();
                if ((skipFile != file) && !file.isParsingOrParsed()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean hasEditedFiles() {
        synchronized (editedFiles) {
            return !editedFiles.isEmpty();
        }
    }

    private final static class EditingTask {
        // field is synchronized by editedFiles lock
        private RequestProcessor.Task task;
        private final ChangeListener bufListener;
        private final FileBuffer buf;
        private long lastModified = -1;

        public EditingTask(final FileBuffer buf, ChangeListener bufListener) {
            assert (bufListener != null);
            this.bufListener = bufListener;
            assert (buf != null);
            this.buf = buf;
            this.buf.addChangeListener(bufListener);
        }

        public boolean updateLastModified() {
            long lm = this.buf.lastModified();
            if (this.lastModified == lm) {
                return false;
            }
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                System.err.printf("EditingTask.updateLastModified: set lastModified from %d to %d\n", this.lastModified, lm);// NOI18N
            }
            this.lastModified = lm;
            return true;
        }
        
        public void setTask(Task task) {
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                System.err.printf("EditingTask.setTask: set new EditingTask %d for %s\n", task.hashCode(), buf.getUrl());
            }
            this.task = task;
        }

        public void cancelTask() {
            if (this.task != null) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    if (!task.isFinished()) {
                        new Exception("EditingTask.cancelTask: cancelling previous EditingTask " + task.hashCode()).printStackTrace(System.err); // NOI18N
                    } else {
                        new Exception("EditingTask.cancelTask: cancelTask where EditingTask was finished " + task.hashCode()).printStackTrace(System.err); // NOI18N
                    }
                }
                try {
                    this.task.cancel();
                } catch (Throwable ex) {
                    System.err.println("EditingTask.cancelTask: cancelled with exception:");
                    ex.printStackTrace(System.err);
                }
            }
            this.buf.removeChangeListener(bufListener);
        }

        private Task getTask() {
            return this.task;
        }
    }
    
    private final Map<CsmFile, EditingTask> editedFiles = new HashMap<CsmFile, EditingTask>();

    public 
    @Override
    ProjectBase findFileProject(CharSequence absPath, boolean waitFilesCreated) {
        ProjectBase retValue = super.findFileProject(absPath, waitFilesCreated);
        // trick for tracemodel. We should accept all not registered files as well, till it is not system one.
        if (retValue == null && ParserThreadManager.instance().isStandalone()) {
            retValue = absPath.toString().startsWith("/usr") ? retValue : this; // NOI18N
        }
        return retValue;
    }

    @Override
    public boolean isArtificial() {
        return false;
    }

    @Override
    public NativeFileItem getNativeFileItem(CsmUID<CsmFile> file) {
        return nativeFiles.getNativeFileItem(file);
    }

    @Override
    protected void putNativeFileItem(CsmUID<CsmFile> file, NativeFileItem nativeFileItem) {
        nativeFiles.putNativeFileItem(file, nativeFileItem);
    }

    @Override
    protected NativeFileItem removeNativeFileItem(CsmUID<CsmFile> file) {
        return nativeFiles.removeNativeFileItem(file);
    }

    @Override
    protected void clearNativeFileContainer() {
        nativeFiles.clear();
    }
    private final NativeFileContainer nativeFiles = new NativeFileContainer();

    @Override
    protected void onDispose() {
        nativeFiles.clear();
        editedFiles.clear();
        projectRoots.clear();
    }
    
    private final SourceRootContainer projectRoots = new SourceRootContainer(false);
    @Override
    protected SourceRootContainer getProjectRoots() {
        return projectRoots;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    public 
    @Override
    void write(RepositoryDataOutput aStream) throws IOException {
        super.write(aStream);
        // we don't need this since ProjectBase persists fqn
        //UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        //aFactory.writeUID(getUID(), aStream);
        LibraryManager.getInstance(this).writeProjectLibraries(getUID(), aStream);
    }

    public ProjectImpl(RepositoryDataInput input) throws IOException {
        super(input);
        // we don't need this since ProjectBase persists fqn
        //UIDObjectFactory aFactory = UIDObjectFactory.getDefaultFactory();
        //CsmUID uid = aFactory.readUID(input);
        //LibraryManager.getInsatnce().read(uid, input);
        LibraryManager.getInstance(this).readProjectLibraries(getUID(), input);
    //nativeFiles = new NativeFileContainer();
    }

    ////////////////////////////////////////////////////////////////////////////
    private final static RequestProcessor RP = new RequestProcessor("ProjectImpl RP", 50); // NOI18N
    private void scheduleParseOnEditing(final FileImpl file) {
        RequestProcessor.Task task;
        int delay;
        synchronized (editedFiles) {
            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                new Exception("scheduleParseOnEditing " + file).printStackTrace(System.err); // NOI18N
            }            
            EditingTask pair = editedFiles.get(file);
            if (pair == null) {
                // we were removed between rescheduling and finish of edit
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    System.err.println("scheduleParseOnEditing: file was removed " + file);
                }
                return;
            }
            if (!pair.updateLastModified()) {
                // no need to schedule the second parse
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    System.err.println("scheduleParseOnEditing: no updates " + file + " : " + pair.lastModified);
                }
                return;
            }
            
            // markReparseNeeded have to be called synchroniously 
            // otherwise it will be delayed till DeepReparsingUtils.reparseOnEditingFile is called from task
            // but task is delayed (or even turned off), so this could never happen and client 
            // using CsmFile.scheduleParsing(true) get file without wait for reparsing, because 
            // file is still in state PARSED if delayed till task starts execution
            // see #203526 - Code completion is empty if typing too fast
            file.markReparseNeeded(false);
            task = pair.getTask();
            if (task == null) {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("scheduleParseOnEditing: edited file " + csmFile);
                    }
                    System.err.println("scheduleParseOnEditing: current file " + file);
                }
                task = RP.create(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                                System.err.printf("scheduleParseOnEditing: RUN scheduleParseOnEditing task for %s\n", file);
                            }
                            if (isDisposing()) {
                                return;
                            }
                            DeepReparsingUtils.reparseOnEditingFile(ProjectImpl.this, file);
                        } catch (AssertionError ex) {
                            DiagnosticExceptoins.register(ex);
                        } catch (Exception ex) {
                            DiagnosticExceptoins.register(ex);
                        }
                    }
                }, true);
                task.setPriority(Thread.MIN_PRIORITY);
                pair.setTask(task);
            } else {
                if (TraceFlags.TRACE_182342_BUG || TraceFlags.TRACE_191307_BUG) {
                    for (CsmFile csmFile : editedFiles.keySet()) {
                        System.err.println("reschedule in scheduleParseOnEditing: edited file " + csmFile);
                    }
                    System.err.println("reschedule in scheduleParseOnEditing: current file " + file);
                }
            }
            delay = TraceFlags.REPARSE_DELAY;
            NamedOption.getAccessor().getBoolean(ReparseOnEditOption.NAME);
            boolean doReparse = NamedOption.getAccessor().getBoolean(ReparseOnEditOption.NAME);
            if (doReparse) {
                if (file.getLastParseTime() / (delay+1) > 2) {
                    delay = Math.max(delay, file.getLastParseTime()+2000);
                }
            } else {
                delay = Integer.MAX_VALUE;
            }
        }
        task.schedule(delay);
    }

    @Override
    public void setDisposed() {
        super.setDisposed();
        synchronized (editedFiles) {
            for (EditingTask task : editedFiles.values()) {
                task.cancelTask();
            }
            editedFiles.clear();
        }
    }

    @ServiceProvider(path=NamedOption.OTHER_CATEGORY, service=NamedOption.class, position=1200)
    public static final class ReparseOnEditOption extends NamedOption {
        private static final String NAME = "reparse-on-document-changed"; //NOI18N
        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(ProjectImpl.class, "Show-reparse-on-document-changed"); //NOI18N
        }

        @Override
        public String getDescription() {
            return NbBundle.getMessage(ProjectImpl.class, "Show-reparse-on-document-changed-AD"); //NOI18N
        }

        @Override
        public OptionKind getKind() {
            return OptionKind.Boolean;
        }

        @Override
        public Object getDefaultValue() {
            return true;
        }
    }
}
