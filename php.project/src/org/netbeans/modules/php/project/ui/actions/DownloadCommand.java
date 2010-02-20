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

package org.netbeans.modules.php.project.ui.actions;

import org.netbeans.modules.php.project.ui.actions.support.Displayable;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ProjectSettings;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.TransferInfo;
import org.netbeans.modules.php.project.connections.ui.TransferFilter;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Download files from remote connection.
 * @author Tomas Mysik
 */
public class DownloadCommand extends RemoteCommand implements Displayable {
    public static final String ID = "download"; // NOI18N
    public static final String DISPLAY_NAME = NbBundle.getMessage(DownloadCommand.class, "LBL_DownloadCommand");

    public DownloadCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    protected Runnable getContextRunnable(final Lookup context) {
        return new Runnable() {
            public void run() {
                invokeActionImpl(context);
            }
        };
    }

    void invokeActionImpl(Lookup context) {
        FileObject[] selectedFiles = CommandUtils.filesForContextOrSelectedNodes(context);
        // #161202
        if (selectedFiles.length == 0) {
            // one selects project node e.g.
            return;
        }

        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(getProject());
        if (!sourcesFilesOnly(sources, selectedFiles)) {
            return;
        }

        InputOutput remoteLog = getRemoteLog(getRemoteConfiguration().getDisplayName());
        DefaultOperationMonitor downloadOperationMonitor = new DefaultOperationMonitor("LBL_Downloading"); // NOI18N
        RemoteClient remoteClient = getRemoteClient(remoteLog, downloadOperationMonitor);
        String projectName = getProject().getName();
        download(remoteClient, remoteLog, downloadOperationMonitor, projectName, true, sources, selectedFiles, null, getProject());
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public static void download(RemoteClient remoteClient, InputOutput remoteLog, DefaultOperationMonitor operationMonitor, String projectName,
            FileObject sources, Set<TransferFile> forDownload) {
        download(remoteClient, remoteLog, operationMonitor, projectName, false, sources, null, forDownload, null);
    }

    private static void download(RemoteClient remoteClient, InputOutput remoteLog, DefaultOperationMonitor operationMonitor, String projectName,
            boolean showDownloadDialog, FileObject sources, FileObject[] filesToDownload, Set<TransferFile> transferFilesToDownload, PhpProject project) {
        String progressTitle = NbBundle.getMessage(DownloadCommand.class, "MSG_DownloadingFiles", projectName);
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(progressTitle, remoteClient);
        TransferInfo transferInfo = null;
        try {
            progressHandle.start();
            Set<TransferFile> forDownload = transferFilesToDownload != null ? transferFilesToDownload : remoteClient.prepareDownload(sources, filesToDownload);

            if (showDownloadDialog) {
                // avoid timeout errors
                remoteClient.disconnect();
                long timestamp = project != null ? ProjectSettings.getLastDownload(project) : -1;
                forDownload = TransferFilter.showDownloadDialog(forDownload, timestamp);
            }

            if (forDownload.size() > 0) {
                progressHandle.finish();
                progressHandle = ProgressHandleFactory.createHandle(progressTitle, remoteClient);
                operationMonitor.progressHandle = progressHandle;
                int workUnits = getWorkUnits(forDownload);
                if (workUnits > 0) {
                    progressHandle.start(workUnits);
                } else {
                    progressHandle.start();
                }
                transferInfo = remoteClient.download(sources, forDownload);
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(DownloadCommand.class, "MSG_DownloadFinished", projectName));
                if (project != null
                        && !remoteClient.isCancelled()
                        && transferInfo.hasAnyTransfered()) { // #153406
                    rememberLastDownload(project, sources, filesToDownload);
                }
            }
        } catch (RemoteException ex) {
            processRemoteException(ex);
        } finally {
            try {
                remoteClient.disconnect();
            } catch (RemoteException ex) {
                processRemoteException(ex);
            }
            if (transferInfo != null) {
                processTransferInfo(transferInfo, remoteLog);
            }
            progressHandle.finish();
        }
    }

    // #142955 - but remember only if one of the selected files is source directory
    //  (otherwise it would make no sense, consider this scenario: upload just one file -> remember timestamp
    //  -> upload another file or the whole project [timestamp is irrelevant])
    private static void rememberLastDownload(PhpProject project, FileObject sources, FileObject[] selectedFiles) {
        for (FileObject fo : selectedFiles) {
            if (sources.equals(fo)) {
                ProjectSettings.setLastDownload(project, TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
                return;
            }
        }
    }
}
