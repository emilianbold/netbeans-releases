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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.netbeans.modules.php.project.connections.transfer.TransferInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Controller for synchronization.
 */
public final class SyncController implements Cancellable {

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
                showPanel(fetchFiles(), resultProcessor);
            }
        });
    }

    @NbBundle.Messages("SyncController.fetching=Fetching {0} files")
    List<SyncItem> fetchFiles() {
        assert !SwingUtilities.isEventDispatchThread();
        List<SyncItem> items = null;
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.SyncController_fetching(phpProject.getName()), this);
        try {
            progressHandle.start();
            FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(phpProject);
            Set<TransferFile> remoteFiles = new HashSet<TransferFile>();
            initRemoteFiles(remoteFiles, remoteClient.prepareDownload(sources, sources));
            Set<TransferFile> localFiles = remoteClient.prepareUpload(sources, sources);
            items = pairFiles(remoteFiles, localFiles);
        } catch (RemoteException ex) {
            disconnect();
            RemoteUtils.processRemoteException(ex);
        } finally {
            progressHandle.finish();
        }
        return items != null ? Collections.synchronizedList(items) : null;
    }

    void showPanel(final List<SyncItem> files, final SyncResultProcessor resultProcessor) {
        if (cancelled || files == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SyncPanel panel = new SyncPanel(phpProject.getName(), remoteConfiguration.getDisplayName(), files);
                if (panel.open(lastTimeStamp == -1)) {
                    doSynchronize(files, resultProcessor);
                } else {
                    disconnect();
                }
            }
        });
    }

    @NbBundle.Messages("SyncController.error.unknown=Unknown reason")
    void doSynchronize(final List<SyncItem> files, final SyncResultProcessor resultProcessor) {
        if (cancelled) {
            // in fact, cannot happen here
            return;
        }
        SYNC_RP.post(new Runnable() {
            @Override
            public void run() {
                SyncResult syncResult = new SyncResult();
                for (SyncItem fileItem : files) {
                    TransferFile remoteTransferFile = fileItem.getRemoteTransferFile();
                    TransferFile localTransferFile = fileItem.getLocalTransferFile();
                    switch (fileItem.getOperation()) {
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
                                TransferInfo uploadInfo = remoteClient.upload(Collections.singleton(localTransferFile));
                                mergeTransferInfo(uploadInfo, syncResult.getUploadTransferInfo());
                            } catch (RemoteException ex) {
                                syncResult.getUploadTransferInfo().addFailed(localTransferFile, ex.getLocalizedMessage());
                            }
                            break;
                        case DELETE_LOCALLY:
                            // XXX recursive delete
                            long start = System.currentTimeMillis();
                            if (!fileItem.getLocalTransferFile().resolveLocalFile().delete()) {
                                syncResult.getDeleteLocallyTransferInfo().addFailed(remoteTransferFile, Bundle.SyncController_error_unknown());
                            }
                            break;
                        case DELETE_REMOTELY:
                            try {
                                // XXX recursive delete
                                TransferInfo deleteInfo = remoteClient.delete(remoteTransferFile);
                                mergeTransferInfo(deleteInfo, syncResult.getDeleteRemotelyTransferInfo());
                            } catch (RemoteException ex) {
                                syncResult.getDeleteRemotelyTransferInfo().addFailed(remoteTransferFile, ex.getLocalizedMessage());
                            }
                            break;
                        default:
                            assert false : "Unsupported synchronization operation: " + fileItem.getOperation();
                    }
                }
                disconnect();
                ProjectSettings.setSyncTimestamp(phpProject, TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
                resultProcessor.process(syncResult);
            }

            private void mergeTransferInfo(TransferInfo from, TransferInfo to) {
                to.setRuntime(to.getRuntime() + from.getRuntime());
                to.getTransfered().addAll(from.getTransfered());
                to.getIgnored().putAll(from.getIgnored());
                to.getPartiallyFailed().putAll(from.getPartiallyFailed());
                to.getFailed().putAll(from.getFailed());
            }

        });
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
        } catch (RemoteException ex1) {
            // XXX log INFO
        }
    }

    private List<SyncItem> pairFiles(Set<TransferFile> remoteFiles, Set<TransferFile> localFiles) {
        List<TransferFile> remoteFilesSorted = new ArrayList<TransferFile>(remoteFiles);
        Collections.sort(remoteFilesSorted, TransferFile.TRANSFER_FILE_COMPARATOR);
        List<TransferFile> localFilesSorted = new ArrayList<TransferFile>(localFiles);
        Collections.sort(localFilesSorted, TransferFile.TRANSFER_FILE_COMPARATOR);

        removeProjectRoot(remoteFilesSorted);
        removeProjectRoot(localFilesSorted);

        List<SyncItem> result = new ArrayList<SyncItem>(Math.max(remoteFiles.size(), localFiles.size()));
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
                result.add(new SyncItem(remote, local, lastTimeStamp));
                remote = null;
                local = null;
            } else {
                int compare = TransferFile.TRANSFER_FILE_COMPARATOR.compare(remote, local);
                if (compare == 0) {
                    // same remote paths
                    result.add(new SyncItem(remote, local, lastTimeStamp));
                    remote = null;
                    local = null;
                } else if (compare < 0) {
                    result.add(new SyncItem(remote, null, lastTimeStamp));
                    remote = null;
                } else {
                    result.add(new SyncItem(null, local, lastTimeStamp));
                    local = null;
                }
            }
        }
        return result;
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
        private final TransferInfo deleteLocallyTransferInfo = new TransferInfo();
        private final TransferInfo deleteRemotelyTransferInfo = new TransferInfo();


        SyncResult() {
        }

        public TransferInfo getDeleteLocallyTransferInfo() {
            return deleteLocallyTransferInfo;
        }

        public TransferInfo getDeleteRemotelyTransferInfo() {
            return deleteRemotelyTransferInfo;
        }

        public TransferInfo getDownloadTransferInfo() {
            return downloadTransferInfo;
        }

        public TransferInfo getUploadTransferInfo() {
            return uploadTransferInfo;
        }

    }

    public interface SyncResultProcessor {
        void process(SyncResult result);
    }

}
