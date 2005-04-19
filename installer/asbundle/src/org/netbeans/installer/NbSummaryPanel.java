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

import com.installshield.product.RequiredBytesTable;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.product.service.product.ProductService;
import com.installshield.product.wizardbeans.InstallAction;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.wizard.RunnableWizardBeanState;
import com.installshield.wizard.service.WizardLog;
import com.installshield.wizard.service.file.FileService;
import java.io.File;

import java.util.Properties;

public class NbSummaryPanel extends TextDisplayPanel
{
    private int type = ProductService.PRE_INSTALL;
    
    private String nbInstallDir = "";
    private String j2seInstallDir = "";
    private String jreInstallDir = "";
    
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
        logEvent(this, Log.DBG, "queryEnter ENTER");
        boolean okay = super.queryEnter(evt);
        try {
            ProductService service = (ProductService) getService(ProductService.NAME);
            String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
            nbInstallDir = (String) service.getProductBeanProperty
            (productURL, Names.CORE_IDE_ID, "installLocation");
            j2seInstallDir = (String) System.getProperties().get("j2seInstallDir");
            jreInstallDir = (String) System.getProperties().get("jreInstallDir");
            logEvent(this, Log.DBG, "queryEnter nbInstallDir: " + nbInstallDir);
            if (type == ProductService.POST_INSTALL) {
                logEvent(this, Log.DBG, "queryEnter POST_INSTALL PANEL");
                logEvent(this, Log.DBG, "queryEnter exitCode: " + getWizard().getExitCode());
                //#48305: Method GenericSoftwareObject.getInstallStatus() does not work. It returns
                //always 0. We must use getWizard().getExitCode() as workaround.
                //ProductTree pt = service.getSoftwareObjectTree(productURL);
                //GenericSoftwareObject gso = (GenericSoftwareObject) pt.getRoot();
                //if (gso.getInstallStatus() == gso.UNINSTALLED) {
                if (getWizard().getExitCode() != -1) {
                    //Installation failed or cancelled.
                    String summaryMessage;
                    if (getWizard().getExitCode() == InstallApplicationServerAction.AS_UNHANDLED_ERROR) {
                        summaryMessage = "$L(org.netbeans.installer.Bundle,SummaryPanel.description1)";
                        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,Product.displayName)";
                        summaryMessage += " " + "$L(org.netbeans.installer.Bundle,SummaryPanel.description3)";
                        summaryMessage += "<br><br>"
                        + "$L(org.netbeans.installer.Bundle,Product.displayName)" + " "
                        + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
                        + nbInstallDir;
                        summaryMessage += "<br><br>"
                        + "$L(org.netbeans.installer.Bundle, SummaryPanel.errorAS,"
                        + "$L(org.netbeans.installer.Bundle, AS.shortName),"
                        +  nbInstallDir + File.separator + InstallApplicationServerAction.IMAGE_DIRECTORY_NAME + ")";
                    } else {
                        InstallAction ia = (InstallAction) getWizardTree().getBean("install");
                        RunnableWizardBeanState state = ia.getState();
                        if (state.getState() == state.CANCELED) {
                            //User cancelled installation (install action)
                            removeAllFiles();
                            summaryMessage = resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.cancel)");
                        } else {
                            logEvent(this, Log.DBG, "queryEnter INSTALLATION FAILED");
                            Properties summary = service.getProductSummary(
                            ProductService.DEFAULT_PRODUCT_SOURCE,
                            ProductService.POST_INSTALL,
                            ProductService.HTML);
                            summaryMessage = summary.getProperty(ProductService.SUMMARY_MSG);
                            summaryMessage += resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.errorNB)");
                        }
                    }
                    setText(summaryMessage);
                } else {
                    //setText(resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.description)"));
                    logEvent(this, Log.DBG, "queryEnter INSTALLATION SUCCESSFUL");
                    logEvent(this, Log.DBG, "queryEnter summaryPostInstallMsg:'" + getPostInstallSummaryMessage() + "'");
                    setText(getPostInstallSummaryMessage());
                }
            } else if (type == ProductService.PRE_INSTALL) {
                /*Properties summary = service.getProductSummary(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                type,
                ProductService.HTML);
                setText(summary.getProperty(ProductService.SUMMARY_MSG));*/
                logEvent(this, Log.DBG, "queryEnter PRE_INSTALL PANEL");
                logEvent(this, Log.DBG, "summaryPreInstallMsg:'" + getPreInstallSummaryMessage() + "'");
                setText(getPreInstallSummaryMessage());
            } else if (type == ProductService.POST_UNINSTALL) {
                logEvent(this, Log.DBG, "queryEnter POST_UNINSTALL PANEL");
                logEvent(this, Log.DBG, "summaryPostUninstallMsg:'" + getPostUninstallSummaryMessage() + "'");
                setText(getPostUninstallSummaryMessage());
            } else if (type == ProductService.PRE_UNINSTALL) {
                logEvent(this, Log.DBG, "queryEnter PRE_UNINSTALL PANEL");
                logEvent(this, Log.DBG, "summaryPreUninstallMsg:'" + getPreUninstallSummaryMessage() + "'");
                setText(getPreUninstallSummaryMessage());
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
        
        try {
            FileService fileService = (FileService) getService(FileService.NAME);
            logEvent(this, Log.DBG, "removeAllFiles Deleting completely: " + nbInstallDir);
            int ret = fileService.deleteDirectory(nbInstallDir,false,true);
            logEvent(this, Log.DBG, "removeAllFiles Done. " + nbInstallDir + " deleted.");
        } catch (ServiceException ex) {
            //Nothing to do. Ignore.
            System.out.println("serviceexception ex:" + ex);
            ex.printStackTrace();
        }
    }
    
    private String getPostInstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle, SummaryPanel.description1)";
        summaryMessage += " " + "$L(org.netbeans.installer.Bundle, Product.displayName)"
        + " " + "$L(org.netbeans.installer.Bundle, SummaryPanel.description2)";
        summaryMessage += " " + "$L(org.netbeans.installer.Bundle, AS.shortName)";
        summaryMessage += " " + "$L(org.netbeans.installer.Bundle, SummaryPanel.description3)";
        
        //Location of NB
        summaryMessage += "<br><br>"
        + "$L(org.netbeans.installer.Bundle,Product.displayName)" + " "
        + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
        + nbInstallDir;
        
        //Location of AS
        if (Util.isMacOSX()) {
            summaryMessage += "<br><br>"
            + "$L(org.netbeans.installer.Bundle,AS.shortName)" + " "
            + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
            + nbInstallDir
            + File.separator + resolveString("$L(org.netbeans.installer.Bundle,Product.nbLocationBelowInstallRoot)")
            + File.separator + InstallApplicationServerAction.IMAGE_DIRECTORY_NAME;
        } else {
            summaryMessage += "<br><br>"
            + "$L(org.netbeans.installer.Bundle,AS.shortName)" + " "
            + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
            + nbInstallDir + File.separator + InstallApplicationServerAction.IMAGE_DIRECTORY_NAME;
        }
        
        //How to run IDE
        if (Util.isWindowsOS()) {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description51,netbeans.exe)";
        } else if (Util.isMacOSX()) {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description52)";
        } else {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description51,netbeans)";
        }
        
        //How to run uninstaller
        if (Util.isWindowsOS()) {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description53,uninstaller.exe)";
        } else {
            summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.description53,uninstaller)";
        }
        
        //Info about default AS administrator UN/PW
        summaryMessage += "$L(org.netbeans.installer.Bundle,SummaryPanel.adminInfo)";
        
        return summaryMessage;
    }
    
    private String getPreInstallSummaryMessage() {
        String summaryMessage = resolveString("$L(org.netbeans.installer.Bundle,PreviewPanel.previewInstallMessage,"
        + "$L(org.netbeans.installer.Bundle,Product.displayName),"
        + "$L(org.netbeans.installer.Bundle,AS.name))")
        + "<br>" + nbInstallDir;
        
        summaryMessage += "<br>" + "$L(org.netbeans.installer.Bundle,PreviewPanel.previewSize)" + "<br>"
        + getTotalSize();
        return summaryMessage;
    }
    
    private String getPostUninstallSummaryMessage() {
        String summaryMessage = resolveString("$L(org.netbeans.installer.Bundle, PreviewPanel.previewPostUninstallMessage," 
        + "$L(org.netbeans.installer.Bundle, Product.displayName),"
        + "$L(org.netbeans.installer.Bundle, AS.name))");
        
        summaryMessage += "<br><br>"
        + resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.descriptionPostUninstall,"
        + "$L(org.netbeans.installer.Bundle, Product.userDir))");
        if (Util.isWindowsOS()) {
            summaryMessage += " "
            + resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.descriptionPostUninstallWindows)");
        } else {
            summaryMessage += " "
            + resolveString("$L(org.netbeans.installer.Bundle, SummaryPanel.descriptionPostUninstallUnix)");
        }
        
        return summaryMessage;
    }
    
    private String getPreUninstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle, PreviewPanel.previewPreUninstallMessage," 
        + "$L(org.netbeans.installer.Bundle, Product.displayName),"
        + "$L(org.netbeans.installer.Bundle, AS.name),"
        + nbInstallDir + ")";
        
        return summaryMessage;
    }
    
    private String getTotalSize() {
        RequiredBytesTable table;
        long size = 0;
        long asSize = 0;
        String mBytes = null;
        
        try {
            ProductService service = (ProductService)getService(ProductService.NAME);
            String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
            table = service.getRequiredBytes(productURL,Names.CORE_IDE_ID);
            size = table.getBytes(nbInstallDir) >> 20;
            logEvent(this, Log.DBG, "Size of NetBeans: " + size);
            
            asSize = getASSize() >> 20;
            logEvent(this, Log.DBG, "Adjusted size of AS: " + asSize);
            size = size + asSize;
        } catch (ServiceException e) {
            logEvent(this, Log.ERROR, e);
        }
        if (size > 0) {
            mBytes = Long.toString(size);
        } else {
            logEvent(this, Log.ERROR, "Error: Total product installation size is 0.");
        }
        return mBytes + " MB";  //NOI18N
    }
    
    private long getASSize() {
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
}
