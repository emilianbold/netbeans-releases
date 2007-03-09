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

public class InstallApplicationServerAction extends ProductAction implements FileFilter {
    
    //return code incase an error returns
    public static final int AS_UNHANDLED_ERROR = -500;
    
    private static final String AS_SETUP_DIR    = "as_setup";
    private static final String STATE_FILE_NAME = "statefile";
    public static final String UNINST_DIRECTORY_NAME = "_uninst";
    
    protected static final String JDK_DIRECTORY_NAME = "java";
    protected static final String POINTBASE_DIRECTORY_NAME = "pointbase";
    
    private static final String INSTALL_SH    = "custom-install.sh";
    private static final String UNINSTALL_SH  = "custom-uninstall.sh";
    private static final String INSTALL_BAT   = "custom-install.bat";
    private static final String UNINSTALL_BAT = "custom-uninstall.bat";
    private static final String AS8_LICENSE   = "appserv.lic";
    private static final String PERM_LICENSE  = "plf";
    
    protected static int installMode = 0;
    private static final int INSTALL = 0;
    private static final int UNINSTALL = 1;
    private static int adminPortNumber;
    
    private String statusDesc = "";
    private String instDirPath;
    private String imageDirPath;
    /** Location of JDK on which installer is running. */
    private String jdkDirPath;
    
    private String rootInstallDir;
    /** NetBeans installation directory. */
    private String nbInstallDir;
    private String statefilePath;
    private String asSetupDirPath;
    
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
            support.putClass(InstallApplicationServerAction.PEFileFilter.class.getName());
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
        
        instDirPath = rootInstallDir + File.separator + UNINST_DIRECTORY_NAME;
        logEvent(this, Log.DBG,"instDirPath: "+ instDirPath);
        
        imageDirPath  = Util.getASInstallDir();
        logEvent(this, Log.DBG,"imageDirPath: "+ imageDirPath);
        
	asSetupDirPath = instDirPath + File.separator + AS_SETUP_DIR;
	if (Util.isWindowsOS() || Util.isMacOSX()) {
	    statefilePath = asSetupDirPath + File.separator + STATE_FILE_NAME;
	} else {
	    statefilePath = instDirPath + File.separator + STATE_FILE_NAME;
	}
        logEvent(this, Log.DBG,"statefilePath: "+ statefilePath);
        //Set JDK selected in JDKSearchPanel. It will be used to run AS Installer.
        jdkDirPath = Util.getJdkHome();
        logEvent(this, Log.DBG,"jdkDirPath: "+ jdkDirPath);

        // Get port information
	/*
        adminPort = (String)System.getProperties().get("adminPort");
        webPort = (String)System.getProperties().get("webPort");
        httpsPort = (String)System.getProperties().get("httpsPort");
	*/
        tmpDir = Util.getTmpDir();
       
        mutableOperationState = support.getOperationState();
    }

    
    public void install(ProductActionSupport support) {
        long currtime = System.currentTimeMillis();
        statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installMessage,"
        + "$L(org.netbeans.installer.Bundle, AS.shortName))")
        + " " + resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.waitMessage)")
        + "\n" + resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.firewallWarning)");
        support.getOperationState().setStatusDescription(statusDesc);
        
        try {
            init(support);
            installMode = INSTALL;
            
            File setupFile = new File(statefilePath);
            logEvent(this, Log.DBG,"setupFile="+ setupFile.getAbsolutePath());
            mutableOperationState.setStatusDetail(setupFile.getAbsolutePath());
            
            if (!setupFile.exists()) {
		//InstallerExceptions.setErrors(true);
                logEvent(this, Log.ERROR, "Could not find Setup File - " + setupFile.getAbsolutePath());
                // Need I18N here
                System.out.println("Could not find Setup File - " + setupFile.getAbsolutePath());
                return;
            } 

            if (Util.isWindowsOS()) {
		createScript(asSetupDirPath + File.separator + "as-win-install.template",
			     INSTALL_BAT, INSTALL);
	    } else if (Util.isMacOSX()) {
                String installTemplate = asSetupDirPath + File.separator + "as-macosx-install.template";
		boolean executable = createScript(installTemplate, INSTALL_SH, INSTALL);
		if (!executable) {
		    // Can't execute install script so exit
		    //InstallerExceptions.setErrors(true);
		    //InstallerExceptions.addErrorMsg(resolveString("$L(com.sun.installer.InstallerResources,IE_EXEC_PERM_NOT_SET)") + INSTALL_SH);
		    logEvent(this, Log.ERROR, "Could not set execute permissions for Mac OS X install script: " + INSTALL_SH);
		    return;
		}
	    } else {
                String installTemplate = instDirPath + File.separator + "as-unix-install.template";
		boolean executable = createScript(installTemplate, INSTALL_SH, INSTALL);
		if (!executable) {
		    // Can't execute install script so exit
		    //InstallerExceptions.setErrors(true);
		    //InstallerExceptions.addErrorMsg(resolveString("$L(com.sun.installer.InstallerResources,IE_EXEC_PERM_NOT_SET)") + INSTALL_SH);
		    logEvent(this, Log.ERROR, "Could not set execute permissions for Unix install script: " + INSTALL_SH);
		    return;
		}
	    }
            
            boolean modified = modifyStatefile(setupFile);
            
            if (!modified) {
                logEvent(this, Log.DBG, "Error occured while modifying the statefile " + setupFile.getAbsolutePath());
                if (invalidPortFound == true) {
                    logEvent(this, Log.ERROR, "Error occured while searching for unused port."
                    + " Please make sure one from each of the 3 following port ranges is not in"
                    + " use:\n\t4848 - 4858\n\t8081 - 8091\n\t1043 - 1053\nClean up the partial"
                    + " install and rerun the installer.");
                }
		//InstallerExceptions.setErrors(true);
                setAppserverExitCode(AS_UNHANDLED_ERROR);
                return;
            }
            mutableOperationState.setStatusDetail(imageDirPath);
            
            String cmdArray[] = new String[1];
            // To be cleaned up later
            if (Util.isWindowsOS()) {
                cmdArray[0] = "\"" + asSetupDirPath + File.separator 
		    + INSTALL_BAT + "\"";
            } else if (Util.isMacOSX()) {
                cmdArray[0] = asSetupDirPath + File.separator + INSTALL_SH;
            } else {
                cmdArray[0] = instDirPath + File.separator + INSTALL_SH;
            }
            
            logEvent(this, Log.DBG,"****RunCommand Start " );
            runCommand(cmdArray, support);
            logEvent(this, Log.DBG,"****RunCommand End " );
            
	    // installPermanentLicense();
            
            //for debugging purposes, allow not to remove instDirPath
            boolean cleanInstDir = !(Boolean.getBoolean("keep.as_inst"));
            logEvent(this, Log.DBG,"cleanInstDir -> " + cleanInstDir);
            statusDesc = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.cleanInstDir,"
            + "$L(org.netbeans.installer.Bundle, AS.shortName))");
            mutableOperationState.setStatusDescription(statusDesc);
            
            if (cleanInstDir) {
		if (Util.isWindowsOS()) {
		    Util.deleteDirectory(new File(asSetupDirPath), this);
		    mutableOperationState.setStatusDescription("");
		    logEvent(this, Log.DBG,"Deleted contents of: " + asSetupDirPath);
                } else if (Util.isMacOSX()) {
		    Util.deleteDirectory(new File(asSetupDirPath), this);
		    mutableOperationState.setStatusDescription("");
		    logEvent(this, Log.DBG,"Deleted contents of: " + asSetupDirPath);
		} else {
		    File script = new File(instDirPath, INSTALL_SH);
		    if (script.exists()) {
			script.delete();
			logEvent(this, Log.DBG,"Deleted file: " + script.getAbsolutePath());
		    }
		    File statefile = new File(statefilePath);
		    if (statefile.exists()) {
			statefile.delete();
			logEvent(this, Log.DBG,"Deleted file: " + statefile.getAbsolutePath());
		    }
                    
		    String installerName = findASInstaller();
		    if (installerName != null) {
			File installer = new File(instDirPath, installerName);
			if (installer.exists()) {
			    installer.delete();
			    logEvent(this, Log.DBG,"Deleted file: " + installer.getAbsolutePath());
			}
		    }
		}
	    }
            //removeAppserverFromAddRemovePrograms();
            //cleanAppserverStartMenu();

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
            
            File file;
            //Delete files created during installation
            file = new File(instDirPath + File.separator + "as-install.log");
            if (file.exists()) {
                if (file.delete()) {
                    logEvent(this, Log.DBG, "File: " + file + " deleted.");
                } else {
                    logEvent(this, Log.DBG, "File: " + file + " could not be deleted.");
                }
            }
        } catch (Exception ex) {
            logEvent(this, Log.ERROR, ex);
            logEvent(this, Log.DBG, ex);
        }
    }
    
    //threads should only run in install mode until ISMP supports them
    private void runCommand(String[] cmdArray, ProductActionSupport support)
    throws Exception{
        boolean doProgress = !(Boolean.getBoolean("no.progress"));
        logEvent(this, Log.DBG,"doProgress -> " + doProgress);
        
        //mutableOperationState = support.getOperationState();
        
        logEvent(this, Log.DBG,"cmdArray -> " + Arrays.asList(cmdArray).toString());
        try {
            String prevLogFile = getPELogPath(getPEDirLogPath());
            
            runCommand.execute(cmdArray, null, null);
            
            if ((installMode == INSTALL) && doProgress) {
                startProgress();
            }
            
            runCommand.waitFor();
            
            logEvent(this, Log.DBG,runCommand.print());
            
            int status = runCommand.getReturnStatus();
            logEvent(this, Log.DBG, "status code = " + status + " which is " + ((status == 73 || status == 72 || status == 0) ? "successful" : "unsuccessful")); 
            
            if (!isCompletedSuccessfully() || ((status != 0) && status != (installMode == INSTALL ? 73 : 72))) {
                String mode = (installMode == INSTALL) ? "install" : "uninstall";
                String command = Util.arrayToString(cmdArray, " ");
                logEvent(this, Log.DBG, "Error occured while " + mode + "ing [" + status + "] -> " + command);
                logEvent(this, Log.ERROR, "Error occured while " + mode + "ing [" + status +  "] -> " + command);

                //InstallerExceptions.setErrors(true);
                setAppserverExitCode(AS_UNHANDLED_ERROR);

                String currentLogFile = getPELogPath(getPEDirLogPath());
                logEvent(this, Log.DBG, "currentLogFile = " + currentLogFile);
                logEvent(this, Log.DBG, "prevLogFile = " + prevLogFile);
                if (currentLogFile != null && !prevLogFile.equals(currentLogFile)) {
                     logEvent(this, Log.DBG, "there is a log file");
                     logEvent(this, Log.ERROR, "Error occured while " 
                     + resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installMessage,"
                     + "$L(org.netbeans.installer.Bundle, AS.shortName))")
                     + "View log file " + currentLogFile + " for more details.");
                } else {
                     currentLogFile = getPEDirLogPath();
                     String tmp = ".";
                     if (status == 50) {
                          tmp = resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.fatalError,"
                          + "$L(org.netbeans.installer.Bundle, AS.shortName))");
                     }

                     logEvent(this, Log.DBG, "there is NO log file");
                     logEvent(this, Log.ERROR, "Error occured while "
                     + resolveString("$L(org.netbeans.installer.Bundle, ProgressPanel.installMessage,"
                     + "$L(org.netbeans.installer.Bundle, AS.shortName))") 
                     + tmp);
                }
            } else {
                System.getProperties().put("appserverHome", imageDirPath);
            }
            
            if((installMode == INSTALL) && doProgress) {
                stopProgress();
            }
            
            // System.out.println("getPELogPath() = " + (getPELogPath(getPEDirLogPath())));

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
    
    /** check whether or not the un/installation was successful*/
    private boolean isCompletedSuccessfully() {
        File file = new File(imageDirPath, "appserv_uninstall.class");
        if (installMode == UNINSTALL) {
            //check for the following file. If it doesn't exists, the uninstallation didn't go thru
            success = !(file.exists());
        } else if (installMode == INSTALL) {
            success = (file.exists());
        }
        logEvent(this, Log.DBG, "success is " + success);
        System.getProperties().put("isAppServerInstallationSuccessful",new Boolean(success));
        return success;
    }
    
    /**returns whther the file was modified successfully or not */
    private boolean modifyStatefile(File setupFile)
    throws Exception {
        File setupFileNew = new File(setupFile.getAbsolutePath() + ".new");
        
        BufferedReader reader = new BufferedReader(new FileReader(setupFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(setupFileNew));
        
        logEvent(this, Log.DBG,"InstallApplicationServerAction: in modifyStatefile(): setupFile=" 
        + setupFile.getAbsolutePath() + "; setupFileNew=" + setupFileNew.getAbsolutePath());
        
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("defaultInstallDirectory")) {
                line = "defaultInstallDirectory = " + imageDirPath;
            }
            else if (line.startsWith("currentInstallDirectory")) {
                line = "currentInstallDirectory = " + imageDirPath;
            }
            else if (line.startsWith("JDK_LOCATION")) {
                String jdkHome = (String)System.getProperties().get("jdkHome");
                line = "JDK_LOCATION = " + jdkDirPath; 
            }
            else if (line.startsWith("INST_ASADMIN_PORT")) {
                logEvent(this, Log.DBG,"Checking INST_ASADMIN_PORT");
                // Make sure check the "adminPort" property in silent mode
                String portNumber;
                if (adminPort != null && adminPort.length() > 0) {
                    portNumber = adminPort;
                } else {
                    portNumber = line.substring(line.indexOf('=') + 1).trim();
                }
                logEvent(this, Log.DBG,"Start with adminPort =" + portNumber);
                int validPortNum =  NetUtils.findValidPort(portNumber, 15);
		adminPortNumber = validPortNum;
                line = "INST_ASADMIN_PORT = " + validPortNum;
                System.getProperties().put("adminPort",String.valueOf(validPortNum));
                if (validPortNum == -1) {
                    invalidPortFound = true;
                    logEvent(this, Log.ERROR,"Cannot find unused adminPort - return value " + validPortNum);
                } else {
                    logEvent(this, Log.DBG,"Found valid adminPort =" + validPortNum);
                }
            }
            else if (line.startsWith("INST_ASWEB_PORT")) {
                logEvent(this, Log.DBG,"Checking INST_ASWEB_PORT");
                String portNumber;
                if (webPort != null && webPort.length() > 0) {
                    portNumber = webPort;
                } else {
                    portNumber = line.substring(line.indexOf('=') + 1).trim();
                }
                int validPortNum =  NetUtils.findValidPort(portNumber, 15);
                line = "INST_ASWEB_PORT = " + validPortNum;
                System.getProperties().put("webPort",String.valueOf(validPortNum));
                if (validPortNum == -1) {
                    invalidPortFound = true;
                    logEvent(this, Log.ERROR,"Cannot find unused asWebPort - return value " + validPortNum);
                } else {
                    logEvent(this, Log.DBG,"Found valid webPort =" + validPortNum);
                }
            }
            else if (line.startsWith("INST_HTTPS_PORT")) {
                logEvent(this, Log.DBG,"Checking INST_HTTPS_PORT");
                String portNumber;
                if (httpsPort != null && httpsPort.length() > 0) {
                    portNumber = httpsPort;
                } else {
                    portNumber = line.substring(line.indexOf('=') + 1).trim();
                }
                int validPortNum =  NetUtils.findValidPort(portNumber, 15);
                line = "INST_HTTPS_PORT = " + validPortNum;
                System.getProperties().put("httpsPort",String.valueOf(validPortNum));
                if (validPortNum == -1) {
                    invalidPortFound = true;
                    logEvent(this, Log.ERROR,"Cannot find unused httpsPort - return value " + validPortNum);
                } else {
                    logEvent(this, Log.DBG,"Found valid httpsPort =" + validPortNum);
                }
            }
            /*
            else if (line.startsWith("INST_ASADMIN_USERNAME = ")) {
                String name = line.substring(line.indexOf('=') + 1).trim();
                System.getProperties().put("INST_ASADMIN_USERNAME",name);
            }
            else if (line.startsWith("INST_ASADMIN_PASSWORD = ")) {
                String passwd = line.substring(line.indexOf('=') + 1).trim();
                System.getProperties().put("INST_ASADMIN_PASSWORD", passwd);
            }
            else if (line.startsWith("INST_ASADMIN_PORT = ")) {
                logEvent(this, Log.DBG,"Checking INST_ASADMIN_PORT");
                String portNumber = line.substring(line.indexOf('=') + 1).trim();
                // int validPortNum =  getValidPortNumber(this, portNumber, 10);
                // Make sure check the "adminPort" property in silent mode
                int validPortNum =  findValidPortNumber(portNumber, 10);
                line = "INST_ASADMIN_PORT = " + validPortNum;
                System.getProperties().put("INST_ASADMIN_PORT",String.valueOf(validPortNum));
            }
            else if (line.startsWith("INST_ASWEB_PORT = ")) {
                logEvent(this, Log.DBG,"Checking INST_ASWEB_PORT");
                String portNumber = line.substring(line.indexOf('=') + 1).trim();
                int validPortNum =  getValidPortNumber(this, portNumber, 10);
                line = "INST_ASWEB_PORT = " + validPortNum;
                System.getProperties().put("INST_ASWEB_PORT",String.valueOf(validPortNum));
            }
            else if (line.startsWith("INST_HTTPS_PORT = ")) {
                logEvent(this, Log.DBG,"Checking INST_HTTPS_PORT");
                String portNumber = line.substring(line.indexOf('=') + 1).trim();
                int validPortNum =  getValidPortNumber(this, portNumber, 10);
                line = "INST_HTTPS_PORT = " + validPortNum;
                System.getProperties().put("INST_HTTPS_PORT",String.valueOf(validPortNum));
            }
            */
            else if (line.startsWith("AS_INSTALL_CONFIG_DIR")) {
                logEvent(this, Log.DBG,"Checking AS_INSTALL_CONFIG_DIR");
                String appserverConfigDir = line.substring(line.indexOf('=') + 1).trim();
                System.getProperties().put("AS_INSTALL_CONFIG_DIR", appserverConfigDir);
            }
            logEvent(this, Log.DBG,line);
            writer.write(line + System.getProperty("line.separator"));
        }
        reader.close();
        
        logEvent(this, Log.DBG,"Finished modifying the file " + setupFileNew.getAbsolutePath());
        
        if (!setupFile.delete()) {
            logEvent(this, Log.ERROR, "Could not delete - " + setupFile.getAbsolutePath());
            return false;
        }
        
        logEvent(this, Log.DBG,"Removing " + setupFile.getAbsolutePath());
        
        writer.close();

        // Now if cannot locate unused port, return false
        if (invalidPortFound) {
            logEvent(this, Log.ERROR, "Could not find valid port -  aborting the install of PE");
            return false;
        }
        
        if (!setupFileNew.renameTo(setupFile)) {
            logEvent(this, Log.ERROR, "Could not rename - " + setupFileNew.getAbsolutePath());
            return false;
        }
        logEvent(this, Log.DBG,"Renaming " + setupFileNew.getAbsolutePath() + " to " + setupFile.getAbsolutePath());        
        return true;
    }
    
    /** Validates the different port numbers(orb,http,web,pointbase)
     * @param portNumber initial portNumber to validate
     * @param numberOfAttempts number of attempts made to find a valid port  
     *                         number. if the inital portNumber is not valid. 
     *                         -1 indicates forever.
     */
    static public int RANDOM_PORT_NUMBER = 3566;
    static public int getValidPortNumber(Log logger, String portNumber, int numberOfTries)
    throws Exception {
	String serverName = "localhost";
	if(Util.isLinuxOS()) {
	    try {
		serverName = java.net.InetAddress.getLocalHost().getHostName();
	    } catch (Exception ex) {
		Util.logStackTrace(logger, ex);
	    }
	}
        
        logger.logEvent(logger, logger.DBG,"serverName -> " + serverName);

        int intportNumber=0;
        if(portNumber == null || portNumber.length() == 0 ||
        portNumber.length() > 5)
            portNumber = Integer.toString(RANDOM_PORT_NUMBER); //default number
        try {
            intportNumber = Integer.parseInt(portNumber);
        }
        catch (NumberFormatException dummy) {
            
        }
        
        /**    if portnumber does not match the range then return false **/
        if(intportNumber <= 0 || intportNumber > 65535)
            intportNumber = RANDOM_PORT_NUMBER;
        
        boolean forever = (numberOfTries < 0) ? true : false;
        while (forever || (numberOfTries >= 0)) {
            try {
                Socket socket = new Socket(serverName, intportNumber);
                OutputStream theOutputStream = socket.getOutputStream();
                InputStream theInputStream = socket.getInputStream();
                theOutputStream.close();
                theOutputStream = null;
                theInputStream.close();
                theInputStream = null;
                socket.close();
                socket = null;
            }
            catch (Exception ex) {
                /**Valid Port/Socket **/               
                //System.out.println("***ValidPort*** -> " + intportNumber + "\n" + ex);
                return intportNumber;
                
            }
            /**InValid Port/Socket **/           
            //System.out.println("InvalidPort-> " + intportNumber + "\n" );
            if (!forever) {
                --numberOfTries;
                if (numberOfTries < 0)
                    throw new RuntimeException("No available ports found.") ;
            }
            intportNumber = ( ++intportNumber > 65535) ?  0 : intportNumber;
        }
        //should never reach here
        return -1;
    }
    
    /**
     * List the files which shouldn't be cleaned up after installation
     *
     * @param  pathname  The abstract pathname to be tested
     * @return  <code>true</code> if and only if <code>pathname</code>
     *         should be included
     */
    public boolean accept(File pathname) {
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
    }
    
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
    
    /** Removes the Uninstall menu item from Appserver start menu */
    public void cleanAppserverStartMenu() {
        if (Util.isWindowsOS()) {
            String folder = "Sun Microsystems" + File.separator +  "J2EE 1.4 SDK";
            logEvent(this, Log.DBG,"Removing Appserver Uninstall From Programs Menu ->" + folder);
            try {
                DesktopService ds = (DesktopService)getService(DesktopService.NAME);
                // context help see: $(ISMP)\InstallShield\MP50\platforms\win32\index.html
		String context = "$UserPrograms$";
                logEvent(this, Log.DBG,"Attr: -> " + ds.getDesktopFolderAttributes(context, folder).toString());
                ds.removeDesktopItem(context, folder, "Uninstall");
		logEvent(this, Log.DBG, "remove menu item Uninstall");
            } catch (ServiceException se) {
                se.printStackTrace();
            }
        }
    }

    /** Return path to AS install/uninstall log files. */
    public String getPEDirLogPath() {
        String dirPath = "";
        if (Util.isWindowsOS()) {
            dirPath = imageDirPath;
        } else if (Util.isMacOSX()) {
            dirPath = imageDirPath;
        } else if (Util.isLinuxOS()) {
            dirPath = imageDirPath;
        } else if (Util.isSunOS()) {
            dirPath = imageDirPath;
        }
        return dirPath;
    }

    public String getPELogPath(String dirPath) {

        File logDir = new File(dirPath);
        
        if (!logDir.exists()) {
            // System.out.println( "Directory doesn't exists - " + logDir.getAbsolutePath());
            return null;
        }
        
        FileFilter ff = new InstallApplicationServerAction.PEFileFilter();
        File[] list = logDir.listFiles(ff);

        if (list == null || list.length < 1) {
            // System.out.println("*.log file not found in - " + logDir.getAbsolutePath());
            return null;
        }
        
        String recentFilePath = (String) list[list.length - 1].getAbsolutePath();
        // System.out.println("log path = " + recentFilePath);

        return recentFilePath;
    }
    
    /** Returns checksum for appserver directory in bytes */
    public long getCheckSum() {
        if (Util.isWindowsOS()) {
            return 105000000L;
        } else if (Util.isSunOS()) {
            return 115000000L;
        } else if (Util.isLinuxOS()) {
            return 105000000L;
        } else if (Util.isMacOSX()) {
            return 105000000L;
        }
        return 105000000L;
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
        
        tmpDir = Util.getTmpDir();
        
        logEvent(this, Log.DBG,"in getRequiredBytes(): tmpDir -> " + tmpDir);
        // same for all platforms, actually Solaris is biggest
        req.addBytes(tmpDir , 40000000L);
        
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
		logEvent(this, Log.ERROR, "FileService is null. Cannot set file as executable: " + filename);
		return false;
	    }
	    fileService.setFileExecutable(filename);
	} catch (Exception ex) {
            logEvent(this, Log.ERROR, "Cannot set file as executable: " + filename
		     + "\nException: " + ex);
	    return false;
	}
	return true;
    }
    
    /** Create the App Server install or uninstall script from the provided 
     *  template.
     * @template - the script to use as a template
     * @scriptName - the name of the generated script
     */
    private boolean createScript(String template, String scriptName, int scriptType)
	throws Exception {
	logEvent(this, Log.DBG, "Creating script: " + scriptName);
	File templateFile = new File(template);
	String parent = templateFile.getParent();
	if (parent == null) {
	    return false; // should always have the _uninst dir as parent
	}

	File scriptFile = new File(parent + File.separator + scriptName);

        BufferedReader reader = new BufferedReader(new FileReader(templateFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(scriptFile));
        
	String logfile;
	if (scriptType == INSTALL) {
	    logfile = "as-install.log";
	} else {
	    logfile = "as-uninstall.log";
	}
	// Replace the script variables with real values
	if (Util.isWindowsOS()) {
	    winScriptSetup(reader, writer, logfile, scriptType);
	} else if (Util.isMacOSX()) {
            String installerName = asSetupDirPath + File.separator + "setup";
            macosxScriptSetup(reader, writer, logfile, installerName, scriptType);
	} else {
            String installerName = instDirPath + File.separator + findASInstaller();
	    unixScriptSetup(reader, writer, logfile, installerName);
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
	} else {
	    logEvent(this, Log.DBG, scriptFile.getAbsolutePath() +
		     " could not be set as executable file.");
	}
	return false;
    }

    private void winScriptSetup(BufferedReader reader, BufferedWriter writer, 
        String logfileName, int type) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("SET APPSERVERDIR")) {
                line = "SET APPSERVERDIR=\"" + imageDirPath + "\"";
            } else if (line.startsWith("SET INSTDIR")) {
		if (type == INSTALL) {
		    line = "SET INSTDIR=\"" + asSetupDirPath + "\"";
		} else {
		    line = "SET INSTDIR=\"" + instDirPath + "\"";
		}
            } else if (line.startsWith("SET STATEFILE")) {
                line = "SET STATEFILE=\"" + statefilePath + "\"";
            } else if (line.startsWith("SET LOGFILE")) {
                line = "SET LOGFILE=\"" + instDirPath + File.separator + logfileName + "\"";
            } else if (line.startsWith("SET TMPDIR")) {
                line = "SET TMPDIR=\"" + tmpDir + "\"";
            } else if (line.startsWith("SET DRIVE")) {
                line = "SET DRIVE="
		+ instDirPath.substring(0, instDirPath.indexOf(File.separator));
            } else if (line.startsWith("SET JAVAHOME")) {
                line = "SET JAVAHOME=\"" + jdkDirPath + "\"";
	    }
            writer.write(line + System.getProperty("line.separator"));
        }
    }

    private void macosxScriptSetup(BufferedReader reader, BufferedWriter writer,
        String logfileName, String installerName, int type) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("EXECNAME")) {
                line = "EXECNAME=" + installerName;
            } else if (line.startsWith("APPSERVERDIR")) {
                line = "APPSERVERDIR=" + imageDirPath;
            } else if (line.startsWith("INSTDIR")) {
                line = "INSTDIR=" + instDirPath;
		if (type == INSTALL) {
		    line = "INSTDIR=" + asSetupDirPath;
		} else {
		    line = "INSTDIR=" + instDirPath;
		}
            } else if (line.startsWith("STATEFILE")) {
                line = "STATEFILE=" + statefilePath;
            } else if (line.startsWith("LOGFILE")) {
                line = "LOGFILE=" + instDirPath + File.separator + logfileName;
            } else if (line.startsWith("TMPDIR")) {
                line = "TMPDIR=" + tmpDir;
            } else if (line.startsWith("JAVAHOME")) {
                line = "JAVAHOME=" + jdkDirPath;
	    }
            writer.write(line + System.getProperty("line.separator"));
        }
    }
    
    private void unixScriptSetup(BufferedReader reader, BufferedWriter writer,
        String logfileName, String installerName) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("EXECNAME")) {
                line = "EXECNAME=" + installerName;
            } else if (line.startsWith("APPSERVERDIR")) {
                line = "APPSERVERDIR=" + imageDirPath;
            } else if (line.startsWith("INSTDIR")) {
                line = "INSTDIR=" + instDirPath;
            } else if (line.startsWith("STATEFILE")) {
                line = "STATEFILE=" + statefilePath;
            } else if (line.startsWith("LOGFILE")) {
                line = "LOGFILE=" + instDirPath + File.separator + logfileName;
            } else if (line.startsWith("TMPDIR")) {
                line = "TMPDIR=" + tmpDir;
            } else if (line.startsWith("JAVAHOME")) {
                line = "JAVAHOME=" + jdkDirPath;
	    }
            writer.write(line + System.getProperty("line.separator"));
        }
    }
    
    private String findASInstaller () {
	String installerName = null;
	String arch = (String) System.getProperty("os.arch");
        File installDirFile = new File(instDirPath);
        logEvent(this, Log.DBG, "createInstallScript installDirFile: " + installDirFile);
        File [] children = installDirFile.listFiles();
        String installerPrefix = resolveString("$L(org.netbeans.installer.Bundle,AS.installerPrefix)");
        if (Util.isWindowsOS()) {
            //Try to locate Windows AS installer
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().startsWith(installerPrefix) && (children[i].getName().indexOf("windows") != -1) &&
                    children[i].getName().endsWith(".exe")) {
                    installerName = children[i].getName();
                    break;
                }
            }
        } else if (Util.isLinuxOS()) {
            //Try to locate Linux AS installer
            for (int i = 0; i < children.length; i++) {
                if (children[i].getName().startsWith(installerPrefix) && (children[i].getName().indexOf("linux") != -1) &&
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
                        children[i].getName().endsWith(".bin")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
	    } else {
                //Try to locate Solaris X86 JDK installer
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().startsWith(installerPrefix) && (children[i].getName().indexOf("solaris-i586") != -1) && 
                        children[i].getName().endsWith(".bin")) {
                        installerName = children[i].getName();
                        break;
                    }
                }
	    }
	}
        if (installerName != null) {
            logEvent(this, Log.DBG, "findASInstaller AS installer found: " + installerName);
        } else {
            logEvent(this, Log.DBG, "findASInstaller AS installer NOT found. AS cannot be installed.");
            installerName = "as-installer-not-found";
        }
        return installerName;
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
            logEvent(this, Log.DBG,"Finished loop loop:" + loop);
        }
        
        public void finish() {
            loop = false;
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
        
        /**check if the operation is canceled. */
        private boolean isCanceled() {
            if (mos.isCanceled() && loop) {
                logEvent(this, Log.DBG,"MOS is cancelled");
                loop = false;
                runCommand.interrupt();
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
    
    /** FileFilter to extract the PE logfile name */
    class PEFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            String name = pathname.getName();

            String logName = resolveString("$L(org.netbeans.installer.Bundle,AS.installerLogName)");
            String prefix = (installMode == INSTALL ? "Install" : "Uninstall") + logName;
            if (name.startsWith(prefix) && name.endsWith(".log")) {
                return true;
            }
            return false;
        }
    }
}
