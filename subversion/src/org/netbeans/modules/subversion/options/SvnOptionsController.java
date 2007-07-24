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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.subversion.Annotator;
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
    private final AnnotationSettings annotationSettings;            
        
    public SvnOptionsController() {        
        
        int repositoryModeMask = Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_REMOVE;
        String title = org.openide.util.NbBundle.getMessage(SvnOptionsController.class, "CTL_Repository_Location");
        repository = new Repository(repositoryModeMask, title); // NOI18N
        
        annotationSettings = new AnnotationSettings();
        
        panel = new SvnOptionsPanel();
        panel.browseButton.addActionListener(this);
        panel.manageConnSettingsButton.addActionListener(this);
        panel.manageLabelsButton.addActionListener(this);
        
        String tooltip = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettingsPanel.annotationTextField.toolTipText", Annotator.LABELS);               
        panel.annotationTextField.setToolTipText(tooltip);                
        panel.addButton.addActionListener(this);         
    }
    
    public void update() {
        
        panel.executablePathTextField.setText(SvnModuleConfig.getDefault().getExecutableBinaryPath());
        panel.annotationTextField.setText(SvnModuleConfig.getDefault().getAnnotationFormat());                   
                      
        annotationSettings.update();
        repository.refreshUrlHistory();
        
    }
    
    public void applyChanges() {
                                 
        // executable
        SvnModuleConfig.getDefault().setExecutableBinaryPath(panel.executablePathTextField.getText());                
        SvnModuleConfig.getDefault().setAnnotationFormat(panel.annotationTextField.getText());            
        
        // {folder} variable setting
        annotationSettings.applyChanges();
        Subversion.getInstance().getAnnotator().refresh();
        Subversion.getInstance().refreshAllAnnotations();
        
        // connection
        repository.storeRecentUrls();
    }
    
    public void cancel() {
        repository.refreshUrlHistory();
    }
    
    public boolean isValid() {
        return true;
    }
    
    public boolean isChanged() {
        return false; // NOI18N // XXX
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("netbeans.optionsDialog.advanced.subversion");
    }
    
    public javax.swing.JComponent getComponent(org.openide.util.Lookup masterLookup) {
        return panel;
    }
    
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        
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
        } else if (evt.getSource() == panel.addButton) {
            onAddClick();
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
        fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.executablePathTextField.setText(f.getAbsolutePath());
        }
    }
    
    private void onManageConnClick() {
        boolean ok = repository.show(NbBundle.getMessage(SvnOptionsController.class, "CTL_ManageConnections"), new HelpCtx(Repository.class), true);
        if(!ok) {
            repository.refreshUrlHistory();
        }
    }
    
    private void onManageLabelsClick() {     
        String labelFormat = panel.annotationTextField.getText().replaceAll(" ", "");        
        annotationSettings.show(labelFormat != null && labelFormat.indexOf("{folder}") > -1);                
    }            
    private class LabelVariable {
        private String description;
        private String variable;
         
        public LabelVariable(String variable, String description) {
            this.description = description;
            this.variable = variable;
        }
         
        public String toString() {
            return description;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getVariable() {
            return variable;
        }
    }
    
    private void onAddClick() {
        LabelsPanel labelsPanel = new LabelsPanel();
        List<LabelVariable> variables = new ArrayList<LabelVariable>(Annotator.LABELS.length);
        for (int i = 0; i < Annotator.LABELS.length; i++) {   
            LabelVariable variable = new LabelVariable(
                    Annotator.LABELS[i], 
                    "{" + Annotator.LABELS[i] + "} - " + NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.label." + Annotator.LABELS[i])
            );
            variables.add(variable);   
        }       
        labelsPanel.labelsList.setListData(variables.toArray(new LabelVariable[variables.size()]));                
                
        String title = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.labelVariables.title");
        String acsd = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettings.labelVariables.acsd");

        DialogDescriptor dialogDescriptor = new DialogDescriptor(labelsPanel, title);
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(true);
        
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        
        labelsPanel.labelsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    dialog.setVisible(false);
                }
            }        
        });                 
        
        dialog.setVisible(true);
        
        if(DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue())) {
            
            Object[] selection = (Object[])labelsPanel.labelsList.getSelectedValues();
            
            String variable = "";
            for (int i = 0; i < selection.length; i++) {
                variable += "{" + ((LabelVariable)selection[i]).getVariable() + "}";
            }

            String annotation = panel.annotationTextField.getText();

            int pos = panel.annotationTextField.getCaretPosition();
            if(pos < 0) pos = annotation.length();

            StringBuffer sb = new StringBuffer(annotation.length() + variable.length());
            sb.append(annotation.substring(0, pos));
            sb.append(variable);
            if(pos < annotation.length()) {
                sb.append(annotation.substring(pos, annotation.length()));
            }
            panel.annotationTextField.setText(sb.toString());
            panel.annotationTextField.requestFocus();
            panel.annotationTextField.setCaretPosition(pos + variable.length());            
            
        }        
    }        
    
}
