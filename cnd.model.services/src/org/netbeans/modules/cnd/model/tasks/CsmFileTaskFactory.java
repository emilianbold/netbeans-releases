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
package org.netbeans.modules.cnd.model.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.RequestProcessor;

/**
 *  CsmFile analogue of CsmFileTaskFactory
 * 
 * This factory should be registered in the global lookup by listing its fully qualified
 * name in file <code>META-INF/services/org.netbeans.modules.cnd.modelimpl.csm.scheduling.CsmFileTaskFactory</code>.
 * 
 * @author Sergey Grinev
 */
public abstract class CsmFileTaskFactory {
    private final Map<FileObject, CsmFile> fobj2csm = new HashMap<FileObject, CsmFile>();
    private final Map<CsmFile, Runnable> csm2task = new HashMap<CsmFile, Runnable>();
    private final ProgressListener progressListener;

    protected CsmFileTaskFactory() {
        progressListener = new ProgressListener();
        CsmModelAccessor.getModel().addProgressListener(progressListener);
    }

    protected abstract Runnable createTask(FileObject file);

    protected abstract Collection<FileObject> getFileObjects();

    protected final void fileObjectsChanged() {
        final List<FileObject> currentFiles = new ArrayList<FileObject>(getFileObjects());

        WORKER.post(new Runnable() {

            public void run() {
                stateChangedImpl(currentFiles);
            }
        });
    }

    private void stateChangedImpl(List<FileObject> currentFiles) {
        //System.err.println("stateChangedImpl, newFiles: " + currentFiles.size() + ", file2Tasks: " + fobj2csm.size());
        Map<CsmFile, Runnable> toRemove = new HashMap<CsmFile, Runnable>();
        Map<CsmFile, Runnable> toAdd = new HashMap<CsmFile, Runnable>();

        synchronized (this) {
            List<FileObject> addedFiles = new ArrayList<FileObject>(currentFiles);
            List<FileObject> removedFiles = new ArrayList<FileObject>(fobj2csm.keySet());

            addedFiles.removeAll(fobj2csm.keySet());
            removedFiles.removeAll(currentFiles);

            //remove old tasks:
            for (FileObject r : removedFiles) {
                CsmFile csmFile = fobj2csm.remove(r);
                csm2task.remove(csmFile);

                if (csmFile == null) {
                    //TODO: log
                    continue;
                }

                toRemove.put(csmFile, csm2task.remove(r));
            }

            //add new tasks:
            for (FileObject fileObject : addedFiles) {
                if (fileObject == null) {
                    continue;
                }
                if (!fileObject.isValid()) {
                    continue;
                }
                CsmFile csmFile = CsmUtilities.getCsmFile(fileObject, false);

                if (csmFile != null) {
                    Runnable task = createTask(fileObject);

                    toAdd.put(csmFile, task);

                    fobj2csm.put(fileObject, csmFile);
                    csm2task.put(csmFile, task);
                }
            }
        }


        for (Entry<CsmFile, Runnable> e : toRemove.entrySet()) {
            //System.err.println("### removing file from taskfactory " + e.getKey().getName());
            // XXX: we need remove event
            //scheduler.removeParseCompletionTask(e.getKey(), e.getValue());
        }

        for (Entry<CsmFile, Runnable> e : toAdd.entrySet()) {
             WORKER.post(e.getValue());
        }
    }

    public final synchronized void reschedule(FileObject file) throws IllegalArgumentException {
        CsmFile source = fobj2csm.get(file);

        if (source == null) {
            return;
        }
        
        runTask(source);
    }
    
    private final void runTask(CsmFile file) {
        Runnable task = csm2task.get(file);
        
        if (task!=null) {
            WORKER.post(task);
        }
    }
    
    private static RequestProcessor WORKER = new RequestProcessor("CsmFileTaskFactory", 1); //NOI18N

    static {
        CsmFileTaskFactoryManager.ACCESSOR = new CsmFileTaskFactoryManager.Accessor() {

            public void fireChangeEvent(CsmFileTaskFactory f) {
                f.fileObjectsChanged();
            }
        };
    }

    private class ProgressListener extends CsmProgressAdapter {

        @Override
        public void fileParsingFinished(CsmFile file) {
            runTask(file);
        }
    }
}
