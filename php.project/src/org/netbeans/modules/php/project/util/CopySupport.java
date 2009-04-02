/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.util;

import org.netbeans.modules.php.project.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radek Matous
 */
public class CopySupport extends FileChangeAdapter implements PropertyChangeListener, FileChangeListener {
    private static boolean showMessage = true;
    private volatile PhpProject project;
    private volatile boolean isProjectOpened;
    private FileOperationFactory operationFactory;
    private FileSystem fileSystem;
    private FileChangeListener weakFileChangeListener;
    private static final RequestProcessor RP = new RequestProcessor("PHP file change handler"); // NOI18N
    private static final Queue<Callable<Boolean>> operationsQueue = new ConcurrentLinkedQueue<Callable<Boolean>>();
    private static final RequestProcessor.Task processingTask = createProcessingTask();

    public static CopySupport getInstance() {
        return new CopySupport();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        prepareOperation(getOperationFactory().createCopyHandler(fe.getFile()));
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        prepareOperation(getOperationFactory().createCopyHandler(fe.getFile()));
    }

    @Override
    public void fileChanged(FileEvent fe) {
        prepareOperation(getOperationFactory().createCopyHandler(fe.getFile()));
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        prepareOperation(getOperationFactory().createDeleteHandler(fe.getFile()));
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        String originalName = fe.getName();
        String ext = fe.getExt();
        if (ext != null && ext.trim().length() > 0) {
            originalName += "." + ext;//NOI18N
        }
        prepareOperation(getOperationFactory().createRenameHandler(fe.getFile(), originalName));
    }


    public void projectOpened(PhpProject project) {
        isProjectOpened = true;
        if (this.project == null) {
            this.project = project;
            ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, this);
            this.operationFactory = new ProxyOperationFactory(project);
        } 
        init(false);
    }

    public void projectClosed(PhpProject project) {
        isProjectOpened = false;
        unregisterFileChangeListener();
    }

    PhpProject getProject() {
        return project;
    }

    private static RequestProcessor.Task createProcessingTask() {
        return RP.create(new Runnable() {
            public void run() {
                Callable<Boolean> operation = operationsQueue.poll();
                while (operation != null) {
                    try {
                        operation.call();
                    } catch (Exception ex) {
                        CopySupport.showProblem(ex);
                    }
                    operation = operationsQueue.poll();
                }
            }
        });
    }

    private synchronized FileOperationFactory getOperationFactory() {
        assert operationFactory != null;
        return operationFactory;
    }

    private void prepareOperation(Callable<Boolean> callable) {
        if (callable != null) {
            operationsQueue.offer(callable);
            processingTask.schedule(300);
        }
    }


    private void registerFileChangeListener(FileObject sourcesDirectory) {
        if (weakFileChangeListener == null) {
            try {
                fileSystem = sourcesDirectory.getFileSystem();
                weakFileChangeListener = FileUtil.weakFileChangeListener(this, fileSystem);
                fileSystem.addFileChangeListener(weakFileChangeListener);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    synchronized private void unregisterFileChangeListener() {
        if (weakFileChangeListener != null) {
            fileSystem.removeFileChangeListener(weakFileChangeListener);
            fileSystem = null;
            weakFileChangeListener = null;
        }
    }

    synchronized private void init(boolean initCopy) {
        unregisterFileChangeListener();
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        if (sourcesDirectory != null) {
            showMessage = true;
            if (initCopy) {
                prepareOperation(getOperationFactory().createInitHandler(sourcesDirectory));
            }
            registerFileChangeListener(sourcesDirectory);
        }
    }

    public void waitFinished() {
        processingTask.schedule(0);
        processingTask.waitFinished();
    }

    public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
        final String propertyName = propertyChangeEvent.getPropertyName();
        if (isProjectOpened) {
            if (propertyName.equals(PhpProjectProperties.COPY_SRC_TARGET) ||
                    propertyName.equals(PhpProjectProperties.SRC_DIR) ||
                    propertyName.equals(PhpProjectProperties.COPY_SRC_FILES)) {
                ProjectManager.mutex().readAccess(new Runnable() {

                    public void run() {
                        init(true);
                    }
                });
            }
        }
    }

    private static class ProxyOperationFactory extends FileOperationFactory {

        final FileOperationFactory localFactory;
        final FileOperationFactory remoteFactory;

        ProxyOperationFactory(PhpProject project) {
            this.localFactory = new LocalOperationFactory(project);
            this.remoteFactory = new RemoteOperationFactory(project);
        }

        @Override
        Callable<Boolean> createCopyHandler(FileObject source) {
            return createHandler(localFactory.createCopyHandler(source), remoteFactory.createCopyHandler(source));
        }

        @Override
        Callable<Boolean> createDeleteHandler(FileObject source) {
            return createHandler(localFactory.createDeleteHandler(source), remoteFactory.createDeleteHandler(source));
        }

        @Override
        Callable<Boolean> createInitHandler(FileObject source) {
            return createHandler(localFactory.createInitHandler(source), remoteFactory.createInitHandler(source));
        }

        @Override
        Callable<Boolean> createRenameHandler(FileObject source, String oldName) {
            return createHandler(localFactory.createRenameHandler(source, oldName), remoteFactory.createRenameHandler(source, oldName));
        }

        private static Callable<Boolean> createHandler(final Callable<Boolean> localHandler, final Callable<Boolean> remoteHandler) {
            return (localHandler != null || remoteHandler != null) ? new Callable<Boolean>() {

                public Boolean call() throws Exception {
                    boolean localRetval = true;
                    boolean remoteRetval = true;
                    if (localHandler != null) {
                        localRetval = localHandler.call();
                    }
                    if (remoteHandler != null) {
                        remoteRetval = remoteHandler.call();
                    }
                    return remoteRetval && localRetval;
                }
            } : null;
        }
    }

    private static void showProblem(Exception ex) {
        if (showMessage) {
            //DialogDisplayer.getDefault().notify(new NotifyDescriptor.Exception(ex, ex.getMessage()));
            Exceptions.printStackTrace(ex);
            showMessage = false;
        }
    }
}


