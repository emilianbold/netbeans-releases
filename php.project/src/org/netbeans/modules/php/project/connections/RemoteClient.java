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

// XXX
// check local vs remote file
//  - if remote not found => skip (add to ignored)
//  - if remote is folder and local is file (and vice versa) => skip (add to ignored)
// translate some of well-known exceptions
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
                LOGGER.fine("Reply is " + ftpClient.getReplyString());
            }
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                LOGGER.fine("Disconnecting because of negative reply");
                ftpClient.disconnect();
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpRefusedConnection", configuration.getHost()), ftpClient.getReplyString());
            }

            // login
            LOGGER.fine("Login as " + configuration.getUserName());
            if (!ftpClient.login(configuration.getUserName(), password)) {
                LOGGER.fine("Login unusuccessful -> logout");
                ftpClient.logout();
                // remove password from a memory storage
                PASSWORDS.remove(configuration.hashCode());
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpLoginFailed"), ftpClient.getReplyString());
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
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", baseRemoteDirectory), ftpClient.getReplyString());
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
            throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotConnect", configuration.getHost()), ex, ftpClient.getReplyString());
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
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotLogout", configuration.getHost()), ex, ftpClient.getReplyString());
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
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage()));
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
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading file " + fileName + " => " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + fileName);
            }
            // XXX lock the file?
            InputStream is = new FileInputStream(new File(baseLocalDir, file.getRelativePath(true)));
            try {
                if (ftpClient.storeFile(fileName, is)) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getFailureMessage(fileName, true));
                }
            } finally {
                is.close();
            }
        }
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
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage()));
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", exc.getMessage()));
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
            // XXX handle if exists but it is a file
            if (!localFile.exists()) {
                if (!localFile.mkdirs()) {
                    transferFailed(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_CannotCreateDir", localFile));
                    return;
                }
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
            }
            assert parent.isDirectory() : "Parent file of " + localFile + " must be a directory";

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading " + file.getRelativePath() + " => " + localFile.getAbsolutePath());
            }

            // XXX check if the remote file exists?

            if (!cdBaseRemoteDirectory(file.getParentRelativePath(), false)) {
                LOGGER.fine("Remote directory " + file.getParentRelativePath() + " does not exist => ignoring file " + file.getRelativePath());
                transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", file.getParentRelativePath()));
                return;
            }

            // XXX lock the file?
            OutputStream os = new FileOutputStream(localFile);
            try {
                if (ftpClient.retrieveFile(file.getName(), os)) {
                    transferSucceeded(transferInfo, file);
                } else {
                    transferFailed(transferInfo, file, getFailureMessage(file.getName(), false));
                }
            } finally {
                os.close();
            }
        } else {
            transferIgnored(transferInfo, file, NbBundle.getMessage(RemoteClient.class, "MSG_FtpUnknownFileType", file.getRelativePath()));
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
        transferInfo.addFailed(file, reason);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Failed: " + file + ", reason: " + reason);
        }
    }

    private void transferIgnored(TransferInfo transferInfo, TransferFile file, String reason) {
        transferInfo.addIgnored(file, reason);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Ignored: " + file + ", reason: " + reason);
        }
    }

    private String getFailureMessage(String fileName, boolean upload) {
        String message = null;
        int replyCode = ftpClient.getReplyCode();
        if (FTPReply.isNegativePermanent(replyCode)
                || FTPReply.isNegativeTransient(replyCode)) {
            message = NbBundle.getMessage(RemoteClient.class, "MSG_FtpErrorReason", ftpClient.getReplyString());
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
                throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotChangeDirectory", "/"), ftpClient.getReplyString());
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
                    throw new RemoteException(NbBundle.getMessage(RemoteClient.class, "MSG_FtpCannotCreateDirectory", dir), ftpClient.getReplyString());
                } else if (!ftpClient.changeWorkingDirectory(dir)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Cannot enter directory: " + ftpClient.printWorkingDirectory() + TransferFile.SEPARATOR + dir);
                    }
                    return false;
                    // XXX
                    //throw new RemoteException("Cannot change directory '" + dir + "' [" + ftpClient.getReplyString() + "]");
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
        }
    }
}
