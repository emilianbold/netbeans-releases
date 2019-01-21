/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.installations;

import org.netbeans.modules.db.mysql.impl.Installation;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.util.Utilities;

/**
 * Defines the AMP stack distribution called "XAMPP" for Linux
 * See <a href="http://www.apachefriends.org/en/xampp-linux.html">
 * http://www.apachefriends.org/en/xampp-linux.html</a>
 * 
 */
public class WindowsXAMPPInstallation implements Installation {
    private static final String DEFAULT_BASE_PATH = "C:/xampp"; // NOI18N
    private static final String START_PATH="/mysql_start.bat";
    private static final String STOP_PATH="/mysql_stop.bat";
    private static final String ADMIN_URL = "http://localhost/phpmyadmin";
    private static final String DEFAULT_PORT = "3306";
    
    private String basePath = DEFAULT_BASE_PATH;
    
    private static final WindowsXAMPPInstallation DEFAULT = 
            new WindowsXAMPPInstallation(DEFAULT_BASE_PATH);
    
    public static final WindowsXAMPPInstallation getDefault() {
        return DEFAULT;
    }
    
    private WindowsXAMPPInstallation(String basePath) {
        this.basePath = basePath;
    }

    public boolean isStackInstall() {
        return true;
    }

    public boolean isInstalled() {
        return Utilities.isWindows() &&
                Utils.isValidExecutable(getStartCommand()[0]);
    }

    public String[] getAdminCommand() {
        return new String[] { ADMIN_URL, "" };
    }

    public String[] getStartCommand() {
        String command = basePath + START_PATH; // NOI18N
        return new String[] { command, "" };
    }

    public String[] getStopCommand() {
        String command = basePath + STOP_PATH; // NOI18N
        return new String[] { command, "" };
    }
    
    public String getDefaultPort() {
        return DEFAULT_PORT;
    }
    
    @Override
    public String toString() {
        return "XAMPP Installation - " + basePath; //NOI18N
    }
}
