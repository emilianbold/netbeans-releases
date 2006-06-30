/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */



package org.netbeans.xtest.util;

/**
 *
 * @author  mb115822
 */
public class OSNameMappingTable {

    private static final String [][] OSNAMES_MAPPING = {
        // solaris for sparc
        {"Solaris 2.6","SunOS","5.6","sparc"},
        {"Solaris 7","SunOS","5.7","sparc"},
        {"Solaris 8","SunOS","5.8","sparc"},
        {"Solaris 9","SunOS","5.9","sparc"},
        // solaris for intel
        {"Solaris 2.6 x86","SunOS","5.6","x86"},
        {"Solaris 7 x86","SunOS","5.7","x86"},
        {"Solaris 8 x86","SunOS","5.8","x86"},
        {"Solaris 9 x86","SunOS","5.9","x86"},
        // linux (redhat)
        {"RedHat Linux 7.2","Linux","2.4.7-10","i386"},
        {"RedHat Linux 7.2","Linux","2.4.7-10","x86"},
        // linux (Sun Linux)
        {"Sun Linux 5.0","Linux","2.4.9-31enterprise","i386"},        
        // Windows NT 4
        {"Windows NT 4.0","Windows NT","4.0","x86"},
        // Windows 2000
        {"Windows 2000","Windows 2000","5.0","x86"},
        // Windows XP
        {"Windows XP","Windows 2000","5.1","x86"},
        {"Windows XP","Windows XP","5.1","x86"}                
    };
    
    public static final String UNKNOWN_OS = "Unknown";
    
    /** static methods class */
    private OSNameMappingTable() {
    }
    
    public static String getFullOSName(String osName, String osVersion, String osArch) {
        for (int i=0; i < OSNAMES_MAPPING.length; i++) {
            String[] mappingRow = OSNAMES_MAPPING[i];
            if (mappingRow[1].equalsIgnoreCase(osName)) {
                if (mappingRow[2].equalsIgnoreCase(osVersion)) {
                    if (mappingRow[3].equalsIgnoreCase(osArch)) {
                        return  mappingRow[0];
                    }
                }
            }
        }
        // return unknown
        return UNKNOWN_OS;
    }
    
}
