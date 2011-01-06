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

/*
 * RevisionsPanel.java
 *
 * Created on Dec 21, 2010, 5:14:40 PM
 */

package org.netbeans.modules.git.ui.repository;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.libs.git.GitClient;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitFileInfo;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.netbeans.libs.git.SearchCriteria;
import org.netbeans.libs.git.progress.ProgressMonitor;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.client.GitClientExceptionHandler;
import org.netbeans.modules.git.client.GitProgressSupport;
import org.netbeans.modules.git.ui.output.OutputLogger;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class RevisionListPanel extends javax.swing.JPanel {

    private ProgressMonitor.DefaultProgressMonitor listHistoryMonitor;
    private final DefaultListModel revisionListModel;
    private GitProgressSupport supp;
    private final Object LOCK = new Object();
    private String lastHWRevision;
    private File lastHWRepository;
    private Revision currRevision;
    private File currRepository;
    private File[] currRoots;
    
    /** Creates new form RevisionsPanel */
    public RevisionListPanel() {
        lstRevisions.setModel(revisionListModel = new DefaultListModel());
        lstRevisions.setFixedCellHeight(-1);
        lstRevisions.setCellRenderer(new RevisionRenderer());
        initComponents();
    }
    
    private static class RevisionRenderer extends JTextPane implements ListCellRenderer {

        private Style selectedStyle;
        private Style normalStyle;
        private Color selectionBackground;
        private Color selectionForeground;

        public RevisionRenderer () {
            selectionBackground = new JList().getSelectionBackground();
            selectionForeground = new JList().getSelectionForeground();

            selectedStyle = addStyle("selected", null); // NOI18N
            StyleConstants.setForeground(selectedStyle, selectionForeground); // NOI18N
            StyleConstants.setBackground(selectedStyle, selectionBackground); // NOI18N
            normalStyle = addStyle("normal", null); // NOI18N
            StyleConstants.setForeground(normalStyle, UIManager.getColor("List.foreground")); // NOI18N

            setLayout(new BorderLayout());
            setBorder(null);
        }
        
        @Override
        public Component getListCellRendererComponent (JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            GitRevisionInfo revision = (GitRevisionInfo) value;
            StyledDocument sd = getStyledDocument();

            Style style;
            Color backgroundColor;

            if (isSelected) {
                backgroundColor = selectionBackground;
                style = selectedStyle;
            } else {
                backgroundColor = UIManager.getColor("List.background"); // NOI18N
                style = normalStyle;
            }
            setBackground(backgroundColor);

            try {
                // clear document
                sd.remove(0, sd.getLength());
                String revStr = revision.getRevision();
                if (revStr.length() > 20) {
                    revStr = revStr.substring(0, 17) + "..."; //NOI18N
                }
                sd.insertString(0, revStr, style);
            } catch (BadLocationException e) {
                //
            }

            return this;
        }
        
    }
    
    
    void updateHistory (File repository, File[] roots, Revision revision) {
        synchronized (LOCK) {
            if ((repository == lastHWRepository || lastHWRepository != null && lastHWRepository.equals(repository))
                    && (revision == null && lastHWRevision == null || lastHWRevision != null && revision != null && lastHWRevision.equals(revision.getRevision()))) {
                // no change made (selected repository i the same and selected revision is the same)
                return;
            }
            currRepository = repository;
            currRevision = revision;
            currRoots = roots;
            if (supp != null) {
                supp.cancel();
            }
            if (listHistoryMonitor != null) {
                listHistoryMonitor.cancel();
            }
            supp = new ListHistoryProgressSupport();
            supp.start(Git.getInstance().getRequestProcessor(repository), repository, NbBundle.getMessage(RevisionListPanel.class, "LBL_RevisionList.LoadingRevisions")); //NOI18N
        }
    }

    private class ListHistoryProgressSupport extends GitProgressSupport.NoOutputLogging {
        @Override
        public void perform () {
            Revision rev;
            File repository;
            File[] roots;
            synchronized (LOCK) {
                rev = currRevision;
                repository = currRepository;
                roots = currRoots;
            }
            GitRevisionInfo[] revisions = new GitRevisionInfo[0];
            if (repository != null) {
                listHistoryMonitor = new ProgressMonitor.DefaultProgressMonitor();
                if (isCanceled()) {
                    return;
                }
                try {
                    GitClient client = getClient();
                    SearchCriteria criteria = new SearchCriteria();
                    criteria.setFiles(roots);
                    if (rev != null) {
                        criteria.setRevisionTo(rev.getRevision());
                    }
                    revisions = client.log(criteria, listHistoryMonitor);
                    
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                }
                if (!isCanceled()) {
                    final GitRevisionInfo[] toModel = revisions;
                    synchronized (LOCK) {
                        lastHWRepository = repository;
                        lastHWRevision = rev == null ? null : rev.getRevision();
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run () {
                            GitRevisionInfo oldSelection = (GitRevisionInfo) lstRevisions.getSelectedValue();
                            GitRevisionInfo selection = null;
                            revisionListModel.clear();
                            for (GitRevisionInfo info : toModel) {
                                revisionListModel.addElement(new GitRevisionInfoDelegate(info)); // overrde toString, so one can Ctrl+C the revision string
                                if (oldSelection != null && oldSelection.getRevision().equals(info.getRevision())) {
                                    selection = info;
                                }
                            }
                            if (selection != null) {
                                lstRevisions.setSelectedValue(selection, false);
                            }
                        }
                    });
                }
            }
        }
    }
    
    private static class GitRevisionInfoDelegate implements GitRevisionInfo {
        private final GitRevisionInfo info;

        public GitRevisionInfoDelegate (GitRevisionInfo info) {
            this.info = info;
        }

        @Override
        public String getRevision () {
            return info.getRevision();
        }

        @Override
        public String getShortMessage () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getFullMessage () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getCommitTime () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public GitUser getAuthor () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public GitUser getCommitter () {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Map<File, GitFileInfo> getModifiedFiles () throws GitException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String toString () {
            return getRevision();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        lblMore10 = new javax.swing.JLabel();
        lblAll = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        lstRevisions.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(lstRevisions);

        lblMore10.setText(org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.lblMore10.text")); // NOI18N
        lblMore10.setEnabled(false);

        lblAll.setText(org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.lblAll.text")); // NOI18N
        lblAll.setEnabled(false);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(RevisionListPanel.class, "RevisionListPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblMore10)
                        .addGap(6, 6, 6)
                        .addComponent(lblAll)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblMore10, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAll))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAll;
    private javax.swing.JLabel lblMore10;
    final javax.swing.JList lstRevisions = new javax.swing.JList();
    // End of variables declaration//GEN-END:variables

}
