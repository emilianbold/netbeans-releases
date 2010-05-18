/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.connection.AbstractConnection;
import org.netbeans.lib.cvsclient.connection.ConnectionModifier;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.util.LoggedDataInputStream;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

import javax.net.SocketFactory;
import java.io.*;
import java.util.*;
import java.net.Socket;
import java.net.UnknownHostException;

import com.jcraft.jsch.*;
import org.openide.util.NbBundle;

/**
 * Provides SSH tunnel for :ext: connection method. 
 * 
 * @author Maros Sandor
 */
public class SSHConnection extends AbstractConnection {

    private static final String CVS_SERVER_COMMAND = System.getenv("CVS_SERVER") != null?
        System.getenv("CVS_SERVER") + " server": "cvs server";  // NOI18N

    private final SocketFactory socketFactory;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    
    private Session session;
    private ChannelExec channel;

    /**
     * Creates new SSH connection object.
     * 
     * @param socketFactory socket factory to use when connecting to SSH server
     * @param host host names of the SSH server
     * @param port port number of SSH server
     * @param username SSH username
     * @param password SSH password
     */ 
    public SSHConnection(SocketFactory socketFactory, String host, int port, String username, String password) {
        this.socketFactory = socketFactory;
        this.host = host;
        this.port = port;
        this.username = username != null ? username : System.getProperty("user.name"); // NOI18N
        this.password = password;
    }

    public void open() throws AuthenticationException, CommandAbortedException {

        Properties props = new Properties();
        props.put("StrictHostKeyChecking", "no"); // NOI18N
        
        JSch jsch = new JSch();
        try {
            session = jsch.getSession(username, host, port);
            session.setSocketFactory(new SocketFactoryBridge());
            session.setConfig(props);
            session.setUserInfo(new SSHUserInfo());
            session.connect();
        } catch (JSchException e) {
            throw new AuthenticationException(e, NbBundle.getMessage(SSHConnection.class, "BK3001"));
        }
        
        try {
            channel = (ChannelExec) session.openChannel("exec"); // NOI18N
            channel.setCommand(CVS_SERVER_COMMAND);
            setInputStream(new LoggedDataInputStream(new SshChannelInputStream(channel)));
            setOutputStream(new LoggedDataOutputStream(channel.getOutputStream()));
            channel.connect();
        } catch (JSchException e) {
            IOException ioe = new IOException(NbBundle.getMessage(SSHConnection.class, "BK3002"));
            ioe.initCause(e);
            throw new AuthenticationException(ioe, NbBundle.getMessage(SSHConnection.class, "BK3003"));
        } catch (IOException e) {
            throw new AuthenticationException(e, NbBundle.getMessage(SSHConnection.class, "BK3003"));
        }
    }

    /**
     * Verifies that we can successfuly connect to the SSH server and run 'cvs server' command on it.
     * 
     * @throws AuthenticationException if connection to the SSH server cannot be established (network problem)
     */ 
    public void verify() throws AuthenticationException {
        try {
            open();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignore
            }
            if (channel.getExitStatus() != -1) {
                throw new AuthenticationException(CVS_SERVER_COMMAND, NbBundle.getMessage(SSHConnection.class, "BK3004", CVS_SERVER_COMMAND));
            }
            close();
        } catch (CommandAbortedException e) {
            throw new AuthenticationException(e, NbBundle.getMessage(SSHConnection.class, "BK3005"));
        } catch (IOException e) {
            throw new AuthenticationException(e, NbBundle.getMessage(SSHConnection.class, "Bk3006"));
        } finally {
            reset();
        }
    }

    private void reset() {
        session = null;
        channel = null;
        setInputStream(null);
        setOutputStream(null);
    }
    
    public void close() throws IOException {
        if (channel != null) channel.disconnect();
        if (session != null) session.disconnect();
        reset();
    }

    public boolean isOpen() {
        return channel != null && channel.isConnected();
    }

    public int getPort() {
        return port;
    }

    public void modifyInputStream(ConnectionModifier modifier) throws IOException {
        modifier.modifyInputStream(getInputStream());
    }

    public void modifyOutputStream(ConnectionModifier modifier) throws IOException {
        modifier.modifyOutputStream(getOutputStream());
    }

    /**
     * Provides JSch with SSH password.
     */ 
    private class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
        public String getPassphrase() {
            return null;
        }

        public String getPassword() {
            return password;
        }

        public boolean promptPassword(String message) {
            return true;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptYesNo(String message) {
            return false;
        }

        public void showMessage(String message) {
        }

        public String[] promptKeyboardInteractive(String destination,
                                                  String name,
                                                  String instruction,
                                                  String[] prompt,
                                                  boolean[] echo){
          String[] response=new String[prompt.length];
          if(prompt.length==1){
            response[0]=password;
          }
          return response;                                                
        }
    }
    
    /**
     * Bridges com.jcraft.jsch.SocketFactory and javax.net.SocketFactory. 
     */ 
    private class SocketFactoryBridge implements com.jcraft.jsch.SocketFactory {

        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return socketFactory.createSocket(host, port);
        }

        public InputStream getInputStream(Socket socket) throws IOException {
            return socket.getInputStream();
        }

        public OutputStream getOutputStream(Socket socket) throws IOException {
            return socket.getOutputStream();
        }
    }
    
    private static class SshChannelInputStream extends FilterInputStream {
        
        private final Channel channel;

        public SshChannelInputStream(Channel channel) throws IOException {
            super(channel.getInputStream());
            this.channel = channel;
        }

        public int available() throws IOException {
            checkChannelState();
            return super.available();
        }

        private void checkChannelState() throws IOException {
            int exitStatus = channel.getExitStatus();
            if (exitStatus > 0 || exitStatus < -1) throw new IOException(NbBundle.getMessage(SSHConnection.class, "BK3004", CVS_SERVER_COMMAND));
            if (exitStatus == 0 || channel.isEOF()) throw new EOFException(NbBundle.getMessage(SSHConnection.class, "BK3007"));
        }
    }
}
