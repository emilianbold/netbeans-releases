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

package org.netbeans.modules.git.remote.ui.repository;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.git.remote.cli.GitBranch;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;

/**
 *
 */
public class RevisionDialogController implements ActionListener, DocumentListener, PropertyChangeListener, ListSelectionListener {
    private final RevisionDialog panel;
    private final VCSFileProxy repository;
    private final RevisionInfoPanelController infoPanelController;
    private final PropertyChangeSupport support;
    public static final String PROP_VALID = "RevisionDialogController.valid"; //NOI18N
    public static final String PROP_REVISION_ACCEPTED = "RevisionDialogController.revisionAccepted"; //NOI18N
    private boolean valid;
    private final Timer t;
    private boolean internally;
    private final VCSFileProxy[] roots;
    private String revisionString;
    private String mergingInto;
    private Revision revisionInfo;
    private DefaultListModel<Object> branchModel;

    public RevisionDialogController (VCSFileProxy repository, VCSFileProxy[] roots, String initialRevision) {
        this(repository, roots);
        panel.revisionField.setText(initialRevision);
        panel.revisionField.setCaretPosition(panel.revisionField.getText().length());
        panel.revisionField.moveCaretPosition(0);
        hideFields(new JComponent[] { panel.lblBranch, panel.branchesPanel });
    }

    /**
     * 
     * @param repository
     * @param roots
     * @param branches if this is an empty map, branches will be loaded in background
     * @param defaultBranchName branch you want to select by default or <code>null</code> to preselect the current branch
     */
    public RevisionDialogController (VCSFileProxy repository, VCSFileProxy[] roots, Map<String, GitBranch> branches, String defaultBranchName) {
        this(repository, roots);
        hideFields(new JComponent[] { panel.lblRevision, panel.revisionField, panel.btnSelectRevision });
        setModel(branches, defaultBranchName);
    }

    private RevisionDialogController (VCSFileProxy repository, VCSFileProxy[] roots) {
        infoPanelController = new RevisionInfoPanelController(repository);
        this.panel = new RevisionDialog(infoPanelController.getPanel());
        this.repository = repository;
        this.roots = roots;
        this.support = new PropertyChangeSupport(this);
        this.t = new Timer(500, this);
        t.stop();
        infoPanelController.loadInfo(revisionString = panel.revisionField.getText());
        attachListeners();
    }
    
    public RevisionDialog getPanel () {
        return panel;
    }

    public void setEnabled (boolean enabled) {
        panel.btnSelectRevision.setEnabled(enabled);
        panel.revisionField.setEnabled(enabled);
    }

    public Revision getRevision () {
        return revisionInfo == null ? new Revision(revisionString, revisionString) : revisionInfo;
    }
    
    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

    public void setMergingInto (String revision) {
        mergingInto = revision;
        infoPanelController.displayMergedStatus(revision);
    }

    private void attachListeners () {
        panel.btnSelectRevision.addActionListener(this);
        panel.revisionField.getDocument().addDocumentListener(this);
        panel.lstBranches.addListSelectionListener(this);
        panel.lstBranches.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (e.getSource() == panel.lstBranches) {
                    if (e.getClickCount() == 2 && revisionInfo != null) {
                        e.consume();
                        support.firePropertyChange(PROP_REVISION_ACCEPTED, null, revisionInfo);
                    }
                }
            }
        });
        infoPanelController.addPropertyChangeListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnSelectRevision) {
            openRevisionPicker();
        } else if (e.getSource() == t) {
            t.stop();
            infoPanelController.loadInfo(revisionString);
        }
    }

    @Override
    public void valueChanged (ListSelectionEvent e) {
        if (!e.getValueIsAdjusting() && e.getSource() == panel.lstBranches) {
            selectedBranchChanged();
        }
    }

    private void openRevisionPicker () {
        RevisionPicker picker = new RevisionPicker(repository, roots);
        picker.displayMergedStatus(mergingInto);
        if (picker.open()) {
            Revision selectedRevision = picker.getRevision();
            internally = true;
            try {
                panel.revisionField.setText(selectedRevision.getRevision());
                panel.revisionField.setCaretPosition(0);
            } finally {
                internally = false;
            }
            if (!selectedRevision.getRevision().equals(revisionString)) {
                revisionString = selectedRevision.getRevision();
                updateRevision();
            }
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        revisionChanged();
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        revisionChanged();
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }

    private void setValid (boolean flag, Revision revision) {
        boolean oldValue = valid;
        valid = flag;
        revisionInfo = revision;
        if (valid != oldValue) {
            support.firePropertyChange(PROP_VALID, oldValue, valid);
        }
    }

    private void revisionChanged () {
        if (!internally) {
            revisionString = panel.revisionField.getText();
            updateRevision();
        }
    }
    
    private void updateRevision () {
        setValid(false, null);
        t.restart();
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RevisionInfoPanelController.PROP_VALID) {
            setValid(Boolean.TRUE.equals(evt.getNewValue()), infoPanelController.getInfo());
        }
    }

    private void hideFields (JComponent[] fields) {
        for (JComponent field : fields) {
            field.setVisible(false);
        }
    }

    @NbBundle.Messages({
        "MSG_RevisionDialog.noBranches=No Branches"
    })
    private void setModel (Map<String, GitBranch> branches, String toSelectBranchName) {
        if (branches.isEmpty()) {
            loadBranches(toSelectBranchName);
            return;
        }
        final List<Revision> branchList = new ArrayList<>(branches.size());
        List<Revision> remoteBranchList = new ArrayList<>(branches.size());
        Revision activeBranch = null;
        for (Map.Entry<String, GitBranch> e : branches.entrySet()) {
            GitBranch branch = e.getValue();
            Revision rev = null;
            if (branch.isRemote()) {
                rev = new Revision.BranchReference(branch);
                remoteBranchList.add(rev);
            } else if (branch.getName() != GitBranch.NO_BRANCH) {
                rev = new Revision.BranchReference(branch);
                branchList.add(rev);
            }
            if (rev != null && (toSelectBranchName != null && toSelectBranchName.equals(branch.getName())
                    || toSelectBranchName == null && branch.isActive())) {
                activeBranch = rev;
            }
        }
        Comparator<Revision> comp = new Comparator<Revision>() {
            @Override
            public int compare (Revision b1, Revision b2) {
                return b1.getRevision().compareTo(b2.getRevision());
            }
        };
        Collections.sort(branchList, comp);
        Collections.sort(remoteBranchList, comp);
        branchList.addAll(remoteBranchList);
        final Revision toSelect = activeBranch;
        branchModel = new DefaultListModel<>();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                if (branchList.isEmpty()) {
                    branchModel.addElement(Bundle.MSG_RevisionDialog_noBranches());
                } else {
                    for (Revision rev : branchList) {
                        branchModel.addElement(rev);
                    }
                }
                panel.lstBranches.setModel(branchModel);
                panel.lstBranches.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        return super.getListCellRendererComponent(list, value instanceof Revision ? ((Revision) value).getRevision() : value, index, isSelected, cellHasFocus);
                    }
                });
                if (toSelect != null) {
                    panel.lstBranches.setSelectedValue(toSelect, true);
                }
                selectedBranchChanged();
                if (!branchList.isEmpty()) {
                    GitUtils.<Revision>attachQuickSearch(branchList, panel.branchesPanel, panel.lstBranches, branchModel, new GitUtils.SearchCallback<Revision>() {

                        @Override
                        public boolean contains (Revision rev, String needle) {
                            return rev.getRevision().toLowerCase(Locale.getDefault()).contains(needle.toLowerCase(Locale.getDefault()));
                        }
                    });
                }
            }
        });
    }

    private void selectedBranchChanged () {
        Object activeBranch = panel.lstBranches.getSelectedValue();
        if (activeBranch instanceof Revision) {
            revisionString = ((Revision) activeBranch).getRevision();
            setValid(valid, (Revision) activeBranch);
            t.restart();
        } else {
            revisionString = activeBranch instanceof Revision ? ((Revision) activeBranch).getRevision() : Bundle.MSG_RevisionDialog_noBranches();
            updateRevision();
        }
    }

    @NbBundle.Messages({
        "RevisionDialogController.loadingBranches=Loading Branches..."
    })
    private void loadBranches (final String defaultBranch) {
        DefaultListModel model = new DefaultListModel();
        model.addElement(Bundle.RevisionDialogController_loadingBranches());
        panel.lstBranches.setModel(model);
        panel.lstBranches.setEnabled(false);
        new GitProgressSupport.NoOutputLogging() {
            
            @Override
            protected void perform () {
                final Map<String, GitBranch> branches = RepositoryInfo.getInstance(repository).getBranches();
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run () {
                        panel.lstBranches.setEnabled(true);
                        setModel(branches.isEmpty()
                                ? Collections.singletonMap(GitBranch.NO_BRANCH, GitBranch.NO_BRANCH_INSTANCE)
                                : branches, defaultBranch);
                    }
                });
            }
        }.start(Git.getInstance().getRequestProcessor(repository), repository, Bundle.RevisionDialogController_loadingBranches());
    }
}
