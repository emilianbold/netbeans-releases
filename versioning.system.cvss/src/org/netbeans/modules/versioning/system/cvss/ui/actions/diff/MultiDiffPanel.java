/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateExecutor;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.lib.cvsclient.command.update.UpdateCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.awt.UndoRedo;
import org.openide.windows.WindowManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.LifecycleManager;
import org.openide.ErrorManager;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.diff.EditorSaveCookie;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import static org.netbeans.modules.versioning.util.CollectionUtils.copyArray;

/**
 *
 * @author Maros Sandor
 */
class MultiDiffPanel extends javax.swing.JPanel implements ActionListener, VersioningListener, DiffSetupSource, PropertyChangeListener, PreferenceChangeListener {
    
    /**
     * Array of DIFF setups that we show in the DIFF view. Contents of this array is changed if
     * the user switches DIFF types.
     */
    private Setup[] setups;
    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private EditorCookie[] editorCookies;

    private final DiffLookup lookup = new DiffLookup();
    
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 

    /**
     * Context in which to DIFF.
     */
    private final Context context;

    private int                     displayStatuses;

    /**
     * Display name of the context of this diff.
     */ 
    private final String contextName;
    
    private int currentType;
    private int currentIndex = -1;
    private int currentModelIndex = -1;
    
    private RequestProcessor.Task prepareTask;
    private DiffPrepareTask dpt;

    private AbstractAction nextAction;
    private AbstractAction          prevAction;
    
    /**
     * null for view that are not
     */
    private RequestProcessor.Task   refreshTask;

    private ExecutorGroup group;
    private volatile boolean        executed;

    private JComponent              diffView;
    private DiffFileTable           fileTable;
    private boolean                 dividerSet;

    /**
     * panel that is used for displaying the diff if {@code JSplitPane}
     * is not used
     */
    private final PlaceholderPanel diffViewPanel;
    private JComponent infoPanelLoadingFromRepo;

    /**
     * Creates diff panel and immediatelly starts loading...
     */
    public MultiDiffPanel(Context context, int initialType, String contextName, ExecutorGroup group) {
        this.context = context;
        this.contextName = contextName;
        this.group = group;
        currentType = initialType;

        diffViewPanel = null;
        initComponents();
        initFileTable();
        initToolbarButtons();
        initNextPrevActions();
        refreshTask = org.netbeans.modules.versioning.util.Utils.createTask(new RefreshViewTask());
        if (CvsModuleConfig.getDefault().getPreferences().getBoolean("autoDiffRefresh", true)) {
            onRefreshButton();
        } else {
            refreshTask.schedule(0);
        }
        refreshComponents();
    }

    /**
     * Construct diff component showing just one file.
     * It hides All, Local, Remote toggles and file chooser combo.
     */
    public MultiDiffPanel(File file, String rev1, String rev2) {
        context = null;
        contextName = file.getName();
        initComponents();
        initToolbarButtons();
        initNextPrevActions();

        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);

        // mimics refreshSetups()
        Setup[] localSetups = new Setup[] {new Setup(file, rev1, rev2)};
        setSetups(localSetups, DiffUtils.setupsToEditorCookies(localSetups));
        setDiffIndex(0, 0);
        dpt = new DiffPrepareTask(setups);
        prepareTask = CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(dpt);
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        removeAll();
        splitPane = null;
        setLayout(new BorderLayout());
        controlsToolBar.setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        add(controlsToolBar, BorderLayout.NORTH);
        add(replacement, BorderLayout.CENTER);
    }

    private void setSetups(Setup[] setups, EditorCookie[] cookies) {
        this.setups = setups;
        this.editorCookies = cookies;
    }

    private boolean fileTableSetSelectedIndexContext;

    public void tableRowSelected(int viewIndex) {
        if (fileTableSetSelectedIndexContext) return;
        setDiffIndex(viewIndex, 0);
    }
    
    UndoRedo getUndoRedo() {
        return delegatingUndoRedo;
    }

    private void cancelBackgroundTasks() {
        if (prepareTask != null) {
            prepareTask.cancel();
        }
    }

    public Lookup getLookup() {
        return lookup;
    }

    boolean canClose() {
        if (setups == null) {
            return true;
        }

        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        DiffUtils.cleanThoseWithEditorPaneOpen(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        return (saveCookies.length == 0)
               || SaveBeforeClosingDiffConfirmation.allSaved(saveCookies);
    }

    private static SaveCookie[] getSaveCookies(Setup[] setups,
                                               EditorCookie[] editorCookies) {
        assert setups.length == editorCookies.length;

        final int length = setups.length;
        SaveCookie[] proResult = new SaveCookie[length];

        int count = 0;
        for (int i = 0; i < length; i++) {
            EditorCookie editorCookie = editorCookies[i];
            if (editorCookie == null) {
                continue;
            }

            File baseFile = setups[i].getBaseFile();
            if (baseFile == null) {
                continue;
            }

            FileObject fileObj = FileUtil.toFileObject(baseFile);
            if (fileObj == null) {
                continue;
            }

            proResult[count++] = new EditorSaveCookie(editorCookie,
                                                      fileObj.getNameExt());
        }

        return CollectionUtils.shortenArray(proResult, count);
    }

    /**
     * Called by the enclosing TopComponent to interrupt the fetching task.
     */
    void componentClosed() {
        setSetups((Setup[]) null, null);
        /**
         * must disable these actions, otherwise key shortcuts would trigger them even after tab closure
         * see #159266
         */
        prevAction.setEnabled(false);
        nextAction.setEnabled(false);
        cancelBackgroundTasks(); 
    }

    public synchronized void setGroup(ExecutorGroup group) {
        this.group = group;
        if (executed && group != null) {
            group.executed();
        }
    }

    void requestActive() {
        if (diffView != null) {
            diffView.requestFocusInWindow();
        }
    }

    private void initFileTable() {
        fileTable = new DiffFileTable(this);
        splitPane.setTopComponent(fileTable.getComponent());
        splitPane.setBottomComponent(getInfoPanelLoading());
    }

    private void initToolbarButtons() {
        if (context != null) {
            commitButton.addActionListener(this);
            localToggle.addActionListener(this);
            remoteToggle.addActionListener(this);
            allToggle.addActionListener(this);

            commitButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "MSG_CommitDiff_Tooltip", contextName));
            updateButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "MSG_UpdateDiff_Tooltip", contextName));
            ButtonGroup grp = new ButtonGroup();
            grp.add(localToggle);
            grp.add(remoteToggle);
            grp.add(allToggle);
            if (currentType == Setup.DIFFTYPE_LOCAL) localToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_REMOTE) remoteToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_ALL) allToggle.setSelected(true);

            commitButton.setEnabled(false);
        } else {
            localToggle.setVisible(false);
            remoteToggle.setVisible(false);
            allToggle.setVisible(false);

            commitButton.setVisible(false);
            refreshButton.setVisible(false);
            updateButton.setVisible(false);
        }
    }
        
    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-next.png"))) {  // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/diff/Bundle").
                                                   getString("CTL_DiffPanel_Next_Tooltip"));                
            }
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/diff/Bundle").
                                                   getString("CTL_DiffPanel_Prev_Tooltip"));                
            }
            public void actionPerformed(ActionEvent e) {
                onPrevButton();
            }
        };
        nextButton.setAction(nextAction);
        prevButton.setAction(prevAction);
    }
    
    private JComponent getInfoPanelLoading() {
        if (infoPanelLoadingFromRepo == null) {
            infoPanelLoadingFromRepo = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
        }
        return infoPanelLoadingFromRepo;
    }

    private void refreshComponents() {
        DiffController view = setups != null && currentModelIndex != -1 ? setups[currentModelIndex].getView() : null;
        int currentDifferenceIndex = view != null ? view.getDifferenceIndex() : -1;
        if (view != null) {
            nextAction.setEnabled(currentIndex < setups.length - 1 || currentDifferenceIndex < view.getDifferenceCount() - 1);
        } else {
            nextAction.setEnabled(false);
        }
        prevAction.setEnabled(currentIndex > 0 || currentDifferenceIndex > 0);
        dividerSet = false;
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (refreshTask != null) {
            CvsVersioningSystem.getInstance().getStatusCache().addVersioningListener(this);
            CvsModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
        }
        JComponent parent = (JComponent) getParent();
        parent.getActionMap().put("jumpNext", nextAction);  // NOI18N
        parent.getActionMap().put("jumpPrev", prevAction); // NOI18N
    }

    private void updateSplitLocation() {
        if (dividerSet) return;
        JComponent parent = (JComponent) getParent();
        Dimension dim = parent == null ? new Dimension() : parent.getSize();
        if (dim.width <=0 || dim.height <=0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateSplitLocation();
                }
            });
            return;
        }
        dividerSet = true;
        JTable jt = fileTable.getTable();
        int optimalLocation = jt.getPreferredSize().height + jt.getTableHeader().getPreferredSize().height;
        if (optimalLocation > dim.height / 3) {
            optimalLocation = dim.height / 3;
        }
        if (optimalLocation <= jt.getTableHeader().getPreferredSize().height) {
            optimalLocation = jt.getTableHeader().getPreferredSize().height * 3;
        }
        splitPane.setDividerLocation(optimalLocation);
    }

    @Override
    public void removeNotify() {
        CvsVersioningSystem.getInstance().getStatusCache().removeVersioningListener(this);
        if (refreshTask != null) {
            CvsModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        }
        super.removeNotify();
    }
    
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            if (!affectsView(event)) return;
            if (CvsVersioningSystem.getInstance().getParameter(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING) != null) {
                refreshTask.schedule(1000);
            } else {
                refreshTask.schedule(200);
            }
        } else if (event.getId() == CvsVersioningSystem.EVENT_PARAM_CHANGED) {
            if (event.getParams()[0].equals(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING)) {
                if (CvsVersioningSystem.getInstance().getParameter(CvsVersioningSystem.PARAM_BATCH_REFRESH_RUNNING) == null) {
                    refreshTask.schedule(0);
                }
            }
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(CvsModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
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
            if (oldInfo.getStatus() == newInfo.getStatus()) return false;
        }
        return context.contains(file);
    }
    
    private void setDiffIndex(int idx, int location) {
        assert EventQueue.isDispatchThread();
        currentIndex = idx;
        DiffController view = null;
        
        if (currentIndex != -1) {
            currentModelIndex = showingFileTable() ? fileTable.getModelIndex(currentIndex) : 0;
            view = setups[currentModelIndex].getView();

            // enable Select in .. action
            FileObject fileObj = null;
            EditorCookie.Observable observableEditorCookie = null;
            File baseFile = setups[currentModelIndex].getBaseFile();
            if (baseFile != null) {
                fileObj = FileUtil.toFileObject(baseFile);
            }
            TopComponent tc = (TopComponent) getClientProperty(TopComponent.class);
            if (tc != null) {
                Node node = setups[currentModelIndex].getNode();
                tc.setActivatedNodes(new Node[] {node == null ? Node.EMPTY : node});
            }
            EditorCookie editorCookie = editorCookies[currentModelIndex];
            if (editorCookie instanceof EditorCookie.Observable) {
                observableEditorCookie = (EditorCookie.Observable) editorCookie;
            }
            
            diffView = null;
            if (view != null) {
                if (showingFileTable()) {
                    fileTableSetSelectedIndexContext = true;
                    fileTable.setSelectedIndex(currentIndex);
                    fileTableSetSelectedIndexContext = false;
                }
                diffView = view.getJComponent();
                diffView.getActionMap().put("jumpNext", nextAction);  // NOI18N
                diffView.getActionMap().put("jumpPrev", prevAction);  // NOI18N
                displayDiffView();
                if (location == -1) {
                    location = view.getDifferenceCount() - 1;
                }
                if (location >=0 && location < view.getDifferenceCount()) {
                    view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, location);
                }
            } else {
                diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
            }            
            lookup.setData(fileObj, observableEditorCookie, diffView.getActionMap());
        } else {
            currentModelIndex = -1;
            lookup.setData();
            diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoFileSelected"));
            lookup.setData(diffView.getActionMap());
            displayDiffView();
        }

        delegatingUndoRedo.setDiffView(diffView);

        refreshComponents();
//        if (focus) {
//            diffView.requestFocusInWindow();
//        }
    }

    private boolean showingFileTable() {
        return fileTable != null;
    }

    private void displayDiffView() {
        if (splitPane != null) {
            int gg = splitPane.getDividerLocation();
            splitPane.setBottomComponent(diffView);
            splitPane.setDividerLocation(gg);
        } else {
            diffViewPanel.setComponent(diffView);
        }
        diffView.requestFocusInWindow(); // #181451 HACK
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == commitButton) onCommitButton();
        else if (source == localToggle || source == remoteToggle || source == allToggle) onDiffTypeChanged();
    }

    private void onRefreshButton() {
        LifecycleManager.getDefault().saveAll();
        executeUpdateCommand(true);
    }

    private void onUpdateButton() {
        LifecycleManager.getDefault().saveAll();
        executeUpdateCommand(false);
    }
    
    private void onCommitButton() {
        assert EventQueue.isDispatchThread();
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        if ((saveCookies.length == 0)
                || SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
            CommitAction.invokeCommit(contextName, context, null);
        }
    }


    // TODO: reuse this code ... the same pattern is in SynchronizePanel
    private void executeUpdateCommand(boolean doNoChanges) {
        if (context == null || context.getRootFiles().length == 0) return;
        UpdateCommand cmd = new UpdateCommand();
        String msg;
        if (doNoChanges) {
            msg =  NbBundle.getMessage(MultiDiffPanel.class, "BK0001");
        } else {
            msg = NbBundle.getMessage(MultiDiffPanel.class, "BK0002");
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

        final ExecutorGroup group = new ExecutorGroup(msg);
        group.addExecutors(UpdateExecutor.splitCommand(cmd, CvsVersioningSystem.getInstance(), options, contextName));
        group.addBarrier(new Runnable() {
            @Override
            public void run() {
                if (!group.isFailed()) {
                    refreshTask.schedule(0);
                }
            }
        });
        org.netbeans.modules.versioning.util.Utils.post(new Runnable () {
            @Override
            public void run () {
                group.execute();
            }
        });
    }
    
    /** Next that is driven by visibility. It continues to next not yet visible difference. */
    private void onNextButton() {
        if (showingFileTable()) {
            currentIndex = fileTable.getSelectedIndex();
            currentModelIndex = fileTable.getSelectedModelIndex();
        }

        DiffController view = setups[currentModelIndex].getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (++currentDifferenceIndex >= view.getDifferenceCount()) {
                if (++currentIndex >= setups.length) {
                    currentIndex--;
                } else {
                    setDiffIndex(currentIndex, 0);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (++currentIndex >= setups.length) currentIndex = 0;
            setDiffIndex(currentIndex, 0);
        }
        refreshComponents();
    }

    private void onPrevButton() {
        DiffController view = setups[currentModelIndex].getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (--currentDifferenceIndex < 0) {
                if (--currentIndex < 0) {
                    currentIndex++;
                } else {
                    setDiffIndex(currentIndex, -1);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (--currentIndex < 0) currentIndex = setups.length - 1;
            setDiffIndex(currentIndex, -1);
        }
        refreshComponents();
    }

    /**
     * @return setups, takes into account Local, Remote, All switch
     */
    public Collection<Setup> getSetups() {
        if (setups == null) {
            return Collections.emptySet();
        } else {
            return Arrays.asList(setups);
        }
    }

    public String getSetupDisplayName() {
        return contextName;
    }


    private void refreshSetups() {
        if (dpt != null) {
            prepareTask.cancel();
        }

        File [] files;
        switch (currentType) {
        case Setup.DIFFTYPE_LOCAL:
            displayStatuses = FileInformation.STATUS_LOCAL_CHANGE;
            break;
        case Setup.DIFFTYPE_REMOTE:
            displayStatuses = FileInformation.STATUS_REMOTE_CHANGE; 
            break;
        case Setup.DIFFTYPE_ALL:
            displayStatuses = FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_REMOTE_CHANGE; 
            break;
        default:
            throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
        }
        files = DiffExecutor.getModifiedFiles(context, displayStatuses);
        
        final Setup [] newSetups = new Setup[files.length];
        for (int i = 0; i < newSetups.length; i++) {
            File file = files[i];
            newSetups[i] = new Setup(file, currentType);
            newSetups[i].setNode(new DiffNode(newSetups[i], new CvsFileNode(file)));
        }
        Arrays.sort(newSetups, new SetupsComparator());
        final EditorCookie[] cookies = DiffUtils.setupsToEditorCookies(newSetups);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                setSetups(newSetups, cookies);
                fileTable.setTableModel(setups, editorCookies);

                if (setups.length == 0) {
                    String noContentLabel;
                    switch (currentType) {
                        case Setup.DIFFTYPE_LOCAL:
                            noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoLocalChanges");
                            break;
                        case Setup.DIFFTYPE_REMOTE:
                            noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoRemoteChanges");
                            break;
                        case Setup.DIFFTYPE_ALL:
                            noContentLabel = NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoAllChanges");
                            break;
                        default:
                            throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
                    }
                    setSetups((Setup[]) null, null);
//            navigationCombo.setModel(new DefaultComboBoxModel(new Object [] { noContentLabel }));
                    fileTable.getComponent().setEnabled(false);
                    fileTable.getComponent().setPreferredSize(null);
                    Dimension dim = fileTable.getComponent().getPreferredSize();
                    fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                    diffView = null;
                    diffView = new NoContentPanel(noContentLabel);
                    displayDiffView();
                    nextAction.setEnabled(false);
                    prevAction.setEnabled(false);
                    revalidate();
                    repaint();
                    executed();
                } else {
                    fileTable.getComponent().setEnabled(true);
                    fileTable.getComponent().setPreferredSize(null);
                    Dimension dim = fileTable.getComponent().getPreferredSize();
                    fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                    setDiffIndex(0, 0);
                    commitButton.setEnabled(true);
                    dpt = new DiffPrepareTask(setups);
                    prepareTask = CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(dpt);
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException ex) {
                //
            } catch (InvocationTargetException ex) {
                //
            }
        }
    }

    private void onDiffTypeChanged() {
        if (localToggle.isSelected()) {
            if (currentType == Setup.DIFFTYPE_LOCAL) return;
            currentType = Setup.DIFFTYPE_LOCAL;
        } else if (remoteToggle.isSelected()) {
            if (currentType == Setup.DIFFTYPE_REMOTE) return;
            currentType = Setup.DIFFTYPE_REMOTE;
        } else if (allToggle.isSelected()) {
            if (currentType == Setup.DIFFTYPE_ALL) return;
            currentType = Setup.DIFFTYPE_ALL;
        }
        CvsModuleConfig.getDefault().setLastUsedModificationContext(currentType);
        refreshTask.schedule(0);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            refreshComponents();
        }
    }

    private class DiffPrepareTask implements Runnable {
        
        private final Setup[] prepareSetups;

        public DiffPrepareTask(Setup [] prepareSetups) {
            this.prepareSetups = prepareSetups;
        }

        public void run() {
            try {
                for (int i = 0; i < prepareSetups.length; i++) {
                    if (prepareSetups != setups) return;
                    try {
                        prepareSetups[i].initSources(group);  // slow network I/O
                        final int fi = i;
                        StreamSource ss1 = prepareSetups[fi].getFirstSource();
                        StreamSource ss2 = prepareSetups[fi].getSecondSource();
                        final DiffController view = DiffController.createEnhanced(ss1, ss2);  // possibly executing slow external diff
                        view.addPropertyChangeListener(MultiDiffPanel.this);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                prepareSetups[fi].setView(view);
                                if (prepareSetups != setups) {
                                    return;
                                }
                                if (currentModelIndex == fi) {
                                    setDiffIndex(currentIndex, 0);
                                }
                                if (splitPane != null) {
                                    updateSplitLocation();
                                }
                            }
                        });
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            } finally {
                executed();
            }
        }
    }

    private synchronized void executed() {
        if (group != null) {
            group.executed();
        } else {
            // to warn setGroup that DiffPrepareTask already finished
            executed = true;
        }
    }

    private static class SetupsComparator extends Utils.ByImportanceComparator {

        private FileStatusCache cache;

        public SetupsComparator() {
            cache = CvsVersioningSystem.getInstance().getStatusCache();
        }

        @Override
        public int compare(Object o1, Object o2) {
            File file1 = ((Setup) o1).getBaseFile();
            File file2 = ((Setup) o2).getBaseFile();
            int cmp = super.compare(cache.getStatus(file1), cache.getStatus(file2));
            if (cmp == 0) {
                return file1.getName().compareToIgnoreCase(file2.getName());
            }
            return cmp;
        }
    }

    private class RefreshViewTask implements Runnable {
        @Override
        public void run() {
            refreshSetups();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlsToolBar = new javax.swing.JToolBar();
        allToggle = new javax.swing.JToggleButton();
        jPanel3 = new javax.swing.JPanel();
        localToggle = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        remoteToggle = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        updateButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        commitButton = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();

        controlsToolBar.setFloatable(false);
        controlsToolBar.setRollover(true);

        allToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/remote_vs_local.png"))); // NOI18N
        allToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_All_Tooltip")); // NOI18N
        allToggle.setFocusable(false);
        allToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        allToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(allToggle);

        jPanel3.setMaximumSize(new java.awt.Dimension(12, 32767));

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 23, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel3);

        localToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/local_vs_local.png"))); // NOI18N
        localToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Local_Tooltip")); // NOI18N
        localToggle.setFocusable(false);
        localToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        localToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(localToggle);

        jPanel4.setMaximumSize(new java.awt.Dimension(12, 32767));

        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 12, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 23, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel4);

        remoteToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/remote_vs_remote.png"))); // NOI18N
        remoteToggle.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Remote_Tooltip")); // NOI18N
        remoteToggle.setFocusable(false);
        remoteToggle.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        remoteToggle.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(remoteToggle);

        jPanel1.setMaximumSize(new java.awt.Dimension(80, 32767));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 80, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 23, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel1);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-next.png"))); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Next_Tooltip")); // NOI18N
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(nextButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/diff-prev.png"))); // NOI18N
        prevButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Prev_Tooltip")); // NOI18N
        prevButton.setFocusable(false);
        prevButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        prevButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(prevButton);

        jPanel2.setMaximumSize(new java.awt.Dimension(30, 32767));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 30, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 23, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel2);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/refresh.png"))); // NOI18N
        refreshButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "refreshButton.toolTipText")); // NOI18N
        refreshButton.setFocusable(false);
        refreshButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        refreshButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        controlsToolBar.add(refreshButton);

        updateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/update.png"))); // NOI18N
        updateButton.setText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "jButton1.text")); // NOI18N
        updateButton.setFocusable(false);
        updateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        updateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        updateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateButtonActionPerformed(evt);
            }
        });
        controlsToolBar.add(updateButton);

        jPanel5.setMaximumSize(new java.awt.Dimension(20, 32767));

        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 20, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 23, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel5);

        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versioning/system/cvss/resources/icons/commit.png"))); // NOI18N
        commitButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MSG_CommitDiff_Tooltip")); // NOI18N
        commitButton.setFocusable(false);
        commitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        commitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(commitButton);

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(controlsToolBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
            .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(controlsToolBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(splitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
        onUpdateButton();
    }//GEN-LAST:event_updateButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        onRefreshButton();
    }//GEN-LAST:event_refreshButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton allToggle;
    private javax.swing.JButton commitButton;
    private javax.swing.JToolBar controlsToolBar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JToggleButton localToggle;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JToggleButton remoteToggle;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JButton updateButton;
    // End of variables declaration//GEN-END:variables
    
}
