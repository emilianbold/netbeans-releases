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
package org.netbeans.modules.php.project.util;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.RemoteConnections;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.TransferInfo;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Radek Matous
 */
final class RemoteOperationFactory extends FileOperationFactory {

    private RemoteClient remoteClient;
    private final PhpProject project;

    RemoteOperationFactory(PhpProject project) {
        this.project = project;
    }

    private boolean isEnabled() {
        boolean uploadOnSave = isRemoteConfigSelected(project) ? isUploadOnSave(project) : false;
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        return (uploadOnSave) ? isValidRemoteConfig(project) && sourcesDirectory != null : false;
    }

    @Override
    Callable<Boolean> createCopyHandler(final FileObject source) {
        return (isEnabled()) ? new Callable<Boolean>() {

            public Boolean call() throws Exception {
                RemoteClient client = getRemoteClient(project);
                Set<TransferFile> transferFiles = Collections.emptySet();
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
                FileObject sourceRoot = (client != null) ? sourcesDirectory : null;
                boolean sourceFileValid = sourceRoot != null ? isSourceFileValid(sourceRoot, source) : false;
                if (sourceFileValid) {
                    transferFiles = client.prepareUpload(sourceRoot, source);
                }
                if (client != null) {
                    try {
                        if (transferFiles.size() > 0) {
                            TransferInfo transferInfo = client.upload(sourceRoot, transferFiles);
                            return !transferInfo.hasAnyFailed()
                                    && !transferInfo.hasAnyPartiallyFailed()
                                    && !transferInfo.hasAnyIgnored();
                        }
                    } finally {
                        client.disconnect();
                    }
                }
                return false;
            }
        } : null;
    }

    @Override
    Callable<Boolean> createDeleteHandler(final FileObject source) {
        return (isEnabled()) ? new Callable<Boolean>() {

            public Boolean call() throws Exception {
                boolean success = false;

                RemoteClient client = getRemoteClient(project);
                Set<TransferFile> transferFiles = Collections.emptySet();
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
                FileObject sourceRoot = (client != null) ? sourcesDirectory : null;
                boolean sourceFileValid = sourceRoot != null ? isSourceFileValid(sourceRoot, source) : false;
                if (sourceFileValid) {
                    transferFiles = client.prepareDelete(sourceRoot, source);
                }
                if (client != null) {
                    success = true;
                    try {
                        for (TransferFile file : transferFiles) {
                            if (client.exists(file)) {
                                TransferInfo transferInfo = client.delete(file);
                                if (!transferInfo.hasAnyTransfered()) {
                                    success = false;
                                }
                            }
                        }
                    } finally {
                        client.disconnect();
                    }
                }
                return success;
            }
        } : null;
    }

    @Override
    Callable<Boolean> createInitHandler(final FileObject source) {
        return null;
    }

    @Override
    Callable<Boolean> createRenameHandler(final FileObject source, final String oldName) {
                return (isEnabled()) ? new Callable<Boolean>() {

            public Boolean call() throws Exception {
                RemoteClient client = getRemoteClient(project);
                TransferFile fromTransferFile = null;
                TransferFile toTransferFile = null;
                FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
                FileObject sourceRoot = (client != null) ? sourcesDirectory : null;
                boolean sourceFileValid = sourceRoot != null ? isSourceFileValid(sourceRoot, source) : false;
                if (sourceFileValid) {
                    String baseDirectory = FileUtil.toFile(sourcesDirectory).getAbsolutePath();
                    File sourceFile = FileUtil.toFile(source);
                    toTransferFile = TransferFile.fromFileObject(source,baseDirectory);
                    fromTransferFile = TransferFile.fromFile(new File(sourceFile.getParentFile(), oldName),baseDirectory);
                }
                if (client != null) {
                    try {
                        if (fromTransferFile != null && toTransferFile != null) {
                            if (client.exists(fromTransferFile)) {
                                return client.rename(fromTransferFile, toTransferFile);
                            } else {
                                // file not exists remotely => not error
                                return true;
                            }
                        }
                    } finally {
                        client.disconnect();
                    }
                }
                return false;
            }
        } : null;

    }

    protected static boolean isValidRemoteConfig(PhpProject project) {
        //TODO: no UI I supposed to be called from  ConfigAction.isValid(false)
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
        return getRemoteConfiguration(project) != null;
    }

    protected static boolean isUploadOnSave(PhpProject project) {
        UploadFiles howToUpload = ProjectPropertiesSupport.getRemoteUpload(project);
        return (howToUpload != null) ? UploadFiles.ON_SAVE.equals(howToUpload) : false;
    }

    protected static boolean isRemoteConfigSelected(PhpProject project) {
        PhpProjectProperties.RunAsType runAs = ProjectPropertiesSupport.getRunAs(project);
        return PhpProjectProperties.RunAsType.REMOTE.equals(runAs);
    }

    protected synchronized RemoteClient getRemoteClient(PhpProject project) {
        if (remoteClient == null) {
            remoteClient = new RemoteClient(getRemoteConfiguration(project), new RemoteClient.AdvancedProperties()
                    .setAdditionalInitialSubdirectory(getRemoteDirectory(project))
                    .setPreservePermissions(ProjectPropertiesSupport.areRemotePermissionsPreserved(project))
                    .setUploadDirectly(ProjectPropertiesSupport.isRemoteUploadDirectly(project)));
        }
        return remoteClient;
    }

    @Override
    synchronized void invalidate() {
        remoteClient = null;
    }

    protected static RemoteConfiguration getRemoteConfiguration(PhpProject project) {
        String configName = getRemoteConfigurationName(project);
        assert configName != null && configName.length() > 0 : "Remote configuration name must be selected";

        return RemoteConnections.get().remoteConfigurationForName(configName);
    }

    protected static String getRemoteConfigurationName(PhpProject project) {
        return ProjectPropertiesSupport.getRemoteConnection(project);
    }

    protected static String getRemoteDirectory(PhpProject project) {
        return ProjectPropertiesSupport.getRemoteDirectory(project);
    }
}
