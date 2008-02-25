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

package org.netbeans.modules.db.mysql.installations;

import org.netbeans.modules.db.mysql.Installation;
import org.netbeans.modules.db.mysql.Installation.Command;
import org.openide.util.Utilities;

/**
 * Defines the AMP stack distribution called "XAMPP" for Linux
 * See <a href="http://www.apachefriends.org/en/xampp-linux.html">
 * http://www.apachefriends.org/en/xampp-linux.html</a>
 * 
 * @author David Van Couvering
 */
public class LinuxDefaultInstallation implements Installation {
    private static final String DEFAULT_PORT = "3306";
    private static final String GKSU = "/usr/bin/gksu";
    private static final String SERVICE_CMD = "/etc/init.d/mysql";
        
    private static final LinuxDefaultInstallation DEFAULT = 
            new LinuxDefaultInstallation();
    
    public static final LinuxDefaultInstallation getDefault() {
        return DEFAULT;
    }
    
    private LinuxDefaultInstallation() {
    }

    public boolean isStackInstall() {
        return true;
    }

    public boolean isValidOnCurrentOS() {
        return Utilities.isUnix();
    }

    public String[] getAdminCommand() {
        return new String[] { "", "" };
    }

    public String[] getStartCommand() {
        String args = SERVICE_CMD + " start"; // NOI18N
        return new String[] { GKSU, args };
    }

    public String[] getStopCommand() {
        String args = SERVICE_CMD + " stop"; // NOI18N
        return new String[] { GKSU, args };
    }
    
    public String getDefaultPort() {
        return DEFAULT_PORT;
    }

    public Installation getInstallation(String command, Command cmdType) {
        return null; // not supported
    }

}
