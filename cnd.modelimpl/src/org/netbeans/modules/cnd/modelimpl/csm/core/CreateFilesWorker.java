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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
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

    CreateFilesWorker(ProjectBase project) {
        this.project = project;
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

    void createProjectFilesIfNeed(List<NativeFileItem> items, boolean sources,
            Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator) {

        List<FileImpl> reparseOnEdit = new ArrayList<FileImpl>();
        List<NativeFileItem> reparseOnPropertyChanged = Collections.synchronizedList(new ArrayList<NativeFileItem>());
        AtomicBoolean enougth = new AtomicBoolean(false);
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
            CreateFileRunnable r = new CreateFileRunnable(countDownLatch, list, sources, removedFiles,
                    validator, project, reparseOnEdit, reparseOnPropertyChanged, enougth);
            PROJECT_FILES_WORKER.post(r);
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException ex) {
        }
        //for (NativeFileItem nativeFileItem : items) {
        //    if (!createProjectFilesIfNeedRun(nativeFileItem, sources, removedFiles, validator,
        //            reparseOnEdit, reparseOnPropertyChanged, enougth)) {
        //        return;
        //    }
        //}
        if (!reparseOnEdit.isEmpty()) {
            DeepReparsingUtils.reparseOnEdit(reparseOnEdit, project, true);
        }
        if (!reparseOnPropertyChanged.isEmpty()) {
            DeepReparsingUtils.reparseOnPropertyChanged(reparseOnPropertyChanged, project);
        }
    }

    private class CreateFileRunnable implements Runnable {
        private final CountDownLatch countDownLatch;
        private final List<NativeFileItem> nativeFileItems;
        private final boolean sources;
        private final Set<NativeFileItem> removedFiles;
        private final ProjectSettingsValidator validator;
        private final ProjectBase project;
        private final List<FileImpl> reparseOnEdit;
        private final List<NativeFileItem> reparseOnPropertyChanged;
        private final AtomicBoolean enougth;

        private CreateFileRunnable(CountDownLatch countDownLatch, List<NativeFileItem> nativeFileItems, boolean sources,
            Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator, ProjectBase project,
            List<FileImpl> reparseOnEdit, List<NativeFileItem> reparseOnPropertyChanged, AtomicBoolean enougth){
            this.countDownLatch = countDownLatch;
            this.nativeFileItems = nativeFileItems;
            this.sources = sources;
            this.removedFiles = removedFiles;
            this.validator = validator;
            this.project = project;
            this.reparseOnEdit = reparseOnEdit;
            this.reparseOnPropertyChanged = reparseOnPropertyChanged;
            this.enougth = enougth;
        }

        @Override
        public void run() {
            try {
                for(NativeFileItem nativeFileItem : nativeFileItems) {
                    if (!createProjectFilesIfNeedRun(nativeFileItem, sources, removedFiles, validator,
                                            reparseOnEdit, reparseOnPropertyChanged, enougth)){
                        return;
                    }
                }
            } finally {
                countDownLatch.countDown();
            }
        }
        private boolean createProjectFilesIfNeedRun(NativeFileItem nativeFileItem, boolean sources,
                Set<NativeFileItem> removedFiles, ProjectSettingsValidator validator,
                List<FileImpl> reparseOnEdit, List<NativeFileItem> reparseOnPropertyChanged, AtomicBoolean enougth){
            if (enougth.get()) {
                return false;
            }
            final CsmModelState modelState = ModelImpl.instance().getState();
            if (modelState == CsmModelState.CLOSING || modelState == CsmModelState.OFF) {
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
                }
                return true;
            }
            assert (nativeFileItem.getFileObject() != null) : "native file item must have valid File object";
            if (TraceFlags.DEBUG) {
                ModelSupport.trace(nativeFileItem);
            }
            try {
                project.createIfNeed(nativeFileItem, sources, CreateFilesWorker.this.getLWM(), validator, reparseOnEdit, reparseOnPropertyChanged);
                if (project.isValidating() && RepositoryUtils.getRepositoryErrorCount(project) > 0) {
                    enougth.set(true);
                    return false;
                }
            } catch (Exception ex) {
                DiagnosticExceptoins.register(ex);
            }
            return true;
        }
    }
}
