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

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.modules.versioning.util.DelegatingUndoRedo;
import org.netbeans.modules.versioning.util.VersioningEvent;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.client.PropertiesClient;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.commit.CommitAction;
import org.netbeans.modules.subversion.ui.status.StatusAction;
import org.netbeans.modules.subversion.ui.update.UpdateAction;
import org.netbeans.api.diff.DiffController;
import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.Lookup;
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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.modules.subversion.SvnFileNode;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.versioning.diff.DiffLookup;
import org.netbeans.modules.versioning.diff.DiffUtils;
import org.netbeans.modules.versioning.diff.EditorSaveCookie;
import org.netbeans.modules.versioning.diff.SaveBeforeClosingDiffConfirmation;
import org.netbeans.modules.versioning.diff.SaveBeforeCommitConfirmation;
import org.netbeans.modules.versioning.util.CollectionUtils;
import org.netbeans.modules.versioning.util.PlaceholderPanel;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import static org.netbeans.modules.versioning.util.CollectionUtils.copyArray;

/**
 *
 * @author Maros Sandor
 */
public class MultiDiffPanel extends javax.swing.JPanel implements ActionListener, VersioningListener, DiffSetupSource, PropertyChangeListener, PreferenceChangeListener {
    
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
    private final Context context;
    private final File diffedFile;

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

    private SvnProgressSupport executeStatusSupport;
    
    /**
     * Creates diff panel and immediatelly starts loading...
     */
    public MultiDiffPanel(Context context, int initialType, String contextName) {
        assert EventQueue.isDispatchThread();
        this.context = context;
        this.diffedFile = null;
        this.contextName = contextName;
        currentType = initialType;
        initComponents();
        initFileTable();
        initToolbarButtons();
        initNextPrevActions();
        diffViewPanel = null;
        refreshComponents();
        refreshTask = org.netbeans.modules.versioning.util.Utils.createTask(new RefreshViewTask());
        onRefreshButton();
    }

    /**
     * Construct diff component showing just one file.
     * It hides All, Local, Remote toggles and file chooser combo.
     */
    public MultiDiffPanel(File file, String rev1, String rev2, boolean forceNonEditable) {
        assert EventQueue.isDispatchThread();
        context = null;
        diffedFile = file;
        contextName = file.getName();
        initComponents();
        initToolbarButtons();
        initNextPrevActions();

        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);

        // mimics refreshSetups()
        setSetups(new Setup(file, rev1, rev2, forceNonEditable));
        setDiffIndex(0, 0);
        dpt = new DiffPrepareTask(setups);
        prepareTask = Subversion.getInstance().getParallelRequestProcessor().post(dpt);
    }

    /**
     * Diff component with a differences between local and remote changes in a single file
     * Commit and refresh buttons are hidden, update button enabled and visible.
     * @param file diffed file
     * @param status remote status of the file
     */
    public MultiDiffPanel(File file, ISVNStatus status) {
        assert EventQueue.isDispatchThread();
        context = null;
        diffedFile = file;
        contextName = file.getName();
        initComponents();
        initToolbarButtons();
        initNextPrevActions();

        diffViewPanel = new PlaceholderPanel();
        diffViewPanel.setComponent(getInfoPanelLoading());
        replaceVerticalSplitPane(diffViewPanel);

        refreshButton.setVisible(false);

        // mimics refreshSetups()
        setSetups(new Setup(file, status));
        setDiffIndex(0, 0);
        dpt = new DiffPrepareTask(setups);
        prepareTask = Subversion.getInstance().getParallelRequestProcessor().post(dpt);
    }

    private void replaceVerticalSplitPane(JComponent replacement) {
        removeAll();
        splitPane = null;
        setLayout(new BorderLayout());
        controlsToolBar.setPreferredSize(new Dimension(Short.MAX_VALUE, 25));
        add(controlsToolBar, BorderLayout.NORTH);
        add(replacement, BorderLayout.CENTER);
    }

    private void setSetups(Setup... setups) {
        this.setups = setups;
        this.editorCookies = (setups != null)
                             ? DiffUtils.setupsToEditorCookies(setups)
                             : null;
    }

    private boolean fileTableSetSelectedIndexContext;

    void tableRowSelected(int viewIndex) {
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
        setSetups((Setup[]) null);
        /**
         * must disable these actions, otherwise key shortcuts would trigger them even after tab closure
         * see #159266
         */
        prevAction.setEnabled(false);
        nextAction.setEnabled(false);
        cancelBackgroundTasks();
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

            commitButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Commit_Tooltip"));
            updateButton.setToolTipText(NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Update_Tooltip"));
            ButtonGroup grp = new ButtonGroup();
            grp.add(localToggle);
            grp.add(remoteToggle);
            grp.add(allToggle);
            if (currentType == Setup.DIFFTYPE_LOCAL) localToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_REMOTE) remoteToggle.setSelected(true);
            else if (currentType == Setup.DIFFTYPE_ALL) allToggle.setSelected(true);

            commitButton.setEnabled(false);
        } else {
            commitButton.setVisible(false);
            updateButton.setVisible(false);
            localToggle.setVisible(false);
            remoteToggle.setVisible(false);
            allToggle.setVisible(false);
            refreshButton.setVisible(false);
        }
    }

    private void initNextPrevActions() {
        nextAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-next.png"))) {  // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/diff/Bundle").
                                                   getString("CTL_DiffPanel_Next_Tooltip"));                
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                onNextButton();
            }
        };
        prevAction = new AbstractAction(null, new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-prev.png"))) { // NOI18N
            {
                putValue(Action.SHORT_DESCRIPTION, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/diff/Bundle").
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
        assert EventQueue.isDispatchThread();
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
            Subversion.getInstance().getStatusCache().addVersioningListener(this);
            SvnModuleConfig.getDefault().getPreferences().addPreferenceChangeListener(this);
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
        Subversion.getInstance().getStatusCache().removeVersioningListener(this);
        if (refreshTask != null) {
            SvnModuleConfig.getDefault().getPreferences().removePreferenceChangeListener(this);
        }
        super.removeNotify();
    }
    
    @Override
    public void versioningEvent(VersioningEvent event) {
        if (event.getId() == FileStatusCache.EVENT_FILE_STATUS_CHANGED) {
            if (!affectsView(event)) {
                return;
            }
            refreshTask.schedule(200);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == commitButton) onCommitButton();
        else if (source == localToggle || source == remoteToggle || source == allToggle) onDiffTypeChanged();
    }

    private void onRefreshButton() {
        if (context == null || context.getRoots().isEmpty()) {
            return;
        }

        if(executeStatusSupport!=null) {
            executeStatusSupport.cancel();
            executeStatusSupport = null;
        }
        
        LifecycleManager.getDefault().saveAll();
        RequestProcessor rp = Subversion.getInstance().getRequestProcessor();
        executeStatusSupport = new SvnProgressSupport() {
            @Override
            public void perform() {                                                
                StatusAction.executeStatus(context, this);
                if (!isCanceled()) {
                    refreshTask.schedule(50);
                }
            }
        };
        SVNUrl url;
        try {
            url = ContextAction.getSvnUrl(context); 
        } catch(SVNClientException ex)  {
            SvnClientExceptionHandler.notifyException(ex, true, true);     
            return;             
        }
        executeStatusSupport.start(rp, url, NbBundle.getMessage(MultiDiffPanel.class, "MSG_Refresh_Progress"));
    }                    

    private void onUpdateButton() {
        if (context != null) {
            UpdateAction.performUpdate(context, contextName);
        } else if (diffedFile != null) {
            UpdateAction.performUpdate(diffedFile);
        }
    }
    
    private void onCommitButton() {
        EditorCookie[] editorCookiesCopy = copyArray(editorCookies);
        DiffUtils.cleanThoseUnmodified(editorCookiesCopy);
        SaveCookie[] saveCookies = getSaveCookies(setups, editorCookiesCopy);

        if ((saveCookies.length == 0)
                || SaveBeforeCommitConfirmation.allSaved(saveCookies)) {
            CommitAction.commit(contextName, context, false);
        }
    }

    /** Next that is driven by visibility. It continues to next not yet visible difference. */
    private void onNextButton() {
        assert setups != null : "setups is null";                       //NOI18N
        assert setups[currentModelIndex] != null
                        : "setups[" + currentModelIndex + "] is null";  //NOI18N
        if ((setups == null) || (setups[currentModelIndex] == null)) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    nextButton.setEnabled(false);
                }
            });
            return;
        }

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
            } else if (currentDifferenceIndex < view.getDifferenceCount()) {
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

    private class SetupsPrepareSupport extends SvnProgressSupport {
        @Override
        protected void perform() {
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
            File[] files = SvnUtils.getModifiedFiles(context, status);
            if (isCanceled()) {
                return;
            }
            final int localDisplayStatuses = status;
            final Setup[] localSetups = computeSetups(files, status);
            if (localSetups == null) {
                return;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    displayStatuses = localDisplayStatuses;
                    setSetups(localSetups);
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
                                throw new IllegalStateException("Unknown DIFF type:" + currentType); // NOI18N
                        }
                        setSetups((Setup[]) null);
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
                    } else {
                        fileTable.getComponent().setEnabled(true);
                        fileTable.getComponent().setPreferredSize(null);
                        Dimension dim = fileTable.getComponent().getPreferredSize();
                        fileTable.getComponent().setPreferredSize(new Dimension(dim.width + 1, dim.height));
                        setDiffIndex(0, 0);
                        commitButton.setEnabled(true);
                        dpt = new DiffPrepareTask(setups);
                        prepareTask = Subversion.getInstance().getParallelRequestProcessor().post(dpt);
                    }
                }
            };
            if (EventQueue.isDispatchThread()) {
                runnable.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(runnable);
                } catch (InterruptedException ex) {
                    Subversion.LOG.log(Level.FINE, null, ex);
                } catch (InvocationTargetException ex) {
                    Subversion.LOG.log(Level.FINE, null, ex);
                }
            }
        }

        private Setup[] computeSetups(File[] files, int displayStatus) {
            List<Setup> newSetups = new ArrayList<Setup>(files.length);
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (!file.isDirectory()) {
                    Setup setup = new Setup(file, null, currentType);
                    setup.setNode(new DiffNode(setup, new SvnFileNode(file), displayStatus));
                    newSetups.add(setup);
                }
                addPropertiesSetups(file, newSetups, displayStatus);
                if (isCanceled()) {
                    return null;
                }
            }
            Collections.sort(newSetups, new SetupsComparator());
            return newSetups.toArray(new Setup[newSetups.size()]);
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey().startsWith(SvnModuleConfig.PROP_COMMIT_EXCLUSIONS)) {
            repaint();
        }
    }

    private void addPropertiesSetups(File base, List<Setup> newSetups, int displayStatus) {
        if (currentType == Setup.DIFFTYPE_REMOTE) return;

        DiffProvider diffAlgorithm = (DiffProvider) Lookup.getDefault().lookup(DiffProvider.class);
        PropertiesClient client = new PropertiesClient(base);
        try {
            Map<String, byte[]> baseProps = client.getBaseProperties();
            Map<String, byte[]> localProps = client.getProperties();

            Set<String> allProps = new TreeSet<String>(localProps.keySet());
            allProps.addAll(baseProps.keySet());
            for (String key : allProps) {
                boolean isBase = baseProps.containsKey(key);
                boolean isLocal = localProps.containsKey(key);
                boolean propertiesDiffer = true;
                if (isBase && isLocal) {
                    Property p1 = new Property(baseProps.get(key));
                    Property p2 = new Property(localProps.get(key));
                    Difference[] diffs = diffAlgorithm.computeDiff(p1.toReader(), p2.toReader());
                    propertiesDiffer = (diffs.length != 0);
                }
                if (propertiesDiffer) {
                    Setup setup = new Setup(base, key, currentType);
                    setup.setNode(new DiffNode(setup, new SvnFileNode(base), displayStatus));
                    newSetups.add(setup);
                }
            }
        } catch (IOException e) {
            // no need to litter log with expected exceptions:
            // when parent is not versioned, the exception will allways be thrown
            FileInformation parentInfo = Subversion.getInstance().getStatusCache().getCachedStatus(base.getParentFile());
            Level logLevel = parentInfo != null && (parentInfo.getStatus() & FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY) != 0
                    ? Level.FINE : Level.INFO;
            Subversion.LOG.log(logLevel, null, e);
        }
    }

    private void onDiffTypeChanged() {
        assert EventQueue.isDispatchThread();
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
        SvnModuleConfig.getDefault().setLastUsedModificationContext(currentType);
        refreshTask.schedule(0);
    }

    @Override
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

        @Override
        public void run() {
            IOException exception = null;
            for (int i = 0; i < prepareSetups.length; i++) {
                if (prepareSetups != setups) return;
                try {
                    prepareSetups[i].initSources();  // slow network I/O
                    final int fi = i;
                    StreamSource ss1 = prepareSetups[fi].getFirstSource();
                    StreamSource ss2 = prepareSetups[fi].getSecondSource();
                    final DiffController view = DiffController.createEnhanced(ss1, ss2);  // possibly executing slow external diff
                    view.addPropertyChangeListener(MultiDiffPanel.this);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
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
                    Subversion.LOG.log(Level.INFO, null, e);
                    if (exception == null) {
                        // save only the first exception
                        exception = e;
                    }
                }
            }
            if (exception != null) {
                // notify user of the failure
                final IOException e = exception;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        SvnClientExceptionHandler.notifyException(e, true, true);
                    }
                });
            }
        }
    }

    private static class SetupsComparator implements Comparator<Setup> {

        private SvnUtils.ByImportanceComparator delegate = new SvnUtils.ByImportanceComparator();
        private FileStatusCache cache;

        public SetupsComparator() {
            cache = Subversion.getInstance().getStatusCache();
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
            new SetupsPrepareSupport().start(Subversion.getInstance().getRequestProcessor(), null,
                    NbBundle.getMessage(MultiDiffPanel.class, "MSG_PrepareSetups_Progress")).waitFinished(); //NOI18N
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

        allToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/remote_vs_local.png"))); // NOI18N
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

        localToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/local_vs_local.png"))); // NOI18N
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

        remoteToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/remote_vs_remote.png"))); // NOI18N
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

        nextButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-next.png"))); // NOI18N
        nextButton.setToolTipText(org.openide.util.NbBundle.getMessage(MultiDiffPanel.class, "CTL_DiffPanel_Next_Tooltip")); // NOI18N
        nextButton.setFocusable(false);
        nextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        nextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        controlsToolBar.add(nextButton);

        prevButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/diff-prev.png"))); // NOI18N
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

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/refresh.png"))); // NOI18N
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

        updateButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/update.png"))); // NOI18N
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

        commitButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/resources/icons/commit.png"))); // NOI18N
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
            if (SvnUtils.isBinary(value)) {
                return new StringReader(NbBundle.getMessage(MultiDiffPanel.class, "LBL_Diff_NoBinaryDiff"));  // hexa-flexa txt? // NOI18N
            } else {
                try {
                    return new InputStreamReader(new ByteArrayInputStream(value), "utf8");  // NOI18N
                } catch (UnsupportedEncodingException ex) {
                    Subversion.LOG.log(Level.SEVERE, null, ex);
                    return new StringReader("[ERROR: " + ex.getLocalizedMessage() + "]"); // NOI18N
                }
            }
        }
    }
}
