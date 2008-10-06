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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.php.project.connections.ui.PasswordPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Remote client able to connect/disconnect to FTP
 * as well as download/upload files to a FTP server.
 * <p>
 * Every method throws {@link RemoteException} if any error occurs.
 * @author Tomas Mysik
 */
public class RemoteClient implements Cancellable {
    private static final Logger LOGGER = Logger.getLogger(RemoteClient.class.getName());
    private static final String NB_METADATA_DIR = "nbproject"; // NOI18N
    private static final String[] IGNORED_REMOTE_DIRS = new String[] {".", ".."}; // NOI18N
    private static final int TRIES_TO_TRANSFER = 3; // number of tries if file download/upload fails
    private static final String TMP_NEW_SUFFIX = ".new~"; // NOI18N
    private static final String TMP_OLD_SUFFIX = ".old~"; // NOI18N

    // store not provided passwords in memory only
    private static final Map<Integer, String> PASSWORDS = new HashMap<Integer, String>();

    private final RemoteConfiguration configuration;
    private final InputOutput io;
    private final String baseRemoteDirectory;
    private FTPClient ftpClient;
    private volatile boolean cancelled = false;

    /**
     * @see RemoteClient#RemoteClient(org.netbeans.modules.php.project.connections.RemoteConfiguration, org.openide.windows.InputOutput, java.lang.String)
     */
    public RemoteClient(RemoteConfiguration configuration) {
        this(configuration, null, null);
    }

    /**
     * @see RemoteClient#RemoteClient(org.netbeans.modules.php.project.connections.RemoteConfiguration, org.openide.windows.InputOutput, java.lang.String)
     */
    public RemoteClient(RemoteConfiguration configuration, InputOutput io) {
        this(configuration, io, null);
    }

    /**
     * Create a new remote client.
     * @param configuration {@link RemoteConfiguration remote configuration} of a connection.
     * @param io {@link InputOutput}, the displayer of protocol commands, can be <code>null</code>.
     *           Displays all the commands received from server.
     * @param additionalInitialSubdirectory additional directory which must start with {@value TransferFile#SEPARATOR} and is appended
     *                                      to {@link RemoteConfiguration#getInitialDirectory()} and
     *                                      set as default base remote directory. Can be <code>null</code>.
     */
    public RemoteClient(RemoteConfiguration configuration, InputOutput io, String additionalInitialSubdirectory) {
        assert configuration != null;
        this.configuration = configuration;
        this.io = io;
        StringBuilder baseDir = new StringBuilder(configuration.getInitialDirectory());
        if (additionalInitialSubdirectory != null && additionalInitialSubdirectory.length() > 0) {
            baseDir.append(additionalInitialSubdirectory);
        }
        baseRemoteDirectory = baseDir.toString().replaceAll(TransferFile.SEPARATOR + "{2,}", TransferFile.SEPARATOR); // NOI18N
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Remote client created with configuration: " + configuration + " and base remote directory: " + baseRemoteDirectory);
        }
    }

    public void connect() throws RemoteException {
        init();
        try {
            // connect
            int timeout = configuration.getTimeout() * 1000;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Connecting to " + configuration.getHost() + " [timeout: " + timeout + " ms]");
            }
            //before connection - not to force user to put password faster than timeout
            String password = getPassword();
            ftpClient.setDefaultTimeout(timeout);
            ftpClient.connect(configuration.getHost(), configuration.getPort());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Reply is " + getReplyString());
            }
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                LOGGER.fine("Disconnecting because of negative reply");
                ftpClient.disconnect();
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpRefusedConnection", configuration.getHost()), getReplyString());
            }

            // login
            LOGGER.fine("Login as " + configuration.getUserName());
            if (!ftpClient.login(configuration.getUserName(), password)) {
                LOGGER.fine("Login unusuccessful -> logout");
                ftpClient.logout();
                // remove password from a memory storage
                PASSWORDS.remove(configuration.hashCode());
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpLoginFailed"), getReplyString());
            }
            LOGGER.fine("Login successful");

            if (configuration.isPassiveMode()) {
                LOGGER.fine("Setting passive mode");
                ftpClient.enterLocalPassiveMode();
            }

            // binary mode as a default
            LOGGER.fine("Setting file type to BINARY");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Remote system is " + ftpClient.getSystemName());
            }

            // cd to base remote directory
            if (!cdBaseRemoteDirectory()) {
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", baseRemoteDirectory), getReplyString());
            }

        } catch (IOException ex) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Exception while disconnecting", ex);
                }
            }
            LOGGER.log(Level.FINE, "Exception while connecting", ex);
            throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotConnect", configuration.getHost()), ex, getReplyString());
        }
    }

    public void disconnect() throws RemoteException {
        init();
        LOGGER.log(Level.FINE, "Remote client trying to disconnect");
        if (ftpClient.isConnected()) {
            LOGGER.log(Level.FINE, "Remote client connected -> disconnecting");
            try {
                ftpClient.logout();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "Error while disconnecting", ex);
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotLogout", configuration.getHost()), ex, getReplyString());
            } finally {
                try {
                    ftpClient.disconnect();
                    LOGGER.log(Level.FINE, "Remote client disconnected");
                } catch (IOException ex) {
                    LOGGER.log(Level.FINE, "Remote client disconnected with exception", ex);
                }
            }
        }
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public void reset() {
        cancelled = false;
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
                File f = getLocalFile(file, baseLocalDir);
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
            for (TransferFile file : filesToUpload) {
                if (cancelled) {
                    LOGGER.fine("Upload cancelled");
                    break;
                }

                try {
                    uploadFile(transferInfo, baseLocalDir, file);
                } catch (IOException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
                    continue;
                }
            }
        } finally {
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
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", file.getParentRelativePath()));
                return;
            }

            String fileName = file.getName();
            String tmpFileName = fileName + TMP_NEW_SUFFIX;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading file " + fileName + " => " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + tmpFileName);
            }
            // XXX lock the file?
            InputStream is = new FileInputStream(new File(baseLocalDir, file.getRelativePath(true)));
            boolean success = false;
            try {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    if (ftpClient.storeFile(tmpFileName, is)) {
                        success = true;
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.fine(String.format("The %d. attempt to upload '%s' was successful", i, file.getRelativePath() + TMP_NEW_SUFFIX));
                        }
                        break;
                    } else if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("The %d. attempt to upload '%s' was NOT successful", i, file.getRelativePath() + TMP_NEW_SUFFIX));
                    }
                }
            } finally {
                is.close();
                if (success) {
                    success = moveRemoteFile(fileName, tmpFileName);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s renamed to %s: %s", tmpFileName, fileName, success));
                    }
                }
                if (success) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getFailureMessage(fileName, true));
                    boolean deleted = ftpClient.deleteFile(tmpFileName);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Unsuccessfully uploaded file %s deleted: %s", file.getRelativePath() + TMP_NEW_SUFFIX, deleted));
                    }
                }
            }
        }
    }

    private boolean moveRemoteFile(String fileName, String tmpFileName) throws IOException {
        String oldPath = fileName + TMP_OLD_SUFFIX;
        boolean moved = ftpClient.rename(tmpFileName, fileName);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("File %s directly renamed to %s: %s", tmpFileName, fileName, moved));
        }
        if (moved) {
            return true;
        }
        // possible cleanup
        ftpClient.deleteFile(oldPath);

        // try to move the old file, move the new file, delete the old file
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Renaming in chain: (1) <file> -> <file>.old~ ; (2) <file>.new~ -> <file> ; (3) rm <file>.old~");
        }
        moved = ftpClient.rename(fileName, oldPath);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(1) File %s renamed to %s: %s", fileName, oldPath, moved));
        }
        if (!moved) {
            return false;
        }
        moved = ftpClient.rename(tmpFileName, fileName);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("(2) File %s renamed to %s: %s", tmpFileName, fileName, moved));
        }
        if (!moved) {
            // try to restore the original file
            boolean restored = ftpClient.rename(oldPath, fileName);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(String.format("(-) File %s restored to original %s: %s", oldPath, fileName, restored));
            }
        } else {
            boolean deleted = ftpClient.deleteFile(oldPath);
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
                    StringBuilder relativePath = new StringBuilder(baseRemoteDirectory);
                    if (file.getRelativePath() != TransferFile.CWD) {
                        relativePath.append(TransferFile.SEPARATOR);
                        relativePath.append(file.getRelativePath());
                    }
                    String relPath = relativePath.toString();
                    for (FTPFile child : ftpClient.listFiles()) {
                        if (isVisible(child)) {
                            LOGGER.fine("File " + file + " added to download queue");
                            queue.offer(TransferFile.fromFtpFile(child, baseRemoteDirectory,  relPath));
                        } else {
                            LOGGER.fine("File " + file + " NOT added to download queue [invisible]");
                        }
                    }
                } catch (IOException exc) {
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
            for (TransferFile file : filesToDownload) {
                if (cancelled) {
                    LOGGER.fine("Download cancelled");
                    break;
                }

                try {
                    downloadFile(transferInfo, baseLocalDir, file);
                } catch (IOException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage().trim()));
                    continue;
                }
            }
        } finally {
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
        File localFile = getLocalFile(file, baseLocalDir);
        if (file.isDirectory()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading directory: " + file);
            }
            if (!cdBaseRemoteDirectory(file.getRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getRelativePath() + " does not exist => ignoring");
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", file.getRelativePath()));
                return;
            }
            // in fact, useless but probably expected
            if (!localFile.exists()) {
                if (!localFile.mkdirs()) {
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
                if (!parent.mkdirs()) {
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

            File tmpLocalFile = new File(localFile.getAbsolutePath() + TMP_NEW_SUFFIX);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading " + file.getRelativePath() + " => " + tmpLocalFile.getAbsolutePath());
            }

            if (!cdBaseRemoteDirectory(file.getParentRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getParentRelativePath() + " does not exist => ignoring file " + file.getRelativePath());
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", file.getParentRelativePath()));
                return;
            }

            // XXX lock the file?
            OutputStream os = new FileOutputStream(tmpLocalFile);
            boolean success = false;
            try {
                for (int i = 1; i <= TRIES_TO_TRANSFER; i++) {
                    if (ftpClient.retrieveFile(file.getName(), os)) {
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
                    success = moveLocalFile(localFile, tmpLocalFile);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("File %s renamed to %s: %s", tmpLocalFile, localFile, success));
                    }
                }
                if (success) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getFailureMessage(file.getName(), false));
                    boolean deleted = tmpLocalFile.delete();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine(String.format("Unsuccessfully downloaded file %s deleted: %s", tmpLocalFile, deleted));
                    }
                }
            }
        } else {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpUnknownFileType", file.getRelativePath()));
        }
    }

    private boolean moveLocalFile(final File localFile, final File tmpLocalFile) {
        final boolean[] moved = new boolean[1];
        FileUtil.runAtomicAction(new Runnable() {
            public void run() {
                File oldPath = new File(localFile.getAbsolutePath() + TMP_OLD_SUFFIX);
                String tmpLocalFileName = tmpLocalFile.getName();
                String localFileName = localFile.getName();
                String oldPathName = oldPath.getName();
                moved[0] = tmpLocalFile.renameTo(localFile);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("File %s directly renamed to %s: %s", tmpLocalFileName, localFileName, moved[0]));
                }
                if (moved[0]) {
                    return;
                }
                // possible cleanup
                deleteLocalFile(oldPath, ""); // NOI18N

                // try to move the old file, move the new file, delete the old file
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Renaming in chain: (1) <file> -> <file>.old~ ; (2) <file>.new~ -> <file> ; (3) rm <file>.old~");
                }
                moved[0] = localFile.renameTo(oldPath);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("(1) File %s renamed to %s: %s", localFileName, oldPathName, moved[0]));
                }
                if (!moved[0]) {
                    return;
                }
                moved[0] = tmpLocalFile.renameTo(localFile);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine(String.format("(2) File %s renamed to %s: %s", tmpLocalFileName, localFileName, moved[0]));
                }
                if (!moved[0]) {
                    // try to restore the original file
                    boolean restored = oldPath.renameTo(localFile);
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

    private void deleteLocalFile(File file, String logMsgPrefix) {
        if (!file.exists()) {
            return;
        }
        boolean deleted = file.delete();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format(logMsgPrefix + "File %s deleted: %s", file.getName(), deleted));
        }
    }

    private File getLocalFile(TransferFile transferFile, File localFile) {
        if (transferFile.getRelativePath() == TransferFile.CWD) {
            return localFile;
        }
        return new File(localFile, transferFile.getRelativePath(true));
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

    private String getFailureMessage(String fileName, boolean upload) {
        String message = null;
        int replyCode = ftpClient.getReplyCode();
        if (FTPReply.isNegativePermanent(replyCode)
                || FTPReply.isNegativeTransient(replyCode)) {
            message = NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", getReplyString());
        } else {
            message = NbBundle.getMessage(RemoteClient.class, upload ? "MSG_FtpCannotUploadFile" : "MSG_FtpCannotDownloadFile", fileName);
        }
        return message;
    }

    private void init() {
        if (ftpClient != null) {
            return;
        }
        LOGGER.log(Level.FINE, "FTP client creating");
        ftpClient = new FTPClient();

        if (io != null) {
            ftpClient.addProtocolCommandListener(new PrintCommandListener(io));
        }
        LOGGER.log(Level.FINE, "Protocol command listener added");
    }

    private void ensureConnected() throws RemoteException {
        init();
        if (!ftpClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }
    }

    private boolean cdBaseRemoteDirectory() throws IOException, RemoteException {
        return cdRemoteDirectory(baseRemoteDirectory, true);
    }

    private boolean cdBaseRemoteDirectory(String subdirectory, boolean create) throws IOException, RemoteException {
        assert subdirectory == null || !subdirectory.startsWith(TransferFile.SEPARATOR) : "Subdirectory must be null or relative [" + subdirectory + "]" ;

        String path = baseRemoteDirectory;
        if (subdirectory != null && !subdirectory.equals(TransferFile.CWD)) {
            path = baseRemoteDirectory + TransferFile.SEPARATOR + subdirectory;
        }
        return cdRemoteDirectory(path, create);
    }

    private boolean cdRemoteDirectory(String directory, boolean create) throws IOException, RemoteException {
        LOGGER.fine("Changing directory to " + directory);
        boolean success = ftpClient.changeWorkingDirectory(directory);
        if (!success && create) {
            return createAndCdRemoteDirectory(directory);
        }
        return success;
    }

    /**
     * Create file path on FTP server <b>in the current directory</b>.
     * @param filePath file path to create, can be even relative (e.g. "a/b/c/d").
     */
    private boolean createAndCdRemoteDirectory(String filePath) throws IOException, RemoteException {
        LOGGER.fine("Creating file path " + filePath);
        if (filePath.startsWith(TransferFile.SEPARATOR)) {
            // enter root directory
            if (!ftpClient.changeWorkingDirectory(TransferFile.SEPARATOR)) {
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", "/"), getReplyString());
            }
        }
        for (String dir : filePath.split(TransferFile.SEPARATOR)) {
            if (dir.length() == 0) {
                // handle paths like "a//b///c/d" (dir can be "")
                continue;
            }
            if (!ftpClient.changeWorkingDirectory(dir)) {
                if (!ftpClient.makeDirectory(dir)) {
                    // XXX check 52x codes
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot create directory: " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotCreateDirectory", dir), getReplyString());
                } else if (!ftpClient.changeWorkingDirectory(dir)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot enter directory: " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    return false;
                    // XXX
                    //throw new RemoteException("Cannot change directory '" + dir + "' [" + getReplyString() + "]");
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Directory '" + ftpClient.printWorkingDirectory() + "' created and entered");
                }
            }
        }
        return true;
    }

    private String getPassword() {
        String password = configuration.getPassword();
        assert password != null;
        if (password.length() > 0) {
            return password;
        }
        password = PASSWORDS.get(configuration.hashCode());
        if (password != null) {
            return password;
        }
        PasswordPanel passwordPanel = new PasswordPanel(configuration.getUserName());
        DialogDescriptor input = new DialogDescriptor(passwordPanel,
                NbBundle.getMessage(RemoteClient.class, "LBL_EnterPassword", configuration.getDisplayName()));//NOI18N
        input.setOptions(new Object[]{passwordPanel.getOKButton(), passwordPanel.getCancelButton()});
        if (DialogDisplayer.getDefault().notify(input) == passwordPanel.getOKButton()) {
            password = passwordPanel.getPassword();
            PASSWORDS.put(configuration.hashCode(), password);
            return password;
        }
        return ""; // NOI18N
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
        if (file.getName().equals(NB_METADATA_DIR)) {
            return false;
        }
        return VisibilityQuery.getDefault().isVisible(file);
    }

    // some FTP servers return ".." in directory listing (e.g. Cerberus FTP server) - so ignore them
    private boolean isVisible(FTPFile ftpFile) {
        // #142682
        if (ftpFile == null) {
            // hmm, really weird...
            return false;
        }
        if (ftpFile.isDirectory()) {
            String name = ftpFile.getName();
            for (String ignored : IGNORED_REMOTE_DIRS) {
                if (name.equals(ignored)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getReplyString() {
        String reply = ftpClient.getReplyString();
        if (reply == null) {
            return null;
        }
        return reply.trim();
    }

    private static class PrintCommandListener implements ProtocolCommandListener {
        private final InputOutput io;

        public PrintCommandListener(InputOutput io) {
            assert io != null;
            this.io = io;
        }

        public void protocolCommandSent(ProtocolCommandEvent event) {
            processEvent(event);
        }

        public void protocolReplyReceived(ProtocolCommandEvent event) {
            processEvent(event);
        }

        private void processEvent(ProtocolCommandEvent event) {
            String message = event.getMessage();
            if (message.startsWith("PASS ")) { // NOI18N
                // hide password
                message = "PASS ******\n"; // NOI18N
            }
            if (event.isReply()
                    && (FTPReply.isNegativeTransient(event.getReplyCode()) || FTPReply.isNegativePermanent(event.getReplyCode()))) {
                io.getErr().print(message);
                io.getErr().flush();
            } else {
                io.getOut().print(message);
                io.getOut().flush();
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Command listener: " + message.trim());
            }
        }
    }
}
