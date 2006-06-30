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
