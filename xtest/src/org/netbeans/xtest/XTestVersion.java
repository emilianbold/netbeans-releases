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

/**
 *
 * @author  mb115822
 */
public class XTestVersion  extends Task {
        
    public XTestVersion() {
    }    
    
    private static String UNKNOWN="Unknown";
    private static Package xtestPackage = null;    
    
    private static Manifest getManifest() {
        final String info="org/netbeans/xtest/version_info";         
        try {
            return new Manifest(XTestVersion.class.getClassLoader().getResourceAsStream(info));            
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new MissingResourceException("Version info not available",null,null);
        }
    }
    
    private static void printoutAttributes(Attributes atts) {
        Iterator keys = atts.keySet().iterator();
        while (keys.hasNext()) {
            Attributes.Name key = (Attributes.Name)keys.next();
            System.err.println("Attr:"+key+":"+atts.getValue(key));
        }
    }
    
    public static String getMajorVersion() {
        return getManifest().getMainAttributes().getValue("XTest-MajorVersion");
    }
    
    public static String getMinorVersion() {
        return getManifest().getMainAttributes().getValue("XTest-MinorVersion");
    }
    
    public static String getBranch() {
        return getManifest().getMainAttributes().getValue("XTest-Branch");
    }
    
    public void execute() throws BuildException {        
        String version = "unknown";
        try {
            version = XTestVersion.getMajorVersion()+"."+XTestVersion.getMinorVersion()+" "+XTestVersion.getBranch();
        } catch (MissingResourceException mre) {
            // cannot find resource --- unkonwn version
        }
        log("XTest version: "+version);
    }
    
}
