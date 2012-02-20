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
package org.netbeans.modules.php.project.connections.synchronize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.common.RemoteUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.transfer.TransferFile;
import org.openide.filesystems.FileObject;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Controller for synchronization.
 */
public final class SynchronizeController implements Cancellable {

    static final RequestProcessor SYNCHRONIZE_RP = new RequestProcessor("Remote Synchronization", 1); // NOI18N

    final PhpProject phpProject;
    final RemoteClient remoteClient;
    final RemoteConfiguration remoteConfiguration;
    final Long lastTimeStamp = null;

    volatile boolean cancelled = false;


    public SynchronizeController(PhpProject phpProject, RemoteClient remoteClient, RemoteConfiguration remoteConfiguration) {
        this.phpProject = phpProject;
        this.remoteClient = remoteClient;
        this.remoteConfiguration = remoteConfiguration;
    }

    public void synchronize() {
        SYNCHRONIZE_RP.post(new Runnable() {
            @Override
            public void run() {
                showPanel(fetchFiles());
            }
        });
    }

    @NbBundle.Messages("SynchronizeController.fetching=Fetching {0} files")
    List<FileItem> fetchFiles() {
        assert !SwingUtilities.isEventDispatchThread();
        List<FileItem> items = null;
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(Bundle.SynchronizeController_fetching(phpProject.getName()), this);
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

    void showPanel(final List<FileItem> files) {
        if (cancelled || files == null) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SynchronizePanel panel = new SynchronizePanel(phpProject.getName(), remoteConfiguration.getDisplayName(), files);
                if (panel.open(lastTimeStamp == null)) {
                    doSynchronize(files);
                } else {
                    disconnect();
                }
            }
        });
    }

    void doSynchronize(final List<FileItem> files) {
        if (cancelled) {
            // in fact, cannot happen here
            return;
        }
        SYNCHRONIZE_RP.post(new Runnable() {
            @Override
            public void run() {
                // XXX synchronize
                System.out.println("--------------- synchronizing...");
                disconnect();
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

    private List<FileItem> pairFiles(Set<TransferFile> remoteFiles, Set<TransferFile> localFiles) {
        List<TransferFile> remoteFilesSorted = new ArrayList<TransferFile>(remoteFiles);
        Collections.sort(remoteFilesSorted, TransferFile.TRANSFER_FILE_COMPARATOR);
        List<TransferFile> localFilesSorted = new ArrayList<TransferFile>(localFiles);
        Collections.sort(localFilesSorted, TransferFile.TRANSFER_FILE_COMPARATOR);

        removeProjectRoot(remoteFilesSorted);
        removeProjectRoot(localFilesSorted);

        List<FileItem> result = new ArrayList<FileItem>(Math.max(remoteFiles.size(), localFiles.size()));
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
                result.add(new FileItem(remote, local, lastTimeStamp));
                remote = null;
                local = null;
            } else {
                int compare = TransferFile.TRANSFER_FILE_COMPARATOR.compare(remote, local);
                if (compare == 0) {
                    // same remote paths
                    result.add(new FileItem(remote, local, lastTimeStamp));
                    remote = null;
                    local = null;
                } else if (compare < 0) {
                    result.add(new FileItem(remote, null, lastTimeStamp));
                    remote = null;
                } else {
                    result.add(new FileItem(null, local, lastTimeStamp));
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

}
