/*
 * @(#)LinuxSystemEnvironment.java	1.1 08/01/11
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.servicetag;

// This class is equivalent to the com.sun.scn.servicetags.LinuxSystemEnvironment
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
 * Linux implementation of the SystemEnvironment class.
 *
 * JDK includes a copy of this class with the package renamed to 
 * "com.sun.servicetag".  Please add java-servicetag@sun.com in any
 * bug or RFE for this system environment data collection implementation.
 */
class LinuxSystemEnvironment extends SystemEnvironment {
    LinuxSystemEnvironment() {
        setHostId(getLinuxHostId());
        setSystemModel(getCommandOutput("/usr/bin/uname", "-i"));
        setSystemManufacturer(getLinuxSystemManufacturer());
        setCpuManufacturer(getLinuxCpuManufacturer());
        setSerialNumber(getLinuxSN());
    }

    private static final int SN	 = 1;
    private static final int SYS = 2;
    private static final int CPU = 3;

    private String getLinuxHostId() {
        String output = getCommandOutput("/usr/bin/hostid");
        // trim off the leading 0x
        if (output.startsWith("0x")) {
            output = output.substring(2);
        }
        return output;
    }

    /**
     * Tries to obtain and return the cpu manufacturer.
     * @return The cpu manufacturer (an empty string if not found or an error occurred)
     */
    private String getLinuxCpuManufacturer() {
        String tmp = getLinuxPSNInfo(CPU);
        if (tmp.length() > 0) {
            return tmp;
        }

        String contents = getFileContent("/proc/cpuinfo");
        for (String l : contents.split("\n")) {
            if (l.contains("vendor_id")) {
                String[] ss = l.split(":", 2);
                if (ss.length > 1) {
                    return ss[1].trim();
                }
            } 
        }

        // returns an empty string if it can't be found or an error happened
        return getLinuxDMIInfo("dmi type 4", "manufacturer");
    }


    /**
     * Tries to obtain and return the system manufacturer.
     * @return The system manufacturer (an empty string if not found or an error occurred)
     */
    private String getLinuxSystemManufacturer() {
        String tmp = getLinuxPSNInfo(SYS);
        if (tmp.length() > 0) {
            return tmp;
        }

        // returns an empty string if it can't be found or an error happened
        return getLinuxDMIInfo("dmi type 1", "manufacturer");
    }

    /**
     * Tries to obtain and return the serial number of the system.
     * @return The serial number (an empty string if not found or an error occurred)
     */
    private String getLinuxSN() {
        String tmp = getLinuxPSNInfo(SN);
        if (tmp.length() > 0) {
            return tmp;
        }

        // returns an empty string if it can't be found or an error happened
        return getLinuxDMIInfo("dmi type 1", "serial number");
    }

    private String getLinuxPSNInfo(int target) {
        // try to read from the psn file if it exists
        String contents = getFileContent("/var/run/psn");
        String[] ss = contents.split("\n");
        if (target <= ss.length) {
            return ss[target-1];
        }

        // default case is to return ""
        return "";
    }

    // reads from dmidecode with the given type and target
    // returns an empty string if nothing was found or an error occurred
    // 
    // Sample output segment:
    // Handle 0x0001
    //         DMI type 1, 25 bytes.
    //         System Information
    //                 Manufacturer: System manufacturer
    //                 Product Name: System Product Name
    //                 Version: System Version
    //                 Serial Number: System Serial Number
    //                 UUID: 3091D719-B25B-D911-959D-6D1B12C7686E
    //                 Wake-up Type: Power Switch

    private String getLinuxDMIInfo(String dmiType, String target) {
        String output = getCommandOutput("/usr/sbin/dmidecode");
        if (output.length() == 0) {
            return output;
        }
        boolean dmiFlag = false;
        for (String s : output.split("\n")) {
            String line = s.toLowerCase();
            if (dmiFlag) {
                if (line.contains(target)) {
                    String key = target + ":";
                    int indx = line.indexOf(key) + key.length();
                    if (line.contains(key) && indx < line.length()) {
                        return line.substring(indx).trim();
                    }
                    String[] ss = line.split(":"); 
                    return ss[ss.length-1];
                }
            } else if (line.contains(dmiType)) {
                dmiFlag = true;
            }
        }
        return "";
    }

}
