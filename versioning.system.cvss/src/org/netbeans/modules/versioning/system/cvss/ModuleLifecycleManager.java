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

package org.netbeans.modules.versioning.system.cvss;

import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.netbeans.core.NbTopManager;
import org.netbeans.ModuleManager;
import org.netbeans.Module;
import org.netbeans.InvalidException;

import javax.swing.*;
import java.util.*;

/**
 * Handles module events distributed by NetBeans module
 * framework.
 *
 * <p>It's registered and instantiated from module manifest.
 *
 * @author Petr Kuzel
 * @author Maros Sandor
 */
public final class ModuleLifecycleManager extends ModuleInstall {

    public void validate() throws IllegalStateException {
        final Boolean [] oldEnabled = new Boolean[] { Boolean.FALSE };
        final ModuleManager mgr = org.netbeans.core.startup.Main.getModuleSystem().getManager();
        mgr.mutex().readAccess(new Runnable() {
            public void run() {
                Module m;
                m = mgr.get("org.netbeans.modules.vcs.profiles.cvsprofiles");
                if (m != null && m.isEnabled()) oldEnabled[0] = Boolean.TRUE;
                m = mgr.get("org.netbeans.modules.vcs.advanced");
                if (m != null && m.isEnabled()) oldEnabled[0] = Boolean.TRUE;
                m = mgr.get("org.netbeans.modules.vcs.profiles.vss");
                if (m != null && m.isEnabled()) oldEnabled[0] = Boolean.TRUE;
                m = mgr.get("org.netbeans.modules.vcs.profiles.pvcs");
                if (m != null && m.isEnabled()) oldEnabled[0] = Boolean.TRUE;
            }
        });
        if (!oldEnabled[0].booleanValue()) return;
        if (JOptionPane.showConfirmDialog(null, NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning"), 
                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Install_Warning_Title"), 
                                          JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION)
            throw new IllegalStateException("Installation aborted by user");
        // TODO: We cannot disable old modules here, because it somehow does not work ... we postpone this work to CvsVersioningSystem.init()
    }

    public boolean closing() {
        return CvsVersioningSystem.getInstance().closing();
    }

    /**
     * Enables old VCS modules. 
     */ 
    private void enableCurrentModules() {
        final ModuleManager mgr = NbTopManager.get().getModuleSystem().getManager();
        mgr.mutex().writeAccess(new Runnable() {
            public void run() {
                Module m;
                Set modules = new HashSet();
                m = mgr.get("org.netbeans.modules.vcs.profiles.cvsprofiles");
                if (m != null && !m.isEnabled()) modules.add(m);
                m = mgr.get("org.netbeans.modules.vcs.advanced");
                if (m != null && !m.isEnabled()) modules.add(m);
                m = mgr.get("org.netbeans.modules.vcs.profiles.vss");
                if (m != null && !m.isEnabled()) modules.add(m);
                m = mgr.get("org.netbeans.modules.vcs.profiles.pvcs");
                if (m != null && !m.isEnabled()) modules.add(m);
                if (modules.size() > 0)
                    try {
                        mgr.enable(modules);
                    } catch (InvalidException e) {
                        ErrorManager.getDefault().notify(e);
                    }
            }
        });
    }
    
    public void uninstalled() {
        if (JOptionPane.showConfirmDialog(null, NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Uninstall_Warning"), 
                                          NbBundle.getBundle(ModuleLifecycleManager.class).getString("MSG_Uninstall_Warning_Title"), 
                                          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;
        enableCurrentModules();
    }
}
