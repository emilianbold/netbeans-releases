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

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.libs.git.GitBranch;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevisionDialogController implements ActionListener, DocumentListener, PropertyChangeListener, ItemListener {
    private final RevisionDialog panel;
    private final File repository;
    private final RevisionInfoPanelController infoPanelController;
    private final PropertyChangeSupport support;
    public static final String PROP_VALID = "RevisionDialogController.valid"; //NOI18N
    private boolean valid = true;
    private final Timer t;
    private boolean internally;
    private final File[] roots;
    private String revision;

    public RevisionDialogController (File repository, File[] roots, String initialRevision) {
        this(repository, roots);
        panel.revisionField.setText(initialRevision);
        panel.revisionField.setCaretPosition(panel.revisionField.getText().length());
        panel.revisionField.moveCaretPosition(0);
        hideFields(new JComponent[] { panel.lblBranch, panel.cmbBranches });
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
        infoPanelController.loadInfo(revision = panel.revisionField.getText());
        attachListeners();
    }
    
    public RevisionDialog getPanel () {
        return panel;
    }

    public void setEnabled (boolean enabled) {
        panel.btnSelectRevision.setEnabled(enabled);
        panel.revisionField.setEnabled(enabled);
    }

    public String getRevision () {
        return revision;
    }
    
    public void addPropertyChangeListener (PropertyChangeListener list) {
        support.addPropertyChangeListener(list);
    }
    
    public void removePropertyChangeListener (PropertyChangeListener list) {
        support.removePropertyChangeListener(list);
    }

    private void attachListeners () {
        panel.btnSelectRevision.addActionListener(this);
        panel.revisionField.getDocument().addDocumentListener(this);
        panel.cmbBranches.addItemListener(this);
        infoPanelController.addPropertyChangeListener(this);
    }

    @Override
    public void actionPerformed (ActionEvent e) {
        if (e.getSource() == panel.btnSelectRevision) {
            openRevisionPicker();
        } else if (e.getSource() == t) {
            t.stop();
            infoPanelController.loadInfo(revision);
        }
    }

    @Override
    public void itemStateChanged (ItemEvent e) {
        if (e.getSource() == panel.cmbBranches) {
            selectedBranchChanged();
        }
    }

    private void openRevisionPicker () {
        RevisionPicker picker = new RevisionPicker(repository, roots);
        if (picker.open()) {
            Revision selectedRevision = picker.getRevision();
            internally = true;
            try {
                panel.revisionField.setText(selectedRevision.toString());
                panel.revisionField.setCaretPosition(0);
            } finally {
                internally = false;
            }
            if (!selectedRevision.getName().equals(revision)) {
                revision = selectedRevision.getName();
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
        if (valid != oldValue) {
            support.firePropertyChange(PROP_VALID, oldValue, valid);
        }
    }

    private void revisionChanged () {
        if (!internally) {
            revision = panel.revisionField.getText();
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

    private void setModel (Map<String, GitBranch> branches) {
        final List<GitBranch> branchList = new ArrayList<GitBranch>(branches.size());
        List<GitBranch> remoteBranchList = new ArrayList<GitBranch>(branches.size());
        for (Map.Entry<String, GitBranch> e : branches.entrySet()) {
            GitBranch branch = e.getValue();
            if (branch.isRemote()) {
                remoteBranchList.add(branch);
            } else if (branch.getName() != GitBranch.NO_BRANCH) {
                branchList.add(branch);
            }
        }
        Comparator<GitBranch> comp = new Comparator<GitBranch>() {
            @Override
            public int compare (GitBranch b1, GitBranch b2) {
                return b1.getName().compareTo(b2.getName());
            }
        };
        Collections.sort(branchList, comp);
        Collections.sort(remoteBranchList, comp);
        branchList.addAll(remoteBranchList);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run () {
                panel.cmbBranches.setModel(new DefaultComboBoxModel(branchList.isEmpty() ? new Object[] { NbBundle.getMessage(RevisionDialogController.class, "MSG_RevisionDialog.selectBranch") } 
                        : branchList.toArray(new GitBranch[branchList.size()])));//NOI18N
                panel.cmbBranches.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        return super.getListCellRendererComponent(list, value instanceof GitBranch ? ((GitBranch) value).getName() : value, index, isSelected, cellHasFocus);
                    }
                });
                selectedBranchChanged();
            }
        });
    }

    private void selectedBranchChanged () {
        Object activeBranch = panel.cmbBranches.getSelectedItem();
        revision = activeBranch instanceof GitBranch ? ((GitBranch) activeBranch).getName() : NbBundle.getMessage(RevisionDialogController.class, "MSG_RevisionDialog.selectBranch"); //NOI18N
        updateRevision();
    }
}
