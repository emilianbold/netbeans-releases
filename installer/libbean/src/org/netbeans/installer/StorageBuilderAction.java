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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class StorageBuilderAction extends ProductAction {
    
    //return code incase an error returns
    public static final int STORAGE_BUILDER_UNHANDLED_ERROR = -200;
    public static final String STORAGE_BUILDER_TEMP_DIR = "mdrtmpdir";
    public static final String STORAGE_BUILDER_TEMP_FILE = "mdrtmpfile";
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String ideClusterDir;
    private String platformClusterDir;
    private String sbDestDir;
    
    private String statusDesc = "";
    private String nbInstallDir = "";
    private String uninstDir = "";
    private String tempPath = "";
    
    private File mdrTempDir;

    private boolean success = false;
    
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
        tempPath = Util.getTmpDir();
        logEvent(this, Log.DBG,"TempPath: " + tempPath);
        
        ideClusterDir = resolveString("$L(org.netbeans.installer.Bundle,NetBeans.ideClusterDir)");
        platformClusterDir = resolveString("$L(org.netbeans.installer.Bundle,NetBeans.platformClusterDir)");
        sbDestDir = ideClusterDir + File.separator + "mdrstorage";
        
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
            File mdrDestDir = new File(nbInstallDir + File.separator + sbDestDir);
            if (mdrDestDir.exists()) {
                logEvent(this, Log.ERROR,"# # # # # # # #");
                logEvent(this, Log.ERROR,"Fatal error: Storage Builder destination directory already exists.");
                try {
                    ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                    ecservice.setExitCode(STORAGE_BUILDER_UNHANDLED_ERROR);
                } catch (Exception ex) {
                    logEvent(this, Log.ERROR, "Could not set exit code. ");
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
                    logEvent(this, Log.ERROR, "Could not set exit code. ");
                }
                return;
            }
            logEvent(this, Log.DBG,"Created destination dir for SB: " + mdrDestDir.getAbsolutePath());
            
            long startTime = System.currentTimeMillis();
            
            //Set system proeprties for sb
            System.getProperties().put("gjast.location",nbInstallDir + File.separator + ideClusterDir
            + File.separator + "modules" + File.separator + "ext" + File.separator + "gjast.jar");
            
            System.getProperties().put("mdr.filename",mdrTempDir.getAbsolutePath()
            + File.separator + STORAGE_BUILDER_TEMP_FILE);
            
            System.getProperties().put("preparse.files",nbInstallDir + File.separator + "_uninst"
            + File.separator + "storagebuilder" + File.separator + "preparse-files.txt");
                    
            ClassLoader parent = this.getClass().getClassLoader().getParent();
            URL [] classPath = getClassPath();
            URLClassLoader urlClassLoader = new URLClassLoader(classPath,parent);
            Class clazz = Class.forName("org.netbeans.lib.java.storagebuilder.Main",true,urlClassLoader);
            
            //Must be called so that Lookup finds org.netbeans.modules.masterfs.MasterURLMapper
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            
            Method method = null;
            Class[] params = new Class[] {String[].class, String.class};
            
            try {
                method = clazz.getMethod("prebuildJDKStorages", params);
            } catch (NoSuchMethodException exc) {
                logEvent(this, Log.ERROR, "Could not find SB method.");
                logEvent(this, Log.ERROR, exc);
                return;
            }
         
            startProgress();
            
            Object oResult = null;
            String destDir = nbInstallDir + File.separator + ideClusterDir + File.separator + "mdrstorage";
            String jdkHome;
            if (Util.isJDKAlreadyInstalled() && Util.isWindowsOS()) {
                jdkHome = Util.getInstalledJdk();
            } else {
                jdkHome = Util.getJdkHome();
            }
            Object [] args = new Object [] {new String [] {jdkHome}, destDir};
            try {
                oResult = method.invoke(null, args);
            } catch (IllegalAccessException exc) {
                logEvent(this, Log.ERROR, exc);
                return;
            } catch (IllegalArgumentException exc) {
                logEvent(this, Log.ERROR, exc);
                return;
            } catch (InvocationTargetException exc) {
                logEvent(this, Log.ERROR, exc);
                return;
            }
            
            logEvent(this, Log.DBG,"Storage Builder returned: " + oResult);
            
            long endTime = System.currentTimeMillis();
            logEvent(this, Log.DBG,"Storage builder took: " + (endTime - startTime) + "ms");
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
        } finally {
            stopProgress();
            
            //Delete temporary dir
            Util.deleteCompletely(mdrTempDir,support);
            logEvent(this, Log.DBG,"Deleted temporary dir for SB: " + mdrTempDir.getAbsolutePath());
        }
        logEvent(this, Log.DBG,"Running Storage Builder took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    /** Create array of URLs filed with classpath elements for running
     * storage builder.
     */
    private URL [] getClassPath () throws MalformedURLException {
        URL [] classPath;
        String s;
        //For Windows replace "\" in path by "/" to get correct URL.
        if (Util.isWindowsOS()) {
            s = nbInstallDir.replace('\\','/');
        } else {
            s = nbInstallDir;
        }
        logEvent(this, Log.DBG,"getClassPath Path prefix1: " + s);
        //Replace spaces in URL
        if (Util.isWindowsOS()) {
            //On Windows path starts with disk like C: so we must add additional slash
            s = "file:///" + s.replaceAll(" ","%20") + "/";
        } else {
            s = "file://" + s.replaceAll(" ","%20") + "/";
        }
        logEvent(this, Log.DBG,"getClassPath Path prefix2: " + s);
        try {
            classPath = new URL [] 
            {
            new URL(s + ideClusterDir + "/modules/org-netbeans-jmi-javamodel.jar"),
            new URL(s + ideClusterDir + "/modules/org-netbeans-modules-javacore.jar"),
            new URL(s + platformClusterDir + "/modules/org-netbeans-modules-masterfs.jar"),
            
            new URL(s + "_uninst/storagebuilder/storagebuilder.jar"),
            
            new URL(s + platformClusterDir + "/core/org-openide-filesystems.jar"),
            new URL(s + platformClusterDir + "/lib/org-openide-util.jar"),
            new URL(s + platformClusterDir + "/lib/org-openide-modules.jar"),
            
            new URL(s + platformClusterDir + "/modules/org-openide-actions.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-awt.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-dialogs.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-execution.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-explorer.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-io.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-loaders.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-nodes.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-options.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-text.jar"),
            new URL(s + platformClusterDir + "/modules/org-openide-windows.jar"),
            
            new URL(s + ideClusterDir + "/modules/org-netbeans-api-java.jar"),
            new URL(s + ideClusterDir + "/modules/javax-jmi-model.jar"),
            new URL(s + ideClusterDir + "/modules/javax-jmi-reflect.jar"),
            new URL(s + ideClusterDir + "/modules/org-netbeans-api-mdr.jar"),
            new URL(s + ideClusterDir + "/modules/org-netbeans-modules-mdr.jar"),
            new URL(s + ideClusterDir + "/modules/org-netbeans-modules-jmiutils.jar"),
            new URL(s + ideClusterDir + "/modules/org-netbeans-modules-projectapi.jar"),
            new URL(s + ideClusterDir + "/modules/org-netbeans-modules-classfile.jar"),
            new URL(s + ideClusterDir + "/modules/ext/java-parser.jar")
            };
        } catch (MalformedURLException exc) {
            throw exc;
        }
        //Dump class path
        for (int i = 0; i < classPath.length; i++) {
            logEvent(this, Log.DBG,"cl[" + i + "]: " + classPath[i].toString());
        }
        return classPath;
    }
    
    /** Does cleaning. */
    public void uninstall(ProductActionSupport support) {
        init(support);
        installMode = UNINSTALL;
        
        String fileName;
        //Delete directory created by storage builder during install
        fileName = nbInstallDir + File.separator + sbDestDir;
        logEvent(this, Log.DBG,"Deleting: " + fileName);
        Util.deleteCompletely(new File(fileName),support);
        
        fileName = uninstDir + File.separator + "storagebuilder" + File.separator + "storagebuilder.log";
        logEvent(this, Log.DBG,"Deleting: " + fileName);
        Util.deleteCompletely(new File(fileName),support);
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
        tempPath = Util.getTmpDir();
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
            long sleepTime = 1000L;
            percentageStart = mos.getProgress().getPercentComplete();
            logEvent(this, Log.DBG,"Starting percentageStart: " + percentageStart);
            while (loop) {
                //logEvent(this, Log.DBG,"looping");
                try {
                    //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                    updateProgressBar();
                    Thread.currentThread().sleep(sleepTime);
                    if (isCanceled()) {
                        return;
                    }
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
            logEvent(this, Log.DBG,"Finished loop loop:" + loop);
        }
        
        public void finish() {
            loop = false;
            mos.setStatusDetail("");
            logEvent(this, Log.DBG,"Finishing");;
            if (!mos.isCanceled()) {
                mos.setStatusDescription("");
                percentageCompleted = mos.getProgress().getPercentComplete();
                for (; percentageCompleted <= 100; percentageCompleted++) {
                    logEvent(this, Log.DBG,"percentageCompleted = " + percentageCompleted + " updateCounter " + mos.getUpdateCounter());
                    mos.updatePercentComplete(ESTIMATED_TIME, 1L, 100L);
                }
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
