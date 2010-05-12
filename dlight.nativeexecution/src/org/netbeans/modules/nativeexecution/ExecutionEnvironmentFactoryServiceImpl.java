/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.nativeexecution;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = org.netbeans.modules.nativeexecution.spi.ExecutionEnvironmentFactoryService.class, position = 100)
public class ExecutionEnvironmentFactoryServiceImpl implements ExecutionEnvironmentFactoryService {

    private static final ExecutionEnvironment LOCAL = new ExecutionEnvironmentImpl();
    public static final int DEFAULT_PORT = Integer.getInteger("cnd.remote.port", 22); //NOI18N

    /**
     * Returns an instance of <tt>ExecutionEnvironment</tt> for localexecution.
     */
    @Override
    public ExecutionEnvironment getLocal() {
        if (!HostInfoUtils.isHostInfoAvailable(LOCAL)) {
            getLocalHostInfo();
        }
        return LOCAL;
    }

    /**
     * Creates a new instance of <tt>ExecutionEnvironment</tt>. If <tt>host</tt>
     * refers to the localhost or is <tt>null</tt> then task, started in this
     * environment will be executed locally. Otherwise it will be executed
     * remotely using ssh connection to the specified host using default ssh
     * port (22).
     *
     * @param user user name to be used in this environment
     * @param host host identification string (either hostname or IP address)
     */
    @Override
    public ExecutionEnvironment createNew(String user, String host) {
        return createNew(user, host, DEFAULT_PORT);
    }

    /**
     * Creates a new instance of <tt>ExecutionEnvironment</tt>.
     * It is allowable to pass <tt>null</tt> values for <tt>user</tt> and/or
     * <tt>host</tt> params. In this case
     * <tt>System.getProperty("user.name")</tt> will be used as a username and
     * <tt>HostInfo.LOCALHOST</tt> will be used for <tt>host</tt>.
     * If sshPort == 0 and host identification string represents remote host,
     * port 22 will be used.
     *
     * @param user user name for ssh connection.
     * @param host host identification string. Either hostname or IP address.
     * @param sshPort port to be used to establish ssh connection.
     */
    @Override
    public ExecutionEnvironment createNew(String user, String host, int port) {
        return new ExecutionEnvironmentImpl(user, host, port);
    }

    /**
     * Returns a string representation of the executionEnvironment,
     * so that client can store it (for example, in properties)
     * and restore later via fromUniqueID
     * either user@host or "localhost"
     */
    @Override
    public String toUniqueID(ExecutionEnvironment executionEnvironment) {
        if (!(executionEnvironment instanceof ExecutionEnvironmentImpl)) {
            return null;
        }

        if (executionEnvironment.isLocal()) {
            // "localhost" is for compatibility with remote development 6.5
            return "localhost"; //NOI18N
        } else {
            String hostAndPort = executionEnvironment.getHost() + ':' + executionEnvironment.getSSHPort();
            if (executionEnvironment.getUser() == null || executionEnvironment.getUser().length() == 0) {
                return hostAndPort;
            } else {
                return executionEnvironment.getUser() + '@' + hostAndPort;
            }
        }
    }

    /**
     * Creates an instance of ExecutionEnvironment
     * by string that was got via toUniqueID() method
     * @param hostKey a string that was returned by toUniqueID() method.
     */
    @Override
    public ExecutionEnvironment fromUniqueID(String hostKey) {
        // TODO: remove this check and refactor clients to use getLocal() instead
        if ("localhost".equals(hostKey) || "127.0.0.1".equals(hostKey)) { //NOI18N
            return LOCAL;
        }

        String user;
        String host;
        int pos = hostKey.indexOf('@', 0); //NOI18N

        if (pos < 0) {
            user = "";
            host = hostKey;
        } else {
            user = hostKey.substring(0, pos);
            host = hostKey.substring(pos + 1);
        }
        int colonPos = host.indexOf(':');
        if (colonPos > 0) {
            String strPort = host.substring(colonPos + 1);
            host = host.substring(0, colonPos);
            try {
                int port = Integer.parseInt(strPort);
                return createNew(user, host, port);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return createNew(user, host);
            }
        }
        return createNew(user, host);
    }

    @Override
    public ExecutionEnvironment createNew(String schema) {
        return null;
    }

    private synchronized void getLocalHostInfo() {
        if (HostInfoUtils.isHostInfoAvailable(LOCAL)) {
            return;
        }

        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    HostInfoUtils.getHostInfo(LOCAL);
                } catch (IOException ex) {
                } catch (CancellationException ex) {
                }
            }
        };

        RequestProcessor.getDefault().post(task).waitFinished();
    }
}
