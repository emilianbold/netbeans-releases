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

package org.netbeans.modules.tomcat5.wizard;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.netbeans.modules.tomcat5.TomcatManager.TomcatVersion;
import org.netbeans.modules.tomcat5.util.TomcatInstallUtil;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.netbeans.modules.tomcat5.ide.*;
import org.netbeans.modules.tomcat5.util.TomcatProperties;

/**
 * Iterator for the add Tomcat server wizard.
 *
 * @author abadea
 */
public class AddInstanceIterator implements WizardDescriptor.InstantiatingIterator {
    
    public final static String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N    
    private final static String PROP_CONTENT_DATA = "WizardPanel_contentData";  // NOI18N
    private final static String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; // NOI18N
    private final static String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N
    
    private final static String[] CONTENT_DATA = new String[] { 
        NbBundle.getMessage(AddInstanceIterator.class, "LBL_InstanceProperties") };

    private WizardDescriptor wizard;
    private InstallPanel panel;
    
    private final TomcatVersion tomcatVersion;
    
    public AddInstanceIterator(TomcatVersion tomcatVersion) {
        this.tomcatVersion = tomcatVersion;
        
    }

    public void removeChangeListener(javax.swing.event.ChangeListener l) {
    }

    public void addChangeListener(javax.swing.event.ChangeListener l) {
    }

    public void uninitialize(WizardDescriptor wizard) {
    }

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
    }

    public void previousPanel() {
        throw new NoSuchElementException();
    }

    public void nextPanel() {
        throw new NoSuchElementException();
    }

    public String name() {
        return null;
    }

    public Set instantiate() throws java.io.IOException {
        Set result = new HashSet();
        String displayName = getDisplayName();
        String url = panel.getVisual().getUrl();
        String username = panel.getVisual().getUsername();
        String password = panel.getVisual().getPassword();
        try {
            InstanceProperties ip = InstanceProperties.createInstanceProperties(
                    url, username, password, displayName);
            Properties prop = panel.getVisual().getProperties ();
            Enumeration en = prop.propertyNames ();
            while (en.hasMoreElements ()) {
                String key = (String) en.nextElement ();
                ip.setProperty (key, prop.getProperty (key));
            }
            ip.setProperty(TomcatProperties.PROP_RUNNING_CHECK_TIMEOUT,
                    Integer.toString(TomcatProperties.DEF_VALUE_RUNNING_CHECK_TIMEOUT));
            
            result.add(ip);
            checkStartupScript(panel.getVisual().getHomeDir());
        } catch (Exception ex) {
            ErrorManager.getDefault().log(ErrorManager.EXCEPTION, ex.getMessage());
        }
        return result;
    }

    public boolean hasPrevious() {
        return false;
    }

    public boolean hasNext() {
        return false;
    }

    public WizardDescriptor.Panel current() {
        if (panel == null) {
            panel = new InstallPanel(tomcatVersion);
        }
        setContentData((JComponent)panel.getComponent());
        setContentSelectedIndex((JComponent)panel.getComponent());
        return panel;
    }

    private void setContentData(JComponent component) {
        if (component.getClientProperty(PROP_CONTENT_DATA) == null) {
            component.putClientProperty(PROP_CONTENT_DATA, CONTENT_DATA);
        }
    }

    private void setContentSelectedIndex(JComponent component) {
        if (component.getClientProperty(PROP_CONTENT_SELECTED_INDEX) == null) {
            component.putClientProperty(PROP_CONTENT_SELECTED_INDEX, new Integer(0));
        }
    }

    private String getDisplayName() {
        return (String)wizard.getProperty(PROP_DISPLAY_NAME);
    }
    
    /** check for missing startup script - workaround for Tomcat Windows installer distribution */
    private void checkStartupScript(File homeDir) {
        String CATALINA = Utilities.isWindows() ? StartTomcat.CATALINA_BAT 
                                                : StartTomcat.CATALINA_SH;
        boolean catalinaOK = new File(homeDir, "bin/" + CATALINA).exists(); // NOI18N

        String SETCLASSPATH = Utilities.isWindows() ? StartTomcat.SETCLASSPATH_BAT
                                                    : StartTomcat.SETCLASSPATH_SH;
        boolean setclasspathOK = new File(homeDir, "bin/" + SETCLASSPATH).exists(); // NOI18N

        if (!catalinaOK || !setclasspathOK) {
            File bundledHome = TomcatInstallUtil.getBundledHome();
            // INFO: DO NOT FORGET TO CHANGE THE BUNDLED TOMCAT VERSION (AND THE VERSION STRING BELLOW) WHEN UPGRADING
            //       5.5.17  - hopefully this string helps not to miss it;)
            if (tomcatVersion != TomcatManager.TomcatVersion.TOMCAT_55 
                    || bundledHome == null || !bundledHome.exists()) {
                // If there is no bundled Tomcat or the being installed Tomcat is a different
                // version, there is no place where to get the startup scripts from. Lets inform 
                // the user about the problem at least.
                String msg;
                if (!catalinaOK && !setclasspathOK) {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_scripts_fix_by_hand", CATALINA, SETCLASSPATH);
                } else {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_script_fix_by_hand", !catalinaOK ? CATALINA : SETCLASSPATH);
                }
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            } else {
                // The IDE can copy the startup scripts for the user, lets ask
                // him whether to do it or not.
                String msg;
                if (!catalinaOK && !setclasspathOK) {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_scripts", CATALINA, SETCLASSPATH);
                } else {
                    msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_no_startup_script", !catalinaOK ? CATALINA : SETCLASSPATH);
                }
                NotifyDescriptor nd =
                        new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
                if (DialogDisplayer.getDefault().notify(nd).equals(NotifyDescriptor.YES_OPTION)) {
                    try {
                        if (bundledHome != null) {
                            if (!catalinaOK) {
                                FileUtil.copyFile(
                                    FileUtil.toFileObject(new File(bundledHome, "bin/" + CATALINA)), // NOI18N
                                    FileUtil.toFileObject(new File(homeDir, "bin")),    // NOI18N
                                    CATALINA.substring(0, CATALINA.indexOf("."))    // NOI18N
                                );
                            }
                            if (!setclasspathOK) {
                                FileUtil.copyFile(
                                    FileUtil.toFileObject(new File(bundledHome, "bin/" + SETCLASSPATH)), // NOI18N
                                    FileUtil.toFileObject(new File(homeDir, "bin")),        // NOI18N
                                    SETCLASSPATH.substring(0, SETCLASSPATH.indexOf("."))    // NOI18N
                                );
                            }
                        }
                    } catch (IOException e) {
                        msg = NbBundle.getMessage(AddInstanceIterator.class, "MSG_startup_scripts_copy_failed");
                        nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }
            }
        }
    }
}
