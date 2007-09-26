/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
