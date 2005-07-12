/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * ManagedReport.java
 *
 * Created on December 14, 2001, 7:24 PM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pe.PEConstants;
import java.util.Comparator;
import java.io.*;
import org.netbeans.xtest.pe.*;
import org.netbeans.xtest.util.FileUtils;

/**
 *
 * @author  mb115822
 * @version 
 */
public class ManagedReport extends XTestResultsReport  {

    /** Creates new ManagedReport */
    public ManagedReport() {
    }

    // besides all attributes (but no elements !!!) use also these attributes
    public String   xmlat_osArch;
    public String   xmlat_osName;
    public String   xmlat_osVersion;
    public String   xmlat_javaVendor;
    public String   xmlat_javaVersion;
    // is it current report (ide.zips are archived for current reports only )    
    public short xmlat_reportStatus = ManagedReport.NEW;
    // mapped hostnname (when mapping is used)
    public String   xmlat_mappedHostname;
    
    
    public static final short NEW=1;
    public static final short OLD=2;
    public static final short IN_TRANSITION=3;
    
    public static final short TO_BE_DELETED = 4;
    
    public static final short ALL = -1;
    
    
    // business methods
   
    public void readXTestResultsReport(XTestResultsReport xtr) {
        super.readXTestResultsReport(xtr);
        // if SystemInfo is available - read also this
        if (xtr.xmlel_SystemInfo != null) {
            readSystemInfo(xtr.xmlel_SystemInfo[0]);            
        }  
    }
    
    // read system info to managed report
    public void readSystemInfo(SystemInfo si) {
        xmlat_osArch = si.xmlat_osArch;
        xmlat_osName = si.xmlat_osName;
        xmlat_osVersion = si.xmlat_osVersion;
        xmlat_javaVendor = si.xmlat_javaVendor;
        xmlat_javaVersion = si.xmlat_javaVersion;
    }
    
    
    public void readManagedReport(ManagedReport mr) {
        readXTestResultsReport(mr);
        xmlat_osArch = mr.xmlat_osArch;
        xmlat_osName = mr.xmlat_osName;
        xmlat_osVersion = mr.xmlat_osVersion;
        xmlat_javaVendor = mr.xmlat_javaVendor;
        xmlat_javaVersion = mr.xmlat_javaVersion;
        xmlat_reportStatus = mr.xmlat_reportStatus;
        xmlat_mappedHostname = mr.xmlat_mappedHostname;
    }
    
    
    public String getHostnameAsOS() {
        String result = null;
        // need to update for Widndows XP - JDK 1.3 does not know Windows XP
        if (xmlat_osName!=null) {
            result = xmlat_osName;
        }
        if (xmlat_osVersion != null) {
            result += "-"+xmlat_osVersion;
        }
        // Windows XP and JDK 1.3 rename
        /*
        if (result.equals("Windows 2000-5.1")) {
            // is this really true ?
            result = "Windows XP-5.1";
        }
         **/

        if (xmlat_osArch != null) {
            result += "-"+xmlat_osArch;
        }
        if (result == null) {
            result = "Unknown-Machine";
        }
        return result;
    }
    
    public boolean isNew() {
        return (xmlat_reportStatus == NEW);
    }
    
    public boolean isInTransition() {
        return (xmlat_reportStatus == IN_TRANSITION);
    }
    
    public boolean isOld() {
        return (xmlat_reportStatus == OLD);
    }
    
    public boolean isToBeDeleted() {
        return (xmlat_reportStatus == TO_BE_DELETED);
    }
    
    public void setReportStatus(short status) {
        xmlat_reportStatus = status;
    }
    
    public void setMappedHostname(String mappedHostname) {
        xmlat_mappedHostname = mappedHostname;
    }
    
    public String getMappedHostname() {
        return xmlat_mappedHostname;
    }
    
    
    public String getPathToResultsRoot() {
        if (xmlat_webLink == null) return null;
                
        int index = xmlat_webLink.lastIndexOf("/index.html");
        if (index != -1) {
            return xmlat_webLink.substring(0,index);
        } else {
            return xmlat_webLink;
        }
       
    }
    
    public String getPathToProject() {
        int idx = xmlat_webLink.indexOf(File.separatorChar);
        if (idx != -1) {
            return xmlat_webLink.substring(0,idx);
        }
        else {
            return xmlat_webLink;
        }
    }
    
    
    // delete all ide.zip files, all working directories, etc ...
    public void deleteDetailedData(File reportRoot, boolean leaveIDELog, boolean leaveAntLogs) throws IOException {
        File testRunDirs[] = ResultsUtils.listTestRuns(reportRoot);
        for (int i=0; i<testRunDirs.length; i++) {
            File testRunDir = testRunDirs[i];
            File log_dir = new File(testRunDir, PEConstants.ANT_LOGDIR_LOCATION);
            if (log_dir.exists())
                if (!leaveAntLogs) {
                    FileUtils.deleteDirectory(log_dir,false);
                }
            File testBagDirs[] = ResultsUtils.listTestBags(testRunDir);
            for (int j=0; j<testBagDirs.length; j++) {
                File testBagDir = testBagDirs[j];
                if (leaveIDELog) {
                    File ideZip = new File(testBagDir,PEConstants.IDE_USERDIR_LOCATION+"/ide.zip");
                    if (ideZip.exists()) {
                        ideZip.delete();
                    }
                } else {
                    File sysDir = new File(testBagDir,PEConstants.SYSDIR_LOCATION);
                    if (sysDir.isDirectory()) {
                        FileUtils.deleteDirectory(sysDir,false);
                    }
                }
                File userDir = new File(testBagDir,"user");
                if (userDir.exists()) {
                    FileUtils.deleteDirectory(userDir,false);
                }
            }
        }
    }
    
    
    // deletes whole report
    public void deleteFullReport(File reportRoot) throws IOException {
        if (!reportRoot.isDirectory()) {
            throw new IllegalArgumentException("reportRoot is not a valid directory - cannot delete");            
        }
        // delete full report
        if (!FileUtils.delete( reportRoot )) {
            throw new IOException("Cannot delete directory "+reportRoot);
        }
    }
    
    // toString() method
    /*
    public String toString() {
        StringBuffer str = new StringBuffer("ManagedReport: ");
        str.append(
     
    }
    */
    
    private static String invalidFileMessage = "";
    
    public static String getInvalidFileMessage() {
        return ManagedReport.invalidFileMessage;
    }
    
    private static boolean checkFile(File file) {
        if (!file.isFile()) {
            invalidFileMessage = "File "+file+" not found.";
            return false;
        }
        return true;
    }
    
    // check validity of the XML part of the report
    public static boolean areXMLFilesValid(File reportRootDir) {
        return 
            checkFile(new File(reportRootDir,getXMLResultsFilename(PEConstants.TESTREPORT_XML_FILE))) &&
            checkFile(new File(reportRootDir,getXMLResultsFilename(PEConstants.TESTREPORT_FAILURES_XML_FILE)));
    }

    // check validity of the report
    // this means, report has all xml and html files generated, including index.html
    public static boolean areReportFilesValid(File reportRootDir) {
        // xml files
        if (!areXMLFilesValid(reportRootDir)) {
            return false;
        }
        // html files
        return 
            checkFile(new File(reportRootDir,getHTMLResultsFilename(PEConstants.INDEX_HTML_FILE))) &&
            checkFile(new File(reportRootDir,getHTMLResultsFilename(PEConstants.MAIN_NAVIGATOR_HTML_FILE))) &&
            checkFile(new File(reportRootDir,getHTMLResultsFilename(PEConstants.SYSTEMINFO_HTML_FILE))) &&
            checkFile(new File(reportRootDir,getHTMLResultsFilename(PEConstants.TESTREPORT_HTML_FILE))) &&
            checkFile(new File(reportRootDir,getHTMLResultsFilename(PEConstants.TESTREPORT_FAILURES_HTML_FILE)));
    }

    
    private static String getXMLResultsFilename(String filename) {
        return PEConstants.XMLRESULTS_DIR+File.separator+filename;
    }
    
    private static String getHTMLResultsFilename(String filename) {
        if (filename.equals(PEConstants.INDEX_HTML_FILE)) {
            return filename;
        } else {
            return PEConstants.HTMLRESULTS_DIR+File.separator+filename;
        }
    }
    
    
    
    private static final String[] COMPARISION_FIELDS = new String[] {
        "xmlat_host","xmlat_project","xmlat_build",
        "xmlat_testingGroup","xmlat_testedType","xmlat_comment",
        "xmlat_osArch","xmlat_osName","xmlat_osVersion","xmlat_javaVersion",
        "xmlat_javaVendor"
    };
    
    public boolean equals(Object obj) {        
        if (!(obj instanceof ManagedReport)) return false;
        if (obj == this) return true;
        try {
            return equalObjectsByFields(this,obj,COMPARISION_FIELDS);            
        } catch (java.lang.NoSuchFieldException nsfe) {
            return false;
        }
    }
    
    
    public boolean isEqualTo(String project, String build, String testingGroup, String testedType, String host) {
        if ((project!=null)&(xmlat_project!=null)) {
            if (!project.equals(xmlat_project)) {
                return false;
            }
        } else {
            if (project!=null) {
                return false;
            }
        }                
        if ((build!=null)&(xmlat_build!=null)) {
            if (!build.equals(xmlat_build)) {
                return false;
            }
        } else {
            if (build!=null) {
                return false;
            }
        }
        if ((testingGroup!=null)&(xmlat_testingGroup!=null)) {
            if (!testingGroup.equals(xmlat_testingGroup)) {
                return false;
            }
        } else {
            if (testingGroup!=null) {
                return false;
            }
        }
        
        if ((testedType!=null)&(xmlat_testedType!=null)) {
            if (!testedType.equals(xmlat_testedType)) {
                return false;
            }
        } else {
            if (testedType!=null) {
                return false;
            }
        }
        
       if ((host!=null)&(xmlat_host!=null)) {
            if (!host.equals(xmlat_host)) {
                return false;
            }
        } else {
            if (host!=null) {
                return false;
            }
        }
        return true;
    }        
    

    public int indexOf(ManagedReport[] reports) {
        if (reports == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
        for (int i=0; i<reports.length; i++) {
            if (this.equals(reports[i])) {
                // found the same object in reports array
                return i;
            }
        }
        // object was not found
        return -1;
    }
    
    
    public static class BuildComparator implements Comparator {
                
        public boolean equals(Object obj) {
            if (obj instanceof BuildComparator) {
                return true;
            } else {
                return false;
            }
        }
        
        public int compare(Object o1, Object o2) {
            ManagedReport mr1 = (ManagedReport)o1;
            ManagedReport mr2 = (ManagedReport)o2;
            // check for build strings
            int result = mr1.xmlat_build.toLowerCase().compareTo(mr2.xmlat_build.toLowerCase());
            if (result != 0) return result;
            // equals, so check for testing date
            result = mr1.xmlat_timeStamp.compareTo(mr2.xmlat_timeStamp);
            if (result != 0) return result;
            // equals, so check for host
            return mr1.xmlat_host.toLowerCase().compareTo(mr2.xmlat_host.toLowerCase());
        }
    }
    
    
    

}
