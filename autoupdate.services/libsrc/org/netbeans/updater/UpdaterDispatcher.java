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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.updater;

import java.io.File;
import javax.swing.SwingUtilities;

/**
 * @author  Jiri Rechtacek
 */
public final class UpdaterDispatcher implements Runnable {
    
    UpdaterDispatcher () {}
    
    private Boolean disable = null;
    private Boolean install = null;
    private Boolean uninstall = null;
    
    public static final String UPDATE_DIR = "update"; // NOI18N
    public static final String DEACTIVATE_DIR = "deactivate"; // NOI18N
    
    public static final String DEACTIVATE_LATER = "deactivate_later.txt"; // NOI18N
    
    /** Explore <cluster>/update directory and schedules actions handler for
     * Install/Update, Uninstall or Disable modules
     * 
     */
    private void dispatch () {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ";
        try {
            // uninstall first
            if (isUninstallScheduled ()) {
                ModuleDeactivator.delete ();
            }

            // then disable
            if (isDisableScheduled ()) {
                ModuleDeactivator.disable ();
            }

            // finally install/update
            if (isInstallScheduled ()) {
                try {
                    ModuleUpdater mu = new ModuleUpdater (null);
                    mu.start ();
                    mu.join ();
                } catch (InterruptedException ex) {
                    System.out.println("Error: " + ex);
                }
            }
        } catch (Exception x) {
            System.out.println ("Error: Handling delete throws " + x);
        } finally {
            UpdaterFrame.getUpdaterFrame ().unpackingFinished ();
        }
    }
    
    private boolean isDisableScheduled () {
        if (disable == null) {
            exploreUpdateDir ();
        }
        return disable;
    }
    
    private boolean isUninstallScheduled () {
        if (uninstall == null) {
            exploreUpdateDir ();
        }
        return uninstall;
    }
    
    private boolean isInstallScheduled () {
        if (install == null) {
            exploreUpdateDir ();
        }
        return install;
    }
    
    private void exploreUpdateDir () {
        // initialize to false
        install = false;
        uninstall = false;
        disable = false;
        
        // go over all clusters
        for (File cluster : UpdateTracking.clusters (true)) {
            File updateDir = new File (cluster, UPDATE_DIR);
            if (updateDir.exists () && updateDir.isDirectory ()) {
                // install/update
                if (install == null || ! install) {
                    install = ! ModuleUpdater.getModulesToInstall (cluster).isEmpty ();
                }
                // uninstall
                if (uninstall == null || ! uninstall) {
                    uninstall = ModuleDeactivator.hasModulesForDelete (updateDir);
                }
                // disable
                if (disable == null || ! disable) {
                    disable = ModuleDeactivator.hasModulesForDisable (updateDir);
                }
            }
        }
    }

    public void run () {
        dispatch ();
        UpdaterFrame.disposeSplash ();
    }
    
}
