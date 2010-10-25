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

package org.netbeans.modules.git.ui.status;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import org.netbeans.modules.git.FileInformation;
import org.netbeans.modules.git.FileInformation.Status;
import org.netbeans.modules.git.FileStatusCache;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.checkout.CheckoutPathsAction;
import org.netbeans.modules.git.ui.commit.CommitAction;
import org.netbeans.modules.git.ui.commit.GitFileNode;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.NoContentPanel;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author ondra
 */
class VersioningPanelController implements ActionListener, PropertyChangeListener {

    private final GitVersioningTopComponent tc;
    private final VersioningPanel panel;
    private VCSContext context;
    private EnumSet<Status> displayStatuses;
    private final NoContentPanel noContentComponent = new NoContentPanel();
    private static final RequestProcessor RP = new RequestProcessor("GitVersioningWindow", 1, true); //NOI18N
    private RequestProcessor.Task refreshNodesTask = RP.create(new RefreshNodesTask());
    private final ApplyChangesTask applyChangeTask = new ApplyChangesTask();
    private RequestProcessor.Task changeTask = RP.create(applyChangeTask);
    static final Logger LOG = Logger.getLogger(VersioningPanelController.class.getName());
    private final SyncTable syncTable;

    VersioningPanelController (GitVersioningTopComponent tc) {
        this.tc = tc;
        this.panel = new VersioningPanel();

        initDisplayStatus();
        onDisplayedStatusChanged();
        syncTable = new SyncTable(new StatusTableModel());
        setVersioningComponent(syncTable.getComponent());
        
        attachListeners();
        tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        tc.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "prevInnerView"); // NOI18N
        tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N
        tc.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.SHIFT_MASK | InputEvent.ALT_MASK), "nextInnerView"); // NOI18N

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

    void focus () {
    }

    JPanel getPanel () {
        return panel;
    }

    void setContext (VCSContext context) {
        if (context != this.context) {
            this.context = context;
            refreshNodes();
        }
    }

    void cancelRefresh() {
    }

    private void attachListeners() {
        panel.tgbHeadVsWorking.addActionListener(this);
        panel.tgbHeadVsIndex.addActionListener(this);
        panel.tgbIndexVsWorking.addActionListener(this);
        panel.btnCommit.addActionListener(this);
        panel.btnCheckout.addActionListener(this);
        panel.btnDiff.addActionListener(this);
        panel.btnRefresh.addActionListener(this);
        Git.getInstance().getFileStatusCache().addPropertyChangeListener(this);
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
        // TODO persist selection
        if (panel.tgbHeadVsWorking.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING);
//            GitModuleConfig.getDefault().setLastUsedModificationContext(Setup.DIFFTYPE_LOCAL);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanelController.class, "MSG_No_Changes_HeadWorking")); // NOI18N
        } else if (panel.tgbHeadVsIndex.isSelected()) {
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_HEAD_VS_INDEX);
//            GitModuleConfig.getDefault().setLastUsedModificationContext(Setup.DIFFTYPE_LOCAL);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanelController.class, "MSG_No_Changes_HeadIndex")); // NOI18N
        } else {
            setDisplayStatuses(FileInformation.STATUS_MODIFIED_INDEX_VS_WORKING);
//            GitModuleConfig.getDefault().setLastUsedModificationContext(Setup.DIFFTYPE_ALL);
            noContentComponent.setLabel(NbBundle.getMessage(VersioningPanelController.class, "MSG_No_Changes_IndexWorking")); // NOI18N
        }
    }

    private void setDisplayStatuses (EnumSet<Status> displayStatuses) {
        this.displayStatuses = displayStatuses;
        refreshNodes();
    }

    @Override
    public void actionPerformed (final ActionEvent e) {
        if (e.getSource() == panel.tgbHeadVsIndex || e.getSource() == panel.tgbHeadVsWorking 
                || e.getSource() == panel.tgbIndexVsWorking) {
            onDisplayedStatusChanged();
            return;
        }
        Utils.postParallel(new Runnable() {
            @Override
            public void run() {
                if (e.getSource() == panel.btnCheckout) {
                    SystemAction.get(CheckoutPathsAction.class).performAction(context);
                } else if (e.getSource() == panel.btnCommit) {
                    SystemAction.get(CommitAction.class).performAction(context);
                } else if (e.getSource() == panel.btnRefresh) {
                    GitProgressSupport supp = SystemAction.get(StatusAction.class).scanStatus(context);
                    if (!(supp == null || supp.isCanceled())) {
                        refreshNodes();
                    }
                }
            }
        }, 0);
    }

    private void initDisplayStatus () {
        // TODO read from preferences
        panel.tgbHeadVsWorking.setSelected(true);
        displayStatuses = FileInformation.STATUS_MODIFIED_HEAD_VS_WORKING;
    }

    private void setVersioningComponent (final JComponent component)  {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                Component [] children = panel.getComponents();
                for (int i = 0; i < children.length; i++) {
                    Component child = children[i];
                    if (child != panel.jPanel2) {
                        if (child == component) {
                            return;
                        } else {
                            panel.remove(child);
                            break;
                        }
                    }
                }
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = GridBagConstraints.REMAINDER; gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.FIRST_LINE_START; gbc.fill = GridBagConstraints.BOTH;
                gbc.weightx = 1; gbc.weighty = 1;

                panel.add(component, gbc);
                panel.revalidate();
                panel.repaint();
            }
        });
    }

    private void refreshNodes () {
        if (context != null) {
            refreshNodesTask.cancel();
            refreshNodesTask.schedule(0);
        }
    }

    private void applyChange (FileStatusCache.ChangedEvent event) {
        if (context != null) {
            synchronized (applyChangeTask.changes) {
                applyChangeTask.changes.add(event);
            }
            changeTask.schedule(1000);
        }
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (FileStatusCache.PROP_FILE_STATUS_CHANGED.equals(evt.getPropertyName())) {
            FileStatusCache.ChangedEvent changedEvent = (FileStatusCache.ChangedEvent) evt.getNewValue();
            if (affectsView((FileStatusCache.ChangedEvent) evt.getNewValue())) {
                applyChange(changedEvent);
            }
            return;
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

    private class RefreshNodesTask implements Runnable {
        @Override
        public void run() {
            final List<StatusNode> nodes = new LinkedList<StatusNode>();
            Git git = Git.getInstance();
            File[] interestingFiles = git.getFileStatusCache().listFiles(context.getRootFiles(), displayStatuses);
            for (File f : interestingFiles) {
                File root = git.getRepositoryRoot(f);
                if (root != null) {
                    nodes.add(new StatusNode(new GitFileNode(root, f)));
                }
            }
            Mutex.EVENT.readAccess(new Runnable () {
                @Override
                public void run() {
                    syncTable.setNodes(nodes.toArray(new StatusNode[nodes.size()]));
                    if (nodes.isEmpty()) {
                        setVersioningComponent(noContentComponent);
                    } else {
                        setVersioningComponent(syncTable.getComponent());
                    }
                }
            });
        }
    }

    /**
     * Eliminates unnecessary cache.listFiles call as well as the whole node creation process ()
     */
    private class ApplyChangesTask implements Runnable {
        private final Set<FileStatusCache.ChangedEvent> changes = new HashSet<FileStatusCache.ChangedEvent>();
        @Override
        public void run() {
            final Set<FileStatusCache.ChangedEvent> events;
            synchronized (changes) {
                events = new HashSet<FileStatusCache.ChangedEvent>(changes);
                changes.clear();
            }
            // remove irrelevant changes
            for (Iterator<FileStatusCache.ChangedEvent> it = events.iterator(); it.hasNext(); ) {
                FileStatusCache.ChangedEvent evt = it.next();
                if (!affectsView(evt)) {
                    it.remove();
                }
            }
            Git git = Git.getInstance();
            Collection<StatusNode> nodes = syncTable.getNodes();
            Map<File, StatusNode> nodesAsMap = new HashMap<File, StatusNode>(nodes.size());
            for (StatusNode node : nodes) {
                nodesAsMap.put(node.getFile(), node);
            }
            // sort changes
            final List<StatusNode> toRemove = new LinkedList<StatusNode>();
            final List<StatusNode> toRefresh = new LinkedList<StatusNode>();
            final List<StatusNode> toAdd = new LinkedList<StatusNode>();
            for (FileStatusCache.ChangedEvent evt : events) {
                FileInformation newInfo = evt.getNewInfo();
                StatusNode node = nodesAsMap.get(evt.getFile());
                if (newInfo.containsStatus(displayStatuses)) {
                    if (node != null) {
                        toRefresh.add(node);
                    } else {
                        File root = git.getRepositoryRoot(evt.getFile());
                        if (root != null) {
                            toAdd.add(new StatusNode(new GitFileNode(root, evt.getFile())));
                        }
                    }
                } else if (node != null) {
                    toRemove.add(node);
                }
            }

            Mutex.EVENT.readAccess(new Runnable () {
                @Override
                public void run() {
                    syncTable.updateNodes(toRemove, toRefresh, toAdd);
                    Collection<StatusNode> nodes = syncTable.getNodes();
                    if (nodes.isEmpty()) {
                        setVersioningComponent(noContentComponent);
                    } else {
                        setVersioningComponent(syncTable.getComponent());
                    }
                }
            });
        }
    }
}
