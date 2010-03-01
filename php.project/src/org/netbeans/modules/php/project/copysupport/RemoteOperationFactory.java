/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.copysupport;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpVisibilityQuery;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.TransferInfo;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;


/**
 * @author Radek Matous
 */
final class RemoteOperationFactory extends FileOperationFactory {
    private static final Logger LOGGER = Logger.getLogger(RemoteOperationFactory.class.getName());

    // @GuardedBy(this)
    private RemoteClient remoteClient;

    RemoteOperationFactory(PhpProject project) {
        super(project);
    }

    @Override
    protected boolean isEnabled() {
        return isEnabled(true);
    }

    private boolean isEnabled(boolean verbose) {
        boolean remoteConfigSelected = isRemoteConfigSelected();
        boolean uploadOnSave = false;
        if (remoteConfigSelected) {
            uploadOnSave = isUploadOnSave();
        }
        if (verbose) {
            LOGGER.log(Level.FINE, "REMOTE copying enabled for project {0}: {1}", new Object[] {project.getName(), remoteConfigSelected && uploadOnSave});
            if (!remoteConfigSelected) {
                LOGGER.fine("\t-> remote config not selected");
            }
            if (!uploadOnSave) {
                LOGGER.fine("\t-> upload on save not selected");
            }
        }
        return remoteConfigSelected && uploadOnSave;
    }

    @Override
    protected synchronized void resetInternal() {
        remoteClient = null;
    }

    @Override
    Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected Callable<Boolean> createInitHandlerInternal(final FileObject source) {
        LOGGER.log(Level.FINE, "No INIT handler needed for project {0}", project.getName());
        return null;
    }

    @Override
    protected Callable<Boolean> createCopyHandlerInternal(final FileObject source, FileEvent fileEvent) {
        LOGGER.log(Level.FINE, "Creating COPY handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running COPY handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                if (!isValid(source)) {
                    return null;
                }

                RemoteClient client = getRemoteClient();
                try {
                    return doCopy(client, source);
                } finally {
                    client.disconnect();
                }
            }
        };
    }

    @Override
    protected Callable<Boolean> createRenameHandlerInternal(final FileObject source, final String oldName, FileRenameEvent fileRenameEvent) {
        LOGGER.log(Level.FINE, "Creating RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running RENAME handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                if (!isValid(source)) {
                    return null;
                }

                RemoteClient client = getRemoteClient();
                try {
                    return doRename(client, source, oldName);
                } finally {
                    client.disconnect();
                }
            }
        };
    }

    @Override
    protected Callable<Boolean> createDeleteHandlerInternal(final FileObject source, FileEvent fileEvent) {
        LOGGER.log(Level.FINE, "Creating DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
        return new Callable<Boolean>() {
            public Boolean call() throws Exception {
                LOGGER.log(Level.FINE, "Running DELETE handler for {0} (project {1})", new Object[] {getPath(source), project.getName()});
                if (!isValid(source)) {
                    return null;
                }

                RemoteClient client = getRemoteClient();
                try {
                    return doDelete(client, source);
                } finally {
                    client.disconnect();
                }
            }
        };
    }

    private boolean isValid(FileObject source) {
        LOGGER.log(Level.FINE, "Validating source {0} for {1}", new Object[] {getPath(source), project.getName()});
        if (!isRemoteConfigValid()) {
            LOGGER.fine("\t-> invalid (invalid config)");
            return false;
        }
        if (!isSourceFileValid(source)) {
            LOGGER.fine("\t-> invalid (invalid source)");
            return false;
        }
        return true;
    }

    protected boolean isRemoteConfigValid() {
        if (!isEnabled(false)) {
            LOGGER.log(Level.FINE, "REMOTE copying not enabled for project {0}", project.getName());
            return false;
        }
        if (isInvalid()) {
            LOGGER.log(Level.FINE, "REMOTE copying invalid for project {0}", project.getName());
            return false;
        }
        if (getSources() == null) {
            LOGGER.log(Level.WARNING, "REMOTE copying disabled for project {0}. Reason: source root is null", project.getName());
            return false;
        }
        if (getRemoteConfiguration() == null) {
            LOGGER.log(Level.INFO, "REMOTE copying disabled for project {0}. Reason: remote config not found", project.getName());

            if (askUser(NbBundle.getMessage(RemoteOperationFactory.class, "MSG_RemoteConfigNotFound", project.getName()))) {
                showCustomizer();
            }
            invalidate();
            return false;
        }
        // XXX validate remote config, see below
        // XXX no UI I supposed to be called from  ConfigAction.isValid(false)
        /*
        java.lang.IllegalStateException: Should not acquire Children.MUTEX while holding ProjectManager.mutex()
        at org.openide.nodes.Children$ProjectManagerDeadlockDetector.execute(Children.java:1805)
        at org.openide.util.Mutex.doWrapperAccess(Mutex.java:1320)
        at org.openide.util.Mutex.readAccess(Mutex.java:351)
        at org.openide.explorer.ExplorerManager.setRootContext(ExplorerManager.java:499)
        at org.netbeans.modules.project.uiapi.CategoryView.<init>(CategoryView.java:91)
        at org.netbeans.spi.project.ui.support.ProjectCustomizer.createCustomizerPane(ProjectCustomizer.java:250)
        at org.netbeans.spi.project.ui.support.ProjectCustomizer.createCustomizerDialog(ProjectCustomizer.java:156)
        at org.netbeans.spi.project.ui.support.ProjectCustomizer.createCustomizerDialog(ProjectCustomizer.java:236)
        at org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl$1.run(CustomizerProviderImpl.java:96)
        at org.openide.util.Mutex.doEvent(Mutex.java:1335)
        at org.openide.util.Mutex.readAccess(Mutex.java:345)
        at org.netbeans.modules.php.project.ui.customizer.CustomizerProviderImpl.showCustomizer(CustomizerProviderImpl.java:82)
        at org.netbeans.modules.php.project.ui.actions.support.ConfigAction.showCustomizer(ConfigAction.java:138)
        at org.netbeans.modules.php.project.ui.actions.support.ConfigActionRemote.isValid(ConfigActionRemote.java:75)
        at org.netbeans.modules.php.project.util.RemoteOperationFactory.isValidRemoteConfig(RemoteOperationFactory.java:126)
        at org.netbeans.modules.php.project.util.RemoteOperationFactory.isEnabled(RemoteOperationFactory.java:69)
        at org.netbeans.modules.php.project.util.CopySupport$CopyImpl.prepareOperation(CopySupport.java:233)
        at org.netbeans.modules.php.project.util.CopySupport$CopyImpl.fileChanged(CopySupport.java:162)
         */
        //ConfigAction action = ConfigAction.get(Type.REMOTE, project);
        //return action.isValid(false);
        return true;
    }

    protected synchronized RemoteClient getRemoteClient() {
        if (remoteClient == null) {
            remoteClient = new RemoteClient(getRemoteConfiguration(), new RemoteClient.AdvancedProperties()
                    .setAdditionalInitialSubdirectory(ProjectPropertiesSupport.getRemoteDirectory(project))
                    .setPreservePermissions(ProjectPropertiesSupport.areRemotePermissionsPreserved(project))
                    .setUploadDirectly(ProjectPropertiesSupport.isRemoteUploadDirectly(project))
                    .setPhpVisibilityQuery(PhpVisibilityQuery.forProject(project)));
        }
        return remoteClient;
    }

    protected boolean isUploadOnSave() {
        return UploadFiles.ON_SAVE.equals(ProjectPropertiesSupport.getRemoteUpload(project));
    }

    protected boolean isRemoteConfigSelected() {
        return RunAsType.REMOTE.equals(ProjectPropertiesSupport.getRunAs(project));
    }

    protected RemoteConfiguration getRemoteConfiguration() {
        String configName = ProjectPropertiesSupport.getRemoteConnection(project);
        assert StringUtils.hasText(configName) : "Remote configuration name must be selected for project " + project.getName();
        return RemoteConnections.get().remoteConfigurationForName(configName);
    }

    Boolean doCopy(RemoteClient client, FileObject source) throws RemoteException {
        LOGGER.log(Level.FINE, "Uploading file {0} for project {1}", new Object[] {getPath(source), project.getName()});
        FileObject sourceRoot = getSources();
        Set<TransferFile> transferFiles = client.prepareUpload(sourceRoot, source);
        if (transferFiles.size() > 0) {
            TransferInfo transferInfo = client.upload(sourceRoot, transferFiles);
            if (!transferInfo.hasAnyFailed()
                    && !transferInfo.hasAnyPartiallyFailed()
                    && !transferInfo.hasAnyIgnored()) {
                LOGGER.fine("\t-> success");
                return true;
            }
            LOGGER.fine("\t-> failure");
            LOGGER.log(Level.INFO, "Upload failed: {0}", transferInfo);
            return false;
        }
        LOGGER.fine("\t-> nothing to upload?!");
        return null;
    }

    Boolean doRename(RemoteClient client, FileObject source, String oldName) throws RemoteException {
        FileObject sourceRoot = getSources();
        String baseDirectory = FileUtil.toFile(sourceRoot).getAbsolutePath();
        File sourceFile = FileUtil.toFile(source);
        TransferFile toTransferFile = TransferFile.fromFileObject(source, baseDirectory);
        TransferFile fromTransferFile = TransferFile.fromFile(new File(sourceFile.getParentFile(), oldName), baseDirectory);
        LOGGER.log(Level.FINE, "Renaming file {0} -> {1} for project {2}", new Object[] {fromTransferFile.getRelativePath(), toTransferFile.getRelativePath(), project.getName()});
        if (client.exists(fromTransferFile)) {
            if (client.rename(fromTransferFile, toTransferFile)) {
                LOGGER.fine("\t-> success");
                return true;
            } else {
                LOGGER.fine("\t-> failure");
                return false;
            }
        }
        // file not exist remotely => simply upload it
        LOGGER.fine("\t-> does not exist -> uploading");
        return doCopy(client, source);
    }

    Boolean doDelete(RemoteClient client, FileObject source) throws RemoteException {
        LOGGER.log(Level.FINE, "Deleting file {0} for project {1}", new Object[] {getPath(source), project.getName()});
        Boolean success = null;
        Set<TransferFile> transferFiles = client.prepareDelete(getSources(), source);
        for (TransferFile file : transferFiles) {
            LOGGER.log(Level.FINE, "Deleting remote file {0}", file.getRelativePath());
            if (!client.exists(file)) {
                LOGGER.fine("\t-> does not exist -> ignoring");
            } else {
                TransferInfo transferInfo = client.delete(file);
                if (transferInfo.hasAnyTransfered()) {
                    LOGGER.fine("\t-> success");
                } else {
                    LOGGER.fine("\t-> failure");
                    LOGGER.log(Level.INFO, "Remote delete failed: {0}", transferInfo);
                    success = false;
                }
            }
        }
        return success;
    }

    @Override
    protected boolean isValid(FileEvent fileEvent) {
        return !fileEvent.firedFrom(RemoteClient.DOWNLOAD_ATOMIC_ACTION);
    }
}
