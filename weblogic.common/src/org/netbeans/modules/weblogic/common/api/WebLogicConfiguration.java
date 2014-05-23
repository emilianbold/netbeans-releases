/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.weblogic.common.api;

import java.io.File;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;

/**
 *
 * @author Petr Hejl
 */
public final class WebLogicConfiguration {

    public static final Version VERSION_10 = Version.fromJsr277NotationWithFallback("10"); // NOI18N

    private final File serverHome;

    private final String username;

    private final String password;

    private final File domainHome;

    private final String host;

    private final int port;

    // @GuardedBy("this")
    private WebLogicLayout layout;

    private WebLogicConfiguration(File serverHome, String username, String password,
            File domainHome, String host, int port) {
        this.serverHome = serverHome;
        this.username = username;
        this.password = password;
        this.domainHome = domainHome;
        this.host = host;
        this.port = port;
    }

    public static WebLogicConfiguration forLocalDomain(File serverHome, File domainHome,
            String username, String password) {
        // FIXME port
        return new WebLogicConfiguration(serverHome, username, password, domainHome, "localhost", 7001);
    }

    public static WebLogicConfiguration forRemoteDomain(File serverHome, String host, int port,
            String username, String password) {
        return new WebLogicConfiguration(serverHome, username, password, null, host, port);
    }

    public boolean isRemote() {
        return domainHome == null;
    }

    @NonNull
    public File getServerHome() {
        return serverHome;
    }

    @NullUnknown
    public File getDomainHome() {
        return domainHome;
    }

    @NonNull
    public String getAdminURL() {
        // XXX
        return "t3://" + host + ":" + port;
    }

    @NonNull
    public String getSiteURL() {
        return null;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @NonNull
    public synchronized WebLogicLayout getLayout() {
        if (layout == null) {
            layout = new WebLogicLayout(this);
        }
        return layout;
    }
}
