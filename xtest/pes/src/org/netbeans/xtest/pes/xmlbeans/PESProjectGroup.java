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
 * ProjectGroup.java
 *
 * Created on May 14, 2002, 9:34 AM
 */

package org.netbeans.xtest.pes.xmlbeans;


import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pes.XSLTransformers;
import org.netbeans.xtest.util.*;
import org.netbeans.xtest.pe.*;
import java.io.*;
import java.util.*;
// logging stuff
import org.netbeans.xtest.pes.PESLogger;
import java.util.logging.Level;

/**
 *
 * @author  breh
 */
public class PESProjectGroup extends XMLBean {
    
    public static final int ALL_BUILDS = -1;
    public static final int NO_MATRICES = 0;
    
    /** Creates a new instance of ProjectGroup */
    public PESProjectGroup() {
    }
    
    // attributes
    // name of the project group
    public String xmlat_name = null;    
    // description of the project group
    public String xmlat_description = null;    
    // is it main project group - main is displayed on the first page
    public boolean xmlat_main = false;
    // are history matrices generated for this web
    public int xmlat_historyMatrices = PESProjectGroup.NO_MATRICES;
    // number of current builds to be available on the first
    // page - older builds are moved to a separate page
    public int xmlat_currentBuilds = PESProjectGroup.ALL_BUILDS;
    // are old builds truncated ?
    public int xmlat_detailedData = PESProjectGroup.ALL_BUILDS;
    // are any very old builds deleted ?
    public int xmlat_deleteAge = 0;
    
    // is this project group uploaded to database (default = yes if there was specified database upload path )
    public boolean xmlat_uploadToDatabase = true;
    
    
    // elements
    // projects in the group
    public PESProject xmlel_PESProject[];
    
    // business methods
    // name of the group
    public String getName() {
        return xmlat_name;
    }
    
    // description of the group
    public String getDescription() {
        return xmlat_description;
    }
    
    // is this group current - the main one, where all unsorted results are published
    public boolean isMain() {
        return xmlat_main;
    }
    
    // transform matrices
    public int getHistoryMatricesAge() {
        return xmlat_historyMatrices;
    }
    
    // truncate Old builds ?
    public int getDetailedDataAge() {
        return xmlat_detailedData;
    }
    
    // get number of builds to be on the first page
    public int getNumberOfCurrentBuilds() {
        return xmlat_currentBuilds;
    }
    
    // get age after which are builds considered for removal
    public int getDeleteAge() {
        return xmlat_deleteAge;
    }
   
    // validator
    public void checkValidity() throws PESConfigurationException {
        if (xmlat_name == null) throw new PESConfigurationException("PESConfig: PESWeb: PESProjectGroup: name is not set");
        if (xmlat_description == null) throw new PESConfigurationException("PESConfig: PESWeb: PESProjectGroup: description is not set");
        if (!isMain()) {
            if ((xmlel_PESProject == null) | (xmlel_PESProject.length == 0)) throw new PESConfigurationException("PESConfig: PESWeb: PESProjectGroup: group is not main and no projects are defined within");
        }
        if (xmlel_PESProject != null) {
            for (int i=0; i < xmlel_PESProject.length; i++) {
            xmlel_PESProject[i].checkValidity();
        }
            
        }
    }
    
    // business methods 
 
    // pesWeb
    private PESWeb parentWeb;
    private ManagedGroup myManagedGroup;
    
    // all incoming reports belonging to this group
    private Collection incomingReports = new ArrayList();
         
    public boolean addIncomingReport(IncomingReport report, boolean always) {
        // check whether the incoming report does belong to any of the 
        // included projects in this web        
        if (isThisReportMine(report) | (always & isMain())) {
            // include it to incomings ....
            PESLogger.logger.finer("processing incoming report from :"+report.getReportRoot());
            incomingReports.add(report);
            // are hostnames mapped ?
            if (parentWeb.areHostnamesMapped()) {
                String mappedHostname = parentWeb.getMappedHostname(report);
                report.setMappedHostname(mappedHostname);
                PESLogger.logger.finer("Mapping hostname "+report.getHost()+" to "+report.getMappedHostname());
            }
            
            return true;
        }
        return false;
    }
    
    // process this part of web
    public ManagedGroup processResults() throws IOException {
        PESLogger.logger.fine("processing results in group"+getName());
        Iterator i = incomingReports.iterator();        
        Collection failedReports = new ArrayList();
        // copy reports and retransform them
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof IncomingReport) {
                // this is what we need
                IncomingReport report = (IncomingReport)o;
                
                if (!report.isReconfiguration()) {
                    PESLogger.logger.finer("processing incoming report from :"+report.getArchiveFile().getPath());
                    // get output directory
                    File outputDir = parentWeb.assignDirectory(report);
                    PESLogger.logger.finest("outputdir is:"+outputDir);
                    // set the webLink attribute
                    report.setWebLink(parentWeb.getWebLink(outputDir));
                    PESLogger.logger.finest("weblink is:"+report.getWebLink());
                    
                    try {
                        // unpack the whole report
                        if (report.isReplace()) {
                            FileUtils.deleteDirectory(outputDir,true);
                        }
                        File zipFile = report.getArchiveFile();
                        PESLogger.logger.finer("unpacking zip");
                        ZipUtils.unpackZip(zipFile,outputDir);                        
                        
                        // try to get the whole report in one bean -> if it fails 
                        // then this zip is invalid ...
                        PESLogger.logger.finer("loading the whole report (checking it's validity)");
                        XTestResultsReport xtr = ResultsUtils.loadXTestResultsReport(outputDir);
                        // now try to save the report to a uploadworkdir (only if this projectgroup should be uploaded)
                        if (isUploadToDatabase()) {
                            prepareDatabaseData(report,xtr);
                        }
                        // clean not needed stuff (if required)
                        if (parentWeb.isTruncated()) {
                            report.deleteDetailedData(outputDir,false,false);
                        }
                        
                        // retransform if needed
                        try {
                            boolean indexExists = (new File(outputDir,"index.html")).isFile();
                            PESLogger.logger.finest("Does index exist = "+indexExists);
                            boolean retransform = parentWeb.isTruncated() | (!indexExists);
                            PESLogger.logger.finest("Retransform = "+retransform);
                            String mappedHostname = parentWeb.getMappedHostname(report);
                            XSLTransformers.transformResultsForServer(outputDir,parentWeb.isTruncated(), parentWeb.includeIDELogs(),
                            parentWeb.includeExceptions() ,mappedHostname,this,retransform);
                        } catch (Exception e) {
                            PESLogger.logger.log(Level.WARNING,"Results retransformation failed for report from"+report.getReportRoot(),e);
                        }
                    } catch (Exception e) {
                        PESLogger.logger.log(Level.WARNING,"Caught exception when processing report from "+report.getReportRoot()+" report is not valid",e);
                        // exception - delete the directory
                        FileUtils.deleteDirectory(outputDir,false);
                        // this report cannot be added to web
                        failedReports.add(report);
                        report.setValid(false, "Caught exception when processing report from "+report.getReportRoot()+". "+e.getMessage());
                    }
                    
                } else { // reconfiguration stuff
                    // we need to retransform the main navigator ...
                    try {
                        //PESLogger.logger.finer("this report is going to reconfigured");
                        File outputDir = report.getReportDir();
                        PESLogger.logger.finer("Retransforming result in "+outputDir);
                        String mappedHostname = parentWeb.getMappedHostname(report);
                        XSLTransformers.transformResultsForServer(outputDir,parentWeb.isTruncated(), parentWeb.includeIDELogs(),
                            parentWeb.includeExceptions(), mappedHostname,this, parentWeb.isTruncated());
                    } catch (Exception e) {
                            PESLogger.logger.log(Level.WARNING,"Results retransformation failed for report from"+report.getReportRoot(),e);
                            failedReports.add(report);
                    }
                }
            } else {
                // hey - how is this possible ?????
            }
        }
        // Ok, we're done - remove failed results from incoming reports
        // and save this the group's report        
        incomingReports.removeAll(failedReports);
        IncomingReport[] icArray = (IncomingReport[])incomingReports.toArray(new IncomingReport[0]);
        myManagedGroup = ManagedGroup.loadManagedGroup(parentWeb,this);                
        myManagedGroup.addIncomingReports(icArray);
        // now what to so with failedReports ????
        // move failed reports to invalids to invalid and removed from this place
        moveInvalidReports(failedReports);
        // is saving really necessary ? I don't think so ..
        // myManagedGroup.saveManagedGroup();            
        return myManagedGroup;
    }
    
    
    public void processWebPages() throws IOException {
        if (myManagedGroup != null) {
            myManagedGroup.processWebPages();
        }
        myManagedGroup.saveManagedGroup();
    }
    
    
    // get all projects available in this group
    public String[] getProjectsIDs() {
        if (xmlel_PESProject == null) return null;
        String projects[] = new String[xmlel_PESProject.length];
        for (int i=0; i < xmlel_PESProject.length; i++) {
            projects[i] = xmlel_PESProject[i].xmlat_project;
        }
        return projects;
    }
    

    
    // does this report belong to this web
    private boolean isThisReportMine(XTestResultsReport xtr) {
        if (xmlel_PESProject != null ) {
            for (int i=0; i< xmlel_PESProject.length; i++) {
                if (xmlel_PESProject[i].isThisReportMine(xtr)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // set parent web
    public void finishInitialization(PESWeb web) {
        if (xmlel_PESProject != null) {
            for (int i=0; i < xmlel_PESProject.length; i++) {
                if (xmlel_PESProject[i] != null) {
                    xmlel_PESProject[i].finishInitialization();
                }
            }
        }
        // finish initialization of this object
        this.parentWeb = web;
    }
    
    /** are results from this project group uploaded to database ?
     * @return Value of property xmlat_uploadToDatabase.
     *
     */
    public boolean isUploadToDatabase() {
        if (parentWeb.isUploadToDatabase()) {
            return xmlat_uploadToDatabase;
        } else {
            return false;
        }
    }    
    
    
    // 
    private void moveInvalidReports(Collection invalidReports) {
        if (invalidReports == null) return;
        if (invalidReports.isEmpty()) return;
        PESLogger.logger.info("Proecssing invalid results found when processing zips");
        Iterator i = invalidReports.iterator();
        try {
            File invalidDir = parentWeb.pesConfig.getIncomingInvalidDir();
            while (i.hasNext()) {
                Object o = i.next();
                if (o instanceof IncomingReport) {
                    IncomingReport report = (IncomingReport)o;
                    try {
                        PESLogger.logger.warning("found invalid archive: "+report.getArchiveFile()+
                                ", moving it to "+invalidDir+
                                "\nThe reason is: "+report.getInvalidMessage());
                        FileUtils.moveFileToDir(report.getArchiveFile(),invalidDir);
                    } catch (IOException ioe) {
                        PESLogger.logger.log(Level.WARNING,"Caugth IOException when moving invalid zips to 'invalid':"+ioe.getMessage(),ioe);
                    }
                }
            }
        } catch (IOException ioe) {
            PESLogger.logger.log(Level.WARNING,"IOException cautgh when getting incoming/invalid dir",ioe);
        }
    }
    
    
    // preapres data for database upload (creates files in the upload workdir)
    public void prepareDatabaseData(IncomingReport ir, XTestResultsReport xtr) throws IOException {
        PESLogger.logger.finer("report will be uploaded to database");
        // save report
        String localHostName = NetUtils.getLocalHostName();
        // wait one second, so we can be 100% sure the filename is unique
        try {Thread.sleep(1000);} catch (InterruptedException ie) {}
        String timeStamp = (new java.text.SimpleDateFormat("yyMMdd-HHmmss")).format(new java.util.Date());
        String baseFilename = "pr-"+localHostName+"-"+timeStamp+".";
        File xtrFile = new File(parentWeb.pesConfig.getDBUploadsWorkdir(),baseFilename+"xtr.xml");
        xtr.saveXMLBean(xtrFile);
        
        // prepare and save metadata report
        IncomingReport uploadedIR = new IncomingReport();
        uploadedIR.readIncomingReport(ir);
        // set report root to show to the xml file name
        uploadedIR.setReportRoot(xtrFile.getName());
        // set weblink to the http:// format
        String webURL = parentWeb.getWebURL();
        // set team (if not set)
        if (uploadedIR.getTeam() == null) {            
            String pesTeam = parentWeb.pesConfig.getTeam();
            PESLogger.logger.warning("Team is not specified in the report from ${pes_web_home}/"+ir.getWebLink()+" Assigning team to this PES default:"+pesTeam);
            uploadedIR.setTeam(pesTeam);
        }
        
        if (webURL != null) {
            uploadedIR.setWebLink(webURL+ir.getWebLink());
        } else {
            PESLogger.logger.warning("webURL is not specified in PESWeb ->  results will not be reachable from database ");
        }
        
        File irFile = new File(parentWeb.pesConfig.getDBUploadsWorkdir(),baseFilename+"ir.xml");
        uploadedIR.saveXMLBean(irFile);
    }
    
}
