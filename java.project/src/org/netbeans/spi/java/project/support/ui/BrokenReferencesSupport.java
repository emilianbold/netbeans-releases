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

package org.netbeans.spi.java.project.support.ui;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.java.project.BrokenReferencesAlertPanel;
import org.netbeans.modules.java.project.BrokenReferencesCustomizer;
import org.netbeans.modules.java.project.BrokenReferencesModel;
import org.netbeans.modules.java.project.JavaSettings;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Support for managing broken project references. Project freshly checkout from
 * VCS can has broken references of several types: reference to other project, 
 * reference to a foreign file, reference to an external source root, reference
 * to a Java Library or reference to a Java Platform. This class has helper
 * methods for detection of these problems and for fixing them.
 * <div class="nonnormative">
 * Typical usage of this class it to check whether the project has some broken
 * references and if it has then providing an action on project's node which
 * allows to correct these configuration problems by showing broken references
 * customizer.
 * </div>
 * @author David Konecny
 */
public class BrokenReferencesSupport {

    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;

    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private BrokenReferencesSupport() {}

    /**
     * Checks whether the project has some broken references or not.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param referenceHelper ReferenceHelper associated with the project.
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platformProperties array of property names which values hold
     *    name of the platform(s) used by the project. These platforms will be
     *    checked for existence. For example for J2SE project the property
     *    name is one and it is "platform.active". The name of the default
     *    platform is expected to be "default_platform" and this platform
     *    always exists.
     * @return true if some problem was found and it is necessary to give
     *    user a chance to fix them
     */
    public static boolean isBroken(AntProjectHelper projectHelper, 
            ReferenceHelper referenceHelper, String[] properties, String[] platformProperties) {
        return BrokenReferencesModel.isBroken(projectHelper, 
            projectHelper.getStandardPropertyEvaluator(), properties, platformProperties);
    }
    
    /**
     * Shows UI customizer which gives users chance to fix encountered problems.
     * @param projectHelper AntProjectHelper associated with the project.
     * @param referenceHelper ReferenceHelper associated with the project.
     * @param properties array of property names which values hold
     *    references which may be broken. For example for J2SE project
     *    the property names will be: "javac.classpath", "run.classpath", etc.
     * @param platformProperties array of property names which values hold
     *    name of the platform(s) used by the project. These platforms will be
     *    checked for existence. For example for J2SE project the property
     *    name is one and it is "platform.active". The name of the default
     *    platform is expected to be "default_platform" and this platform
     *    always exists.
     */
    public static void showCustomizer(AntProjectHelper projectHelper, 
            ReferenceHelper referenceHelper, String[] properties, String[] platformProperties) {
        BrokenReferencesModel model = new BrokenReferencesModel(projectHelper, referenceHelper, properties, platformProperties);
        BrokenReferencesCustomizer customizer = new BrokenReferencesCustomizer(model);
        JButton close = new JButton (NbBundle.getMessage(BrokenReferencesCustomizer.class,"LBL_BrokenLinksCustomizer_Close")); // NOI18N
        close.getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(BrokenReferencesCustomizer.class,"ACSD_BrokenLinksCustomizer_Close")); // NOI18N
        String projectDisplayName = "???"; // NOI18N
        try {
            Project project = ProjectManager.getDefault().findProject(projectHelper.getProjectDirectory());
            if (project != null) {
                projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
            }
        } catch (IOException e) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }
        DialogDescriptor dd = new DialogDescriptor(customizer, 
            NbBundle.getMessage(BrokenReferencesCustomizer.class, 
            "LBL_BrokenLinksCustomizer_Title", projectDisplayName), // NOI18N
            true, new Object[] {close}, close, DialogDescriptor.DEFAULT_ALIGN, null, null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(dd);
            dlg.setVisible(true);
        } finally {
            if (dlg != null)
                dlg.dispose();
        }
    }

    /**
     * Show alert message box informing user that a project has broken
     * references. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box.
     */
    public static synchronized void showAlert() {
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown || 
            brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis() ||
            !JavaSettings.getDefault().isShowAgainBrokenRefAlert()) {
                return;
        }
        brokenAlertShown = true;
        final Runnable task = new Runnable() {
            public void run() {
                try {
                    JButton closeOption = new JButton (NbBundle.getMessage(BrokenReferencesAlertPanel.class, "CTL_Broken_References_Close"));
                    closeOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BrokenReferencesAlertPanel.class, "AD_Broken_References_Close"));
                    DialogDescriptor dd = new DialogDescriptor(new BrokenReferencesAlertPanel(), 
                        NbBundle.getMessage(BrokenReferencesAlertPanel.class, "MSG_Broken_References_Title"),
                        true,
                        new Object[] {closeOption},
                        closeOption,
                        DialogDescriptor.DEFAULT_ALIGN,
                        null,
                        null); // NOI18N
                    dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                    Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                    dlg.setVisible(true);
                } finally {
                    synchronized (BrokenReferencesSupport.class) {
                        brokenAlertLastTime = System.currentTimeMillis();
                        brokenAlertShown = false;
                    }
                }
            }
        };
        SwingUtilities.invokeLater(new Runnable() {
            public void run () {
                Frame f = WindowManager.getDefault().getMainWindow();
                if (f == null || f.isShowing()) {
                    task.run();
                }
                else {
                    new MainWindowListener (f,task);
                }
            }
        });
    }
    
    
    private static class MainWindowListener extends WindowAdapter {
        
        private Frame frame;
        private Runnable task;
        
        /**
         * Has to be called by the event thread!
         *
         */
        public MainWindowListener (Frame frame, Runnable task) {
            assert frame != null && task != null;
            assert SwingUtilities.isEventDispatchThread();
            this.frame = frame;
            this.task = task;
            frame.addWindowListener(this);
        }
        
        public /*@Override*/ void windowOpened(java.awt.event.WindowEvent e) {
            MainWindowListener.this.frame.removeWindowListener (this);
            SwingUtilities.invokeLater(this.task);
        }
    }
    
    
}
