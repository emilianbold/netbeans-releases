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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Radek Matous
 */
public class CopySupport extends FileChangeAdapter implements PropertyChangeListener, FileChangeListener {
    private static final int PROGRESS_INITIAL_DELAY = 1000;

    private volatile PhpProject project;
    private volatile boolean isProjectOpened;
    private ProxyOperationFactory operationFactory;
    private FileSystem fileSystem;
    private FileChangeListener weakFileChangeListener;
    private static final RequestProcessor RP = new RequestProcessor("PHP file change handler"); // NOI18N
    private static final Queue<Callable<Boolean>> operationsQueue = new ConcurrentLinkedQueue<Callable<Boolean>>();
    private static final RequestProcessor.Task processingTask = createProcessingTask();
    private static final Logger LOGGER = Logger.getLogger(CopySupport.class.getName());
    private static final boolean IS_FINE_LOGGABLE = LOGGER.isLoggable(Level.FINE);


    public static CopySupport getInstance() {
        return new CopySupport();
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject source = fe.getFile();
        if (sourcesDirectory != null && isHandled(sourcesDirectory, source))  {
            if (IS_FINE_LOGGABLE) {
                String format = "processing fileFolderCreated event \"%s\" from project \"%s\"";//NOI18N
                LOGGER.fine(String.format(format, fe.toString(), project.getName()));
            }
            prepareOperation(getOperationFactory().createCopyHandler(source));
        }
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject source = fe.getFile();
        if (sourcesDirectory != null && isHandled(sourcesDirectory, source))  {
            if (IS_FINE_LOGGABLE) {
                String format = "processing fileDataCreated event \"%s\" from project \"%s\"";//NOI18N
                LOGGER.fine(String.format(format, fe.toString(), project.getName()));
            }
            prepareOperation(getOperationFactory().createCopyHandler(source));
        }
    }

    @Override
    public void fileChanged(FileEvent fe) {
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject source = fe.getFile();
        if (sourcesDirectory != null && isHandled(sourcesDirectory, source))  {
            if (IS_FINE_LOGGABLE) {
                String format = "processing fileChanged event \"%s\" from project \"%s\"";//NOI18N
                LOGGER.fine(String.format(format, fe.toString(), project.getName()));
            }
            prepareOperation(getOperationFactory().createCopyHandler(source));
        }
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject source = fe.getFile();
        if (sourcesDirectory != null && isHandled(sourcesDirectory, source))  {
            if (IS_FINE_LOGGABLE) {
                String format = "processing fileDeleted event \"%s\" from project \"%s\"";//NOI18N
                LOGGER.fine(String.format(format, fe.toString(), project.getName()));
            }
            prepareOperation(getOperationFactory().createDeleteHandler(source));
        }
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        String originalName = fe.getName();
        String ext = fe.getExt();
        if (ext != null && ext.trim().length() > 0) {
            originalName += "." + ext;//NOI18N
        }
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        FileObject source = fe.getFile();
        if (sourcesDirectory != null && isHandled(sourcesDirectory, source))  {
            if (IS_FINE_LOGGABLE) {
                String format = "processing fileRenamed event \"%s\" from project \"%s\"";//NOI18N
                LOGGER.fine(String.format(format, fe.toString(), project.getName()));
            }
            prepareOperation(getOperationFactory().createRenameHandler(source, originalName));
        }
    }


    public void projectOpened(PhpProject project) {
        isProjectOpened = true;
        if (this.project == null) {
            this.project = project;
            ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, this);
            this.operationFactory = new ProxyOperationFactory(project);
        }
        this.operationFactory.init();
        init(true);
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
                        Exceptions.printStackTrace(ex);
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

    private static boolean isHandled(FileObject sourcesDirectory, FileObject source) {
        return FileUtil.isParentOf(sourcesDirectory, source) || sourcesDirectory == source;
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
                if (IS_FINE_LOGGABLE) {
                    String format = "+Copy support for project \"%s\" registers FS listener: \"%s\"";//NOI18N
                    LOGGER.fine(String.format(format, project.getName(), fileSystem.getDisplayName()));
                }
                fileSystem.addFileChangeListener(weakFileChangeListener);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    synchronized private void unregisterFileChangeListener() {
        if (weakFileChangeListener != null) {
            if (IS_FINE_LOGGABLE) {
                String format = "-Copy support for project \"%s\" unregisters FS listener: \"%s\"";//NOI18N
                LOGGER.fine(String.format(format, project.getName(), fileSystem.getDisplayName()));
            }
            fileSystem.removeFileChangeListener(weakFileChangeListener);
            fileSystem = null;
            weakFileChangeListener = null;
        }
    }

    synchronized void init(boolean initCopy) {
        if (IS_FINE_LOGGABLE) {
                String format = "Copy support for project \"%s\" INIT";//NOI18N
                LOGGER.fine(String.format(format, project.getName()));
        }
        unregisterFileChangeListener();
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        if (sourcesDirectory != null) {
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
            // invalidate factories, e.g. remote client (it's better to simply create a new client)
            operationFactory.invalidate();
            if (propertyName.equals(PhpProjectProperties.COPY_SRC_TARGET) ||
                    propertyName.equals(PhpProjectProperties.SRC_DIR) ||
                    propertyName.equals(PhpProjectProperties.COPY_SRC_FILES)) {
                ProjectManager.mutex().readAccess(new Runnable() {

                    public void run() {
                        if (IS_FINE_LOGGABLE) {
                            String format = "Copy support for project \"%s\" propertyChange processing...";//NOI18N
                            LOGGER.fine(String.format(format, project.getName()));
                        }
                        init(true);
                    }
                });
            }
        }
    }

    private static class ProxyOperationFactory extends FileOperationFactory {

        final FileOperationFactory localFactory;
        final FileOperationFactory remoteFactory;
        boolean localFactoryError;
        boolean remoteFactoryError;

        ProxyOperationFactory(PhpProject project) {
            this.localFactory = new LocalOperationFactory(project);
            this.remoteFactory = new RemoteOperationFactory(project);
        }

        void init() {
            this.localFactoryError = false;
            this.remoteFactoryError = false;
        }

        void invalidate() {
            localFactory.invalidate();
            remoteFactory.invalidate();
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

        private Callable<Boolean> createHandler(final Callable<Boolean> localHandler, final Callable<Boolean> remoteHandler) {
            return (localHandler != null || remoteHandler != null) ? new Callable<Boolean>() {

                public Boolean call() throws Exception {
                    boolean localRetval = true;
                    boolean remoteRetval = true;
                    Exception localExc = null;
                    Exception remoteExc = null;

                    if (!localFactoryError && localHandler != null) {
                        ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(CopySupport.class, "LBL_LocalSynchronization"));
                        progress.setInitialDelay(PROGRESS_INITIAL_DELAY);
                        try {
                            progress.start();
                            localRetval = localHandler.call();
                        } catch (Exception exc) {
                            LOGGER.log(Level.INFO, "Copy Support Fail: ", exc);
                            localRetval = false;
                            localExc = exc;
                        } finally {
                            progress.finish();
                        }
                    }
                    if (!localRetval) {
                        String message = NbBundle.getMessage(CopySupport.class, "LBL_Copy_Support_Fail");
                        Object continueCopying = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, JOptionPane.YES_NO_OPTION));
                        if (!continueCopying.equals(JOptionPane.YES_OPTION)) {
                            localFactoryError = true;
                            LOGGER.log(Level.INFO, "Copy Support Disabled By User", localExc);
                        }
                    }

                    if (!remoteFactoryError && remoteHandler != null) {
                        ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(CopySupport.class, "LBL_RemoteSynchronization"));
                        progress.setInitialDelay(PROGRESS_INITIAL_DELAY);
                        try {
                            progress.start();
                            remoteRetval = remoteHandler.call();
                        } catch (Exception exc) {
                            LOGGER.log(Level.INFO, "Remote On Save Fail: ", exc);
                            remoteRetval = false;
                            remoteExc = exc;
                        } finally {
                            progress.finish();
                        }
                        if (!remoteRetval) {
                            String message = NbBundle.getMessage(CopySupport.class, "LBL_Remote_On_Save_Fail");
                            Object continueCopying = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation(message, JOptionPane.YES_NO_OPTION));
                            if (continueCopying.equals(JOptionPane.YES_OPTION)) {
                                remoteFactoryError = true;
                                LOGGER.log(Level.INFO, "Remote On Save  Disabled By User", remoteExc);
                            }
                        }
                    }
                    return remoteRetval && localRetval;
                }
            } : null;
        }
    }
}


