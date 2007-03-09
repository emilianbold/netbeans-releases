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
import com.installshield.product.service.product.ProductService;
import com.installshield.util.Log;
import com.installshield.wizard.platform.win32.Win32RegistryService;
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

public class InstallJ2sdkAction extends ProductAction implements FileFilter {
    
    //return code incase an error returns
    public static final int JDK_UNHANDLED_ERROR = -250;
    public static final int JRE_UNHANDLED_ERROR = -251;
    
    private static final int JDK_INSTALL_TYPE = 1;
    private static final int JRE_INSTALL_TYPE = 2;
    
    private int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    
    private String statusDesc = "";
    private String nbInstallDir = "";
    private String j2seInstallDir = "";
    private String jreInstallDir = "";
    private String tempDir = "";
    private String defaultSubdir = "";
    private String origJ2SEInstallDir = "";
    
    private boolean success = false;
    
    private RunCommand runCommand = new RunCommand();
    
    private ProgressThread progressThread;
    private MutableOperationState mutableOperationState;
    
    public InstallJ2sdkAction() {
    }
    
    public void build(ProductBuilderSupport support) {
        try {
            support.putClass(RunCommand.class.getName());
            support.putClass("org.netbeans.installer.RunCommand$StreamAccumulator");
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
    throws Exception {
        ProductService productService = (ProductService) getService(ProductService.NAME);
        nbInstallDir = (String) productService.getProductBeanProperty
        (ProductService.DEFAULT_PRODUCT_SOURCE,null,"absoluteInstallLocation");
        logEvent(this, Log.DBG,"nbInstallDir: " + nbInstallDir);
        
        origJ2SEInstallDir = (String) System.getProperties().get("j2seInstallDir");
        
        logEvent(this, Log.DBG,"$D(common): " + resolveString("$D(common)"));
        logEvent(this, Log.DBG,"$D(install): " + resolveString("$D(install)"));
        jreInstallDir = resolveString("$D(install)") + "\\Java\\"
        + resolveString("$L(org.netbeans.installer.Bundle,JRE.defaultInstallDirectory)");
        logEvent(this, Log.DBG,"jreInstallDir: " + jreInstallDir);
        System.getProperties().put("jreInstallDir",jreInstallDir);
        
        tempDir = Util.getTmpDir();
        logEvent(this, Log.DBG,"Tempdir: " + tempDir);
        
        mutableOperationState = support.getOperationState();
    }
    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.installMessage,"
        + "$L(org.netbeans.installer.Bundle,JDK.shortName))")
	+ " "
	+ resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.waitMessage)") ;
        
        support.getOperationState().setStatusDescription(statusDesc);
        
        defaultSubdir = resolveString("$L(org.netbeans.installer.Bundle,JDK.defaultInstallDirectory)");
        
        try {
            init(support);
            installMode = INSTALL;
            if (!Util.isWindowsOS()) {
                j2seInstallDir = origJ2SEInstallDir + File.separator + defaultSubdir;
            } else {
                j2seInstallDir = origJ2SEInstallDir;
            }
            
            String uninstDir  = origJ2SEInstallDir + File.separator + "_uninst";
	    if (!Util.isWindowsOS()) {
		String jdkInstallScript = uninstDir + File.separator 
                + "j2se-install.template";
		if (!createInstallScript(jdkInstallScript, "custom-install")) {
                    //Cannot set install script executable so exit.
                    return;
                }
		String jdkUninstallScript = uninstDir + File.separator 
		+ "j2se-uninstall.template";
		createUninstallScript(jdkUninstallScript, "uninstall.sh");
	    } else {
		String jdkInstallScript = nbInstallDir + File.separator + "_uninst"
                + File.separator + "custom-install-jdk.template";
		createInstallScriptJDKWindows(jdkInstallScript, "custom-install-jdk.bat");
            }
            String execName;
            int paramCount;

	    // We'll be running the j2se installer in silent mode so most 
	    // of this code is putting together the path and script or exe to
	    // run. Determine the script or exe to run.
            if (Util.isWindowsNT() || Util.isWindows98()) {
                execName =  findJDKWindowsInstaller();
                paramCount = 1;
            } else if (Util.isWindowsOS()) {
                execName = "custom-install-jdk.bat";
                paramCount = 1;
            } else {
                execName = "custom-install";
                paramCount = 1;
            }
            
            String cmdArray[] = new String[paramCount];
            String execPath;
            if (Util.isWindowsOS()) {
                //Not for Win 98/Win NT
                execPath  = nbInstallDir + File.separator + "_uninst" + File.separator + execName;
            } else {
                execPath  = uninstDir + File.separator + execName;
            }
            String envP[] = null;
            
	    // Put the command and arguments together for windows
            if (Util.isWindowsNT() || Util.isWindows98()) {
                cmdArray[0] = "\"" + nbInstallDir + File.separator + execName + "\""
                + " /s /v\"/qn INSTALLDIR=\\\"" + j2seInstallDir + "\\\"\"";
            } else if (Util.isWindowsOS()) {
                cmdArray[0] = execPath;
            } else {
                cmdArray[0] = execPath;
            }
            
	    // Invoke the correct command
            logEvent(this, Log.DBG,"# # # # # # # #");
            logEvent(this, Log.DBG,"Start Invoking JDK installer: cmdArray -> " + Arrays.asList(cmdArray).toString());
            int jdkReturnStatus = runExternalCommand(cmdArray, envP, support, JDK_INSTALL_TYPE);
            
            //Public JRE can be only installed when JDK installation is successfull
            if (jdkReturnStatus == 0) {
                //Update status description
                if (Util.isWindowsOS() && !Util.isJREAlreadyInstalled()) {
                    statusDesc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.installMessage,"
                    + "$L(org.netbeans.installer.Bundle,JRE.shortName))")
                    + " "
                    + resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.waitMessage)") ;

                    support.getOperationState().setStatusDescription(statusDesc);
                }
                if (Util.isWindowsNT() || Util.isWindows98()) {
                    //Install public JRE only when it is not already installed.
                    if (!Util.isJREAlreadyInstalled()) {
                        String jreInstaller = findJREWindowsInstaller() + File.separator + "jre.msi";
                        if (jreInstaller != null) {
                            cmdArray[0] = "msiexec.exe /qn /i \"" + jreInstaller + "\""
                            + " IEXPLORER=1 MOZILLA=1";
                            logEvent(this, Log.DBG,"# # # # # # # #");
                            logEvent(this, Log.DBG,"Start Invoking JRE installer: cmdArray -> " + Arrays.asList(cmdArray).toString());
                            runExternalCommand(cmdArray, envP, support, JRE_INSTALL_TYPE);
                        }
                    }
                } else if (Util.isWindowsOS()) {
                    //Install public JRE only when it is not already installed.
                    if (!Util.isJREAlreadyInstalled()) {
                        String jreInstallScript = nbInstallDir + File.separator + "_uninst"
                        + File.separator + "custom-install-jre.template";
                        createInstallScriptJREWindows(jreInstallScript, "custom-install-jre.bat");
                        execName = "custom-install-jre.bat";
                        paramCount = 1;
                        cmdArray = new String[paramCount];
                        execPath   = nbInstallDir + File.separator + "_uninst" + File.separator + execName;
                        cmdArray[0] = execPath;
                        logEvent(this, Log.DBG,"# # # # # # # #");
                        logEvent(this, Log.DBG,"Start Invoking JRE installer: cmdArray -> " + Arrays.asList(cmdArray).toString());
                        runExternalCommand(cmdArray, envP, support, JRE_INSTALL_TYPE);
                    }
                }
            }
            
            //Delete files
            if (Util.isWindowsNT() || Util.isWindows98()) {
                FileService fileService = (FileService) getService(FileService.NAME);
                String jdkInstaller = nbInstallDir + File.separator + findJDKWindowsInstaller();
                if (fileService.fileExists(jdkInstaller)) {
                    fileService.deleteFile(jdkInstaller);
                }
            }
            
            //Move JDK up one dir level
            if (!Util.isWindowsOS()) {
                moveJ2SEDirContents();
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
            installMode = UNINSTALL;
            logEvent(this, Log.DBG,"origJ2SEInstallDir = " + origJ2SEInstallDir);
            logEvent(this, Log.DBG,"nbInstallDir = " + nbInstallDir);
            
            //Delete files
            if (Util.isWindowsOS()) {
                FileService fileService = (FileService) getService(FileService.NAME);
                String uninstDir = nbInstallDir + File.separator + "_uninst";
                String fileName;
                fileName = uninstDir + File.separator + "custom-install-jre.template";
                if (fileService.fileExists(fileName)) {
                    logEvent(this, Log.DBG,"Deleting: " + fileName);
                    fileService.deleteFile(fileName);
                }
                fileName = uninstDir + File.separator + "custom-install-jre.bat";
                if (fileService.fileExists(fileName)) {
                    logEvent(this, Log.DBG,"Deleting: " + fileName);
                    fileService.deleteFile(fileName);
                }
                fileName = uninstDir + File.separator + "custom-install-jdk.template";
                if (fileService.fileExists(fileName)) {
                    logEvent(this, Log.DBG,"Deleting: " + fileName);
                    fileService.deleteFile(fileName);
                }
                fileName = uninstDir + File.separator + "custom-install-jdk.bat";
                if (fileService.fileExists(fileName)) {
                    logEvent(this, Log.DBG,"Deleting: " + fileName);
                    fileService.deleteFile(fileName);
                }
                fileName = uninstDir + File.separator + "install-jdk.log";
                if (fileService.fileExists(fileName)) {
                    logEvent(this, Log.DBG,"Deleting: " + fileName);
                    fileService.deleteFile(fileName);
                }
                fileName = uninstDir + File.separator + "install-jre.log";
                if (fileService.fileExists(fileName)) {
                    logEvent(this, Log.DBG,"Deleting: " + fileName);
                    fileService.deleteFile(fileName);
                }
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
        logEvent(this, Log.DBG,"J2SE uninstallation took: (ms) " + (System.currentTimeMillis() - currtime));
    }
    
    //threads should only run in install mode until ISMP supports them
    private int runExternalCommand (String[] cmdArray, String [] envP, ProductActionSupport support, int type)
    throws Exception{
        boolean doProgress = !(Boolean.getBoolean("no.progress"));
        logEvent(this, Log.DBG,"doProgress -> " + doProgress);
        
        //mutableOperationState = support.getOperationState();
        logEvent(this, Log.DBG,"cmdArray -> " + Arrays.asList(cmdArray).toString());
        int status = 0;
        try {
            if (Util.isWindowsNT() || Util.isWindows98()) {
                //HACK: don't exec script for NT or 98
                runCommand.execute(cmdArray[0], envP, null);
            }
            else {
                runCommand.execute(cmdArray, envP, null);
            }
            
            if ((installMode == INSTALL) && doProgress) {
                startProgress();
            }
            
            runCommand.waitFor();
            logEvent(this, Log.DBG,runCommand.print());
            
            status = runCommand.getReturnStatus();
            logEvent(this, Log.DBG,"Return status: " + status);
            
            if((installMode == INSTALL) && doProgress) {
                stopProgress();
            }
            
            if (type == JDK_INSTALL_TYPE) {
                if (status == 0) {
                    if (!isCompletedSuccessfully()) {
                        String mode = (installMode == INSTALL) ? "install" : "uninstall";
                        String commandStr = Util.arrayToString(cmdArray, " ");
                        logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + commandStr);
                        logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + commandStr);
                        try {
                            ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                            ecservice.setExitCode(JDK_UNHANDLED_ERROR);
                        } catch (Exception ex) {
                            logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                        }
                        return JDK_UNHANDLED_ERROR;
                    }
                } else {
                    String mode = (installMode == INSTALL) ? "install" : "uninstall";
                    String commandStr = Util.arrayToString(cmdArray, " ");
                    logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + commandStr);
                    logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + commandStr);
                    try {
                        ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                        ecservice.setExitCode(JDK_UNHANDLED_ERROR);
                    } catch (Exception ex) {
                        logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                    }
                    return JDK_UNHANDLED_ERROR;
                }
            } else {
                if (status != 0) {
                    String mode = (installMode == INSTALL) ? "install" : "uninstall";
                    String commandStr = Util.arrayToString(cmdArray, " ");
                    logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + commandStr);
                    logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + commandStr);
                    try {
                        ExitCodeService ecservice = (ExitCodeService)getService(ExitCodeService.NAME);
                        ecservice.setExitCode(JRE_UNHANDLED_ERROR);
                    } catch (Exception ex) {
                        logEvent(this, Log.ERROR, "Couldn't set exit code. ");
                    }
                    return JRE_UNHANDLED_ERROR;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return status;
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
            if (path.equals(origJ2SEInstallDir + File.separator + "uninstall.sh")
            || path.equals(origJ2SEInstallDir + File.separator + "uninstall.bat"))
                return false;
        }
        else if (installMode == UNINSTALL) {
            if (path.equals(origJ2SEInstallDir + File.separator + "uninstall.log"))
                return false;
        }
        
        return true;
    }
    
    /** Returns checksum for j2sdk directory in bytes. It does not include public JRE on Windows.
     * JRE can be installed on another disk so it is split. */
    public long getCheckSum() {
        if (Util.isWindowsOS()) {
            if (Util.isJDKAlreadyInstalled() && Util.isJREAlreadyInstalled()) {
                return 0L;
            } else if (!Util.isJDKAlreadyInstalled() && Util.isJREAlreadyInstalled()) {
                //Install only JDK, JRE is already installed
                return 124000000L;
            } else if (Util.isJDKAlreadyInstalled() && !Util.isJREAlreadyInstalled()) {
                //We cannot install JRE if JDK is already installed, as we do not know if
                //JRE installer is installed with JDK
                return 0L;
            } else if (!Util.isJDKAlreadyInstalled() && !Util.isJREAlreadyInstalled()) {
                //Install JDK and JRE (124+72)
                return 196000000L;
            }
        } else if (Util.isLinuxOS()) {
            return 150000000L;
        } else if (Util.isSolarisSparc()) {
            return 148000000L;
        } else if (Util.isSolarisX86()) {
            return 140000000L;
        }
        return 0L;
    }
    
    /* Returns the required bytes table information.
     * @return required bytes table.
     * @see com.installshield.product.RequiredBytesTable
     */
    public RequiredBytesTable getRequiredBytes() throws ProductException {
        //#48948: We must set dirs here because init() is not run yet when getRequiredBytes
        //is called.
        origJ2SEInstallDir = (String) System.getProperties().get("j2seInstallDir");
        tempDir = Util.getTmpDir();
        
        RequiredBytesTable req = new RequiredBytesTable();
	//  String imageDirPath = getProductTree().getInstallLocation(this);
	// logEvent(this, Log.DBG,"imageDirPath -> " + imageDirPath);
        req.addBytes(origJ2SEInstallDir, getCheckSum());
        logEvent(this, Log.DBG, "origJ2SEInstallDir: " + origJ2SEInstallDir);
        logEvent(this, Log.DBG, "tempDir: " + tempDir);
	logEvent(this, Log.DBG, "Total size = " + req.getTotalBytes());
        
        if (Util.isWindowsNT() || Util.isWindows98()) {
            //TMP dir is by default on system disk and we are unable to change it #48281
	    //Cache of JDK installer is stored to Local Settings folder. It is 55MB.
            //TMP is used by bundled JVM and MSI to store its cache temporarily about 170MB
            //when it is checked here bundled JVM is already present in TMP so only about
            //additional 50MB is necessary ie.130MB+50MB=180MB at system dir.
	    String sysDir = new String( (new Character(getWinSystemDrive())).toString().concat(":\\"));
            logEvent(this, Log.DBG, "sysDir: " + sysDir);
            req.addBytes(sysDir, 55000000L);
            req.addBytes(sysDir, 50000000L);
            
            //Base images in common folder
            String commonDir = resolveString("$D(common)");
            if (!Util.isJDKAlreadyInstalled() && Util.isJREAlreadyInstalled()) {
                //Install only JDK, JRE is already installed
                req.addBytes(commonDir, 72000000L);
            } else if (!Util.isJDKAlreadyInstalled() && !Util.isJREAlreadyInstalled()) {
                //Install JDK and JRE (72+30)
                req.addBytes(commonDir, 102000000L);
            }
        } else if (Util.isWindowsOS()) {
            //Cache of JDK installer is stored to Local Settings folder. It is 55MB.
            //TMP is used by bundled JVM and MSI to store its cache temporarily about 170MB
            //when it is checked here bundled JVM is already present in TMP so only about
            //additional 50MB is necessary
	    String sysDir = new String( (new Character(getWinSystemDrive())).toString().concat(":\\"));
            logEvent(this, Log.DBG, "sysDir: " + sysDir);
	    req.addBytes(sysDir, 55000000L);
            req.addBytes(tempDir, 50000000L);
            
            if (!Util.isJREAlreadyInstalled()) {
                jreInstallDir = resolveString("$D(install)") + "\\Java\\"
                + resolveString("$L(org.netbeans.installer.Bundle,JRE.defaultInstallDirectory)");
                req.addBytes(jreInstallDir,70000000L);
            }
            
            //Base images in common folder
            String commonDir = resolveString("$D(common)");
            if (!Util.isJDKAlreadyInstalled() && Util.isJREAlreadyInstalled()) {
                //Install only JDK, JRE is already installed
                req.addBytes(commonDir, 72000000L);
            } else if (!Util.isJDKAlreadyInstalled() && !Util.isJREAlreadyInstalled()) {
                //Install JDK and JRE (72+30)
                req.addBytes(commonDir, 102000000L);
            }
	}
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
        File installDirFile = new File(origJ2SEInstallDir);
        logEvent(this, Log.DBG, "createInstallScript installDirFile: " + installDirFile);
        File [] children = installDirFile.listFiles();
        String installerPrefix = resolveString("$L(org.netbeans.installer.Bundle,JDK.installerPrefix)");
	if (Util.isLinuxOS()) {
            //Try to locate Linux JDK installer
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().startsWith(installerPrefix) && (children[i].getName().indexOf("linux-i586") != -1) && 
                    children[i].getName().endsWith(".bin")) {
                    installerName = children[i].getName();
                    break;
                }
            }
	} else if (Util.isSunOS()) {
	    if (arch.startsWith("sparc")) {
                //Try to locate Solaris Sparc JDK installer
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith(installerPrefix) && (children[i].getName().indexOf("solaris-sparc") != -1) && 
                        children[i].getName().endsWith(".sh")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
	    } else {
                //Try to locate Solaris X86 JDK installer
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith(installerPrefix) && (children[i].getName().indexOf("solaris-i586") != -1) && 
                        children[i].getName().endsWith(".sh")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
	    }
	}
        if (installerName != null) {
            logEvent(this, Log.DBG, "createInstallScript JDK installer found: " + installerName);
        } else {
            logEvent(this, Log.DBG, "createInstallScript JDK installer NOT found. JDK cannot be installed.");
            installerName = "jdk-installer-not-found";
        }

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("J2SE_INSTALL_DIR=")) {
                line = "J2SE_INSTALL_DIR=" + origJ2SEInstallDir;
            } else if (line.startsWith("J2SE_INSTALLER_NAME=")) {
                line = "J2SE_INSTALLER_NAME=" + installerName;
            } else if (line.startsWith("J2SE_VER=")) {
                line = "J2SE_VER=" + resolveString("$L(org.netbeans.installer.Bundle,JDK.version)");
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not delete file: " + templateFile);
	}              
        writer.close();
	if (setExecutable(scriptFile.getAbsolutePath())) {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath()
            + " is set as executable file.");
	    return true;
	} else {
            return false;
        }
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
                line = "J2SE_INSTALL_DIR=" + origJ2SEInstallDir;
            } else if (line.startsWith("J2SE_VER=")) {
                line = "J2SE_VER=" + resolveString("$L(org.netbeans.installer.Bundle,JDK.version)");
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
	if (!templateFile.delete()) {
	    logEvent(this, Log.ERROR, "Could not uninstall template file: " + templateFile);
	}
        
        writer.close();
	if (setExecutable(scriptFile.getAbsolutePath())) {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath()
            + " is set as executable file.");
	    return true;
	} else {
            return false;
        }
    }
    
    /** Create the JDK install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createInstallScriptJDKWindows (String template, String scriptName)
    throws Exception {
        logEvent(this, Log.DBG,"createInstallScriptJDKWindows ENTER");
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String installerName = findJDKWindowsInstaller();
        String driveName = nbInstallDir.substring(0, nbInstallDir.indexOf(File.separator));
        String logFileName = nbInstallDir + File.separator + "_uninst" + File.separator + "install-jdk.log";
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET DRIVE=")) {
                line = "SET DRIVE=" + driveName;
            } else if (line.startsWith("SET LOGFILE=")) {
                line = "SET LOGFILE=" + "\"" + logFileName + "\"";
            } else if (line.startsWith("SET JDK_INSTALL_DIR=")) {
                line = "SET JDK_INSTALL_DIR=" + "\"" + j2seInstallDir + "\\\"";
            } else if (line.startsWith("SET JDK_INSTALLER_LOCATION=")) {
                line = "SET JDK_INSTALLER_LOCATION=" + "\"" + nbInstallDir + "\"";
            } else if (line.startsWith("SET JDK_INSTALLER_NAME=")) {
                line = "SET JDK_INSTALLER_NAME=" + installerName;
            } else if (line.startsWith("SET TMP=")) {
                line = "SET TMP=" + tempDir;
            } else if (line.startsWith("SET TEMP=")) {
                line = "SET TEMP=" + tempDir;
            }
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
        writer.close();
	return true;
    }
    
    /** Create the JRE install script from the provided template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createInstallScriptJREWindows (String template, String scriptName)
    throws Exception {
        logEvent(this, Log.DBG,"createInstallScriptJREWindows ENTER");
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have j2se as parent
	}
        File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
        String installerPath = findJREWindowsInstaller();
        String driveName = installerPath.substring(0, installerPath.indexOf(File.separator));
        String logFileName = nbInstallDir + File.separator + "_uninst" + File.separator + "install-jre.log";
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET DRIVE=")) {
                line = "SET DRIVE=" + driveName;
            } else if (line.startsWith("SET LOGFILE=")) {
                line = "SET LOGFILE=" + "\"" + logFileName + "\"";
            } else if (line.startsWith("SET TMP=")) {
                line = "SET TMP=" + tempDir;
            } else if (line.startsWith("SET TEMP=")) {
                line = "SET TEMP=" + tempDir;
            } else if (line.startsWith("SET JRE_INSTALLER_LOCATION=")) {
                line = "SET JRE_INSTALLER_LOCATION=" + "\"" + installerPath + "\"";
            } else if (line.startsWith("SET JRE_MSI_PROJECT=")) {
                line = "SET JRE_MSI_PROJECT=" + "jre.msi";
            }
            logEvent(this, Log.DBG, "JRE line:" + line);
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
        writer.close();
	return true;
    }
    
    private String findJDKWindowsInstaller () {
	String installerName = null;
        //Try to locate Windows JDK installer
        File installDirFile = new File(nbInstallDir);
        logEvent(this, Log.DBG, "findJDKWindowsInstaller installDirFile: " + installDirFile);
        File [] children = installDirFile.listFiles();
        String installerPrefix = resolveString("$L(org.netbeans.installer.Bundle,JDK.installerPrefix)");
        for (int i = 0; i < children.length; i++) {
            if (children[i].getName().startsWith(installerPrefix) && (children[i].getName().indexOf("windows-i586") != -1) && 
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
    
    /** Find path to public JRE installer ie. jre.msi file WITHOUT file itself. 
     * @return null if jre.msi file for given JRE version is not found 
     */
    private String findJREWindowsInstaller () {
        //+ "\\Java\\Update\\Base Images\\jdk1.5.0.b64\\patch-jdk1.5.0_01.b06\\jre.msi\"";
	String installerName = null;
        //String baseDir = resolveString("$D(common)") + "\\Java\\Update\\Base Images\\";
        File baseDirFile = new File(resolveString("$D(common)") + "\\Java\\Update\\Base Images\\");
        if (!baseDirFile.exists()) {
            return null;
        }
        String defaultJDKDir = resolveString("$L(org.netbeans.installer.Bundle,JDK.patchDirectory)");
        String patchJDKDir = resolveString("$L(org.netbeans.installer.Bundle,JDK.defaultInstallDirectory)");
        File [] files = baseDirFile.listFiles();
        File jdkDirFile = null;
        boolean found = false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith(defaultJDKDir)) {
                found = true;
                jdkDirFile = files[i];
                break;
            }
        }
        if (!found) {
            return null;
        }
        if (!jdkDirFile.exists()) {
            return null;
        }
        
        files = jdkDirFile.listFiles();
        File patchDirFile = null;
        found = false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith("patch-" + patchJDKDir)) {
                found = true;
                patchDirFile = files[i];
                break;
            }
        }
        if (!found) {
            return null;
        }
        if (!patchDirFile.exists()) {
            return null;
        }
        File jreInstallerFile = new File(patchDirFile,"jre.msi");
        if (!jreInstallerFile.exists()) {
            return null;
        }
        
        logEvent(this, Log.DBG, "findJREWindowsInstaller JRE installer found: " + jreInstallerFile.getPath());
        return patchDirFile.getPath();
    }
    
    private boolean moveJ2SEDirContents() {
         try {
             FileService fileService = (FileService)getService(FileService.NAME);
             if (fileService == null) {
                 logEvent(this, Log.DBG, "FileService is null. Cannot move J2SE files");
                 return false;
             }
             if (fileService != null) {
                 File srcFile = new File(j2seInstallDir);
                 File[] srcFileList = srcFile.listFiles();
                 if (srcFileList == null) {
                     logEvent(this, Log.DBG, "Could not rename the J2SE files.");
                     return false;
                 }
                 String parent = srcFile.getParent();                 
                 logEvent(this, Log.DBG, "Moving files from " + j2seInstallDir +
                          "\n to  " + parent);
                 try {
                     for (int i=0; i < srcFileList.length; i++) {
                         logEvent(this, Log.DBG, "Rename " +
                                  srcFileList[i].getAbsolutePath() +
                                  "  to  " + parent + "/" +
                                  srcFileList[i].getName());
                                 srcFileList[i].renameTo(new File(parent +
                                                          File.separator +
                                                          srcFileList[i].getName()));
                     }
                 } catch (Exception ex) {
                     logEvent(this, Log.DBG, "Could not rename the J2SE files.");
                     logEvent(this, Log.DBG, ex);
                     return false;
                 }
                 try {
                     fileService.deleteDirectory(j2seInstallDir, true, false);
                 } catch (Exception ex) {
                     logEvent(this, Log.DBG,
                              "Could not remove empty J2SDK directory: " + j2seInstallDir);
                 }
             }
         } catch (Exception ex) {
             logEvent(this, Log.ERROR, "Cannot get FileService for moving J2SE files\nException: " + ex);
             return false;
         }
         return true;
     }

    /** inner class to update the progress pane while installation */
    class ProgressThread extends Thread {
        private boolean loop = true;
        private  MutableOperationState mos;
        private File jdkDir;
        private File jreDir;
        
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
            jdkDir = new File(j2seInstallDir);
            jreDir = new File(jreInstallDir);
            logFile = new File(j2seInstallDir, "install.log");
            checksum = getCheckSum();
            //JRE is installed by default, adjust checksum to display progress bar correctly
            if (!Util.isJREAlreadyInstalled()) {
                checksum += 70000000L;
            }
        }
        
        public void run() {
            long sleepTime = 1000L;
            while (loop) {
                try {
                    if (jdkDir.exists()) {
                        //logEvent(this, Log.DBG,"going 2 updateProgressBar");
                        updateProgressBar();
                        //logEvent(this, Log.DBG,"going 2 updateStatusDetail");
                        updateStatusDetail();
                    } else {
                        updateStatusDetail();
                    }
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
            logEvent(this, Log.DBG,"Finishing");
            if(!mos.isCanceled()) {
                mos.setStatusDescription("");
                //As this action is not the last one (there is now StorageBuilder after)
                //it is not necessary to increment percentage completed.
                /*for (; percentageCompleted <= 100; percentageCompleted++) {
                    logEvent(this, Log.DBG,"percentageCompleted = " + percentageCompleted + " updateCounter " + mos.getUpdateCounter());
                    mos.updatePercentComplete(ESTIMATED_TIME, 1L, 100L);
                }*/
            } else {
                String statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installationCancelled)");
                mos.setStatusDescription(statusDesc);
                mos.getProgress().setPercentComplete(0);
            }
            
        }
        
        /** Check if the operation is canceled. */
        private boolean isCanceled() {
            if(mos.isCanceled() && loop) {
                logEvent(this, Log.DBG,"MOS is cancelled");
                loop = false;
                runCommand.interrupt();
            }
            
            return mos.isCanceled();
        }
        
        /** Updates the progress bar. */
        private void updateProgressBar() {
            if (isCanceled()) {
                return;
            }
            long size = Util.getFileSize(jdkDir) + Util.getFileSize(jreDir);
            long perc = (size * 100) / checksum;
            logEvent(this, Log.DBG,"installed size = " + size + " perc = " + perc);
            if (perc <= percentageCompleted) {
                return;
            }
            long increment = perc - percentageCompleted;
            mos.updatePercentComplete(ESTIMATED_TIME, increment, 100L);
            percentageCompleted = perc;
        }
        
        /** Updates the status detail. */
        public void updateStatusDetail() {
            if (isCanceled()) return;
            if (!jdkDir.exists()) {
                String desc = resolveString("$L(org.netbeans.installer.Bundle,ProgressPanel.prepareMessage,"
                + "$L(org.netbeans.installer.Bundle,JDK.shortName))");
                mos.setStatusDetail(desc);
                logEvent(this, Log.DBG,"StatusDetailThread-> " + lastPathShown + " NOT created yet");
                return;
            }
            String recentFilePath = fileComp.getMostRecentFile(jdkDir).getAbsolutePath();
            logEvent(this, Log.DBG,"StatusDetailThread-> " + recentFilePath + "  MODIFIED!!!");
            String filename = getDisplayPath(recentFilePath);
            if (Util.isWindowsOS()) {
                mos.setStatusDetail(filename);
            } else {
                mos.setStatusDetail(modifyFilename(filename));
            }
        }
        
        private String modifyFilename(String name) {
             StringBuffer filename = new StringBuffer(name);
             int subdirSize = defaultSubdir.length();
             int nameSize = name.length();
             for (int i=0; i < nameSize-subdirSize; i++) {
                 if (defaultSubdir.equals(filename.substring(i,i+subdirSize))) {
                     name = filename.substring(0, i)
                          + filename.substring(i+subdirSize+1, nameSize);
                     break;
                 }
             }
             return name;
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
