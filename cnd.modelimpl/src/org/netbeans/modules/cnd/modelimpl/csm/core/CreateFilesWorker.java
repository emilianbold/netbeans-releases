/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
final class CreateFilesWorker {
    private FileModel lwm;
    private boolean lwmInited;
    private final ProjectBase project;
    private final RequestProcessor PROJECT_FILES_WORKER = new RequestProcessor("Project Files", CndUtils.getNumberCndWorkerThreads()); // NOI18N
    private final Set<FileImpl> reparseOnEdit = Collections.synchronizedSet(new HashSet<FileImpl>());
    private final Set<NativeFileItem> reparseOnPropertyChanged = Collections.synchronizedSet(new HashSet<NativeFileItem>());
    private final AtomicBoolean failureDetected = new AtomicBoolean(false);
    private final Set<NativeFileItem> removedFiles;
    private final ProjectSettingsValidator validator;
    CreateFilesWorker(ProjectBase project, Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator) {
        this.project = project;
        this.removedFiles = removedFiles;
        this.validator = validator;
    }

    private synchronized FileModel getLWM() {
        if (!lwmInited) {
            FileModelProvider provider = Lookup.getDefault().lookup(FileModelProvider.class);
            if (provider != null) {
                lwm = provider.getFileModel(project);
            }
            lwmInited = true;
        }
        return lwm;
    }

    void createProjectFilesIfNeed(List<NativeFileItem> items, boolean sources) {

        Set<CsmUID<CsmFile>> handledFiles = Collections.synchronizedSet(new HashSet<CsmUID<CsmFile>>(items.size()));
        int size = items.size();
        int threads = CndUtils.getNumberCndWorkerThreads()*3;
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        int chunk = (size/threads) + 1;
        Iterator<NativeFileItem> it = items.iterator();
        for (int i = 0; i < threads; i++) {
            ArrayList<NativeFileItem> list = new ArrayList<NativeFileItem>(chunk);
            for(int j = 0; j < chunk; j++){
                if(it.hasNext()){
                    list.add(it.next());
                } else {
                    break;
                }
            }
            CreateFileRunnable r = new CreateFileRunnable(countDownLatch, list, sources, handledFiles);
            PROJECT_FILES_WORKER.post(r);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
        }
        if (!failureDetected.get()) {
            // no issues with repository was found so far
            if (validator != null && !sources) {
                final Set<CsmUID<CsmFile>> allFilesUID = new HashSet<CsmUID<CsmFile>>(project.getHeaderFilesUID());
                allFilesUID.removeAll(handledFiles);
                if (TraceFlags.TRACE_VALIDATION) {
                    for (CsmUID<CsmFile> csmUID : handledFiles) {
                        System.err.printf("Hanlded %s - %d\n", csmUID, System.identityHashCode(csmUID));
                    }
                    for (CsmUID<CsmFile> csmUID : allFilesUID) {
                        System.err.printf("To handle %s - %d\n", csmUID, System.identityHashCode(csmUID));
                    }
                }
                // check that all headers are checked as well even if they are not part
                // of underlying NativeProject associated with CsmProject
                for (CsmUID<CsmFile> csmUID : allFilesUID) {
                    if (csmUID == null || RepositoryUtils.getRepositoryErrorCount(project) > 0) {
                        failureDetected.set(true);
                        break;
                    }
                    CsmFile file = UIDCsmConverter.UIDtoFile(csmUID);
                    if (file instanceof FileImpl) {
                        FileImpl fileImpl = (FileImpl) file;
                        if (fileImpl.getState() == FileImpl.State.INITIAL || !fileImpl.validate()) {
                            if (TraceFlags.TRACE_VALIDATION) {
                                System.err.printf("Validation: %s file [%d %s] to be parsed, because of state %s\n", fileImpl.getAbsolutePath(), System.identityHashCode(csmUID), fileImpl.getFileType(), fileImpl.getState()); //NOI18N
                            }
                            reparseOnEdit.add(fileImpl);
                        } else {
                            if (TraceFlags.TRACE_VALIDATION) {
                                System.err.printf("Validation: skip %s file [%d %s], because of state %s\n", fileImpl.getAbsolutePath(), System.identityHashCode(csmUID), fileImpl.getFileType(), fileImpl.getState()); //NOI18N
                            }
                        }
                    } else {
                        failureDetected.set(true);
                        RepositoryUtils.registerRepositoryError(project, new Exception("Validation: file was not restored from " + csmUID)); // NOI18N
                        System.err.printf("Validation: file was not restored from %s\n", csmUID); //NOI18N
                        break;
                    }
                }
                if (TraceFlags.DEBUG_BROKEN_REPOSITORY) {
                    failureDetected.set(true);
                    RepositoryUtils.registerRepositoryError(project, new Exception("Validation: INTENTIONAL interrupt")); // NOI18N
                }
            }
        }
    }

    /*package*/ void finishProjectFilesCreation() {
        if (!failureDetected.get()) {
            // add to parse all needed elements
            if (!reparseOnEdit.isEmpty()) {
                for (FileImpl file : reparseOnEdit) {
                    DeepReparsingUtils.tryPartialReparseOnChangedFile(project, file);
                }
            }
            if (!reparseOnPropertyChanged.isEmpty()) {
                DeepReparsingUtils.reparseOnPropertyChanged(reparseOnPropertyChanged, project, false);
            }
        }
    }

    /*package*/void checkLibraries() {
        if (!failureDetected.get() && validator != null) {
            // check libraries and find if our storage has extra model to contribute
            reparseOnEdit.addAll(project.checkLibrariesAfterRestore());
        }
    }

    private class CreateFileRunnable implements Runnable {
        private final CountDownLatch countDownLatch;
        private final List<NativeFileItem> nativeFileItems;
        private final boolean sources;
        private final Set<CsmUID<CsmFile>> handledFiles;

        private CreateFileRunnable(CountDownLatch countDownLatch, List<NativeFileItem> nativeFileItems, boolean sources, Set<CsmUID<CsmFile>> handledFiles){
            this.countDownLatch = countDownLatch;
            this.nativeFileItems = nativeFileItems;
            this.sources = sources;
            this.handledFiles = handledFiles;
        }
        
        @Override
        public void run() {
            try {
                if (TraceFlags.PARSE_HEADERS_WITH_SOURCES && !sources) {
                    return;
                } 
                for(NativeFileItem nativeFileItem : nativeFileItems) {
                    if (!createProjectFilesIfNeedRun(nativeFileItem)){
                        return;
                    }
                }
            } finally {
                countDownLatch.countDown();
            }
        }
        private boolean createProjectFilesIfNeedRun(NativeFileItem nativeFileItem){
            if (failureDetected.get()) {
                return false;
            }
            final CsmModelState modelState = ModelImpl.instance().getState();
            if (modelState == CsmModelState.CLOSING || modelState == CsmModelState.OFF) {
                if (TraceFlags.TRACE_VALIDATION || TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("createProjectFilesIfNeedRun: %s file [%s] is interrupted on closing model\n", nativeFileItem.getAbsolutePath(), project.getName());
                }                
                return false;
            }            
            if (project.isDisposing()) {
                if (TraceFlags.TRACE_MODEL_STATE) {
                    System.err.printf("filling parser queue interrupted for %s\n", project.getName());
                }
                return false;
            }
            if (removedFiles.contains(nativeFileItem)) {
                FileImpl file = project.getFile(nativeFileItem.getAbsolutePath(), true);
                if (file != null) {
                    project.removeFile(nativeFileItem.getAbsolutePath());
                    this.handledFiles.add(UIDCsmConverter.fileToUID(file));
                }
                return true;
            }
            assert (nativeFileItem.getFileObject() != null) : "native file item must have valid File object";
            if (TraceFlags.DEBUG) {
                ModelSupport.trace(nativeFileItem);
            }
            try {
                FileImpl fileImpl = project.createIfNeed(nativeFileItem, CreateFilesWorker.this.getLWM(), validator, reparseOnEdit, reparseOnPropertyChanged);
                this.handledFiles.add(UIDCsmConverter.fileToUID(fileImpl));
                if (project.isValidating() && RepositoryUtils.getRepositoryErrorCount(project) > 0) {
                    failureDetected.set(true);
                    return false;
                }
            } catch (Exception ex) {
                DiagnosticExceptoins.register(ex);
            }
            return true;
        }
    }
}
