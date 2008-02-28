/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.mercurial.ui.diff;

import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.LinkedHashSet;
import javax.swing.SwingUtilities;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.mercurial.util.HgCommand;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.log.RepositoryRevision;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;


/**
 *
 * @author  Padraig O'Briain
 */
public class ExportDiffPanel extends javax.swing.JPanel implements ActionListener {

    private File                            repository;
    private RequestProcessor.Task           refreshViewTask;
    private Thread                          refreshViewThread;
    private static final RequestProcessor   rp = new RequestProcessor("MercurialExportDiff", 1);  // NOI18N
    private RepositoryRevision              repoRev;
    private static final int HG_REVISION_TARGET_LIMIT = 100;
    private File fileToDiff;

    /** Creates new form ExportDiffPanel */
    public ExportDiffPanel(File repo, RepositoryRevision repoRev, File fileToDiff) {
        this.fileToDiff = fileToDiff;
        this.repoRev = repoRev;
        repository = repo;
        refreshViewTask = rp.create(new RefreshViewTask());
        initComponents();
        if(fileToDiff != null){
            org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, NbBundle.getMessage(ExportDiffPanel.class, 
                    "ExportDiffPanel.revisionsLabel.text.forFileDiff")); // NOI18N
            exportHintLabel.setText(NbBundle.getMessage(ExportDiffPanel.class, 
                    "ExportDiffPanel.exportHintLabel.text.forFileDiff")); // NOI18N
        }
        browseButton.addActionListener(this);
        refreshViewTask.schedule(0);
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
        return outputFileTextField.getText();
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
        fileLabel = new javax.swing.JLabel();
        browseButton = new javax.swing.JButton();
        exportHintLabel = new javax.swing.JLabel();

        revisionsLabel.setLabelFor(revisionsComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(revisionsLabel, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.revisionsLabel.text")); // NOI18N

        fileLabel.setLabelFor(outputFileTextField);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.fileLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ExportDiffPanel.browseButtonl.text")); // NOI18N

        exportHintLabel.setForeground(java.awt.Color.gray);
        org.openide.awt.Mnemonics.setLocalizedText(exportHintLabel, org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "LBL_EXPORT_INFO")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(revisionsLabel)
                    .add(fileLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(outputFileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(exportHintLabel)
                    .add(revisionsComboBox, 0, 305, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(27, 27, 27)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(revisionsLabel)
                    .add(revisionsComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(exportHintLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 14, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileLabel)
                    .add(browseButton)
                    .add(outputFileTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(26, 26, 26))
        );

        revisionsComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ACSD_revisionsComboBox")); // NOI18N
        outputFileTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ACSD_outputFileTextField")); // NOI18N
        browseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ExportDiffPanel.class, "ACSD_browseButton")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    

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
                refreshViewThread = Thread.currentThread();
                Thread.interrupted();  // clear interupted status
                ph.start();
            }else{
                initislRevsSet.add(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Fetching_Revisions")); // NOI18N
                targetsModel = new DefaultComboBoxModel(new Vector<String>(initislRevsSet));
                revisionsComboBox.setModel(targetsModel);
                refreshViewThread = Thread.currentThread();
                Thread.interrupted();  // clear interupted status
                ph.start();

                refreshRevisions();
            }
            setDefaultOutputFile();
        } finally {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ph.finish();
                    refreshViewThread = null;
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
        outputFileTextField.setText(file.getAbsolutePath());
    }

    private void refreshRevisions() {
        java.util.List<String> targetRevsList = HgCommand.getRevisions(repository, HG_REVISION_TARGET_LIMIT); 

        Set<String>  targetRevsSet = new LinkedHashSet<String>();

        int size;
        if( targetRevsList == null){
            size = 0;
            targetRevsSet.add(NbBundle.getMessage(ExportDiffPanel.class, "MSG_Revision_Default")); // NOI18N
        }else{
            size = targetRevsList.size();
            int i = 0 ;
            while(i < size){
                targetRevsSet.add(targetRevsList.get(i));
                i++;
            }
        }
        ComboBoxModel targetsModel = new DefaultComboBoxModel(new Vector<String>(targetRevsSet));
        revisionsComboBox.setModel(targetsModel);

        if (targetRevsSet.size() > 0 ) {
            revisionsComboBox.setSelectedIndex(0);
        }
    }

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == browseButton) {
            onBrowseClick();
        }
    }

    private void onBrowseClick() {
        File oldFile = null;
        final JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(ExportDiffPanel.class, "ACSD_BrowseFolder"), oldFile);   // NO I18N
        fileChooser.setDialogTitle(NbBundle.getMessage(ExportDiffPanel.class, "Browse_title"));                                            // NO I18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.setApproveButtonMnemonic(NbBundle.getMessage(ExportDiffPanel.class, "OK_Button").charAt(0));                      // NO I18N
        fileChooser.setApproveButtonText(NbBundle.getMessage(ExportDiffPanel.class, "OK_Button"));                                        // NO I18N
        fileChooser.setCurrentDirectory(new File(HgModuleConfig.getDefault().getExportFolder()));
        DialogDescriptor dd = new DialogDescriptor(fileChooser, NbBundle.getMessage(ExportDiffPanel.class, "Browse_title"));              // NO I18N
        dd.setOptions(new Object[0]);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        fileChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String state = e.getActionCommand();
                if (state.equals(JFileChooser.APPROVE_SELECTION)) {
                    File f = fileChooser.getSelectedFile();
                    if (f != null) {
                        outputFileTextField.setText(f.getAbsolutePath());
                    }
                }
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    private class RefreshViewTask implements Runnable {
        public void run() {
            setupModels();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JLabel exportHintLabel;
    private javax.swing.JLabel fileLabel;
    final javax.swing.JTextField outputFileTextField = new javax.swing.JTextField();
    private javax.swing.JComboBox revisionsComboBox;
    private javax.swing.JLabel revisionsLabel;
    // End of variables declaration//GEN-END:variables
    
}
