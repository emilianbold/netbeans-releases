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

package org.netbeans.modules.j2ee.common.ui;

import java.awt.Dialog;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.j2ee.common.*;
import org.netbeans.modules.j2ee.common.ui.NoSelectedServerWarning;
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
     * Shows UI customizer which gives users chance to fix encountered problems,
     * i.e. choose appropriate application server.
     *
     * @param j2eeSpec one of {@link J2eeModule#EJB}, {@link J2eeModule#EAR}
     * @param moduleType
     * @return selected application server. Might be <code>null</code>.
     */
    public static String selectServer(final String j2eeSpec, final Object moduleType) {
        return NoSelectedServerWarning.selectServerDialog(
                new Object[] { moduleType }, j2eeSpec,
                NbBundle.getMessage(BrokenServerSupport.class, "LBL_Resolve_Missing_Server_Title"),
                NbBundle.getMessage(BrokenServerSupport.class, "ACSD_Resolve_Missing_Server")); //  NOI18N
    }

    /**
     * Show alert message box informing user that a project has missing
     * server. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box.
     */
    public static synchronized void showAlert() {
        if (Boolean.getBoolean("j2eeserver.no.server.instance.check")) {
            return;
        }
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown
            || brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis() 
            || !J2EEUISettings.getDefault().isShowAgainBrokenServerAlert()) {
                return;
        }
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        BrokenServerAlertPanel alert = new BrokenServerAlertPanel();
                        JButton close = new JButton(
                                NbBundle.getMessage(BrokenServerSupport.class, "LBL_BrokenServerCustomizer_Close"));
                        close.getAccessibleContext().setAccessibleDescription(
                                NbBundle.getMessage(BrokenServerSupport.class, "ACSD_BrokenServerCustomizer_Close"));
                        DialogDescriptor dd = new DialogDescriptor(
                                alert, 
                                NbBundle.getMessage(BrokenServerAlertPanel.class, "MSG_Broken_Server_Title"),
                                true, 
                                new Object[] {close}, 
                                close, 
                                DialogDescriptor.DEFAULT_ALIGN, 
                                null, 
                                null);
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
