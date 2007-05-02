// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
// </editor-fold>

package org.netbeans.modules.j2ee.sun.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import org.openide.ErrorManager;
import org.openide.util.Utilities;

/**
 * Parser for asenv.conf and asenv.bat
 */
public class Asenv {
    
    transient private final java.util.Properties props = new java.util.Properties();
    
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_JAVA = "AS_JAVA";
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_NS_BIN = "AS_NSS_BIN";
    /**
     * key for path to jdk stored in asenv file
     */
    public static final String AS_HADB = "AS_HADB";
    /**
     * key to path of default domains in asenv file
     */
    public static final String AS_DEF_DOMAINS_PATH = "AS_DEF_DOMAINS_PATH";
    
    /**
     * Creates a new instance of Asenv
     * @param platformRoot root of the platform
     */
    public Asenv(File platformRoot) {
        String ext = (Utilities.isWindows() ? "bat" : "conf");          // NOI18N
        File asenv = new File(platformRoot,"config/asenv."+ext);            // NOI18N
        FileReader fReader = null;
        BufferedReader bReader = null;
        try {
            fReader = new FileReader(asenv);
            bReader = new BufferedReader(fReader);
            
            String line = bReader.readLine();
            while (line != null) {
                StringTokenizer strtok = new StringTokenizer(line,"=");
                if (strtok.countTokens() == 2) {
                    String key = strtok.nextToken();
                    String val = strtok.nextToken();
                    if (key.startsWith("set ")) {
                        key = key.substring(3).trim();
                    }
                    if (val.startsWith("\"")) {
                        val = val.substring(1,val.length()-1);
                    }
                    props.put(key,val);
                }
                line = bReader.readLine(); 
            } 
        } catch (FileNotFoundException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ex);
        } finally {
            if (null != bReader) {
                try {
                    bReader.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ioe);
                }
            }
            if (null != fReader) {
                try {
                    fReader.close();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,ioe);
                }
            }
        }
    }
    
    /**
     * Get values from asenv file
     * @param key variable defined in asenv
     * @return associated value    
     */
    public String get(final String key) {
        return (String) props.getProperty(key);
    }
    
}

