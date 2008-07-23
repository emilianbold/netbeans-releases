/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.DeadlockDetector.Deadlock;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.views.ViewModelListener;

import org.netbeans.spi.viewmodel.Models;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author  Dan
 */
public class DebuggingView extends TopComponent implements org.openide.util.HelpCtx.Provider,
       ExplorerManager.Provider, PropertyChangeListener, TreeExpansionListener, TreeModelListener,
       AdjustmentListener, ChangeListener {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "debugging"; //NOI18N
    public static final int BAR_WIDTH = 8;
    
    static final Color hitsColor = new Color(255, 255, 178);
    static final Color hitsBarColor = new Color(230, 230, 130);
    static final Color deadlockColor = UIManager.getDefaults().getColor("nb.errorForeground"); // new Color(252, 157, 159); 
    static final Color greenBarColor = new Color(189, 230, 170);
    private transient Color treeBackgroundColor = UIManager.getDefaults().getColor("Tree.background"); // NOI18N
    
    private transient ExplorerManager manager = new ExplorerManager();
    private transient ViewModelListener viewModelListener;
    private Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
    private PreferenceChangeListener prefListener;
    private SessionsComboBoxListener sessionsComboListener;

    private transient ImageIcon resumeIcon;
    private transient ImageIcon focusedResumeIcon;
    private transient ImageIcon pressedResumeIcon;
    private transient ImageIcon suspendIcon;
    private transient ImageIcon focusedSuspendIcon;
    private transient ImageIcon pressedSuspendIcon;
    
    private DebugTreeView treeView;
    private TapPanel tapPanel;
    private InfoPanel infoPanel;
    private JPDADebugger debugger;
    private Session session;
    private JPDADebugger previousDebugger;
    
    private JPanel leftPanel;
    private JPanel rightPanel;
    
    private final ThreadsListener threadsListener;
    private transient Reference<TopComponent> lastSelectedTCRef;
    private transient Reference<TopComponent> componentToActivateAfterClose;
    
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static Reference<DebuggingView> instance = null;
    
    /** Creates new form DebuggingView */
    public DebuggingView() {
        setIcon(Utilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/debugging.png")); // NOI18N
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
        
        initComponents();
    
        threadsListener = ThreadsListener.getDefault();
        threadsListener.setDebuggingView(this);
        
        resumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_16.png"));
        focusedResumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_focused_16.png"));
        pressedResumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_pressed_16.png"));
        suspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_16.png"));
        focusedSuspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_focused_16.png"));
        pressedSuspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_pressed_16.png"));
        
        setBackground(treeBackgroundColor);
        
        treeView = new DebugTreeView();
        treeView.setRootVisible(false);
        treeView.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        treeView.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        mainPanel.add(treeView, BorderLayout.CENTER);
        
        leftPanel = new JPanel();
        leftPanel.setBackground(treeBackgroundColor);
        leftPanel.setPreferredSize(new Dimension(BAR_WIDTH, 0));
        leftPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        rightPanel = new ZebraPanel(treeView);
        rightPanel.setBackground(treeBackgroundColor);
        rightPanel.setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, 0));
        rightPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        tapPanel = new TapPanel();
        tapPanel.setOrientation(TapPanel.DOWN);
        tapPanel.setExpanded(true);
        
        infoPanel = new InfoPanel(tapPanel);
        tapPanel.add(infoPanel);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(tapPanel, gridBagConstraints);
        
        manager.addPropertyChangeListener(this);
        treeView.addTreeExpansionListener(this);
        TreeModel model = treeView.getTree().getModel();
        model.addTreeModelListener(this);
        
        prefListener = new DebuggingPreferenceChangeListener();
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, preferences));
        sessionsComboListener = new SessionsComboBoxListener();
        
        scrollBarPanel.setVisible(false);
        treeScrollBar.addAdjustmentListener(this);
        treeView.getViewport().addChangeListener(this);

        setSuspendTableVisible(preferences.getBoolean(FiltersDescriptor.SHOW_SUSPEND_TABLE, true));
    }
 
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sessionComboBox = new javax.swing.JComboBox();
        mainScrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        scrollBarPanel = new javax.swing.JPanel();
        treeScrollBar = new javax.swing.JScrollBar();
        leftPanel1 = new javax.swing.JPanel();
        rightPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        sessionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Java Project" }));
        sessionComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(sessionComboBox, gridBagConstraints);

        mainScrollPane.setBorder(null);
        mainScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setPreferredSize(new java.awt.Dimension(32, 10));

        mainPanel.setLayout(new java.awt.BorderLayout());
        mainScrollPane.setViewportView(mainPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mainScrollPane, gridBagConstraints);

        scrollBarPanel.setMaximumSize(new java.awt.Dimension(2147483647, 17));
        scrollBarPanel.setLayout(new java.awt.BorderLayout());

        treeScrollBar.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollBarPanel.add(treeScrollBar, java.awt.BorderLayout.CENTER);

        leftPanel1.setPreferredSize(new java.awt.Dimension(8, 0));
        leftPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollBarPanel.add(leftPanel1, java.awt.BorderLayout.WEST);

        rightPanel1.setPreferredSize(new java.awt.Dimension(24, 0));
        rightPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        scrollBarPanel.add(rightPanel1, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(scrollBarPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel leftPanel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel rightPanel1;
    private javax.swing.JPanel scrollBarPanel;
    private javax.swing.JComboBox sessionComboBox;
    private javax.swing.JScrollBar treeScrollBar;
    // End of variables declaration//GEN-END:variables

    public void setRootContext(Models.CompoundModel model, DebuggerEngine engine) {
        {   // Destroy the old node
            Node root = manager.getRootContext();
            if (root != null) {
                try {
                    root.destroy();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (engine != null) {
            final JPDADebugger deb = engine.lookupFirst(null, JPDADebugger.class);
            synchronized (this) {
                if (previousDebugger != null) {
                    previousDebugger.removePropertyChangeListener(this);
                }
                previousDebugger = this.debugger;
                this.debugger = deb;
                if (deb != null) {
                    this.session = engine.lookupFirst(null, Session.class);
                    deb.addPropertyChangeListener(this);
                } else {
                    this.session = null;
                }
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    threadsListener.changeDebugger(deb);
                }
            });
        } else {
            synchronized (this) {
                if (previousDebugger != null) {
                    previousDebugger.removePropertyChangeListener(this);
                }
                previousDebugger = null;
                this.debugger = null;
                this.session = null;
            }
            threadsListener.changeDebugger(null);
        }
        Node root;
        if (model == null) {
            root = Node.EMPTY;
        } else {
            root = Models.createNodes(model, treeView);
            treeView.setExpansionModel(model);
        }
        manager.setRootContext(root);
        refreshView();
        adjustTreeScrollBar();
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public static DebuggingView getInstance() {
        DebuggingView view;
        view = (DebuggingView) WindowManager.getDefault().findTopComponent(ID);
        if (view == null) {
            view = getDefault();
        }
        return view;
    }

    public void setSuspendTableVisible(boolean visible) {
        rightPanel.setVisible(visible);
    }
    
    /**
     * Singleton accessor reserved for the window systemm only. The window
     * system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code. </em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized DebuggingView getDefault() {
        DebuggingView view;
        if (instance == null) {
            view = new DebuggingView();
            instance = new WeakReference<DebuggingView>(view);
        } else {
            view = instance.get();
            if (view == null) {
                view = new DebuggingView();
                instance = new WeakReference<DebuggingView>(view);
            }
        }
        return view;
    }
    
    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing ();
        if (viewModelListener != null) {
            viewModelListener.setUp();
            return;
        }
        if (viewModelListener != null) {
            throw new InternalError ();
        }
        viewModelListener = new ViewModelListener ("DebuggingView", this);
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden ();
        viewModelListener.destroy ();
    }

    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }

    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    @Override
    protected void componentOpened() {
        // Remember which component was active so that we can re-activate it
        // after Debugging is closed.
        super.componentOpened();
        Mode debuggingMode = WindowManager.getDefault().findMode(this);
        lastSelectedTCRef = new WeakReference(debuggingMode.getSelectedTopComponent());
        requestActive();
    }

    @Override
    public boolean canClose() {
        // Check whether we're active, if so, we'll re-activate the previously
        // active component.
        Mode debuggingMode = WindowManager.getDefault().findMode(this);
        if (debuggingMode.getSelectedTopComponent() == this) {
            componentToActivateAfterClose = lastSelectedTCRef;
        } else {
            componentToActivateAfterClose = null;
        }
        return super.canClose();
    }

    @Override
    protected void componentClosed() {
        // Re-activate the previously active component, if any.
        TopComponent lastSelectedTC = (componentToActivateAfterClose != null) ? componentToActivateAfterClose.get() : null;
        if (lastSelectedTC != null) {
            lastSelectedTC.requestActive();
        }
        super.componentClosed();
    }
    
    
    
//    public org.openide.util.HelpCtx getHelpCtx() {
//        return new org.openide.util.HelpCtx("NetbeansDebuggerSourcesNode"); // NOI18N
//    }
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow ();
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage (DebuggingView.class, "CTL_Debugging_view"); // NOI18N
    }
    
    @Override
    public String getToolTipText() {
        return NbBundle.getMessage (DebuggingView.class, "CTL_Debugging_tooltip"); // NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (ExplorerManager.PROP_ROOT_CONTEXT.equals(propertyName) || 
                ExplorerManager.PROP_NODE_CHANGE.equals(propertyName)) {
            refreshView();
        } else if (JPDADebugger.PROP_CURRENT_THREAD.equals(propertyName)) {
            refreshView(); // [TODO]
        } else if (propertyName.equals (ExplorerManager.PROP_SELECTED_NODES)) {
            setActivatedNodes ((Node[]) evt.getNewValue ());
        }
    }

    private static boolean isJPDASession(Session s) {
        DebuggerEngine engine = s.getCurrentEngine ();
        if (engine == null) return false;
        return engine.lookupFirst(null, JPDADebugger.class) != null;
    }
    
    private void updateSessionsComboBox() {
        //Object selection = sessionComboBox.getSelectedItem();
        sessionComboBox.removeActionListener(sessionsComboListener);
        ComboBoxModel model = sessionComboBox.getModel();
        sessionComboBox.removeAllItems();
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Session[] sessions = dm.getSessions();
        for (int x = 0; x < sessions.length; x++) {
            if (isJPDASession(sessions[x])) {
                sessionComboBox.addItem(new SessionItem(sessions[x]));
            }
        }
        if (model.getSize() == 0) {
            sessionComboBox.addItem(new SessionItem(null));
        }
        sessionComboBox.setSelectedItem(new SessionItem(dm.getCurrentSession()));
        sessionComboBox.addActionListener(sessionsComboListener);
    }
    
    // **************************************************************************
    // implementation of TreeExpansion and TreeModel listener
    // **************************************************************************
    
    public void treeExpanded(TreeExpansionEvent event) {
        //System.out.println("treeExpanded: " + event.getPath().getLastPathComponent());
        refreshView();
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        //System.out.println("treeCollapsed: " + event.getPath().getLastPathComponent());
        refreshView();
    }

    public void treeNodesChanged(TreeModelEvent e) {
        //TreePath treePath = e.getTreePath();
        //System.out.println("treeNodesChanged: " + (treePath != null ? treePath.getLastPathComponent() : "NULL"));
        refreshView();
    }

    public void treeNodesInserted(TreeModelEvent e) {
        //TreePath treePath = e.getTreePath();
        //System.out.println("treeNodesInserted: " + (treePath != null ? treePath.getLastPathComponent() : "NULL"));
        refreshView();
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        //TreePath treePath = e.getTreePath();
        //System.out.println("treeNodesRemoved: " + (treePath != null ? treePath.getLastPathComponent() : "NULL"));
        refreshView();
    }

    public void treeStructureChanged(TreeModelEvent e) {
        //System.out.println("treeStructureChanged: " + (treePath != null ? treePath.getLastPathComponent() : "NULL"));
        refreshView();
    }
    
    // **************************************************************************
    
    InfoPanel getInfoPanel() {
        return infoPanel;
    }
    
    void refreshView() {
        SwingUtilities.invokeLater(new ViewRefresher());
    }

    private void adjustTreeScrollBar() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JViewport viewport = treeView.getViewport();
                Dimension viewSize = viewport.getExtentSize();
                Dimension treeSize = viewport.getViewSize();
                if (treeSize.width <= viewSize.width) {
                    scrollBarPanel.setVisible(false);
                } else {
                    scrollBarPanel.setVisible(true);
                    treeScrollBar.setMaximum(treeSize.width);
                    treeScrollBar.setVisibleAmount(viewSize.width);
                }
            }
        });
    }
    
    // **************************************************************************
    // implementation of AdjustmentListener (listens on horizontal scrollbar
    // connected to treeView)
    // **************************************************************************
    
    public void adjustmentValueChanged(AdjustmentEvent e) {
        JViewport viewport = treeView.getViewport();
        Point position = viewport.getViewPosition();
        Dimension viewSize = viewport.getExtentSize();
        Rectangle newRect = new Rectangle(e.getValue(), position.y, viewSize.width, viewSize.height);
        ((JComponent)viewport.getView()).scrollRectToVisible(newRect);
    }
    
    // **************************************************************************
    // implementation of ComponentListener on treeView
    // **************************************************************************
    
    public void stateChanged(ChangeEvent e) {
        adjustTreeScrollBar();
    }
    
    // **************************************************************************
    // inner classes
    // **************************************************************************
    
    private final class DebuggingPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (FiltersDescriptor.SHOW_SUSPEND_TABLE.equals(key)) {
                setSuspendTableVisible(evt.getNewValue().equals("true"));
            }
        }

    }

    private final class ViewRefresher implements Runnable {

        public void run() {
            // remove components in both panels, they will be recreated
            leftPanel.removeAll();
            rightPanel.removeAll();
            int sx = (rightPanel.getWidth() - ClickableIcon.CLICKABLE_ICON_WIDTH) / 2;
            int sy = 0;

            JPDAThread currentThread = debugger != null ? debugger.getCurrentThread() : null;
            // collect all deadlocked threads
            Set<Deadlock> deadlocks = debugger != null ? debugger.getThreadsCollector().getDeadlockDetector().getDeadlocks() : Collections.EMPTY_SET;
            if (deadlocks == null) {
                deadlocks = Collections.EMPTY_SET;
            }
            Set<JPDAThread> deadlockedThreads = new HashSet<JPDAThread>();
            for (Deadlock deadlock : deadlocks) {
                deadlockedThreads.addAll(deadlock.getThreads());
            }

            int mainPanelHeight = 0;
            int treeViewWidth = 0;
            int leftBarHeight = 0;
            boolean isCurrent = false;
            boolean isAtBreakpoint = false;
            boolean isInDeadlock = false;
            Object currentObject = null;

            for (TreePath path : treeView.getVisiblePaths()) {
                Node node = Visualizer.findNode(path.getLastPathComponent());
                JPDAThread jpdaThread = node.getLookup().lookup(JPDAThread.class);
                JPDAThreadGroup jpdaThreadGroup = node.getLookup().lookup(JPDAThreadGroup.class);
                if (jpdaThread != null || jpdaThreadGroup != null) {
                    if (currentObject != null) {
                        addPanels(currentObject, isCurrent, isAtBreakpoint, isInDeadlock, leftBarHeight, sx, sy);
                    }
                    leftBarHeight = 0;
                    if (jpdaThread != null) {
                        isCurrent = jpdaThread == currentThread && jpdaThread.isSuspended();
                        isAtBreakpoint = threadsListener.isBreakpointHit(jpdaThread);
                        isInDeadlock = deadlockedThreads.contains(jpdaThread);
                    } else {
                        isCurrent = false;
                        isAtBreakpoint = false;
                        isInDeadlock = false;
                    }
                    currentObject = jpdaThread != null ? jpdaThread : jpdaThreadGroup;
                }

                JTree tree = treeView.getTree();
                Rectangle rect = tree.getRowBounds(tree.getRowForPath(path));
                int height = rect != null ? (int) Math.round(rect.getHeight()) : 0;
                mainPanelHeight += height;
                treeViewWidth = rect != null ? Math.max(treeViewWidth, (int) Math.round(rect.getX() + rect.getWidth())) : treeViewWidth;
                leftBarHeight += height;
                sy += height;
            } // for
            if (currentObject != null) {
                addPanels(currentObject, isCurrent, isAtBreakpoint, isInDeadlock, leftBarHeight, sx, sy);
            }

            leftPanel.revalidate();
            leftPanel.repaint();
            rightPanel.revalidate();
            rightPanel.repaint();
            treeView.getTree().setPreferredSize(new Dimension(treeViewWidth, 10));
            mainPanel.setPreferredSize(new Dimension(10, mainPanelHeight));
            treeView.getTree().revalidate(); // [TODO] reduce revalidate calls
            treeView.revalidate();
            mainScrollPane.revalidate();
            mainPanel.revalidate();

            updateSessionsComboBox(); // [TODO]
        }

        private void addPanels(Object jpdaObject, boolean current, boolean atBreakpoint,
                boolean inDeadlock, int height, int sx, int sy) {
            JPanel leftBar = new BarPanel(current, atBreakpoint, inDeadlock, height);
            leftPanel.add(leftBar);
            
            JPanel rightBar = new JPanel(new GridBagLayout());
            rightBar.setBackground(treeBackgroundColor);
            rightBar.setOpaque(false);
            rightBar.setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, height));
            ClickableIcon icon = null;
            if (jpdaObject instanceof JPDAThread) {
                icon = new ClickableIcon(resumeIcon, focusedResumeIcon, pressedResumeIcon, suspendIcon,
                        focusedSuspendIcon, pressedSuspendIcon, (JPDAThread)jpdaObject, treeView);
                icon.setBackground(treeBackgroundColor);
                icon.setOpaque(false);
                
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                
                rightBar.add(icon, gridBagConstraints);
                icon.initializeState(DebuggingView.this, sx, sy, ClickableIcon.CLICKABLE_ICON_WIDTH, height);
            }
            rightPanel.add(rightBar);
            
//            BarsRecord barsRecord = new BarsRecord(leftBar, rightBar, icon, jpdaObject);
//            jpdaObjectToIndex.put(jpdaObject, barsRecord);
        }
    }
    
    private class BarPanel extends JPanel {
        
        private Color secondaryBarColor = null;
        private int height;
        boolean isEmpty;
        
        BarPanel(boolean isCurrent, boolean isAtBreakpoint, boolean isInDeadlock, int height) {
            this.height = height;
            String toolTipText = null;
            isEmpty = true;
            setPreferredSize(new Dimension(BAR_WIDTH, height));
            if (isInDeadlock) {
                setBackground(deadlockColor);
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_DEADLOCKED_THREAD_TIP");
            } else if (isCurrent) {
                setBackground(greenBarColor);
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_CURRENT_BAR_TIP");
            } else if (isAtBreakpoint) {
                setBackground(hitsBarColor);
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_BREAKPOINT_HIT_TIP");
            } else {
                setBackground(treeBackgroundColor);
                setOpaque(false);
            }
            if (toolTipText != null) {
                setToolTipText(toolTipText);
                isEmpty = false;
            }
            if (isCurrent && isInDeadlock) {
                secondaryBarColor = greenBarColor;
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_CURRENT_DEADLOCKED_TIP");
            }
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (isEmpty) {
                return;
            }
            Color originalColor = g.getColor();
            Rectangle clipRect = g.getClipBounds();
            if (secondaryBarColor != null) {
                g.setColor(secondaryBarColor);
                Rectangle bounds = getBounds();
                Rectangle rect = new Rectangle (bounds.width / 2 - 1, 0, (bounds.width + 1) / 2 + 1, bounds.height);
                rect = rect.intersection(clipRect);
                if (!rect.isEmpty()) {
                    g.fillRect(rect.x, rect.y, rect.width, rect.height);
                }
            }
            g.setColor(treeBackgroundColor);
            if (clipRect.y == 0) {
                g.drawLine(clipRect.x, 0, clipRect.x + clipRect.width - 1, 0);
            }
            if (clipRect.height == height) {
                g.drawLine(clipRect.x, height - 1, clipRect.x + clipRect.width - 1, height - 1);
            }
            g.setColor(originalColor);
        }
        
    }
    
    private class SessionsComboBoxListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            SessionItem si = (SessionItem)sessionComboBox.getSelectedItem();
            if (si != null) {
                Session ses = si.getSession();
                DebuggerManager dm = DebuggerManager.getDebuggerManager();
                if (ses != null && ses != dm.getCurrentSession()) {
                    dm.setCurrentSession(ses);
                }
            }
        }
        
    }
    
    private class SessionItem {
        
        private Session session;

        SessionItem(Session session) {
            this.session = session;
        }
        
        public Session getSession() {
            return session;
        }

        @Override
        public String toString() {
            if (session != null) {
                return session.getName();
            } else {
                return '<' + NbBundle.getMessage(DebuggingView.class, "LBL_No_Session_Running") + '>';
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SessionItem)) {
                return false;
            }
            Session s = ((SessionItem)obj).getSession();
            if (session == null) {
                return s == null;
            } else {
                return session.equals(s);
            }
        }

        @Override
        public int hashCode() {
            return 29 * 3 + (this.session != null ? this.session.hashCode() : 0);
        }

    }

}