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
 * Project.java
 *
 * Created on May 14, 2002, 9:33 AM
 */

package org.netbeans.xtest.pes.xmlbeans;

import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pes.PESLogger;
import java.util.logging.Level;
import org.netbeans.xtest.util.FileUtils;
import org.netbeans.xtest.pe.*;
import java.util.*;
import java.io.*;

/**
 *
 * @author  breh
 */
public class PESWeb extends XMLBean {
    
    
    
    
    private static final String XMLDATADIR_NAME="_xmldata";
    
    /** Creates a new instance of Project */
    public PESWeb() {
    }
     
    // attributes
    
    // description of the web
    public String xmlat_description;
    // web root of this web
    public String xmlat_webroot;
    // type of this web
    public static String MAIN="main";
    public static String COPY="copy";
    public static String ARCHIVE="archive";
    public String xmlat_type = PESWeb.MAIN;
    // is this web truncated
    public boolean xmlat_truncate = false;
    
    // are hostnames masked
    public boolean xmlat_maskHostnames = false;
    // are hostnames on this web mapped to different ones 
    public String xmlat_hostnameMappingFile;
    // include IDE logs in reports
    public boolean xmlat_includeIDELogs = true;
    // include Exception details in reports
    public boolean xmlat_includeExceptions = true;
    
    // is this project group uploaded to database (default = yes if there was specified database upload path )
    public boolean xmlat_uploadToDatabase = true;    
    
    // web url where are the results available
    public String xmlat_webURL;
    
    
    // project group
    public PESProjectGroup xmlel_PESProjectGroup[];
    
    
    // getters
    public File getWebRoot() throws IOException {
        if (xmlat_webroot != null) {
            File root = new File(xmlat_webroot);
            if (!root.isDirectory()) {
                if (!root.mkdirs()) {
                    throw new IOException("webroot directory ("+root+") cannot be created");
                }
            }
            return root;
        } else {
            throw new IOException("webroot directory not specified");
        }
    }
    
    // validator
    public void checkValidity() throws PESConfigurationException {
        if (xmlat_webroot == null) throw new PESConfigurationException("PESConfig: PESWeb: webroot is not set");
        if ((xmlel_PESProjectGroup == null) | (xmlel_PESProjectGroup.length == 0)) throw new PESConfigurationException("PESConfig: PESWeb: no PESProjectGroup elements defined");
        int mainCount = 0;        
        for (int i=0; i < xmlel_PESProjectGroup.length; i++) {
            if (xmlel_PESProjectGroup[i].isMain()) {
                mainCount++;
            }
            xmlel_PESProjectGroup[i].checkValidity();
        }
        if (mainCount == 0)  throw new PESConfigurationException("PESConfig: PESWeb: no main PESProjectGroup is defined");
        if (mainCount > 1)  throw new PESConfigurationException("PESConfig: PESWeb: only one main PESProjectGroup can be defined");
        
    }
    
    
    // business methods and variables
    
    
    // reference to config
    PESConfig pesConfig = null;
    
    private ManagedWeb managedWeb;
        
    // hostname mapping table
    private Properties hostnameMap = new Properties();
    
    // get this PES default team
    public String getTeam() {
        return pesConfig.getTeam();
    }
    
    // is web of given type 
    public boolean isType(String type) {
        if (xmlat_type.equals(type)) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isTruncated() {
        return xmlat_truncate;
    }
    
    public boolean includeIDELogs() {
        return xmlat_includeIDELogs;
    }
    
    public boolean includeExceptions() {
        return xmlat_includeExceptions;
    }
    
    
    // is hostname mapped to other name ?
    
    private String getMappedHostname(String hostname) {
        return hostnameMap.getProperty(hostname);
    }
    
    
    public String getMappedHostname(ManagedReport mr) {
        // masking stuff
        String newName = null;
        // mapping stuff
        if (hostnameMappingFileExists()) {
            newName = getMappedHostname(mr.getHost());
        }
        if (newName == null) {
            if (areHostnamesMapped()) {
                newName = mr.getHostnameAsOS();
            }
        }
        return newName;
    }
    
    public boolean areHostnamesMasked() {
        return xmlat_maskHostnames;
    }
    
    public boolean hostnameMappingFileExists() {
        if (xmlat_hostnameMappingFile != null) {
            if (new File(xmlat_hostnameMappingFile).isDirectory()) {
                return true;
            }
        } 
        return false;
        
    }
    
    
    public boolean areHostnamesMapped() {
        return (hostnameMappingFileExists() | areHostnamesMasked());
    }

    /** Getter for property xmlat_webURL.
     * @return Value of property xmlat_webURL.
     *
     */
    public java.lang.String getWebURL() {
        if ((xmlat_webURL != null)&(xmlat_webURL.length() > 0)) {
            if (xmlat_webURL.charAt(xmlat_webURL.length()-1) != '/') {
                // add slash at the end of the character
                return xmlat_webURL+"/";
            } else {
                // url looks ok
                return xmlat_webURL;
            }
        } else {
            return null;
        }
    }    
    
     
    
    
    // add incoming report to this web 
    // if this report does not belong to this web - return false
    // otherwise add it and return true
    public boolean addIncomingReport(IncomingReport report) {
        // check whether the incoming report does belong to any of the 
        // included projects in this web
        PESLogger.logger.finer("adding incoming report to web:"+xmlat_description);
        if (xmlel_PESProjectGroup != null) {
            boolean result = false;
            boolean always = false;
            if (this.isType(PESWeb.MAIN)) {
                // always add incoming report (archives are
                // processed earlier and results removed
                always = true;
            }
            
            int mainGroupIndex = -1;
            for (int i=0; i < xmlel_PESProjectGroup.length; i++) {
                if (xmlel_PESProjectGroup[i].isMain()) {
                    mainGroupIndex = i;
                } else {
                    if ( xmlel_PESProjectGroup[i].addIncomingReport(report,always)) {
                        result = true;
                    }
                }
            }
            if ((result == false) & (mainGroupIndex > -1)) {
                result = xmlel_PESProjectGroup[mainGroupIndex].addIncomingReport(report,always);
            }            
            // if result is true - report was added. If this web contains mapped names, map the name
            
            return result;
        } else {
            return false;
        }
    }
    
    

    /* reconfiguration method - searching for all already processe reports */
    
    public Collection searchForAllReports() throws IOException {
        PESLogger.logger.info("searching for all report in web:"+xmlat_description);
        File webRoot = getWebRoot();
        PESLogger.logger.fine("webroot is "+webRoot);
        Collection incomingReports = new ArrayList();
        // for all projects
        File[] projectDirs = FileUtils.listSubdirectories(webRoot);
        for (int i=0; i<projectDirs.length; i++) {
            if (!projectDirs[i].getName().equals(PESWeb.XMLDATADIR_NAME)) {
                // for all builds
                File[] buildDirs = FileUtils.listSubdirectories(projectDirs[i]);
                for (int j=0; j<buildDirs.length; j++) {
                    // for all testypes/groups
                    File[] typeDirs = FileUtils.listSubdirectories(buildDirs[j]);
                    for (int k=0; k<typeDirs.length; k++) {
                        // for all hosts
                        File[] hostDirs = FileUtils.listSubdirectories(typeDirs[k]);
                        for (int l=0; l<hostDirs.length; l++) {
                            // investigate report ...
                            PESLogger.logger.fine("Searching in "+hostDirs[l]);                            
                            // try to load XTest results report - if not successfull ... that's bad
                            try {
                                IncomingReport ic = new IncomingReport();
                                ic.setReportRoot(hostDirs[l].getAbsolutePath());
                                // is report valid
                                if (ic.areReportFilesValid()) {
                                    File xtrFile = new File(hostDirs[l], PEConstants.XMLRESULTS_DIR+
                                    File.separator + PEConstants.TESTREPORT_XML_FILE);
                                    XTestResultsReport xtr = XTestResultsReport.loadFromFile(xtrFile);
                                    ic.readXTestResultsReport(xtr);
                                    ic.setReconfiguration(true);
                                    ic.setValid(true);
                                    ic.xmlat_webLink = getWebLink(hostDirs[l]);
                                    
                                    
                                    incomingReports.add(ic);
                                } else {
                                    PESLogger.logger.info("Report found in "+hostDirs[l]+" is not valid. Please remove it from web");
                                }
                            } catch (IOException ioe) {
                                PESLogger.logger.log(Level.WARNING,"IOException caught when processing XTestResultReport in:"+hostDirs[l],ioe);                                    
                            } catch (ClassNotFoundException cnfe) {
                                PESLogger.logger.log(Level.WARNING,"ClassNotFoundException caught when processing XTestResultReport in:"+hostDirs[l],cnfe);
                            }
                            
                        }
                    }
                }
            }
        }        
        return incomingReports;
    }
    
    // return data dir - dir where PESWeb stores it's data
    
    public File getDataDir() throws IOException {
        String dataDirName = PESWeb.XMLDATADIR_NAME;
        File dataDir = new File(getWebRoot(),dataDirName);
        if (!dataDir.isDirectory()) {
            if (!dataDir.mkdirs()) {
                throw new IOException("Cannot create PESWeb datadir:"+dataDir);
            }
        }
        return dataDir;
    }
    
    public void finishInitialization(PESConfig config) {
        this.pesConfig = config;
        if (xmlel_PESProjectGroup != null) {
            for (int i=0; i < xmlel_PESProjectGroup.length; i++) {
                if (xmlel_PESProjectGroup[i] != null) {
                    xmlel_PESProjectGroup[i].finishInitialization(this);
                }
            }
        }
        // also load mapping file if specified
        if (xmlat_hostnameMappingFile != null) {                        
            File mappingFile = new File(xmlat_hostnameMappingFile);
            if (mappingFile.isFile()) {
                try {
                    InputStream is = new FileInputStream(mappingFile);
                    hostnameMap.load(is);
                } catch (IOException ioe) {
                    PESLogger.logger.log(Level.WARNING,"IOException caught when ploading mapping file:"+xmlat_hostnameMappingFile,ioe);                    
                    hostnameMap = new Properties();
                }
            } else {
                PESLogger.logger.log(Level.WARNING,"Mapping file '"+xmlat_hostnameMappingFile+"' not found");
            }
        }
        // is upload to database activated (databaseUploadPath is specified in thec config file)
        if (xmlat_uploadToDatabase == true) {
            // only when uploadToDatabase is enabled in this project
            if (config.getDatabaseUploadPath() == null) {
                // databaseUploadPath is not specified -> disable db upload functionality
                xmlat_uploadToDatabase = false;
            }
        }
    }
    
    
    // process this web
    public void processResults() {
        if (xmlel_PESProjectGroup != null) {          
            PESLogger.logger.fine("processing web:"+xmlat_description);
            ManagedGroup[] groups = new ManagedGroup[xmlel_PESProjectGroup.length];            
            for (int i=0; i < xmlel_PESProjectGroup.length; i++) {
                PESLogger.logger.fine(" processing group:"+xmlel_PESProjectGroup[i].xmlat_name);
                try {
                    groups[i] = xmlel_PESProjectGroup[i].processResults();
                } catch (IOException ioe) {
                    PESLogger.logger.log(Level.WARNING,"IOException caught when processing group "+xmlel_PESProjectGroup[i].getName(),ioe); 
                }
            }
            // now save web as well
            try {
                managedWeb = ManagedWeb.loadManagedWeb(this);            
                managedWeb.xmlel_ManagedGroup = (ManagedGroup[])XMLBean.shrinkArray(groups);
                managedWeb.saveManagedWeb(2);
                
            } catch (IOException ioe) {
                PESLogger.logger.log(Level.WARNING,"IOException caught when processing web "+xmlat_description,ioe);
            }
            
        } 
    }
    
    
    // transformations
    public void processWebPages() {
        // do the transformation stuff
        PESLogger.logger.fine("processing web:"+xmlat_description);
        for (int i=0; i< xmlel_PESProjectGroup.length; i++) {
            PESLogger.logger.fine(" processing group:"+xmlel_PESProjectGroup[i].xmlat_name);
            try {
                xmlel_PESProjectGroup[i].processWebPages();
            } catch (IOException ioe) {
                PESLogger.logger.log(Level.WARNING,"IOException caught when processing group "+xmlel_PESProjectGroup[i].getName(),ioe); 
            }
        }
    }
    
    
    // find approriate directory for this report
    public File assignDirectory(IncomingReport report) throws IOException {
        if (report == null) {
            throw new IllegalArgumentException("report cannot be null");
        }
        String base = report.getProject() + File.separator +
                report.getBuild() + File.separator + report.getTestingGroup() +
                "-" + report.getTestedType();
        
        String hostname;        
        if (areHostnamesMapped()) { 
            if (report.getMappedHostname() != null) {
                hostname = report.getMappedHostname();
            } else {
                hostname = report.getHost();
            }
        } else {
            hostname = report.getHost();
        }
        
        PESLogger.logger.finest("assigned hostname is "+hostname);
        
        File newDir = new File(getWebRoot(),FileUtils.normalizeName(base+File.separator+hostname));
        if (newDir.isDirectory()) {
            if (report.isReplace()) {
                // no problem - this report needs to be replaced
                FileUtils.deleteDirectory(newDir,true);
                return newDir;
            }
        } 
        // we need to find a new directory for this report
        int i=0;
        while (newDir.exists()) {
            i++;
            newDir = new File(getWebRoot(),FileUtils.normalizeName(base+File.separator+hostname+"_"+i));
        }
        // we should have it now
        boolean result = newDir.mkdirs();
        // log assigned directory
        PESLogger.logger.finest("assigned directory is "+hostname);
        if (result) {
            return newDir;
        } else {
            throw new IOException("Cannot create directory "+newDir);
        }        
    }
    
    
    
    public String getWebLink(File assignedDirectory) throws IOException {
        if (assignedDirectory == null) {
            throw new IllegalArgumentException("argument cannot be null");
        }
        String rootPath = getWebRoot().getAbsolutePath();
        String assignedPath = assignedDirectory.getAbsolutePath();
        if (assignedPath.startsWith(rootPath) & (assignedPath.length() > rootPath.length())) {            
            String result = assignedPath.substring(rootPath.length()+1).replace('\\','/')+"/index.html";            
            return result;
        } else {
            throw new IOException("cannot determine web link - assigned directory does not begin with web root path");
        }
        
    }
    
    
    /** are results from this project group uploaded to database ?
     * @return Value of property xmlat_uploadToDatabase.
     *
     */
    public boolean isUploadToDatabase() {        
        return xmlat_uploadToDatabase;
    }    
    
    
    /** set if results shoud be uploaded to database
     * this method is usually used when finishing initializaion - if PESConfig.databaseUploadPath is not specified -> this property is
     * set to false, regardless of the value in the xml config file 
     */
    void setUploadToDatabase(boolean uploadToDatabase) {
        xmlat_uploadToDatabase = uploadToDatabase;
    }
        

    
    private static boolean deleteFilesWithSuffix(File dir, final String suffix, int depth) {
        if (depth == 0) {
            return true;
        }
        if (dir == null) {
            return false;
        }        
        
        PESLogger.logger.finer("Deleting directory "+dir+" with suffix "+suffix+" and depth "+depth);
        
        File[] subDirs = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        
        boolean result = true;
        for (int i=0;i<subDirs.length;i++) {
            if (deleteHTMLFiles(subDirs[i],depth-1)==false) {
                result = false;
            }
        }
        
        File[] htmls = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile()) {
                    if (suffix!=null) {
                        if (file.getName().endsWith(suffix)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            }
        });
        
        for (int i=0; i<htmls.length; i++) {
            if (htmls[i].delete() == false) {
                result = false;
            }
        }
        return result;
    }
    
    private static boolean deleteHTMLFiles(File dir, int depth) {
        return deleteFilesWithSuffix(dir,".html",depth);
    }
    
    private static boolean deleteXMLFiles(File dir, int depth) {
        return deleteFilesWithSuffix(dir,".xml",depth);
    }
    
    
    public void cleanOldConfiguration() throws IOException {
        deleteHTMLFiles(getWebRoot(),4);
        //deleteXMLFiles(getWebRoot(),2);
        FileUtils.deleteDirectory(getDataDir(),true);
    }
    
    
    // delete html files of this web (only these made by server)
    public boolean deleteGeneratedPages() {
        try {
            
            File webRoot = this.getWebRoot();
            return deleteHTMLFiles(webRoot,2);
        } catch (IOException ioe) {            
            PESLogger.logger.log(Level.WARNING,"IOException caught when deleting HTML Files:",ioe);
            return false;
        }
    }
    
    
    
    
    
    // process incoming reports on this web
    
    
    // get all projects managed by this web
    private PESProject[] getAllProjects() {
        if (xmlel_PESProjectGroup != null) {
            // compute the new array size
            int arraySize = 0;
            for (int i=0; i< xmlel_PESProjectGroup.length; i++) {
                if (xmlel_PESProjectGroup[i].xmlel_PESProject != null) {
                    arraySize += xmlel_PESProjectGroup[i].xmlel_PESProject.length;
                }
            }
            // now copy the array
            PESProject[] projects = new PESProject[arraySize];
            for (int i=0, p=0; i< xmlel_PESProjectGroup.length; i++) {                
                if (xmlel_PESProjectGroup[i].xmlel_PESProject != null) {
                    for (int j=0; j < xmlel_PESProjectGroup[i].xmlel_PESProject.length; j++, p++) {
                        projects[p] = xmlel_PESProjectGroup[i].xmlel_PESProject[j];
                    }
                }
            }            
            return projects;
            
        } else {
            return new PESProject[0];
        }
    }
    
    
    // does this report belong to this web
    private boolean isThisReportMine(XTestResultsReport xtr) {
        // main web type always accepts all projects
        if (xmlat_type.equals(PESWeb.MAIN)) {
            return true;
        }
        // web is not main - ask subprojects if it does belong to
        // this web
        PESProject[] projects = getAllProjects();
        boolean result = false;
        for (int i=0; i < projects.length ; i++) {
            if (projects[i].isThisReportMine(xtr)) {
                // yes this is mine report
                result = true;
                break;
            }
        }
        return result;
    }

    
    
}
