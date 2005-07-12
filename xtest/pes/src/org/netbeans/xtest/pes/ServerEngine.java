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
 * ServerEngine.java
 *
 * Created on May 27, 2002, 11:49 AM
 */

package org.netbeans.xtest.pes;

import java.io.*;
import java.util.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pes.xmlbeans.*;
import org.netbeans.xtest.util.*;
import org.netbeans.xtest.pe.*;
import java.util.logging.Level;
import java.text.SimpleDateFormat;

// xml serialize stuff
import org.netbeans.xtest.xmlserializer.*;
import org.netbeans.xtest.pes.dbfeeder.*;

// xml stuff
import org.w3c.dom.*;
import javax.xml.parsers.*;

/**
 *
 * @author  mb115822
 */
public class ServerEngine {
    
    
    
    private static ServerEngine serverEngine = null;
    
    private PESConfig pesConfig;
    

    /** Creates a new instance of PEServer */
    private ServerEngine () {
    }
    
    public static ServerEngine createServerEngine(File pesConfigFile) throws InstantiationException {
        PESConfig pesConfig;
        if (!pesConfigFile.isFile()) {
            throw new InstantiationException("PES config file "+pesConfigFile+" does not exist");
        } 
        try {
            pesConfig = PESConfig.loadConfig(pesConfigFile);
        } catch (IOException ioe) {
            throw new InstantiationException("ServerEngine encountered problems when reading config file "+pesConfigFile);            
        } catch (ClassNotFoundException cnfe) {
            throw new InstantiationException("File "+pesConfigFile+" is not valid PES config");
        } catch (PESConfigurationException pesce) {
            throw new InstantiationException("Configuration error: "+pesce.getMessage());        
        }
        return createServerEngine(pesConfig);
    }
    
    public static ServerEngine createServerEngine(PESConfig pesCfg) throws InstantiationException {                
        if (pesCfg == null) {
            throw new InstantiationException("Cannot create ServerEngine without proper config (config set tu null)");
        }
        ServerEngine se = new ServerEngine();                
        se.pesConfig = pesCfg;
        // set logging level for this ServerEngine
        se.setLoggingLevel(pesCfg.getLoggingLevel());
        // when using emails - set logging level for emails as well
        if (pesCfg.canUseEmails()) {
            se.addEmailLogger(pesCfg.getEmailLoggingLevel(), pesCfg.getPESMailer());
        }
        // singleton behavior ...
        ServerEngine.serverEngine = se;
        return se;
    }
    
    public static ServerEngine getServerEngine() throws InstantiationException {
        if (serverEngine != null) {
            return serverEngine;
        } else {
             throw new InstantiationException("ServerEngine was not correctly initialized, run createServerEngine() method first ");            
        }
    }
    
    public PESConfig getPESConfig() {
        return pesConfig;
    }
    
    /* start task, based on argument */
    public void runCommand(String command) {
        if (command.equalsIgnoreCase("run")) {
            // run the processing
            run();
            // upload zips to a shared location (if applicable)
            uploadZips(false);
        } else if (command.toLowerCase().startsWith("reconfig")) {        
           reconfigure();
            // upload zips to a shared location (if applicable)
            uploadZips(true);           
        } else if (command.equalsIgnoreCase("uploadstatus")) {
            uploadZips(true);
        } else if (command.toLowerCase().startsWith("uploadproject")) {
        } else {
            throw new IllegalArgumentException("Command '"+command+"' is not undestood by PES");
        }
    }
    
    
    /* usuall method which is called run */
    public void run() {
        try {
            PESLogger.logger.fine("Processing new results");
            cleanProcessingWorkdir();
            Collection newResults;
            newResults = scanPackedResults(false);
            newResults.addAll(scanPackedResults(true));
            
            /*
            IncomingResults ir = new IncomingResults();
            ir.xmlel_IncomingReport = (IncomingReport[])newResults.toArray(new IncomingReport[0]);
            ir.saveXMLBean(new File(pesConfig.getWorkDir(),"testincoming.xml"));
             **/
            addIncomingResults(newResults);
            processWebs();
            // move all invalid zips to invalid subdir
            processInvalidResults(newResults);
            // delete all processed results - we're done
            deleteIncomingResults(newResults);
            // prepare zips for database upload
            prepareZipsForDBUpload();
        } catch (IOException ioe) {
            PESLogger.logger.severe("IOException caught when processing new results:"+ioe.getMessage());
            PESLogger.logger.log(Level.FINER,"IOException details",ioe);            
        }
        PESLogger.logger.fine("Processing new results done");
    }
    
    /* run when reconfiguring webs ...
     * it is more complicated, because it has to 
     * browse main web if it does not contain any data no 
     * longer belonging to it.
     * 
     */
    public void reconfigure() {
        PESLogger.logger.info("Reconfiguring PES");
        PESWeb[] webs = pesConfig.getPESWebs("",true);
        for (int i=0; i<webs.length; i++) {
            try {
                webs[i].cleanOldConfiguration();
                Collection results = webs[i].searchForAllReports();
                addIncomingResults(results);
                processWebs();
            } catch (IOException ioe) {
                System.out.println("Reconfigure: ioe");
                ioe.printStackTrace();
            }
        }
        PESLogger.logger.info("Reconfiguring PES done");
    }
    
    

    public void uploadStatus() {
        PESLogger.logger.info("Sending PES status to DbFeeder");
        uploadZips(true);
        PESLogger.logger.info("Sending PES status done");
    }
    
    

    
    /** upload prepared zips to database 
     */
    public void uploadZips(boolean statusOnly) {        
        if (pesConfig.getDatabaseUploadPath() != null) {
            PESLogger.logger.info("Uploading database zips to a shared location");
            try {
                File workDir = pesConfig.getDBUploadsWorkdir();
                File uploadDir = pesConfig.getDatabaseUploadDir();
                // create uploadmetadata object
                String mailContact = null;
                if (pesConfig.canUseEmails()) {
                    mailContact = pesConfig.getPESMailer().getToAddress();
                }
                UploadMetadata metadata = new UploadMetadata(mailContact);                
                // list all zips in the workdir                
                File[] zips;
                if (!statusOnly) {
                    zips = FileUtils.listFiles(workDir,"pes-",".zip");
                    metadata.setUploadedZips(zips);
                } else {
                    zips = new File[0];
                }
                // now we should create pes status ..
                // for each web in this config create new webstatus and scan projects' status
                WebStatus[] webs = new WebStatus[pesConfig.xmlel_PESWeb.length];
                for (int i=0; i < pesConfig.xmlel_PESWeb.length; i++) {
                    webs[i] = new WebStatus(pesConfig.xmlel_PESWeb[i]);
                    webs[i].scanWebMetadata();
                }
                // 
                metadata.setWebs(webs);                
                
                // now get the name
                String metadataFilename = metadata.getMetadataFilename();
                
                File currentFile = null;
                try {
                    // now try to copy all zips to the shared location
                    // only if not status only
                    if (!statusOnly) {
                        for (int i=0; i<zips.length; i++) {
                            currentFile = zips[i];
                            PESLogger.logger.fine("Uploading "+currentFile.getPath()+" to "+uploadDir.getPath());
                            FileUtils.moveFileToDir(currentFile, uploadDir);
                        }
                    }
                    // now upload metadata as well
                    // if not status only, only if we have one or more zips
                    if (statusOnly | (zips.length > 0)) {
                        currentFile = new File(uploadDir,metadataFilename);
                        PESLogger.logger.fine("Uploading "+metadataFilename+" to "+uploadDir.getPath());
                        try {
                            Document metadataDocument = XMLSerializer.toDOMDocument(metadata,XMLSerializer.ALL);
                            SerializeDOM.serializeToFile(metadataDocument, currentFile);
                        } catch (XMLSerializeException xse) {
                            PESLogger.logger.log(Level.SEVERE,"Caught XMLSerializeException when uploading database metadata: "+metadataFilename+" to: "+uploadDir.getAbsolutePath(),xse);
                        } catch (ParserConfigurationException pce) {
                            PESLogger.logger.log(Level.SEVERE,"Caught ParserConfigurationException when uploading database metadata: "+currentFile.getAbsolutePath()+" to: "+uploadDir.getAbsolutePath(),pce);
                        }
                    }
                } catch (IOException ioe) {
                    // a problem encountered when moving files
                    PESLogger.logger.log(Level.SEVERE,"Caught IOException when uploading database zip: "+currentFile.getAbsolutePath()+" to: "+uploadDir.getAbsolutePath(),ioe);
                }                 
            } catch (IOException ioe) {
                PESLogger.logger.log(Level.SEVERE,"Caught IOException when checking workdir / upload dir / database zips.",ioe);
            }
            PESLogger.logger.info("Upload finished");
        } else {
            PESLogger.logger.info("Not uploading zips, because no database upload path is specified -> upload is disabled ");
        }
    }
    
    // private ServerEngine methods 
    
    
    // set logger
    private boolean setLoggingLevel(String loggingLevel) {
        if (loggingLevel != null) {
            try {
                return PESLogger.setConsoleLoggingLevel(loggingLevel);
            } catch (IllegalArgumentException iae) {
                System.err.println("Bad logging level specified:"+loggingLevel);                
            }
        }
        return false;
    }
    
    // set email logger
    private boolean addEmailLogger(String loggingLevel, PESMailer mailer) {
        if (loggingLevel != null) {
            try {
                return PESLogger.addEmailLogger(loggingLevel, mailer);
            } catch (IllegalArgumentException iae) {
                System.err.println("Bad logging level specified:"+loggingLevel);
                // might be also mailer=null - TBF
            }
        }
        return false;
    }
    
    
    // check whether the directory is valid
    private static void checkDirValidity(File dir) {
        if (dir == null) throw new IllegalArgumentException("Argument cannot be null");
        if (!dir.isDirectory()) throw new IllegalArgumentException("Argument "+dir+" has to be valid directory");
    }
    
    
    /** Return all results archives available in this directory
     * - if valid is set to true - returns only valid files/ otherwise
     * return invalid files
     * 
     */
    private File[] scanResults(File dir, final boolean valid) throws IOException {
        checkDirValidity(dir); 
        // scan all files beginning with 'xtr-' and ending with '.zip'
        File[] zips = dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile()) {
                    if ((file.getName().startsWith("xtr-")&file.getName().endsWith(".zip"))==valid) {
                        long currentTime = System.currentTimeMillis();
                        long fileModTime = file.lastModified();
                        // accept only files older than one minute
                        if (((currentTime - fileModTime) > 60000) & (fileModTime != 0)) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }        
        });
        
        // 
        if (zips == null) {
            zips = new File[0];
        }        
        return zips;
    }
    
    
    /**
     * create temporary dir in dir
     *
     */
    private static File createTempDir(File dir) throws IOException {
        checkDirValidity(dir); 
        File filename = File.createTempFile("xtr-","-wrk",dir);
        filename.delete();
        if (!filename.mkdirs()) {
            throw new IOException("Cannot create temporary dir '"+filename+"' in "+dir);
        }
        return filename;
    }
    
    /**
     *
     *
     *
     */
    /*
    private void cleanWorkdir() throws IOException {
        File workDir = pesConfig.getWorkdir();
        PESLogger.logger.fine("Cleaning workdir="+workDir);
        boolean result = FileUtils.deleteDirectory(workDir,true);
        if (result == false) {
            PESLogger.logger.warning("Warning: ServerEngine cannot clean working directory:"+workDir);            
        }
    }
    */
    
    private void cleanProcessingWorkdir() throws IOException {
        File workDir = pesConfig.getProcessingWorkdir();
        PESLogger.logger.fine("Cleaning processing workdir="+workDir);
        boolean result = FileUtils.deleteDirectory(workDir,true);
        if (result == false) {
            PESLogger.logger.warning("Warning: ServerEngine cannot clean working directory:"+workDir);            
        }
    }    

    /*
    private void cleanDBUploadsWorkdir() throws IOException {
        File workDir = pesConfig.getProcessingWorkdir();
        PESLogger.logger.fine("Cleaning processing workdir="+workDir);
        boolean result = FileUtils.deleteDirectory(workDir,true);
        if (result == false) {
            PESLogger.logger.warning("Warning: ServerEngine cannot clean working directory:"+workDir);            
        }
    } 
     */   
    
    /** unpack results from incoming to workdir
     * - return all processes stuff as IncomingReport object grouped in collection
     */
    private Collection scanPackedResults(boolean replace) throws IOException {        
        File[] zips;
        if (replace) {
            PESLogger.logger.fine("processing incoming/replace dir");            
            zips = scanResults(pesConfig.getIncomingReplaceDir(),true);
        } else {
            PESLogger.logger.fine("processing incoming dir");             
            zips = scanResults(pesConfig.getIncomingDir(),true);
        }
        // prepare collection
        Collection results = new ArrayList(zips.length);
        
        // do the partial unpacking work for all found zips
        File workDir = pesConfig.getProcessingWorkdir();
        
        for (int i=0; i<zips.length; i++) {
            File tempDir = createTempDir(workDir);
            if (!processZip(results, zips[i], workDir, tempDir, replace)) {
                // delete already unpacked stuff - if available
                PESLogger.logger.finest("Report is not valid - deleting tempDir "+tempDir);
                FileUtils.delete(tempDir);
            }

        }
        
        // now process the files with invalid names        
        if (replace) {
            PESLogger.logger.finer("searching for invalid files from incoming/replace dir");
            zips = scanResults(pesConfig.getIncomingReplaceDir(),false);
        } else {
            PESLogger.logger.finer("searching for invalid files from incoming dir");        
            zips = scanResults(pesConfig.getIncomingDir(),false);
        }
                
        for (int i=0; i<zips.length; i++) {
            PESLogger.logger.finer("found invalid file :"+zips[i]);
            IncomingReport newReport = new IncomingReport();
            results.add(newReport);
            newReport.setValid(false, "Invalid archive file name.");
            newReport.setArchiveFile(zips[i]);
        }
        return results;
    }
    
    private boolean processZip(Collection results, File zip, File workDir, File tempDir, boolean replace) throws IOException {
        PESLogger.logger.finer("processing zip:"+zip+" - trying to unpack to "+tempDir);

        IncomingReport newReport = new IncomingReport();
        results.add(newReport);
        newReport.setArchiveFile(zip);
        newReport.setReplace(replace);
        try {
            // xml files
            ZipUtils.unpackZip(zip,tempDir,PEConstants.XMLRESULTS_DIR);
            // html files
            ZipUtils.unpackZip(zip,tempDir,PEConstants.HTMLRESULTS_DIR);
            ZipUtils.unpackZip(zip,tempDir,PEConstants.INDEX_HTML_FILE);
        } catch (IOException ioe) {
            PESLogger.logger.log(Level.WARNING,"IOE caught when partialy unpacking zip files",ioe);
            newReport.setValid(false, "IOE caught when partialy unpacking zip files: "+ioe.getMessage());
            return false;
        }
        // well, we're still not sure whether the report is valid
        // we need to check it in more details
        newReport.setReportRoot(tempDir.getAbsolutePath());

        // check existence of all important files
        if (!ManagedReport.areReportFilesValid(tempDir)) {
            PESLogger.logger.fine("Report is not valid - "+ManagedReport.getInvalidFileMessage());
            newReport.setValid(false, ManagedReport.getInvalidFileMessage());
            return false;
        }

        // check whether result is also a valid XTestResultReport
        // try to load XTestResultsReport
        File reportFile = new File(tempDir, PEConstants.XMLRESULTS_DIR+
                                            File.separator+
                                            PEConstants.TESTREPORT_XML_FILE);
        PESLogger.logger.finest(" trying to load XTestResultsReport from "+reportFile);

        try {
            XTestResultsReport xtr = XTestResultsReport.loadFromFile(reportFile);
            PESLogger.logger.finest("XTestResultsReport loaded ");
            // ok report was loaded - what about validity - is valid ?
            if (xtr.isValid()) {
                // report looks as valid
                PESLogger.logger.finest("XTestResultsReport seems to be valid");
                newReport.readXTestResultsReport(xtr);
            } else {
                // report is not valid
                //PESLogger.logger.warning("XTestResultsReport from "+zip+" is is not valid. The reason is: "+xtr.getInvalidMessage());
                //newReport.setValid(false, "XTestResultsReport from "+zip+" is is not valid. The reason is: "+xtr.getInvalidMessage())
                PESLogger.logger.fine("XTestResultsReport from "+zip+" is is not valid. The reason is: "+xtr.getInvalidMessage());;
                newReport.setValid(false, xtr.getInvalidMessage());
                return false;
            }
        } catch (Exception e) {
            // some problem ... report cannot be loaded to XTestResultRseport object
            PESLogger.logger.log(Level.FINEST,"Loading report failed, Exception thrown:",e);
            newReport.setValid(false, "Loading report failed, Exception thrown: "+e.getMessage());
            return false;
        }
        // set validity of processed report
        newReport.setValid(true);
        return true;
    }
    
    /** move invalid results to invalid dir
     * - also remove invalid results from supplied collection - we're done
     * with invalid results for now
     */
    private void processInvalidResults(Collection results) throws IOException {
        if (results == null) return;
        if (results.isEmpty()) return;
        PESLogger.logger.fine("Processing invalid results");
        Collection removedResults = new ArrayList();
        Iterator i = results.iterator();
        File invalidDir = pesConfig.getIncomingInvalidDir();
        while (i.hasNext()) {
            Object o = i.next();
            if (o instanceof IncomingReport) {
                IncomingReport report = (IncomingReport)o;
                if (!report.isValid()) {
                    // delete file ....
                    try {
                        PESLogger.logger.warning("found invalid archive: "+report.getArchiveFile()+
                                ", moving it to "+invalidDir+
                                "\nThe reason is: "+report.getInvalidMessage());
                        removedResults.add(report);
                        FileUtils.moveFileToDir(report.getArchiveFile(),invalidDir);
                    } catch (IOException ioe) {                        
                        PESLogger.logger.log(Level.WARNING,"Caugth IOException when moving invalid zips to 'invalid':"+ioe.getMessage(),ioe);                        
                    }
                }
            } else {
                // this should never happend - if it even does -> remove the object
                removedResults.add(o);
            }
        }
        // now remove all invalid results from the collection
        results.removeAll(removedResults);
    }
    
    
    // add incoming results to all webs
    private void addIncomingResults(Collection results) {        
        PESLogger.logger.fine("Adding incoming results");
        // get all 'archive' webs
        PESWeb archives[] = pesConfig.getPESWebs(PESWeb.ARCHIVE,false);
        // get the rest of the webs
        PESWeb nonArchives[] = pesConfig.getPESWebs(PESWeb.ARCHIVE,true);
        
        Collection archivedReports = new ArrayList();
        Iterator i = results.iterator();
        while (i.hasNext()) {
            IncomingReport report = (IncomingReport)i.next();
            if (report.isValid()) {
                PESLogger.logger.finer("adding report:"+report.getArchiveFile());;
                boolean isArchived = false;
                for (int j = 0; j < archives.length; j++) {
                    if (archives[j].addIncomingReport(report)) {
                        isArchived = true;
                    }
                }
                if (!isArchived) {
                    // if the result was not already archived
                    // propagate it to main and copy sites
                    for (int j=0; j < nonArchives.length; j++) {
                        nonArchives[j].addIncomingReport(report);
                    }
                }
     }
        }
    }
    
    
    // delete incoming results
    private void deleteIncomingResults(Collection results) {
        PESLogger.logger.fine("Deleting incoming results");
        Iterator i = results.iterator();
        while (i.hasNext()) {
            IncomingReport report = (IncomingReport)i.next();
            PESLogger.logger.finer("deleting report "+report.getArchiveFile());
            if (report.getArchiveFile().delete()==false) {
                PESLogger.logger.finer("cannot delete result archive:"+report.getArchiveFile());
            }
        }
    }
    
    
    // process webs
    private void processWebs() {
        PESLogger.logger.fine("Processing webs");
        PESWeb[] webs = pesConfig.getPESWebs("",true);
        for (int i=0;i<webs.length; i++) {            
            webs[i].processResults();
            webs[i].processWebPages();
        }
    }
    
    
    
    
    // upload results to the database (only to the path where pes feeder expects the results)
    private void prepareZipsForDBUpload() {
        // create zips for uploads
        PESLogger.logger.fine("Preparing zips for db upload");
        // pack all pr*.xml files
        File zipFile = null;
        try {
            File workDir = pesConfig.getDBUploadsWorkdir();
            // list all suitabe files
            File[] filesToPack = workDir.listFiles(new FilenameFilter() {
                                public boolean accept(File f, String name) {
                                    if (name.startsWith("pr-") & (name.endsWith(".xml"))) {
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            });
            // create an archive
            // get timestamp
            if (filesToPack.length > 0) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-HHmmss");
                String timestamp = formatter.format(new Date());  
                zipFile = new File(workDir, "pes-"+NetUtils.getLocalHostName()+"-"+timestamp+".zip");
                ZipUtils.createZip(zipFile, filesToPack, workDir);
                // delete packed files
                if (FileUtils.deleteFiles(filesToPack) == false) {
                    PESLogger.logger.severe("Unable to one or more files prepared for database upload "); 
                }
            }            
        } catch (IOException ioe) {
            PESLogger.logger.log(Level.SEVERE,"Exception caught when preparing zips for database upload",ioe);
            // delete the zip if there was some problem -> may be it will create the next time correctly
            if (zipFile != null) {
                zipFile.delete();
            }
        }
    }
}
