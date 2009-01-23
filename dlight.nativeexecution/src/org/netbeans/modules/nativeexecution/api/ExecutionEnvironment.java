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
package org.netbeans.modules.nativeexecution.api;

import org.netbeans.modules.nativeexecution.util.HostInfo;
import org.netbeans.modules.nativeexecution.util.HostNotConnectedException;

/**
 * Configuration of environment for NativeTasks execution.
 */
final public class ExecutionEnvironment {
    
    private final String user;
    private final String host;
    private final int sshPort;
    private final String toString;

    /**
     * Creates new instance of <tt>ExecutionEnvironment</tt> for local execution.
     */
    public ExecutionEnvironment() {
        this(null, null);
    }

    /**
     * Creates new instance of <tt>ExecutionEnvironment</tt>.
     * @param user user name to be used in this environment
     * @param host host identification string (either hostname or IP address)
     */
    public ExecutionEnvironment(String user, String host) {
        this(user, host, 0);
    }

    /**
     * Creates new instance of <tt>ExecutionEnvironment</tt>.
     * It is allowable to pass <tt>null</tt> values for <tt>user</tt> and/or
     * <tt>host</tt> params. In this case
     * <tt>System.getProperty("user.name")</tt> will be used as username and
     * <tt>HostInfo.LOCALHOST</tt> will be used for <tt>host</tt>.
     * If sshPort == 0 and host identification string represents remote host,
     * port 22 will be used.
     *
     * @param user user name for ssh connection
     * @param host host identification string. Either hostname or IP address
     * @param sshPort port to be used to establish ssh connection.
     */
    public ExecutionEnvironment(final String user, final String host, final int sshPort) {
        if (user == null) {
            this.user = System.getProperty("user.name"); // NOI18N
        } else {
            this.user = user;
        }

        if (host == null) {
            this.host = HostInfo.LOCALHOST;
        } else {
            this.host = host;
        }

        if (!isLocalhost() && sshPort == 0) {
            this.sshPort = 22;
        } else {
            this.sshPort = sshPort;
        }

        toString = this.user + "@" + this.host +
                (this.sshPort == 0 ? "" : ":" + this.sshPort);
    }

    /**
     * Returns host identification string.
     * @return the same host name/ip string that was used for this
     * <tt>ExecutionEnvironment</tt> creation.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns string representation of this <tt>ExecutionEnvironment</tt>.
     * @return string representation of this <tt>ExecutionEnvironment</tt> in
     *         form user@host[:port]
     */
    @Override
    public String toString() {
        return toString;
    }

    /**
     * Returns username that is used for ssh connection.
     * @return username for ssh connection establishment.
     */
    public String getUser() {
        return user;
    }

    /**
     * Returns port number that is used for ssh connection.
     * @return port that is used for ssh connection in this environment. 0 means
     *         that no ssh connection is required for this environment.
     */
    public int getSSHPort() {
        return sshPort;
    }

    /**
     * Returns true if ssh connection is required for this environment.
     *
     * So, generally, this means that host itself could be a localhost, but if
     * sshPort is set, it will be treated as a remote one.
     *
     * @return true if ssh connection is required for this environment.
     * @see #isLocal()
     *
     */
    public boolean isRemote() {
        return !isLocal();
    }

    /**
     * Opposite to <tt>isRemote()</tt>.
     * @return true if no ssh connection required for this environment.
     * @see #isRemote() 
     */
    public boolean isLocal() {
        return sshPort == 0;
    }

    /**
     * Returns true if <tt>obj</tt> represents the same
     * <tt>ExecutionEnvironment</tt>. Two execution environments are equal if 
     * and only if <tt>host</tt>, <tt>user</tt> and <tt>sshPort</tt> are all
     * equal.
     *
     * @param obj object to compare with
     * @return <tt>true</tt> if this <tt>ExecutionEnvironment</tt> equals to <tt>obj</tt>
     * or not.
     */
    @Override
    public boolean equals(Object obj) {
        ExecutionEnvironment ee = null;

        if (obj != null && obj instanceof ExecutionEnvironment) {
            ee = (ExecutionEnvironment) obj;
        } else {
            return false;
        }

        boolean result = ((ee.isLocalhost() && isLocalhost()) ||
                ee.host.equals(host)) &&
                ee.user.equals(user) &&
                ee.sshPort == sshPort;

        return result;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.user != null ? this.user.hashCode() : 0);
        hash = 97 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 97 * hash + this.sshPort;
        return hash;
    }

    public final boolean isUnix() throws HostNotConnectedException {
        return HostInfo.isUnix(this);
}

    public final boolean isLocalhost() {
        return HostInfo.isLocalhost(host);
    }

    public String getOS() throws HostNotConnectedException {
        return HostInfo.getOS(this);
    }

    public String getPlatformPath() {
        return HostInfo.getPlatformPath(this);
    }

}
