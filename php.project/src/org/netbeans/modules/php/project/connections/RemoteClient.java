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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
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
            LOGGER.fine("Connecting to " + configuration.getHost());
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

    public synchronized void upload(FileObject baseLocalDirectory, FileObject... filesToUpload) throws RemoteException {
        assert baseLocalDirectory != null;
        assert filesToUpload != null;
        assert baseLocalDirectory.isFolder() : "Base local directory must be a directory";
        assert filesToUpload.length > 0 : "At least one file to upload must be specified";

        init();
        if (!ftpClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }

        long start = System.currentTimeMillis();
        Set<FileObject> uploaded = new HashSet<FileObject>();
        Set<FileObject> failed = new HashSet<FileObject>();
        File baseLocalDir = FileUtil.toFile(baseLocalDirectory);
        try {
            uploadFiles(uploaded, failed, baseLocalDir, filesToUpload);
        } finally {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploaded files: " + uploaded);
                LOGGER.fine("Failed files: " + failed);
                LOGGER.fine("Operation took: " + (System.currentTimeMillis() - start) + " ms");
            }
        }
    }

    private void uploadFiles(Set<FileObject> uploaded, Set<FileObject> failed, File baseLocalDir, FileObject... filesToUpload) throws RemoteException {
        assert Thread.holdsLock(this);

        // sort files by name for better performance
        Arrays.sort(filesToUpload, FILE_OBJECT_COMPARATOR);

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Uploading files (base directory: " + baseLocalDir + ")");
            for (FileObject file : filesToUpload) {
                LOGGER.fine("\t" + file);
            }
        }

        for (FileObject fo : filesToUpload) {
            // XXX cancelable
            try {
                if (uploaded.contains(fo)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Skipping, file already uploaded: " + fo);
                    }
                    return;
                } else if (failed.contains(fo)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Skipping, file already failed: " + fo);
                    }
                    return;
                }
                uploadFile(uploaded, failed, baseLocalDir, fo);
                uploaded.add(fo);
            } catch (IOException ex) {
                failed.add(fo);
                // XXX
                throw new RemoteException("Error while uploading files to the server", ex);
            } catch (RemoteException ex) {
                failed.add(fo);
                throw ex;
            }
        }
    }

    private void uploadFile(Set<FileObject> uploaded, Set<FileObject> failed, File baseLocalDir, FileObject fo) throws IOException, RemoteException {
        assert Thread.holdsLock(this);

        if (fo.isFolder()) {
            // folder => upload all the children
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading all the children of: " + fo);
            }
            FileObject[] children = fo.getChildren();
            if (children.length > 0) {
                uploadFiles(uploaded, failed, baseLocalDir, children);
            }
        } else {
            // file => simply upload it

            // XXX performance
            // change directory first
            cdBaseRemoteDirectory();
            String relativePath = PropertyUtils.relativizeFile(baseLocalDir, FileUtil.toFile(fo.getParent()));
            if (!".".equals(relativePath)) { // NOI18N
                changeDirectory(relativePath, true);
            }

            // upload file
            String fileName = fo.getNameExt();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Uploading " + fileName + " => " + ftpClient.printWorkingDirectory() + "/" + fileName);
            }
            // XXX lock the file?
            InputStream is = fo.getInputStream();
            try {
                if (ftpClient.storeFile(fileName, is)) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Uploaded " + fo);
                    }
                } else {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Failed " + fo);
                    }
                }
            } finally {
                is.close();
            }
        }
    }

    public synchronized void changeDirectory(String path, boolean create) throws RemoteException {
        init();
        if (!ftpClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }

        try {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Changing directory to: " + path + " [create: " + create + "]");
            }
            if (!ftpClient.changeWorkingDirectory(path) && create) {
                createAndCdRemoteDirectory(path);
            }
        } catch (IOException ex) {
            // XXX
            throw new RemoteException("XXX", ex);
        }
    }

    public synchronized String getWorkingDirectory() throws RemoteException {
        init();
        if (!ftpClient.isConnected()) {
            LOGGER.fine("Client not connected -> connecting");
            connect();
        }

        try {
            return ftpClient.printWorkingDirectory();
        } catch (IOException ex) {
            // XXX
            throw new RemoteException("XXX", ex);
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

    private void cdBaseRemoteDirectory() throws IOException, RemoteException {
        assert Thread.holdsLock(this);
        LOGGER.fine("Changing directory to " + baseRemoteDirectory);
        if (!ftpClient.changeWorkingDirectory(baseRemoteDirectory)) {
            createAndCdRemoteDirectory(baseRemoteDirectory);
        }
        if (!ftpClient.changeWorkingDirectory(baseRemoteDirectory)) {
            // XXX check return codes
            throw new RemoteException("Cannot change directory " + baseRemoteDirectory);
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
                throw new RemoteException("Cannot change directory '/' [" + ftpClient.getReplyString() + "]");
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

    private static final class FileObjectComparator implements Comparator<FileObject> {
        public int compare(FileObject fo1, FileObject fo2) {
            assert fo1 != null;
            assert fo2 != null;
            if (fo1.isData()) {
                return 1;
            } else if (fo2.isData()) {
                return -1;
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
