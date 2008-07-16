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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Remote client able to connect/disconnect to FTP
 * as well as download/upload files to a FTP server.
 * <p>
 * Every method is synchronized and throws {@link RemoteException}
 * if any error occurs.
 * @author Tomas Mysik
 */
public class RemoteClient {
    private static final Logger LOGGER = Logger.getLogger(RemoteClient.class.getName());
    private static final Comparator<FileObject> FILE_OBJECT_COMPARATOR = new FileObjectComparator();

    private final RemoteConfiguration configuration;
    private final String baseRemoteDirectory;
    private FTPClient ftpClient;

    public RemoteClient(RemoteConfiguration configuration) {
        this(configuration, null);
    }

    public RemoteClient(RemoteConfiguration configuration, String additionalInitialSubdirectory) {
        assert configuration != null;
        StringBuilder baseDir = new StringBuilder(configuration.getInitialDirectory());
        if (additionalInitialSubdirectory != null && additionalInitialSubdirectory.length() > 0) {
            baseDir.append("/"); // NOI18N
            baseDir.append(additionalInitialSubdirectory);
        }
        this.configuration = configuration;
        baseRemoteDirectory = baseDir.toString().replaceAll("/{2,}", "/"); // NOI18N
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Remote client created with configuration: " + configuration + " and base remote directory: " + baseRemoteDirectory);
        }
    }

    public synchronized void connect() throws RemoteException {
        init();
        try {
            // connect
            int timeout = configuration.getTimeout() * 1000;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Connecting to " + configuration.getHost() + " [timeout: " + timeout + " ms]");
            }
            ftpClient.setDefaultTimeout(timeout);
            ftpClient.connect(configuration.getHost(), configuration.getPort());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Reply is " + ftpClient.getReplyString());
            }
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                LOGGER.fine("Disconnecting because of negative reply");
                ftpClient.disconnect();
                // XXX
                throw new RemoteException("FTP server refused connection.");
            }

            // login
            LOGGER.fine("Login as " + configuration.getUserName());
            if (!ftpClient.login(configuration.getUserName(), configuration.getPassword())) {
                LOGGER.fine("Login unusuccessful -> logout");
                ftpClient.logout();
                return;
            }
            LOGGER.fine("Login successful");

            // XXX mostly behind firewalls today
            LOGGER.fine("Setting passive mode");
            ftpClient.enterLocalPassiveMode();
            // XXX binary mode as a default?
            LOGGER.fine("Setting file type to BINARY");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Remote system is " + ftpClient.getSystemName());
            }

            // cd to base remote directory
            cdBaseRemoteDirectory();

        } catch (IOException ex) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Exception while disconnecting", ex);
                }
            }
            LOGGER.log(Level.FINE, "Exception while connecting", ex);
            // XXX
            throw new RemoteException("Could not connect to server.", ex);
        }
    }

    public synchronized void disconnect() throws RemoteException {
        init();
        LOGGER.log(Level.FINE, "Remote client trying to disconnect");
        if (ftpClient.isConnected()) {
            LOGGER.log(Level.FINE, "Remote client connected -> disconnecting");
            try {
                ftpClient.logout();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "XXX", ex);
                // XXX
                throw new RemoteException("XXX", ex);
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

    public synchronized TransferInfo<FileObject> upload(FileObject baseLocalDirectory, FileObject... filesToUpload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToUpload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToUpload.length > 0 : "At least one file to upload must be specified";

        ensureConnected();
        final long start = System.currentTimeMillis();
        TransferInfo<FileObject> transferInfo = new TransferInfo<FileObject>();

        Queue<FileObject> queue = new LinkedList<FileObject>();
        for (FileObject fo : filesToUpload) {
            queue.add(fo);
        }

        try {
            File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
            while(!queue.isEmpty()) {
                FileObject fo = queue.poll();

                if (!checkFileToTransfer(transferInfo, fo)) {
                    continue;
                }

                try {
                    uploadFile(baseLocalDir, fo);
                    transferSucceeded(transferInfo, fo);
                } catch (IOException exc) {
                    transferFailed(transferInfo, fo);
                    continue;
                } catch (RemoteException exc) {
                    transferFailed(transferInfo, fo);
                    continue;
                }

                for (FileObject child : fo.getChildren()) {
                    queue.offer(child);
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

    private void uploadFile(File baseLocalDir, FileObject fo) throws IOException, RemoteException {
        assert Thread.holdsLock(this);

        if (fo.isFolder()) {
            // folder => just ensure that it exists
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading directory: " + fo);
            }
            // in fact, useless but probably expected
            String relativePath = PropertyUtils.relativizeFile(baseLocalDir, FileUtil.toFile(fo));
            cdRemoteDirectory(relativePath);
        } else {
            // file => simply upload it

            String relativePath = PropertyUtils.relativizeFile(baseLocalDir, FileUtil.toFile(fo.getParent()));
            cdRemoteDirectory(relativePath);

            String fileName = fo.getNameExt();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading file " + fileName + " => " + ftpClient.printWorkingDirectory() + "/" + fileName);
            }
            // XXX lock the file?
            InputStream is = fo.getInputStream();
            try {
                ftpClient.storeFile(fileName, is);
            } finally {
                is.close();
            }
        }
    }

    private <T> boolean checkFileToTransfer(TransferInfo<T> transferInfo, T type) {
        if (transferInfo.isTransfered(type)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Skipping, file already transfered: " + type);
            }
            return false;
        } else if (transferInfo.isFailed(type)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Skipping, file already failed: " + type);
            }
            return false;
        } else if (transferInfo.isIgnored(type)) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Skipping, file already ignored: " + type);
            }
            return false;
        }
        return true;
    }

    private <T> void transferSucceeded(TransferInfo<T> transferInfo, T type) {
        transferInfo.addTransfered(type);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Uploaded " + type);
        }
    }

    private <T> void transferFailed(TransferInfo<T> transferInfo, T type) {
        transferInfo.addFailed(type);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Failed " + type);
        }
    }

    // XXX probably add stack for actual remote path
    public synchronized TransferInfo<RemoteFile> download(FileObject baseLocalDirectory, FileObject... filesToDownload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToDownload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToDownload.length > 0 : "At least one file to download must be specified";

        ensureConnected();

        long start = System.currentTimeMillis();
        TransferInfo<RemoteFile> transferInfo = new TransferInfo<RemoteFile>();

        // XXX optimize filesToDownload (if there is sources there, remove all the other files etc.)
        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        // XXX FTPFile => RemoteFile
        Set<RemoteFile> remoteFiles = new HashSet<RemoteFile>();
        for (FileObject fo : filesToDownload) {
            String pathname = PropertyUtils.relativizeFile(baseLocalDir, FileUtil.toFile(fo));
            FTPFile[] listFiles;
            try {
                // remove, see "XXX optimize ..."
                if (".".equals(pathname)) { // NOI18N
                    cdBaseRemoteDirectory();
                } else {
//                    if (!changeDirectory(baseRemoteDirectory + "/" + pathname, false)) {
//                        // XXX add to ignored
//                        continue;
//                    }
                }
                for (FTPFile fTPFile : ftpClient.listFiles()) {
                    remoteFiles.add(new RemoteFile(fTPFile, baseRemoteDirectory, ftpClient.printWorkingDirectory()));
                }
            } catch (IOException ex) {
                // XXX
                //transferInfo.addIgnored(pathname);
                // XXX
                throw new RemoteException("Error while downloading files from the server [" + pathname + "]", ex);
            }
        }

        try {
            downloadFiles(transferInfo, baseLocalDir, remoteFiles.toArray(new RemoteFile[remoteFiles.size()]));
        } finally {
            transferInfo.setRuntime(System.currentTimeMillis() - start);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine(transferInfo.toString());
            }
        }
        return transferInfo;
    }

    // can be merged with downloadFile()
    private void downloadFiles(TransferInfo<RemoteFile> transferInfo, File baseLocalDir, RemoteFile... filesToDownload) throws RemoteException {
        assert Thread.holdsLock(this);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Downloading files (base local directory: " + baseLocalDir + ")");
            for (RemoteFile file : filesToDownload) {
                LOGGER.fine("\t" + file);
            }
        }

        for (RemoteFile file : filesToDownload) {
            // XXX cancelable
            try {
                if (transferInfo.isTransfered(file)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Skipping, file already downloaded: " + file);
                    }
                    return;
                } else if (transferInfo.isFailed(file)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Skipping, file already failed: " + file);
                    }
                    return;
                } else if (transferInfo.isIgnored(file)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Skipping, file already ignored: " + file);
                    }
                    return;
                }
                downloadFile(transferInfo, baseLocalDir, file);
            } catch (IOException ex) {
                transferInfo.addFailed(file);
                // XXX
                throw new RemoteException("Error while downloading files from the server", ex);
            } catch (RemoteException ex) {
                transferInfo.addFailed(file);
                throw ex;
            }
        }
    }

    private void downloadFile(TransferInfo<RemoteFile> transferInfo, File baseLocalDir, RemoteFile file) throws IOException, RemoteException {
        assert Thread.holdsLock(this);

        // XXX
        // check local vs remote file
        //  - if remote not found => skip (add to ignored)
        //  - if remote is folder and local is file (and vice versa) => skip (add to ignored)
        // for non-existing - simply download it?
        // for folder - download all the fildren
        // for file - just download the file (maybe check whether it is opened in the editor?)

        // XXX performance performance performance
        // change directory first
        //cdBaseRemoteDirectory();

        File localFile = new File(baseLocalDir, file.getRelativePath());
        if (file.isDirectory()) {
            // folder => download all the children
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading all the children of: " + file);
            }
            String absolutePath = file.getAbsolutePath();
            // XXX handle if exists but it is a file
            if (!localFile.exists()) {
                localFile.mkdirs();
            }
            //changeDirectory(absolutePath, false);
            FTPFile[] files = ftpClient.listFiles();
            if (files.length > 0) {
                List<RemoteFile> remoteFiles = new ArrayList<RemoteFile>(files.length);
                for (FTPFile fTPFile : ftpClient.listFiles()) {
                    remoteFiles.add(new RemoteFile(fTPFile, baseRemoteDirectory, absolutePath));
                }
                downloadFiles(transferInfo, baseLocalDir, remoteFiles.toArray(new RemoteFile[remoteFiles.size()]));
            }
        } else {
            // file => simply download it

            // download file
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Downloading " + file.getAbsolutePath() + " => " + localFile.getAbsolutePath());
            }
            // XXX lock the file?
            OutputStream os = new FileOutputStream(localFile);
            try {
                if (ftpClient.retrieveFile(file.getAbsolutePath(), os)) {
                    transferInfo.addTransfered(file);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Downloaded " + file);
                    }
                } else {
                    transferInfo.addFailed(file);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Failed " + file);
                    }
                }
            } finally {
                os.close();
            }
        }
    }

    private void init() {
        assert Thread.holdsLock(this);
        if (ftpClient != null) {
            return;
        }
        LOGGER.log(Level.FINE, "FTP client creating");
        ftpClient = new FTPClient();
        // XXX
        ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        LOGGER.log(Level.FINE, "Protocol command listener added");
    }

    private void ensureConnected() throws RemoteException {
        assert Thread.holdsLock(this);
        init();
        if (!ftpClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }
    }

    private void cdBaseRemoteDirectory() throws IOException, RemoteException {
        cdRemoteDirectory(null);
    }

    private void cdRemoteDirectory(String subdirectory) throws IOException, RemoteException {
        assert Thread.holdsLock(this);
        assert subdirectory == null || !subdirectory.startsWith("/") : "Subdirectory must be null or relative (cannot start with '/' (slash))" ;

        String path = baseRemoteDirectory;
        if (subdirectory != null && !subdirectory.equals(".")) { // NOI18N
            path = baseRemoteDirectory + "/" + subdirectory; // NOI18N
        }
        LOGGER.fine("Changing directory to " + path);
        if (!ftpClient.changeWorkingDirectory(path)) {
            createAndCdRemoteDirectory(path);
        }
    }

    /**
     * Create file path on FTP server <b>in the current directory</b>.
     * @param filePath file path to create, can be even relative (e.g. "a/b/c/d").
     */
    private void createAndCdRemoteDirectory(String filePath) throws IOException, RemoteException {
        assert Thread.holdsLock(this);
        LOGGER.fine("Creating file path " + filePath);
        if (filePath.startsWith("/")) { // NOI18N
            // enter root directory
            if (!ftpClient.changeWorkingDirectory("/")) { // NOI18N
                throw new RemoteException("Cannot change root directory '/' [" + ftpClient.getReplyString() + "]");
            }
        }
        for (String dir : filePath.split("/")) { // NOI18N
            if (dir.length() == 0) {
                // handle paths like "a//b///c/d" (dir can be "")
                continue;
            }
            if (!ftpClient.changeWorkingDirectory(dir)) {
                if (!ftpClient.makeDirectory(dir)) {
                    // XXX check 52x codes
                    throw new RemoteException("Cannot create directory '" + dir + "' [" + ftpClient.getReplyString() + "]");
                } else if (!ftpClient.changeWorkingDirectory(dir)) {
                    // XXX
                    throw new RemoteException("Cannot change directory '" + dir + "' [" + ftpClient.getReplyString() + "]");
                }
                LOGGER.fine("Directory '" + ftpClient.printWorkingDirectory() + "' created and entered");
            }
        }
    }

    // XXX - improve
    private static final class FileObjectComparator implements Comparator<FileObject> {
        public int compare(FileObject fo1, FileObject fo2) {
            assert fo1 != null;
            assert fo2 != null;
            if (fo1.isData()) {
                return -1;
            } else if (fo2.isData()) {
                return 1;
            }
            return 0;
        }
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

    public static final class TransferInfo<T> {
        private final Set<T> transfered = new HashSet<T>();
        private final Set<T> failed = new HashSet<T>();
        private final Set<T> ignored = new HashSet<T>();
        private long runtime;

        public Set<T> getTransfered() {
            return Collections.unmodifiableSet(transfered);
        }

        public Set<T> getFailed() {
            return Collections.unmodifiableSet(failed);
        }

        public Set<T> getIgnored() {
            return Collections.unmodifiableSet(ignored);
        }

        public long getRuntime() {
            return runtime;
        }

        public boolean isTransfered(T fo) {
            return transfered.contains(fo);
        }

        public boolean isFailed(T fo) {
            return failed.contains(fo);
        }

        public boolean isIgnored(T fo) {
            return ignored.contains(fo);
        }

        void addTransfered(T fo) {
            transfered.add(fo);
        }

        void addFailed(T fo) {
            failed.add(fo);
        }

        void addIgnored(T fo) {
            ignored.add(fo);
        }

        void setRuntime(long runtime) {
            this.runtime = runtime;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(200);
            sb.append(getClass().getName());
            sb.append(" [transfered: "); // NOI18N
            sb.append(transfered);
            sb.append(", failed: "); // NOI18N
            sb.append(failed);
            sb.append(", ignored: "); // NOI18N
            sb.append(ignored);
            sb.append(", runtime: "); // NOI18N
            sb.append(runtime);
            sb.append(" ms]"); // NOI18N
            return sb.toString();
        }
    }

    private static class PrintCommandListener implements ProtocolCommandListener {

        private final PrintWriter writer;

        public PrintCommandListener(PrintWriter writer) {
            this.writer = writer;
        }

        public void protocolCommandSent(ProtocolCommandEvent event) {
            writer.print(event.getMessage());
            writer.flush();
        }

        public void protocolReplyReceived(ProtocolCommandEvent event) {
            writer.print(event.getMessage());
            writer.flush();
        }
    }
}
