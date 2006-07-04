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

import java.io.File;
import java.util.StringTokenizer;
import java.util.Vector;
import com.installshield.util.Log;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.util.LocalizedStringResolver;
import com.installshield.wizard.service.WizardServicesUI;

public class JDKSelectionPanel extends DirectoryChooserPanel {

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
            JDKInfoAux desc = (JDKInfoAux) jdkHomeList.elementAt(i);
            item.setValue(desc.getHome());
            if (desc.getVersion().length() > 0) {
                item.setDescription(desc.getHome() + "    (v. " + desc.getVersion() + ")");
            } else {
                item.setDescription(desc.getHome());
            }
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
        
        boolean belowRecommendedJDK = isBelowRecommendedJDK(jdkHome);
        logEvent(this, Log.DBG, "belowRecommendedJDK: " + belowRecommendedJDK);
        Util.setJdkHome(jdkHome);
        // set flag so that we can send user a message in install summary panel.
        if (belowRecommendedJDK) {
            logEvent(this, Log.DBG, "User Selected Java Home is below recommended JDK: " + jdkHome); //NOI18N
        }
        logEvent(this, Log.DBG, "User Selected Java Home: " + jdkHome); //NOI18N
        Util.setBelowRecommendedJDK(belowRecommendedJDK);
        
        return true;
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
        //Following check is done only for asbundle installer on Windows and Linux.
        if (Names.INSTALLER_AS_BUNDLE.equals(Util.getStringPropertyValue(Names.INSTALLER_TYPE)) &&
            (Util.isWindowsOS() || Util.isLinuxOS())) {
            //For asbundle check if JDK is 32bit or 64bit, 64bit is not supported
            //Get os.arch property value
            String jvm = Util.getJVMName();
            File jvmFile = new File(jdkHome + File.separator + "bin" + File.separator + jvm);
            RunCommand runCommand = new RunCommand();
            String [] cmdArr = new String[5];
            cmdArr[0] = jvmFile.getAbsolutePath();
            cmdArr[1] = "-cp";
            cmdArr[2] = JDKSearchAction.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            cmdArr[3] = "org.netbeans.installer.Verify";
            cmdArr[4] = "os.arch";
            runCommand.execute(cmdArr);
            runCommand.waitFor();

            String osArch = runCommand.getOutputLine().trim();
            if (osArch.equals("amd64")) {
                String msg = resolveString("$L(org.netbeans.installer.Bundle,JDKSelectionPanel.64bitJDKNotSupported,"
                + "$L(org.netbeans.installer.Bundle,AS.name))");
                String title = resolveString("$L(org.netbeans.installer.Bundle,JDKSelectionPanel.checkJdkDialogTitle)");
                showErrorMsg(title,msg);
                return false;
            }
        }

        return true;
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
