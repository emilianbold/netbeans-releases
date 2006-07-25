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

package org.netbeans.installer;

import com.installshield.product.ProductAction;
import com.installshield.product.ProductActionSupport;
import com.installshield.product.ProductBuilderSupport;
import com.installshield.product.ProductException;
import com.installshield.product.RequiredBytesTable;
import com.installshield.product.service.desktop.DesktopService;
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.platform.win32.Win32RegistryService;
import com.installshield.wizard.service.MutableOperationState;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.exitcode.ExitCodeService;
import com.installshield.wizard.service.file.FileService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;

public class InstallApplicationServerAction extends ProductAction {
    
    //return code incase an error returns
    public static final int AS_UNHANDLED_ERROR = -500;
    
    protected static int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String statusDesc = "";
    private String instDirPath;
    private String imageDirPath;
    /** Location of JDK on which installer is running. */
    private String jdkDirPath;
    
    private String rootInstallDir;
    /** NetBeans installation directory. */
    private String nbInstallDir;
    
    private boolean success = false;
    private boolean invalidPortFound = false;

    // Port info
    private String adminPort = null;
    private String webPort = null;
    private String httpsPort = null;
    
    private String tmpDir = null;

    private RunCommand runCommand = new RunCommand();
    
    //thread for updating the progress pane
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public InstallApplicationServerAction() {}
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass("org.netbeans.installer.RunCommand$StreamAccumulator");
            support.putClass(FileComparator.class.getName());
            support.putClass(Util.class.getName());
            support.putClass(NetUtils.class.getName());
            support.putClass(InstallApplicationServerAction.ProgressThread.class.getName());
            support.putRequiredService(Win32RegistryService.NAME);
            support.putRequiredService(DesktopService.NAME);
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init(ProductActionSupport support) throws Exception {
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        
        rootInstallDir = resolveString((String)pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation"));
        if (Util.isMacOSX()) {
            nbInstallDir = rootInstallDir + File.separator 
            + resolveString("$L(org.netbeans.installer.Bundle,Product.nbLocationBelowInstallRoot)");
        } else {
            nbInstallDir = rootInstallDir;
        }
        
        instDirPath = rootInstallDir + File.separator + "_uninst" + File.separator + "jboss";
        logEvent(this, Log.DBG,"instDirPath: "+ instDirPath);
        
        imageDirPath  = Util.getASInstallDir();
        logEvent(this, Log.DBG,"imageDirPath: "+ imageDirPath);
        
        //Set JDK selected in JDKSearchPanel. It will be used to run AS Installer.
        jdkDirPath = Util.getJdkHome();
        logEvent(this, Log.DBG,"jdkDirPath: "+ jdkDirPath);

        tmpDir = Util.getTmpDir();
       
        mutableOperationState = support.getOperationState();
    }

    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installMessage,"
        + "$L(org.netbeans.installer.Bundle, AS.shortName))")
        + " " + resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.waitMessage)");
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = INSTALL;
            
            mutableOperationState.setStatusDetail(imageDirPath);
            
            String cmdArray[] = new String[6];
            // To be cleaned up later
            cmdArray[0] = jdkDirPath + File.separator + "bin" + File.separator + "java";
            cmdArray[1] = "-jar";
            cmdArray[2] = instDirPath + File.separator 
            + resolveString("$L(org.netbeans.installer.Bundle, AS.installer)");
            cmdArray[3] = "-installGroup";
            cmdArray[4] = "ejb3";
            cmdArray[5] = "installpath=" + imageDirPath;
            
            logEvent(this, Log.DBG,"* * * * RunCommand Start " );
            File workDir = new File(instDirPath);
            runCommand(cmdArray, workDir, support);
            logEvent(this, Log.DBG,"* * * * RunCommand End " );
            
            statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.cleanInstDir,"
            + "$L(org.netbeans.installer.Bundle, AS.shortName))");
            mutableOperationState.setStatusDescription(statusDesc);
            
            //Delete JBoss installer
            File jbInstaller = new File(cmdArray[2]);
            jbInstaller.delete();
            logEvent(this, Log.DBG,"Deleted file: " + jbInstaller);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        
        logEvent(this, Log.DBG,"Appserver installation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    public void uninstall(ProductActionSupport support) {
        logEvent(this, Log.DBG,"Uninstalling -> ");
        //statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.uninstallWait)");
        //support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = UNINSTALL;
            //Delete JBoss installer
            File dir = new File(instDirPath);
            Util.deleteDirectory(dir);
            logEvent(this, Log.DBG,"Deleted folder: " + dir);
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
    }
    
    //threads should only run in install mode until ISMP supports them
    private void runCommand(String[] cmdArray, File workDir, ProductActionSupport support) throws Exception{
        boolean doProgress = !(Boolean.getBoolean("no.progress"));
        logEvent(this, Log.DBG,"doProgress -> " + doProgress);
        
        //mutableOperationState = support.getOperationState();
        
        logEvent(this, Log.DBG,"cmdArray -> " + Arrays.asList(cmdArray).toString());
        try {
            runCommand.execute(cmdArray, null, workDir);
            
            if ((installMode == INSTALL) && doProgress) {
                startProgress();
            }
            
            runCommand.waitFor();
            
            logEvent(this, Log.DBG,runCommand.print());
            
            int status = runCommand.getReturnStatus();
            logEvent(this, Log.DBG, "status code = " + status + " which is " + ((status == 0) ? "successful" : "unsuccessful")); 
            
            if (status != 0) {
                String mode = (installMode == INSTALL) ? "install" : "uninstall";
                String command = Util.arrayToString(cmdArray, " ");
                logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + command);
                logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + command);

                //InstallerExceptions.setErrors(true);
                setAppserverExitCode(AS_UNHANDLED_ERROR);
            }
            
            if((installMode == INSTALL) && doProgress) {
                stopProgress();
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private void setAppserverExitCode(int code) {
          try {
              ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
              ecservice.setExitCode(code);
          } catch (Exception ex) {
              logEvent(this, Log.ERROR, "Couldn't set exit code. "); 
          }
    }
    
    /**
     * List the files which shouldn't be cleaned up after installation
     *
     * @param  pathname  The abstract pathname to be tested
     * @return  <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    /*public boolean accept(File pathname) {
        String path = pathname.getAbsolutePath();
        if (installMode == INSTALL) {
            if ( path.equals(instDirPath + File.separator + UNINSTALL_SH)
            || path.equals(instDirPath + File.separator + UNINSTALL_BAT)
            || path.equals(instDirPath + File.separator + "uninstall.dat")
            || path.equals(instDirPath + File.separator + "uninstall.bin")
            || path.equals(instDirPath + File.separator + "uninstall.exe")
            || path.equals(instDirPath + File.separator + "_jvm"))
                return false;  
        }
        else if (installMode == UNINSTALL) {
            if ( path.equals(imageDirPath + File.separator + "uninstall.log"))
                return false;  
        }
        
        return true;
    }*/
    
    /** Removes the Appserver entry in Add/Remove Programs panel */
    public void removeAppserverFromAddRemovePrograms() {
        if (Util.isWindowsOS()) {
            logEvent(this, Log.DBG,"Updating Add/Remove Programs ...");
            try {
                Win32RegistryService regserv = (Win32RegistryService) getService(Win32RegistryService.NAME);
                regserv.deleteKey(Win32RegistryService.HKEY_LOCAL_MACHINE,
                "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
                "Sun Java System Application Server Platform Edition", false);
            } catch (ServiceException se) {
                se.printStackTrace();
            }
        }
    }
    
    /** Returns checksum for appserver directory in bytes */
    public long getCheckSum() {
        return 55000000L;
    }
    
    /* Returns the required bytes table information for application server.  
     * @return required bytes table for application server.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        String asInstallDirPath = Util.getASInstallDir();
        logEvent(this, Log.DBG,"imageDirPath -> " + asInstallDirPath);
        
        RequiredBytesTable req = new RequiredBytesTable();
        req.addBytes(asInstallDirPath, getCheckSum());
        
        return req;
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
	int count = 0;
        while (progressThread.isAlive() && count < 20) {
            logEvent(this, Log.DBG,"Waiting for progressThread to die...");
            // Sometimes the 1st interrupt is not good enough
	    if (count == 10)
		progressThread.interrupt();
	    else if (count == 15) {
                // now try something else instead of interrupt
		progressThread.setLoop(false);
	    }

            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {}
	    count++;
        }
        logEvent(this, Log.DBG,"ProgressThread interrupted");
        progressThread.finish();
        //progressThread = null;
        
        Thread.currentThread().yield();
        logEvent(this, Log.DBG,"active Threads -> " + Thread.currentThread().activeCount());
    }
    
    /** inner class to update the progress pane while installation */
    class ProgressThread extends Thread {
        private boolean loop = true;
        private  MutableOperationState mos;
        private File appserverDir;
        
        //progress bar related variables
        private long percentageCompleted = 0L;
        private long checksum = 0L;
        
        //status detail related variables
        //progress dots (...) after the path if it is being shown since s while
        private final FileComparator fileComp = new FileComparator();
        private final int MIN_DOTS = 3;
        private int fileCounter = 0;
        private String lastPathShown;
        
        //status description related variables
        private File logFile;
        private BufferedReader logFileReader = null;
        private boolean doStatusDescUpdate = true;
        
        //variables related to pkg unzipping before installation. Only for Solaris
        private boolean isUnzipping = false;
        private File unzipLog;
        private BufferedReader unzipLogReader = null;
        private long startTime = 0L;
        
        public ProgressThread() {
            this.mos = mutableOperationState;
            lastPathShown = imageDirPath;
            appserverDir = new File(imageDirPath);
            logFile = new File(instDirPath, "as-install.log");
            checksum = getCheckSum();
            
            if (Util.isSunOS()) {
                unzipLog = new File(instDirPath, "unzip.log");
                isUnzipping = true;
                startTime = System.currentTimeMillis();
                String statusDesc2 = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.unzippingPackages)");
                mos.setStatusDescription(statusDesc + "\n" + statusDesc2);
            }
        }
        
	public void setLoop(boolean b) {
	    loop = b;
	}

        public void run() {
            long sleepTime = 1000L;
            while (loop) {
                //logEvent(this, Log.DBG,"looping");
                try {
                    if (appserverDir.exists()) {
                        //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                        updateProgressBar();
                        //logEvent(this, Log.DBG,"going 2 updateStatusDetail");
                        updateStatusDetail();
                        //logEvent(this, Log.DBG,"going 2 updateStatusDescription");
                        if (doStatusDescUpdate) {
                            updateStatusDescription();
                        }
                    } else {
                        if (isUnzipping) {
                            updateUnzippingInfo();
                        } else {
                            updateStatusDetail();
                        }
                    }
                    Thread.currentThread().sleep(sleepTime);
                    if (isCanceled()) {
                        return;
                    }
                } catch (InterruptedIOException ex) {
                    //ex.printStackTrace();
                    loop = false;
                    return;
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
            logEvent(this, Log.DBG,"Finishing");
            if(!mos.isCanceled()) {
                mos.setStatusDescription("");
                /*for (; percentageCompleted <= 100; percentageCompleted++) {
                    logEvent(this, Log.DBG,"percentageCompleted = " + percentageCompleted + " updateCounter " + mos.getUpdateCounter());
                    mos.updatePercentComplete(ESTIMATED_TIME, 1L, 100L);
                }*/
            } else {
                String statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installationCancelled)");
                mos.setStatusDescription(statusDesc);
                mos.getProgress().setPercentComplete(0);
            }
            
            stopReader(logFileReader);
        }
        
        /**check if the operation is canceled. If not yield to other threads.*/
        private boolean isCanceled() {
            if (mos.isCanceled() && loop) {
                logEvent(this, Log.DBG,"MOS is cancelled");
                loop = false;
                runCommand.interrupt();
            } else {
                Thread.currentThread().yield();
            }
            
            return mos.isCanceled();
        }
               
        /** Updates the progress bar*/
        private void updateProgressBar() {
            if (isCanceled()) {
                return;
            }

            long size = Util.getFileSize(appserverDir);
            long perc = (size * 100) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted) {
                return;
            }
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            percentageCompleted = perc;
        }
        
        /** Updates the status detail*/
        public void updateStatusDetail() {
            if (isCanceled()) {
                return;
            }
            if (!appserverDir.exists()) {
                mos.setStatusDetail(getDisplayPath(lastPathShown));
                logEvent(this, Log.DBG,"StatusDetailThread-> " + lastPathShown + " NOT created yet");
                return;
            }
            String recentFilePath = fileComp.getMostRecentFile(appserverDir).getAbsolutePath();
            logEvent(this, Log.DBG,"StatusDetailThread-> " + recentFilePath + "  MODIFIED!!!");
            mos.setStatusDetail(getDisplayPath(recentFilePath));
        }
        
        //progress dots (...) after the path if it is being shown since s while
        private String getDisplayPath(String recentFilePath) {
            try {
                String displayStr = recentFilePath;
                int max_len = 60;
                if (displayStr.length() > max_len) {
                    String fileName = displayStr.substring(displayStr.lastIndexOf(File.separatorChar));
                    //If filename is too long cut it
                    if (fileName.length() > 40) {
                        fileName = fileName.substring(fileName.length() - 40);
                    }
                    displayStr = displayStr.substring(0, max_len - fileName.length() - 4)
                    + "...."
                    + fileName;
                }
                if (!recentFilePath.equalsIgnoreCase(lastPathShown)) {
                    lastPathShown = recentFilePath;
                    fileCounter = 0;
                    return displayStr;
                } else if (fileCounter < MIN_DOTS) {
                    fileCounter++;
                    return displayStr;
                }
                fileCounter = Math.max(fileCounter % 10, MIN_DOTS);
                char [] array = new char[fileCounter];
                Arrays.fill(array, '.');
                fileCounter++;
                return  displayStr + " " + String.valueOf(array);
            } catch (Exception ex) {
                String trace = Util.getStackTrace(ex);
                logEvent(this, Log.DBG, trace);
                logEvent(this, Log.ERROR, trace);                          
                return recentFilePath;
            }
        }
        
        public void updateStatusDescription() throws Exception {
            if (isCanceled()) {
                return;
            }
            try{
                if (logFileReader == null) {
                    if (!logFile.exists()) {
                        logEvent(this, Log.DBG,"StatusDescriptionThread-> Logfile NOT created yet");
                        return;
                    }
                    logEvent(this, Log.DBG,"StatusDescriptionThread-> Logfile CREATED!!!");
                    logFileReader = new BufferedReader(new FileReader(logFile));
                    success = true;
                }
                
                if (logFileReader.ready()) {
                    String line = null;
                    while ((line = logFileReader.readLine()) != null) {
                        logEvent(this, Log.DBG,"line = " + line);
                        //check if there is an error
                        if (success && (line.toLowerCase().indexOf("error") != -1)) {
                            success = false;
                            mos.setStatusDescription(statusDesc + "\n" + line);
                        }
                    }                    
                }
            } catch (Exception ex) {
                mos.setStatusDescription("");
                stopReader(logFileReader);
                doStatusDescUpdate = false;
                if ((ex instanceof InterruptedIOException) 
                   || (ex instanceof InterruptedException)) {
                    throw ex;
                }
            }
        }
        
        public void updateUnzippingInfo() {
            if (isCanceled()) {
                return;
            }
            try {
                if (unzipLogReader == null) {
                    if (!unzipLog.exists()) {
                        return;
                    }
                    unzipLogReader = new BufferedReader(new FileReader(unzipLog));
                }
                
                if (unzipLogReader.ready()) {
                    String line = null;
                    while ((line = unzipLogReader.readLine()) != null) {
                        if (line.equalsIgnoreCase("DONE")) {
                            throw new Exception();   
                        } else {
                            mos.setStatusDetail(line);
                        }
                    }                   
                }
            } catch (Exception ex) {
                isUnzipping = false;
                mos.setStatusDetail("");
                stopReader(unzipLogReader);
                mos.setStatusDescription(statusDesc);
            }
        }
 
        private void stopReader(BufferedReader reader) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
                reader = null;
            } 
        }
    }
    
}
