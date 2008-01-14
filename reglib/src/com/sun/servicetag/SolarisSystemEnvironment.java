/*
 * @(#)SolarisSystemEnvironment.java	1.1 08/01/11
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.servicetag;

// This class is equivalent to the com.sun.scn.servicetags.SolarisSystemEnvironment
// class in the Sun Connection source.
//
// The Service Tags team maintains the latest version of the implementation
// for system environment data collection.  JDK will include a copy of
// the most recent released version for a JDK release.  We rename
// the package to com.sun.servicetag so that the Sun Connection
// product always uses the latest version from the com.sun.scn.servicetags
// package. JDK and users of the com.sun.servicetag API
// (e.g. NetBeans and SunStudio) will use the version in JDK.
//
// So we keep this class in src/share/classes instead of src/<os>/classes.

import java.io.*;

/**
 * Solaris implementation of the SystemEnvironment class.
 *
 * JDK includes a copy of this class with the package renamed to 
 * "com.sun.servicetag".  Please add java-servicetag@sun.com in any
 * bug or RFE for this system environment data collection implementation.
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
                    if (!tmp.equalsIgnoreCase("not available")
                            && !tmp.equalsIgnoreCase("to be filled by o.e.m")) {
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
