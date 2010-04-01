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

package org.netbeans.modules.php.project.connections.sftp;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.common.PasswordPanel;
import org.netbeans.modules.php.project.connections.spi.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * @author Tomas Mysik
 */
public class SftpClient implements RemoteClient {
    private static final Logger LOGGER = Logger.getLogger(SftpClient.class.getName());
    private static final Map<Integer, String> PASSWORDS = new HashMap<Integer, String>();
    private static final Map<Integer, String> PASSPHRASES = new HashMap<Integer, String>();
    private static final Map<Integer, Set<String>> MESSAGES = new HashMap<Integer, Set<String>>();

    private static final SftpLogger DEV_NULL_LOGGER = new DevNullLogger();
    private final SftpConfiguration configuration;
    private final SftpLogger sftpLogger;
    private Session sftpSession;
    private ChannelSftp sftpClient;

    public SftpClient(SftpConfiguration configuration, InputOutput io) {
        assert configuration != null;
        this.configuration = configuration;

        if (io != null) {
            sftpLogger = new SftpLogger(io);
            LOGGER.log(Level.FINE, "Protocol command listener added");
        } else {
            sftpLogger = DEV_NULL_LOGGER;
            LOGGER.log(Level.FINE, "No protocol command listener will be used");
        }
    }

    private void init() throws RemoteException {
        if (sftpClient != null && sftpClient.isConnected()) {
            LOGGER.log(Level.FINE, "SFTP client already created and connected");
            return;
        }
        LOGGER.log(Level.FINE, "SFTP client creating");

        String host = configuration.getHost();
        int port = configuration.getPort();
        int timeout = configuration.getTimeout() * 1000;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Connecting to {0} [timeout: {1} ms]", new Object[] {host, timeout});
        }
        String username = configuration.getUserName();
        String password = configuration.getPassword();
        String knownHostsFile = configuration.getKnownHostsFile();
        String identityFile = configuration.getIdentityFile();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Login as {0}", username);
        }

        JSch jsch = null;
        Channel channel = null;
        jsch = new JSch();
        try {
            JSch.setLogger(sftpLogger);
            sftpSession = jsch.getSession(username, host, port);
            if (StringUtils.hasText(knownHostsFile)) {
                jsch.setKnownHosts(knownHostsFile);
            }
            if (StringUtils.hasText(identityFile)) {
                jsch.addIdentity(identityFile);
            }
            if (StringUtils.hasText(password)) {
                sftpSession.setPassword(password);
            }
            sftpSession.setUserInfo(new SftpUserInfo(configuration));
            sftpSession.setTimeout(timeout);
            sftpSession.connect(timeout);

            channel = sftpSession.openChannel("sftp"); // NOI18N
            channel.connect();
            sftpClient = (ChannelSftp) channel;

        } catch (JSchException exc) {
            // remove password from a memory storage
            PASSWORDS.remove(configuration.hashCode());
            PASSPHRASES.remove(configuration.hashCode());
            MESSAGES.remove(configuration.hashCode());
            disconnect();
            LOGGER.log(Level.FINE, "Exception while connecting", exc);
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotConnect", configuration.getHost()), exc);
        }

    }

    @Override
    public void connect() throws RemoteException {
        init();
        assert sftpClient.isConnected();
        try {

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Remote server version is {0}", sftpClient.getServerVersion());
            }
        } catch (SftpException exc) {
            // can be ignored
            LOGGER.log(Level.FINE, "Exception while getting server version", exc);
        }
    }

    @Override
    public void disconnect() throws RemoteException {
        if (sftpSession == null) {
            // nothing to do
            LOGGER.log(Level.FINE, "Remote client not created yet => nothing to do");
            return;
        }
        LOGGER.log(Level.FINE, "Remote client trying to disconnect");
        if (sftpSession.isConnected()) {
            LOGGER.log(Level.FINE, "Remote client connected -> disconnecting");
            JSch.setLogger(DEV_NULL_LOGGER);
            sftpSession.disconnect();
            LOGGER.log(Level.FINE, "Remote client disconnected");
        }
        sftpClient = null;
        sftpSession = null;

        sftpLogger.info("QUIT"); // NOI18N
        sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_Goodbye"));
    }

    /** not supported by JSCh */
    @Override
    public String getReplyString() {
        return null;
    }

    /** not supported by JSCh */
    @Override
    public String getNegativeReplyString() {
        return null;
    }

    @Override
    public boolean isConnected() {
        if (sftpClient == null) {
            return false;
        }
        return sftpClient.isConnected();
    }

    @Override
    public String printWorkingDirectory() throws RemoteException {
        try {
            sftpLogger.info("PWD"); // NOI18N

            String pwd = sftpClient.pwd();

            sftpLogger.info(pwd);
            return pwd;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while pwd", ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotPwd", configuration.getHost()), ex);
        }
    }

    @Override
    public boolean storeFile(String remote, InputStream local) throws RemoteException {
        try {
            sftpLogger.info("STOR " + remote); // NOI18N

            sftpClient.put(local, remote);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_FileReceiveOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while storing file " + remote, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotStoreFile", remote), ex);
        }
    }

    @Override
    public boolean deleteFile(String pathname) throws RemoteException {
        return delete(pathname, false);
    }

    @Override
    public boolean deleteDirectory(String pathname) throws RemoteException {
        return delete(pathname, true);
    }

    private boolean delete(String pathname, boolean directory) throws RemoteException {
        try {
            sftpLogger.info("DELE " + pathname); // NOI18N

            if (directory) {
                sftpClient.rmdir(pathname);
            } else {
                sftpClient.rm(pathname);
            }

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_FileDeleteOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while deleting file " + pathname, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean rename(String from, String to) throws RemoteException {
        try {
            sftpLogger.info("RNFR " + from); // NOI18N
            sftpLogger.info("RNTO " + to); // NOI18N

            sftpClient.rename(from, to);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_RenameSuccessful"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, String.format("Error while renaming file %s -> %s", from, to), ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public List<RemoteFile> listFiles() throws RemoteException {
        List<RemoteFile> result = null;
        String pwd = null;
        try {
            pwd = sftpClient.pwd();
            sftpLogger.info("LIST"); // NOI18N
            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirListing"));

            @SuppressWarnings("unchecked")
            Collection<ChannelSftp.LsEntry> files = sftpClient.ls(pwd);
            result = new ArrayList<RemoteFile>(files.size());
            for (ChannelSftp.LsEntry entry : files) {
                result.add(new RemoteFileImpl(entry));
            }

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirectorySendOk"));
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while listing files for " + pwd, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotListFiles", pwd), ex);
        }
        return result;
    }

    @Override
    public boolean retrieveFile(String remote, OutputStream local) throws RemoteException {
        try {
            sftpLogger.info("RETR " + remote); // NOI18N

            sftpClient.get(remote, local);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_FileSendOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while retrieving file " + remote, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotStoreFile", remote), ex);
        }
    }

    @Override
    public boolean changeWorkingDirectory(String pathname) throws RemoteException {
        try {
            sftpLogger.info("CWD " + pathname); // NOI18N

            sftpClient.cd(pathname);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_CdOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while changing directory " + pathname, ex);
            sftpLogger.error(NbBundle.getMessage(SftpClient.class, "LOG_CdKo"));
            return false;
        }
    }

    @Override
    public boolean makeDirectory(String pathname) throws RemoteException {
        try {
            sftpLogger.info("MKD " + pathname); // NOI18N

            sftpClient.mkdir(pathname);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_MkDirOk", pathname));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while creating directory " + pathname, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public int getPermissions(String path) throws RemoteException {
        int permissions = -1;
        try {
            sftpLogger.info("LIST " + path); // NOI18N
            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirListing"));

            ChannelSftp.LsEntry file = getFile(path);
            if (file != null) {
                permissions = file.getAttrs().getPermissions();
            }

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_DirectorySendOk"));
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while getting permissions for " + path, ex);
        }
        return permissions;
    }

    @Override
    public boolean setPermissions(int permissions, String path) throws RemoteException {
        try {
            sftpLogger.info(String.format("chmod %d %s", permissions, path)); // NOI18N

            sftpClient.chmod(permissions, path);

            sftpLogger.info(NbBundle.getMessage(SftpClient.class, "LOG_ChmodOk"));
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while setting permissions for " + path, ex);
            sftpLogger.error(ex.getLocalizedMessage());
            return false;
        }
    }

    @Override
    public boolean exists(String parent, String name) throws RemoteException {
        String fullPath = parent + "/" + name; // NOI18N
        try {
            sftpClient.ls(fullPath);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while checking existence of " + fullPath, ex);
        }
        return false;
    }

    private ChannelSftp.LsEntry getFile(String path) throws SftpException {
        assert path != null && path.trim().length() > 0;

        @SuppressWarnings("unchecked")
        List<ChannelSftp.LsEntry> files = sftpClient.ls(path);
        // in fact, the size of the list should be exactly 1
        LOGGER.fine(String.format("Exactly 1 file should be found for %s; found %d", path, files.size()));
        if (files.size() > 0) {
            return files.get(0);
        }
        return null;
    }

    static String getPasswordForUser(SftpConfiguration configuration) {
        String password = PASSWORDS.get(configuration.hashCode());
        if (password == null) {
            PasswordPanel passwordPanel = PasswordPanel.forUser(configuration.getDisplayName(), configuration.getUserName());
            if (passwordPanel.open()) {
                password = passwordPanel.getPassword();
                PASSWORDS.put(configuration.hashCode(), password);
            }
        }
        return password;
    }

    static String getPasswordForCertificate(SftpConfiguration configuration) {
        String password = PASSPHRASES.get(configuration.hashCode());
        if (password == null) {
            PasswordPanel passwordPanel = PasswordPanel.forCertificate(configuration.getDisplayName());
            if (passwordPanel.open()) {
                password = passwordPanel.getPassword();
                PASSPHRASES.put(configuration.hashCode(), password);
            }
        }
        return password;
    }

    static void showMessageForConfiguration(SftpConfiguration configuration, String message) {
        if (!StringUtils.hasText(message)
                || getMessages(configuration).contains(message)) {
            return;
        }
        MessagePanel messagePanel = new MessagePanel(message);
        DialogDescriptor descriptor = new DialogDescriptor(
                messagePanel,
                configuration.getDisplayName(),
                true,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);
        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION) {
            if (messagePanel.doNotShowThisMessageAgain()) {
                getMessages(configuration).add(message);
            }
        }
    }

    private static Set<String> getMessages(SftpConfiguration configuration) {
        Set<String> messages;
        synchronized (MESSAGES) {
            messages = MESSAGES.get(configuration.hashCode());
            if (messages == null) {
                messages = new HashSet<String>();
                MESSAGES.put(configuration.hashCode(), messages);
            }
        }
        return messages;
    }

    private static final class RemoteFileImpl implements RemoteFile {
        private final ChannelSftp.LsEntry entry;

        public RemoteFileImpl(ChannelSftp.LsEntry entry) {
            assert entry != null;
            this.entry = entry;
        }

        @Override
        public String getName() {
            return entry.getFilename();
        }

        @Override
        public boolean isDirectory() {
            return entry.getAttrs().isDir();
        }

        @Override
        public boolean isFile() {
            return !isDirectory();
        }

        @Override
        public long getSize() {
            return entry.getAttrs().getSize();
        }

        @Override
        public long getTimestamp() {
            return entry.getAttrs().getMTime();
        }
    }

    private static class SftpLogger implements com.jcraft.jsch.Logger {
        private final InputOutput io;

        public SftpLogger(InputOutput io) {
            this.io = io;
        }

        @Override
        public boolean isEnabled(int level) {
            return level >= com.jcraft.jsch.Logger.INFO;
        }

        @Override
        public void log(int level, String message) {
            assert io != null;
            OutputWriter writer = null;
            if (level <= com.jcraft.jsch.Logger.INFO) {
                writer = io.getOut();
            } else {
                writer = io.getErr();
            }
            writer.println(message.trim());
            writer.flush();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Command listener: {0}", message.trim());
            }
        }

        public void info(String message) {
            log(com.jcraft.jsch.Logger.INFO, message);
        }

        public void error(String message) {
            log(com.jcraft.jsch.Logger.ERROR, message);
        }
    }

    /**
     * Because {@link java.nio.channels.ClosedByInterruptException} is raised while disconnecting SFTP session
     * (this exception makes oproblems to NB output window).
     * @see #disconnect()
     */
    private static final class DevNullLogger extends SftpLogger {

        public DevNullLogger() {
            super(null);
        }

        @Override
        public boolean isEnabled(int level) {
            return false;
        }

        @Override
        public void log(int level, String message) {
        }
    }

    private static final class SftpUserInfo implements UserInfo, UIKeyboardInteractive {
        private final SftpConfiguration configuration;
        private volatile String passwd;
        private volatile String passphrase;

        public SftpUserInfo(SftpConfiguration configuration) {
            assert configuration != null;

            this.configuration = configuration;
        }

        @Override
        public boolean promptYesNo(String message) {
            NotifyDescriptor descriptor = new NotifyDescriptor(
                    message,
                    NbBundle.getMessage(SftpClient.class, "LBL_Warning"),
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    new Object[] {NotifyDescriptor.YES_OPTION, NotifyDescriptor.NO_OPTION},
                    NotifyDescriptor.YES_OPTION);
            return DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION;
        }

        @Override
        public String getPassphrase() {
            return passphrase;
        }

        @Override
        public boolean promptPassphrase(String message) {
            passphrase = getPasswordForCertificate(configuration);
            return passphrase != null;
        }

        @Override
        public String getPassword() {
            return passwd;
        }

        @Override
        public boolean promptPassword(String message) {
            passwd = getPasswordForUser(configuration);
            return passwd != null;
        }

        @Override
        public void showMessage(String message) {
            showMessageForConfiguration(configuration, message);
        }

        @Override
        public String[] promptKeyboardInteractive(String destination, String name, String instruction,
                String[] prompt, boolean[] echo) {

            // #166555
            if (prompt.length == 1
                    && echo.length == 1 && !echo[0]) {
                // ask for password
                passwd = configuration.getPassword();
                if (!StringUtils.hasText(passwd)) {
                    passwd = getPasswordForUser(configuration);
                }
                if (StringUtils.hasText(passwd)) {
                    return new String[] {passwd};
                }
            }
            return null;
        }
    }
}
