/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.diff;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.libs.git.GitClient.DiffMode;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.checkout.RevertChangesAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.commit.GitFileNode;
import org.netbeans.modules.versioning.util.status.VCSStatusTableModel;
import org.netbeans.modules.git.ui.status.StatusAction;
import org.netbeans.modules.versioning.util.status.VCSStatusTable;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.diff.EditorSaveCookie;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Cancellable;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 *
 * @author ondra
 */
public class MultiDiffPanelController implements ActionListener, PropertyChangeListener, PreferenceChangeListener {
    private final VCSContext context;
    private EnumSet<Status> displayStatuses;
    private final DelegatingUndoRedo delegatingUndoRedo = new DelegatingUndoRedo();
    private Mode mode;
    private final MultiDiffPanel panel;
    private AbstractAction nextAction;
    private AbstractAction prevAction;

    /**
     * panel that is used for displaying the diff if {@code JSplitPane}
     * is not used
     */
    private final PlaceholderPanel diffViewPanel;
    private JComponent infoPanelLoadingFromRepo;
    static final Logger LOG = Logger.getLogger(MultiDiffPanelController.class.getName());
    private DiffFileTable fileTable;
    private static final RequestProcessor RP = new RequestProcessor("GitDiffWindow", 1, true); //NOI18N
    private RequestProcessor.Task refreshNodesTask = RP.create(new RefreshNodesTask());
    private final ApplyChangesTask applyChangeTask = new ApplyChangesTask();
    private RequestProcessor.Task changeTask = RP.create(applyChangeTask);

    private boolean dividerSet;

    // TODO Merge with GitVersioningPanelController

    /**
     * DIFF setups that we show in the DIFF view. Contents is changed when the user switches DIFF types or a file under the context changes.
     */
    private final Map<File, Setup> setups = new HashMap<File, Setup>();
    /**
     * editor cookies belonging to the files being diffed.
     * The array may contain {@code null}s if {@code EditorCookie}s
     * for the corresponding files were not found.
     *
     * @see  #nodes
     */
    private final Map<File, EditorCookie> editorCookies = new HashMap<File, EditorCookie>();
    private JComponent diffView;

    private RequestProcessor.Task prepareTask;
    private DiffPrepareTask dpt;

    private File currentFile;
    private boolean fileTableSetSelectedIndexContext;
    private final DiffLookup lookup = new DiffLookup();

    private GitProgressSupport statusRefreshSupport;
    private PreferenceChangeListener prefList;

    public MultiDiffPanelController (VCSContext context) {
        this.context = context;
        panel = new MultiDiffPanel();
        diffViewPanel = null;
        initFileTable();
        initToolbarButtons();
        initNextPrevActions();
        initPanelMode();
        attachListeners();
        refreshComponents();
    }

    public MultiDiffPanelController (File file) {
        this.context = null;
        panel = new MultiDiffPanel();
        this.currentFile = file;
        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);
        initToolbarButtons();
        initNextPrevActions();
        initPanelMode();
        attachListeners();
        refreshComponents();
    }

    public MultiDiffPanelController (File file, String rev1, String rev2) {
        context = null;
        this.currentFile = file;
        panel = new MultiDiffPanel();
        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);
        initToolbarButtons();
        initNextPrevActions();
        for (JComponent c : new JComponent[] { panel.tgbHeadVsIndex, panel.tgbHeadVsWorking, panel.tgbIndexVsWorking }) {
            c.setVisible(false);
        }
        // mimics refreshSetups()
        Map<File, Setup> localSetups = Collections.singletonMap(currentFile, new Setup(file, rev1, rev2));
        setSetups(localSetups, Collections.<File, EditorCookie>emptyMap());
        setDiffIndex(file, 0, false);
        dpt = new DiffPrepareTask(setups.values().toArray(new Setup[setups.size()]));
        prepareTask = Utils.createTask(dpt);
        prepareTask.schedule(0);
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        panel.removeAll();
        panel.setLayout(new BorderLayout());
        panel.add(panel.controlToolbar, BorderLayout.NORTH);
        panel.add(replacement, BorderLayout.CENTER);
    }

    void setActions (JComponent comp) {
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N
        comp.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N

        panel.getActionMap().put("prevInnerView", new AbstractAction("") { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextInnerView();
            }
        });
        panel.getActionMap().put("nextInnerView", new AbstractAction("") { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                onPrevInnerView();
            }
        });
    }

    public JPanel getPanel () {
        return panel;
    }

    PropertyChangeListener list;
    private void attachListeners() {
        panel.tgbHeadVsWorking.addActionListener(this);
        panel.tgbHeadVsIndex.addActionListener(this);
        panel.tgbIndexVsWorking.addActionListener(this);
        panel.btnCommit.addActionListener(this);
        panel.btnRevert.addActionListener(this);
        panel.btnRefresh.addActionListener(this);
        Git.getInstance().getFileStatusCache().addPropertyChangeListener(list = WeakListeners.propertyChange(this, Git.getInstance().getFileStatusCache()));
        GitModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(
                prefList = WeakListeners.create(PreferenceChangeListener.class, this, GitModuleConfig.getDefault().getPreferences()));
    }

    Lookup getLookup () {
        return lookup;
    }

    boolean canClose() {
        if (setups.isEmpty()) {
            return true;
        }
        SaveCookie[] saveCookies = getSaveCookies(true);
        return (saveCookies.length == 0) || SaveBeforeClosingDiffConfirmation.allSaved(saveCookies);
    }

    // <editor-fold defaultstate="collapsed" desc="save cookie support">
    public SaveCookie[] getSaveCookies(boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = getEditorCookiesIntern(ommitOpened);
        SaveCookie[] saveCookies = getSaveCookies(editorCookiesCopy);
        return saveCookies;
    }

    public EditorCookie[] getEditorCookies(boolean ommitOpened) {
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

    private EditorCookie[] getEditorCookiesIntern(boolean ommitOpened) {
        EditorCookie[] editorCookiesCopy = editorCookies.values().toArray(new EditorCookie[editorCookies.values().size()]);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        if (ommitOpened) {
            DiffUtils.cleanThoseWithEditorPaneOpen(editorCookiesCopy);
        }
        return editorCookiesCopy;
    }

    private SaveCookie[] getSaveCookies(EditorCookie[] editorCookies) {
        List<SaveCookie> proResult = new LinkedList<SaveCookie>();
        Set<EditorCookie> editorCookieSet = new HashSet<EditorCookie>(Arrays.asList(editorCookies));
        for (Map.Entry<File, EditorCookie> e : this.editorCookies.entrySet()) {
            if (editorCookieSet.contains(e.getValue())) {
                File baseFile = e.getKey();
                FileObject fileObj = FileUtil.toFileObject(baseFile);
                if (fileObj == null) {
                    continue;
                }
                proResult.add(new EditorSaveCookie(e.getValue(), fileObj.getNameExt()));
            }
        }
        return proResult.toArray(new SaveCookie[proResult.size()]);
    }// </editor-fold>

    UndoRedo getUndoRedo () {
        return delegatingUndoRedo;
    }

    public void componentClosed () {
        setSetups(Collections.<File, Setup>emptyMap(), Collections.<File, EditorCookie>emptyMap());
        prevAction.setEnabled(false);
        nextAction.setEnabled(false);
        cancelBackgroundTasks();
        setups.clear();
        editorCookies.clear();
        
        if (list != null) {
            Git.getInstance().getFileStatusCache().removePropertyChangeListener(list);
        }
        if (prefList != null) {
            GitModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(prefList);
        }
    }

    private void cancelBackgroundTasks() {
        if (prepareTask != null) {
            prepareTask.cancel();
        }
        GitProgressSupport supp = statusRefreshSupport;
        if(supp != null) {
            supp.cancel();
        }
    }

    void focus () {
        if (fileTable != null) {
            fileTable.focus();
        }
    }

    private void displayDiffView() {
        if (context != null) {
            int gg = panel.splitPane.getDividerLocation();
            panel.splitPane.setBottomComponent(diffView);
            panel.splitPane.setDividerLocation(gg);
        } else {
            diffViewPanel.setComponent(diffView);
        }
    }

    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/diff-next.png"))) {  //NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.nextButton.toolTipText")); //NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/git/resources/icons/diff-prev.png"))) { //NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(MultiDiffPanel.class, "MultiDiffPanel.prevButton.toolTipText")); //NOI18N
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onPrevButton();
            }
        };
        panel.nextButton.setAction(nextAction);
        panel.prevButton.setAction(prevAction);
    }

    private void initFileTable () {
        fileTable = new DiffFileTable(new VCSStatusTableModel<DiffNode>(new DiffNode[0]));
        fileTable.addPropertyChangeListener(this);
        panel.splitPane.setTopComponent(fileTable.getComponent());
        panel.splitPane.setBottomComponent(getInfoPanelLoading());
    }

    private void initToolbarButtons () {
        if (context != null) {
            panel.btnCommit.setEnabled(false);
            panel.btnRevert.setEnabled(false);
        } else {
            panel.btnCommit.setVisible(false);
            panel.btnRevert.setVisible(false);
            panel.btnRefresh.setVisible(false);
        }
    }
    
    private JComponent getInfoPanelLoading () {
        if (infoPanelLoadingFromRepo == null) {
            infoPanelLoadingFromRepo = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
        }
        return infoPanelLoadingFromRepo;
    }

    private void refreshComponents () {
        Setup setup = setups.get(currentFile);
        DiffController view = setup == null ? null : setup.getView();
        int currentDifferenceIndex = view != null ? view.getDifferenceIndex() : -1;
        if (view != null) {
            nextAction.setEnabled(currentDifferenceIndex < view.getDifferenceCount() - 1 || fileTable != null && fileTable.getNextFile(currentFile) != null);
        } else {
            nextAction.setEnabled(false);
        }
        prevAction.setEnabled(currentDifferenceIndex > 0 || fileTable != null && fileTable.getPrevFile(currentFile) != null);
    }

    private void onPrevInnerView() {
        if (panel.tgbHeadVsWorking.isSelected()) {
            panel.tgbHeadVsIndex.setSelected(true);
        } else if (panel.tgbHeadVsIndex.isSelected()) {
            panel.tgbIndexVsWorking.setSelected(true);
        } else {
            panel.tgbHeadVsWorking.setSelected(true);
        }
        onDisplayedStatusChanged();
    }

    private void onNextInnerView() {
        if (panel.tgbHeadVsWorking.isSelected()) {
            panel.tgbIndexVsWorking.setSelected(true);
        } else if (panel.tgbIndexVsWorking.isSelected()) {
            panel.tgbHeadVsIndex.setSelected(true);
        } else {
            panel.tgbHeadVsWorking.setSelected(true);
        }
        onDisplayedStatusChanged();
    }

    private void onDisplayedStatusChanged () {
        if (panel.tgbHeadVsWorking.isSelected()) {
            mode = Mode.HEAD_VS_WORKING_TREE;
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING);
            if (context != null) GitModuleConfig.getDefault().setLastUsedModificationContext(mode);
            if (fileTable != null) fileTable.setSelectedMode(DiffMode.HEAD_VS_WORKINGTREE);
        } else if (panel.tgbHeadVsIndex.isSelected()) {
            mode = Mode.HEAD_VS_INDEX;
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX);
            if (context != null) GitModuleConfig.getDefault().setLastUsedModificationContext(mode);
            if (fileTable != null) fileTable.setSelectedMode(DiffMode.HEAD_VS_INDEX);
        } else {
            mode = Mode.INDEX_VS_WORKING_TREE;
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_INDEX_VS_WORKING);
            if (context != null) GitModuleConfig.getDefault().setLastUsedModificationContext(mode);
            if (fileTable != null) fileTable.setSelectedMode(DiffMode.INDEX_VS_WORKINGTREE);
        }
    }

    private void setDisplayStatuses (EnumSet<Status> displayStatuses) {
        this.displayStatuses = displayStatuses;
        if (context != null) {
            refreshNodes();
        } else {
            Map<File, Setup> localSetups = Collections.singletonMap(currentFile, new Setup(currentFile, mode, false));
            Map<File, EditorCookie> localCookies = getCookiesFromSetups(localSetups);
            setSetups(localSetups, localCookies);
            dpt = new DiffPrepareTask(setups.values().toArray(new Setup[setups.size()]));
            prepareTask = Utils.createTask(dpt);
            prepareTask.schedule(0);
        }
    }

    private void setSetups (Map<File, Setup> setups, Map<File, EditorCookie> editorCookies) {
        for (Map.Entry<File, Setup> e : this.setups.entrySet()) {
            Setup setup = e.getValue();
            if (setup != null) {
                setup.getFirstSource().close();
                setup.getSecondSource().close();
            }
        }
        this.setups.clear();
        this.setups.putAll(setups);
        this.editorCookies.clear();
        this.editorCookies.putAll(editorCookies);
    }

    private void onNextButton() {
//        if (showingFileTable()) {
//            currentIndex = fileTable.getSelectedIndex();
//            currentModelIndex = fileTable.getSelectedModelIndex();
//        }
        DiffController view = setups.get(currentFile).getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (++currentDifferenceIndex >= view.getDifferenceCount()) { // also passes for view.getDifferenceCount() == 0
                File nextFile = fileTable.getNextFile(currentFile);
                if (nextFile != null) {
                    setDiffIndex(nextFile, 0, true);
                }
            } else {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            assert false : "Whats this?";
//            if (++currentIndex >= setups.length) currentIndex = 0;
//            setDiffIndex(currentIndex, 0, true);
        }
        refreshComponents();
    }

    private void onPrevButton() {
        DiffController view = setups.get(currentFile).getView();
        if (view != null) {
            int currentDifferenceIndex = view.getDifferenceIndex();
            if (--currentDifferenceIndex < 0) {
                File prevFile = fileTable.getPrevFile(currentFile);
                if (prevFile != null) {
                    setDiffIndex(prevFile, -1, true);
                }
            } else if (currentDifferenceIndex < view.getDifferenceCount()) {
                view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, currentDifferenceIndex);
            }
        } else {
            assert false : "Whats this?";
//            if (--currentIndex < 0) currentIndex = setups.length - 1;
//            setDiffIndex(currentIndex, -1, true);
        }
        refreshComponents();
    }

    private void setDiffIndex (File file, int location, boolean restartPrepareTask) {
        currentFile = file;
        Setup setup = setups.get(currentFile);
        DiffController view = null;

        if (setup != null) {
            if (restartPrepareTask) {
                if (dpt != null) {
                    dpt.cancel();
                }
                startPrepareTask();
            }
            view = setup.getView();

            // enable Select in .. action
            FileObject fileObj = FileUtil.toFileObject(currentFile);
            EditorCookie.Observable observableEditorCookie = null;
            TopComponent tc = (TopComponent) panel.getClientProperty(TopComponent.class);
            if (tc != null) {
                Node node = setup.getNode();
                tc.setActivatedNodes(new Node[] {node == null ? Node.EMPTY : node});
            }
            EditorCookie editorCookie = editorCookies.get(currentFile);
            if (editorCookie instanceof EditorCookie.Observable) {
                observableEditorCookie = (EditorCookie.Observable) editorCookie;
            }

            diffView = null;
            if (view != null) {
                if (fileTable != null) {
                    fileTableSetSelectedIndexContext = true;
                    fileTable.setSelectedNodes(new File[] { currentFile });
                    fileTableSetSelectedIndexContext = false;
                }
                diffView = view.getJComponent();
                diffView.getActionMap().put("jumpNext", nextAction);  // NOI18N
                diffView.getActionMap().put("jumpPrev", prevAction);  // NOI18N
                displayDiffView();
                if (location == -1) {
                    location = view.getDifferenceCount() - 1;
                }
                if (location >= 0 && location < view.getDifferenceCount()) {
                    view.setLocation(DiffController.DiffPane.Modified, DiffController.LocationType.DifferenceIndex, location);
                }
            } else {
                diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoContent"));
                displayDiffView();
            }
            lookup.setData(fileObj, observableEditorCookie, diffView.getActionMap());
        } else {
            lookup.setData();
            diffView = new NoContentPanel(NbBundle.getMessage(MultiDiffPanel.class, "MSG_DiffPanel_NoFileSelected"));
            lookup.setData(diffView.getActionMap());
            displayDiffView();
        }

        delegatingUndoRedo.setDiffView(diffView);

        refreshComponents();
    }

    @Override
    public void actionPerformed (final ActionEvent e) {
        if (e.getSource() == panel.tgbHeadVsIndex || e.getSource() == panel.tgbHeadVsWorking
                || e.getSource() == panel.tgbIndexVsWorking) {
            onDisplayedStatusChanged();
        } else {
            Utils.postParallel(new Runnable() {
                @Override
                public void run() {
                    if (e.getSource() == panel.btnRevert) {
                        SystemAction.get(RevertChangesAction.class).performAction(context);
                    } else if (e.getSource() == panel.btnCommit) {
                        SystemAction.get(CommitAction.GitViewCommitAction.class).performAction(context);
                    } else if (e.getSource() == panel.btnRefresh) {
                        statusRefreshSupport = SystemAction.get(StatusAction.class).scanStatus(context);
                        if (statusRefreshSupport != null) {
                            statusRefreshSupport.getTask().waitFinished();
                            if (!statusRefreshSupport.isCanceled()) {
                                refreshNodes();
                            }
                        }
                    }
                }
            }, 0);
        }
    }

    private void applyChange (FileStatusCache.ChangedEvent event) {
        if (context != null) {
            LOG.log(Level.FINE, "Planning refresh for {0}", event.getFile());
            synchronized (changes) {
                changes.put(event.getFile(), event);
            }
            changeTask.schedule(1000);
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (FileStatusCache.PROP_FILE_STATUS_CHANGED.equals(evt.getPropertyName())) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) evt.getNewValue();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "File status for file {0} changed from {1} to {2}", new Object[] { 
                    changedEvent.getFile(), 
                    changedEvent.getOldInfo(),
                    changedEvent.getNewInfo() } );
            }
            if (affectsView(changedEvent)) {
                applyChange(changedEvent);
            }
        } else if (DiffController.PROP_DIFFERENCES.equals(evt.getPropertyName())) {
            refreshComponents();
        } else if (VCSStatusTable.PROP_SELECTED_FILES.equals(evt.getPropertyName())) {
            tableRowSelected((File[]) evt.getNewValue());
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(GitModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            panel.repaint();
        }
    }

    private boolean affectsView (FileStatusCache.ChangedEvent changedEvent) {
        File file = changedEvent.getFile();
        FileInformation oldInfo = changedEvent.getOldInfo();
        FileInformation newInfo = changedEvent.getNewInfo();
        if (oldInfo == null) {
            if (!newInfo.containsStatus(displayStatuses)) return false;
        } else {
            if (!oldInfo.containsStatus(displayStatuses) && !newInfo.containsStatus(displayStatuses)) return false;
        }
        return context == null ? false: context.contains(file);
    }

    private void initPanelMode () {
        mode = GitModuleConfig.getDefault().getLastUsedModificationContext();
        panel.tgbHeadVsWorking.setSelected(true);
        switch (mode) {
            case HEAD_VS_WORKING_TREE:
                panel.tgbHeadVsWorking.setSelected(true);
                break;
            case HEAD_VS_INDEX:
                panel.tgbHeadVsIndex.setSelected(true);
                break;
            case INDEX_VS_WORKING_TREE:
                panel.tgbIndexVsWorking.setSelected(true);
                break;
        }
        onDisplayedStatusChanged();
    }

    private void updateSplitLocation () {
        JComponent parent = (JComponent) panel.getParent();
        Dimension dim = parent == null ? new Dimension() : parent.getSize();
        if (dim.width <= 0 || dim.height <= 0) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateSplitLocation();
                }
            });
            return;
        }
        JTable jt = fileTable.getDiffTable();
        int optimalLocation = jt.getPreferredSize().height + jt.getTableHeader().getPreferredSize().height;
        if (optimalLocation > dim.height / 3) {
            optimalLocation = dim.height / 3;
        }
        if (optimalLocation <= jt.getTableHeader().getPreferredSize().height) {
            optimalLocation = jt.getTableHeader().getPreferredSize().height * 3;
        }
        if (dividerSet && panel.splitPane.getDividerLocation() <= optimalLocation) return;
        panel.splitPane.setDividerLocation(optimalLocation);
        dividerSet = true;
    }

    public void tableRowSelected (File[] selectedFiles) {
        if (fileTableSetSelectedIndexContext) return;
        setDiffIndex(selectedFiles.length == 1 ? selectedFiles[0] : null, 0, true);
    }

    // <editor-fold defaultstate="collapsed" desc="refreshing tasks">
    private void refreshNodes () {
        if (context != null) {
            refreshNodesTask.cancel();
            refreshNodesTask.schedule(0);
        }
    }

    private void startPrepareTask() {
        Setup[] toInitialize = getSetupsToRefresh();
        if (toInitialize.length > 0) {
            dpt = new DiffPrepareTask(toInitialize);
            prepareTask = Utils.createTask(dpt);
            prepareTask.schedule(0);
        }
    }

    private Setup[] getSetupsToRefresh () {
        List<Setup> toRefresh = new LinkedList<Setup>(Collections.singletonList(setups.get(currentFile)));
        // get neighbouring setups
        for (int i = 1; i <= 2; ++i) {
            for (int sign : new int[] { 1, -1 }) {
                File file = fileTable.getNeighbouringFile(currentFile, sign * i);
                if (file != null) {
                    Setup s = setups.get(file);
                    if (s != null) {
                        toRefresh.add(s);
                    }
                }
            }
        }
        return toRefresh.toArray(new Setup[toRefresh.size()]);
    }

    private static Map<File, EditorCookie> getCookiesFromSetups(Map<File, Setup> localSetups) {
        Setup[] retSetups = localSetups.values().toArray(new Setup[localSetups.values().size()]);
        EditorCookie[] cookies = DiffUtils.setupsToEditorCookies(retSetups);
        Map<File, EditorCookie> map = new HashMap<File, EditorCookie>();
        for (int i = 0; i < cookies.length; ++i) {
            if (cookies[i] != null) {
                map.put(retSetups[i].getBaseFile(), cookies[i]);
            }
        }
        return map;
    }

    private class RefreshViewTask {

        protected void updateView() {
            if (setups.isEmpty()) {
                String noContentLabel = ""; //NOI18N
                switch (mode) {
                    case HEAD_VS_WORKING_TREE:
                        noContentLabel = NbBundle.getMessage(MultiDiffPanelController.class, "MSG_No_Changes_HeadWorking"); //NOI18N
                        break;
                    case HEAD_VS_INDEX:
                        noContentLabel = NbBundle.getMessage(MultiDiffPanelController.class, "MSG_No_Changes_HeadIndex"); //NOI18N
                        break;
                    case INDEX_VS_WORKING_TREE:
                        noContentLabel = NbBundle.getMessage(MultiDiffPanelController.class, "MSG_No_Changes_IndexWorking"); //NOI18N
                        break;
                }
                fileTable.getComponent().setEnabled(false);
                fileTable.getComponent().setPreferredSize(null);
                Dimension dim = fileTable.getComponent().getPreferredSize();
                fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                diffView = new NoContentPanel(noContentLabel);
                displayDiffView();
                nextAction.setEnabled(false);
                prevAction.setEnabled(false);
                panel.btnCommit.setEnabled(false);
                panel.btnRevert.setEnabled(false);
            } else {
                fileTable.getComponent().setEnabled(true);
                fileTable.getComponent().setPreferredSize(null);
                Dimension dim = fileTable.getComponent().getPreferredSize();
                fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                panel.btnCommit.setEnabled(true);
                panel.btnRevert.setEnabled(true);
            }
            if (panel.splitPane != null) {
                updateSplitLocation();
            }
            panel.revalidate();
            panel.repaint();
        }

        protected Map<File, Setup> computeSetups(List<DiffNode> nodes) {
            Map<File, Setup> newSetups = new HashMap<File, Setup>(nodes.size());
            for (DiffNode node : nodes) {
                newSetups.put(node.getFile(), new Setup(node, mode));
            }
            return newSetups;
        }
    }

    private final class RefreshNodesTask extends RefreshViewTask implements Runnable {

        @Override
        public void run() {
            final List<DiffNode> nodes = new LinkedList<DiffNode>();
            Git git = Git.getInstance();
            File[] interestingFiles = git.getFileStatusCache().listFiles(context.getRootFiles(), displayStatuses);
            for (File f : interestingFiles) {
                File root = git.getRepositoryRoot(f);
                if (root != null) {
                    nodes.add(new DiffNode(new GitFileNode(root, f), mode));
                }
            }
            final Map<File, Setup> localSetups = computeSetups(nodes);
            final Map<File, EditorCookie> cookies = getCookiesFromSetups(localSetups);
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    dividerSet = false;
                    setSetups(localSetups, cookies);
                    fileTable.setNodes(new HashMap<File, EditorCookie>(cookies), nodes.toArray(new DiffNode[nodes.size()]));
                    updateView();
                }
            });
        }
    }

    private final Map<File, FileStatusCache.ChangedEvent> changes = new HashMap<File, FileStatusCache.ChangedEvent>();
    /**
     * Eliminates unnecessary cache.listFiles call as well as the whole node creation process ()
     */
    private final class ApplyChangesTask extends RefreshViewTask implements Runnable {

        @Override
        public void run() {
            final Set<FileStatusCache.ChangedEvent> events;
            synchronized (changes) {
                events = new HashSet<FileStatusCache.ChangedEvent>(changes.values());
                changes.clear();
            }
            // remove irrelevant changes
            for (Iterator<FileStatusCache.ChangedEvent> it = events.iterator(); it.hasNext();) {
                FileStatusCache.ChangedEvent evt = it.next();
                if (!affectsView(evt)) {
                    LOG.log(Level.FINE, "ApplyChanges: file {0} does not affect view", evt.getFile());
                }
            }
            Git git = Git.getInstance();
            Map<File, DiffNode> nodes = Mutex.EVENT.readAccess(new Mutex.Action<Map<File, DiffNode>>() {
                @Override
                public Map<File, DiffNode> run() {
                    return fileTable.getNodes();
                }
            });
            // sort changes
            final List<DiffNode> toRemove = new LinkedList<DiffNode>();
            final List<DiffNode> toRefresh = new LinkedList<DiffNode>();
            final List<DiffNode> toAdd = new LinkedList<DiffNode>();
            for (FileStatusCache.ChangedEvent evt : events) {
                FileInformation newInfo = evt.getNewInfo();
                DiffNode node = nodes.get(evt.getFile());
                if (newInfo.containsStatus(displayStatuses)) {
                    if (node != null) {
                        toRefresh.add(node);
                        LOG.log(Level.FINE, "ApplyChanges: refreshing node {0}", node);
                    } else {
                        File root = git.getRepositoryRoot(evt.getFile());
                        if (root != null) {
                            DiffNode toAddNode = new DiffNode(new GitFileNode(root, evt.getFile()), mode);
                            toAdd.add(toAddNode);
                            LOG.log(Level.FINE, "ApplyChanges: adding node {0}", toAddNode);
                        }
                    }
                } else if (node != null) {
                    toRemove.add(node);
                    LOG.log(Level.FINE, "ApplyChanges: removing node {0}", node);
                }
            }

            // new setups and editor cookies
            final Map<File, Setup> localSetups = computeSetups(toAdd);
            localSetups.putAll(computeSetups(toRefresh));
            final Map<File, EditorCookie> cookies = getCookiesFromSetups(localSetups);
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    for (DiffNode node : toAdd) {
                        setups.put(node.getFile(), localSetups.get(node.getFile()));
                        EditorCookie cookie = cookies.get(node.getFile());
                        if(cookie != null) {
                            editorCookies.put(node.getFile(), cookie);
                        }
                    }
                    for (DiffNode node : toRemove) {
                        setups.remove(node.getFile());
                        editorCookies.remove(node.getFile());
                    }
                    fileTable.updateNodes(new HashMap<File, EditorCookie>(MultiDiffPanelController.this.editorCookies), toRemove, toRefresh, toAdd);
                    updateView();
                }
            });
        }
    }

    private class DiffPrepareTask implements Runnable, Cancellable {

        private final Setup[] prepareSetups;
        private boolean canceled;

        public DiffPrepareTask(Setup[] prepareSetups) {
            assert EventQueue.isDispatchThread();
            assert !Arrays.asList(prepareSetups).contains(null);
            this.prepareSetups = prepareSetups;
        }

        @Override
        public void run() {
            canceled = false;
            IOException exception = null;
            for (final Setup setup : prepareSetups) {
                if (setup.getView() == null) {
                    if (Thread.interrupted() || canceled) {
                        return;
                    }
                    try {
                        setup.initSources();  // slow network I/O
                        if (Thread.interrupted() || canceled) {
                            return;
                        }
                        StreamSource ss1 = setup.getFirstSource();
                        StreamSource ss2 = setup.getSecondSource();
                        final DiffController view = DiffController.createEnhanced(ss1, ss2);  // possibly executing slow external diff
                        view.addPropertyChangeListener(MultiDiffPanelController.this);
                        if (Thread.interrupted() || canceled) {
                            return;
                        }
                        setup.setView(view);
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                if (setup.getBaseFile().equals(currentFile)) {
                                    setDiffIndex(setup.getBaseFile(), 0, false);
                                }
                            }
                        });
                    } catch (IOException e) {
                        if (!GitClientExceptionHandler.isCancelledAction(e)) {
                            LOG.log(Level.INFO, null, e);
                            if (exception == null) {
                                // save only the first exception
                                exception = e;
                            }
                        }
                    }
                }
            }
            if (exception != null) {
                // notify user of the failure
                GitClientExceptionHandler.notifyException(exception, true);
            }
        }

        @Override
        public boolean cancel() {
            return this.canceled = true;
        }
    }// </editor-fold>
}
