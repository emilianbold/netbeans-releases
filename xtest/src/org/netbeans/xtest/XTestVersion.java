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
    
    private Manifest getManifest() {             
        try {
            File xtestHome = new File(xtestHomeProperty);
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
    
    private void printoutAttributes(Attributes atts) {
        Iterator keys = atts.keySet().iterator();
        while (keys.hasNext()) {
            Attributes.Name key = (Attributes.Name)keys.next();
            System.err.println("Attr:"+key+":"+atts.getValue(key));
        }
    }
    
    public String getMajorVersion() {
        return getManifest().getMainAttributes().getValue("XTest-MajorVersion");
    }
    
    public String getMinorVersion() {
        return getManifest().getMainAttributes().getValue("XTest-MinorVersion");
    }

    public String getBranch() {
        return getManifest().getMainAttributes().getValue("XTest-Branch");
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
