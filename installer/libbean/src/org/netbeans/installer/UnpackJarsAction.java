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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import java.io.File;

public class UnpackJarsAction extends ProductAction {
    
    //return code incase an error returns
    public static final int UNPACK_JARS_UNHANDLED_ERROR = -400;
    
    public static final int UNPACK_JARS_MD5_ERROR = -401;

    private static final String JARS_CATALOG_FILE = "packedjars.xml";
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String statusDesc = "";
    private String nbInstallDir = "";
    private String rootInstallDir = "";
    private String uninstDir = "";
    
    private boolean success = false;
    
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public UnpackJarsAction () {
    }
    
    public void build (ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass("org.netbeans.installer.RunCommand$StreamAccumulator");
            support.putClass(Util.class.getName());
            support.putClass(UnpackJarsAction.ProgressThread.class.getName());
            support.putClass(UnpackJars.class.getName());
            support.putClass(UnpackJars.ErrorCatcher.class.getName());
            support.putClass(UnpackJars.JarItem.class.getName());
            support.putClass(UnpackJars.ProgressInfo.class.getName());
            support.putClass(XMLUtil.class.getName());
            support.putClass(XMLUtil.CustomEntityResolver.class.getName());
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init (ProductActionSupport support) {
        rootInstallDir = resolveString("$P(absoluteInstallLocation)");
        if (Util.isMacOSX()) {
            nbInstallDir = rootInstallDir + File.separator 
            + resolveString("$L(org.netbeans.installer.Bundle,Product.nbLocationBelowInstallRoot)");
        } else {
            nbInstallDir = rootInstallDir;
        }
        logEvent(this, Log.DBG,"nbInstallDir: " + nbInstallDir);
        uninstDir = nbInstallDir + File.separator + "_uninst";
        logEvent(this, Log.DBG,"uninstDir: " + uninstDir);
        
        mutableOperationState = support.getOperationState();

    }
    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.unpackJarsMessage)")
	+ " "
	+ resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.waitMessage)") ;
        
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = INSTALL;
            
            long startTime = System.currentTimeMillis();

            File catalog = new File(uninstDir, JARS_CATALOG_FILE);
            if (!catalog.exists()) {
                try {
                    logEvent(this, Log.ERROR,"# # # # # # # #");
                    logEvent(this, Log.ERROR,"Fatal error: Cannot find jar catalog file: " + catalog);
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Could not set exit code.");
                }
                return;
            }
            UnpackJars unpackJars = new UnpackJars();
            unpackJars.init(nbInstallDir,this);
            boolean ret;
            ret = unpackJars.parseCatalog(this, catalog.getAbsolutePath());
            if (!ret) {
                try {
                    logEvent(this, Log.ERROR,"# # # # # # # #");
                    logEvent(this, Log.ERROR,"Fatal error: Cannot parse jar catalog.");
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Could not set exit code.");
                }
                return;
            }

            startProgress(unpackJars);

            FileService fileService = null;
            try {
                fileService = (FileService) getService(FileService.NAME);
                if (fileService == null) {
                    logEvent(this,Log.ERROR,"Error: Cannot get FileService.");
                }
            } catch (Exception ex) {
                logEvent(this,Log.ERROR,"Error: Cannot get FileService.");
                logEvent(this,Log.ERROR,"Exception: " + ex.getMessage());
                Util.logStackTrace(this,ex);
            }
            if (fileService == null) {
                try {
                    logEvent(this,Log.ERROR,"# # # # # # # #");
                    logEvent(this,Log.ERROR,"Fatal error: Cannot get file service.");
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this,Log.ERROR,"Could not set exit code.");
                }
                return;
            }

            int retVal = unpackJars.unpackJars(this, fileService);
            if (retVal != 0) {
                try {
                    logEvent(this, Log.ERROR,"# # # # # # # #");
                    logEvent(this, Log.ERROR,"Fatal error: Cannot unpack jars.");
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(retVal);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Could not set exit code.");
                }
                return;
            }
            
            long endTime = System.currentTimeMillis();

            logEvent(this, Log.DBG,"Unpack Jars took: " + (endTime - startTime) + "ms");
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        } finally {
            stopProgress();
        }
    }
    
    /** Does cleaning of unpacked jars. */
    public void uninstall(ProductActionSupport support) {
        init(support);
        installMode = UNINSTALL;
        //Perform work
        File catalog = new File(uninstDir, JARS_CATALOG_FILE);
        if (!catalog.exists()) {
            try {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Cannot find jar catalog file: " + catalog);
                ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
            } catch (Exception ex) {
                logEvent(this, Log.ERROR, "Could not set exit code.");
            }
            return;
        }
        UnpackJars unpackJars = new UnpackJars();
        unpackJars.init(nbInstallDir,this);
        boolean ret;
        ret = unpackJars.parseCatalog(this, catalog.getAbsolutePath());
        if (!ret) {
            try {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Cannot parse jar catalog.");
                ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                ecservice.setExitCode(UNPACK_JARS_UNHANDLED_ERROR);
            } catch (Exception ex) {
                logEvent(this, Log.ERROR, "Could not set exit code.");
            }
            return;
        }
        unpackJars.deleteJars(this);
        catalog.delete();
    }
    
    /**
     * Returns difference between total size of uncompressed jars and total size
     * of compressed jars size to show correct total install size on Preview panel.
     * Total size of compressed jars is already included in beanCoreIDE.
     */
    public long getCheckSum() {
        return 72000000L;
    }

    /* Returns the required bytes table information.
     * @return required bytes table.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        RequiredBytesTable req = new RequiredBytesTable();
        
        nbInstallDir = resolveString("$P(absoluteInstallLocation)");
        logEvent(this,Log.DBG,"getRequiredBytes nbInstallDir: " + nbInstallDir);
        req.addBytes(nbInstallDir, getCheckSum());
        
        return req;
    }

    private static int ESTIMATED_TIME = 3500; // tenths of seconds

    public int getEstimatedTimeToInstall() {
        return ESTIMATED_TIME;
    }
    
    public void startProgress (UnpackJars unpackJars) {
        progressThread = new ProgressThread(unpackJars);
        progressThread.start();
    }
    
    public void stopProgress() {
        //Method startProgress() must be called first
        if (progressThread == null) {
            return;
        }
        logEvent(this, Log.DBG,"in progress stop");
        progressThread.finish();
        logEvent(this, Log.DBG,"Finishing ProgressThread");
        //wait until progressThread is interrupted
        while (progressThread.isAlive()) {
            logEvent(this, Log.DBG,"Waiting for progressThread to die...");
            try {
                Thread.currentThread().sleep(1000);
            } catch (Exception ex) {}
        }
        logEvent(this, Log.DBG,"ProgressThread finished");
        progressThread = null;
        logEvent(this, Log.DBG,"active Threads -> " + Thread.currentThread().activeCount());
    }

    /** Inner class to update the progress pane while installation */
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

        private UnpackJars unpackJars;
        
        public ProgressThread (UnpackJars unpackJars) {
            this.mos = mutableOperationState;
            this.unpackJars = unpackJars;
            checksum = unpackJars.totalOriginalSize;
        }
        
        public void run() {
            long sleepTime = 500L;
            percentageStart = mos.getProgress().getPercentComplete();
            logEvent(this, Log.DBG,"Starting percentageStart: " + percentageStart);
            while (loop) {
                try {
                    updateProgressBar();
                    Thread.currentThread().sleep(sleepTime);
                    if (isCanceled()) {
                        return;
                    }
                } catch (InterruptedException ex) {
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
            logEvent(this, Log.DBG,"Finished loop loop:" + loop);
        }
        
        public void finish() {
            loop = false;
            mos.setStatusDetail("");
            logEvent(this, Log.DBG,"Finishing");;
            if (!mos.isCanceled()) {
                mos.setStatusDescription("");
            } else {
                String statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installationCancelled)");
                mos.setStatusDescription(statusDesc);
                mos.getProgress().setPercentComplete(0);
            }
            
        }
        
        /** Check if the operation is canceled. */
        private boolean isCanceled() {
            if (mos.isCanceled() && loop) {
                logEvent(this, Log.DBG,"MOS is cancelled");
                loop = false;
            }
            
            return mos.isCanceled();
        }
        
        /** Updates the progress bar. */
        private void updateProgressBar() {
            if (isCanceled()) {
                return;
            }
            long size = unpackJars.getProgressInfo().getCompletedBytes();
            long perc = (size * (100 - percentageStart)) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted) {
                return;
            }
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            mos.setStatusDetail(unpackJars.getProgressInfo().getFileName());
            percentageCompleted = perc;
        }
        
    }
    
}
