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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.ui.status;

import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.HgException;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.update.UpdateAction;
import org.netbeans.modules.mercurial.ui.diff.DiffAction;
import org.netbeans.modules.mercurial.ui.diff.Setup;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.*;
import org.openide.util.Exceptions;
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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.logging.Level;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Exception;

/**
 * The main class of the Synchronize view, shows and acts on set of file roots.
 *
 * @author Maros Sandor
 */
class VersioningPanel extends JPanel implements ExplorerManager.Provider, PreferenceChangeListener, PropertyChangeListener, ActionListener {
    
    private ExplorerManager             explorerManager;
    private final HgVersioningTopComponent parentTopComponent;
    private final Mercurial             mercurial;
    private VCSContext                  context;
    private int                         displayStatuses;
    private String                      branchInfo;
    private SyncTable                   syncTable;
    private RequestProcessor.Task       refreshViewTask;
    private Thread                      refreshViewThread;
    
    private HgProgressSupport           hgProgressSupport;
    private static final RequestProcessor   rp = new RequestProcessor("MercurialView", 1, true);  // NOI18N
    
    private final NoContentPanel noContentComponent = new NoContentPanel();
    
    private static final int HG_UPDATE_TARGET_LIMIT = 100;
    
    /**
     * Creates a new Synchronize Panel managed by the given versioning system.
     *
     * @param parent enclosing top component
     */
    public VersioningPanel(HgVersioningTopComponent parent) {
        this.parentTopComponent = parent;
        this.mercurial = Mercurial.getInstance();
        refreshViewTask = rp.create(new RefreshViewTask());
        explorerManager = new ExplorerManager();
        displayStatuses = FileInformation.STATUS_LOCAL_CHANGE;
        noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_All")); // NOI18N
        syncTable = new SyncTable();
        
        initComponents();
        setVersioningComponent(syncTable.getComponent());
        reScheduleRefresh(0);
        
        // XXX click it in form editor, probbaly requires  Mattisse >=v2
        jPanel2.setFloatable(false);
        jPanel2.putClientProperty("JToolBar.isRollover", Boolean.TRUE);  // NOI18N
        jPanel2.setLayout(new ToolbarLayout());
    }
    
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }    
    
    public void refreshUpdateTargets(File repository) {
        if( repository == null) return;
        
        java.util.List<String> targetRevsList = HgCommand.getAllRevisions(repository);
        Set<String>  targetRevsSet = new LinkedHashSet<String>();
        
        int size;
        if( targetRevsList == null){
            size = 0;
            targetRevsSet.add(NbBundle.getMessage(VersioningPanel.class, "MSG_Update_Target_Default")); // NOI18N
        }else{
            size = targetRevsList.size();
            int i = 0 ;
            while( i < size && i < HG_UPDATE_TARGET_LIMIT){
                targetRevsSet.add(targetRevsList.get(i));
                i++;
            }
        }
        
        // TODO: need to add action handle so when Otehr is selected a 
        // dialog is brought up to select revision
        //if(size > HG_UPDATE_TARGET_LIMIT){
        //    targetRevsSet.add(NbBundle.getMessage(VersioningPanel.class, "MSG_Target_Other")); // NOI18N
        //}
        
        ComboBoxModel targetsModel = new DefaultComboBoxModel(new Vector<String>(targetRevsSet));                        
        updateTargetComboBox.setModel(targetsModel);
        
        if (targetRevsSet.size() > 0 ) {         
            updateTargetComboBox.setSelectedIndex(0);       
        }                 
    }
        
    public void propertyChange(PropertyChangeEvent evt) {

        if (evt.getPropertyName() == FileStatusCache.PROP_FILE_STATUS_CHANGED) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) evt.getNewValue();
            Mercurial.LOG.log(Level.FINE, "Status.propertyChange(): {0} file:  {1}", new Object [] { parentTopComponent.getContentTitle(), changedEvent.getFile()} ); // NOI18N
            //Diagnostics.println("Status.propertyChange(): " + // NOI18N
            //    parentTopComponent.getContentTitle() + " file: " + changedEvent.getFile()); // NOI18N
            if (!affectsView(evt)) return;
            reScheduleRefresh(1000);
        } 
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class, this);
            if (tc == null) return;
            tc.setActivatedNodes((Node[]) evt.getNewValue());
        }
    }
    
    /**
     * Sets roots (directories) to display in the view.
     *
     * @param ctx new context if the Versioning panel
     */
    void setContext(VCSContext ctx) {
        context = ctx;
        //reScheduleRefresh(0);
    }
    
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }
    
    public void addNotify() {
        super.addNotify();
        HgModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        mercurial.getFileStatusCache().addPropertyChangeListener(this);        
        mercurial.addPropertyChangeListener(this);
        explorerManager.addPropertyChangeListener(this);
        reScheduleRefresh(0);   // the view does not listen for changes when it is not visible
    }
    
    public void removeNotify() {
        HgModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        mercurial.getFileStatusCache().removePropertyChangeListener(this);
        mercurial.removePropertyChangeListener(this);
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
        
    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        if (context == null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    syncTable.setTableModel(new SyncFileNode[0]);
                    File root = HgUtils.getRootFile(HgUtils.getCurrentContext(null));
                    if (root != null) {
                        setRepositoryBranchInfo(root);
                        refreshUpdateTargets(root);                
                    }
                 }
            });
            return;
        }
        // XXX attach Cancelable hook
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(VersioningPanel.class, "MSG_Refreshing_Versioning_View")); // NOI18N
        try {
            refreshViewThread = Thread.currentThread();
            Thread.interrupted();  // clear interupted status
            ph.start();
            final SyncFileNode [] nodes = getNodes(context, displayStatuses);  // takes long
            
            if (nodes == null) {
                return;
                // finally section
            }
            
            final String [] tableColumns;
            final String branchTitle;
            File [] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
            if (files == null || files.length == 0) return;

            File root = mercurial.getTopmostManagedParent(files[0]);
            if (nodes.length > 0) {
                boolean stickyCommon = false;
                for (int i = 1; i < nodes.length; i++) {
                    if (Thread.interrupted()) {
                        // TODO set model that displays that fact to user
                        return;
                    }
                }
                
                // For now hide branch: tableColumns = new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_BRANCH,SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH };
                tableColumns = new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH };

                String title = null;
                try{
                    
                    String branchName = HgCommand.getBranchName(root);
                    if (branchName != null){
                        title = NbBundle.getMessage(VersioningPanel.class, "CTL_VersioningView_BranchTitle", branchName); // NOI18N
                    }else{
                        title = NbBundle.getMessage(VersioningPanel.class, "CTL_VersioningView_UnnamedBranchTitle"); // NOI18N
                    }                    
                }catch (HgException ex){
                    NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(ex);
                    DialogDisplayer.getDefault().notifyLater(e);
                }
                branchTitle = title;
            } else {
                tableColumns = null;
                branchTitle = null;
            }
            setRepositoryBranchInfo(root);
            refreshUpdateTargets(root);                
            
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

    

    private void setRepositoryBranchInfo(File root){
        String branchInfo = null;
        try     {
            int rev = HgCommand.getBranchRev(root);
            
            if (rev > -1) {
                branchInfo = org.openide.util.NbBundle.getMessage(VersioningPanel.class,
                        "CTL_VersioningView_BranchInfo", // NOI18N
                        rev,
                        HgCommand.getBranchShortChangesetHash(root));
            } else {
                branchInfo = org.openide.util.NbBundle.getMessage(VersioningPanel.class,
                        "CTL_VersioningView_BranchInfoNotCommitted"); // NOI18N
            }
        } catch (HgException ex) {
            Exceptions.printStackTrace(ex);
        }
        String repositoryStatus = NbBundle.getMessage(VersioningPanel.class, "CTL_VersioningView_StatusTitle", branchInfo); // NOI18N
        if( !repositoryStatus.equals(statusLabel.getText())){
            statusLabel.setText(repositoryStatus);
        }
    }
    
    private SyncFileNode [] getNodes(VCSContext context, int includeStatus) {
        HgFileNode [] fnodes = mercurial.getNodes(context, includeStatus);
        SyncFileNode [] nodes = new SyncFileNode[fnodes.length];
        for (int i = 0; i < fnodes.length; i++) {
            if (Thread.interrupted()) return null;
            HgFileNode fnode = fnodes[i];
            nodes[i] = new SyncFileNode(fnode, this);
        }
        return nodes;
    }
    
    public int getDisplayStatuses() {
        return displayStatuses;
    }
    
    public String getDisplayBranchInfo() {
        return branchInfo;
    }
    
    /**
     * Performs the "cvs commit" command on all diplayed roots plus "cvs add" for files that are not yet added. // NOI18N
     */
    private void onCommitAction() {
        //TODO: Status Commit Action
        LifecycleManager.getDefault().saveAll();            
        CommitAction.commit(parentTopComponent.getContentTitle(), context);
    }
    
    /**
     * Performs the "cvs update" command on all diplayed roots. // NOI18N
     */
    private void onUpdateAction() {
        int index = updateTargetComboBox.getSelectedIndex();
        if(index == 0){
            UpdateAction.update(context, null);
        }else{
            String revStr = (String) updateTargetComboBox.getSelectedItem();
            revStr = revStr.substring(0, revStr.indexOf(" ")); // NOI18N
            UpdateAction.update(context, revStr);
        }
        parentTopComponent.contentRefreshed();
    }
    
    /**
     * Refreshes statuses of all files in the view. It does
     * that by issuing the "hg status -mardui" command, updating the cache // NOI18N
     * and refreshing file nodes.
     */
    private void onRefreshAction() {
        LifecycleManager.getDefault().saveAll();
        if(context == null || context.getRootFiles().size() == 0) {
            return;
        }
        refreshStatuses();
    }
    
    /**
     * Programmatically invokes the Refresh action.
     * Connects to repository and gets recent status.
     */
    void performRefreshAction() {
        refreshStatuses();
    }
    
    /* Async Connects to repository and gets recent status. */
    private void refreshStatuses() {
        if(hgProgressSupport!=null) {
            hgProgressSupport.cancel();
            hgProgressSupport = null;
        }
        
        final String repository  = HgUtils.getRootPath(context);
        if (repository == null) return;

        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
        hgProgressSupport = new HgProgressSupport() {
            public void perform() {
                StatusAction.executeStatus(context, this);
                setupModels();
            }
        };

        parentTopComponent.contentRefreshed();
        hgProgressSupport.start(rp, repository, org.openide.util.NbBundle.getMessage(VersioningPanel.class, "LBL_Refresh_Progress")); // NOI18N

    }
    
    /**
     * Shows Diff panel for all files in the view. The initial type of diff depends on the sync mode: Local, Remote, All.
     * In Local mode, the diff shows CURRENT <-> BASE differences. In Remote mode, it shows BASE<->HEAD differences.
     */
    private void onDiffAction() {
        String title = parentTopComponent.getContentTitle();
        if (displayStatuses == FileInformation.STATUS_LOCAL_CHANGE) {
            LifecycleManager.getDefault().saveAll();
            DiffAction.diff(context, Setup.DIFFTYPE_LOCAL, title);
        } else if (displayStatuses == FileInformation.STATUS_REMOTE_CHANGE) {
            DiffAction.diff(context, Setup.DIFFTYPE_REMOTE, title);
        } else {
            LifecycleManager.getDefault().saveAll();
            DiffAction.diff(context, Setup.DIFFTYPE_ALL, title);
        }
    }
    
    
    
    private void onDisplayedStatusChanged() {
        setDisplayStatuses(FileInformation.STATUS_REMOTE_CHANGE | FileInformation.STATUS_LOCAL_CHANGE);
        noContentComponent.setLabel(NbBundle.getMessage(VersioningPanel.class, "MSG_No_Changes_All")); // NOI18N
    }
    
    private void setDisplayStatuses(int displayStatuses) {
        this.displayStatuses = displayStatuses;
        reScheduleRefresh(0);
    }
    
    private boolean affectsView(PropertyChangeEvent event) {
        FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) event.getNewValue();
        File file = changedEvent.getFile();
        FileInformation oldInfo = changedEvent.getOldInfo();
        FileInformation newInfo = changedEvent.getNewInfo();
        if (oldInfo == null) {
            if ((newInfo.getStatus() & displayStatuses) == 0) return false;
        } else {
            if ((oldInfo.getStatus() & displayStatuses) + (newInfo.getStatus() & displayStatuses) == 0) return false;
        }
        File root = Mercurial.getInstance().getTopmostManagedParent(file);
        setRepositoryBranchInfo(root);
        refreshUpdateTargets(root);                
        return context == null? false: context.contains(file);
    }

    /** Reloads data from cache */
    private void reScheduleRefresh(int delayMillis) {
        refreshViewTask.schedule(delayMillis);
    }
    
    // HACK copy&paste HACK, replace by save/restore of column width/position
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
     * "prefered" width from components itself. // NOI18N
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
        
        private Set<JComponent> adjusted = new HashSet<JComponent>();
        
        public void removeLayoutComponent(Component comp) {
        }
        
        public void layoutContainer(Container parent) {
            Dimension dim = VersioningPanel.this.getSize();
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
            Dimension dim = VersioningPanel.this.getSize();
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
            Dimension dim = VersioningPanel.this.getSize();
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

        jComboBox1 = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JToolBar();
        jPanel4 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        btnRefresh = new javax.swing.JButton();
        btnDiff = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btnCommit = new javax.swing.JButton();
        btnUpdate = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        updateTargetComboBox = new javax.swing.JComboBox();

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setLayout(new java.awt.GridBagLayout());

        jPanel2.setBorderPainted(false);

        jPanel4.setOpaque(false);
        jPanel2.add(jPanel4);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/mercurial/ui/status/Bundle"); // NOI18N
        statusLabel.setText(bundle.getString("CTL_Versioning_Status_Table_Title")); // NOI18N
        statusLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statusLabel.setMaximumSize(new java.awt.Dimension(120, 17));
        statusLabel.setMinimumSize(new java.awt.Dimension(120, 17));
        jPanel2.add(statusLabel);
        statusLabel.getAccessibleContext().setAccessibleName(bundle.getString("CTL_Versioning_Status_Table_Title")); // NOI18N

        jPanel1.setOpaque(false);
        jPanel1.add(jSeparator1);

        jPanel2.add(jPanel1);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jPanel2.add(jSeparator2);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/refresh.png"))); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(VersioningPanel.class, "CTL_Synchronize_Action_Refresh_Tooltip")); // NOI18N
        btnRefresh.setMaximumSize(new java.awt.Dimension(28, 28));
        btnRefresh.setMinimumSize(new java.awt.Dimension(28, 28));
        btnRefresh.setPreferredSize(new java.awt.Dimension(22, 25));
        btnRefresh.addActionListener(this);
        jPanel2.add(btnRefresh);
        btnRefresh.getAccessibleContext().setAccessibleName("Refresh Status");

        btnDiff.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff.png"))); // NOI18N
        btnDiff.setToolTipText(bundle.getString("CTL_Synchronize_Action_Diff_Tooltip")); // NOI18N
        btnDiff.setFocusable(false);
        btnDiff.setPreferredSize(new java.awt.Dimension(22, 25));
        btnDiff.addActionListener(this);
        jPanel2.add(btnDiff);
        btnDiff.getAccessibleContext().setAccessibleName("Diff All");

        jPanel3.setOpaque(false);
        jPanel2.add(jPanel3);

        btnCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/commit.png"))); // NOI18N
        btnCommit.setToolTipText(bundle.getString("CTL_CommitForm_Action_Commit_Tooltip")); // NOI18N
        btnCommit.setFocusable(false);
        btnCommit.setPreferredSize(new java.awt.Dimension(22, 25));
        btnCommit.addActionListener(this);
        jPanel2.add(btnCommit);
        btnCommit.getAccessibleContext().setAccessibleName("Commit");

        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/update.png"))); // NOI18N
        btnUpdate.setToolTipText(bundle.getString("CTL_Synchronize_Action_Update_Tooltip")); // NOI18N
        btnUpdate.setFocusable(false);
        btnUpdate.setPreferredSize(new java.awt.Dimension(22, 25));
        btnUpdate.addActionListener(this);
        jPanel2.add(btnUpdate);
        btnUpdate.getAccessibleContext().setAccessibleName("Update");

        jPanel5.setOpaque(false);
        jPanel2.add(jPanel5);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(VersioningPanel.class, "CTL_Versioning_Status_Update_Target")); // NOI18N
        jPanel2.add(jLabel1);

        updateTargetComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "<default - tip>" }));
        jPanel2.add(updateTargetComboBox);

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
        if (evt.getSource() == btnDiff) {
            VersioningPanel.this.btnDiffActionPerformed(evt);
        }
        else if (evt.getSource() == btnCommit) {
            VersioningPanel.this.btnCommitActionPerformed(evt);
        }
        else if (evt.getSource() == btnUpdate) {
            VersioningPanel.this.btnUpdateActionPerformed(evt);
        }
        else if (evt.getSource() == btnRefresh) {
            VersioningPanel.this.btnRefreshActionPerformed(evt);
        }
    }// </editor-fold>//GEN-END:initComponents

private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        onRefreshAction();
}//GEN-LAST:event_btnRefreshActionPerformed
    
    private void btnDiffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDiffActionPerformed
        onDiffAction();
    }//GEN-LAST:event_btnDiffActionPerformed
    
    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        onUpdateAction();
    }//GEN-LAST:event_btnUpdateActionPerformed
    
    private void btnCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommitActionPerformed
        onCommitAction();
    }//GEN-LAST:event_btnCommitActionPerformed
        
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCommit;
    private javax.swing.JButton btnDiff;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnUpdate;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JToolBar jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JComboBox updateTargetComboBox;
    // End of variables declaration//GEN-END:variables
    
}
