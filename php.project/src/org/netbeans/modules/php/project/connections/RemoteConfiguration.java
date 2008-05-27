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
    private final boolean anonymousLogin;
    private final String initialDirectory;
    private final int timeout;

    public RemoteConfiguration(String displayName, String name, ConnectionType connectionType, String host, int port, String userName,
            boolean anonymousLogin, String initialDirectory, int timeout) {
        this.displayName = displayName;
        this.name = name;
        this.connectionType = connectionType;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.anonymousLogin = anonymousLogin;
        this.initialDirectory = initialDirectory;
        this.timeout = timeout;
    }

    RemoteConfiguration(final ConfigManager.Configuration cfg) {
        displayName = cfg.getDisplayName();
        name = cfg.getName();
        connectionType = ConnectionType.valueOf(cfg.getValue(RemoteConnections.TYPE));
        host = cfg.getValue(RemoteConnections.HOST);
        port = Integer.parseInt(cfg.getValue(RemoteConnections.PORT));
        userName = cfg.getValue(RemoteConnections.USER);
        anonymousLogin = Boolean.valueOf(cfg.getValue(RemoteConnections.ANONYMOUS_LOGIN));
        initialDirectory = cfg.getValue(RemoteConnections.INITIAL_DIRECTORY);
        timeout = Integer.parseInt(cfg.getValue(RemoteConnections.TIMEOUT));
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

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public String getUserName() {
        return userName;
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
        sb.append(userName);
        sb.append(", anonymousLogin: "); // NOI18N
        sb.append(anonymousLogin);
        sb.append(", initialDirectory: "); // NOI18N
        sb.append(initialDirectory);
        sb.append(", timeout: "); // NOI18N
        sb.append(timeout);
        sb.append("]"); // NOI18N
        return sb.toString();
    }
}
