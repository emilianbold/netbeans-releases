/*
 * SystemInfoTask.java
 *
 * Created on November 13, 2001, 12:23 PM
 */

package org.netbeans.xtest.pe;

import org.apache.tools.ant.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import java.io.*;

// for getting hostname 
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author  mb115822
 * @version 
 */
public class SystemInfoTask extends Task {

    /** Creates new SystemInfoTask */
    public SystemInfoTask() {
    }    
    
    private File outfile;
    
    public void setOutFile(File outfile) {
        this.outfile = outfile;
    }
    
    
    public String getHost() {
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhe) {
            // we cannot get the hostname
            host = "UnknownHost";
        }
        
        return host;
    }
    
    /*
    private void gatherSystemInfo() {
        host = getHostName();
        os_arch = System.getProperty("");
    }*/
    
    
    public String getXTestProject() {
        return "UnknownProject";
        //return project;
    }
    
    public String getBuild() {
        return "UnknownBuild";
    }
    
    public String getOsArch() {
        return System.getProperty("os.arch");
    }
    
    public String getOsName() {
        return System.getProperty("os.name");
    }
    
    public String getOsVersion() {
        return System.getProperty("os.version");
    }
    
    public String getJavaVendor() {
        return System.getProperty("java.vendor");
    }
    
    public String getJavaVersion() {
        return System.getProperty("java.version");
    }
    
    public String getUserLanguage() {
        return System.getProperty("user.language");
    }
    
    public String getUserRegion() {
        return System.getProperty("user.region");
    }
    
    
    
    
    public SystemInfo getSystemInfo() {
        SystemInfo si = new SystemInfo();
        si.xmlat_host = getHost();
        si.xmlat_project = getXTestProject();
        si.xmlat_build = getBuild();
        si.xmlat_osArch = getOsArch();
        si.xmlat_osName = getOsName();
        si.xmlat_osVersion = getOsVersion();
        si.xmlat_javaVendor = getJavaVendor();
        si.xmlat_javaVersion = getJavaVersion();
        si.xmlat_userLanguage = getUserLanguage();
        si.xmlat_userRegion = getUserRegion();
        return si;
    }
    

    public void execute () throws BuildException {
        SystemInfo si = getSystemInfo();
        System.err.println("SI:"+si);
        try {
            FileOutputStream outStream = new FileOutputStream(this.outfile);            
            SerializeDOM.serializeToStream(si.toDocument(),outStream);
        } catch (IOException ioe) {
            System.err.println("SystemInfoTask - cannot save systeminfo");
            ioe.printStackTrace(System.err);
        } catch (Exception e) {
            System.err.println("SystemInfoTask - XMLBean exception ???");
            e.printStackTrace(System.err);           
        }
    }
}
