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

package org.netbeans.modules.git.ui.repository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
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
import org.netbeans.libs.git.GitBranch;
import org.openide.awt.QuickSearch;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevisionDialogController implements ActionListener, DocumentListener, PropertyChangeListener, ListSelectionListener {
    private final RevisionDialog panel;
    private final File repository;
    private final RevisionInfoPanelController infoPanelController;
    private final PropertyChangeSupport support;
    public static final String PROP_VALID = "RevisionDialogController.valid"; //NOI18N
    private boolean valid = true;
    private final Timer t;
    private boolean internally;
    private final File[] roots;
    private String revisionString;
    private String mergingInto;
    private Revision revisionInfo;
    private DefaultListModel<Object> branchModel;

    public RevisionDialogController (File repository, File[] roots, String initialRevision) {
        this(repository, roots);
        panel.revisionField.setText(initialRevision);
        panel.revisionField.setCaretPosition(panel.revisionField.getText().length());
        panel.revisionField.moveCaretPosition(0);
        hideFields(new JComponent[] { panel.lblBranch, panel.branchesPanel });
    }

    public RevisionDialogController (File repository, File[] roots, Map<String, GitBranch> branches) {
        this(repository, roots);
        hideFields(new JComponent[] { panel.lblRevision, panel.revisionField, panel.btnSelectRevision });
        setModel(branches);
    }

    private RevisionDialogController (File repository, File[] roots) {
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

    private void setValid (boolean flag) {
        boolean oldValue = valid;
        valid = flag;
        if (valid) {
            revisionInfo = infoPanelController.getInfo();
        } else {
            revisionInfo = null;
        }
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
        setValid(false);
        t.restart();
    }

    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getPropertyName() == RevisionInfoPanelController.PROP_VALID) {
            setValid(Boolean.TRUE.equals(evt.getNewValue()));
        }
    }

    private void hideFields (JComponent[] fields) {
        for (JComponent field : fields) {
            field.setVisible(false);
        }
    }

    @NbBundle.Messages({
        "MSG_RevisionDialog.selectBranch=Select Branch"
    })
    private void setModel (Map<String, GitBranch> branches) {
        final List<Revision> branchList = new ArrayList<Revision>(branches.size());
        List<Revision> remoteBranchList = new ArrayList<Revision>(branches.size());
        Revision activeBranch = null;
        for (Map.Entry<String, GitBranch> e : branches.entrySet()) {
            GitBranch branch = e.getValue();
            if (branch.isRemote()) {
                remoteBranchList.add(new Revision.BranchReference(branch));
            } else if (branch.getName() != GitBranch.NO_BRANCH) {
                Revision rev = new Revision.BranchReference(branch);
                branchList.add(rev);
                if (branch.isActive()) {
                    activeBranch = rev;
                }
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
        branchModel = new DefaultListModel<Object>();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                if (branchList.isEmpty()) {
                    branchModel.addElement(Bundle.MSG_RevisionDialog_selectBranch());
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
                    attachQuickSearch(branchList);
                }
            }
        });
    }

    private void selectedBranchChanged () {
        Object activeBranch = panel.lstBranches.getSelectedValue();
        revisionString = activeBranch instanceof Revision ? ((Revision) activeBranch).getRevision() : Bundle.MSG_RevisionDialog_selectBranch();
        updateRevision();
    }
    
    private boolean quickSearchActive;
    private void attachQuickSearch (final List<Revision> branchList) {
        final QuickSearch qs = QuickSearch.attach(panel.branchesPanel, BorderLayout.SOUTH, new QuickSearch.Callback() {
            
            private int currentPosition = 0;
            private final List<Revision> results = new ArrayList<Revision>(branchList);
            
            @Override
            public void quickSearchUpdate (String searchText) {
                quickSearchActive = true;
                Revision selected = branchList.get(0);
                if (currentPosition > -1) {
                    selected = results.get(currentPosition);
                }
                results.clear();
                results.addAll(branchList);
                if (!searchText.isEmpty()) {
                    for (ListIterator<Revision> it = results.listIterator(); it.hasNext(); ) {
                        Revision rev = it.next();
                        if (!rev.getRevision().contains(searchText)) {
                            it.remove();
                        }
                    }
                }
                currentPosition = results.indexOf(selected);
                if (currentPosition == -1 && !results.isEmpty()) {
                    currentPosition = 0;
                }
                updateView();
            }

            @Override
            public void showNextSelection (boolean forward) {
                if (currentPosition != -1) {
                    currentPosition += forward ? 1 : -1;
                    if (currentPosition < 0) {
                        currentPosition = results.size() - 1;
                    } else if (currentPosition == results.size()) {
                        currentPosition = 0;
                    }
                    updateSelection();
                }
            }

            @Override
            public String findMaxPrefix (String prefix) {
                return prefix;
            }

            @Override
            public void quickSearchConfirmed () {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        panel.lstBranches.requestFocusInWindow();
                    }
                });
            }

            @Override
            public void quickSearchCanceled () {
                quickSearchUpdate("");
                quickSearchActive = false;
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        panel.lstBranches.requestFocusInWindow();
                    }
                });
            }

            private void updateView () {
                branchModel.removeAllElements();
                for (Revision r : results) {
                    branchModel.addElement(r);
                }
                updateSelection();
            }

            private void updateSelection () {
                if (currentPosition > -1 && currentPosition < results.size()) {
                    Revision rev = results.get(currentPosition);
                    panel.lstBranches.setSelectedValue(rev, true);
                }
            }
        });
        qs.setAlwaysShown(true);
        panel.lstBranches.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped (KeyEvent e) {
                qs.processKeyEvent(e);
            }

            @Override
            public void keyPressed (KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        || e.getKeyCode() == KeyEvent.VK_ESCAPE && !quickSearchActive) {
                    // leave events up to other components
                } else {
                    qs.processKeyEvent(e);
                }
            }

            @Override
            public void keyReleased (KeyEvent e) {
                qs.processKeyEvent(e);
            }
        });
    }
}
