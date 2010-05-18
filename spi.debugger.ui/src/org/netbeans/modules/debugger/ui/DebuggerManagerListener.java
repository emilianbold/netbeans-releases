/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import java.awt.Dimension;
import java.awt.event.ActionListener;
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
import java.util.WeakHashMap;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.SwingUtilities;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.debugger.ui.actions.DebuggerAction;
import org.netbeans.spi.debugger.ActionsProvider;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.TopComponentGroup;
import org.openide.windows.WindowManager;


/**
 * This listener notifies about changes in the
 * {@link DebuggerManager}.
 *
 * @author Jan Jancura, Martin Entlicher
 */
public class DebuggerManagerListener extends DebuggerManagerAdapter {

    private static final String PROPERTY_CLOSED_TC = "closedTopComponents"; // NOI18N

    private List<DebuggerEngine> openedGroups = new LinkedList<DebuggerEngine>();
    private final Map<DebuggerEngine, List<? extends Component>> openedComponents = new HashMap<DebuggerEngine, List<? extends Component>>();
    private Set<Reference<Component>> componentsInitiallyOpened = new HashSet<Reference<Component>>();
    private final Map<DebuggerEngine, List<? extends Component>> closedToolbarButtons = new HashMap<DebuggerEngine, List<? extends Component>>();
    private final Map<DebuggerEngine, List<? extends Component>> usedToolbarButtons = new HashMap<DebuggerEngine, List<? extends Component>>();
    private final Map<Component, Dimension> toolbarButtonsPrefferedSize = new HashMap<Component, Dimension>();
    private final Map<Mode, Reference<TopComponent>> lastSelectedTopComponents = new WeakHashMap<Mode, Reference<TopComponent>>();

    private static final List<Component> OPENED_COMPONENTS = new LinkedList<Component>();

    @Override
    public void engineAdded (DebuggerEngine engine) {
        openEngineComponents(engine);
        setupToolbar(engine);
    }

    private void openEngineComponents (final DebuggerEngine engine) {
        synchronized (openedComponents) {
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
                RequestProcessor rp = engine.lookupFirst(null, RequestProcessor.class);
                if (rp == null) {
                    rp = RequestProcessor.getDefault();
                }
                rp.post (new Runnable () {
                    public void run () {
                        List<Component> cs = new ArrayList<Component>(componentProxies.size());
                        try {
                            final List<TopComponent> topComponentsToOpen = new ArrayList<TopComponent>(componentProxies.size());
                            for (final BeanContextChildComponentProxy cp : componentProxies) {
                                final Component[] c = new Component[] { null };
                                final boolean[] doOpen = new boolean[] { false };
                                try {
                                    SwingUtilities.invokeAndWait(new Runnable() {
                                        public void run() {
                                            c[0] = cp.getComponent();
                                            doOpen[0] = (cp instanceof DesignMode) ? ((DesignMode) cp).isDesignTime() : true;
                                        }
                                    });
                                    if (c[0] == null) {
                                        //throw new NullPointerException("No component from "+cp);
                                        continue;
                                    }
                                } catch (Exception ex) {
                                    Exceptions.printStackTrace(ex);
                                    continue;
                                }
                                cs.add(c[0]);
                                if (c[0] instanceof TopComponent) {
                                    final TopComponent tc = (TopComponent) c[0];
                                    boolean wasClosed = Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                                            getProperties(PROPERTY_CLOSED_TC).getBoolean(tc.getName(), false);
                                    boolean wasOpened = !Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                                            getProperties(PROPERTY_CLOSED_TC).getBoolean(tc.getName(), true);
                                    if (doOpen[0] && !wasClosed || !doOpen[0] && wasOpened) {
                                        topComponentsToOpen.add(tc);
                                    }
                                } else {
                                    if (doOpen[0]) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                c[0].setVisible(true);
                                            }
                                        });
                                    }
                                }
                            }
                            if (topComponentsToOpen.size() > 0) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        openTopComponents(topComponentsToOpen);
                                    }
                                });
                            }
                        } finally {
                            synchronized (openedComponents) {
                                componentsToOpen.clear();
                                componentsToOpen.addAll(cs);
                                openedComponents.notifyAll();
                            }
                            synchronized (OPENED_COMPONENTS) {
                                if (componentsInitiallyOpened.size() == 0) {
                                    OPENED_COMPONENTS.addAll(cs);
                                } else {
                                    List<Component> ocs = new ArrayList<Component>(cs);
                                    for (Reference<Component> cref : componentsInitiallyOpened) {
                                        ocs.remove(cref.get());
                                    }
                                    OPENED_COMPONENTS.addAll(ocs);
                                }
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
                        }
                    });
                }
                openedGroups.add(engine);
            }
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

    private void openTopComponents(List<TopComponent> components) {
        assert SwingUtilities.isEventDispatchThread();
        Set<Mode> modesWithVisibleTC = new HashSet<Mode>();
        for (TopComponent tc : components) {
            tc.open();
            Mode mode = WindowManager.getDefault().findMode(tc);
            if (modesWithVisibleTC.add(mode)) {
                TopComponent tcSel = mode.getSelectedTopComponent();
                if (tcSel != null && tcSel != tc) {
                    WeakReference<TopComponent> lastSelectedTCRef = new WeakReference(tcSel);
                    lastSelectedTopComponents.put(mode, lastSelectedTCRef);
                }
                String side = null;
                try {
                    side = (String) mode.getClass().getMethod("getSide").invoke(mode);
                } catch (Exception ex) {}
                if (side == null) {
                    tc.requestVisible();
                }
            }
        }
    }

    private void closeTopComponentsList(List<TopComponent> components) {
        assert SwingUtilities.isEventDispatchThread();
        List<TopComponent> componentToActivateAfterClose = new ArrayList<TopComponent>();
        for (TopComponent tc : components) {
            Mode mode = WindowManager.getDefault().findMode(tc);
            if (mode.getSelectedTopComponent() == tc) {
                Reference<TopComponent> tcActRef = lastSelectedTopComponents.remove(mode);
                if (tcActRef != null) {
                    TopComponent tcAct = tcActRef.get();
                    if (tcAct != null && tcAct.isOpened()) {
                        componentToActivateAfterClose.add(tcAct);
                    }
                }
            }
            tc.close();
        }
        for (TopComponent tc : componentToActivateAfterClose) {
            tc.requestVisible();
        }
    }

    private final void setupToolbar(final DebuggerEngine engine) {
        List<? extends ActionsProvider> actionsProviderList = engine.lookup(null, ActionsProvider.class);
        final Set engineActions = new HashSet();
        for (ActionsProvider ap : actionsProviderList) {
            engineActions.addAll(ap.getActions());
        }
        final List<Component> buttonsToClose = new ArrayList<Component>();
        buttonsToClose.add(new java.awt.Label("EMPTY"));
        final boolean isFirst;
        synchronized (closedToolbarButtons) {
            isFirst = closedToolbarButtons.isEmpty();
            closedToolbarButtons.put(engine, buttonsToClose);
        }
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                List<Component> buttonsClosed = new ArrayList<Component>();
                List<Component> buttonsUsed = new ArrayList<Component>();
                try {
                    if (ToolbarPool.getDefault ().getConfiguration ().equals(ToolbarPool.DEFAULT_CONFIGURATION)) {
                        ToolbarPool.getDefault ().setConfiguration("Debugging"); // NOI18N
                    }
                    Toolbar debugToolbar = ToolbarPool.getDefault ().findToolbar("Debug");
                    if (debugToolbar == null) return ;
                    for (Component c : debugToolbar.getComponents()) {
                        if (c instanceof AbstractButton) {
                            Action a = ((AbstractButton) c).getAction();
                            if (a == null) {
                                ActionListener[] actionListeners = ((AbstractButton) c).getActionListeners();
                                for (ActionListener l : actionListeners) {
                                    if (l instanceof Action) {
                                        a = (Action) l;
                                        break;
                                    }
                                };
                            }
                            if (a != null && a instanceof DebuggerAction) {
                                Object action = ((DebuggerAction) a).getAction();
                                //System.err.println("Engine "+engine+" contains action "+a+"("+action+") = "+engineActions.contains(action));
                                boolean containsAction = engineActions.contains(action);
                                if (isFirst && !containsAction) {
                                    // For the first engine disable toolbar buttons for actions that are not provided
                                    c.setVisible(false);
                                    buttonsClosed.add(c);
                                    toolbarButtonsPrefferedSize.put(c, c.getPreferredSize());
                                    c.setPreferredSize(new Dimension(0, 0));
                                }
                                if (!isFirst && containsAction) {
                                    // For next engine enable toolbar buttons that could be previously disabled
                                    // and are used for actions that are provided.
                                    Dimension d = toolbarButtonsPrefferedSize.remove(c);
                                    if (d != null) {
                                        c.setPreferredSize(d);
                                    }
                                    c.setVisible(true);
                                }
                                if (containsAction) {
                                    // Keep track of buttons used by individual engines.
                                    buttonsUsed.add(c);
                                }
                            }
                        }
                    }
                    debugToolbar.revalidate();
                    debugToolbar.repaint();
                } finally {
                    synchronized (closedToolbarButtons) {
                        usedToolbarButtons.put(engine, buttonsUsed);
                        buttonsToClose.clear();
                        buttonsToClose.addAll(buttonsClosed);
                        closedToolbarButtons.notifyAll();
                    }
                }
            }
        });
    }

    @Override
    public void engineRemoved (DebuggerEngine engine) {
        //boolean doCloseToolbar = false;
        synchronized (openedComponents) {
            List<? extends Component> openedWindows = openedComponents.remove(engine);
            if (openedWindows != null) {
                // If it's not filled yet by AWT, wait...
                if (openedWindows.size() == 1 && openedWindows.get(0) instanceof java.awt.Label) {
                    try {
                       openedComponents.wait();
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
                            final List<TopComponent> topComponentsToClose = new ArrayList<TopComponent>(windowsToClose.size());
                            for (Component c : windowsToClose) {
                                if (c instanceof TopComponent) {
                                    TopComponent tc = (TopComponent) c;
                                    boolean isOpened = tc.isOpened();
                                    Properties.getDefault().getProperties(DebuggerManagerListener.class.getName()).
                                            getProperties(PROPERTY_CLOSED_TC).setBoolean(tc.getName(), !isOpened);
                                    if (isOpened) {
                                        topComponentsToClose.add(tc);
                                    }
                                } else {
                                    c.setVisible(false);
                                }
                            }
                            closeTopComponentsList(topComponentsToClose);
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
                /*doCloseToolbar = true;
                SwingUtilities.invokeLater (new Runnable () {
                    public void run () {
                        closeToolbar();
                    }
                });*/
            }
        }
        //closeToolbar(engine, doCloseToolbar);
        closeToolbar(engine);
    }

    private final void closeToolbar(DebuggerEngine engine) {
        final boolean doCloseToolbar;
        synchronized (closedToolbarButtons) {
            List<? extends Component> closedButtons = closedToolbarButtons.remove(engine);
            doCloseToolbar = closedToolbarButtons.isEmpty();
            if (closedButtons != null) {
                // If it's not filled yet by AWT, wait...
                while (closedButtons.size() == 1 && closedButtons.get(0) instanceof java.awt.Label) {
                    try {
                       closedToolbarButtons.wait();
                    } catch (InterruptedException iex) {}
                }
                List<? extends Component> usedButtons = usedToolbarButtons.remove(engine);
                ToolbarPool.getDefault().waitFinished();
                if (!ToolbarPool.getDefault ().getConfiguration ().equals("Debugging")) {
                    return ;
                }
                final Toolbar debugToolbar = ToolbarPool.getDefault ().findToolbar("Debug");
                if (!doCloseToolbar) {
                    // An engine is removed, but there remain others =>
                    // actions that remained enabled because of this are disabled unless needed by other engines.
                    // Check whether the toolbar buttons are used by some other engine...
                    final List<Component> usedByAllButtons = new ArrayList<Component>();
                    for (List<? extends Component> ltc : usedToolbarButtons.values()) {
                        usedByAllButtons.addAll(ltc);
                    }
                    final List<Component> buttonsToClose = new ArrayList<Component>(usedButtons);
                    buttonsToClose.removeAll(usedByAllButtons);
                    if (!buttonsToClose.isEmpty()) {
                        SwingUtilities.invokeLater (new Runnable () {
                            public void run () {
                                for (Component c : buttonsToClose) {
                                    c.setVisible(false);
                                    toolbarButtonsPrefferedSize.put(c, c.getPreferredSize());
                                    c.setPreferredSize(new Dimension(0, 0));
                                }
                                debugToolbar.revalidate();
                                debugToolbar.repaint();
                            }
                        });
                    }
                } else {
                    SwingUtilities.invokeLater (new Runnable () {
                        public void run () {
                            for (Component c : debugToolbar.getComponents()) {
                                if (c instanceof AbstractButton) {
                                    Dimension d = toolbarButtonsPrefferedSize.remove(c);
                                    if (d != null) {
                                        c.setPreferredSize(d);
                                    }
                                    c.setVisible(true);
                                }
                            }
                            debugToolbar.revalidate();
                            debugToolbar.repaint();
                        }
                    });
                }
            }
        }
        if (doCloseToolbar) {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    ToolbarPool.getDefault ().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION); // NOI18N
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
        ToolbarPool.getDefault().waitFinished();
        if (ToolbarPool.getDefault().getConfiguration().equals("Debugging")) { // NOI18N
            ToolbarPool.getDefault().setConfiguration(ToolbarPool.DEFAULT_CONFIGURATION);
        }
    }

}
