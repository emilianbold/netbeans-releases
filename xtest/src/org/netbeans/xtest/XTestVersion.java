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
 * Created on March 5, 2003, 4:06 PM
 */

package org.netbeans.xtest;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.util.*;
import java.util.jar.*;
import java.io.*;

/**
 *
 * @author  mb115822
 */
public class XTestVersion  extends Task {

    public XTestVersion() {
    }

    private static String UNKNOWN="Unknown";

    private String xtestHomeProperty;

    public static Manifest getManifest(String xtestHome) {
        try {
            File xtestHomeFile = new File(xtestHome);
            File xtestJar = new File(xtestHome,"lib/xtest.jar");
            JarFile xtestJarFile = new JarFile(xtestJar);
            Manifest man = xtestJarFile.getManifest();
            if (man == null) {
                throw new MissingResourceException("Cannot find manifest in xtest.jar",null,null);
            } else {
                return man;
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
            throw new MissingResourceException("Version info not available",null,null);
        } 
    }
    
    private Manifest getManifest() {
        return getManifest(xtestHomeProperty);
    }
    
    private void printoutAttributes(Attributes atts) {
        Iterator keys = atts.keySet().iterator();
        while (keys.hasNext()) {
            Attributes.Name key = (Attributes.Name)keys.next();
            System.err.println("Attr:"+key+":"+atts.getValue(key));
        }
    }
    
    
    public static String getMajorVersion(Manifest man) {
            return man.getMainAttributes().getValue("XTest-MajorVersion");
    }
    
    public static String getMinorVersion(Manifest man) {
        return man.getMainAttributes().getValue("XTest-MinorVersion");
    }    
    
    public static String getBranch(Manifest man) {
        return man.getMainAttributes().getValue("XTest-Branch");
    }        
        
    
    public String getMajorVersion() {
        return getMajorVersion(getManifest());
    }
    
    public String getMinorVersion() {
        return getMinorVersion(getManifest());
    }

    public String getBranch() {
        return getBranch(getManifest());
    }    
    
    public void execute() throws BuildException {
        xtestHomeProperty = this.getProject().getProperty("xtest.home");
        if (xtestHomeProperty == null) {
            throw new BuildException("Cannot provide version when xtest.home property is not set. "
                        +"Please use -Dxtest.home=${your-xtest-home} to run the command");
        }
        String version = "unknown";
        try {
            version = getMajorVersion()+"."+getMinorVersion()+" "+getBranch();
        } catch (Exception mre) {
            // cannot find resource --- unkonwn version
        }
        log("XTest version: "+version);        
    }
    
    
}
