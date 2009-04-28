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
package org.netbeans.modules.mercurial.ui.diff;

import java.awt.BorderLayout;
import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.HgProgressSupport;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.OutputLogger;
import org.netbeans.modules.mercurial.ui.log.HgLogMessage;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.netbeans.modules.versioning.util.ExportDiffSupport.AbstractExportDiffPanel;


/**
 *
 * @author  Padraig O'Briain
 */
public class ExportDiffPanel extends javax.swing.JPanel {

    private File                            repository;
    private RequestProcessor.Task           refreshViewTask;
    private static final RequestProcessor   rp = new RequestProcessor("MercurialExportDiff", 1);  // NOI18N
    private RepositoryRevision              repoRev;
    private File fileToDiff;
    private HgLogMessage[] messages;
    private int fetchRevisionLimit = Mercurial.HG_NUMBER_TO_FETCH_DEFAULT;
    private boolean bGettingRevisions = false;
    private File [] roots;

    AbstractExportDiffPanel p;

    /** Creates new form ExportDiffPanel */
    public ExportDiffPanel(File repo, RepositoryRevision repoRev, File [] roots, File fileToDiff) {
        this.fileToDiff = fileToDiff;
        this.repoRev = repoRev;
        this.roots = roots;
        this.repository = repo;
        this.refreshViewTask = rp.create(new RefreshViewTask());
        initComponents();
        revisionsComboBox.setMaximumRowCount(Mercurial.HG_MAX_REVISION_COMBO_SIZE);
        if(fileToDiff != null){
            org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, NbBundle.getMessage(ExportDiffPanel.class, 
                    "ExportDiffPanel.revisionsLabel.text.forFileDiff")); // NOI18N
            exportHintLabel.setText(NbBundle.getMessage(ExportDiffPanel.class, 
                    "ExportDiffPanel.exportHintLabel.text.forFileDiff")); // NOI18N
        }
    }

    public String getSelectedRevision() {
        String revStr = (String) revisionsComboBox.getSelectedItem();
        if (revStr != null) {
            if (revStr.equals(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Fetching_Revisions"))) { // NOI18N
                revStr = "tip"; // NOI18N
            } else {
                revStr = revStr.substring(0, revStr.indexOf(" ")); // NOI18N
            }
        }
        return revStr;
    }

    public String getOutputFileName() {
        return p.getOutputFileText();
    }

    void setInsidePanel(AbstractExportDiffPanel insidePanel) {
        p = insidePanel;
        jPanel1.setLayout(new BorderLayout());
        jPanel1.add(insidePanel, BorderLayout.NORTH);
        refreshViewTask.schedule(0);
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
        exportHintLabel = new javax.swing.JLabel();
        changesetPanel1 = new org.netbeans.modules.mercurial.ui.repository.ChangesetPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();

        revisionsLabel.setLabelFor(revisionsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.revisionsLabel.text")); // NOI18N

        revisionsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                revisionsComboBoxActionPerformed(evt);
            }
        });

        exportHintLabel.setForeground(java.awt.Color.gray);
        org.openide.awt.Mnemonics.setLocalizedText(exportHintLabel, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "LBL_EXPORT_INFO")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.jLabel1.text")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 562, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(changesetPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, exportHintLabel)))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(34, 34, 34)
                        .add(revisionsLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(revisionsComboBox, 0, 380, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(20, 20, 20)
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(exportHintLabel)
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(revisionsLabel))
                .add(18, 18, 18)
                .add(changesetPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        revisionsComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ACSD_revisionsComboBox")); // NOI18N
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

        if (revStr != null && revStr.equals(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_20_Revisions"))) {
            bGetMore = true;
            limit = Mercurial.HG_FETCH_20_REVISIONS;
        } else if (revStr != null && revStr.equals(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_50_Revisions"))) {
            bGetMore = true;
            limit = Mercurial.HG_FETCH_50_REVISIONS;
        } else if (revStr != null && revStr.equals(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_All_Revisions"))) {
            bGetMore = true;
            limit = Mercurial.HG_FETCH_ALL_REVISIONS;
        }
        if (bGetMore && !bGettingRevisions) {
            fetchRevisionLimit = limit;
            RequestProcessor rp = Mercurial.getInstance().getRequestProcessor(repository);
            HgProgressSupport hgProgressSupport = new HgProgressSupport() {
                public void perform() {
                    changesetPanel1.clearInfo();
                    refreshRevisions();
                }
            };
            hgProgressSupport.start(rp, repository,
                    org.openide.util.NbBundle.getMessage(Mercurial.class, "MSG_Fetching_Revisions")); // NOI18N
        }
        return bGetMore;
    }
    
    /**
     * Must NOT be run from AWT.
     */
    private void setupModels() {
        // XXX attach Cancelable hook
        final ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Fetching_Revisions")); // NOI18N
        try {
            Set<String>  initislRevsSet = new LinkedHashSet<String>();
            ComboBoxModel targetsModel;
            if(repoRev != null){
                String revStr = repoRev.getLog().getRevision() + " (" + repoRev.getLog().getCSetShortID() + ")";// NOI18N
                if(fileToDiff !=null)
                    revStr = fileToDiff.getName() + " - " + revStr ;// NOI18N
                initislRevsSet.add(revStr); 
                targetsModel = new DefaultComboBoxModel(new Vector<String>(initislRevsSet));              
                revisionsComboBox.setModel(targetsModel);
                revisionsComboBox.setEditable(false);
                setDefaultOutputFile();
                changesetPanel1.setInfo(repoRev.getLog());
                Thread.interrupted();  // clear interupted status
                ph.start();
            }else{
                initislRevsSet.add(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Fetching_Revisions")); // NOI18N
                targetsModel = new DefaultComboBoxModel(new Vector<String>(initislRevsSet));
                revisionsComboBox.setModel(targetsModel);
                setDefaultOutputFile();
                Thread.interrupted();  // clear interupted status
                ph.start();

                refreshRevisions();
            }
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ph.finish();
                }
            });
        }
    }

    private void setDefaultOutputFile() {
        String folderName = HgModuleConfig.getDefault().getExportFolder();
        String fileName;
        if (fileToDiff != null && repoRev != null && repoRev.getLog() != null && repository != null) { //"<filename-ext>_%b_%r_%h"
            fileName = fileToDiff.getName().replace('.', '-') + "_" +  //NOI18N
                    repoRev.getLog().getRevision() + "_" +  //NOI18N
                    repoRev.getLog().getCSetShortID(); //NOI18N
        }else if (repoRev != null && repoRev.getLog() != null && repository != null){
            fileName = HgModuleConfig.getDefault().getExportFilename().replace("%b", repository.getName()); //NOI18N
            fileName = fileName.replace("%r", repoRev.getLog().getRevision()); //NOI18N
            fileName = fileName.replace("%h", repoRev.getLog().getCSetShortID()); //NOI18N
        }else if (repository != null){
            fileName = HgModuleConfig.getDefault().getExportFilename().replace("%b", repository.getName()); //NOI18N
        }else{
            fileName = HgModuleConfig.getDefault().getExportFilename();            
        }
        File file = new File(folderName, fileName + ".patch");  //NOI18N
        p.setOutputFileText(file.getAbsolutePath());
    }

    private void refreshRevisions() {
        bGettingRevisions = true;
        OutputLogger logger = OutputLogger.getLogger(Mercurial.MERCURIAL_OUTPUT_TAB_TITLE);
        Set<File> setRoots = new HashSet<File>(Arrays.asList(roots));        
        messages = HgCommand.getLogMessagesNoFileInfo(repository, setRoots, fetchRevisionLimit, logger);

        Set<String>  targetRevsSet = new LinkedHashSet<String>();
        int size;
        if( messages == null){
            size = 0;
            targetRevsSet.add(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Revision_Default")); // NOI18N
        }else{
            size = messages.length;
            int i = 0 ;
            while(i < size){
                targetRevsSet.add(messages[i].getRevision() + " (" + messages[i].getCSetShortID() + ")"); // NOI18N
                i++;
            }
        }
        if(targetRevsSet.size() > 0){
            targetRevsSet.add(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_20_Revisions"));
            targetRevsSet.add(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_50_Revisions"));
            targetRevsSet.add(NbBundle.getMessage(Mercurial.class, "MSG_Fetch_All_Revisions"));
        }
        ComboBoxModel targetsModel = new DefaultComboBoxModel(new Vector<String>(targetRevsSet));
        revisionsComboBox.setModel(targetsModel);

        if (targetRevsSet.size() > 0 ) {
            revisionsComboBox.setSelectedIndex(0);
        }
        bGettingRevisions = false;
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.netbeans.modules.mercurial.ui.repository.ChangesetPanel changesetPanel1;
    private javax.swing.JLabel exportHintLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JComboBox revisionsComboBox;
    private javax.swing.JLabel revisionsLabel;
    // End of variables declaration//GEN-END:variables
    
}
