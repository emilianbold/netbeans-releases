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
package org.netbeans.modules.subversion.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.modules.subversion.Annotator;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnClientFactory;
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
    private static final HashSet<String> allowedExecutables = new HashSet<String>(Arrays.asList(new String[] {"svn", "svn.exe", "libsvnjavahl-1.dll", "libsvnjavahl-1.so"} )); //NOI18N
        
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
        panel.cbOpenOutputWindow.setSelected(SvnModuleConfig.getDefault().getAutoOpenOutput());
        annotationSettings.update();
        repository.refreshUrlHistory();
        panel.excludeNewFiles.setSelected(SvnModuleConfig.getDefault().getExludeNewFiles());
        panel.prefixRepositoryPath.setSelected(SvnModuleConfig.getDefault().isRepositoryPathPrefixed());
        
    }
    
    public void applyChanges() {                                 
        // executable
        if(!panel.executablePathTextField.getText().equals(SvnModuleConfig.getDefault().getExecutableBinaryPath())) {
            SvnClientFactory.reset();
        }
        SvnModuleConfig.getDefault().setExecutableBinaryPath(panel.executablePathTextField.getText());                
        SvnModuleConfig.getDefault().setAnnotationFormat(panel.annotationTextField.getText());
        SvnModuleConfig.getDefault().setAutoOpenOutputo(panel.cbOpenOutputWindow.isSelected());
        SvnModuleConfig.getDefault().setExcludeNewFiles(panel.excludeNewFiles.isSelected());
        SvnModuleConfig.getDefault().setRepositoryPathPrefixed(panel.prefixRepositoryPath.isSelected());

        // {folder} variable setting
        annotationSettings.applyChanges();
        Subversion.getInstance().getAnnotator().refresh();
        Subversion.getInstance().refreshAllAnnotations();        
    }
    
    public void cancel() {
        repository.refreshUrlHistory();
    }
    
    public boolean isValid() {
        return true;
    }
    
    public boolean isChanged() {        
        return !panel.executablePathTextField.getText().equals(SvnModuleConfig.getDefault().getExecutableBinaryPath()) || 
               !panel.annotationTextField.getText().equals(SvnModuleConfig.getDefault().getAnnotationFormat()) || 
               repository.isChanged() || 
               annotationSettings.isChanged();
    }
        
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx(getClass());
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
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || allowedExecutables.contains(f.getName());
            }
            @Override
            public String getDescription() {
                return NbBundle.getMessage(SvnOptionsController.class, "FileChooser.SvnExecutables.desc"); //NOI18N
            }
        });
        fileChooser.showDialog(panel, NbBundle.getMessage(SvnOptionsController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            if (f.isFile()) {
                f = f.getParentFile();
            }
            panel.executablePathTextField.setText(f.getAbsolutePath());
        }
    }
    
    private void onManageConnClick() {
        boolean ok = repository.show(NbBundle.getMessage(SvnOptionsController.class, "CTL_ManageConnections"), new HelpCtx(Repository.class), true);
        if(ok) {            
            repository.storeRecentUrls();
        } else {    
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
         
        @Override
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
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    dialog.setVisible(false);
                }
            }        
        });                 
        
        dialog.setVisible(true);
        
        if(DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue())) {
            
            Object[] selection = labelsPanel.labelsList.getSelectedValues();
            
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
