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

package org.netbeans.modules.php.project.connections.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.spi.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.netbeans.modules.php.project.connections.ui.PasswordPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class FtpClient implements RemoteClient {
    private static final Logger LOGGER = Logger.getLogger(FtpClient.class.getName());
    // store not provided passwords in memory only
    private static final Map<Integer, String> PASSWORDS = new HashMap<Integer, String>();

    private final FtpConfiguration configuration;
    private final InputOutput io;
    private final FTPClient ftpClient;


    public FtpClient(RemoteConfiguration configuration, InputOutput io) {
        assert configuration != null;
        assert configuration instanceof FtpConfiguration : "FTP configuration needed but the type was " + configuration.getClass().getName();
        this.configuration = (FtpConfiguration) configuration;
        this.io = io;

        LOGGER.log(Level.FINE, "FTP client creating");
        ftpClient = new FTPClient();
        if (io != null) {
            ftpClient.addProtocolCommandListener(new PrintCommandListener(io));
        }
        LOGGER.log(Level.FINE, "Protocol command listener added");
    }

    public void connect() throws RemoteException {
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
                throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpRefusedConnection", configuration.getHost()), getReplyString());
            }

            // login
            LOGGER.fine("Login as " + configuration.getUserName());
            if (!ftpClient.login(configuration.getUserName(), password)) {
                LOGGER.fine("Login unusuccessful -> logout");
                ftpClient.logout();
                // remove password from a memory storage
                PASSWORDS.remove(configuration.hashCode());
                throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpLoginFailed"), getReplyString());
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
        } catch (IOException ex) {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    LOGGER.log(Level.FINE, "Exception while disconnecting", e);
                }
            }
            LOGGER.log(Level.FINE, "Exception while connecting", ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotConnect", configuration.getHost()), ex, getReplyString());
        }
    }

    public void disconnect() throws RemoteException {
        LOGGER.log(Level.FINE, "Remote client trying to disconnect");
        if (ftpClient.isConnected()) {
            LOGGER.log(Level.FINE, "Remote client connected -> disconnecting");
            try {
                ftpClient.logout();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, "Error while disconnecting", ex);
                throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotLogout", configuration.getHost()), ex, getReplyString());
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
                NbBundle.getMessage(FtpClient.class, "LBL_EnterPassword", configuration.getDisplayName()));//NOI18N
        input.setOptions(new Object[]{passwordPanel.getOKButton(), passwordPanel.getCancelButton()});
        if (DialogDisplayer.getDefault().notify(input) == passwordPanel.getOKButton()) {
            password = passwordPanel.getPassword();
            PASSWORDS.put(configuration.hashCode(), password);
            return password;
        }
        return ""; // NOI18N
    }

    public String getReplyString() {
        String reply = ftpClient.getReplyString();
        if (reply == null) {
            return null;
        }
        return reply.trim();
    }

    public String getNegativeReplyString() {
        int replyCode = ftpClient.getReplyCode();
        if (FTPReply.isNegativePermanent(replyCode)
                || FTPReply.isNegativeTransient(replyCode)) {
            return getReplyString();
        }
        return null;
    }

    public boolean isConnected() {
        return ftpClient.isConnected();
    }

    public String printWorkingDirectory() throws RemoteException {
        try {
            return ftpClient.printWorkingDirectory();
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error while pwd", ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotPwd", configuration.getHost()), ex, getReplyString());
        }
    }

    public boolean storeFile(String remote, InputStream local) throws RemoteException {
        try {
            return ftpClient.storeFile(remote, local);
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error while storing file " + remote, ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotStoreFile", remote), ex, getReplyString());
        }
    }

    public boolean deleteFile(String pathname) throws RemoteException {
        try {
            return ftpClient.deleteFile(pathname);
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error while deleting file " + pathname, ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotDeleteFile", pathname), ex, getReplyString());
        }
    }

    public boolean rename(String from, String to) throws RemoteException {
        try {
            return ftpClient.rename(from, to);
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, String.format("Error while renaming file %s -> %s", from, to), ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotRenameFile", from, to), ex, getReplyString());
        }
    }

    public List<RemoteFile> listFiles(PathInfo pathInfo) throws RemoteException {
        List<RemoteFile> result = null;
        try {
            FTPFile[] files = ftpClient.listFiles();
            result = new ArrayList<RemoteFile>(files.length);
            for (FTPFile f : files) {
                // #142682
                if (f == null) {
                    // hmm, really weird...
                    LOGGER.fine("NULL returned for listing of " + pathInfo);
                    continue;
                }
                result.add(new RemoteFileImpl(f));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error while listing files for " + pathInfo, ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotListFiles", pathInfo.getFullPath()), ex, getReplyString());
        }
        return result;
    }

    public boolean retrieveFile(String remote, OutputStream local) throws RemoteException {
        try {
            return ftpClient.retrieveFile(remote, local);
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error while retrieving file " + remote, ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotStoreFile", remote), ex, getReplyString());
        }
    }

    public boolean changeWorkingDirectory(String pathname) throws RemoteException {
        try {
            return ftpClient.changeWorkingDirectory(pathname);
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error while changing directory " + pathname, ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotChangeDirectory", pathname), ex, getReplyString());
        }
    }

    public boolean makeDirectory(String pathname) throws RemoteException {
        try {
            return ftpClient.makeDirectory(pathname);
        } catch (IOException ex) {
            LOGGER.log(Level.FINE, "Error while creating directory " + pathname, ex);
            throw new RemoteException(NbBundle.getMessage(FtpClient.class, "MSG_FtpCannotCreateDirectory", pathname), ex, getReplyString());
        }
    }

    private static final class PrintCommandListener implements ProtocolCommandListener {
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

    private static final class RemoteFileImpl implements RemoteFile {
        private final FTPFile ftpFile;

        public RemoteFileImpl(FTPFile ftpFile) {
            assert ftpFile != null;
            this.ftpFile = ftpFile;
        }

        public String getName() {
            return ftpFile.getName();
        }

        public boolean isDirectory() {
            return ftpFile.isDirectory();
        }

        public boolean isFile() {
            return ftpFile.isFile();
        }

        public long getSize() {
            return ftpFile.getSize();
        }
    }
}
