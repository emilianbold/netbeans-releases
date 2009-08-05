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

package org.netbeans.modules.php.project.connections;

import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.spi.RemoteConnectionProvider;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.windows.InputOutput;

/**
 * Remote client able to connect/disconnect to a remote server
 * as well as download/upload files from/to a remote server.
 * <p>
 * Every method throws {@link RemoteException} if any error occurs.
 * <p>
 * This class is not threadsafe.
 * @author Tomas Mysik
 */
public final class RemoteClient implements Cancellable {
    private static final Logger LOGGER = Logger.getLogger(RemoteClient.class.getName());
    private static final AdvancedProperties DEFAULT_ADVANCED_PROPERTIES = new AdvancedProperties();
    private static final OperationMonitor DEV_NULL_OPERATION_MONITOR = new DevNullOperationMonitor();
    private static final Set<String> IGNORED_DIRS = new HashSet<String>(Arrays.asList(".", "..", "nbproject")); // NOI18N
    private static final int TRIES_TO_TRANSFER = 3; // number of tries if file download/upload fails
    private static final String LOCAL_TMP_NEW_SUFFIX = ".new~"; // NOI18N
    private static final String LOCAL_TMP_OLD_SUFFIX = ".old~"; // NOI18N
    private static final String REMOTE_TMP_NEW_SUFFIX = ".new"; // NOI18N
    private static final String REMOTE_TMP_OLD_SUFFIX = ".old"; // NOI18N

    public static enum Operation { UPLOAD, DOWNLOAD, DELETE };

    private final RemoteConfiguration configuration;
    private final AdvancedProperties properties;
    private final OperationMonitor operationMonitor;
    private final String baseRemoteDirectory;
    private final org.netbeans.modules.php.project.connections.spi.RemoteClient remoteClient;
    private volatile boolean cancelled = false;

    /**
     * @see RemoteClient#RemoteClient(org.netbeans.modules.php.project.connections.spi.RemoteConfiguration, org.openide.windows.InputOutput, java.lang.String, boolean)
     */
    public RemoteClient(RemoteConfiguration configuration) {
        this(configuration, DEFAULT_ADVANCED_PROPERTIES);
    }

    /**
     * Create a new remote client.
     * @param configuration {@link RemoteConfiguration remote configuration} of a connection.
     * @param properties advanced properties of a connection.
     */
    public RemoteClient(RemoteConfiguration configuration, AdvancedProperties properties) {
        assert configuration != null;
        assert properties != null;

        this.configuration = configuration;
        this.properties = properties;

        OperationMonitor monitor = properties.getOperationMonitor();
        if (monitor != null) {
            operationMonitor = monitor;
        } else {
            operationMonitor = DEV_NULL_OPERATION_MONITOR;
        }

        // base remote directory
        StringBuilder baseDirBuffer = new StringBuilder(configuration.getInitialDirectory());
        String additionalInitialSubdirectory = properties.getAdditionalInitialSubdirectory();
        if (StringUtils.hasText(additionalInitialSubdirectory)) {
            if (!additionalInitialSubdirectory.startsWith(TransferFile.SEPARATOR)) {
                throw new IllegalArgumentException("additionalInitialSubdirectory must start with " + TransferFile.SEPARATOR);
            }
            baseDirBuffer.append(additionalInitialSubdirectory);
        }
        String baseDir = baseDirBuffer.toString();
        // #150646 - should not happen now, likely older nb project metadata
        if (baseDir.length() > 1
                && baseDir.endsWith(TransferFile.SEPARATOR)) {
            baseDir = baseDir.substring(0, baseDir.length() - 1);
        }

        baseRemoteDirectory = baseDir.replaceAll(TransferFile.SEPARATOR + "{2,}", TransferFile.SEPARATOR); // NOI18N

        assert baseRemoteDirectory.startsWith(TransferFile.SEPARATOR) : "base directory must start with " + TransferFile.SEPARATOR + ": " + baseRemoteDirectory;

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Remote client created with configuration: %s, advanced properties: %s, base remote directory: %s",
                    configuration, properties, baseRemoteDirectory));
        }


        // remote client itself
        org.netbeans.modules.php.project.connections.spi.RemoteClient client = null;
        for (RemoteConnectionProvider provider : RemoteConnections.get().getConnectionProviders()) {
            client = provider.getRemoteClient(configuration, properties.getInputOutput());
            if (client != null) {
                break;
            }
        }
        assert client != null : "no suitable remote client for configuration: " + configuration;
        this.remoteClient = client;
    }

    public void connect() throws RemoteException {
        remoteClient.connect();
        assert remoteClient.isConnected() : "Remote client should be connected";

        // cd to base remote directory
        if (!cdBaseRemoteDirectory()) {
            if (remoteClient.isConnected()) {
                disconnect();
            }
            throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", baseRemoteDirectory), remoteClient.getReplyString());
        }
    }

    public void disconnect() throws RemoteException {
        remoteClient.disconnect();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public void reset() {
        cancelled = false;
    }

    public boolean exists(TransferFile file) throws RemoteException {
        ensureConnected();

        LOGGER.fine(String.format("Checking whether file %s exists", file));
        cdBaseRemoteDirectory();
        boolean exists = remoteClient.exists(file.getParentRelativePath(), file.getName());
        LOGGER.fine(String.format("Exists: %b", exists));
        return exists;
    }

    public boolean rename(TransferFile from, TransferFile to) throws RemoteException {
        ensureConnected();

        LOGGER.fine(String.format("Moving file from %s to %s", from, to));
        cdBaseRemoteDirectory();
        boolean success = remoteClient.rename(from.getRelativePath(), to.getRelativePath());
        LOGGER.fine(String.format("Success: %b", success));
        return success;
    }

    public Set<TransferFile> prepareUpload(FileObject baseLocalDirectory, FileObject... filesToUpload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToUpload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToUpload.length > 0 : "At least one file to upload must be specified";

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
        Queue<TransferFile> queue = new LinkedList<TransferFile>();
        for (FileObject fo : filesToUpload) {
            if (isVisible(FileUtil.toFile(fo))) {
                LOGGER.fine("File " + fo + " added to upload queue");
                queue.offer(TransferFile.fromFileObject(fo, baseLocalAbsolutePath));
            } else {
                LOGGER.fine("File " + fo + " NOT added to upload queue [invisible]");
            }
        }

        Set<TransferFile> files = new HashSet<TransferFile>();
        while(!queue.isEmpty()) {
            if (cancelled) {
                LOGGER.fine("Prepare upload cancelled");
                break;
            }

            TransferFile file = queue.poll();

            if (!files.add(file)) {
                // file already in set
                LOGGER.fine("File " + file + " already in queue");
                continue;
            }

            if (file.isDirectory()) {
                File f = getLocalFile(baseLocalDir, file);
                File[] children = f.listFiles();
                if (children != null) {
                    for (File child : children) {
                        if (isVisible(child)) {
                            LOGGER.fine("File " + child + " added to upload queue");
                            queue.offer(TransferFile.fromFile(child, baseLocalAbsolutePath));
                        } else {
                            LOGGER.fine("File " + child + " NOT added to upload queue [invisible]");
                        }
                    }
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Prepared for upload: " + files);
        }
        return files;
    }

    public TransferInfo upload(FileObject baseLocalDirectory, Set<TransferFile> filesToUpload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToUpload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToUpload.size() > 0 : "At least one file to upload must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        TransferInfo transferInfo = new TransferInfo();

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);

        // XXX order filesToUpload?
        try {
            operationMonitor.operationStart(Operation.UPLOAD, filesToUpload);
            for (TransferFile file : filesToUpload) {
                if (cancelled) {
                    LOGGER.fine("Upload cancelled");
                    break;
                }

                operationMonitor.operationProcess(Operation.UPLOAD, file);
                try {
                    uploadFile(transferInfo, baseLocalDir, file);
                } catch (IOException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                    continue;
                }
            }
        } finally {
            operationMonitor.operationFinish(Operation.UPLOAD, filesToUpload);
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void uploadFile(TransferInfo transferInfo, File baseLocalDir, TransferFile file) throws IOException, RemoteException {
        if (file.isDirectory()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading directory: " + file);
            }
            // in fact, useless but probably expected
            cdBaseRemoteDirectory(file.getRelativePath(), true);
            transferSucceeded(transferInfo, file);
        } else {
            // file => simply upload it

            assert file.getParentRelativePath() != null : "Must be underneath base remote directory! [" + file + "]";
            if (!cdBaseRemoteDirectory(file.getParentRelativePath(), true)) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", file.getParentRelativePath()));
                return;
            }

            String fileName = file.getName();

            int oldPermissions = -1;
            if (properties.isPreservePermissions()) {
                oldPermissions = remoteClient.getPermissions(fileName);
                LOGGER.fine(String.format("Original permissions of %s: %d", fileName, oldPermissions));
            } else {
                LOGGER.fine("Permissions are not preserved.");
            }

            String tmpFileName = null;
            if (properties.isUploadDirectly()) {
                LOGGER.fine("File will be uploaded directly.");
                tmpFileName = fileName;
            } else {
                tmpFileName = fileName + REMOTE_TMP_NEW_SUFFIX;
                LOGGER.fine("File will be uploaded using a temporary file.");
            }

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading file " + fileName + " => " + remoteClient.printWorkingDirectory() + TransferFile.SEPARATOR + tmpFileName);
            }
            // XXX lock the file?
            InputStream is = new FileInputStream(new File(baseLocalDir, file.getRelativePath(true)));
            boolean success = false;
            try {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    if (remoteClient.storeFile(tmpFileName, is)) {
                        success = true;
                        if (LOGGER.isLoggable(Level.FINE)) {
                            String f = file.getRelativePath() + (properties.isUploadDirectly() ? "" : REMOTE_TMP_NEW_SUFFIX);
                            LOGGER.fine(String.format("The %d. attempt to upload '%s' was successful", i, f));
                        }
                        break;
                    } else if (LOGGER.isLoggable(Level.FINE)) {
                        String f = file.getRelativePath() + (properties.isUploadDirectly() ? "" : REMOTE_TMP_NEW_SUFFIX);
                        LOGGER.fine(String.format("The %d. attempt to upload '%s' was NOT successful", i, f));
                    }
                }
            } finally {
                is.close();
                if (success) {
                    if (!properties.isUploadDirectly()) {
                        success = moveRemoteFile(tmpFileName, fileName);
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("File %s renamed to %s: %s", tmpFileName, fileName, success));
                        }
                    }

                    if (properties.isPreservePermissions() && success && oldPermissions != -1) {
                        int newPermissions = remoteClient.getPermissions(fileName);
                        LOGGER.fine(String.format("New permissions of %s: %d", fileName, newPermissions));
                        if (oldPermissions != newPermissions) {
                            LOGGER.fine(String.format("Setting permissions %d for %s.", oldPermissions, fileName));
                            boolean permissionsSet = remoteClient.setPermissions(oldPermissions, fileName);
                            if (LOGGER.isLoggable(Level.FINE)) {
                                LOGGER.fine(String.format("Permissions for %s set: %s", fileName, permissionsSet));
                                LOGGER.fine(String.format("Permissions for %s read: %s", fileName, remoteClient.getPermissions(fileName)));
                            }
                            if (!permissionsSet) {
                                transferPartiallyFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_PermissionsNotSet", oldPermissions, file.getName()));
                            }
                        }
                    }
                }
                if (success) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getOperationFailureMessage(Operation.UPLOAD, fileName));
                    boolean deleted = remoteClient.deleteFile(tmpFileName);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Unsuccessfully uploaded file %s deleted: %s", file.getRelativePath() + REMOTE_TMP_NEW_SUFFIX, deleted));
                    }
                }
            }
        }
    }

    private boolean moveRemoteFile(String source, String target) throws RemoteException {
        boolean moved = remoteClient.rename(source, target);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("File %s directly renamed to %s: %s", source, target, moved));
        }
        if (moved) {
            return true;
        }
        // possible cleanup
        String oldPath = target + REMOTE_TMP_OLD_SUFFIX;
        remoteClient.deleteFile(oldPath);

        // try to move the old file, move the new file, delete the old file
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Renaming in chain: (1) <file> -> <file>.old~ ; (2) <file>.new~ -> <file> ; (3) rm <file>.old~");
        }
        moved = remoteClient.rename(target, oldPath);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(1) File %s renamed to %s: %s", target, oldPath, moved));
        }
        if (!moved) {
            return false;
        }
        moved = remoteClient.rename(source, target);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(2) File %s renamed to %s: %s", source, target, moved));
        }
        if (!moved) {
            // try to restore the original file
            boolean restored = remoteClient.rename(oldPath, target);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("(-) File %s restored to original %s: %s", oldPath, target, restored));
            }
        } else {
            boolean deleted = remoteClient.deleteFile(oldPath);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("(3) File %s deleted: %s", oldPath, deleted));
            }
        }
        return moved;
    }

    public Set<TransferFile> prepareDownload(FileObject baseLocalDirectory, FileObject... filesToDownload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToDownload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToDownload.length > 0 : "At least one file to download must be specified";

        ensureConnected();

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        String baseLocalAbsolutePath = baseLocalDir.getAbsolutePath();
        Queue<TransferFile> queue = new LinkedList<TransferFile>();
        for (FileObject fo : filesToDownload) {
            if (isVisible(FileUtil.toFile(fo))) {
                LOGGER.fine("File " + fo + " added to download queue");
                queue.offer(TransferFile.fromFileObject(fo, baseLocalAbsolutePath));
            } else {
                LOGGER.fine("File " + fo + " NOT added to download queue [invisible]");
            }
        }

        Set<TransferFile> files = new HashSet<TransferFile>();
        while(!queue.isEmpty()) {
            if (cancelled) {
                LOGGER.fine("Prepare download cancelled");
                break;
            }

            TransferFile file = queue.poll();

            if (!files.add(file)) {
                // file already in set
                LOGGER.fine("File " + file + " already in queue");
                continue;
            }

            if (file.isDirectory()) {
                try {
                    if (!cdBaseRemoteDirectory(file.getRelativePath(), false)) {
                        LOGGER.fine("Remote directory " + file.getRelativePath() + " cannot be entered or does not exist => ignoring");
                        // XXX maybe return somehow ignored files as well?
                        continue;
                    }
                    String relPath = getRemoteRelativePath(file);
                    for (RemoteFile child : remoteClient.listFiles()) {
                        if (isVisible(getLocalFile(baseLocalDir, file, child))) {
                            LOGGER.fine("File " + child + " added to download queue");
                            queue.offer(TransferFile.fromRemoteFile(child, baseRemoteDirectory, relPath));
                        } else {
                            LOGGER.fine("File " + child + " NOT added to download queue [invisible]");
                        }
                    }
                } catch (RemoteException exc) {
                    LOGGER.fine("Remote directory " + file.getRelativePath() + "/* cannot be entered or does not exist => ignoring");
                    // XXX maybe return somehow ignored files as well?
                }
            }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Prepared for download: " + files);
        }
        return files;
    }

    public TransferInfo download(FileObject baseLocalDirectory, Set<TransferFile> filesToDownload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToDownload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToDownload.size() > 0 : "At least one file to download must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        TransferInfo transferInfo = new TransferInfo();

        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);

        // XXX order filesToDownload?
        try {
            operationMonitor.operationStart(Operation.DOWNLOAD, filesToDownload);
            for (TransferFile file : filesToDownload) {
                if (cancelled) {
                    LOGGER.fine("Download cancelled");
                    break;
                }

                operationMonitor.operationProcess(Operation.DOWNLOAD, file);
                try {
                    downloadFile(transferInfo, baseLocalDir, file);
                } catch (IOException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                    continue;
                }
            }
        } finally {
            operationMonitor.operationFinish(Operation.DOWNLOAD, filesToDownload);

            // refresh filesystem
            FileUtil.refreshFor(baseLocalDir);

            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void downloadFile(TransferInfo transferInfo, File baseLocalDir, TransferFile file) throws IOException, RemoteException {
        File localFile = getLocalFile(baseLocalDir, file);
        if (file.isDirectory()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading directory: " + file);
            }
            if (!cdBaseRemoteDirectory(file.getRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getRelativePath() + " does not exist => ignoring");
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", file.getRelativePath()));
                return;
            }
            // in fact, useless but probably expected
            if (!localFile.exists()) {
                if (!mkLocalDirs(localFile)) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDir", localFile));
                    return;
                }
            } else if (localFile.isFile()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_DirFileCollision", file));
                return;
            }
            transferSucceeded(transferInfo, file);
        } else if (file.isFile()) {
            // file => simply download it

            // #142682 - because from the ui we get only files (folders are removed) => ensure parent folder exists
            File parent = localFile.getParentFile();
            assert parent != null : "File " + localFile + " has no parent file?!";
            if (!parent.exists()) {
                if (!mkLocalDirs(parent)) {
                    transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDir", parent));
                    return;
                }
            } else if (parent.isFile()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_DirFileCollision", file));
                return;
            } else if (localFile.exists() && !localFile.canWrite()) {
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FileNotWritable", localFile));
                return;
            }
            assert parent.isDirectory() : "Parent file of " + localFile + " must be a directory";

            File tmpLocalFile = new File(localFile.getAbsolutePath() + LOCAL_TMP_NEW_SUFFIX);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading " + file.getRelativePath() + " => " + tmpLocalFile.getAbsolutePath());
            }

            if (!cdBaseRemoteDirectory(file.getParentRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getParentRelativePath() + " does not exist => ignoring file " + file.getRelativePath());
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", file.getParentRelativePath()));
                return;
            }

            // XXX lock the file?
            OutputStream os = new FileOutputStream(tmpLocalFile);
            boolean success = false;
            try {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    if (remoteClient.retrieveFile(file.getName(), os)) {
                        success = true;
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("The %d. attempt to download '%s' was successful", i, file.getRelativePath()));
                        }
                        break;
                    } else if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("The %d. attempt to download '%s' was NOT successful", i, file.getRelativePath()));
                    }
                }
            } finally {
                os.close();
                if (success) {
                    // move the file
                    success = moveLocalFile(tmpLocalFile, localFile);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s renamed to %s: %s", tmpLocalFile, localFile, success));
                    }
                }
                if (success) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getOperationFailureMessage(Operation.DOWNLOAD, file.getName()));
                    boolean deleted = tmpLocalFile.delete();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Unsuccessfully downloaded file %s deleted: %s", tmpLocalFile, deleted));
                    }
                }
            }
        } else {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_UnknownFileType", file.getRelativePath()));
        }
    }

    private boolean moveLocalFile(final File source, final File target) {
        final boolean[] moved = new boolean[1];
        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                File oldPath = new File(target.getAbsolutePath() + LOCAL_TMP_OLD_SUFFIX);
                String tmpLocalFileName = source.getName();
                String localFileName = target.getName();
                String oldPathName = oldPath.getName();

                if (!target.exists()) {
                    moved[0] = renameLocalFileTo(source, target);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s directly renamed to %s: %s", tmpLocalFileName, localFileName, moved[0]));
                    }
                    if (moved[0]) {
                        return;
                    }
                }
                // possible cleanup
                deleteLocalFile(oldPath, ""); // NOI18N

                // try to move the old file, move the new file, delete the old file
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Renaming in chain: (1) <file> -> <file>.old~ ; (2) <file>.new~ -> <file> ; (3) rm <file>.old~");
                }
                // intentional usage of java.io.File!!
                //  (if the file is opened in the editor, it's not closed, just refreshed)
                moved[0] = target.renameTo(oldPath);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("(1) File %s renamed to %s: %s", localFileName, oldPathName, moved[0]));
                }
                if (!moved[0]) {
                    return;
                }
                moved[0] = renameLocalFileTo(source, target);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("(2) File %s renamed to %s: %s", tmpLocalFileName, localFileName, moved[0]));
                }
                if (!moved[0] && oldPath.exists() && !target.exists()) {
                    // try to restore the original file
                    boolean restored = renameLocalFileTo(oldPath, target);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("(-) File %s restored to original %s: %s", oldPathName, localFileName, restored));
                    }
                    return;
                }
                deleteLocalFile(oldPath, "(3) "); // NOI18N
            }
        });
        assert moved[0] || !moved[0];
        return moved[0];
    }

    private File getLocalFile(File localFile, TransferFile transferFile) {
        if (transferFile.getRelativePath() == TransferFile.CWD) {
            return localFile;
        }
        return new File(localFile, transferFile.getRelativePath(true));
    }

    // #169778
    private File getLocalFile(File localFile, TransferFile parent, RemoteFile file) {
        return new File(getLocalFile(localFile, parent), file.getName());
    }


    public Set<TransferFile> prepareDelete(FileObject baseLocalDirectory, FileObject... filesToDelete) throws RemoteException {
        LOGGER.fine("Preparing files to delete => calling prepareUpload because in fact the same operation is done");
        return prepareUpload(baseLocalDirectory, filesToDelete);
    }

    public TransferInfo delete(Set<TransferFile> filesToDelete) throws RemoteException {
        assert filesToDelete != null;
        assert filesToDelete.size() > 0 : "At least one file to upload must be specified";

        ensureConnected();

        final long start = System.currentTimeMillis();
        TransferInfo transferInfo = new TransferInfo();

        try {
            // first, remove all the files
            //  then remove _empty_ directories (motivation is to prevent data loss; somebody else could upload some file there)
            Set<TransferFile> files = getFiles(filesToDelete);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Only files: %s => %s", filesToDelete, files));
            }
            delete(transferInfo, files);

            Set<TransferFile> dirs = getDirectories(filesToDelete);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("Only dirs: %s => %s", filesToDelete, dirs));
            }
            delete(transferInfo, dirs);

            assert filesToDelete.size() == files.size() + dirs.size() : String.format("%s does not match files and dirs: %s %s", filesToDelete, files, dirs);
        } finally {
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    private void delete(TransferInfo transferInfo, Set<TransferFile> filesToDelete) {
        for (TransferFile file : filesToDelete) {
            if (cancelled) {
                LOGGER.fine("Delete cancelled");
                break;
            }

            try {
                deleteFile(transferInfo, file);
            } catch (IOException exc) {
                transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                continue;
            } catch (RemoteException exc) {
                transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_ErrorReason", exc.getMessage().trim()));
                continue;
            }
        }
    }

    private void deleteFile(TransferInfo transferInfo, TransferFile file) throws IOException, RemoteException {
        boolean success = false;
        cdBaseRemoteDirectory();
        if (file.isDirectory()) {
            // folder => try to delete it but it can fail (most probably when it's not empty)
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Deleting directory: " + file);
            }
            success = remoteClient.deleteDirectory(file.getRelativePath());
            LOGGER.fine("Folder deleted: " + success);
        } else {
            // file => simply delete it
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Deleting file: " + file);
            }

            success = remoteClient.deleteFile(file.getRelativePath());
            LOGGER.fine("File deleted: " + success);
        }

        if (success) {
            transferSucceeded(transferInfo, file);
        } else {
            String msg = null;
            if (!remoteClient.exists(file.getParentRelativePath(), file.getName())) {
                msg = NbBundle.getMessage(RemoteClient.class, "MSG_FileNotExists", file.getName());
            } else {
                // maybe non empty dir?
                if (file.isDirectory()
                        && cdBaseRemoteDirectory(file.getParentRelativePath(), false)
                        && remoteClient.listFiles().size() > 0) {
                    msg = NbBundle.getMessage(RemoteClient.class, "MSG_FolderNotEmpty", file.getName());
                } else {
                    msg = getOperationFailureMessage(Operation.DELETE, file.getName());
                }
            }
            transferFailed(transferInfo, file, msg);
        }
    }

    private void transferSucceeded(TransferInfo transferInfo, TransferFile file) {
        transferInfo.addTransfered(file);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Transfered: " + file);
        }
    }

    private void transferFailed(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isFailed(file)) {
            transferInfo.addFailed(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Failed: " + file + ", reason: " + reason);
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Failed: " + file + ", reason: " + reason + " [ignored, failed already]");
            }
        }
    }

    private void transferPartiallyFailed(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isPartiallyFailed(file)) {
            transferInfo.addPartiallyFailed(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Partially failed: " + file + ", reason: " + reason);
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Partially failed: " + file + ", reason: " + reason + " [ignored, partially failed already]");
            }
        }
    }

    private void transferIgnored(TransferInfo transferInfo, TransferFile file, String reason) {
        if (!transferInfo.isIgnored(file)) {
            transferInfo.addIgnored(file, reason);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Ignored: " + file + ", reason: " + reason);
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Ignored: " + file + ", reason: " + reason + " [ignored, ignored already]");
            }
        }
    }

    private String getOperationFailureMessage(Operation operation, String fileName) {
        String message = remoteClient.getNegativeReplyString();
        if (message == null) {
            String key = null;
            switch (operation) {
                case UPLOAD:
                    key = "MSG_CannotUploadFile"; // NOI18N
                    break;
                case DOWNLOAD:
                    key = "MSG_CannotDownloadFile"; // NOI18N
                    break;
                case DELETE:
                    key = "MSG_CannotDeleteFile"; // NOI18N
                    break;
                default:
                    throw new IllegalArgumentException("Unknown operation type: " + operation);
            }
            message = NbBundle.getMessage(RemoteClient.class, key, fileName);
        }
        return message;
    }

    private void ensureConnected() throws RemoteException {
        if (!remoteClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }
    }

    private boolean cdBaseRemoteDirectory() throws RemoteException {
        return cdRemoteDirectory(baseRemoteDirectory, true);
    }

    private boolean cdBaseRemoteDirectory(String subdirectory, boolean create) throws RemoteException {
        assert subdirectory == null || !subdirectory.startsWith(TransferFile.SEPARATOR) : "Subdirectory must be null or relative [" + subdirectory + "]" ;

        String path = baseRemoteDirectory;
        if (subdirectory != null && !subdirectory.equals(TransferFile.CWD)) {
            path = baseRemoteDirectory + TransferFile.SEPARATOR + subdirectory;
        }
        return cdRemoteDirectory(path, create);
    }

    private boolean cdRemoteDirectory(String directory, boolean create) throws RemoteException {
        LOGGER.fine("Changing directory to " + directory);
        boolean success = remoteClient.changeWorkingDirectory(directory);
        if (!success && create) {
            return createAndCdRemoteDirectory(directory);
        }
        return success;
    }

    /**
     * Create file path on remote server <b>in the current directory</b>.
     * @param filePath file path to create, can be even relative (e.g. "a/b/c/d").
     */
    private boolean createAndCdRemoteDirectory(String filePath) throws RemoteException {
        LOGGER.fine("Creating file path " + filePath);
        if (filePath.startsWith(TransferFile.SEPARATOR)) {
            // enter root directory
            if (!remoteClient.changeWorkingDirectory(TransferFile.SEPARATOR)) {
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_CannotChangeDirectory", "/"), remoteClient.getReplyString());
            }
        }
        for (String dir : filePath.split(TransferFile.SEPARATOR)) {
            if (dir.length() == 0) {
                // handle paths like "a//b///c/d" (dir can be "")
                continue;
            }
            if (!remoteClient.changeWorkingDirectory(dir)) {
                if (!remoteClient.makeDirectory(dir)) {
                    // XXX check 52x codes
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot create directory: " + remoteClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDirectory", dir), remoteClient.getReplyString());
                } else if (!remoteClient.changeWorkingDirectory(dir)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot enter directory: " + remoteClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    return false;
                    // XXX
                    //throw new RemoteException("Cannot change directory '" + dir + "' [" + remoteClient.getReplyString() + "]");
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Directory '" + remoteClient.printWorkingDirectory() + "' created and entered");
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [remote configuration: "); // NOI18N
        sb.append(configuration);
        sb.append(", baseRemoteDirectory: "); // NOI18N
        sb.append(baseRemoteDirectory);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    private static boolean isVisible(File file) {
        assert file != null;
        if (IGNORED_DIRS.contains(file.getName())) {
            return false;
        }
        return VisibilityQuery.getDefault().isVisible(file);
    }

    private static boolean mkLocalDirs(File folder) {
        try {
            FileUtil.createFolder(folder);
        } catch (IOException exc) {
            LOGGER.log(Level.INFO, null, exc);
            return false;
        }
        return true;
    }

    /**
     * Similar to {@link File#renameTo(java.io.File)} but uses {@link FileObject}s.
     * @param source a source file, must exist.
     * @param target a target file, cannot exist.
     * @return <code>true</code> if the rename was successful, <code>false</code> otherwise.
     */
    private static boolean renameLocalFileTo(File source, File target) {
        long start = 0L;
        if (LOGGER.isLoggable(Level.FINE)) {
            start = System.currentTimeMillis();
        }
        assert source.exists() : "Source file must exist " + source;
        assert !target.exists() : "Target file cannot exist " + target;

        FileObject sourceFO = FileUtil.toFileObject(source);
        assert sourceFO != null : "Source fileobject must exist " + source;

        String name = getName(target.getName());
        String ext = FileUtil.getExtension(target.getName());

        boolean moved = false;
        try {
            FileLock lock = sourceFO.lock();
            try {
                sourceFO.rename(lock, name, ext);
                moved = true;
            } catch (IOException exc) {
                LOGGER.log(Level.INFO, null, exc);
            } finally {
                lock.releaseLock();
            }
        } catch (IOException exc) {
            LOGGER.log(Level.WARNING, null, exc);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("Move %s -> %s took: %sms", source, target, (System.currentTimeMillis() - start)));
        }
        return moved;
    }

    private void deleteLocalFile(File file, String logMsgPrefix) {
        if (!file.exists()) {
            return;
        }
        boolean deleted = file.delete();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format(logMsgPrefix + "File %s deleted: %s", file.getName(), deleted));
        }
    }

    private static String getName(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    private String getRemoteRelativePath(TransferFile file) {
        StringBuilder relativePath = new StringBuilder(baseRemoteDirectory);
        if (file.getRelativePath() != TransferFile.CWD) {
            relativePath.append(TransferFile.SEPARATOR);
            relativePath.append(file.getRelativePath());
        }
        return relativePath.toString();
    }

    private Set<TransferFile> getFiles(Set<TransferFile> all) {
        Set<TransferFile> files = new HashSet<TransferFile>();
        for (TransferFile file : all) {
            if (file.isFile()) {
                files.add(file);
            }
        }
        return files;
    }

    private Set<TransferFile> getDirectories(Set<TransferFile> all) {
        // we need to get longest paths first to be able to delete directories properly
        //  (e.g. to have [a/b, a] and not [a, a/b])
        Set<TransferFile> dirs = new TreeSet<TransferFile>(new Comparator<TransferFile>() {
            private final String SEPARATOR = Pattern.quote(TransferFile.SEPARATOR);
            public int compare(TransferFile o1, TransferFile o2) {
                int cmp = o2.getRelativePath().split(SEPARATOR).length - o1.getRelativePath().split(SEPARATOR).length;
                // do not miss any item
                return cmp != 0 ? cmp : 1;
            }
        });
        for (TransferFile file : all) {
            if (file.isDirectory()) {
                dirs.add(file);
            }
        }
        return dirs;
    }

    public static interface OperationMonitor {
        /**
         * {@link Operation} started for the files.
         * @param operation {@link Operation} currently run
         * @param forFiles collection of files for which the operation started
         */
        void operationStart(Operation operation, Collection<TransferFile> forFiles);

        /**
         * {@link Operation} process for the file.
         * @param operation {@link Operation} currently run
         * @param forFile files for which the operation is run
         */
        void operationProcess(Operation operation, TransferFile forFile);

        /**
         * {@link Operation} finished for the files.
         * @param operation {@link Operation} currently run
         * @param forFiles collection of files for which the operation finished
         */
        void operationFinish(Operation operation, Collection<TransferFile> forFiles);
    }

    private static final class DevNullOperationMonitor implements OperationMonitor {
        public void operationStart(Operation operation, Collection<TransferFile> forFiles) {
        }
        public void operationProcess(Operation operation, TransferFile forFile) {
        }
        public void operationFinish(Operation operation, Collection<TransferFile> forFiles) {
        }
    }

    /**
     * Advanced properties for a {@link RemoteClient}.
     */
    public static final class AdvancedProperties {
        private final InputOutput io;
        private final String additionalInitialSubdirectory;
        private final boolean preservePermissions;
        private final boolean uploadDirectly;
        private final OperationMonitor operationMonitor;

        /**
         * Create advanced properties for a {@link RemoteClient}.
         */
        public AdvancedProperties() {
            this(new AdvancedPropertiesBuilder());
        }

        private AdvancedProperties(AdvancedPropertiesBuilder builder) {
            io = builder.io;
            additionalInitialSubdirectory = builder.additionalInitialSubdirectory;
            preservePermissions = builder.preservePermissions;
            uploadDirectly = builder.uploadDirectly;
            operationMonitor = builder.operationMonitor;
        }

        /**
         * Get additional initial subdirectory (directory which starts with {@value TransferFile#SEPARATOR} and is appended
         * to {@link RemoteConfiguration#getInitialDirectory()} and set as default base remote directory. Can be <code>null</code>.
         * @return additional initial subdirectory, can be <code>null</code>.
         */
        public String getAdditionalInitialSubdirectory() {
            return additionalInitialSubdirectory;
        }

        /**
         * Return properties with configured additional initial subdirectory.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param additionalInitialSubdirectory additional directory which must start with {@value TransferFile#SEPARATOR} and is appended
         *                                      to {@link RemoteConfiguration#getInitialDirectory()} and
         *                                      set as default base remote directory.
         * @return new properties with configured additional initial subdirectory, can be <code>null</code>.
         */
        public AdvancedProperties setAdditionalInitialSubdirectory(String additionalInitialSubdirectory) {
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setAdditionalInitialSubdirectory(additionalInitialSubdirectory));
        }

        /**
         * Get {@link InputOutput}, the displayer of protocol commands, can be <code>null</code>.
         * Displays all the commands received from server.
         * @return {@link InputOutput}, the displayer of protocol commands, can be <code>null</code>.
         */
        public InputOutput getInputOutput() {
            return io;
        }

        /**
         * Return properties with configured displayer of protocol commands.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param io {@link InputOutput}, the displayer of protocol commands.
         *           Displays all the commands received from server.
         * @return new properties with configured displayer of protocol commands
         */
        public AdvancedProperties setInputOutput(InputOutput io) {
            Parameters.notNull("io", io);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setInputOutput(io));
        }

        /**
         * Get {@link OperationMonitor monitor of commands}.
         * @return {@link OperationMonitor monitor of commands}, can be <code>null</code>.
         */
        public OperationMonitor getOperationMonitor() {
            return operationMonitor;
        }

        /**
         * Return properties with configured {@link OperationMonitor monitor of commands}.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param operationMonitor {@link OperationMonitor monitor of commands}.
         * @return new properties with configured {@link OperationMonitor monitor of commands}
         */
        public AdvancedProperties setOperationMonitor(OperationMonitor operationMonitor) {
            Parameters.notNull("operationMonitor", operationMonitor);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setOperationMonitor(operationMonitor));
        }

        /**
         * <code>True</code> if permissions should be preserved; please note that this is not supported for local
         * files (possible in Java 6 and newer only) and also it will very likely cause slow down of file transfer.
         * @return <code>true</code> if permissions should be preserved
         */
        public boolean isPreservePermissions() {
            return preservePermissions;
        }

        /**
         * Return properties with configured preserved permissions.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param preservePermissions <code>true</code> if permissions should be preserved; please note that this is not supported for local
         *                            files (possible in Java 6 and newer only) and also it will very likely cause slow down of file transfer.
         * @return new properties with configured preserved permissions
         */
        public AdvancedProperties setPreservePermissions(boolean preservePermissions) {
            Parameters.notNull("preservePermissions", preservePermissions);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setPreservePermissions(preservePermissions));
        }

        /**
         * <code>True</code> if file upload is done <b>without</b> a temporary file.
         * <b>Warning:</b> can be dangerous.
         * @return <code>true</code> if file upload is done <b>without</b> a temporary file
         */
        public boolean isUploadDirectly() {
            return uploadDirectly;
        }

        /**
         * Return properties with configured direct upload.
         * <p>
         * All other properties of the returned properties are inherited from
         * <code>this</code>.
         *
         * @param uploadDirectly whether to upload files <b>without</b> a temporary file. <b>Warning:</b> can be dangerous.
         * @return new properties with configured direct upload
         */
        public AdvancedProperties setUploadDirectly(boolean uploadDirectly) {
            Parameters.notNull("uploadDirectly", uploadDirectly);
            return new AdvancedProperties(new AdvancedPropertiesBuilder(this).setUploadDirectly(uploadDirectly));
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append("AdvancedProperties [ io: ");
            sb.append(io);
            sb.append(", additionalInitialSubdirectory: ");
            sb.append(additionalInitialSubdirectory);
            sb.append(", preservePermissions: ");
            sb.append(preservePermissions);
            sb.append(", uploadDirectly: ");
            sb.append(uploadDirectly);
            sb.append(", operationMonitor: ");
            sb.append(operationMonitor);
            sb.append(" ]");
            return sb.toString();
        }
    }

    private static final class AdvancedPropertiesBuilder {
        InputOutput io;
        String additionalInitialSubdirectory;
        boolean preservePermissions = false;
        boolean uploadDirectly = false;
        OperationMonitor operationMonitor;

        AdvancedPropertiesBuilder() {
        }

        public AdvancedPropertiesBuilder(AdvancedProperties properties) {
            io = properties.getInputOutput();
            additionalInitialSubdirectory = properties.getAdditionalInitialSubdirectory();
            preservePermissions = properties.isPreservePermissions();
            uploadDirectly = properties.isUploadDirectly();
            operationMonitor = properties.getOperationMonitor();
        }

        public AdvancedPropertiesBuilder setAdditionalInitialSubdirectory(String additionalInitialSubdirectory) {
            this.additionalInitialSubdirectory = additionalInitialSubdirectory;
            return this;
        }

        public AdvancedPropertiesBuilder setInputOutput(InputOutput io) {
            this.io = io;
            return this;
        }

        public AdvancedPropertiesBuilder setOperationMonitor(OperationMonitor operationMonitor) {
            this.operationMonitor = operationMonitor;
            return this;
        }

        public AdvancedPropertiesBuilder setPreservePermissions(boolean preservePermissions) {
            this.preservePermissions = preservePermissions;
            return this;
        }

        public AdvancedPropertiesBuilder setUploadDirectly(boolean uploadDirectly) {
            this.uploadDirectly = uploadDirectly;
            return this;
        }
    }
}
