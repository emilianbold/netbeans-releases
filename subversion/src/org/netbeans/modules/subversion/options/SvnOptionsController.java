/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public final class SvnOptionsController extends OptionsPanelController implements ActionListener {

    private final SvnOptionsPanel panel; 
    private final Repository repository;    
    private final AnnotationSettings labelsSettings;    
    
    public SvnOptionsController() {
        panel = new SvnOptionsPanel();
                
        repository = new Repository(SvnModuleConfig.getDefault().getRecentUrls(), false, true, false, true, 
                                    false, org.openide.util.NbBundle.getMessage(SvnOptionsController.class, "CTL_Repository_Location")); // NOI18N                        
        
        labelsSettings = new AnnotationSettings();
        
        panel.browseButton.addActionListener(this);
        panel.manageConnSettingsButton.addActionListener(this);
        panel.manageLabelsButton.addActionListener(this);        

    }
    
    public void update () {
        
        panel.executablePathTextField.setText(SvnModuleConfig.getDefault().getExecutableBinaryPath());        
        
        labelsSettings.update();
                
        repository.refreshUrlHistory(SvnModuleConfig.getDefault().getRecentUrls());
        
    }
    
    public void applyChanges () {        
                
        // executable
        SvnModuleConfig.getDefault().setExecutableBinaryPath(panel.executablePathTextField.getText());     
        // XXX only if value changed?
        // Subversion.setupSvnClientFactory(); this won't work anyway because the svnclientadapter doesn't allow more setups per client!
        
        // labels
        labelsSettings.applyChanges();
        SvnModuleConfig.getDefault().setAnnotationFormat(labelsSettings.getLabelsFormat());                             
        Subversion.getInstance().getAnnotator().refresh();
        Subversion.getInstance().refreshAllAnnotations();                
        
        // connection                      
        SvnModuleConfig.getDefault().setRecentUrls(repository.getRecentUrls());
        
    }
    
    public void cancel () {
        repository.refreshUrlHistory(SvnModuleConfig.getDefault().getRecentUrls());
    }
    
    public boolean isValid () {
        return true;        
    }
    
    public boolean isChanged () {
        return false; // NOI18N // XXX
    }
    
    public org.openide.util.HelpCtx getHelpCtx () {
        return new org.openide.util.HelpCtx("netbeans.optionsDialog.advanced.subversion");
    }

    public javax.swing.JComponent getComponent (org.openide.util.Lookup masterLookup) {
        return panel;
    }

    public void addPropertyChangeListener (java.beans.PropertyChangeListener l) {
        
    }

    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        
    }

    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == panel.browseButton) {
            onBrowseClick();
        } else if(evt.getSource() == panel.manageConnSettingsButton) {
            onManageConnClick();
        } else if(evt.getSource() == panel.manageLabelsButton) {
            onManageLabelsClick();
        } 
    }
    
    private File getExecutableFile() { 
        String execPath = panel.executablePathTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }
    
    private void onBrowseClick() {
        File oldFile = getExecutableFile();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(SvnOptionsController.class, "ACSD_BrowseFolder"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(SvnOptionsController.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
                for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);

        }
        fileChooser.addChoosableFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
            public String getDescription() {
                return NbBundle.getMessage(SvnOptionsController.class, "SVNExec");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.executablePathTextField.setText(f.getAbsolutePath());
        }
    }
    
    
    
    private void onManageConnClick() {        
        boolean ok = repository.show(
                NbBundle.getMessage(SvnOptionsController.class, "CTL_ManageConnections"),                 
                new HelpCtx(Repository.class));        
        if(!ok) {
            repository.refreshUrlHistory(SvnModuleConfig.getDefault().getRecentUrls());
        }
    }
    
    private void onManageLabelsClick() {
        showDialog(labelsSettings.getPanel(), NbBundle.getMessage(SvnOptionsController.class, "CTL_ManageLabels"), NbBundle.getMessage(SvnOptionsController.class, "ACSD_ManageLabels"), new HelpCtx(AnnotationSettings.class));
    }
    
    private boolean showDialog(JPanel panel, String title, String accesibleDescription, HelpCtx helpCtx) {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title); 
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(helpCtx);               
        dialogDescriptor.setValid(true);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);     
        dialog.getAccessibleContext().setAccessibleDescription(accesibleDescription);
        dialog.setVisible(true);
        
        return DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue());
    }

}
