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

package org.netbeans.modules.j2ee.common.ui;

import java.awt.Dialog;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.common.ui.customizer.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;



/**
 * Support for managing broken/missing servers.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * servers. Once BrokenReferencesSupport will support it, this class should be 
 * removed.
 */
public class BrokenServerSupport {

    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;

    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private BrokenServerSupport() {}

    /**
     * Checks whether the project has a broken/missing server problem.
     *
     * @param serverInstanceID server instance of which should be checked.
     *
     * @return true server instance of the specified id doesn't exist
     */
    public static boolean isBroken(String serverInstanceID) {
        return Deployment.getDefault().getServerID(serverInstanceID) == null;
    }
    
    /**
     * Shows UI customizer which gives users chance to fix encountered problems.
     */
    public static void showCustomizer(ArchiveProjectProperties app) {
        String j2eeSpec = (String)app.get(ArchiveProjectProperties.J2EE_PLATFORM);
        String instance = NoSelectedServerWarning.selectServerDialog(
                new Object[] {J2eeModule.EAR}, j2eeSpec,
                NbBundle.getMessage(BrokenServerSupport.class, "LBL_Resolve_Missing_Server_Title")); //  NOI18N
        if (instance != null) {
            app.put (ArchiveProjectProperties.J2EE_SERVER_INSTANCE, instance);
            app.store ();
        }
    }

    /**
     * Show alert message box informing user that a project has missing
     * server. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box.
     */
    public static synchronized void showAlert() {
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown
            || brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis() 
            || !FoldersListSettings.getDefault().isShowAgainBrokenServerAlert()) {
                return;
        }
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        BrokenServerAlertPanel alert = new BrokenServerAlertPanel();
                        DialogDescriptor dd = new DialogDescriptor(alert, NbBundle.getMessage(BrokenServerAlertPanel.class,"MSG_Broken_Server_Title")); // NOI18N
                        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                        dlg.setVisible(true);
                    } finally {
                        synchronized (BrokenServerSupport.class) {
                            brokenAlertLastTime = System.currentTimeMillis();
                            brokenAlertShown = false;
                        }
                    }
                }
            });
    }
}
