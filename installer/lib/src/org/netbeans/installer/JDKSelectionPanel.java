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

package org.netbeans.installer;

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import com.installshield.util.Log;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.util.LocalizedStringResolver;
import com.installshield.wizard.service.WizardServicesUI;

public class JDKSelectionPanel extends DirectoryChooserPanel 
{    
    private String jdkHome;

    public boolean queryEnter(WizardBeanEvent event) {
        
        // Get the object of JDKSearchAction set in the system property (set by JDKSearchAction)
        // and retrieve the searched JDK Home List
        Vector jdkHomeList = JDKSearchAction.getJdkList();
        
        String description = null;
        if (jdkHomeList.size() != 0) {
            if (Util.isMacOSX() && Util.isASBundle()) {
                description = resolveString
                ("$L(org.netbeans.installer.Bundle,JDKSelectionPanel.description_MacOSX_ASBundle)");
            } else {
                description = resolveString
                ("$L(org.netbeans.installer.Bundle,JDKSelectionPanel.description)");
            }
        } else {
            if (Util.isMacOSX() && Util.isASBundle()) {
                description = resolveString
                ("$L(org.netbeans.installer.Bundle,JDKSelectionPanel.descriptionJdkNotFound_MacOSX_ASBundle)");
            } else {
                description = resolveString
                ("$L(org.netbeans.installer.Bundle,JDKSelectionPanel.descriptionJdkNotFound)");
            }
        }
        
        setDescription(description);
        
        setDestinationCaption("$L(org.netbeans.installer.Bundle, JDKSelectionPanel.jdkHomeLabel)");
        
        String listLabelText = resolveString("$L(org.netbeans.installer.Bundle,JDKSelectionPanel.jdkListLabel)"); //NOI18N
        setDestinationsCaption(listLabelText);
        
        destinations = new Vector(jdkHomeList.size());
        for (int i = 0; i < jdkHomeList.size(); i++) {
            DestinationItem item = new DestinationItem();
            String desc = (String) jdkHomeList.elementAt(i);
            item.setValue(trimVersion(desc));
            item.setDescription(desc);
            destinations.add(item);
        }
        setSelectedDestinationIndex(""+JDKSearchAction.getLatestVersionIndex());
        
        return true;
    }
    
    public boolean queryExit(WizardBeanEvent event) {
        jdkHome = getDestination().trim();
        if(!checkJDK(jdkHome)) {
            return false;
        }
        
        return true;
    }
    
    public void exited(WizardBeanEvent event) {
        super.exited(event);
        
        boolean belowRecommendedJDK = isBelowRecommendedJDK(jdkHome);
        logEvent(this, Log.DBG, "belowRecommendedJDK: " + belowRecommendedJDK);
        Util.setJdkHome(jdkHome);
        // set flag so that we can send user a message in install summary panel.
        if (belowRecommendedJDK) {
            logEvent(this, Log.DBG, "User Selected Java Home is below recommended JDK: " + jdkHome); //NOI18N
        }
        logEvent(this, Log.DBG, "User Selected Java Home: " + jdkHome); //NOI18N
        Util.setBelowRecommendedJDK(belowRecommendedJDK);
    }
    
    private boolean isBelowRecommendedJDK(String jdkHome) {
        if (JDKVersion.isBelowRecommendedJDK(jdkHome)) {
            logEvent(this, Log.DBG, "User Selected Java Home is below recommended JDK: " + jdkHome); //NOI18N
            return true;
        }
        return false;
    }
    
    private boolean checkJDK(String jdkHome) {
        if (jdkHome.trim().equals("")) {
            showLocalizedErrorMsg
            ("org.netbeans.installer.Bundle",
             "JDKSelectionPanel.checkJdkDialogTitle",
             "JDKSelectionPanel.emptyJdkHomeField");
            return false;
        }
        
        File jdkDir = new File(jdkHome);
        jdkHome = jdkDir.getAbsolutePath();
        
        if (!JDKInfo.checkJdkHome(this, jdkHome)) {
            if (Util.isMacOSX() && Util.isASBundle()) {
                showLocalizedErrorMsg
                ("org.netbeans.installer.Bundle",
                 "JDKSelectionPanel.checkJdkDialogTitle",
                 "JDKSelectionPanel.invalidJdkHome_MacOSX_ASBundle");
            } else {
                showLocalizedErrorMsg
                ("org.netbeans.installer.Bundle",
                 "JDKSelectionPanel.checkJdkDialogTitle",
                 "JDKSelectionPanel.invalidJdkHome");
            }
            return false;
        }
        return true;
    }
    
    private String trimVersion(String jdkPath){
        StringTokenizer st = new StringTokenizer(jdkPath,"(");
        return st.nextToken().trim();
    }
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    protected void showLocalizedErrorMsg(String bundle, String titleKey, String msgKey) {
        try {
            String title = LocalizedStringResolver.resolve(bundle, titleKey);
            String msg = LocalizedStringResolver.resolve(bundle, msgKey);
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
    
    protected void showLocalizedErrorMsg(String bundle, String titleKey, String msgKey, String[] params) {
        try {
            String title = LocalizedStringResolver.resolve(bundle, titleKey);
            String msg = LocalizedStringResolver.resolve(bundle, msgKey, params);
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
}
