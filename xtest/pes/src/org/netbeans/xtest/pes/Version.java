/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Version.java
 *
 * Created on August 13, 2003, 4:07 PM
 */

package org.netbeans.xtest.pes;

import java.util.*;
import java.util.jar.*;
import java.io.*;


/**
 * Class providing version of PES
 * @author  Martin Brehovsky
 */
public class Version {
    
    // only static methods
    private Version() {
    }
    
    public static String UNKNOWN_VERSION = "Unknown";
    
    public static String getVersion(String pesHome) {
        try {
            File pesHomeFile = new File(pesHome);
            File pesJar = new File(pesHome,"lib/xtest-pes.jar");
            JarFile pesJarFile = new JarFile(pesJar);
            Manifest mf = pesJarFile.getManifest();
            if (mf != null) {                
                String version = mf.getMainAttributes().getValue("XTest-PES-MajorVersion")
                    +"."+mf.getMainAttributes().getValue("XTest-PES-MinorVersion");
                return version;
            }
        } catch (IOException ioe) {
            // cannot get the version
        }
        return UNKNOWN_VERSION;
    }
    
}
