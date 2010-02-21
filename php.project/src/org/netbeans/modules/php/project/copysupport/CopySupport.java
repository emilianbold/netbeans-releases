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
package org.netbeans.modules.php.project.copysupport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Radek Matous
 */
public final class CopySupport extends FileChangeAdapter implements PropertyChangeListener, FileChangeListener, ChangeListener {
    private static final Logger LOGGER = Logger.getLogger(CopySupport.class.getName());

    public static final boolean ALLOW_BROKEN = Boolean.getBoolean(CopySupport.class.getName() + ".allowBroken"); // NOI18N

    private static final RequestProcessor COPY_SUPPORT_RP = new RequestProcessor("PHP file change handler (copy support)"); // NOI18N
    private static final int FILE_CHANGE_DELAY = 300; // ms
    private static final int PROPERTY_CHANGE_DELAY = 500; // ms
    private static final int PROGRESS_INITIAL_DELAY = 1000; // ms

    static final Queue<Callable<Boolean>> OPERATIONS_QUEUE = new ConcurrentLinkedQueue<Callable<Boolean>>();
    static final RequestProcessor.Task COPY_TASK = createCopyTask();

    final PhpProject project;
    final PhpVisibilityQuery phpVisibilityQuery;

    // process property changes just once
    private final RequestProcessor.Task initTask;

    private volatile boolean projectOpened = false;
    private final ProxyOperationFactory proxyOperationFactory;
    // @GuardedBy(this)
    private FileSystem fileSystem;
    // @GuardedBy(this)
    private FileChangeListener fileChangeListener;

    private CopySupport(final PhpProject project) {
        assert project != null;

        this.project = project;
        phpVisibilityQuery = PhpVisibilityQuery.forProject(project);
        proxyOperationFactory = new ProxyOperationFactory(project);
        ProjectPropertiesSupport.addWeakPropertyEvaluatorListener(project, this);
        RemoteConnections remoteConnections = RemoteConnections.get();
        remoteConnections.addChangeListener(WeakListeners.change(this, remoteConnections));

        initTask = COPY_SUPPORT_RP.create(new Runnable() {
           public void run() {
               init();
            }
        });
    }

    public static CopySupport getInstance(PhpProject project) {
        return new CopySupport(project);
    }

    private static RequestProcessor.Task createCopyTask() {
        return COPY_SUPPORT_RP.create(new Runnable() {
            public void run() {
                Callable<Boolean> operation = OPERATIONS_QUEUE.poll();
                while (operation != null) {
                    try {
                        operation.call();
                    } catch (Exception ex) {
                        LOGGER.log(Level.WARNING, null, ex);
                    }
                    operation = OPERATIONS_QUEUE.poll();
                }
            }
        }, true);
    }

    public void projectOpened() {
        LOGGER.log(Level.FINE, "Opening Copy support for project {0}", project.getName());
        assert !projectOpened : "Copy Support already opened for project " + project.getName();

        projectOpened = true;

        initTask.schedule(PROPERTY_CHANGE_DELAY);
    }

    public void projectClosed() {
        LOGGER.log(Level.FINE, "Closing Copy support for project {0}", project.getName());
        assert projectOpened : "Copy Support already closed for project " + project.getName();

        projectOpened = false;
        unregisterFileChangeListener();
    }

    private void prepareOperation(Callable<Boolean> callable) {
        if (callable != null) {
            OPERATIONS_QUEUE.offer(callable);
            COPY_TASK.schedule(FILE_CHANGE_DELAY);
        }
    }

    synchronized void init() {
        LOGGER.log(Level.FINE, "Copy support INIT for project {0}", project.getName());

        // invalidate factories, e.g. remote client (it's better to simply create a new client)
        proxyOperationFactory.reset();

        if (proxyOperationFactory.isEnabled()) {
            prepareOperation(proxyOperationFactory.createInitHandler(getSources()));
            registerFileChangeListener();
        } else {
            unregisterFileChangeListener();
        }
    }

    private void registerFileChangeListener() {
        LOGGER.log(Level.FINE, "Copy support REGISTERING FS listener for project {0}", project.getName());
        assert Thread.holdsLock(this);

        if (fileChangeListener != null) {
            LOGGER.log(Level.FINE, "\t-> not needed for project {0} (already registered)", project.getName());
            return;
        }
        if (ALLOW_BROKEN) {
            assert fileChangeListener == null : "FS listener cannot yet exist for project " + project.getName();
            try {
                fileSystem = getSources().getFileSystem();
                fileChangeListener = FileUtil.weakFileChangeListener(this, fileSystem);
                fileSystem.addFileChangeListener(fileChangeListener);
                LOGGER.log(Level.FINE, "\t-> NON-RECURSIVE listener registered for project {0}", project.getName());
            } catch (FileStateInvalidException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        } else {
            fileChangeListener = new SourcesFileChangeListener(this);
            FileUtil.addRecursiveListener(fileChangeListener, FileUtil.toFile(getSources()));
            LOGGER.log(Level.FINE, "\t-> RECURSIVE listener registered for project {0}", project.getName());
        }
    }

    private synchronized void unregisterFileChangeListener() {
        LOGGER.log(Level.FINE, "Copy support UNREGISTERING FS listener for project {0}", project.getName());
        if (fileChangeListener == null) {
            LOGGER.log(Level.FINE, "\t-> not needed for project {0} (not registered)", project.getName());
        } else {
            if (ALLOW_BROKEN) {
                assert fileChangeListener != null : "FS listener must be known already for project " + project.getName();
                fileSystem.removeFileChangeListener(fileChangeListener);
                LOGGER.log(Level.FINE, "\t-> NON-RECURSIVE listener unregistered for project {0}", project.getName());
            } else {
                assert fileChangeListener instanceof SourcesFileChangeListener : "FS listener of incorrect type: " + fileChangeListener.getClass().getName();
                // #172777
                try {
                    FileUtil.removeRecursiveListener(fileChangeListener, FileUtil.toFile(getSources()));
                    LOGGER.log(Level.FINE, "\t-> RECURSIVE listener unregistered for project {0}", project.getName());
                } catch (IllegalArgumentException ex) {
                    LOGGER.log(Level.WARNING,
                            "If this happens to you reliably, report issue with steps to reproduce and attach IDE log (http://www.netbeans.org/community/issues).", ex);
                    FileObject originalSources = ((SourcesFileChangeListener) fileChangeListener).getSources();
                    FileObject currentSources = getSources();
                    LOGGER.log(Level.INFO,
                            "registered sources (valid): {0} ({1}), current sources (valid): {2} ({3}), equals: {4}",
                            new Object[] {originalSources, originalSources.isValid(), currentSources, currentSources.isValid(), originalSources.equals(currentSources)});
                }
            }
            fileSystem = null;
            fileChangeListener = null;
        }
    }

    /**
     * @return {@code true} if copying finished or user wants to continue
     */
    public boolean waitFinished() {
        try {
            if (!proxyOperationFactory.isEnabled()) {
                return true;
            }
            if (COPY_TASK.waitFinished(200)) {
                return true;
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return true;
        }
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                NbBundle.getMessage(CopySupport.class, "MSG_CopySupportRunning"),
                NotifyDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FOLDER CREATED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createCopyHandler(source, fe));
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event DATA CREATED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createCopyHandler(source, fe));
    }

    @Override
    public void fileChanged(FileEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FILE CHANGED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createCopyHandler(source, fe));
    }

    @Override
    public void fileDeleted(FileEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FILE DELETED for project {0}", project.getName());
        prepareOperation(proxyOperationFactory.createDeleteHandler(source, fe));
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        FileObject source = getValidProjectSource(fe);
        if (source == null) {
            return;
        }
        LOGGER.log(Level.FINE, "Processing event FILE RENAMED for project {0}", project.getName());
        String originalName = fe.getName();
        String ext = fe.getExt();
        if (StringUtils.hasText(ext)) {
            originalName += "." + ext; // NOI18N
        }
        prepareOperation(proxyOperationFactory.createRenameHandler(source, originalName, fe));
    }

    public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
        if (projectOpened) {
            LOGGER.log(Level.FINE, "Processing event PROPERTY CHANGE for opened project {0}", project.getName());
            initTask.schedule(PROPERTY_CHANGE_DELAY);
        }
    }

    public void stateChanged(ChangeEvent e) {
        if (projectOpened) {
            LOGGER.log(Level.FINE, "Processing event STATE CHANGE (remote connections) for opened project {0}", project.getName());
            initTask.schedule(PROPERTY_CHANGE_DELAY);
        }
    }

    private FileObject getValidProjectSource(FileEvent fileEvent) {
        LOGGER.log(Level.FINEST, "Getting source file for project {0} from {1}", new Object[] {project.getName(), fileEvent});
        FileObject source = fileEvent.getFile();
        if (!PhpProjectUtils.isVisible(phpVisibilityQuery, source)) {
            LOGGER.finest("\t-> null (invisible source)");
            return null;
        }
        if (!CommandUtils.isUnderSources(project, source)) {
            LOGGER.finest("\t-> null (invalid source)");
            return null;
        }
        LOGGER.log(Level.FINE, "Got source file for project {0} from {1}", new Object[] {project.getName(), fileEvent});
        return source;
    }

    FileObject getSources() {
        return ProjectPropertiesSupport.getSourcesDirectory(project);
    }

    private static class ProxyOperationFactory extends FileOperationFactory {

        final FileOperationFactory localFactory;
        final FileOperationFactory remoteFactory;

        ProxyOperationFactory(PhpProject project) {
            super(project);
            this.localFactory = new LocalOperationFactory(project);
            this.remoteFactory = new RemoteOperationFactory(project);
        }

        @Override
        protected void resetInternal() {
            localFactory.reset();
            remoteFactory.reset();
        }

        @Override
        Logger getLogger() {
            return LOGGER;
        }

        @Override
        protected boolean isEnabled() {
            return localFactory.isEnabled()
                    || remoteFactory.isEnabled();
        }

        @Override
        protected Callable<Boolean> createInitHandlerInternal(FileObject source) {
            return createHandler(localFactory.createInitHandler(source), remoteFactory.createInitHandler(source));
        }

        @Override
        protected Callable<Boolean> createCopyHandlerInternal(FileObject source, FileEvent fileEvent) {
            return createHandler(localFactory.createCopyHandler(source, fileEvent), remoteFactory.createCopyHandler(source, fileEvent));
        }

        @Override
        protected Callable<Boolean> createRenameHandlerInternal(FileObject source, String oldName, FileRenameEvent fileRenameEvent) {
            return createHandler(localFactory.createRenameHandler(source, oldName, fileRenameEvent), remoteFactory.createRenameHandler(source, oldName, fileRenameEvent));
        }

        @Override
        protected Callable<Boolean> createDeleteHandlerInternal(FileObject source, FileEvent fileEvent) {
            return createHandler(localFactory.createDeleteHandler(source, fileEvent), remoteFactory.createDeleteHandler(source, fileEvent));
        }

        private Callable<Boolean> createHandler(Callable<Boolean> localHandler, Callable<Boolean> remoteHandler) {
            if (localHandler == null && remoteHandler == null) {
                LOGGER.fine("No handler given");
                return null;
            }
            return new ProxyHandler(localHandler, remoteHandler);
        }

        @Override
        protected boolean isValid(FileEvent fileEvent) {
            return true;
        }

        private final class ProxyHandler implements Callable<Boolean> {
            private final Callable<Boolean> localHandler;
            private final Callable<Boolean> remoteHandler;

            public ProxyHandler(Callable<Boolean> localHandler, Callable<Boolean> remoteHandler) {
                this.localHandler = localHandler;
                this.remoteHandler = remoteHandler;
            }

            public Boolean call() throws Exception {
                Boolean localRetval = callLocal();
                Boolean remoteRetval = callRemote();
                if (localRetval == null && remoteRetval == null) {
                    return null;
                }
                if (localRetval != null && !localRetval) {
                    return false;
                }
                if (remoteRetval != null && !remoteRetval) {
                    return false;
                }
                return true;
            }

            private Boolean callLocal() {
                Boolean localRetval = null;
                Exception localExc = null;

                if (localHandler != null) {
                    LOGGER.log(Level.FINE, "Processing LOCAL copying handler for project {0}", project.getName());

                    ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(CopySupport.class, "LBL_LocalSynchronization"));
                    progress.setInitialDelay(PROGRESS_INITIAL_DELAY);
                    try {
                        progress.start();
                        localRetval = localHandler.call();
                    } catch (Exception exc) {
                        LOGGER.log(Level.INFO, "LOCAL copying fail: ", exc);
                        localRetval = false;
                        localExc = exc;
                    } finally {
                        progress.finish();
                    }
                }
                if (localRetval != null && !localRetval) {
                    if (askUser(NbBundle.getMessage(CopySupport.class, "LBL_Copy_Support_Fail", project.getName()))) {
                        localFactory.invalidate();
                        LOGGER.log(Level.INFO, String.format("LOCAL copying for project %s disabled by user", project.getName()), localExc);
                    }
                }
                return localRetval;
            }

            private Boolean callRemote() {
                Boolean remoteRetval = null;
                Exception remoteExc = null;

                if (remoteHandler != null) {
                    LOGGER.log(Level.FINE, "Processing REMOTE copying handler for project {0}", project.getName());

                    ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(CopySupport.class, "LBL_RemoteSynchronization"));
                    progress.setInitialDelay(PROGRESS_INITIAL_DELAY);
                    try {
                        progress.start();
                        remoteRetval = remoteHandler.call();
                    } catch (Exception exc) {
                        LOGGER.log(Level.INFO, "REMOTE copying fail: ", exc);
                        remoteRetval = false;
                        remoteExc = exc;
                    } finally {
                        progress.finish();
                    }
                    if (remoteRetval != null && !remoteRetval) {
                        if (askUser(NbBundle.getMessage(CopySupport.class, "LBL_Remote_On_Save_Fail", project.getName()))) {
                            remoteFactory.invalidate();
                            LOGGER.log(Level.INFO, String.format("REMOTE copying for project %s disabled by user", project.getName()), remoteExc);
                        }
                    }
                }
                return remoteRetval;
            }
        }
    }

    // #172777
    private static class SourcesFileChangeListener implements FileChangeListener {
        private final CopySupport copySupport;
        private final FileObject sources;

        public SourcesFileChangeListener(CopySupport copySupport) {
            this.copySupport = copySupport;
            this.sources = copySupport.getSources();
        }

        public FileObject getSources() {
            return sources;
        }

        public void fileFolderCreated(FileEvent fe) {
            copySupport.fileFolderCreated(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            copySupport.fileDataCreated(fe);
        }

        public void fileChanged(FileEvent fe) {
            copySupport.fileChanged(fe);
        }

        public void fileDeleted(FileEvent fe) {
            copySupport.fileDeleted(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            copySupport.fileRenamed(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            copySupport.fileAttributeChanged(fe);
        }
    }
}
