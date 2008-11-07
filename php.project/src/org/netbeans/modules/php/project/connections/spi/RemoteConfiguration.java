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

package org.netbeans.modules.php.project.connections.spi;

import org.netbeans.modules.php.project.connections.ConfigManager;

/**
 * Class representing a remote configuration.
 * @author Tomas Mysik
 * @see org.netbeans.modules.php.project.connections.RemoteConnections
 * @see org.netbeans.modules.php.project.connections.RemoteConnections#getRemoteConfigurations()
 */
public abstract class RemoteConfiguration {
    private final String displayName;
    private final String name;

    public RemoteConfiguration(final ConfigManager.Configuration cfg) {
        assert cfg.getName() != null;
        assert cfg.getDisplayName() != null;

        name = cfg.getName();
        displayName = cfg.getDisplayName();
    }

    protected RemoteConfiguration(String name, String displayName) {
        assert name != null;
        assert displayName != null;

        this.name = name;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getName() {
        return name;
    }

    /**
     * Get the URL for the remote configuration, suitable e.g. for some hints etc.
     * @return configuration URL.
     */
    public String getUrl() {
        return getUrl(""); // NOI18N
    }

    /**
     * Get the URL for the remote configuration, suitable e.g. for some hints etc.
     * @param directory directory for which the URL is created (directory is usually appended to the URL).
     * @return configuration URL.
     * @see #getUrl()
     */
    public abstract String getUrl(String directory);

    /**
     * Get the initial remote directory on a server for this configuration.
     * It would be typically '/' for FTP.
     * @return the initial directory, never <code>null</code>.
     */
    public abstract String getInitialDirectory();

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteConfiguration other = (RemoteConfiguration) obj;
        if (name != other.name && (name == null || !name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (name != null ? name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(getClass().getName());
        sb.append(" [displayName: "); // NOI18N
        sb.append(displayName);
        sb.append(", name: "); // NOI18N
        sb.append(name);
        sb.append("]"); // NOI18N
        return sb.toString();
    }

    /**
     * Dummy implementation of {@link RemoteConfiguration}.
     */
    public static final class Empty extends RemoteConfiguration {
        public Empty(String name, String displayName) {
            super(name, displayName);
        }

        public Empty(final ConfigManager.Configuration cfg) {
            super(cfg.getName(), cfg.getDisplayName());
        }

        @Override
        public String getUrl(String directory) {
            return ""; // NOI18N
        }

        @Override
        public String getInitialDirectory() {
            return "/";
        }
    }
}
