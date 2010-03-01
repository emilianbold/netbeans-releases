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
package org.netbeans.modules.mercurial.ui.repository;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;

/**
 *
 * @author  Padraig O'Briain
 */
public class ChangesetPickerPanel extends javax.swing.JPanel {

    private File                            repository;
    private File[]                          roots;
    private RequestProcessor.Task           refreshViewTask;
    private Thread                          refreshViewThread;
    private static final RequestProcessor   rp = new RequestProcessor("ChangesetPicker", 1);  // NOI18N
    private HgLogMessage[] messages;
    private int fetchRevisionLimit = Mercurial.HG_NUMBER_TO_FETCH_DEFAULT;
    private boolean bGettingRevisions = false;
    private Set<String> revisions;
    private static final String HG_TIP = "tip"; // NOI18N

    /** Creates new form ReverModificationsPanel */
     public ChangesetPickerPanel(File repo, File[] files) {
        repository = repo;
        roots = files;
        refreshViewTask = rp.create(new RefreshViewTask());
        initComponents();
        jPanel1.setVisible(false);
        revisionsComboBox.setMaximumRowCount(Mercurial.HG_MAX_REVISION_COMBO_SIZE);
    }

    public File[] getRootFiles () {
        return roots;
    }

    /**
     * Returns an array of two strings, where the one under index 0 is a revision number and the second under index 1 is a changeset string.
     * However note that the changeset can be an empty string, when the revision is e.g. TIP
     * @return
     */
    public String[] getSelectedRevision() {
        String revStr = (String) revisionsComboBox.getSelectedItem();
        String changesetStr = new String();
        if(revStr != null){
            if (revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Revision_Default")) || // NOI18N
                revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_20_Revisions")) || // NOI18N
                revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_50_Revisions")) || // NOI18N
                revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_All_Revisions")) || // NOI18N
                revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Tip_Revision"))) { // NOI18N
                revStr = HG_TIP;
            } else {
                revStr = revStr.substring(0, revStr.indexOf(" ")); // NOI18N
                changesetStr = messages == null ? "" : messages[revisionsComboBox.getSelectedIndex()].getCSetShortID(); //NOI18N
            }
        }
        return new String[] { revStr, changesetStr };
    }

    protected String getRefreshLabel () {
        return NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Refreshing_Revisions"); //NOI18N
    }

    protected RepositoryRevision getDisplayedRevision() {
        return null;
    }

    protected void loadRevisions () {
        assert revisions == null : "Revisions already loaded"; //NOI18N
        refreshViewTask.schedule(0);
    }

    protected void setOptionsPanel (JPanel optionsPanel, Border parentPanelBorder) {
        if (optionsPanel == null) {
            jPanel1.setVisible(false);
        } else {
            if (parentPanelBorder != null) {
                jPanel1.setBorder(parentPanelBorder);
            }
            jPanel1.removeAll();
            jPanel1.add(optionsPanel, BorderLayout.NORTH);
            jPanel1.setVisible(true);
        }
    }

    protected String getRevisionLabel (RepositoryRevision rev) {
        return new StringBuilder(rev.getLog().getRevision()).append(" (").append(rev.getLog().getCSetShortID()).append(")").toString(); //NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        revisionsLabel = new javax.swing.JLabel();
        revisionsComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        changesetPanel1 = new org.netbeans.modules.mercurial.ui.repository.ChangesetPanel();

        revisionsLabel.setLabelFor(revisionsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.revisionsLabel.text")); // NOI18N

        revisionsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revisionsComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, null);

        jLabel2.setForeground(new java.awt.Color(153, 153, 153));
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, null);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "ChangesetPickerPanel.options"))); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(41, 41, 41)
                        .add(revisionsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(revisionsComboBox, 0, 286, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel2)
                            .add(jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(changesetPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(4, 4, 4)
                .add(jLabel2)
                .add(17, 17, 17)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(revisionsLabel)
                    .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(changesetPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 152, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

private void revisionsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_revisionsComboBoxActionPerformed
    int index = revisionsComboBox.getSelectedIndex();
    if(getMore((String) revisionsComboBox.getSelectedItem())) return;//GEN-HEADEREND:event_revisionsComboBoxActionPerformed
    
    if(messages != null && index >= 0 && index < messages.length ){
        changesetPanel1.setInfo(messages[index]);
    }
}                                                 

    private boolean getMore(String revStr) {
        if (bGettingRevisions) return false;//GEN-LAST:event_revisionsComboBoxActionPerformed
        boolean bGetMore = false;
        int limit = -1;

        if (revStr != null && revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_20_Revisions"))) { //NOI18N
            bGetMore = true;
            limit = Mercurial.HG_FETCH_20_REVISIONS;
        } else if (revStr != null && revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_50_Revisions"))) { //NOI18N
            bGetMore = true;
            limit = Mercurial.HG_FETCH_50_REVISIONS;
        } else if (revStr != null && revStr.equals(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_All_Revisions"))) { //NOI18N
            bGetMore = true;
            limit = Mercurial.HG_FETCH_ALL_REVISIONS;
        }
        if (bGetMore && !bGettingRevisions) {
            fetchRevisionLimit = limit;
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
            HgProgressSupport hgProgressSupport = new HgProgressSupport() {
                public void perform() {
                    refreshRevisions();
                }
            };
            hgProgressSupport.start(rp, repository,
                    org.openide.util.NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetching_Revisions")); // NOI18N
        }
        return bGetMore;
    }

    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        // XXX attach Cancelable hook
        final ProgressHandle ph = ProgressHandleFactory.createHandle(getRefreshLabel()); // NOI18N
        try {
            revisions = new LinkedHashSet<String>();
            RepositoryRevision displayedRevision = getDisplayedRevision();
            if (displayedRevision == null) {
                revisions.add(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Tip_Revision")); // NOI18N
            } else {
                revisions.add(getRevisionLabel(displayedRevision));
            }
            ComboBoxModel targetsModel = new DefaultComboBoxModel(new Vector<String>(revisions));
            revisionsComboBox.setModel(targetsModel);
            refreshViewThread = Thread.currentThread();
            Thread.interrupted();  // clear interupted status
            ph.start();
            if (displayedRevision == null) {
                refreshRevisions();
            } else {
                revisionsComboBox.setEditable(false);
                changesetPanel1.setInfo(displayedRevision.getLog());
            }
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ph.finish();
                    refreshViewThread = null;
                }
            });
        }
    }

    private void refreshRevisions() {
        bGettingRevisions = true;
        revisions.remove(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_20_Revisions")); //NOI18N
        revisions.remove(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_50_Revisions")); //NOI18N
        revisions.remove(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_All_Revisions")); //NOI18N
        ComboBoxModel targetsModel = new DefaultComboBoxModel(new Vector<String>(revisions));
        revisionsComboBox.setModel(targetsModel);
        revisionsComboBox.setSelectedIndex(0);
        changesetPanel1.clearInfo();

        OutputLogger logger = OutputLogger.getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
        Set<File> setRoots = new HashSet<File>(Arrays.asList(roots));
        messages = HgCommand.getLogMessagesNoFileInfo(repository, setRoots, fetchRevisionLimit, logger);

        Set<String>  targetRevsSet = new LinkedHashSet<String>();

        int size;
        if( messages == null || messages.length == 0){
            size = 0;
            targetRevsSet.add(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Revision_Default")); // NOI18N
        }else{
            size = messages.length;
            int i = 0 ;
            while(i < size){
                targetRevsSet.add(messages[i].getRevision() + " (" + messages[i].getCSetShortID() + ")"); // NOI18N
                i++;
            }
        }
        if(targetRevsSet.size() > 0){
            targetRevsSet.add(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_20_Revisions")); //NOI18N
            targetRevsSet.add(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_50_Revisions")); //NOI18N
            targetRevsSet.add(NbBundle.getMessage(ChangesetPickerPanel.class, "MSG_Fetch_All_Revisions")); //NOI18N
        }
        targetsModel = new DefaultComboBoxModel(new Vector<String>(targetRevsSet));
        revisionsComboBox.setModel(targetsModel);

        if (targetRevsSet.size() > 0 ) {
            revisionsComboBox.setSelectedIndex(0);
        }
        this.revisions = targetRevsSet;
        bGettingRevisions = false;
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mercurial.ui.repository.ChangesetPanel changesetPanel1;
    protected final javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
    protected final javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox revisionsComboBox;
    protected javax.swing.JLabel revisionsLabel;
    // End of variables declaration//GEN-END:variables
    
}
