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
package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.awt.Dialog;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import javax.swing.JButton;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ui.ServersCustomizer;

/**
 * ServerManager class provides access to the Server Manager dialog.
 *
 * @author sherold
 * @since  1.7
 */
public final class ServerManager {

    /** Do not allow to create instances of this class */
    private ServerManager() {
    }
    
    /**
     * Display the modal Server Manager dialog with the specified server instance 
     * preselected. This method should be called form the AWT event dispatch 
     * thread.
     *
     * @param serverInstanceID server instance which should be preselected, if 
     *        null the first server instance will be preselected.
     */
    public static void showCustomizer(String serverInstanceID) {
        ServerInstance instance =  ServerRegistry.getInstance().getServerInstance(serverInstanceID);
        ServersCustomizer customizer = new ServersCustomizer(instance);
        JButton close = new JButton(NbBundle.getMessage(ServerManager.class,"CTL_Close"));
        close.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ServerManager.class,"AD_Close"));
        DialogDescriptor descriptor = new DialogDescriptor (
                customizer,
                NbBundle.getMessage(ServerManager.class, "TXT_ServerManager"),
                true, 
                new Object[] {close},
                close,
                DialogDescriptor.DEFAULT_ALIGN, 
                new HelpCtx(ServerManager.class),
                null);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
    }
}
