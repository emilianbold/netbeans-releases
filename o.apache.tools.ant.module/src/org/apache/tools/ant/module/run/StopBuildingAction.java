/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.run;

import java.awt.EventQueue;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
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
    private static final Map<Thread,String> activeProcesses = new WeakHashMap<Thread,String>();
    
    static void registerProcess(Thread t, String displayName) {
        synchronized (activeProcesses) {
            assert !activeProcesses.containsKey(t);
            activeProcesses.put(t, displayName);
        }
        EventQueue.invokeLater(new Runnable() {
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
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                SystemAction.get(StopBuildingAction.class).setEnabled(enable);
            }
        });
    }
    
    @Override
    public void performAction() {
        Thread[] toStop = null;
        synchronized (activeProcesses) {
            assert !activeProcesses.isEmpty();
            if (activeProcesses.size() == 1) {
                toStop = activeProcesses.keySet().toArray(new Thread[1]);
            }
        }
        if (toStop == null) {
            // More than one, need to select one.
            Map<Thread,String> activeProcessesClone;
            synchronized (activeProcesses) {
                activeProcessesClone = new HashMap<Thread,String>(activeProcesses);
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
        for (Thread t : toStop) {
            if (t != null) {
                TargetExecutor.stopProcess(t);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected void initialize() {
        super.initialize();
        setEnabled(false); // no processes initially
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        class SpecialMenuItem extends JMenuItem implements DynamicMenuContent {
            public SpecialMenuItem() {
                super(StopBuildingAction.this);
            }
            public JComponent[] getMenuPresenters() {
                String label;
                synchronized (activeProcesses) {
                    switch (activeProcesses.size()) {
                        case 0:
                            label = NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building");
                            break;
                        case 1:
                            label = NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building_one",
                                    activeProcesses.values().iterator().next());
                            break;
                        default:
                            label = NbBundle.getMessage(StopBuildingAction.class, "LBL_stop_building_many");
                    }
                }
                Mnemonics.setLocalizedText(this, label);
                return new JComponent[] {this};
            }
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                return getMenuPresenters();
            }
        }
        return new SpecialMenuItem();
    }
    
}
