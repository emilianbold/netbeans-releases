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
import com.installshield.wizard.service.ServiceException;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.product.GenericSoftwareObject;
import com.installshield.product.ProductTree;

import java.util.Properties;

public class NbSummaryPanel extends TextDisplayPanel
{
    private int type = ProductService.PRE_INSTALL;
    
    private String nbInstallDir = "";
    
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
            (productURL, "beanNB", "installLocation");
            logEvent(this, Log.DBG, "queryEnter nbInstallDir: " + nbInstallDir);
            if (type == ProductService.POST_INSTALL) {
                logEvent(this, Log.DBG, "queryEnter POST_INSTALL PANEL");
                ProductTree pt = service.getSoftwareObjectTree(productURL);
                GenericSoftwareObject gso = (GenericSoftwareObject) pt.getRoot();
                
                if (gso.getInstallStatus() == gso.UNINSTALLED) {
                    //Installation failed
                    Properties summary = service.getProductSummary(
                    productURL,
                    ProductService.POST_INSTALL,
                    ProductService.HTML);
                    logEvent(this, Log.DBG, "queryEnter INSTALLATION FAILED");
                    logEvent(this, Log.DBG, "queryEnter summaryPostMsg:'"
                    + summary.getProperty(ProductService.SUMMARY_MSG) + "'");
                    setText(summary.getProperty(ProductService.SUMMARY_MSG));
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
    
    private String getPostInstallSummaryMessage() {
        String j2seInstallDir = (String) System.getProperties().get("j2seInstallDir");
        
        String summaryMessage = "$L(org.netbeans.installer.Bundle,SummaryPanel.description1)";
        if (!Util.isJDKAlreadyInstalled()) {
            summaryMessage = summaryMessage + " "
            + "$L(org.netbeans.installer.Bundle,JDK.shortName)"
            + " " + "$L(org.netbeans.installer.Bundle,SummaryPanel.description2)";
        }
        summaryMessage = summaryMessage + " "
        + "$L(org.netbeans.installer.Bundle,Product.displayName)";
        summaryMessage = summaryMessage + " "
        + "$L(org.netbeans.installer.Bundle,SummaryPanel.description3)";
        
        if (!Util.isJDKAlreadyInstalled()) {
            summaryMessage = summaryMessage + "<br><br>"
            + "$L(org.netbeans.installer.Bundle,JDK.shortName)" + " "
            + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
            + j2seInstallDir;
        }
        
        summaryMessage = summaryMessage + "<br><br>"
        + "$L(org.netbeans.installer.Bundle,Product.displayName)" + " "
        + "$L(org.netbeans.installer.Bundle,SummaryPanel.description4)" + "<br>"
        + nbInstallDir;
        
        if (Util.isWindowsOS()) {
            summaryMessage = summaryMessage
            + "$L(org.netbeans.installer.Bundle,SummaryPanel.description5,netbeans.exe,uninstaller.exe)";
        } else {
            summaryMessage = summaryMessage
            + "$L(org.netbeans.installer.Bundle,SummaryPanel.description5,netbeans,uninstaller)";
        }
        
        //How to uninstall JDK. We will show this message only when we installed
        //JDK.
        if (!Util.isJDKAlreadyInstalled()) {
            if (Util.isWindowsOS()) {
                summaryMessage = summaryMessage
                + "$L(org.netbeans.installer.Bundle,SummaryPanel.description7,"
                + "$L(org.netbeans.installer.Bundle,JDK.shortName))";
            } else {
                summaryMessage = summaryMessage
                + "$L(org.netbeans.installer.Bundle,SummaryPanel.description6,"
                + "$L(org.netbeans.installer.Bundle,JDK.shortName),"
                + j2seInstallDir + ",uninstall.sh)";
            }
        }
        
        return summaryMessage;
    }
    
    private String getPreInstallSummaryMessage() {
        String j2seInstallDir = (String) System.getProperties().get("j2seInstallDir");
        
        String summaryMessage = "$L(org.netbeans.installer.Bundle,Product.displayName)"
        + " "
        + "$L(org.netbeans.installer.Bundle,PreviewPanel.previewInstallMessage)"
        + "<br>" + nbInstallDir;
        
        // only show j2se install if no prior installation was found
        if (!Util.isJDKAlreadyInstalled()) {
            summaryMessage = summaryMessage + "<br>" + "<br>"
            + "$L(org.netbeans.installer.Bundle,JDK.shortName)"
            + " "
            + "$L(org.netbeans.installer.Bundle,PreviewPanel.previewInstallMessage)" + "<br>"
            + j2seInstallDir + "<br>";
        }
        
        summaryMessage = summaryMessage + "<br>"
        + "$L(org.netbeans.installer.Bundle,PreviewPanel.previewSize)" + "<br>"
        + getTotalSize();
        return summaryMessage;
    }
    
    private String getPostUninstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle,PreviewPanel.previewPostUninstallMessage," 
        + "$L(org.netbeans.installer.Bundle,Product.displayName))";
        
        return summaryMessage;
    }
    
    private String getPreUninstallSummaryMessage() {
        String summaryMessage = "$L(org.netbeans.installer.Bundle,PreviewPanel.previewPreUninstallMessage," 
        + "$L(org.netbeans.installer.Bundle,Product.displayName),"
        + nbInstallDir + ")";
        
        return summaryMessage;
    }
    
    private String getTotalSize() {
        RequiredBytesTable table;
        long size = 0;
        long j2seSize = 0;
        String mBytes = null;
        
        try {
            ProductService service = (ProductService)getService(ProductService.NAME);
            String productURL = ProductService.DEFAULT_PRODUCT_SOURCE;
            table = service.getRequiredBytes(productURL,"beanNB"); //NOI18N
            size = table.getBytes(nbInstallDir) >> 20;
            logEvent(this, Log.DBG, "Size of NetBeans: " + size);
            
            if (!Util.isJDKAlreadyInstalled()) {
                j2seSize = getJ2SESize() >> 20;
                logEvent(this, Log.DBG, "Adjusted size of J2SE: " + j2seSize);
                size = size +j2seSize;
            }
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
    
    private long getJ2SESize() {
        if (Util.isWindowsOS()) {
            return 135000000L;
        } else if (Util.isLinuxOS()) {
            return 140000000L;
        } else if (Util.isSunOS()) {
            return 140000000L;
        }
        return 100000000L;
    }
}
