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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.mercurial.remote.ui.branch;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.logging.Level;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * 
 */
public class BranchSelector implements ListSelectionListener, DocumentListener {

    private final BranchSelectorPanel panel;
    private JButton okButton;
    private final JButton cancelButton;
    private final VCSFileProxy repository;
    private static final RequestProcessor   rp = new RequestProcessor("BranchPicker", 1, true);  // NOI18N
    private boolean bGettingRevisions = false;
    private InitialLoadingProgressSupport loadingSupport;
    private static final String MARK_ACTIVE_HEAD = "*"; //NOI18N
    
    private static final String INITIAL_REVISION = NbBundle.getMessage(BranchSelectorPanel.class, "MSG_Revision_Loading"); //NOI18N
    private static final String NO_BRANCH = NbBundle.getMessage(BranchSelectorPanel.class, "MSG_Revision_NoRevision"); //NOI18N
    private HgLogMessage.HgRevision parentRevision;
    private final Timer filterTimer;
    private HgBranch[] branches;
    private final Object LOCK = new Object();
    
    public BranchSelector (VCSFileProxy repository) {
        this.repository = repository;
        panel = new BranchSelectorPanel();
        panel.branchList.setCellRenderer(new RevisionRenderer());
        
        filterTimer = new Timer(300, new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                filterTimer.stop();
                applyFilter();
            }
        });
        panel.txtFilter.getDocument().addDocumentListener(this);
        panel.branchList.addListSelectionListener(this);
        panel.jPanel1.setVisible(false);
        cancelButton = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(cancelButton, org.openide.util.NbBundle.getMessage(BranchSelector.class, "CTL_BranchSelector_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_BranchSelector_Action_Cancel")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSN_BranchSelector_Action_Cancel")); // NOI18N
    } 
    
    public boolean showDialog (JButton okButton, String title, String branchListDescription) {
        this.okButton = okButton;
        org.openide.awt.Mnemonics.setLocalizedText(panel.jLabel1, branchListDescription);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title, true, new Object[] {okButton, cancelButton}, 
                okButton, DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(this.getClass()), null);
        
        dialogDescriptor.setValid(false);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(title);
        loadRevisions();
        dialog.setVisible(true);
        HgProgressSupport supp = loadingSupport;
        if (supp != null) {
            supp.cancel();
        }
        boolean ret = dialogDescriptor.getValue() == okButton;
        return ret;
    }
    
    public boolean showGeneralDialog () {
        JButton btn = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btn, org.openide.util.NbBundle.getMessage(BranchSelector.class, "CTL_BranchSelectorPanel_Action_OK")); // NOI18N
        btn.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSD_BranchSelectorPanel_Action_OK")); // NOI18N
        btn.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BranchSelector.class, "ACSN_BranchSelectorPanel_Action_OK")); // NOI18N
        btn.setEnabled(false);
        return showDialog(btn, NbBundle.getMessage(BranchSelector.class, "CTL_BranchSelectorPanel.title", repository.getName()), //NOI18N
                NbBundle.getMessage(BranchSelector.class, "BranchSelectorPanel.infoLabel.text")); //NOI18N
    }

    public String getBranchName () {
        HgBranch selectedBranch = getSelectedBranch();
        return selectedBranch == null ? null : selectedBranch.getName();
    }

    private HgBranch getSelectedBranch () {
        if (panel.branchList.getSelectedValue() instanceof HgBranch) {
            return (HgBranch) panel.branchList.getSelectedValue();
        } else {
            return null;
        }
    }

    private String getRefreshLabel () {
        return NbBundle.getMessage(BranchSelectorPanel.class, "MSG_BranchSelector_Refreshing_Branches"); //NOI18N
    }

    private void loadRevisions () {
        loadingSupport = new InitialLoadingProgressSupport();
        loadingSupport.start(rp, repository, getRefreshLabel()); //NOI18N
    }

    public void setOptionsPanel (JPanel optionsPanel, Border parentPanelBorder) {
        if (optionsPanel == null) {
            panel.jPanel1.setVisible(false);
        } else {
            if (parentPanelBorder != null) {
                panel.jPanel1.setBorder(parentPanelBorder);
            }
            panel.jPanel1.removeAll();
            panel.jPanel1.add(optionsPanel, BorderLayout.NORTH);
            panel.jPanel1.setVisible(true);
        }
    }

    @Override
    public void valueChanged (ListSelectionEvent evt) {
        if (!evt.getValueIsAdjusting()) {
            HgBranch branch = null;
            if (panel.branchList.getSelectedValue() instanceof HgBranch) {
                branch = (HgBranch) panel.branchList.getSelectedValue();
            }
            if (branch == null) {
                okButton.setEnabled(false);
            } else {
                okButton.setEnabled(true);
                panel.changesetPanel1.setInfo(branch.getRevisionInfo());
            }
        }
    }

    @Override
    public void insertUpdate (DocumentEvent e) {
        if (!bGettingRevisions) {
            filterTimer.restart();
        }
    }

    @Override
    public void removeUpdate (DocumentEvent e) {
        if (!bGettingRevisions) {
            filterTimer.restart();
        }
    }

    @Override
    public void changedUpdate (DocumentEvent e) {
    }
    
    private class InitialLoadingProgressSupport extends HgProgressSupport {
        @Override
        public void perform () {
            try {
                final DefaultListModel targetsModel = new DefaultListModel();
                targetsModel.addElement(INITIAL_REVISION);
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        panel.branchList.setModel(targetsModel);
                        if (!targetsModel.isEmpty()) {
                            panel.branchList.setSelectedIndex(0);
                        }
                    }
                });
                refreshRevisions(this);
            } finally {
                loadingSupport = null;
            }
        }

        private void refreshRevisions (HgProgressSupport supp) {
            bGettingRevisions = true;
            OutputLogger logger = Mercurial.getInstance().getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
            HgBranch[] fetchedBranches;
            try {
                fetchedBranches = HgCommand.getBranches(repository, logger);
            } catch (HgException ex) {
                Mercurial.LOG.log(Level.INFO, null, ex);
                fetchedBranches = null;
            }
            if( fetchedBranches == null) {
                fetchedBranches = new HgBranch[0];
            }

            if (!supp.isCanceled() && fetchedBranches.length > 0) {
                try {
                    parentRevision = HgCommand.getParent(repository, null, null);
                } catch (HgException ex) {
                    Mercurial.LOG.log(Level.FINE, null, ex);
                }
            }

            if (!supp.isCanceled()) {
                Arrays.sort(fetchedBranches, new Comparator<HgBranch>() {
                    @Override
                    public int compare (HgBranch b1, HgBranch b2) {
                        return b1.getName().compareTo(b2.getName());
                    }
                });
                synchronized (LOCK) {
                    branches = fetchedBranches;
                }
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run () {
                        applyFilter();
                        bGettingRevisions = false;
                    }
                });
            }
        }
    }

    private class RevisionRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof HgBranch) {
                HgBranch branch = (HgBranch) value;
                StringBuilder sb = new StringBuilder().append(branch.getName());
                HgLogMessage.HgRevision parent = parentRevision;
                if (parent != null && parent.getRevisionNumber().equals(branch.getRevisionInfo().getRevisionNumber())) {
                    sb.append(MARK_ACTIVE_HEAD);
                }
                sb.append(" (").append(branch.getRevisionInfo().getCSetShortID().substring(0, 7)); //NOI18N
                if (!branch.isActive()) {
                    sb.append(" - ").append(NbBundle.getMessage(BranchSelector.class, "LBL_BranchSelector.branch.inactive")); //NOI18N
                }
                sb.append(")"); //NOI18N
                value = sb.toString();
            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private void applyFilter () {
        HgBranch selectedBranch = getSelectedBranch();
        DefaultListModel targetsModel = new DefaultListModel();
        targetsModel.removeAllElements();
        HgBranch toSelect = null;
        String filter = panel.txtFilter.getText();
        synchronized (LOCK) {
            for (HgBranch branch : branches) {
                if (applies(filter, branch)) {
                    if (selectedBranch != null && branch.getRevisionInfo().getCSetShortID().equals(selectedBranch.getRevisionInfo().getCSetShortID())) {
                        toSelect = branch;
                    } else if (parentRevision != null && branch.getRevisionInfo().getCSetShortID().equals(parentRevision.getChangesetId())) {
                        toSelect = branch;
                    }
                    targetsModel.addElement(branch);
                }
            }
        }
        if (targetsModel.isEmpty()) {
            targetsModel.addElement(NO_BRANCH);
        }
        if (!Arrays.equals(targetsModel.toArray(), ((DefaultListModel) panel.branchList.getModel()).toArray())) {
            panel.branchList.setModel(targetsModel);
            if (toSelect != null) {
                panel.branchList.setSelectedValue(toSelect, true);
            } else if (targetsModel.size() > 0) {
                panel.branchList.setSelectedIndex(0);
            }
        }
    }

    private boolean applies (String filter, HgBranch branch) {
        boolean applies = filter.isEmpty();
        filter = filter.toLowerCase(Locale.getDefault());
        String inactiveLabel = NbBundle.getMessage(BranchSelector.class, "LBL_BranchSelector.branch.inactive"); //NOI18N
        if (!applies) {
            HgLogMessage message = branch.getRevisionInfo();
            if (branch.getName().contains(filter)
                    || branch.isActive() && "active".startsWith(filter) //NOI18N
                    || !branch.isActive() && inactiveLabel.startsWith(filter) //NOI18N
                    || message.getRevisionNumber().contains(filter)
                    || message.getAuthor().toLowerCase(Locale.getDefault()).contains(filter)
                    || message.getCSetShortID().toLowerCase(Locale.getDefault()).contains(filter)
                    || message.getMessage().toLowerCase(Locale.getDefault()).contains(filter)
                    || message.getUsername().toLowerCase(Locale.getDefault()).contains(filter)
                    || applies(filter, message.getBranches())
                    || applies(filter, message.getTags())
                    || DateFormat.getDateTimeInstance().format(message.getDate()).toLowerCase(Locale.getDefault()).contains(filter)
                    ) {
                applies = true;
            }
        }
        return applies;        
    }
    
    private boolean applies (String format, String[] array) {
        for (String v : array) {
            if (v.toLowerCase(Locale.getDefault()).contains(format)) {
                return true;
            }
        }
        return false;
    }
}
