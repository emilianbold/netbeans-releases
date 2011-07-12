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

package org.netbeans.modules.mercurial.ui.diff;

import org.openide.util.Cancellable;
import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.FileStatusCache;
import org.netbeans.modules.mercurial.FileInformation;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.ui.commit.CommitAction;
import org.netbeans.modules.mercurial.ui.status.StatusAction;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.LifecycleManager;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.mercurial.HgFileNode;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage.HgRevision;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.diff.EditorSaveCookie;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
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
public class MultiDiffPanel extends javax.swing.JPanel implements ActionListener, DiffSetupSource, PropertyChangeListener, PreferenceChangeListener {
    
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
    
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo(); 
    private final DiffLookup lookup = new DiffLookup();

    /**
     * Context in which to DIFF.
     */
    private final VCSContext context;

    private int displayStatuses;

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

    private JComponent              diffView;
    private DiffFileTable           fileTable;
    private boolean                 dividerSet;

    /**
     * panel that is used for displaying the diff if {@code JSplitPane}
     * is not used
     */
    private final PlaceholderPanel diffViewPanel;
    private JComponent infoPanelLoadingFromRepo;

    private HgProgressSupport executeStatusSupport;
    
    /**
     * Creates diff panel and immediatelly starts loading...
     */
    public MultiDiffPanel(VCSContext context, int initialType, String contextName) {
        this.context = context;
        this.contextName = contextName;
        currentType = initialType;
        initComponents();
        setAquaBackground();

        diffViewPanel = null;
        initFileTable();
        initToolbarButtons();
        initNextPrevActions();
        refreshComponents();

        refreshTask = org.netbeans.modules.versioning.util.Utils.createTask(new RefreshViewTask());
        refreshStatuses();
    }

    private void setAquaBackground() {
        if( "Aqua".equals( UIManager.getLookAndFeel().getID() ) ) {             // NOI18N
            Color color = UIManager.getColor("NbExplorerView.background");      // NOI18N
            setBackground(color); 
            controlsToolBar.setBackground(color); 
            jPanel1.setBackground(color); 
            jPanel2.setBackground(color); 
            jPanel4.setBackground(color); 
            jPanel5.setBackground(color); 
        }
    }

    /**
     * Construct diff component showing just one file.
     * It hides All, Local, Remote toggles and file chooser combo.
     */
    public MultiDiffPanel(File file, HgRevision rev1, HgRevision rev2, boolean forceNonEditable) {
        context = null;
        contextName = file.getName();
        initComponents();
        setAquaBackground();

        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);
        initToolbarButtons();
        initNextPrevActions();

        // mimics refreshSetups()
        Setup[] localSetups = new Setup[] {new Setup(file, rev1, rev2, forceNonEditable)};
        setSetups(localSetups, DiffUtils.setupsToEditorCookies(localSetups));
        setDiffIndex(0, 0, false);
        dpt = new DiffPrepareTask(setups);
        prepareTask = Mercurial.getInstance().getRequestProcessor().post(dpt);
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        removeAll();
        splitPane = null;
        setLayout(new BorderLayout());
        controlsToolBar.setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        add(controlsToolBar, BorderLayout.NORTH);
        add(replacement, BorderLayout.CENTER);
    }

    private void setSetups(Setup[] setups, EditorCookie[] editorCookies) {
        this.setups = setups;
        this.editorCookies = editorCookies;
    }

    private boolean fileTableSetSelectedIndexContext;

    public void tableRowSelected(int viewIndex) {
        if (fileTableSetSelectedIndexContext) return;
        setDiffIndex(viewIndex, 0, true);
    }
    
    UndoRedo getUndoRedo() {
        return delegatingUndoRedo;
    }

    private void cancelBackgroundTasks() {
        if (prepareTask != null) {
            prepareTask.cancel();
        }
        if(executeStatusSupport!=null) {
            executeStatusSupport.cancel();
        }
    }

    public Lookup getLookup() {
        return lookup;
    }

    boolean canClose() {
        if (setups == null) {
            return true;
        }

        SaveCookie[] saveCookies = getSaveCookies(true);

        return (saveCookies.length == 0)
               || SaveBeforeClosingDiffConfirmation.allSaved(saveCookies);
    }

    public SaveCookie[] getSaveCookies (boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = getEditorCookiesIntern(ommitOpened);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);
        return saveCookies;
    }

    public EditorCookie[] getEditorCookies (boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = getEditorCookiesIntern(ommitOpened);
        int count = 0, length = editorCookiesCopy.length;
        EditorCookie[] editorCookiesShorten = new EditorCookie[length];
        for (int i = 0; i < length; i++) {
            EditorCookie editorCookie = editorCookiesCopy[i];
            if (editorCookie == null) {
                continue;
            }
            editorCookiesShorten[count++] = editorCookie;
        }
        return CollectionUtils.shortenArray(editorCookiesShorten, count);
    }

    private EditorCookie[] getEditorCookiesIntern (boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        if (ommitOpened) {
            DiffUtils.cleanThoseWithEditorPaneOpen(editorCookiesCopy);
        }
        return editorCookiesCopy;
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

    public void requestActive() {
        if (fileTable != null) {
            fileTable.getTable().requestFocusInWindow();
        } else if (diffView != null) {
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
            commitButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "CTL_CommitDiff_Tooltip"));
            commitButton.setEnabled(false);     //until the diff is loaded
        } else {
            commitButton.setVisible(false);
            refreshButton.setVisible(false);
        }
    }

    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-next.png"))) {  // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/mercurial/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Next_Tooltip"));                
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/mercurial/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Prev_Tooltip"));                
            }
            @Override
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
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (refreshTask != null) {
            Mercurial.getInstance().getFileStatusCache().addPropertyChangeListener(this);
            HgModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
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
                @Override
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
        Mercurial.getInstance().getFileStatusCache().removePropertyChangeListener(this);
        if (refreshTask != null) {
            HgModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        }
        super.removeNotify();
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
        return context == null? false: context.contains(file);
    }
    
    private void setDiffIndex(int idx, int location, boolean restartPrepareTask) {
        currentIndex = idx;
        DiffController view = null;
        
        if (currentIndex != -1) {
            if (restartPrepareTask && dpt != null) {
                dpt.cancel();
                dpt.setTableIndex(currentIndex);
                prepareTask.schedule(100);
            }
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
                displayDiffView();
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == commitButton) onCommitButton();
    }

    private void onRefreshButton() {
        refreshStatuses();
    }

    private void refreshStatuses() {
        if (context == null || context.getRootFiles().isEmpty()) {
            return;
        }

        if(executeStatusSupport!=null) {
            executeStatusSupport.cancel();
            executeStatusSupport = null;
        }
        
        LifecycleManager.getDefault().saveAll();
        RequestProcessor rp = Mercurial.getInstance().getRequestProcessor();
        executeStatusSupport = new HgProgressSupport() {
            @Override
            public void perform() {                                                
                StatusAction.executeStatus(context, this);
                refreshSetups();
            }
        };
        File repositoryRoot = HgUtils.getRootFile(context);
        executeStatusSupport.start(rp, repositoryRoot, NbBundle.getMessage(MultiDiffPanel.class, "MSG_Refresh_Progress"));
    }
    
    private void onCommitButton() {
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        if ((saveCookies.length == 0)
                || SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
            CommitAction.commit(contextName, context);
        }
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
            if (++currentDifferenceIndex >= view.getDifferenceCount()) { // also passes for view.getDifferenceCount() == 0
                if (++currentIndex >= setups.length) {
                    currentIndex--;
                } else {
                    setDiffIndex(currentIndex, 0, true);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (++currentIndex >= setups.length) currentIndex = 0;
            setDiffIndex(currentIndex, 0, true);
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
                    setDiffIndex(currentIndex, -1, true);
                }
            } else if (currentDifferenceIndex < view.getDifferenceCount()) {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            if (--currentIndex < 0) currentIndex = setups.length - 1;
            setDiffIndex(currentIndex, -1, true);
        }
        refreshComponents();
    }

    /**
     * @return setups, takes into account Local, Remote, All switch
     */
    @Override
    public Collection<Setup> getSetups() {
        if (setups == null) {
            return Collections.emptySet();
        } else {
            return Arrays.asList(setups);
        }
    }

    @Override
    public String getSetupDisplayName() {
        return contextName;
    }


    private void refreshSetups() {
        if (dpt != null) {
            prepareTask.cancel();
        }

        int status;
        switch (currentType) {
        case Setup.DIFFTYPE_LOCAL:
            status = FileInformation.STATUS_LOCAL_CHANGE;
            break;
        case Setup.DIFFTYPE_REMOTE:
            status = FileInformation.STATUS_REMOTE_CHANGE;
            break;
        case Setup.DIFFTYPE_ALL:
            status = FileInformation.STATUS_LOCAL_CHANGE | FileInformation.STATUS_REMOTE_CHANGE;
            break;
        default:
            throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
        }
        File [] files = HgUtils.getModifiedFiles(context, status, true);
        final int localDisplayStatuses = status;
        final Setup[] localSetups = computeSetups(files);
        final EditorCookie[] cookies = DiffUtils.setupsToEditorCookies(localSetups);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dividerSet = false;
                displayStatuses = localDisplayStatuses;
                setSetups(localSetups, cookies);
                boolean propertyColumnVisible = false;
                for (Setup setup : setups) {
                    if (setup.getPropertyName() != null) {
                        propertyColumnVisible = true;
                        break;
                    }
                }
                fileTable.setColumns(propertyColumnVisible ? new String[]{DiffNode.COLUMN_NAME_NAME, DiffNode.COLUMN_NAME_PROPERTY, DiffNode.COLUMN_NAME_STATUS, DiffNode.COLUMN_NAME_LOCATION} : new String[]{DiffNode.COLUMN_NAME_NAME, DiffNode.COLUMN_NAME_STATUS, DiffNode.COLUMN_NAME_LOCATION});
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
                            throw new IllegalStateException("Unknown DIFF type:" + currentType);
                    }
                    setSetups((Setup[]) null, null);
                    fileTable.getComponent().setEnabled(false);
                    fileTable.getComponent().setPreferredSize(null);
                    Dimension dim = fileTable.getComponent().getPreferredSize();
                    fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                    diffView = null;
                    diffView = new NoContentPanel(noContentLabel);
                    displayDiffView();
                    nextAction.setEnabled(false);
                    prevAction.setEnabled(false);
                    commitButton.setEnabled(false);
                    revalidate();
                    repaint();
                } else {
                    fileTable.getComponent().setEnabled(true);
                    fileTable.getComponent().setPreferredSize(null);
                    Dimension dim = fileTable.getComponent().getPreferredSize();
                    fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                    setDiffIndex(0, 0, false);
                    commitButton.setEnabled(true);
                    dpt = new DiffPrepareTask(setups);
                    prepareTask = Mercurial.getInstance().getRequestProcessor().post(dpt);
                }
            }
        };
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException ex) {
                Mercurial.LOG.log(Level.FINE, null, ex);
            } catch (InvocationTargetException ex) {
                Mercurial.LOG.log(Level.FINE, null, ex);
            }
        }
    }

    private Setup[] computeSetups(File[] files) {
        List<Setup> newSetups = new ArrayList<Setup>(files.length);
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (!file.isDirectory()) {
                Setup setup = new Setup(file, null, currentType);
                setup.setNode(new DiffNode(setup, new HgFileNode(file)));
                newSetups.add(setup);
            }
        }
        Collections.sort(newSetups, new SetupsComparator());
        return newSetups.toArray(new Setup[newSetups.size()]);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            refreshComponents();
        } else if (FileStatusCache.PROP_FILE_STATUS_CHANGED.equals(evt.getPropertyName())) {
            if (!affectsView(evt)) {
                return;
            }
            refreshTask.schedule(200);
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(HgModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }

    private class DiffPrepareTask implements Runnable, Cancellable {
        
        private final Setup[] prepareSetups;
        private int tableIndex; // index of a row in the table - viewIndex, needs to be translated to modelIndex
        private boolean canceled;

        public DiffPrepareTask(Setup [] prepareSetups) {
            this.prepareSetups = prepareSetups;
            this.tableIndex = 0;
        }

        @Override
        public void run() {
            canceled = false;
            IOException exception = null;
            int[] indexes = prepareIndexesToRefresh();
            for (int i : indexes) {
                if (prepareSetups != setups || Thread.interrupted()) return;
                int modelIndex = fileTable == null ? i : fileTable.getModelIndex(i);
                if (prepareSetups[modelIndex].getView() != null) {
                    continue;
                }
                try {
                    prepareSetups[modelIndex].initSources();  // slow network I/O
                    final int fi = modelIndex;
                    if (Thread.interrupted() || canceled) {
                        return;
                    }
                    StreamSource ss1 = prepareSetups[fi].getFirstSource();
                    StreamSource ss2 = prepareSetups[fi].getSecondSource();
                    final DiffController view = DiffController.createEnhanced(ss1, ss2);  // possibly executing slow external diff
                    view.addPropertyChangeListener(MultiDiffPanel.this);
                    if (Thread.interrupted() || canceled) {
                        return;
                    }
                    prepareSetups[fi].setView(view);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (prepareSetups != setups) {
                                return;
                            }
                            if (currentModelIndex == fi) {
                                setDiffIndex(currentIndex, 0, false);
                            }
                            if (splitPane != null) {
                                updateSplitLocation();
                            }
                        }
                    });
                } catch (IOException e) {
                    if (HgUtils.isCanceled(e)) {
                        Logger.getLogger(MultiDiffPanel.class.getName()).log(Level.FINE, null, e);
                        return;
                    } else {
                        Mercurial.LOG.log(Level.INFO, null, e);
                        if (exception == null) {
                            // save only the first exception
                            exception = e;
                        }
                    }
                }
            }
            if (exception != null) {
                // notify user of the failure
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Exception(exception));
            }
        }

        private int[] prepareIndexesToRefresh () {
            int index = tableIndex;
            int min = Math.max(0, index - 2);
            int max = Math.min(prepareSetups.length - 1, index + 2);
            int[] indexes = new int[max - min + 1];
            // adding tableIndex, tableIndex - 1, tableIndex + 1, tableIndex - 2, tableIndex + 2, etc.
            for (int i = index, j = index + 1, k = 0; i >= min || j <= max; --i, ++j) {
                if (i >= min) {
                    indexes[k++] = i;
                }
                if (j <= max) {
                    indexes[k++] = j;
                }
            }
            return indexes;
        }

        private void setTableIndex(int index) {
            this.tableIndex = index;
        }

        @Override
        public boolean cancel() {
            return this.canceled = true;
        }
    }

    private static class SetupsComparator implements Comparator<Setup> {

        private HgUtils.ByImportanceComparator delegate = new HgUtils.ByImportanceComparator();
        private FileStatusCache cache;

        public SetupsComparator() {
            cache = Mercurial.getInstance().getFileStatusCache();
        }

        @Override
        public int compare(Setup setup1, Setup setup2) {
            int cmp = delegate.compare(cache.getStatus(setup1.getBaseFile()), cache.getStatus(setup2.getBaseFile()));
            if (cmp == 0) {
                return setup1.getBaseFile().getName().compareToIgnoreCase(setup2.getBaseFile().getName());
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        controlsToolBar = new javax.swing.JToolBar();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        nextButton = new javax.swing.JButton();
        prevButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        commitButton = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();

        controlsToolBar.setFloatable(false);
        controlsToolBar.setRollover(true);

        jPanel4.setMaximumSize(new java.awt.Dimension(12, 32767));

        jPanel3.setMaximumSize(new java.awt.Dimension(12, 32767));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 12, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        controlsToolBar.add(jPanel4);

        jPanel1.setMaximumSize(new java.awt.Dimension(80, 32767));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 80, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel1);

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-next.png"))); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Next_Tooltip")); // NOI18N
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(nextButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/diff-prev.png"))); // NOI18N
        prevButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Prev_Tooltip")); // NOI18N
        prevButton.setFocusable(false);
        prevButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        prevButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(prevButton);

        jPanel2.setMaximumSize(new java.awt.Dimension(30, 32767));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel2);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/refresh.png"))); // NOI18N
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

        jPanel5.setMaximumSize(new java.awt.Dimension(20, 32767));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        controlsToolBar.add(jPanel5);

        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/mercurial/resources/icons/commit.png"))); // NOI18N
        commitButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "MSG_CommitDiff_Tooltip")); // NOI18N
        commitButton.setFocusable(false);
        commitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        commitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(commitButton);

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(controlsToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 716, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(controlsToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {                                              
        onRefreshButton();
    }                                             
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton commitButton;
    private javax.swing.JToolBar controlsToolBar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JButton nextButton;
    private javax.swing.JButton prevButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JSplitPane splitPane;
    // End of variables declaration//GEN-END:variables
    
    /** Interprets property blob. */
    static final class Property {
        final byte[] value;

        Property(Object value) {
            this.value = (byte[]) value;
        }

        String getMIME() {            
            return "text/plain"; // NOI18N
        }

        Reader toReader() {
            if (HgUtils.isBinary(value)) {
                return new StringReader(NbBundle.getMessage(MultiDiffPanel.class, "LBL_Diff_NoBinaryDiff"));  // hexa-flexa txt? // NOI18N
            } else {
                try {
                    return new InputStreamReader(new ByteArrayInputStream(value), "utf8");  // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    Mercurial.LOG.log(Level.SEVERE, "UnsupportedEncodingException {0}", ex);
                    return new StringReader("[ERROR: " + ex.getLocalizedMessage() + "]"); // NOI18N
                }
            }
        }
    }
}
