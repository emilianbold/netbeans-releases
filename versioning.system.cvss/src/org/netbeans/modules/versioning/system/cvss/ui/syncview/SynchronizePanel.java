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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.syncview;

import java.util.prefs.PreferenceChangeEvent;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffExecutor;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.windows.TopComponent;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.LifecycleManager;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.util.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.prefs.PreferenceChangeListener;

/**
 * The main class of the Synchronize view, shows and acts on set of file roots. 
 * 
 * @author Maros Sandor
 */
class SynchronizePanel extends JPanel implements ExplorerManager.Provider, PropertyChangeListener, PreferenceChangeListener, VersioningListener, ActionListener {
    
    private ExplorerManager             explorerManager;
    private final CvsSynchronizeTopComponent parentTopComponent;
    private final CvsVersioningSystem   cvs;
    private Context                     context;
    private int                         displayStatuses;
    private boolean                     pendingRefresh;
    
    private SyncTable                   syncTable;
    private RequestProcessor.Task       refreshViewTask;
    private ExecutorGroup               refreshCommandGroup;

    private static final RequestProcessor   rp = new RequestProcessor("CVS-VersioningView", 1);  // NOI18N

    private final NoContentPanel noContentComponent = new NoContentPanel();

    /**
     * Creates a new Synchronize Panel managed by the given versioning system.
     *  
     * @param parent enclosing top component
     */ 
    public SynchronizePanel(CvsSynchronizeTopComponent parent) {
        this.parentTopComponent = parent;
        this.cvs = CvsVersioningSystem.getInstance();
        refreshViewTask = rp.create(new RefreshViewTask());
        explorerManager = new ExplorerManager ();
        displayStatuses = FileInformation.STATUS_REMOTE_CHANGE | FileInformation.STATUS_LOCAL_CHANGE;
        noContentComponent.setLabel(NbBundle.getMessage(SynchronizePanel.class, "MSG_No_Changes_All"));
        syncTable = new SyncTable();

        initComponents();
        setComponentsState();
        setVersioningComponent(syncTable.getComponent());
        reScheduleRefresh(0);

        // XXX click it in form editor
        jPanel2.setFloatable(false);
        jPanel2.putClientProperty("JToolBar.isRollover", Boolean.TRUE);  // NOI18N
        jPanel2.setLayout(new ToolbarLayout());

        parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        parent.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        parent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N
        parent.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N

        getActionMap().put("prevInnerView", new AbstractAction("") { // NOI18N
            public void actionPerformed(ActionEvent e) {
                onNextInnerView();
            }
        });
        getActionMap().put("nextInnerView", new AbstractAction("") { // NOI18N
            public void actionPerformed(ActionEvent e) {
                onPrevInnerView();
            }
        });
    }

    private void onPrevInnerView() {
        if (tgbLocal.isSelected()) {
            tgbRemote.setSelected(true);
        } else if (tgbRemote.isSelected()) {
            tgbAll.setSelected(true);
        } else {
            tgbLocal.setSelected(true);
        }
        onDisplayedStatusChanged();
    }

    private void onNextInnerView() {
        if (tgbLocal.isSelected()) {
            tgbAll.setSelected(true);
        } else if (tgbRemote.isSelected()) {
            tgbLocal.setSelected(true);
        } else {
            tgbRemote.setSelected(true);
        }
        onDisplayedStatusChanged();
    }
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(CvsModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        } 
    }

    CvsVersioningSystem getCvs() {
        return cvs;
    }

    /**
     * Sets roots (directories) to display in the view.
     * 
     * @param ctx new context if the Versioning panel
     */ 
    void setContext(Context ctx) {
        context = ctx;
        reScheduleRefresh(0);
    }
    
    public ExplorerManager getExplorerManager () {
        return explorerManager;
    }
    
    public void addNotify() {
        super.addNotify();
        CvsModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        cvs.getStatusCache().addVersioningListener(this);
        cvs.addVersioningListener(this);
        explorerManager.addPropertyChangeListener(this);
        reScheduleRefresh(0);   // the view does not listen for changes when it is not visible         
    }

    public void removeNotify() {
        CvsModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);        
        cvs.getStatusCache().removeVersioningListener(this);
        cvs.removeVersioningListener(this);
        explorerManager.removePropertyChangeListener(this);
        super.removeNotify();
    }
    
    private void setVersioningComponent(JComponent component)  {
        Component [] children = getComponents();
        for (int i = 0; i < children.length; i++) {
            Component child = children[i];
            if (child != jPanel2) {
                if (child == component) {
                    return;
                } else {
                    remove(child);
                    break;
                }
            }
        }
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START; gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1; gbc.weighty = 1;
        
        add(component, gbc);            
        revalidate();
        repaint();
    }
    
    private void setComponentsState() {
        ButtonGroup grp = new ButtonGroup();
        grp.add(tgbLocal);
        grp.add(tgbRemote);
        grp.add(tgbAll);
        if (displayStatuses == FileInformation.STATUS_LOCAL_CHANGE) {
            tgbLocal.setSelected(true);
        }
        else if (displayStatuses == FileInformation.STATUS_REMOTE_CHANGE) { 
            tgbRemote.setSelected(true);
        }
        else { 
            tgbAll.setSelected(true);
        }
    }

    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        if (context == null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    syncTable.setTableModel(new SyncFileNode[0]);
                }
            });
            return;
        }
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(SynchronizePanel.class, "MSG_Refreshing_Versioning_View"));
        try {
            ph.start();  // XXX created handle does not have set Cancelable hook
            final SyncFileNode [] nodes = getNodes(cvs.getFileTableModel(context, displayStatuses));  // takes long
            if (nodes == null) {
                return;
                // finally section
            }

            final String [] tableColumns;
            final String branchTitle;
            if (nodes.length > 0) {
                boolean stickyCommon = true;
                String currentSticky = nodes[0].getSticky();
                for (int i = 1; i < nodes.length; i++) {
                    if (Thread.interrupted()) {
                        // TODO fast clean model
                        return;
                    }
                    String sticky = nodes[i].getSticky();
                    if (sticky != currentSticky && (sticky == null || currentSticky == null || !sticky.equals(currentSticky))) {
                        stickyCommon = false;
                        break;
                    }
                }
                if (stickyCommon) {
                    tableColumns = new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH };
                    branchTitle = currentSticky.length() == 0 ? null : NbBundle.getMessage(SynchronizePanel.class, "CTL_VersioningView_BranchTitle_Single", currentSticky);
                } else {
                    tableColumns = new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_STICKY, SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH };
                    branchTitle = NbBundle.getMessage(SynchronizePanel.class, "CTL_VersioningView_BranchTitle_Multi");
                }
            } else {
                tableColumns = null;
                branchTitle = null;
            }

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (nodes.length > 0) {
                        syncTable.setColumns(tableColumns);
                        parentTopComponent.setBranchTitle(branchTitle);
                        setVersioningComponent(syncTable.getComponent());
                    } else {
                        setVersioningComponent(noContentComponent);
                    }
                    syncTable.setTableModel(nodes);
                    // finally section, it's enqueued after this request
                }
            });
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ph.finish();
                }
            });
        }
    }
    
    private SyncFileNode [] getNodes(CvsFileTableModel model) {
        CvsFileNode [] fnodes = model.getNodes();
        SyncFileNode [] nodes = new SyncFileNode[fnodes.length];
        for (int i = 0; i < fnodes.length; i++) {
            if (Thread.interrupted()) return null;
            CvsFileNode fnode = fnodes[i];
            nodes[i] = new SyncFileNode(fnode);
        }
        return nodes;
    }
    
    /**
     * Performs the "cvs commit" command on all diplayed roots plus "cvs add" for files that are not yet added.
     */ 
    private void onCommitAction() {
        LifecycleManager.getDefault().saveAll();
        CommitAction.invokeCommit(parentTopComponent.getContentTitle(), context, null);
    }
    
    /**
     * Performs the "cvs update" command on all diplayed roots.
     */ 
    private void onUpdateAction() {
        executeUpdateCommand(false);
    }
    
    /**
     * Refreshes statuses of all files in the view. It does that by issuing the "cvs -n update" command, updating the cache
     * and refreshing file nodes.
     */ 
    private void onRefreshAction() {
        LifecycleManager.getDefault().saveAll();
        refreshStatuses();
    }

    /**
     * Programmatically invokes the Refresh action.
     */ 
    void performRefreshAction() {
        refreshStatuses();
    }

    private void refreshStatuses() {
        executeUpdateCommand(true);
        reScheduleRefresh(1000);
    }

    /**
     * Shows Diff panel for all files in the view. The initial type of diff depends on the sync mode: Local, Remote, All.
     * In Local mode, the diff shows CURRENT <-> BASE differences. In Remote mode, it shows BASE<->HEAD differences. 
     */ 
    private void onDiffAction() {
        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(SynchronizePanel.class, "BK0003"));
        group.progress(NbBundle.getMessage(SynchronizePanel.class, "BK0004"));
        DiffExecutor exec = new DiffExecutor(context, parentTopComponent.getContentTitle());
        if (displayStatuses == FileInformation.STATUS_LOCAL_CHANGE) {
            LifecycleManager.getDefault().saveAll();
            exec.showLocalDiff(group);
        } else if (displayStatuses == FileInformation.STATUS_REMOTE_CHANGE) {
            exec.showRemoteDiff(group);
        } else {
            LifecycleManager.getDefault().saveAll();
            exec.showAllDiff(group);
        }
    }
    
    private void executeUpdateCommand(boolean doNoChanges) {
        if (context == null || context.getRootFiles().length == 0) return;
        UpdateCommand cmd = new UpdateCommand();
        String msg;
        if (doNoChanges) {
            msg = NbBundle.getMessage(SynchronizePanel.class, "BK0001");
        } else {
            msg = NbBundle.getMessage(SynchronizePanel.class, "BK0002");
        }
        cmd.setDisplayName(msg);
        GlobalOptions options = CvsVersioningSystem.createGlobalOptions();
        if (context.getExclusions().size() > 0) {
            options.setExclusions((File[]) context.getExclusions().toArray(new File[context.getExclusions().size()]));
        }
        cmd.setFiles(context.getRootFiles());
        cmd.setBuildDirectories(true);
        cmd.setPruneDirectories(true);
        options.setDoNoChanges(doNoChanges);
        // TODO: javacvs library fails to obey the -P flag when -q is specified
//        options.setModeratelyQuiet(true);
        refreshCommandGroup = new ExecutorGroup(msg);
        refreshCommandGroup.addExecutors(UpdateExecutor.splitCommand(cmd, cvs, options, parentTopComponent.getContentTitle()));
        refreshCommandGroup.execute();
        // XXX should not be there barrier?
        parentTopComponent.contentRefreshed();
    }

    private void onDisplayedStatusChanged() {
        if (tgbLocal.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_LOCAL_CHANGE);
            noContentComponent.setLabel(NbBundle.getMessage(SynchronizePanel.class, "MSG_No_Changes_Local"));
        }
        else if (tgbRemote.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_REMOTE_CHANGE);
            noContentComponent.setLabel(NbBundle.getMessage(SynchronizePanel.class, "MSG_No_Changes_Remote"));
        }
        else if (tgbAll.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_REMOTE_CHANGE | FileInformation.STATUS_LOCAL_CHANGE);
            noContentComponent.setLabel(NbBundle.getMessage(SynchronizePanel.class, "MSG_No_Changes_All"));
        }
    }

    private void setDisplayStatuses(int displayStatuses) {
        this.displayStatuses = displayStatuses;
        reScheduleRefresh(0);
    }

    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            if (!affectsView(event)) return;
            if (cvs.getParameter(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING) != null) {
                reScheduleRefresh(1000);
            } else {
                reScheduleRefresh(200);
            }
        } else if (event.getId() == CvsVersioningSystem.EVENT_PARAM_CHANGED) {
            if (pendingRefresh && event.getParams()[0].equals(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING)) {
                if (cvs.getParameter(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING) == null) {
                    pendingRefresh = false;
                    reScheduleRefresh(0);
                }
            }
        }
    }

    private boolean affectsView(VersioningEvent event) {
        File file = (File) event.getParams()[0];
        FileInformation oldInfo = (FileInformation) event.getParams()[1];
        FileInformation newInfo = (FileInformation) event.getParams()[2];
        if (oldInfo == null) {
            if ((newInfo.getStatus() & displayStatuses) == 0) return false;
        } else {
            if ((oldInfo.getStatus() & displayStatuses) + (newInfo.getStatus() & displayStatuses) == 0) return false;
        }
        if (context != null) {
            return context.contains(file);
        } else {
            return false;
        }
    }

    /** Reloads data from cache */
    private void reScheduleRefresh(int delayMillis) {
        refreshViewTask.schedule(delayMillis);
    }

    // TODO: HACK, replace by save/restore of column width/position
    void deserialize() {
        if (syncTable != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    syncTable.setDefaultColumnSizes();
                }
            });
        }
    }

    void focus() {
        syncTable.focus();
    }

    /**
     * Cancels both:
     * <ul>
     * <li>cache data fetching
     * <li>background cvs -N update
     * </ul>
     */
    public void cancelRefresh() {
        if (refreshCommandGroup != null) {
            refreshCommandGroup.cancel();
        }
        refreshViewTask.cancel();
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
        }
    }

    /**
     * Hardcoded toolbar layout. It eliminates need
     * for nested panels their look is hardly maintanable
     * accross several look and feels
     * (e.g. strange layouting panel borders on GTK+).
     *
     * <p>It sets authoritatively component height and takes
     * "prefered" width from components itself.
     *
     */
    private class ToolbarLayout implements LayoutManager {

        /** Expected border height */
        private int TOOLBAR_HEIGHT_ADJUSTMENT = 4;

        private int TOOLBAR_SEPARATOR_MIN_WIDTH = 12;

        /** Cached toolbar height */
        private int toolbarHeight = -1;

        /** Guard for above cache. */
        private Dimension parentSize;

        private Set adjusted = new HashSet();

        public void removeLayoutComponent(Component comp) {
        }

        public void layoutContainer(Container parent) {
            Dimension dim = SynchronizePanel.this.getSize();
            Dimension max = parent.getSize();

            int reminder = max.width - minimumLayoutSize(parent).width;

            int components = parent.getComponentCount();
            int horizont = 0;
            for (int i = 0; i<components; i++) {
                JComponent comp = (JComponent) parent.getComponent(i);
                if (comp.isVisible() == false) continue;
                comp.setLocation(horizont, 0);
                Dimension pref = comp.getPreferredSize();
                int width = pref.width;
                if (comp instanceof JSeparator && ((dim.height - dim.width) <= 0)) {
                    width = Math.max(width, TOOLBAR_SEPARATOR_MIN_WIDTH);
                }
                if (comp instanceof JProgressBar && reminder > 0) {
                    width += reminder;
                }
//                if (comp == getMiniStatus()) {
//                    width = reminder;
//                }

                // in column layout use taller toolbar
                int height = getToolbarHeight(dim) -1;
                comp.setSize(width, height);  // 1 verySoftBevel compensation
                horizont += width;
            }
        }

        public void addLayoutComponent(String name, Component comp) {
        }

        public Dimension minimumLayoutSize(Container parent) {

            // in column layout use taller toolbar
            Dimension dim = SynchronizePanel.this.getSize();
            int height = getToolbarHeight(dim);

            int components = parent.getComponentCount();
            int horizont = 0;
            for (int i = 0; i<components; i++) {
                Component comp = parent.getComponent(i);
                if (comp.isVisible() == false) continue;
                if (comp instanceof AbstractButton) {
                    adjustToobarButton((AbstractButton)comp);
                } else {
                    adjustToolbarComponentSize((JComponent)comp);
                }
                Dimension pref = comp.getPreferredSize();
                int width = pref.width;
                if (comp instanceof JSeparator && ((dim.height - dim.width) <= 0)) {
                    width = Math.max(width, TOOLBAR_SEPARATOR_MIN_WIDTH);
                }
                horizont += width;
            }

            return new Dimension(horizont, height);
        }

        public Dimension preferredLayoutSize(Container parent) {
            // Eliminates double height toolbar problem
            Dimension dim = SynchronizePanel.this.getSize();
            int height = getToolbarHeight(dim);

            return new Dimension(Integer.MAX_VALUE, height);
        }

        /**
         * Computes vertical toolbar components height that can used for layout manager hinting.
         * @return size based on font size and expected border.
         */
        private int getToolbarHeight(Dimension parent) {

            if (parentSize == null || (parentSize.equals(parent) == false)) {
                parentSize = parent;
                toolbarHeight = -1;
            }

            if (toolbarHeight == -1) {
                BufferedImage image = new BufferedImage(1,1,BufferedImage.TYPE_BYTE_GRAY);
                Graphics2D g = image.createGraphics();
                UIDefaults def = UIManager.getLookAndFeelDefaults();

                int height = 0;
                String[] fonts = {"Label.font", "Button.font", "ToggleButton.font"};      // NOI18N
                for (int i=0; i<fonts.length; i++) {
                    Font f = def.getFont(fonts[i]);
                    FontMetrics fm = g.getFontMetrics(f);
                    height = Math.max(height, fm.getHeight());
                }
                toolbarHeight = height + TOOLBAR_HEIGHT_ADJUSTMENT;
                if ((parent.height - parent.width) > 0) {
                    toolbarHeight += TOOLBAR_HEIGHT_ADJUSTMENT;
                }
            }

            return toolbarHeight;
        }


        /** Toolbar controls must be smaller and should be transparent*/
        private void adjustToobarButton(final AbstractButton button) {

            if (adjusted.contains(button)) return;

            // workaround for Ocean L&F clutter - toolbars use gradient.
            // To make the gradient visible under buttons the content area must not
            // be filled. To support rollover it must be temporarily filled
            if (button instanceof JToggleButton == false) {
                button.setContentAreaFilled(false);
                button.setMargin(new Insets(0, 3, 0, 3));
                button.setBorderPainted(false);
                button.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }

                    public void mouseExited(MouseEvent e) {
                        button.setContentAreaFilled(false);
                        button.setBorderPainted(false);
                    }
                });
            }

            adjustToolbarComponentSize(button);
        }

        private void adjustToolbarComponentSize(JComponent button) {

            if (adjusted.contains(button)) return;

            // as we cannot get the button small enough using the margin and border...
            if (button.getBorder() instanceof CompoundBorder) { // from BasicLookAndFeel
                Dimension pref = button.getPreferredSize();

                // XXX #41827 workaround w2k, that adds eclipsis (...) instead of actual text
                if ("Windows".equals(UIManager.getLookAndFeel().getID())) {  // NOI18N
                    pref.width += 9;
                }
                button.setPreferredSize(pref);
            }

            adjusted.add(button);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel2 = new javax.swing.JToolBar();
        tgbAll = new javax.swing.JToggleButton();
        tgbLocal = new javax.swing.JToggleButton();
        tgbRemote = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JSeparator();
        btnRefresh = new javax.swing.JButton();
        btnDiff = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnUpdate = new javax.swing.JButton();
        btnCommit = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/syncview/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(tgbAll, bundle.getString("CTL_Synchronize_Action_All_Label")); // NOI18N
        tgbAll.setToolTipText(bundle.getString("CTL_Synchronize_Action_All_Tooltip")); // NOI18N
        tgbAll.addActionListener(this);
        jPanel2.add(tgbAll);

        org.openide.awt.Mnemonics.setLocalizedText(tgbLocal, bundle.getString("CTL_Synchronize_Action_Local_Label")); // NOI18N
        tgbLocal.setToolTipText(bundle.getString("CTL_Synchronize_Action_Local_Tooltip")); // NOI18N
        tgbLocal.addActionListener(this);
        jPanel2.add(tgbLocal);

        org.openide.awt.Mnemonics.setLocalizedText(tgbRemote, bundle.getString("CTL_Synchronize_Action_Remote_Label")); // NOI18N
        tgbRemote.setToolTipText(bundle.getString("CTL_Synchronize_Action_Remote_Tooltip")); // NOI18N
        tgbRemote.addActionListener(this);
        jPanel2.add(tgbRemote);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(2, 20));
        jPanel2.add(jSeparator1);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/refresh.png"))); // NOI18N
        btnRefresh.setToolTipText(bundle.getString("CTL_Synchronize_Action_Refresh_Tooltip")); // NOI18N
        btnRefresh.setActionCommand(bundle.getString("CTL_Synchronize_TopComponent_Title")); // NOI18N
        btnRefresh.setPreferredSize(new java.awt.Dimension(22, 23));
        btnRefresh.addActionListener(this);
        jPanel2.add(btnRefresh);

        btnDiff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff.png"))); // NOI18N
        btnDiff.setToolTipText(bundle.getString("CTL_Synchronize_Action_Diff_Tooltip")); // NOI18N
        btnDiff.setPreferredSize(new java.awt.Dimension(22, 25));
        btnDiff.addActionListener(this);
        jPanel2.add(btnDiff);

        jPanel3.setOpaque(false);
        jPanel2.add(jPanel3);

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/update.png"))); // NOI18N
        btnUpdate.setToolTipText(bundle.getString("CTL_Synchronize_Action_Update_Tooltip")); // NOI18N
        btnUpdate.setActionCommand(bundle.getString("CTL_Synchronize_TopComponent_Title")); // NOI18N
        btnUpdate.setPreferredSize(new java.awt.Dimension(22, 25));
        btnUpdate.addActionListener(this);
        jPanel2.add(btnUpdate);

        btnCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/commit.png"))); // NOI18N
        btnCommit.setToolTipText(bundle.getString("CTL_CommitForm_Action_Commit_Tooltip")); // NOI18N
        btnCommit.setPreferredSize(new java.awt.Dimension(22, 25));
        btnCommit.addActionListener(this);
        jPanel2.add(btnCommit);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        add(jPanel2, gridBagConstraints);
    }

    // Code for dispatching events from components to event handlers.

    public void actionPerformed(java.awt.event.ActionEvent evt) {
        if (evt.getSource() == tgbAll) {
            SynchronizePanel.this.tgbAllActionPerformed(evt);
        }
        else if (evt.getSource() == tgbLocal) {
            SynchronizePanel.this.tgbLocalActionPerformed(evt);
        }
        else if (evt.getSource() == tgbRemote) {
            SynchronizePanel.this.tgbRemoteActionPerformed(evt);
        }
        else if (evt.getSource() == btnRefresh) {
            SynchronizePanel.this.btnRefreshActionPerformed(evt);
        }
        else if (evt.getSource() == btnDiff) {
            SynchronizePanel.this.btnDiffActionPerformed(evt);
        }
        else if (evt.getSource() == btnUpdate) {
            SynchronizePanel.this.btnUpdateActionPerformed(evt);
        }
        else if (evt.getSource() == btnCommit) {
            SynchronizePanel.this.btnCommitActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

    private void btnDiffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiffActionPerformed
        onDiffAction();
    }//GEN-LAST:event_btnDiffActionPerformed

    private void tgbAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbAllActionPerformed
        onDisplayedStatusChanged();
    }//GEN-LAST:event_tgbAllActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        onUpdateAction();
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void tgbRemoteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbRemoteActionPerformed
        onDisplayedStatusChanged();
    }//GEN-LAST:event_tgbRemoteActionPerformed

    private void tgbLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tgbLocalActionPerformed
        onDisplayedStatusChanged();
    }//GEN-LAST:event_tgbLocalActionPerformed

    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
        onCommitAction();
    }//GEN-LAST:event_btnCommitActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        onRefreshAction();
    }//GEN-LAST:event_btnRefreshActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnDiff;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JToolBar jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToggleButton tgbAll;
    private javax.swing.JToggleButton tgbLocal;
    private javax.swing.JToggleButton tgbRemote;
    // End of variables declaration//GEN-END:variables

}
