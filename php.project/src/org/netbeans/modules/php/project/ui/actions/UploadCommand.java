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
import java.io.File;
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
import org.netbeans.modules.php.project.connections.ui.transfer.TransferFilesChooser;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Upload files to remote connection.
 * @author Tomas Mysik
 */
public class UploadCommand extends RemoteCommand implements Displayable {
    public static final String ID = "upload"; // NOI18N
    public static final String DISPLAY_NAME = NbBundle.getMessage(UploadCommand.class, "LBL_UploadCommand");

    public UploadCommand(PhpProject project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ID;
    }

    @Override
    protected Runnable getContextRunnable(final Lookup context) {
        return new Runnable() {
            @Override
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

        uploadFiles(selectedFiles, (FileObject[]) null);
    }

    public void uploadFiles(FileObject[] filesToUpload, FileObject[] preselectedFiles) {

        FileObject sources = ProjectPropertiesSupport.getSourcesDirectory(getProject());

        if (!sourcesFilesOnly(sources, filesToUpload)) {
            return;
        }

        InputOutput remoteLog = getRemoteLog(getRemoteConfiguration().getDisplayName());
        DefaultOperationMonitor uploadOperationMonitor = new DefaultOperationMonitor("LBL_Uploading"); // NOI18N
        RemoteClient remoteClient = getRemoteClient(remoteLog, uploadOperationMonitor);
        String progressTitle = NbBundle.getMessage(UploadCommand.class, "MSG_UploadingFiles", getProject().getName());
        ProgressHandle progressHandle = ProgressHandleFactory.createHandle(progressTitle, remoteClient);
        TransferInfo transferInfo = null;
        try {
            progressHandle.start();
            Set<TransferFile> forUpload = remoteClient.prepareUpload(sources, filesToUpload);

            // manage preselected files - it is just enough to touch the file
            if (preselectedFiles != null && preselectedFiles.length > 0) {
                File baseLocalDir = FileUtil.toFile(sources);
                String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
                for (FileObject fo : preselectedFiles) {
                    // we need to touch the _original_ transfer file because of its parent!
                    TransferFile transferFile = TransferFile.fromFileObject(null, fo, baseLocalAbsolutePath);
                    for (TransferFile file : forUpload) {
                        if (transferFile.equals(file)) {
                            file.touch();
                            break;
                        }
                    }
                }
            }

            forUpload = TransferFilesChooser.forUpload(forUpload, ProjectSettings.getLastUpload(getProject())).showDialog();
            if (forUpload.isEmpty()) {
                return;
            }

            if (forUpload.size() > 0) {
                progressHandle.finish();
                progressHandle = ProgressHandleFactory.createHandle(progressTitle, remoteClient);
                uploadOperationMonitor.progressHandle = progressHandle;
                progressHandle.start(getWorkUnits(forUpload));
                transferInfo = remoteClient.upload(sources, forUpload);
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(UploadCommand.class, "MSG_UploadFinished", getProject().getName()));
                if (!remoteClient.isCancelled()
                        && transferInfo.hasAnyTransfered()) { // #153406
                    rememberLastUpload(sources, filesToUpload);
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

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    // #142955 - but remember only if one of the selected files is source directory
    //  (otherwise it would make no sense, consider this scenario: upload just one file -> remember timestamp
    //  -> upload another file or the whole project [timestamp is irrelevant])
    private void rememberLastUpload(FileObject sources, FileObject[] selectedFiles) {
        for (FileObject fo : selectedFiles) {
            if (sources.equals(fo)) {
                ProjectSettings.setLastUpload(getProject(), TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
                return;
            }
        }
    }
}
