/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.servicetag;

// This class is a copy of the com.sun.scn.servicetags.SolarisSystemEnvironment
// class from the Sun Connection source.
//
// The Service Tags team maintains the latest version of the implementation
// for system environment data collection.  JDK will include a copy of
// the most recent released version for a JDK release.	We rename
// the package to com.sun.servicetag so that the Sun Connection
// product always uses the latest version from the com.sun.scn.servicetags
// package. JDK and users of the com.sun.servicetag API
// (e.g. NetBeans and SunStudio) will use the version in JDK.
//
// So we keep this class in src/share/classes instead of src/<os>/classes.

import java.io.*;

/**
 * Solaris implementation of the SystemEnvironment class.
 */
class SolarisSystemEnvironment extends SystemEnvironment {
    SolarisSystemEnvironment() {
        setHostId(getCommandOutput("/usr/bin/hostid"));
        setSystemModel(getCommandOutput("/usr/bin/uname", "-i"));
        setSystemManufacturer(getSolarisSystemManufacturer());
        setCpuManufacturer(getSolarisCpuManufacturer());
        setSerialNumber(getSolarisSN());
    }

    /**
     * Tries to obtain the cpu manufacturer.
     * @return The cpu manufacturer (an empty string if not found or an error occurred)
     */
    private String getSolarisCpuManufacturer() {
        // not fully accurate, this could be another manufacturer (fujitsu for example)
        if ("sparc".equalsIgnoreCase(System.getProperty("os.arch"))) {
            return "Sun Microsystems, Inc";
        }

        // if we're here, then we'll try smbios (type 3)
        return getSmbiosData("3", "Manufacturer: ");
    }

    /**
     * Tries to obtain the system manufacturer.
     * @return The system manufacturer (an empty string if not found or an error occurred)
     */
    private String getSolarisSystemManufacturer() {
        // not fully accurate, this could be another manufacturer (fujitsu for example)
        if ("sparc".equalsIgnoreCase(System.getProperty("os.arch"))) {
            return "Sun Microsystems, Inc";
        }

        // if we're here, then we'll try smbios (type 1)
        return getSmbiosData("1", "Manufacturer: ");
    }

    /**
     * Tries to obtain the serial number.
     * @return The serial number (empty string if not found or an error occurred)
     */
    private String getSolarisSN() {
        // try to read from the psn file if it exists
        String tmp = getFileContent("/var/run/psn");
        if (tmp.length() > 0) {
            return tmp.trim();
        }

        // if we're here, then we'll try sneep
        String tmpSN = getSneepSN();
        if (tmpSN.length() > 0) {
            return tmpSN;
        }

        // if we're here, then we'll try smbios (type 1)
        tmpSN = getSmbiosData("1", "Serial Number: ");
        if (tmpSN.length() > 0) {
            return tmpSN;
        }

        // if we're here, then we'll try smbios (type 3)
        tmpSN = getSmbiosData("3", "Serial Number: ");
        if (tmpSN.length() > 0) {
            return tmpSN;
        }

        // give up and return
        return "";
    }

    // Sample smbios output segment:
    // ID    SIZE TYPE
    // 1     150  SMB_TYPE_SYSTEM (system information)
    //
    //   Manufacturer: Sun Microsystems
    //   Product: Sun Fire X4600
    //   Version: To Be Filled By O.E.M.
    //   Serial Number: 00:14:4F:45:0C:2A
    private String getSmbiosData(String type, String target) {
        String output = getCommandOutput("/usr/sbin/smbios", "-t", type);
        for (String s : output.split("\n")) {
            if (s.contains(target)) {
                int indx = s.indexOf(target) + target.length();
                if (indx < s.length()) {
                    String tmp = s.substring(indx).trim();
                    String lowerCaseStr = tmp.toLowerCase();
                    if (!lowerCaseStr.startsWith("not available")
                            && !lowerCaseStr.startsWith("to be filled by o.e.m")) {
                        return tmp;
                    }
                }
            }
        }

        return "";
    }

    private String getSneepSN() {
        String basedir = getCommandOutput("pkgparam","SUNWsneep","BASEDIR");
        File f = new File(basedir + "/bin/sneep");
        if (f.exists()) {
            String sneepSN = getCommandOutput(basedir + "/bin/sneep");
            if (sneepSN.equalsIgnoreCase("unknown")) {
                return "";
            } else {
                return sneepSN;
            }
        } else {
            return "";
        }
    }

}
