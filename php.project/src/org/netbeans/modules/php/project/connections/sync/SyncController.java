/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.connections.sync;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TmpLocalFile;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.transfer.TransferInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Controller for synchronization.
 */
public final class SyncController implements Cancellable {

    static final Logger LOGGER = Logger.getLogger(SyncController.class.getName());
    static final RequestProcessor SYNC_RP = new RequestProcessor("Remote PHP Synchronization", 1); // NOI18N

    final PhpProject phpProject;
    final RemoteClient remoteClient;
    final RemoteConfiguration remoteConfiguration;
    final long lastTimeStamp;

    volatile boolean cancelled = false;


    public SyncController(PhpProject phpProject, RemoteClient remoteClient, RemoteConfiguration remoteConfiguration) {
        this.phpProject = phpProject;
        this.remoteClient = remoteClient;
        this.remoteConfiguration = remoteConfiguration;
        lastTimeStamp = ProjectSettings.getSyncTimestamp(phpProject);
    }

    public void synchronize(final SyncResultProcessor resultProcessor) {
        SYNC_RP.post(new Runnable() {
            @Override
            public void run() {
                showPanel(fetchSyncItems(), resultProcessor);
            }
        });
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "SyncController.fetching=Fetching {0} files"
    })
    SyncItems fetchSyncItems() {
        assert !SwingUtilities.isEventDispatchThread();
        SyncItems items = null;
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.SyncController_fetching(phpProject.getName()), this);
        try {
            progressHandle.start();
            FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
            Set<TransferFile> remoteFiles = new HashSet<TransferFile>();
            initRemoteFiles(remoteFiles, remoteClient.prepareDownload(sources, sources));
            Set<TransferFile> localFiles = remoteClient.prepareUpload(sources, sources);
            items = pairItems(remoteFiles, localFiles);
        } catch (RemoteException ex) {
            disconnect();
            RemoteUtils.processRemoteException(ex);
        } finally {
            progressHandle.finish();
        }
        return items;
    }

    void showPanel(final SyncItems items, final SyncResultProcessor resultProcessor) {
        if (cancelled || items == null) {
            items.cleanup();
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SyncPanel panel = new SyncPanel(phpProject, remoteConfiguration.getDisplayName(), items.getItems(), remoteClient);
                if (panel.open(lastTimeStamp == -1)) {
                    doSynchronize(items, panel.getItems(), resultProcessor);
                } else {
                    disconnect();
                    items.cleanup();
                }
            }
        });
    }

    void doSynchronize(final SyncItems syncItems, final List<SyncItem> itemsToSynchronize, final SyncResultProcessor resultProcessor) {
        assert SwingUtilities.isEventDispatchThread();

        if (cancelled) {
            // in fact, cannot happen here
            return;
        }
        new Synchronizer(syncItems, itemsToSynchronize, resultProcessor).sync();
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        remoteClient.cancel();
        return true;
    }

    void disconnect() {
        try {
            remoteClient.disconnect();
        } catch (RemoteException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private SyncItems pairItems(Set<TransferFile> remoteFiles, Set<TransferFile> localFiles) {
        List<TransferFile> remoteFilesSorted = new ArrayList<TransferFile>(remoteFiles);
        Collections.sort(remoteFilesSorted, TransferFile.TRANSFER_FILE_COMPARATOR);
        List<TransferFile> localFilesSorted = new ArrayList<TransferFile>(localFiles);
        Collections.sort(localFilesSorted, TransferFile.TRANSFER_FILE_COMPARATOR);

        removeProjectRoot(remoteFilesSorted);
        removeProjectRoot(localFilesSorted);

        SyncItems items = new SyncItems();
        Iterator<TransferFile> remoteFilesIterator = remoteFilesSorted.iterator();
        Iterator<TransferFile> localFilesIterator = localFilesSorted.iterator();
        TransferFile remote = null;
        TransferFile local = null;
        while (remoteFilesIterator.hasNext()
                || localFilesIterator.hasNext()) {
            if (remote == null
                    && remoteFilesIterator.hasNext()) {
                remote = remoteFilesIterator.next();
            }
            if (local == null
                    && localFilesIterator.hasNext()) {
                local = localFilesIterator.next();
            }
            if (remote == null
                    || local == null) {
                items.add(remote, local, lastTimeStamp);
                remote = null;
                local = null;
            } else {
                int compare = TransferFile.TRANSFER_FILE_COMPARATOR.compare(remote, local);
                if (compare == 0) {
                    // same remote paths
                    items.add(remote, local, lastTimeStamp);
                    remote = null;
                    local = null;
                } else if (compare < 0) {
                    items.add(remote, null, lastTimeStamp);
                    remote = null;
                } else {
                    items.add(null, local, lastTimeStamp);
                    local = null;
                }
            }
        }
        return items;
    }

    private void removeProjectRoot(List<TransferFile> files) {
        if (files.isEmpty()) {
            return;
        }
        if (files.get(0).isProjectRoot()) {
            files.remove(0);
        }
    }

    /**
     * Remote files are downloaded lazily so we need to fetch all children.
     */
    private void initRemoteFiles(Set<TransferFile> allRemoteFiles, Collection<TransferFile> remoteFiles) {
        allRemoteFiles.addAll(remoteFiles);
        for (TransferFile file : remoteFiles) {
            initRemoteFiles(allRemoteFiles, file.getChildren());
        }
    }

    //~ Inner classes

    public static final class SyncResult {

        private final TransferInfo downloadTransferInfo = new TransferInfo();
        private final TransferInfo uploadTransferInfo = new TransferInfo();
        private final TransferInfo localDeleteTransferInfo = new TransferInfo();
        private final TransferInfo remoteDeleteTransferInfo = new TransferInfo();


        SyncResult() {
        }

        public TransferInfo getDownloadTransferInfo() {
            return downloadTransferInfo;
        }

        public TransferInfo getUploadTransferInfo() {
            return uploadTransferInfo;
        }

        public TransferInfo getLocalDeleteTransferInfo() {
            return localDeleteTransferInfo;
        }

        public TransferInfo getRemoteDeleteTransferInfo() {
            return remoteDeleteTransferInfo;
        }

    }

    public interface SyncResultProcessor {
        void process(SyncResult result);
    }

    private final class Synchronizer {

        private final SyncItems syncItems;
        private final List<SyncItem> itemsToSynchronize;
        private final SyncResultProcessor resultProcessor;


        public Synchronizer(SyncItems syncItems, List<SyncItem> itemsToSynchronize, SyncResultProcessor resultProcessor) {
            this.syncItems = syncItems;
            this.itemsToSynchronize = itemsToSynchronize;
            this.resultProcessor = resultProcessor;
        }

        public void sync() {
            assert SwingUtilities.isEventDispatchThread();
            // XXX show modal progress window
            SYNC_RP.post(new Runnable() {
                @Override
                public void run() {
                    doSync();
                }
            });
        }

        @NbBundle.Messages("SyncController.error.tmpFileCopyFailed=Failed to copy content of temporary file.")
        void doSync() {
            assert !SwingUtilities.isEventDispatchThread();

            Set<TransferFile> remoteFilesForDelete = new HashSet<TransferFile>();
            Set<TransferFile> localFilesForDelete = new HashSet<TransferFile>();
            final SyncResult syncResult = new SyncResult();
            for (SyncItem syncItem : itemsToSynchronize) {
                TransferFile remoteTransferFile = syncItem.getRemoteTransferFile();
                TransferFile localTransferFile = syncItem.getLocalTransferFile();
                switch (syncItem.getOperation()) {
                    case NOOP:
                        // noop
                        break;
                    case DOWNLOAD:
                    case DOWNLOAD_REVIEW:
                        try {
                            TransferInfo downloadInfo = remoteClient.download(Collections.singleton(remoteTransferFile));
                            mergeTransferInfo(downloadInfo, syncResult.getDownloadTransferInfo());
                        } catch (RemoteException ex) {
                            syncResult.getDownloadTransferInfo().addFailed(remoteTransferFile, ex.getLocalizedMessage());
                        }
                        break;
                    case UPLOAD:
                    case UPLOAD_REVIEW:
                        try {
                            // tmp file?
                            if (copyContent(syncItem.getTmpLocalFile(), localTransferFile.resolveLocalFile())) {
                                TransferInfo uploadInfo = remoteClient.upload(Collections.singleton(localTransferFile));
                                mergeTransferInfo(uploadInfo, syncResult.getUploadTransferInfo());
                            } else {
                                // valid fileobject not found??
                                LOGGER.log(Level.WARNING, "Cannot find FileObject for file {0}", localTransferFile.resolveLocalFile());
                                syncResult.getUploadTransferInfo().addFailed(localTransferFile, Bundle.SyncController_error_tmpFileCopyFailed());
                            }
                        } catch (RemoteException ex) {
                            LOGGER.log(Level.INFO, null, ex);
                            syncResult.getUploadTransferInfo().addFailed(localTransferFile, ex.getLocalizedMessage());
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, null, ex);
                            syncResult.getUploadTransferInfo().addFailed(localTransferFile, Bundle.SyncController_error_tmpFileCopyFailed());
                        }
                        break;
                    case DELETE:
                        // local
                        if (localTransferFile != null) {
                            localFilesForDelete.add(localTransferFile);
                        }
                        // remote
                        if (remoteTransferFile != null) {
                            remoteFilesForDelete.add(remoteTransferFile);
                        }
                        break;
                    default:
                        assert false : "Unsupported synchronization operation: " + syncItem.getOperation();
                }
            }
            deleteFiles(syncResult, remoteFilesForDelete, localFilesForDelete);
            syncItems.cleanup();
            disconnect();
            ProjectSettings.setSyncTimestamp(phpProject, TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
            resultProcessor.process(syncResult);
        }

        private boolean copyContent(TmpLocalFile source, File target) throws IOException {
            if (source == null) {
                // no tmp file
                return true;
            }
            FileObject fileObject = FileUtil.toFileObject(target);
            if (fileObject == null || !fileObject.isValid()) {
                return false;
            }
            InputStream inputStream = source.getInputStream();
            try {
                OutputStream outputStream = fileObject.getOutputStream();
                try {
                    FileUtil.copy(inputStream, outputStream);
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
            return true;
        }

        private void mergeTransferInfo(TransferInfo from, TransferInfo to) {
            to.setRuntime(to.getRuntime() + from.getRuntime());
            to.getTransfered().addAll(from.getTransfered());
            to.getIgnored().putAll(from.getIgnored());
            to.getPartiallyFailed().putAll(from.getPartiallyFailed());
            to.getFailed().putAll(from.getFailed());
        }

        private void deleteFiles(SyncResult syncResult, Set<TransferFile> remoteFiles, Set<TransferFile> localFiles) {
            deleteRemoteFiles(syncResult, remoteFiles);
            deleteLocalFiles(syncResult, localFiles);
        }

        private void deleteRemoteFiles(SyncResult syncResult, Set<TransferFile> remoteFiles) {
            if (remoteFiles.isEmpty()) {
                return;
            }
            try {
                TransferInfo deleteInfo = remoteClient.delete(remoteFiles);
                mergeTransferInfo(deleteInfo, syncResult.getRemoteDeleteTransferInfo());
            } catch (RemoteException ex) {
                // should not happen, can be 'ignored', we are simply not connected
                LOGGER.log(Level.INFO, null, ex);
                for (TransferFile transferFile : remoteFiles) {
                    syncResult.getRemoteDeleteTransferInfo().addFailed(transferFile, ex.getLocalizedMessage());
                    break;
                }
            }
        }

        @NbBundle.Messages({
            "SyncController.error.deleteLocalFile=Cannot delete local file.",
            "SyncController.error.localFolderNotEmpty=Cannot delete local folder because it is not empty."
        })
        private void deleteLocalFiles(SyncResult syncResult, Set<TransferFile> localFiles) {
            // first, delete files
            long start = System.currentTimeMillis();
            TransferInfo deleteInfo = new TransferInfo();
            try {
                Set<TransferFile> folders = new TreeSet<TransferFile>(new Comparator<TransferFile>() {
                    @Override
                    public int compare(TransferFile file1, TransferFile file2) {
                        // longest paths first to be able to delete directories properly
                        int cmp = StringUtils.explode(file2.getLocalPath(), File.separator).size() - StringUtils.explode(file1.getLocalPath(), File.separator).size();
                        return cmp != 0 ? cmp : 1;
                    }

                });
                // first, delete files
                for (TransferFile transferFile : localFiles) {
                    File localFile = transferFile.resolveLocalFile();
                    if (localFile.isDirectory()) {
                        folders.add(transferFile);
                    } else if (localFile.isFile()) {
                        if (localFile.delete()) {
                            deleteInfo.addTransfered(transferFile);
                        } else {
                            deleteInfo.addFailed(transferFile, Bundle.SyncController_error_deleteLocalFile());
                        }
                    }
                }
                // now delete non-empty directories
                for (TransferFile folder : folders) {
                    File localDir = folder.resolveLocalFile();
                    String[] children = localDir.list();
                    if (children != null && children.length == 0) {
                        if (localDir.delete()) {
                            deleteInfo.addTransfered(folder);
                        } else {
                            deleteInfo.addFailed(folder, Bundle.SyncController_error_deleteLocalFile());
                        }
                    } else {
                        deleteInfo.addIgnored(folder, Bundle.SyncController_error_localFolderNotEmpty());
                    }
                }
            } finally {
                deleteInfo.setRuntime(System.currentTimeMillis() - start);
                mergeTransferInfo(deleteInfo, syncResult.getLocalDeleteTransferInfo());
            }
        }

    }

}
