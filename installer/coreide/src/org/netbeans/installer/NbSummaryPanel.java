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

import com.installshield.wizard.WizardBeanEvent;
import com.installshield.product.service.product.ProductService;
import com.installshield.product.wizardbeans.InstallAction;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.wizard.RunnableWizardBeanState;
import com.installshield.wizard.service.WizardLog;
import com.installshield.wizard.service.file.FileService;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Properties;

public class NbSummaryPanel extends TextDisplayPanel {

    private int type = ProductService.PRE_INSTALL;

    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";

    public NbSummaryPanel() {
        setTextSource(TEXT_PROPERTY);
        setContentType(HTML_CONTENT_TYPE);
        setDescription("");
    }
    
    public int getType() {
        return type;        
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public boolean queryEnter(WizardBeanEvent evt) {
        boolean okay = super.queryEnter(evt);
        
        try {
            ProductService service = (ProductService) getService(ProductService.NAME);
            if (type == ProductService.POST_INSTALL) {
                //#48305: Method GenericSoftwareObject.getInstallStatus() does not work. It returns
                //always 0. We must use getWizard().getExitCode() as workaround.
                //if (gso.getInstallStatus() == gso.UNINSTALLED) {
                //ProductTree productTree = service.getSoftwareObjectTree(ProductService.DEFAULT_PRODUCT_SOURCE,null);
                //GenericSoftwareObject gso = (GenericSoftwareObject) productTree.getRoot();
                logEvent(this, Log.DBG, "queryEnter exitCode: " + getWizard().getExitCode());
                if (getWizard().getExitCode() != -1) {
                    //Installation failed or was cancelled.
                    InstallAction ia = (InstallAction) getWizardTree().getBean("install");
                    RunnableWizardBeanState state = ia.getState();
                    if (state.getState() == state.CANCELED) {
                        //User cancelled installation (install action)
                        removeAllFiles();
                        String msg = resolveString(BUNDLE + "SummaryPanel.cancel)");
                        logEvent(this,Log.DBG,"msg: " + msg);
                        setText(msg);
                    } else if (getWizard().getExitCode() == UnpackJarsAction.UNPACK_JARS_UNHANDLED_ERROR) {
                        String msg = resolveString(BUNDLE + "SummaryPanel.errorUnpack)");
                        logEvent(this,Log.DBG,"msg: " + msg);
                        setText(msg);
                    } else if (getWizard().getExitCode() == UnpackJarsAction.UNPACK_JARS_MD5_ERROR) {
                        String msg = resolveString(BUNDLE + "SummaryPanel.errorUnpackMD5)");
                        logEvent(this,Log.DBG,"msg: " + msg);
                        setText(msg);
                    } else {
                        String msg = resolveString(BUNDLE + "SummaryPanel.error)");
                        logEvent(this,Log.DBG,"msg: " + msg);
                        setText(msg);
                    }
                } else {
                    boolean ret = scanLogFile();
                    if (ret) {
                        //Successfull
                        if (Util.isWindowsOS()) {
                            String msg = resolveString
                            (BUNDLE + "SummaryPanel.description,netbeans.exe,uninstaller.exe)");
                            logEvent(this,Log.DBG,"msg: " + msg);
                            setText(msg);
                        } else {
                            String msg = resolveString
                            (BUNDLE + "SummaryPanel.description,netbeans,uninstaller)");
                            logEvent(this,Log.DBG,"msg: " + msg);
                            setText(msg);
                        }
                    } else {
                        //Failure
                        String msg = resolveString(BUNDLE + "SummaryPanel.errorScanLogFile)");
                        logEvent(this,Log.DBG,"msg: " + msg);
                        setText(msg);
                    }
                }
            } else {
                Properties summary = service.getProductSummary(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                type,
                ProductService.HTML);
                String msg = summary.getProperty(ProductService.SUMMARY_MSG);
                if (type == ProductService.POST_UNINSTALL) {
                    msg += "<br><br>"
                    + resolveString(BUNDLE + "SummaryPanel.descriptionPostUninstall,"
                    + BUNDLE + "Product.userDir))");
                    if (Util.isWindowsOS()) {
                        msg += " "
                        + resolveString(BUNDLE + "SummaryPanel.descriptionPostUninstallWindows)");
                    } else {
                        msg += " "
                        + resolveString(BUNDLE + "SummaryPanel.descriptionPostUninstallUnix)");
                    }
                }
                logEvent(this, Log.DBG, "msg: " + msg);
                setText(msg);
            }
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        return okay;
    }
    
    /** Remove created files/dirs when user cancels installation. */
    private void removeAllFiles () {
        WizardLog wizardLog = getWizard().getServices().getWizardLog();
        wizardLog.setLogOutputEnabled(false);
        
        String installDir = resolveString("$P(absoluteInstallLocation)");
        try {
            FileService fileService = (FileService) getService(FileService.NAME);
            String sep = fileService.getSeparator();
            String file = installDir + sep + "_uninst" + sep + "install.log";
            int ret = fileService.deleteFile(file);
            
            file = installDir + sep + "_uninst" + sep + "storagebuilder";
            if (fileService.fileExists(file)) {
                fileService.deleteDirectory(file);
            }
            
            file = installDir + sep + "_uninst";
            ret = fileService.deleteDirectory(file);
            
            ret = fileService.deleteDirectory(installDir);
        } catch (ServiceException ex) {
            //Nothing to do. Ignore.
            System.out.println("serviceexception ex:" + ex);
            ex.printStackTrace();
        }
    }
    
    /** Scans log file for possible errors. Look for java.util.zip.ZipException
     * and java.io.FileNotFoundException. Their presence means corrupted data.
     */
    private boolean scanLogFile () {
        WizardLog wizardLog = getWizard().getServices().getWizardLog();
        String logFileName = wizardLog.getLogOutput();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(logFileName));
            String line;
            while ((line = reader.readLine()) != null) {
                if ((line.indexOf("java.util.zip.ZipException") != -1) ||
                    (line.indexOf("java.io.FileNotFoundException") != -1)) {
                    return false;
                }
            }
        } catch (FileNotFoundException exc) {
            logEvent(this, Log.ERROR, "scanLogFile Exception: " + exc.getMessage());
            Util.logStackTrace(this,exc);
        } catch (IOException exc) {
            logEvent(this, Log.ERROR, "scanLogFile Exception: " + exc.getMessage());
            Util.logStackTrace(this,exc);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException exc) {
                    logEvent(this, Log.ERROR, "scanLogFile Exception: " + exc.getMessage());
                    Util.logStackTrace(this,exc);
                }
            }
        }
        return true;
    }
    
}
