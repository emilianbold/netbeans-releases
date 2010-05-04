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

import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;

/**
 * Class representing an FTP configuration.
 * @author Tomas Mysik
 * @see RemoteConfiguration
 * @see org.netbeans.modules.php.project.connections.RemoteConnections
 */
public final class FtpConfiguration extends RemoteConfiguration {

    private static final String PATH_SEPARATOR = "/"; // NOI18N

    private final String host;
    private final int port;
    private final String userName;
    private final String password;
    private final boolean anonymousLogin;
    private final String initialDirectory;
    private final int timeout;
    private final boolean passiveMode;

    public FtpConfiguration(final ConfigManager.Configuration cfg) {
        super(cfg);

        host = cfg.getValue(FtpConnectionProvider.HOST);
        port = Integer.parseInt(cfg.getValue(FtpConnectionProvider.PORT));
        userName = cfg.getValue(FtpConnectionProvider.USER);
        password = readPassword(cfg, FtpConnectionProvider.PASSWORD);
        anonymousLogin = Boolean.valueOf(cfg.getValue(FtpConnectionProvider.ANONYMOUS_LOGIN));
        initialDirectory = cfg.getValue(FtpConnectionProvider.INITIAL_DIRECTORY);
        timeout = Integer.parseInt(cfg.getValue(FtpConnectionProvider.TIMEOUT));
        passiveMode = Boolean.valueOf(cfg.getValue(FtpConnectionProvider.PASSIVE_MODE));
    }

    public boolean isAnonymousLogin() {
        return anonymousLogin;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String getInitialDirectory() {
        return initialDirectory;
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
        return password != null ? password : ""; // NOI18N
    }

    @Override
    public String getUrl(String directory) {
        assert directory != null;
        String path = initialDirectory;
        if (directory.trim().length() > 0) {
            path += directory;
        }
        return "ftp://" + host + path.replaceAll(PATH_SEPARATOR + "{2,}", PATH_SEPARATOR); // NOI18N
    }

    @Override
    public boolean saveProperty(String key, String value) {
        if (FtpConnectionProvider.PASSWORD.equals(key)) {
            // value cannot be used (is scrambled)
            savePassword(password, FtpConnectionProvider.get().getDisplayName());
            return true;
        }
        return false;
    }

    @Override
    public void notifyDeleted() {
        deletePassword();
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final FtpConfiguration other = (FtpConfiguration) obj;
        if (host != other.host && (host == null || !host.equals(other.host))) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        if (userName != other.userName && (userName == null || !userName.equals(other.userName))) {
            return false;
        }
        if (password != other.password && (password == null || !password.equals(other.password))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 97 * hash + (host != null ? host.hashCode() : 0);
        hash = 97 * hash + port;
        hash = 97 * hash + (userName != null ? userName.hashCode() : 0);
        hash = 97 * hash + (password != null ? password.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [displayName: "); // NOI18N
        sb.append(getDisplayName());
        sb.append(", name: "); // NOI18N
        sb.append(getName());
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
        sb.append(", timeout: "); // NOI18N
        sb.append(timeout);
        sb.append(", passiveMode: "); // NOI18N
        sb.append(passiveMode);
        sb.append("]"); // NOI18N
        return sb.toString();
    }
}
