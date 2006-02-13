/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.installer.cluster;

import com.installshield.wizard.WizardBeanEvent;
import com.installshield.product.service.product.ProductService;
import com.installshield.product.wizardbeans.InstallAction;
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.wizard.RunnableWizardBeanState;
import com.installshield.wizard.service.WizardLog;
import com.installshield.wizard.service.file.FileService;

import java.util.Properties;

public class NbSummaryPanel extends TextDisplayPanel
{
    private int type = ProductService.PRE_INSTALL;
    
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
                //ProductTree pt = service.getSoftwareObjectTree(ProductService.DEFAULT_PRODUCT_SOURCE);
                //GenericSoftwareObject gso = (GenericSoftwareObject) pt.getRoot();
                //if (gso.getInstallStatus() == gso.UNINSTALLED) {
                logEvent(this, Log.DBG, "queryEnter exitCode: " + getWizard().getExitCode());
                if (getWizard().getExitCode() != -1) {
                    //Installation failed/cancelled.
                    InstallAction ia = (InstallAction) getWizardTree().getBean("install");
                    RunnableWizardBeanState state = ia.getState();
                    if (state.getState() == state.CANCELED) {
                        //User cancelled installation (install action)
                        removeAllFiles();
                        String summaryMessage = resolveString("$L(org.netbeans.installer.cluster.Bundle, SummaryPanel.cancel)");
                        setText(summaryMessage);
                    } else {
                        Properties summary = service.getProductSummary(
                        ProductService.DEFAULT_PRODUCT_SOURCE,
                        ProductService.POST_INSTALL,
                        ProductService.HTML);
                        String summaryMessage = summary.getProperty(ProductService.SUMMARY_MSG);
                        summaryMessage += resolveString("$L(org.netbeans.installer.cluster.Bundle, SummaryPanel.error)");
                        setText(summaryMessage);
                    }
                } else {
                    setText(resolveString("$L(org.netbeans.installer.cluster.Bundle, SummaryPanel.description)"));
                }
            } else {
                Properties summary = service.getProductSummary(
                ProductService.DEFAULT_PRODUCT_SOURCE,
                type,
                ProductService.HTML);
                setText(summary.getProperty(ProductService.SUMMARY_MSG));
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
            String installDir = resolveString("$P(absoluteInstallLocation)");
            FileService fileService = (FileService) getService(FileService.NAME);
            String sep = fileService.getSeparator();
            String file = installDir + sep + "_uninst" + sep + "install.log";
            int ret = fileService.deleteFile(file);
            file = installDir + sep + "_uninst";
            ret = fileService.deleteDirectory(file);
            ret = fileService.deleteDirectory(installDir);
        } catch (ServiceException ex) {
            //Nothing to do. Ignore.
            System.out.println("serviceexception ex:" + ex);
            ex.printStackTrace();
        }
    }
}
