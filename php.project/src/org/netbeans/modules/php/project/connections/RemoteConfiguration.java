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

import org.netbeans.modules.php.project.connections.RemoteConnections.ConnectionType;

/**
 * Class representing a remote configuration.
 * @author Tomas Mysik
 * @see RemoteConnections
 * @see RemoteConnections#getRemoteConfigurations()
 */
public final class RemoteConfiguration {
    private final String displayName;
    private final String name;
    private final ConnectionType connectionType;
    private final String host;
    private final int port;
    private final String userName;
    private final String password;
    private final boolean anonymousLogin;
    private final String initialDirectory;
    private final String pathSeparator;
    private final int timeout;
    private final boolean passiveMode;

    /**
     * Constructor suitable for some "well-known" connections, e.g. if one wants
     * some remote configuration and then needs to compare whether it is selected e.g.
     * @param displayName the display name of the configuration.
     */
    public RemoteConfiguration(String displayName) {
        this(displayName, "", ConnectionType.FTP, "", 1, "", "", false, "", "/", 0, false); // NOI18N
    }

    public RemoteConfiguration(String displayName, String name, ConnectionType connectionType, String host, int port, String userName,
            String password, boolean anonymousLogin, String initialDirectory, String pathSeparator, int timeout, boolean passiveMode) {
        assert displayName != null;
        assert name != null;
        assert connectionType != null;
        assert host != null;
        assert port > 0;
        assert userName != null;
        assert password != null;
        assert initialDirectory != null;
        assert pathSeparator != null;
        assert timeout >= 0;

        if (initialDirectory.trim().length() == 0) {
            initialDirectory = pathSeparator;
        }

        this.displayName = displayName;
        this.name = name;
        this.connectionType = connectionType;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.anonymousLogin = anonymousLogin;
        this.initialDirectory = initialDirectory;
        this.pathSeparator = pathSeparator;
        this.timeout = timeout;
        this.passiveMode = passiveMode;
    }

    RemoteConfiguration(final ConfigManager.Configuration cfg) {
        assert cfg.getName() != null;

        displayName = cfg.getDisplayName();
        name = cfg.getName();
        connectionType = ConnectionType.valueOf(cfg.getValue(RemoteConnections.TYPE));
        host = cfg.getValue(RemoteConnections.HOST);
        port = Integer.parseInt(cfg.getValue(RemoteConnections.PORT));
        userName = cfg.getValue(RemoteConnections.USER);
        password = cfg.getValue(RemoteConnections.PASSWORD, true);
        anonymousLogin = Boolean.valueOf(cfg.getValue(RemoteConnections.ANONYMOUS_LOGIN));
        initialDirectory = cfg.getValue(RemoteConnections.INITIAL_DIRECTORY);
        pathSeparator = cfg.getValue(RemoteConnections.PATH_SEPARATOR);
        timeout = Integer.parseInt(cfg.getValue(RemoteConnections.TIMEOUT));
        passiveMode = Boolean.valueOf(cfg.getValue(RemoteConnections.PASSIVE_MODE));
    }

    public boolean isAnonymousLogin() {
        return anonymousLogin;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHost() {
        return host;
    }

    public String getInitialDirectory() {
        return initialDirectory;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isPassiveMode() {
        return passiveMode;
    }

    /**
     * Get the user name or "anonymous" if the configuration uses anonymous login.
     * @return the user name or "anonymous".
     */
    public String getUserName() {
        if (anonymousLogin) {
            return "anonymous"; // NOI18N
        }
        return userName;
    }

    /**
     * Get the password or "nobody@nowhere.net" if the configuration uses anonymous login.
     * @return the password or "nobody@nowhere.net".
     */
    public String getPassword() {
        if (anonymousLogin) {
            return "nobody@nowhere.net"; // NOI18N
        }
        return password != null ? password : "";//NOI18N
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [displayName: "); // NOI18N
        sb.append(displayName);
        sb.append(", name: "); // NOI18N
        sb.append(name);
        sb.append(", connectionType: "); // NOI18N
        sb.append(connectionType);
        sb.append(", host: "); // NOI18N
        sb.append(host);
        sb.append(", port: "); // NOI18N
        sb.append(port);
        sb.append(", userName: "); // NOI18N
        sb.append(getUserName());
        sb.append(", password: *****"); // NOI18N
        sb.append(", anonymousLogin: "); // NOI18N
        sb.append(anonymousLogin);
        sb.append(", initialDirectory: "); // NOI18N
        sb.append(initialDirectory);
        sb.append(", pathSeparator: "); // NOI18N
        sb.append(pathSeparator);
        sb.append(", timeout: "); // NOI18N
        sb.append(timeout);
        sb.append(", passiveMode: "); // NOI18N
        sb.append(passiveMode);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteConfiguration other = (RemoteConfiguration) obj;
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        if (this.host != other.host && (this.host == null || !this.host.equals(other.host))) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        if (this.userName != other.userName && (this.userName == null || !this.userName.equals(other.userName))) {
            return false;
        }
        if (this.password != other.password && (this.password == null || !this.password.equals(other.password))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.host != null ? this.host.hashCode() : 0);
        hash = 97 * hash + this.port;
        hash = 97 * hash + (this.userName != null ? this.userName.hashCode() : 0);
        hash = 97 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }
}
