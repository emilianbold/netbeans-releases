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
 * DbFeeder.java
 *
 * Created on September 30, 2002, 12:03 PM
 */

package org.netbeans.xtest.pes.dbfeeder;


import org.netbeans.xtest.pes.*;
import java.io.*;
import java.util.*;
import org.netbeans.xtest.pe.*;
import org.netbeans.xtest.pe.xmlbeans.*;
import org.netbeans.xtest.pes.xmlbeans.*;
import org.netbeans.xtest.util.*;
import org.netbeans.xtest.pe.*;
import java.util.logging.Level;
import java.text.SimpleDateFormat;

// xml serialize stuff
import org.netbeans.xtest.xmlserializer.*;

// xml stuff
import org.w3c.dom.*;
import javax.xml.parsers.*;

// sql stuff
import java.sql.*;

/**
 *
 * @author  mb115822
 */
public class DbFeeder {
    

    
    private DbFeederConfig config;
    /** Reason why report is not accepted by database. */
    private String notAcceptedReason = "";

    
    /** Creates a new instance of DbFeeder */
    public DbFeeder(DbFeederConfig config) {
        if (config == null) {
            throw new NullPointerException("config cannot be null");
        }
        this.config = config;
        config.setLoggingLevels();        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        PESLogger.logger.info("DbFeeder start");
        String configFilename = System.getProperty("pes.dbfeeder.config");
        if (configFilename == null) {
            invalidUsage("pes.dbfeeder.config property not specified");
            return;
        }
        try {
            DbFeederConfig config = DbFeederConfig.loadCondfig(new File(configFilename));
            DbFeeder dbFeeder = new DbFeeder(config);
            dbFeeder.run();
        } catch (XMLSerializeException xse) {
            PESLogger.logger.log(Level.SEVERE,"Caught XMLSerializeException when running DbFeeder",xse);
        }
        PESLogger.logger.info("DbFeeder stop");
    }
    
    
    public static void invalidUsage(String message) {
        PESLogger.logger.severe(message);
        PESLogger.logger.info("Usage is: TBD !!!!");
    }

    
    public boolean run() {
        
        // here we go !!!
        try {
            // check for possible crashes 
            checkForCrash();
            // scan incoming directory for xml files
            File incomingDir = config.getWorkDirs().getIncoming();
            // get xml files which were modified a minute ago (hmm, just wondering what
            // is happening when users copying files from a different time zone -> possible bug ?
            File[] xmlFiles = FileUtils.listFiles(incomingDir , "pes-",".xml", 60000);
            // if there were any -> get metadata from them and open supplied zips (if any)
            for (int i=0; i<xmlFiles.length; i++) {
                PESLogger.logger.finer("processing xml metadatafile:"+xmlFiles[i].getPath());
                try {
                    File metadataFile = xmlFiles[i];
                    UploadMetadata metadata = UploadMetadata.loadUploadMetadata(metadataFile);
                    // get zip files from uploaded metadata
                    File[] zips = metadata.getUploadedZipFiles(incomingDir);
                    for (int j=0; j < zips.length; j++) {
                        File zipFile = zips[j];
                        try {
                            PESLogger.logger.finer("Unpacking zip file:"+zipFile.getPath());
                            File workdir = config.getWorkDirs().getWork();
                            ZipUtils.unpackZip(zipFile,workdir);
                            // file unpacked - remove it from incomings
                            // remove the zip from metadata
                            metadata.removeUploadedZip(zipFile);
                            // delete the metadataFile (required, because some environments
                            // may not allow to save over r-- file, but since it is in rwx 
                            // directory it can be deleted
                            metadataFile.delete();
                            // save the metadata
                            metadata.saveUploadMetadata(metadataFile);
                            // delete the zip;
                            boolean result = zipFile.delete();
                            // process the unpacked files
                            processUnpackedFiles(metadata);
                            // now if the zip was not deleted - throw IOException
                            if (result == false) {
                                throw new IOException("Cannot delete zip file: "+zipFile.getPath());
                            }
                        } catch (IOException ioe) {
                            PESLogger.logger.log(Level.SEVERE,"IOException caught when processing zip "+zipFile.getPath()+", moving zip to 'invalid' directory",ioe);
                            try {
                                File invalid = config.getWorkDirs().getInvalid();
                                FileUtils.moveFileToDir(zips[i], invalid);
                            } catch (IOException aioe) {
                                PESLogger.logger.log(Level.SEVERE,"IOException caught when moving zip "+zipFile.getPath()+", moving zip to 'invalid' directory",aioe);
                            }
                        }
                    } // upload metadata
                    // Currently not used. See comment at method
                    //updateLocalTeamBuild(metadata);
                    // delete the metadata as well
                    if (!metadataFile.delete()) {
                        PESLogger.logger.severe("Cannot delete upload metadata file: "+metadataFile);
                    }
                } catch (XMLSerializeException xse) {
                    PESLogger.logger.log(Level.SEVERE,"Caught XMLSerializeException when loading UploadMetadatata",xse);
                }
            }
            return true;
        } catch (IOException ioe) {
            PESLogger.logger.log(Level.SEVERE,"IOException caught when running DbFeeder",ioe);
        }
        return false;
    }

    private void processUnpackedFiles(UploadMetadata metadata) throws IOException {
       // process everything found in workdir      
       File workdir = config.getWorkDirs().getWork();       
      
       
       
       // go on with processing the unpacked files
       File[] irFiles = FileUtils.listFiles(workdir, "pr-", "ir.xml");

       for (int i=0; i<irFiles.length; i++) {
           PESLogger.logger.finest("Processing IncomingReport:"+irFiles[i]);
           IncomingReport ir = IncomingReport.loadIncomingReportFromFile(irFiles[i]);
           PESLogger.logger.finest("Relevant XTestResultsReport:"+ir.getReportRoot());
           File xtrFile = new File(workdir, ir.getReportRoot());
           
           if (isIncomingReportAcceptable(ir)) {
               // ready to upload the report to DB
               try {
                   XTestResultsReport xtr = XTestResultsReport.loadXTestResultsReportFromFile(xtrFile);
                   xtr.setWebLink(ir.getWebLink());
                   if (xtr.getTeam() == null) {
                       PESLogger.logger.fine("Updating XTestResultsReport team to "+ir.getTeam());
                       // local xtest admin does not set team to the report ->
                       // use the one from incoming report
                       xtr.setTeam(ir.getTeam());
                       // soudn't we send some warning ?
                       // perhaps no, PES should have already sent some info this                       
                   }
                  
                   if (xtr.isValid()) {
                       uploadToDatabase(xtr, true);
                       // now delete the xtr/ir pair - we're done with it                       
                       // we should probably check the results of the delete operation                       
                       irFiles[i].delete();
                       xtrFile.delete();
                       // delete results for builds beyond deleteAge threshold
                       deleteOldResults(xtr, config.getDeleteAge(), metadata);
                   } else {
                       // XTestResultsReport is invalid
                       PESLogger.logger.warning("XTestResultsReport from file "+
                               xtrFile.getName()+", is not accepted, moving it to 'invalid' directory. "+
                               "The reason is: "+xtr.getInvalidMessage());
                       // notify zip sender - we should give more accurate reason why the report was rejected
                       notifyUser(metadata.getMailContact(), 
                                  "PES: DbFeeder notification",
                                  "Database refuses to upload submitted report "+xtrFile.getName()+".\n"+
                                  "The reason is: "+xtr.getInvalidMessage()+".\n"+
                                  "For more details contact DbFeeder administrator (hit reply in your email client).");
                        // skip this stuff and move it to invalids
                        moveReportPairToInvalids(irFiles[i],xtrFile);
                   }
               } catch (IOException ioe) {
                   PESLogger.logger.log(Level.SEVERE,"Caught IOException when loading XTestResultsReport from "+xtrFile.getPath(),ioe);
               } catch (SQLException sqle) {
                   PESLogger.logger.log(Level.SEVERE,"Caught SQLException when uploading XTestResultsReport from file "+xtrFile.getPath()+" to database"
                                        +" Moving it to 'invalid' directory.",sqle);
                   moveReportPairToInvalids(irFiles[i],xtrFile);
               }
           } else {
               // incoming report is refused !!!!
               PESLogger.logger.warning("IncomingReport from file "+ir.getReportRoot()+
                                        " is not accepted, moving it to 'invalid' directory.\n"+
                                        "The reason is: "+getNotAcceptedReason()+".");
               // notify zip sender - we should give more accurate reason why the report was rejected
               notifyUser(metadata.getMailContact(),
                          "PES: DbFeeder notification",
                          "Database refuses to upload submitted report "+ir.getReportRoot()+"\n"+
                          "The reason is: "+getNotAcceptedReason()+".\n"+
                          "For more details contact DbFeeder administrator (hit reply in your email client).");
               // skip this stuff and move it to invalids
               moveReportPairToInvalids(irFiles[i],xtrFile);               
           }
           // if not. send notification to zip sender/dbfeeder administrator, move zip to invalids and continue with the next incoming report
           // if yes - load XTestResultsReport and upload it to database
           
           PESLogger.logger.finest("XTestResultsReport loaded :");
           // when xtr is succesfully uploaded -> remove it
       }

   }
   
   
   private void moveReportPairToInvalids(File incomingReportFile, File xtestResultsReportFile) {
       try {
           File invalid = config.getWorkDirs().getInvalid();
           FileUtils.moveFileToDir(incomingReportFile, invalid);
           FileUtils.moveFileToDir(xtestResultsReportFile, invalid);
       } catch (IOException ioe) {
           PESLogger.logger.log(Level.SEVERE,"Caught IOException when moving rejected results to 'invalid' dir",ioe);
       }
   }
   
    
    /** Returns a reason why this report is not acceptable by database.
     * @return reason
     */
    private String getNotAcceptedReason() {
        return notAcceptedReason;
    }
    
    /** Sets a reason why report was not accepted by database. */
    private void setNotAcceptedReason(String reason) {
        this.notAcceptedReason += " "+reason;
    }
    
   
   private boolean isIncomingReportAcceptable(IncomingReport ir) {
       boolean result = false;
       notAcceptedReason = "";
       try {
           // check for team           
           
           Connection connection = config.getDatabaseConnection();
           DbUtils dbUtils = new DbUtils(connection);                      
           String projectQuery = "SELECT id FROM Project WHERE id = '"+ir.getProject_id()+"'";
           PESLogger.logger.finest("Checking whether IncomingReport is acceptable project: SQL command:"+projectQuery);
           
           if (dbUtils.anyResultsFromQuery(projectQuery)) {
               result = true;
           } else {
               setNotAcceptedReason("Project.id '"+ir.getProject_id()+"' not found in table Project.");
               result = false;
           }
       } catch (SQLException sqle) {
           PESLogger.logger.log(Level.SEVERE,"Caught SQLException when getting connection from database",sqle);
           setNotAcceptedReason("Caught SQLException when getting connection from database"+sqle.getMessage());
           result = false;
       } 
       return result;
   }
   

   private void uploadToDatabase(XTestResultsReport xtr, boolean replace) throws SQLException {
       Connection connection = null;
       try {
           connection = config.getDatabaseConnection();           
           DbStorage dbs = new DbStorage(connection);
           dbs.storeXTestResultsReport(xtr, replace);
           connection.commit();
           // will this help ?
           //connection.close();
       } catch (SQLException sqle) {
           // just perform rollback and rethrow the exception
           if (connection != null) {
            connection.rollback();
           }
           throw sqle;
       }
   }
   
    /** Deletes results for builds beyond deleteAge threshold. It deletes only
     * builds which have the same attributes like given XTestResultsReport. It 
     * also ignores milestone builds.
     */
   private void deleteOldResults(XTestResultsReport xtr, int deleteAge, UploadMetadata metadata) throws SQLException {
       Connection connection = null;
       try {
           connection = config.getDatabaseConnection();
           DbStorage dbs = new DbStorage(connection);
           dbs.deleteOldResults(xtr, deleteAge);
           connection.commit();
       } catch (SQLException sqle) {
           // just report the exception and perform rollback
           String message = "Caught SQLException when deleting old results: deleteAge="+deleteAge+
                            ", project_id="+xtr.getProject_id()+", team="+xtr.getTeam()+
                            ", testinggroup="+xtr.getTestingGroup()+", testedtype="+xtr.getTestedType();
           PESLogger.logger.log(Level.SEVERE, message, sqle);
           notifyUser(metadata.getMailContact(), "PES: DbFeeder notification", message+"\n"+sqle.getMessage());
           if (connection != null) {
                connection.rollback();
           }
       }
   }
   
   /** Updates table LocalTeamBuild which holds build numbers of results which
    * are available at local PES. Without this info we can't decide whether
    * to create a link to local PES when showing a HTML page of a query from
    * database.
    */
   private void updateLocalTeamBuild(UploadMetadata metadata)  {
       WebStatus[] webs = metadata.getWebs();
       if (webs != null) {
           Connection connection = null;
           try {
               connection = config.getDatabaseConnection();
               DbUtils dbUtils = new DbUtils(connection);
               for (int i=0; i< webs.length; i++) {
                   WebStatus webStatus = webs[i];
                   
                   ProjectStatus[] projects = webStatus.getProjectStatus();
                   if (projects != null) {
                       for (int j=0; j < projects.length; j++) {
                           ProjectStatus status = projects[j];
                           // update the project details
                           DbStorage storage = new DbStorage(connection);
                           try {
                               storage.updateLocalTeamBuild(status.getTeam(), status.getName(), status.getLastBuildAvailable());
                           } catch (SQLException sqle) {
                               PESLogger.logger.log(Level.SEVERE,"Caught SQLException when updating LocalTeamBuild table with values: team="
                                    +status.getTeam()+" name="+status.getName()+" build="+status.getLastBuildAvailable(),sqle);
                           }
                       }
                   }
                   
               }
           } catch (SQLException sqle) {
               PESLogger.logger.log(Level.SEVERE,"Caught SQLException when updating localPESinfo table",sqle);
               if (connection != null) {
                   DbUtils.closeConnection(connection);
               }
           }
       }
   }
   

   // notifies user who uploaded the zip
   public void notifyUser(String userEmail, String subject, String message) {
       PESMailer mailer = config.getPESMailer();
       PESMailer.Message mailMessage = new PESMailer.Message();
       if (userEmail != null) {
            mailMessage.setToAddress(userEmail);
       }
       mailMessage.setSubject(subject);
       mailMessage.setMessage(message);
       try {
           mailer.send(mailMessage);
       } catch (IOException ioe) {
           PESLogger.logger.log(Level.SEVERE,"Caught IOException when sending email",ioe);
       }       
   }
   
   public void notifyDbFeederOwner(String subject, String message) {
       notifyUser(null, subject, message);
   }
   
   // solves situation after crash of this product
   public void checkForCrash() throws IOException {       
       File workDir = config.getWorkDirs().getWork();

       File[] leftFiles = FileUtils.listFiles(workDir, null, null);
       if (leftFiles == null) {
           throw new IOException("Cannot check working directory "+workDir.getPath());
       }
       if (leftFiles.length == 0) {
           // ok, everything is correct - DbFeeder didn't crash
           return;
       }
       // hmm, we have a problem - there was a crash of the DbFeeder - move all found files to the crash directory
       // create a subdir of crash - timestamp is ok
       PESLogger.logger.log(Level.WARNING,"Left files found in workdir, possible crash of DbFeeder detected");
       // wait one second, so we can be 100% sure the filename is unique
       try {Thread.sleep(1000);} catch (InterruptedException ie) {}
       SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd-HHmmss");
       String dirName = "workdir-"+formatter.format(new java.util.Date());
       File crashDir = new File(config.getWorkDirs().getCrash(),dirName);
       if (!crashDir.mkdirs()) {
           throw new IOException("Cannot create subdirectory of crash dir : "+crashDir.getPath());
       }
       // move the found files to crashDir
       for (int i=0; i < leftFiles.length; i++) {
           FileUtils.moveFileToDir(leftFiles[i], crashDir);
       }       
       // fine we're done
       PESLogger.logger.log(Level.SEVERE, "DbFeeder detected that previous session of DbFeeder has crashed\n\n"+
                "Moved all found files in work directory: "+workDir.getPath()+" to crash directory: "+crashDir.getPath());       
   }
 
   
   
}
