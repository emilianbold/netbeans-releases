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

package org.apache.tools.ant.module.run;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;

/**
 * Action which stops the currently running Ant process.
 * If there is more than one, a dialog appears asking you to select one.
 * @author Jesse Glick
 * @see "issue #43143"
 */
public final class StopBuildingAction extends CallableSystemAction {
    
    /**
     * Map from active processing threads to their process display names.
     */
    private static final Map/*<Thread,String>*/ activeProcesses = new WeakHashMap();
    
    static void registerProcess(Thread t, String displayName) {
        synchronized (activeProcesses) {
            assert !activeProcesses.containsKey(t);
            activeProcesses.put(t, displayName);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SystemAction.get(StopBuildingAction.class).setEnabled(true);
            }
        });
    }
    
    static void unregisterProcess(Thread t) {
        final boolean enable;
        synchronized (activeProcesses) {
            assert activeProcesses.containsKey(t);
            activeProcesses.remove(t);
            enable = !activeProcesses.isEmpty();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SystemAction.get(StopBuildingAction.class).setEnabled(enable);
            }
        });
    }
    
    public void performAction() {
        Thread[] toStop = null;
        synchronized (activeProcesses) {
            assert !activeProcesses.isEmpty();
            if (activeProcesses.size() == 1) {
                toStop = (Thread[]) activeProcesses.keySet().toArray(new Thread[1]);
            }
        }
        if (toStop == null) {
            // More than one, need to select one.
            Map/*<Thread,String>*/ activeProcessesClone;
            synchronized (activeProcesses) {
                activeProcessesClone = new HashMap(activeProcesses);
            }
            toStop = StopBuildingAlert.selectProcessToKill(activeProcessesClone);
            synchronized (activeProcesses) {
                for (int i = 0; i < toStop.length; i++) {
                    if (!activeProcesses.containsKey(toStop[i])) {
                        // Oops, process ended while it was being selected... just ignore.
                        toStop[i] = null;
                    }
                }
            }
        }
        for (int i = 0; i < toStop.length; i++) {
            if (toStop[i] != null) {
                TargetExecutor.stopProcess(toStop[i]);
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building");
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    protected void initialize() {
        super.initialize();
        setEnabled(false); // no processes initially
    }

    protected boolean asynchronous() {
        return false;
    }
    
}
