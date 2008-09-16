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

package org.netbeans.modules.cnd.remote.support;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author gordonp
 */
public abstract class RemoteConnectionSupport {
    
    private JSch jsch;
    protected final String key;
    protected Session session;
    protected Channel channel;
    private final String user;
    private final String host;
    private int exit_status;
    private boolean cancelled = false;
    private boolean failed = false;
    private String failureReason;
    private Integer timeout = Integer.getInteger("cnd.remote.timeout"); // NOI18N
    protected static Logger log = Logger.getLogger("cnd.remote.logger"); // NOI18N
    
    public RemoteConnectionSupport(String key, int port) {
        this.key = key;
        int pos = key.indexOf('@');
        user = key.substring(0, pos);
        host = key.substring(pos + 1);
        exit_status = -1; // this is what JSch initializes it to...
        failureReason = "";
        boolean retry = false;
        log.finest("RCS<Init>: Starting " + getClass().getName() + " on " + key);
        
        do {
            try {
                jsch = new JSch();
                jsch.setKnownHosts(System.getProperty("user.home") + "/.ssh/known_hosts"); // NOI18N
                session = jsch.getSession(user, host, port);

                RemoteUserInfo ui = RemoteUserInfo.getUserInfo(key, retry);
                retry = false;
                session.setUserInfo(ui);
                session.connect(timeout == null ? 30000 : timeout.intValue());
                if (!session.isConnected()) {
                    log.fine("RCS<Init>: Connection failed on " + key);
                }
            } catch (JSchException jsce) {
                log.warning("RCS<Init>: Got JSchException [" + jsce.getMessage() + "]");
                String msg = jsce.getMessage();
                if (msg.equals("Auth cancel")) { // NOI18N
                    cancelled = true;
                } else if (msg.equals("Auth fail")) { // NOI18N
                    JButton btRetry = new JButton(NbBundle.getMessage(RemoteConnectionSupport.class, "BTN_Retry"));
                    NotifyDescriptor d = new NotifyDescriptor(
                            NbBundle.getMessage(RemoteConnectionSupport.class, "MSG_AuthFailedRetry"),
                            NbBundle.getMessage(RemoteConnectionSupport.class, "TITLE_AuthFailedRetryDialog"),
                            NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.QUESTION_MESSAGE,
                            new Object[] { btRetry, NotifyDescriptor.CANCEL_OPTION}, btRetry);
                    if (DialogDisplayer.getDefault().notify(d) == btRetry) {
                         retry = true;
                    } else {
                        failed = true;
                        failureReason = msg;
                    }
                } else {
                    failed = true;
                    failureReason = msg;
                }
            }
        } while (retry);
    }
    
    public Channel getChannel() {
        return channel;
    }
    
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    
    protected Channel createChannel(InputStream in, OutputStream out) throws JSchException {
        return (ChannelExec) session.openChannel("exec"); // NOI18N
    }
    
    protected Channel createChannel() throws JSchException {
        return createChannel(null, null);
    }
    
    public int getExitStatus() {
        return !cancelled && channel != null ? channel.getExitStatus() : -1; // JSch initializes exit status to -1
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public void setFailed(String reason) {
        failed = true;
        failureReason = reason;
    }
    
    public boolean isFailed() {
        return failed;
    }

    public boolean isFailedOrCancelled() {
        return failed || cancelled;
    }
    
    protected void setExitStatus(int exit_status) {
        this.exit_status = exit_status;
    }
    
    public void disconnect() {
        if (channel != null && channel.isConnected()) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
            session = null;
        }
    }
    
    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }
}
