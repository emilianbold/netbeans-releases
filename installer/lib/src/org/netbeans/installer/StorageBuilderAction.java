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
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String statusDesc = "";
    private String nbInstallDir = "";
    private String uninstDir = "";
    private String tempDir = "";

    private boolean success = false;
    
    private RunCommand runCommand = new RunCommand();
    
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public StorageBuilderAction () {
    }
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass(Util.class.getName());
            support.putClass("org.netbeans.installer.StorageBuilderAction$ProgressThread");
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init (ProductActionSupport support)
    throws Exception {
	/*
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        instDirPath = resolveString((String)pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation")); */
        nbInstallDir = resolveString("$P(absoluteInstallLocation)");
        logEvent(this, Log.DBG,"nbInstallDir: " + nbInstallDir);
        uninstDir = nbInstallDir + File.separator + "_uninst";
        logEvent(this, Log.DBG,"uninstDir: " + uninstDir);
        logEvent(this, Log.DBG,"jdkHome: " + Util.getJdkHome());
        tempDir = resolveString("$J(temp.dir)");
        logEvent(this, Log.DBG,"Tempdir: " + tempDir);
        
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
            
	    if (Util.isWindowsOS()) {
		String runScript = uninstDir + File.separator 
                + "run-storage-builder-windows.template";
		createRunScriptWindows(runScript, "run-storage-builder-windows.bat");
	    } else {
		String runScript = uninstDir + File.separator 
                + "run-storage-builder-unix.template";
		createRunScript(runScript,"run-storage-builder-unix.sh");
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
            String execPath   = uninstDir + File.separator + execName;
            
	    // Put the command and arguments together for windows
            if (Util.isWindowsOS()) {
                cmdArray[0] = execPath;
            } else {
                cmdArray[0] = execPath;
            }
            
	    // Invoke the correct command
            logEvent(this, Log.DBG,"# # # # # # # #");
            logEvent(this, Log.DBG,"Start Invoking Storage Builder: cmdArray -> " + Arrays.asList(cmdArray).toString());
            runCommand(cmdArray, null, support);
            
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"Running Storage Builder took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    /** Delete given file/folder completely. It is called recursively when necessary.
     * @file - name of file/folder to be deleted
     */
    private void deleteCompletely (File file) {
        if (file.isDirectory()) {
            //Delete content of folder
            File [] fileArr = file.listFiles();
            for (int i = 0; i < fileArr.length; i++) {
                if (fileArr[i].isDirectory()) {
                    deleteCompletely(fileArr[i]);
                }
                logEvent(this, Log.DBG,"Delete file: " + fileArr[i].getPath());
                if (!fileArr[i].delete()) {
                    logEvent(this, Log.DBG,"Cannot delete file: " + fileArr[i].getPath());
                }
            }
        }
        logEvent(this, Log.DBG,"Delete file: " + file.getPath());
        if (!file.delete()) {
            logEvent(this, Log.DBG,"Cannot delete file: " + file.getPath());
        }
    }
    
    /** Does nothing. */
    public void uninstall(ProductActionSupport support) {
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
            
            if (Util.isWindowsOS()) {
                //UGLY HACK: make sure there are enough time elapsed before starting to flush
                int ms = (installMode == INSTALL) ? 2000 : 4500;
                Thread.currentThread().sleep(ms);
            }
            
            
            //int status;
            if (Util.isWindowsNT() || Util.isWindows98()) {
                //HACK: don't flush for NT or 98
            }
            else {
                logEvent(this, Log.DBG,"Flushing ...!");
                runCommand.flush();
                logEvent(this, Log.DBG,"Flushing done!");
                
            }
            
            int status = runCommand.getReturnStatus();
            
            logEvent(this, Log.DBG,"Return status: " + status);
            
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
            
            logEvent(this, Log.DBG,"Flushing 2...!");
            runCommand.flush();
            logEvent(this, Log.DBG,"Flushing 2 done!");
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
        return 67000000L;
        //return 44000000L;
    }
    
    /* Returns the required bytes table information.
     * @return required bytes table.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        //#48948: We must set dirs here because init() is not run yet when getRequiredBytes
        //is called.
        tempDir = resolveString("$J(temp.dir)");
        
        RequiredBytesTable req = new RequiredBytesTable();
        logEvent(this, Log.DBG, "tempDir: " + tempDir);
        
        //Storage is first built to temp dir then it is copied to destination
        req.addBytes(tempDir, 50000000L);
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
    private boolean createRunScript (String template, String scriptName)
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
            } else if (line.startsWith("MDR_TMPDIR=")) {
                line = "MDR_TMPDIR=" + tempDir + File.separator + "mdrtmp" + File.separator + "mdr";
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
    private boolean createRunScriptWindows (String template, String scriptName)
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
            } else if (line.startsWith("JAVA_HOME=")) {
                line = "SET JAVA_HOME=" + Util.getJdkHome();
            } else if (line.startsWith("MDR_TMPDIR=")) {
                line = "SET MDR_TMPDIR=" + tempDir + File.separator + "mdrtmp" + File.separator + "mdr";
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
        private File storageDir;
        
        //progress bar related variables
        private long percentageCompleted = 0L;
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
            storageDir = new File(tempDir + File.separator + "mdrtmp");
            checksum = getCheckSum();
        }
        
        public void run() {
            int sleepTime = 1000;
            while (loop) {
                logEvent(this, Log.DBG,"looping");
                try {
                    //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                    updateProgressBar();

                    sleepTime = 1200;
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
            long size = Util.getFileSize(storageDir);
            long perc = (size * 100) / checksum;
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
