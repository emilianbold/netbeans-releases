/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer;

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.RequiredBytesTable;
import com.installshield.util.Log;
import com.installshield.wizard.service.MutableOperationState;
import com.installshield.wizard.service.exitcode.ExitCodeService;
import com.installshield.wizard.service.file.FileService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;

public class StorageBuilderAction extends ProductAction {
    
    //return code incase an error returns
    public static final int STORAGE_BUILDER_UNHANDLED_ERROR = -200;
    public static final String STORAGE_BUILDER_TEMP_DIR = "mdrtmpdir";
    public static final String STORAGE_BUILDER_DEST_DIR = "ide5" + File.separator + "mdrstorage";
    public static final String STORAGE_BUILDER_TEMP_FILE = "mdrtmpfile";
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String statusDesc = "";
    private String nbInstallDir = "";
    private String uninstDir = "";
    private String tempPath = "";
    
    private File mdrTempDir;

    private boolean success = false;
    
    private RunCommand runCommand = new RunCommand();
    
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public StorageBuilderAction () {
    }
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass("org.netbeans.installer.RunCommand$StreamAccumulator");
            support.putClass(Util.class.getName());
            support.putClass("org.netbeans.installer.StorageBuilderAction$ProgressThread");
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init (ProductActionSupport support) {
	/*
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        instDirPath = resolveString((String)pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation")); */
        nbInstallDir = resolveString("$P(absoluteInstallLocation)");
        logEvent(this, Log.DBG,"nbInstallDir: " + nbInstallDir);
        uninstDir = nbInstallDir + File.separator + "_uninst";
        logEvent(this, Log.DBG,"uninstDir: " + uninstDir);
        logEvent(this, Log.DBG,"jdkHome: " + Util.getJdkHome());
        tempPath = resolveString("$J(temp.dir)");
        logEvent(this, Log.DBG,"TempPath: " + tempPath);
        
        mutableOperationState = support.getOperationState();
    }
    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.storageBuilderMessage)")
	+ " "
	+ resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.waitMessage)") ;
        
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = INSTALL;
            
            //Create new temp dir for storage builder
            mdrTempDir = new File(tempPath + File.separator + STORAGE_BUILDER_TEMP_DIR);
            int i = 1;
            while (mdrTempDir.exists()) {
                mdrTempDir = new File(tempPath + File.separator + STORAGE_BUILDER_TEMP_DIR + i);
                i++;
            }
            if (!mdrTempDir.mkdir()) {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Cannot create temporary directory for Storage Builder.");
                try {
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(STORAGE_BUILDER_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                }
                return;
            }
            logEvent(this, Log.DBG,"Created temporary dir for SB: " + mdrTempDir.getAbsolutePath());
            
            //Create destination dir for storage builder
            File mdrDestDir = new File(nbInstallDir + File.separator + STORAGE_BUILDER_DEST_DIR);
            if (mdrDestDir.exists()) {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Storage Builder destination directory already exists.");
                try {
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(STORAGE_BUILDER_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                }
                return;
            }
            if (!mdrDestDir.mkdir()) {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Cannot create destination directory for Storage Builder.");
                try {
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(STORAGE_BUILDER_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                }
                return;
            }
            logEvent(this, Log.DBG,"Created destination dir for SB: " + mdrDestDir.getAbsolutePath());
            
	    if (Util.isWindowsOS()) {
		String runScript = uninstDir + File.separator + "storagebuilder" + File.separator
                + "run-storage-builder-windows.template";
		createRunScriptWindows(runScript, "run-storage-builder-windows.bat",
                mdrTempDir.getAbsolutePath() + File.separator + STORAGE_BUILDER_TEMP_FILE);
	    } else {
		String runScript = uninstDir + File.separator + "storagebuilder" + File.separator
                + "run-storage-builder-unix.template";
		createRunScript(runScript,"run-storage-builder-unix.sh",
                mdrTempDir.getAbsolutePath() + File.separator + STORAGE_BUILDER_TEMP_FILE);
            }
            
            String execName;
            String driveName;
            int paramCount;

            if (Util.isWindowsOS()) {
                execName = "run-storage-builder-windows.bat";
                paramCount = 1;
            }
            else {
                execName = "run-storage-builder-unix.sh";
                paramCount = 1;
            }
            
            String cmdArray[] = new String[paramCount];
            String execPath   = uninstDir + File.separator + "storagebuilder" + File.separator + execName;
            
	    // Put the command and arguments together for windows
            if (Util.isWindowsOS()) {
                //cmdArray[0] = "cmd /C \"" + execPath + "\"";
                cmdArray[0] = execPath;
            } else {
                cmdArray[0] = execPath;
            }
            
	    // Invoke the correct command
            logEvent(this, Log.DBG,"# # # # # # # #");
            logEvent(this, Log.DBG,"Start Invoking Storage Builder: cmdArray -> " + Arrays.asList(cmdArray).toString());
            runCommand(cmdArray, null, support);
            
            //Delete temporary dir
            Util.deleteCompletely(mdrTempDir,support);
            logEvent(this, Log.DBG,"Deleted temporary dir for SB: " + mdrTempDir.getAbsolutePath());
            
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"Running Storage Builder took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    /** Does cleaning. */
    public void uninstall(ProductActionSupport support) {
        init(support);
        installMode = UNINSTALL;
        
        String fileName;
        fileName = nbInstallDir + File.separator + StorageBuilderAction.STORAGE_BUILDER_DEST_DIR;
        logEvent(this, Log.DBG,"Deleting :" + fileName);
        Util.deleteCompletely(new File(fileName),support);
        
        fileName = uninstDir + File.separator + "storagebuilder" + File.separator + "storagebuilder.log";
        logEvent(this, Log.DBG,"Deleting :" + fileName);
        Util.deleteCompletely(new File(fileName),support);
        
        if (Util.isWindowsOS()) {
            fileName = uninstDir + File.separator + "storagebuilder" + File.separator + "run-storage-builder-windows.bat";
            logEvent(this, Log.DBG,"Deleting :" + fileName);
            Util.deleteCompletely(new File(fileName),support);
        } else {
            fileName = uninstDir + File.separator + "storagebuilder" + File.separator + "run-storage-builder-unix.sh";
            logEvent(this, Log.DBG,"Deleting :" + fileName);
            Util.deleteCompletely(new File(fileName),support);
        }
    }
    
    //threads should only run in install mode until ISMP supports them
    private void runCommand(String[] cmdArray, String [] envP, ProductActionSupport support)
    throws Exception{
        boolean doProgress = !(Boolean.getBoolean("no.progress"));
        logEvent(this, Log.DBG,"doProgress -> " + doProgress);
        
        //mutableOperationState = support.getOperationState();
        logEvent(this, Log.DBG,"cmdArray -> " + Arrays.asList(cmdArray).toString());
        try {
            runCommand.execute(cmdArray, envP, null);
            
            if ((installMode == INSTALL) && doProgress) {
                startProgress();
            }
            
            runCommand.waitFor();
            logEvent(this, Log.DBG,runCommand.print());
            
            int status = runCommand.getReturnStatus();
            logEvent(this, Log.DBG,"Return status: " + status);
            if (status != 0) {
                //Log error
            }
            
            if((installMode == INSTALL) && doProgress) {
                stopProgress();
            }
            
            if (!isCompletedSuccessfully()) {
                String mode = (installMode == INSTALL) ? "install" : "uninstall";
                String commandStr = Util.arrayToString(cmdArray, " ");
                logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + commandStr);
                logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + commandStr);
                try {
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(STORAGE_BUILDER_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
    
    /** check whether or not the un/installation was successful */
    private boolean isCompletedSuccessfully() {
        // For now return success
        return true;
    }
    
    /** Returns checksum for Storage directory in bytes. */
    public long getCheckSum() {
        //Need to set according to JDK version for 1.5.0 it is 64MB
        //for 1.4.2_06 42MB
        //return 67000000L;
        //just indexing src.zip no deep parsing
        return 16000000L;
        //return 44000000L;
    }
    
    /* Returns the required bytes table information.
     * @return required bytes table.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        RequiredBytesTable req = new RequiredBytesTable();
        
        nbInstallDir = resolveString("$P(absoluteInstallLocation)");
        logEvent(this, Log.DBG,"getRequiredBytes nbInstallDir: " + nbInstallDir);
        req.addBytes(nbInstallDir, getCheckSum());
        
        //#48948: We must set dirs here because init() is not run yet when getRequiredBytes
        //is called.
        tempPath = resolveString("$J(temp.dir)");
        logEvent(this, Log.DBG, "getRequiredBytes tempPath: " + tempPath);
        
        //Storage is first built to temp dir then it is copied to destination
        req.addBytes(tempPath, getCheckSum());
	logEvent(this, Log.DBG, "Total (not necessarily on one disk when tempdir is redirected) Mbytes = " + (req.getTotalBytes()>>20));
        logEvent(this, Log.DBG, "RequiredBytesTable: " + req);
        return req;
    }

    private char getWinSystemDrive() {
	char sysDrive = 'C';
        try {
            String sysLib=resolveString("$D(lib)"); // Resolve system library directory 
            logEvent(this, Log.DBG, "System Library directory is "+sysLib); 
            sysDrive=sysLib.charAt(0); // Resolve system drive letter
            logEvent(this, Log.DBG, "Found system drive is: " + String.valueOf(sysDrive));
        } catch(Exception ex) {
            Util.logStackTrace(this,ex);
            return 'C';
        }        
	return sysDrive;
    }
    
    
    private static int ESTIMATED_TIME = 3500; // tenths of seconds
    public int getEstimatedTimeToInstall() {
        return ESTIMATED_TIME;
    }
    
    public void startProgress() {
        progressThread = new ProgressThread();
        progressThread.start();
    }
    
    public void stopProgress() {
        logEvent(this, Log.DBG,"in progress stop");
        progressThread.interrupt();
        logEvent(this, Log.DBG,"interrupting ProgressThread ");
        //wait until progressThread is interrupted
        while (progressThread.isAlive()) {
            logEvent(this, Log.DBG,"Waiting for progressThread to die...");
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {}
        }
        logEvent(this, Log.DBG,"ProgressThread interrupted");
        progressThread.finish();
        //progressThread = null;
        
        Thread.currentThread().yield();
        logEvent(this, Log.DBG,"active Threads -> " + Thread.currentThread().activeCount());
    }

    private boolean setExecutable(String filename) {
	try {
	    FileService fileService = (FileService)getService(FileService.NAME);
	    if (fileService == null) {
		logEvent(this, Log.DBG, "FileService is null. Cannot set file as executable: " + filename);
		return false;
	    }
	    fileService.setFileExecutable(filename);
	} catch (Exception ex) {
            logEvent(this, Log.DBG, "Cannot set file as executable: " + filename
		     + "\nException: " + ex);
	    return false;
	}
	return true;
    }
    
    /** Create run script for storage builder from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createRunScript (String template, String scriptName, String mdrTempPath)
    throws Exception {
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("NB_PATH=")) {
                line = "NB_PATH=" + nbInstallDir;
            } else if (line.startsWith("JAVA_HOME=")) {
                line = "JAVA_HOME=" + Util.getJdkHome();
            } else if (line.startsWith("MDR_TMP_FILE=")) {
                line = "MDR_TMP_FILE=" + mdrTempPath;
            } else if (line.startsWith("LOGFILE=")) {
                line = "LOGFILE=" + uninstDir + File.separator 
                + "storagebuilder" + File.separator + "storagebuilder.log";
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not delete file: " + templateFile);
	}              
        writer.close();
	if (setExecutable(scriptFile.getAbsolutePath())) {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath() +
		     " is set as executable file.");
	    return true;
	}
	return false;
    }
    
    /** Create run script for storage builder from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createRunScriptWindows (String template, String scriptName, String mdrTempPath)
    throws Exception {
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);
        
        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET NB_PATH=")) {
                line = "SET NB_PATH=" + nbInstallDir;
            } else if (line.startsWith("SET JAVA_HOME=")) {
                line = "SET JAVA_HOME=" + Util.getJdkHome();
            } else if (line.startsWith("SET MDR_TMP_FILE=")) {
                line = "SET MDR_TMP_FILE=" + mdrTempPath;
            } else if (line.startsWith("SET LOGFILE=")) {
                line = "SET LOGFILE=" + uninstDir + File.separator 
                + "storagebuilder" + File.separator + "storagebuilder.log";
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
        writer.close();
	return true;
    }
    
    /** inner class to update the progress pane while installation */
    class ProgressThread extends Thread {
        private boolean loop = true;
        private  MutableOperationState mos;
        
        //progress bar related variables
        private long percentageCompleted = 0L;
        private long percentageStart = 0L;
        private long checksum = 0L;
        
        //status detail related variables
        //progress dots (...) after the path if it is being shown since s while
        private final int MIN_DOTS = 3;
        private int fileCounter = 0;
        private String lastPathShown;
        
        //status description related variables
        private File logFile;
        private boolean doStatusDescUpdate = true;
        
        //variables related to pkg unzipping before installation. Only for Solaris
        private boolean isUnzipping = false;
        private File unzipLog;
        private BufferedReader unzipLogReader = null;
        private long startTime = 0L;
        
        public ProgressThread() {
            this.mos = mutableOperationState;
            checksum = getCheckSum();
        }
        
        public void run() {
            int sleepTime = 1000;
            percentageStart = mos.getProgress().getPercentComplete();
            logEvent(this, Log.DBG,"Starting percentageStart: " + percentageStart);
            while (loop) {
                //logEvent(this, Log.DBG,"looping");
                try {
                    //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                    updateProgressBar();

                    sleepTime = 2000;
                    Thread.currentThread().sleep(sleepTime);
                    if (isCanceled()) return;
                } catch (InterruptedException ex) {
                    //ex.printStackTrace();
                    loop = false;
                    return;
                } catch (Exception ex) {
                    loop = false;
                    String trace = Util.getStackTrace(ex);
                    logEvent(this, Log.DBG, trace);
                    logEvent(this, Log.ERROR, trace);
                    return;
                }
            }
        }
        
        public void finish() {
            loop = false;
            Thread.currentThread().yield();
            mos.setStatusDetail("");
            logEvent(this, Log.DBG,"Finishing");;
            if(!mos.isCanceled()) {
                mos.setStatusDescription("");
                percentageCompleted = mos.getProgress().getPercentComplete();
                for (; percentageCompleted <= 100; percentageCompleted++) {
                    logEvent(this, Log.DBG,"percentageCompleted = " + percentageCompleted + " updateCounter " + mos.getUpdateCounter());
                    mos.updatePercentComplete(ESTIMATED_TIME, 1L, 100L);
                }
            }
            else {
                String statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installationCancelled)");
                mos.setStatusDescription(statusDesc);
                mos.getProgress().setPercentComplete(0);
            }
            
        }
        
        /** Check if the operation is canceled. If not yield to other threads. */
        private boolean isCanceled() {
            if(mos.isCanceled() && loop) {
                logEvent(this, Log.DBG,"MOS is cancelled");
                loop = false;
                runCommand.interrupt();
            }
            else {
                Thread.currentThread().yield();
            }
            
            return mos.isCanceled();
        }
        
        /** Updates the progress bar. */
        private void updateProgressBar() {
            if (isCanceled()) {
                return;
            }
            long size = Util.getFileSize(mdrTempDir);
            long perc = (size * (100 - percentageStart)) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted) {
                return;
            }
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            percentageCompleted = perc;
        }
        
    }
    
}
