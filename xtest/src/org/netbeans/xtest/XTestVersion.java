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
        JarInputStream jis = null;
        FileInputStream fis = null;
        //final String info="org/netbeans/xtest/version_info";         
        try {
            File xtestHome = new File(xtestHomeProperty);
            File xtestJar = new File(xtestHome,"lib/xtest.jar");
            fis = new FileInputStream(xtestJar);
            jis = new JarInputStream(fis);
            return jis.getManifest();
            //return new Manifest(XTestVersion.class.getClassLoader().getResourceAsStream(info));            
        } catch (IOException e) {
            e.printStackTrace(System.err);
            throw new MissingResourceException("Version info not available",null,null);
        } finally {
            if (jis != null) {
                try {
                    jis.close();
                } catch (IOException ioe) {}
            } else if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ioe) {}
            }
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
        } catch (MissingResourceException mre) {
            // cannot find resource --- unkonwn version
        }
        log("XTest version: "+version);        
    }
    
}
