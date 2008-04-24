/*
 * DebuggingView.java
 *
 * Created on 3. duben 2008, 15:17
 */

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.ui.views.ViewModelListener;

import org.netbeans.spi.viewmodel.Models;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author  Dan
 */
public class DebuggingView extends TopComponent implements org.openide.util.HelpCtx.Provider,
       ExplorerManager.Provider, PropertyChangeListener, TreeExpansionListener, TreeModelListener {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "debugging"; //NOI18N
    private static final int ICON_WIDTH = 16;
    private static final int BAR_WIDTH = 8;
    
    static final Color hitsColor = new Color(255, 255, 178); // [TODO]
    static final Color deadlockColor = javax.swing.UIManager.getDefaults().getColor("nb.errorForeground"); // [TODO]
    
    private transient Color greenBarColor = new Color(189, 230, 170);
    private transient Color treeBackgroundColor = UIManager.getDefaults().getColor("Tree.background"); // NOI18N
    
    private transient ExplorerManager manager = new ExplorerManager();
    private transient ViewModelListener viewModelListener;
    private Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
    private PreferenceChangeListener prefListener;

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
    
    private ThreadsListener threadsListener;
    
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
    
        setSuspendTableVisible(preferences.getBoolean(FiltersDescriptor.SHOW_SUSPEND_TABLE, true));
        
        threadsListener = new ThreadsListener(this);
        
        resumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_16.png"));
        focusedResumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_focused_16.png"));
        pressedResumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_pressed_16.png"));
        suspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_16.png"));
        focusedSuspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_focused_16.png"));
        pressedSuspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_pressed_16.png"));
        
        treeView = new DebugTreeView();
        treeView.setRootVisible(false);
        treeView.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        treeView.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        mainPanel.add(treeView, BorderLayout.CENTER);
        
        tapPanel = new TapPanel();
        tapPanel.setOrientation(TapPanel.DOWN);
        tapPanel.setExpanded(true);
        // tooltip
        KeyStroke toggleKey = KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()); // [TODO]
        String keyText = Utilities.keyToString(toggleKey);
        
        tapPanel.setToolTipText(NbBundle.getMessage(DebuggingView.class, "LBL_TapPanel", keyText)); //NOI18N
        infoPanel = new InfoPanel(tapPanel);
        tapPanel.add(infoPanel);
        add(tapPanel, BorderLayout.SOUTH);
        
        treeView.setHorizontalScrollBar(treeScrollBar);
        
//        remove(scrollBarPanel);
//        scrollBarPanel.setVisible(false);

        manager.addPropertyChangeListener(this);
        treeView.addTreeExpansionListener(this);
        TreeModel model = treeView.getTree().getModel();
        model.addTreeModelListener(this);
        
        prefListener = new DebuggingPreferenceChangeListener();
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, preferences));
        
        // [TODO] do not hardcode component sizes
    }
 
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sessionComboBox = new javax.swing.JComboBox();
        mainScrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        leftPanel = new javax.swing.JPanel();
        rightPanel = new javax.swing.JPanel();
        scrollBarPanel = new javax.swing.JPanel();
        treeScrollBar = new javax.swing.JScrollBar();
        leftPanel1 = new javax.swing.JPanel();
        rightPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        sessionComboBox.setMaximumRowCount(1);
        sessionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Java Project" }));
        sessionComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        add(sessionComboBox, java.awt.BorderLayout.NORTH);

        mainScrollPane.setBorder(null);
        mainScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setPreferredSize(new java.awt.Dimension(32, 10));

        mainPanel.setLayout(new java.awt.BorderLayout());

        leftPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Tree.background"));
        leftPanel.setPreferredSize(new java.awt.Dimension(8, 0));
        leftPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        mainPanel.add(leftPanel, java.awt.BorderLayout.WEST);

        rightPanel.setBackground(javax.swing.UIManager.getDefaults().getColor("Tree.background"));
        rightPanel.setPreferredSize(new java.awt.Dimension(24, 0));
        rightPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        mainPanel.add(rightPanel, java.awt.BorderLayout.EAST);

        mainScrollPane.setViewportView(mainPanel);

        add(mainScrollPane, java.awt.BorderLayout.CENTER);

        scrollBarPanel.setMaximumSize(new java.awt.Dimension(2147483647, 17));
        scrollBarPanel.setLayout(new java.awt.BorderLayout());

        treeScrollBar.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollBarPanel.add(treeScrollBar, java.awt.BorderLayout.CENTER);

        leftPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("Tree.background"));
        leftPanel1.setPreferredSize(new java.awt.Dimension(8, 0));
        leftPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollBarPanel.add(leftPanel1, java.awt.BorderLayout.WEST);

        rightPanel1.setBackground(javax.swing.UIManager.getDefaults().getColor("Tree.background"));
        rightPanel1.setPreferredSize(new java.awt.Dimension(24, 0));
        rightPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        scrollBarPanel.add(rightPanel1, java.awt.BorderLayout.EAST);

        add(scrollBarPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel leftPanel;
    private javax.swing.JPanel leftPanel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JPanel rightPanel1;
    private javax.swing.JPanel scrollBarPanel;
    private javax.swing.JComboBox sessionComboBox;
    private javax.swing.JScrollBar treeScrollBar;
    // End of variables declaration//GEN-END:variables

    public void setRootContext(Models.CompoundModel model, DebuggerEngine engine) {
        if (engine != null) {
            JPDADebugger deb = engine.lookupFirst(null, JPDADebugger.class);
            synchronized (this) {
                if (previousDebugger != null) {
                    previousDebugger.removePropertyChangeListener(this);
                }
                previousDebugger = this.debugger;
                this.debugger = deb;
                this.session = engine.lookupFirst(null, Session.class);
                deb.addPropertyChangeListener(this);
            }
            threadsListener.changeDebugger(deb);
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
    
//    public void propertyChange(PropertyChangeEvent evt) {
//        //throw new UnsupportedOperationException("Not supported yet.");
//    }
    
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
    
//    public org.openide.util.HelpCtx getHelpCtx() {
//        return new org.openide.util.HelpCtx("NetbeansDebuggerSourcesNode"); // NOI18N
//    }
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public boolean requestFocusInWindow() {
//        return super.requestFocusInWindow();
//        if (debuggingPanel == null) return false;
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
            refreshView();
        } else if (propertyName.equals (ExplorerManager.PROP_SELECTED_NODES)) {
            setActivatedNodes ((Node[]) evt.getNewValue ());
        }
    }

    public void treeExpanded(TreeExpansionEvent event) {
        refreshView();
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        refreshView();
    }

    public void treeNodesChanged(TreeModelEvent e) {
        refreshView();
    }

    public void treeNodesInserted(TreeModelEvent e) {
        refreshView();
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        refreshView();
    }

    public void treeStructureChanged(TreeModelEvent e) {
        refreshView();
    }
    
    // **************************************************************************
    // **************************************************************************
    
    InfoPanel getInfoPanel() {
        return infoPanel;
    }
    
    void refreshView() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                leftPanel.removeAll();
                rightPanel.removeAll();
                int sy = 0;
                int sx = (rightPanel.getWidth() - ICON_WIDTH) / 2;
                JPDAThread currentThread = debugger != null ? debugger.getCurrentThread() : null;
                int mainPanelHeight = 0;
                int treeViewWidth = 0;
                int leftBarHeight = 0;
                boolean isCurrent = false;
                boolean isAtBreakpoint = false;
                for (TreePath path : treeView.getVisiblePaths()) {
                    Node node = Visualizer.findNode(path.getLastPathComponent());
                    JPDAThread jpdaThread = node.getLookup().lookup(JPDAThread.class);
                    if (jpdaThread != null) {
                        if (leftBarHeight > 0) {
                            addLeftBarPart(isCurrent, isAtBreakpoint, leftBarHeight);
                        }
                        leftBarHeight = 0;
                        isCurrent = jpdaThread == currentThread && jpdaThread.isSuspended();
                        isAtBreakpoint = infoPanel.isBreakpointHit(jpdaThread);
                    } else {
                        
                    }
                    
                    JTree tree = treeView.getTree();
                    Rectangle rect = tree.getRowBounds(tree.getRowForPath(path));
                    int height = rect != null ? (int)Math.round(rect.getHeight()) : 0; // [TODO] NPE
                    mainPanelHeight += height;
                    treeViewWidth = Math.max(treeViewWidth, (int)Math.round(rect.getX() + rect.getWidth()));
                    leftBarHeight += height;
                    
                    JLabel icon = jpdaThread != null ?
                        new ClickableIcon(resumeIcon, focusedResumeIcon, pressedResumeIcon,
                        suspendIcon, focusedSuspendIcon, pressedSuspendIcon, jpdaThread) : new JLabel();
                    icon.setPreferredSize(new Dimension(ICON_WIDTH, height));
                    icon.setBackground(treeBackgroundColor);
                    icon.setOpaque(false);
                    rightPanel.add(icon);
                    if (icon instanceof ClickableIcon) {
                        ((ClickableIcon)icon).initializeState(sx, sy, ICON_WIDTH, height);
                    }
                    sy += height;
                } // for
                if (leftBarHeight > 0) {
                    addLeftBarPart(isCurrent, isAtBreakpoint, leftBarHeight);
                }
                
                leftPanel.revalidate();
                leftPanel.repaint();
                rightPanel.revalidate();
                rightPanel.repaint();
                treeView.getTree().setPreferredSize(new Dimension(treeViewWidth, 10)); // [TODO] 10
                mainPanel.setPreferredSize(new Dimension(10, mainPanelHeight)); // [TODO] 10
                treeView.getTree().revalidate(); // [TODO] reduce revalidate calls
                treeView.revalidate();
                mainScrollPane.revalidate();
                mainPanel.revalidate();
                
                sessionComboBox.removeAllItems();
                Node root = manager.getRootContext();
                if (root != null) {
                    String comboItemText = root.getDisplayName();
                    synchronized (DebuggingView.this) {
                        if ((comboItemText == null || comboItemText.length() == 0) && session != null) {
                            comboItemText = session.getName();
                        }
                    }
                    sessionComboBox.addItem(comboItemText != null && comboItemText.length() > 0 ?
                        comboItemText : "Java Project"); // NOI18N [TODO]
                }
            }

            private void addLeftBarPart(boolean isCurrent, boolean isAtBreakpoint, int height) {
                JComponent label = new JPanel();
                String toolTipText = null;
                label.setPreferredSize(new Dimension(BAR_WIDTH, height));
                if (isCurrent) {
                    label.setBackground(greenBarColor);
                    toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_CURRENT_BAR_TIP");
                } else if (isAtBreakpoint) {
                    label.setBackground(hitsColor);
                    toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_BREAKPOINT_HIT_TIP");
                } else {
                    label.setBackground(treeBackgroundColor);
                    label.setOpaque(false);
                }
                if (toolTipText != null) {
                    label.setToolTipText(toolTipText);
                }
                leftPanel.add(label);
            }
        });
    }

    // **************************************************************************
    
    private final class DebuggingPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (FiltersDescriptor.SHOW_SUSPEND_TABLE.equals(key)) {
                setSuspendTableVisible(evt.getNewValue().equals("true")); // [TODO]
            }
        }

    }
    
}