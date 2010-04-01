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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.project.connections.ConfigManager;
import org.netbeans.modules.php.project.connections.spi.RemoteClient;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.connections.spi.RemoteConfigurationPanel;
import org.netbeans.modules.php.project.connections.spi.RemoteConnectionProvider;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public final class FtpConnectionProvider implements RemoteConnectionProvider {
    private static final String FTP_CONNECTION_TYPE = "FTP"; // NOI18N

    static final String TYPE = "type"; // NOI18N
    static final String HOST = "host"; // NOI18N
    static final String PORT = "port"; // NOI18N
    static final String USER = "user"; // NOI18N
    static final String PASSWORD = "password"; // NOI18N
    static final String ANONYMOUS_LOGIN = "anonymousLogin"; // NOI18N
    static final String INITIAL_DIRECTORY = "initialDirectory"; // NOI18N
    static final String TIMEOUT = "timeout"; // NOI18N
    static final String PASSIVE_MODE = "passiveMode"; // NOI18N

    private static final Set<String> PROPERTIES = new HashSet<String>(Arrays.asList(
        TYPE,
        HOST,
        PORT,
        USER,
        PASSWORD,
        ANONYMOUS_LOGIN,
        INITIAL_DIRECTORY,
        TIMEOUT,
        PASSIVE_MODE
    ));
    private static final int DEFAULT_PORT = 21;
    private static final int DEFAULT_TIMEOUT = 30;
    private static final String DEFAULT_INITIAL_DIRECTORY = "/"; // NOI18N

    private FtpConnectionProvider() {
    }

    public static FtpConnectionProvider get() {
        return new FtpConnectionProvider();
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(FtpConnectionProvider.class, "LBL_Ftp");
    }

    @Override
    public Set<String> getPropertyNames() {
        return PROPERTIES;
    }

    @Override
    public RemoteConfiguration createRemoteConfiguration(ConfigManager.Configuration configuration) {
        configuration.putValue(TYPE, FTP_CONNECTION_TYPE);
        configuration.putValue(HOST, ""); // NOI18N
        configuration.putValue(PORT, String.valueOf(DEFAULT_PORT));
        configuration.putValue(USER, ""); // NOI18N
        configuration.putValue(PASSWORD, ""); // NOI18N
        configuration.putValue(ANONYMOUS_LOGIN, String.valueOf(false));
        configuration.putValue(INITIAL_DIRECTORY, DEFAULT_INITIAL_DIRECTORY);
        configuration.putValue(TIMEOUT, String.valueOf(DEFAULT_TIMEOUT));
        configuration.putValue(PASSIVE_MODE, String.valueOf(false));

        assert accept(configuration) : "Not my configuration?!";

        return new FtpConfiguration(configuration);
    }

    @Override
    public RemoteConfiguration getRemoteConfiguration(ConfigManager.Configuration configuration) {
        if (accept(configuration)) {
            return new FtpConfiguration(configuration);
        }
        return null;
    }

    @Override
    public RemoteClient getRemoteClient(RemoteConfiguration remoteConfiguration, InputOutput io) {
        if (remoteConfiguration instanceof FtpConfiguration) {
            return new FtpClient((FtpConfiguration) remoteConfiguration, io);
        }
        return null;
    }

    @Override
    public RemoteConfigurationPanel getRemoteConfigurationPanel(ConfigManager.Configuration configuration) {
        if (accept(configuration)) {
            return new FtpConfigurationPanel();
        }
        return null;
    }

    private boolean accept(ConfigManager.Configuration configuration) {
        String type = configuration.getValue(TYPE);
        return FTP_CONNECTION_TYPE.equals(type);
    }
}
