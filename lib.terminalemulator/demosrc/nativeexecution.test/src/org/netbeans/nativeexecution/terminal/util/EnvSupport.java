/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.nativeexecution.terminal.util;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.nativeexecution.terminal.ui.ProgressDialog;

/**
 *
 * @author ak119685
 */
public final class EnvSupport {

    public static void ensureConnected(final ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog("Prepare " + env.getDisplayName(), new Runnable() { // NOI18N

            public void run() {
                try {
                    if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                        ConnectionManager.getInstance().connectTo(env);
                    }

                    HostInfoUtils.getHostInfo(env);
                } catch (Exception ex) {
                }
            }
        }, null);

        progressDialog.setVisible(true);
    }

    private EnvSupport() {
    }
}
