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

import java.io.*;
import java.net.Socket;
import java.util.*;
import com.installshield.product.*;
import com.installshield.product.service.desktop.DesktopService;
import com.installshield.product.service.product.*;
import com.installshield.util.*;
import com.installshield.wizard.*;
import com.installshield.wizard.platform.win32.*;
import com.installshield.wizard.service.file.*;
import com.installshield.wizard.service.MutableOperationState;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizard.service.exitcode.ExitCodeService;
// import com.installshield.wizard.service.file.FileService;

public class InstallJ2sdkAction extends ProductAction implements FileFilter {
    
    //return code incase an error returns
    public static final int J2SDK_UNHANDLED_ERROR = -250;
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String statusDesc = "";
    private String j2seInstallDir = "";
    private String tempDir = "";
    
    private boolean success = false;
    
    private RunCommand runCommand = new RunCommand();
    
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public InstallJ2sdkAction() {
    }
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass(FileComparator.class.getName());
            support.putClass(Util.class.getName());
            support.putClass("org.netbeans.installer.InstallJ2sdkAction$ProgressThread");
            support.putRequiredService(Win32RegistryService.NAME);
        } catch (Exception ex){
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    private void init(ProductActionSupport support)
    throws Exception{
	/*
        ProductService pservice = (ProductService)getService(ProductService.NAME);
        String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
        instDirPath = resolveString((String)pservice.getProductBeanProperty(productURL,null,"absoluteInstallLocation")); */
	j2seInstallDir = (String) System.getProperties().get("j2seInstallDir");
        
        tempDir = resolveString("$J(temp.dir)");
        logEvent(this, Log.DBG,"Tempdir: " + tempDir);
        
        mutableOperationState = support.getOperationState();
    }
    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.installMessage," +
        "$L(org.netbeans.installer.Bundle,JDK.shortName))")
	+ " "
	+ resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.waitMessage)") ;
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = INSTALL;
            
            String uninstDir  = j2seInstallDir + File.separator + "_uninst";
	    if (!Util.isWindowsOS()) {
		String j2seInstallScript = uninstDir + File.separator 
                + "j2se-install.template";
		createInstallScript(j2seInstallScript, "custom-install");
		String j2seUninstallScript = uninstDir + File.separator 
		+ "j2se-uninstall.template";
		createUninstallScript(j2seUninstallScript, "uninstall.sh");
	    } else {
		String j2seInstallScript = uninstDir + File.separator 
                + "custom-install.template";
		createInstallScriptWindows(j2seInstallScript, "custom-install.bat");
		String j2seUninstallScript = uninstDir + File.separator 
		+ "custom-uninstall.template";
		createUninstallScriptWindows(j2seUninstallScript, "custom-uninstall.bat");
            }
            String execName;
            String driveName;
            int paramCount;

	    // We'll be running the j2se installer in silent mode so most 
	    // of this code is putting together the path and script or exe to
	    // run. Determine the script or exe to run.
            if (Util.isWindowsNT() || Util.isWindows98()) {
                execName =  findJDKWindowsInstaller(j2seInstallDir);
                paramCount = 1;
                driveName = "";                
            }
            else if (Util.isWindowsOS()) {
                driveName = j2seInstallDir.substring(0, j2seInstallDir.indexOf(File.separator));
                execName = "custom-install.bat";
                paramCount = 5;
            }
            else {
                execName = "custom-install";
                driveName = "";
                paramCount = 1;
            }
            
            String cmdArray[] = new String[paramCount];
            String execPath   = uninstDir + File.separator + execName;
            String logPath    = j2seInstallDir + File.separator + "install.log";
            String envP[] = null;
            
	    // Put the command and arguments together for windows
            if (Util.isWindowsNT() || Util.isWindows98()) {
                cmdArray[0] = j2seInstallDir + File.separator + execName
                + " /s /v\"/qn ADDLOCAL=ToolsFeature,DemosFeature,SourceFeature INSTALLDIR=\\\"" + j2seInstallDir + "\\\"\"";
                //envP = new String[2];
                //envP[0] = "TMP=" + tempDir;
                //envP[1] = "TEMP=" + tempDir;
            } else if (Util.isWindowsOS()) {
                cmdArray[0] = execPath;
                cmdArray[1] = "\"" + logPath + "\""; //logfile
                cmdArray[2] = "\"" + j2seInstallDir + "\\\""; //instDir NOTE: the opening backslash is in the script
                cmdArray[3] = driveName;
                cmdArray[4] = "\"" + uninstDir + "\""; //uninstDir
            } else {
                cmdArray[0] = execPath;
            }
            
	    // Invoke the correct command
            logEvent(this, Log.DBG,"Start Invoking: cmdArray -> " + Arrays.asList(cmdArray).toString());
            runCommand(cmdArray, envP, support);
            
            // Clean up
            File file = new File(cmdArray[0]);
            if (file.exists()) {
                file.delete();
                logEvent(this, Log.DBG,"Now cleaning up this file " + file.getAbsolutePath());
            }
            
            //workaround
            if (Util.isWindowsNT() || Util.isWindows98()) {
                file = new File(uninstDir + File.separator + "custom-install.bat" );
                if (file.exists()) {
                    file.delete();
                    logEvent(this, Log.DBG,"Now cleaning up this file " + file.getAbsolutePath());
                }
            }
            
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"J2SE installation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    /** Does nothing. JDK is not uninstalled by jdkbundle uninstaller. */
    public void uninstall(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        logEvent(this, Log.DBG,"Uninstalling -> ");
        //statusDesc = resolveString("$L(com.sun.installer.InstallerResources,UNINSTALLING_WAIT_MSG)");
        //support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            logEvent(this, Log.DBG,"j2seInstallDir = " + j2seInstallDir);
            installMode = UNINSTALL;
            
            logEvent(this, Log.DBG,"Do nothing here -> " + j2seInstallDir);
            
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"J2SE uninstallation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    //threads should only run in install mode until ISMP supports them
    private void runCommand(String[] cmdArray, String [] envP, ProductActionSupport support)
    throws Exception{
        boolean doProgress = !(Boolean.getBoolean("no.progress"));
        logEvent(this, Log.DBG,"doProgress -> " + doProgress);
        
        //mutableOperationState = support.getOperationState();
        logEvent(this, Log.DBG,"cmdArray -> " + Arrays.asList(cmdArray).toString());
        try {
            if (Util.isWindowsNT() || Util.isWindows98()) {
                //HACK: don't exec script for NT or 98
                runCommand.execute(cmdArray[0], envP, null);
            }
            else {
                runCommand.execute(cmdArray, envP, null);
            }
            
            if ((installMode == INSTALL) && doProgress)
                startProgress();
            
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
                    ecservice.setExitCode(J2SDK_UNHANDLED_ERROR);
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
        return Util.checkJdkHome(j2seInstallDir);
    }
    
    
    /** Overridden abstract FileFilter method.
     * List the files which shouldn't be cleaned up after installation
     *
     * @param  pathname  The abstract pathname to be tested
     * @return  <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    public boolean accept(File pathname) {
        String path = pathname.getAbsolutePath();
        if (installMode == INSTALL) {
            if ( path.equals(j2seInstallDir + File.separator + "uninstall.sh")
            || path.equals(j2seInstallDir + File.separator + "uninstall.bat"))
                return false;
        }
        else if (installMode == UNINSTALL) {
            if ( path.equals(j2seInstallDir + File.separator + "uninstall.log"))
                return false;
        }
        
        return true;
    }
    
    
    
    /**Returns checksum for j2sdk directory in bytes*/
    public long getCheckSum() {
        if (Util.isWindowsOS()) {
            return 100000000L;
        }
        else if (Util.isSunOS()) {
            return 130000000L;
        }
        else if (Util.isLinuxOS()) {
            return 130000000L;
        }
        return 0L;
    }
    
    /* Returns the required bytes table information.
     * @return required bytes table.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        
        RequiredBytesTable req = new RequiredBytesTable();
	//  String imageDirPath = getProductTree().getInstallLocation(this);
	// logEvent(this, Log.DBG,"imageDirPath -> " + imageDirPath);
        req.addBytes(j2seInstallDir , getCheckSum());
	logEvent(this, Log.DBG, "Total size = " + req.getTotalBytes());
        //Thread.dumpStack();
        
	if (Util.isWindowsOS()) {
	    // the j2se base image directory goes in the system drive
	    String sysDir = new String( (new Character(getWinSystemDrive())).toString().concat(":\\"));
	    req.addBytes(sysDir, 200000000L);
	}
	logEvent(this, Log.DBG, "Total Mbytes = " + (req.getTotalBytes()>>20));
        return req;
    }

    private char getWinSystemDrive() {
	char sysDrive = 'C';
        try {
            String sysLib=resolveString("$D(lib)"); // Resolve system library directory 
            logEvent(this, Log.DBG, "System Library directory is "+sysLib); 
            sysDrive=sysLib.charAt(0); // Resolve system drive letter
            logEvent(this, Log.DBG, " Found system drive is: " + String.valueOf(sysDrive));
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
    
    /** Create the j2se install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createInstallScript(String template, String scriptName)
	throws Exception {
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
	String installerName = null;
	String arch = (String) System.getProperty("os.arch");
	if (Util.isLinuxOS()) {
            //Try to locate Linux JDK installer
            File installDirFile = new File(j2seInstallDir);
            logEvent(this, Log.DBG, "createInstallScript installDirFile: " + installDirFile);
            File [] children = installDirFile.listFiles();
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("linux-i586") != -1) && 
                    children[i].getName().endsWith(".bin")) {
                    installerName = children[i].getName();
                    break;
                }
            }
            if (installerName != null) {
                logEvent(this, Log.DBG, "createInstallScript JDK installer found: " + installerName);
            } else {
                logEvent(this, Log.DBG, "createInstallScript JDK installer NOT found. JDK cannot be installed.");
                installerName = "jdk-installer-not-found";
            }
	} else if (Util.isSunOS()) {
	    if (arch.startsWith("sparc")) {
                //Try to locate Solaris Sparc JDK installer
                File installDirFile = new File(j2seInstallDir);
                logEvent(this, Log.DBG, "createInstallScript installDirFile: " + installDirFile);
                File [] children = installDirFile.listFiles();
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("solaris-sparc") != -1) && 
                        children[i].getName().endsWith(".sh")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
                if (installerName != null) {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer found: " + installerName);
                } else {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer NOT found. JDK cannot be installed.");
                    installerName = "jdk-installer-not-found";
                }
	    } else {
                //Try to locate Solaris X86 JDK installer
                File installDirFile = new File(j2seInstallDir);
                logEvent(this, Log.DBG, "createInstallScript installDirFile: " + installDirFile);
                File [] children = installDirFile.listFiles();
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("solaris-i586") != -1) && 
                        children[i].getName().endsWith(".sh")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
                if (installerName != null) {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer found: " + installerName);
                } else {
                    logEvent(this, Log.DBG, "createInstallScript JDK installer NOT found. JDK cannot be installed.");
                    installerName = "jdk-installer-not-found";
                }
	    }
	}

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("J2SE_INSTALL_DIR=")) {
                line = "J2SE_INSTALL_DIR=" + j2seInstallDir;
            }
            else if (line.startsWith("J2SE_NAME=")) {
                line = "J2SE_NAME=" + installerName;
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

    /** Create the j2se install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createUninstallScript(String template, String scriptName)
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
            if (line.startsWith("J2SE_INSTALL_DIR=")) {
                line = "J2SE_INSTALL_DIR=" + j2seInstallDir;
            }
            else if (line.startsWith("J2SE_VER=")) {
                line = "J2SE_VER=" + "1.5.0";
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not uninstall template file: " + templateFile);
	}
              
        writer.close();
	if (setExecutable(scriptFile.getAbsolutePath())) {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath() +
		     " is set as executable file.");
	    return true;
	}
	return false;
    }
    
    /** Create the j2se install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createInstallScriptWindows(String template, String scriptName)
	throws Exception {
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String installerName = findJDKWindowsInstaller(j2seInstallDir);
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET INSTALLER_NAME=")) {
                line = "SET INSTALLER_NAME=" + installerName;
            } else if (line.startsWith("SET TMP=")) {
                line = "SET TMP=" + tempDir;
            } else if (line.startsWith("SET TEMP=")) {
                line = "SET TEMP=" + tempDir;
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not delete file: " + templateFile);
	}              
        writer.close();
	return true;
    }
    
    /** Create the j2se install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createUninstallScriptWindows(String template, String scriptName)
	throws Exception {

	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));

	String installerName = findJDKWindowsInstaller(j2seInstallDir);

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET INSTALLER_NAME=")) {
                line = "SET INSTALLER_NAME=" + installerName;
            } else if (line.startsWith("SET TMP=")) {
                line = "SET TMP=" + tempDir;
            } else if (line.startsWith("SET TEMP=")) {
                line = "SET TEMP=" + tempDir;
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not uninstall template file: " + templateFile);
	}
              
        writer.close();
	return true;
    }
    
    private String findJDKWindowsInstaller (String j2seInstallDir) {
	String installerName = null;
        //Try to locate Windows JDK installer
        File installDirFile = new File(j2seInstallDir);
        logEvent(this, Log.DBG, "findJDKWindowsInstaller installDirFile: " + installDirFile);
        File [] children = installDirFile.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().startsWith("jdk-1_5_0") && (children[i].getName().indexOf("windows-i586") != -1) && 
                children[i].getName().endsWith(".exe")) {
                installerName = children[i].getName();
                break;
            }
        }
        if (installerName != null) {
            logEvent(this, Log.DBG, "findJDKWindowsInstaller JDK installer found: " + installerName);
        } else {
            logEvent(this, Log.DBG, "findJDKWindowsInstaller JDK installer NOT found. JDK cannot be installed.");
            installerName = "jdk-installer-not-found";
        }
        return installerName;
    }
    
    /** inner class to update the progress pane while installation */
    class ProgressThread extends Thread {
        private boolean loop = true;
        private  MutableOperationState mos;
        private File j2seDir;
        
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
        private boolean doStatusDescUpdate = true;
        
        //variables related to pkg unzipping before installation. Only for Solaris
        private boolean isUnzipping = false;
        private File unzipLog;
        private BufferedReader unzipLogReader = null;
        private long startTime = 0L;
        
        public ProgressThread() {
            this.mos = mutableOperationState;
            lastPathShown = j2seInstallDir;
            j2seDir = new File(j2seInstallDir);
            logFile = new File(j2seInstallDir, "install.log");
            checksum = getCheckSum();
            
        }
        
        public void run() {
            int sleepTime = 1000;
            while (loop) {
                logEvent(this, Log.DBG,"looping");
                try {
                    if (j2seDir.exists()) {
                        //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                        updateProgressBar();
                        //logEvent(this, Log.DBG,"going 2 updateStatusDetail");
                        updateStatusDetail();
                        
                        sleepTime = 1200;
                    } else {
                        updateStatusDetail();
                        sleepTime = 2000;
                    }
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
                String statusDesc = resolveString("$L(com.sun.installer.InstallerResources,AS_OPERATION_CANCELED)");
                mos.setStatusDescription(statusDesc);
                mos.getProgress().setPercentComplete(0);
            }
            
        }
        
        /**check if the operation is canceled. If not yield to other threads.*/
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
        
        /** Updates the progress bar*/
        private void updateProgressBar() {
            if (isCanceled()) return;
            long size = Util.getFileSize(j2seDir);
            long perc = (size * 100) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted)
                return;
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            percentageCompleted = perc;
        }
        
        /** Updates the status detail*/
        public void updateStatusDetail() {
            if (isCanceled()) return;
            if (!j2seDir.exists()) {
                mos.setStatusDetail(resolveString("$L(com.sun.installer.InstallerResources,J2SDK_EXTRACT_MSG)"));
                logEvent(this, Log.DBG,"StatusDetailThread-> " + lastPathShown + " NOT created yet");
                return;
            }
            String recentFilePath = fileComp.getMostRecentFile(j2seDir).getAbsolutePath();
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
                    displayStr = displayStr.substring(0, max_len - fileName.length() - 4)
                    + "...."
                    + fileName;
                }
                if (! recentFilePath.equalsIgnoreCase(lastPathShown)) {
                    lastPathShown = recentFilePath;
                    fileCounter = 0;
                    return displayStr;
                }
                else if ( fileCounter < MIN_DOTS) {
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
        
        
    }
    
}
