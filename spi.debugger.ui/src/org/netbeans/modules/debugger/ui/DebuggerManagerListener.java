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
package org.netbeans.modules.debugger.ui;

import java.awt.Component;
import java.beans.DesignMode;
import java.beans.beancontext.BeanContextChildComponentProxy;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.api.debugger.Properties;
import org.openide.awt.ToolbarPool;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;


/**
 * This listener notifies about changes in the
 * {@link DebuggerManager}.
 *
 * @author Jan Jancura
 */
public class DebuggerManagerListener extends DebuggerManagerAdapter {

    private static final String PROPERTY_CLOSED_TC = "closedTopComponents"; // NOI18N

    private List<DebuggerEngine> openedGroups = new LinkedList<DebuggerEngine>();
    private Map<DebuggerEngine, List<? extends Component>> openedComponents = new HashMap<DebuggerEngine, List<? extends Component>>();
    private Set<Reference<Component>> componentsInitiallyOpened = new HashSet<Reference<Component>>();

    private static final List<Component> OPENED_COMPONENTS = new LinkedList<Component>();

    @Override
    public synchronized void engineAdded (DebuggerEngine engine) {
        if (openedComponents.containsKey(engine) || openedGroups.contains(engine)) {
            return;
        }
        final List<? extends BeanContextChildComponentProxy> componentProxies =
                engine.lookup(null, BeanContextChildComponentProxy.class);
        //final List<? extends TopComponent> windowsToOpen = engine.lookup(null, TopComponent.class);
        if (componentProxies != null && !componentProxies.isEmpty()) {
            final List<Component> componentsToOpen = new ArrayList<Component>(componentProxies.size());
            componentsToOpen.add(new java.awt.Label("EMPTY"));
            if (openedComponents.isEmpty() && openedGroups.isEmpty()) {
                fillOpenedDebuggerComponents(componentsInitiallyOpened);
            }
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    List<Component> cs = new ArrayList<Component>(componentProxies.size());
                    try {
                        for (BeanContextChildComponentProxy cp : componentProxies) {
                            Component c;
                            try {
                                c = cp.getComponent();
                                if (c == null) {
                                    //throw new NullPointerException("No component from "+cp);
                                    continue;
                                }
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                                continue;
                            }
                            cs.add(c);
                            boolean doOpen = (cp instanceof DesignMode) ? ((DesignMode) cp).isDesignTime() : true;
                            if (c instanceof TopComponent) {
                                TopComponent tc = (TopComponent) c;
                                boolean wasClosed = Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                                        getProperties(PROPERTY_CLOSED_TC).getBoolean(tc.getName(), false);
                                boolean wasOpened = !Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                                        getProperties(PROPERTY_CLOSED_TC).getBoolean(tc.getName(), true);
                                if (doOpen && !wasClosed || !doOpen && wasOpened) {
                                    tc.open();
                                }
                            } else {
                                if (doOpen) {
                                    c.setVisible(true);
                                }
                            }
                        }
                        setupToolbar();
                    } finally {
                        synchronized (DebuggerManagerListener.this) {
                            componentsToOpen.clear();
                            componentsToOpen.addAll(cs);
                            DebuggerManagerListener.this.notifyAll();
                        }
                        synchronized (OPENED_COMPONENTS) {
                            OPENED_COMPONENTS.addAll(cs);
                        }
                    }
                }
            });
            openedComponents.put(engine, componentsToOpen);
        } else {
            if (openedGroups.isEmpty()) {
                // Open debugger TopComponentGroup.
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        TopComponentGroup group = WindowManager.getDefault ().
                            findTopComponentGroup ("debugger"); // NOI18N
                        if (group != null) {
                            group.open ();
                        }
                        setupToolbar();
                    }
                });
            }
            openedGroups.add(engine);
        }
    }

    private void fillOpenedDebuggerComponents(Set<Reference<Component>> componentsInitiallyOpened) {
        // For simplicity, add all opened components. These will not be closed when finishing the debugging session.
        TopComponent.Registry registry = TopComponent.getRegistry();
        synchronized (registry) {
            for (TopComponent tc : registry.getOpened()) {
                componentsInitiallyOpened.add(new WeakReference<Component>(tc));
            }
        }
    }

    private static final void setupToolbar() {
        if (ToolbarPool.getDefault ().getConfiguration ().equals(ToolbarPool.DEFAULT_CONFIGURATION)) {
            ToolbarPool.getDefault ().setConfiguration("Debugging"); // NOI18N
        }
    }

    private static final void closeToolbar() {
        if (ToolbarPool.getDefault ().getConfiguration ().equals("Debugging")) {
            ToolbarPool.getDefault ().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION); // NOI18N
        }
    }

    @Override
    public synchronized void engineRemoved (DebuggerEngine engine) {
        List<? extends Component> openedWindows = openedComponents.remove(engine);
        if (openedWindows != null) {
            // If it's not filled yet by AWT, wait...
            if (openedWindows.size() == 1 && openedWindows.get(0) instanceof java.awt.Label) {
                try {
                    wait();
                } catch (InterruptedException iex) {}
            }
            // Check whether the component is opened by some other engine...
            final List<Component> retainOpened = new ArrayList<Component>();
            for (List<? extends Component> ltc : openedComponents.values()) {
                retainOpened.addAll(ltc);
            }
            final List<Component> windowsToClose = new ArrayList<Component>(openedWindows);
            windowsToClose.removeAll(retainOpened);
            for (Reference<Component> cref : componentsInitiallyOpened) {
                windowsToClose.remove(cref.get());
            }
            if (!windowsToClose.isEmpty()) {
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        for (Component c : windowsToClose) {
                            if (c instanceof TopComponent) {
                                TopComponent tc = (TopComponent) c;
                                boolean isOpened = tc.isOpened();
                                Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                                        getProperties(PROPERTY_CLOSED_TC).setBoolean(tc.getName(), !isOpened);
                                if (isOpened) {
                                    tc.close();
                                }
                            } else {
                                c.setVisible(false);
                            }
                        }
                    }
                });
            }
            synchronized (OPENED_COMPONENTS) {
                OPENED_COMPONENTS.removeAll(windowsToClose);
            }
        } else {
            openedGroups.remove(engine);
            if (openedGroups.isEmpty()) {
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        TopComponentGroup group = WindowManager.getDefault ().
                            findTopComponentGroup ("debugger"); // NOI18N
                        if (group != null) {
                            group.close();
                        }
                    }
                });
            }
        }
        if (openedComponents.isEmpty() && openedGroups.isEmpty()) {
            componentsInitiallyOpened.clear();
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    closeToolbar();
                }
            });
        }
    }

    static void closeDebuggerUI() {
        /*
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine("CLOSING TopComponentGroup...");
        StringWriter sw = new StringWriter();
        new Exception("Stack Trace").fillInStackTrace().printStackTrace(new java.io.PrintWriter(sw));
        java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine(sw.toString());
         */
        // Close debugger TopComponentGroup.
        if (SwingUtilities.isEventDispatchThread()) {
            doCloseDebuggerUI();
        } else {
            SwingUtilities.invokeLater(new Runnable () {
                public void run () {
                    doCloseDebuggerUI();
                }
            });
        }
        //java.util.logging.Logger.getLogger("org.netbeans.modules.debugger.jpda").fine("TopComponentGroup closed.");
    }

    private static void doCloseDebuggerUI() {
        TopComponentGroup group = WindowManager.getDefault ().
                findTopComponentGroup ("debugger"); // NOI18N
        if (group != null) {
            group.close ();
        }
        synchronized (OPENED_COMPONENTS) {
            for (Component c : OPENED_COMPONENTS) {
                if (c instanceof TopComponent) {
                    ((TopComponent) c).close();
                } else {
                    c.setVisible(false);
                }
            }
            OPENED_COMPONENTS.clear();
        }
        if (ToolbarPool.getDefault().getConfiguration().equals("Debugging")) { // NOI18N
            ToolbarPool.getDefault().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION);
        }
    }

}
