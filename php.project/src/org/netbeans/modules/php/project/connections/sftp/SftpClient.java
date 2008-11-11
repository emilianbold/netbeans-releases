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
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.apache.commons.net.ProtocolCommandEvent;
import org.apache.commons.net.ProtocolCommandListener;
import org.netbeans.modules.php.project.connections.RemoteException;
import org.netbeans.modules.php.project.connections.common.PasswordPanel;
import org.netbeans.modules.php.project.connections.spi.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteFile;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public class SftpClient implements RemoteClient {
    private static final Logger LOGGER = Logger.getLogger(SftpClient.class.getName());

    private static final com.jcraft.jsch.Logger DEV_NULL_LOGGER = new DevNullLogger();
    private final SftpConfiguration configuration;
    private final InputOutput io;
    private Session sftpSession;
    private ChannelSftp sftpClient;


    public SftpClient(SftpConfiguration configuration, InputOutput io) {
        assert configuration != null;
        this.configuration = configuration;
        this.io = io;
    }

    private void init() throws RemoteException {
        if (sftpClient != null && sftpClient.isConnected()) {
            LOGGER.log(Level.FINE, "SFTP client alredy created and connected");
            return;
        }
        LOGGER.log(Level.FINE, "SFTP client creating");

        String host = configuration.getHost();
        int port = configuration.getPort();
        int timeout = configuration.getTimeout() * 1000;
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Connecting to " + host + " [timeout: " + timeout + " ms]");
        }
        String username = configuration.getUserName();
        String password = configuration.getPassword();
        String knownHostsFile = configuration.getKnownHostsFile();
        String identityFile = configuration.getIdentityFile();
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Login as " + username);
        }

        JSch jsch = null;
        Channel channel = null;
        jsch = new JSch();
        try {
            if (io != null) {
                JSch.setLogger(new SftpLogger(io));
                LOGGER.log(Level.FINE, "Protocol command listener added");
            }
            sftpSession = jsch.getSession(username, host, port);
            if (PhpProjectUtils.hasText(knownHostsFile)) {
                jsch.setKnownHosts(knownHostsFile);
            }
            if (PhpProjectUtils.hasText(identityFile)) {
                jsch.addIdentity(identityFile);
            }
            if (PhpProjectUtils.hasText(password)) {
                sftpSession.setPassword(password);
            }
            sftpSession.setUserInfo(new SftpUserInfo(configuration.getDisplayName(), username));
            sftpSession.setTimeout(timeout);
            sftpSession.connect();

            channel = sftpSession.openChannel("sftp"); // NOI18N
            channel.connect();
            sftpClient = (ChannelSftp) channel;

        } catch (JSchException exc) {
            disconnect();
            LOGGER.log(Level.FINE, "Exception while connecting", exc);
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotConnect", configuration.getHost()), exc, getReplyString());
        }

    }

    public void connect() throws RemoteException {
        init();
        assert sftpClient.isConnected();
        try {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Remote server version is " + sftpClient.getServerVersion());
            }
        } catch (SftpException exc) {
            // can be ignored
            LOGGER.log(Level.FINE, "Exception while getting server version", exc);
        }
    }

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
    }

    /** not supported by JSCh */
    public String getReplyString() {
        return null;
    }

    /** not supported by JSCh */
    public String getNegativeReplyString() {
        return null;
    }

    public boolean isConnected() {
        if (sftpClient == null) {
            return false;
        }
        return sftpClient.isConnected();
    }

    public String printWorkingDirectory() throws RemoteException {
        try {
            return sftpClient.pwd();
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while pwd", ex);
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotPwd", configuration.getHost()), ex, getReplyString());
        }
    }

    public boolean storeFile(String remote, InputStream local) throws RemoteException {
        try {
            sftpClient.put(local, remote);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while storing file " + remote, ex);
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotStoreFile", remote), ex, getReplyString());
        }
    }

    public boolean deleteFile(String pathname) throws RemoteException {
        try {
            sftpClient.rm(pathname);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while deleting file " + pathname, ex);
            return false;
        }
    }

    public boolean rename(String from, String to) throws RemoteException {
        try {
            sftpClient.rename(from, to);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, String.format("Error while renaming file %s -> %s", from, to), ex);
            return false;
        }
    }

    public List<RemoteFile> listFiles(PathInfo pathInfo) throws RemoteException {
        List<RemoteFile> result = null;
        try {
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> files = sftpClient.ls(sftpClient.pwd());
            result = new ArrayList<RemoteFile>(files.size());
            for (ChannelSftp.LsEntry entry : files) {
                result.add(new RemoteFileImpl(entry));
            }
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while listing files for " + pathInfo, ex);
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotListFiles", pathInfo.getFullPath()), ex, getReplyString());
        }
        return result;
    }

    public boolean retrieveFile(String remote, OutputStream local) throws RemoteException {
        try {
            sftpClient.get(remote, local);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while retrieving file " + remote, ex);
            throw new RemoteException(NbBundle.getMessage(SftpClient.class, "MSG_CannotStoreFile", remote), ex, getReplyString());
        }
    }

    public boolean changeWorkingDirectory(String pathname) throws RemoteException {
        try {
            sftpClient.cd(pathname);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while changing directory " + pathname, ex);
            return false;
        }
    }

    public boolean makeDirectory(String pathname) throws RemoteException {
        try {
            sftpClient.mkdir(pathname);
            return true;
        } catch (SftpException ex) {
            LOGGER.log(Level.FINE, "Error while creating directory " + pathname, ex);
            return false;
        }
    }

    // XXX
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
//            if (event.isReply()
//                    && (FTPReply.isNegativeTransient(event.getReplyCode()) || FTPReply.isNegativePermanent(event.getReplyCode()))) {
//                io.getErr().print(message);
//                io.getErr().flush();
//            } else {
//                io.getOut().print(message);
//                io.getOut().flush();
//            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Command listener: " + message.trim());
            }
        }
    }

    private static final class RemoteFileImpl implements RemoteFile {
        private final ChannelSftp.LsEntry entry;

        public RemoteFileImpl(ChannelSftp.LsEntry entry) {
            assert entry != null;
            this.entry = entry;
        }

        public String getName() {
            return entry.getFilename();
        }

        public boolean isDirectory() {
            return entry.getAttrs().isDir();
        }

        public boolean isFile() {
            return !isDirectory();
        }

        public long getSize() {
            return entry.getAttrs().getSize();
        }
    }

    private static final class SftpLogger implements com.jcraft.jsch.Logger {
        private final InputOutput io;

        public SftpLogger(InputOutput io) {
            assert io != null;
            this.io = io;
        }

        public boolean isEnabled(int level) {
            return level >= com.jcraft.jsch.Logger.INFO;
        }

        public void log(int level, String message) {
            // XXX how to find out negative response?
            io.getOut().println(message);
            io.getOut().flush();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Command listener: " + message.trim());
            }
        }
    }

    /**
     * Because {@link java.nio.channels.ClosedByInterruptException} is raised while disconnecting SFTP session
     * (this exception makes oproblems to NB output window).
     * @see #disconnect()
     */
    private static final class DevNullLogger implements com.jcraft.jsch.Logger {

        public boolean isEnabled(int level) {
            return false;
        }

        public void log(int level, String message) {
        }
    }

    private static final class SftpUserInfo implements UserInfo, UIKeyboardInteractive {
        private final String configurationName;
        private final String userName;
        private String passwd;

        public SftpUserInfo(String configurationName, String userName) {
            assert configurationName != null;
            assert userName != null;

            this.configurationName = configurationName;
            this.userName = userName;
        }

        public String getPassword() {
            return passwd;
        }

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

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            PasswordPanel passwordPanel = new PasswordPanel(configurationName, userName);
            boolean ok = passwordPanel.open();
            if (ok) {
                passwd = passwordPanel.getPassword();
            }
            return ok;
        }

        public void showMessage(String message) {
            JOptionPane.showMessageDialog(null, message);
        }

        // taken from examples from JSCh library
        final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        private Container panel;

        public String[] promptKeyboardInteractive(String destination, String name, String instruction,
                String[] prompt, boolean[] echo) {

            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts = new JTextField[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]), gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if (echo[i]) {
                    texts[i] = new JTextField(20);
                } else {
                    texts[i] = new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }

            if (JOptionPane.showConfirmDialog(null, panel,
                    destination + ": " + name, // NOI18N
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                String[] response = new String[prompt.length];
                for (int i = 0; i < prompt.length; i++) {
                    response[i] = texts[i].getText();
                }
                return response;
            } else {
                return null;  // cancel
            }
        }
    }
}
