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

package org.netbeans.modules.mercurial.options;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import org.netbeans.modules.mercurial.Mercurial;
import org.netbeans.modules.mercurial.MercurialAnnotator;
import org.netbeans.modules.mercurial.HgModuleConfig;
import org.netbeans.modules.mercurial.ui.properties.PropertiesPanel;
import org.netbeans.modules.mercurial.ui.properties.PropertiesTable;
import org.netbeans.modules.mercurial.ui.properties.PropertiesTableModel;
import org.netbeans.spi.options.OptionsPanelController;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

final class MercurialOptionsPanelController extends OptionsPanelController implements ActionListener {
    
    private MercurialPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    
    public MercurialOptionsPanelController() {

        panel = new MercurialPanel(this);
        panel.execPathBrowseButton.addActionListener(this);
        panel.exportFilenameBrowseButton.addActionListener(this);

        String tooltip = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.annotationTextField.toolTipText", MercurialAnnotator.LABELS); // NOI18N

        panel.annotationTextField.setToolTipText(tooltip);
        panel.addButton.addActionListener(this);
        panel.manageButton.addActionListener(this);
    }

    public void update() {
        getPanel().load();
        changed = false;
    }
    
    public void applyChanges() {
        if (!validateFields()) return;

        getPanel().store();
        // {folder} variable setting
        Mercurial.getInstance().getMercurialAnnotator().refresh();
        Mercurial.getInstance().refreshAllAnnotations();

        changed = false;
    }
    
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }
    
    public boolean isValid() {
        return getPanel().valid();
    }
    
    public boolean isChanged() {
        return changed;
    }
    
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set  // NOI18N
    }
    
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == panel.execPathBrowseButton) {
            onExecPathBrowseClick();
        } else if (evt.getSource() == panel.exportFilenameBrowseButton) {
            onExportFilenameBrowseClick();
        } else if (evt.getSource() == panel.addButton) {
            onAddClick();
        } else if (evt.getSource() == panel.manageButton) {
            onManageClick();
        }
    }

    private File getExportFile() {
        String execPath = panel.exportFilenameTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }

    private File getExecutableFile() {
        String execPath = panel.executablePathTextField.getText();
        return FileUtil.normalizeFile(new File(execPath));
    }

    private Boolean validateFields() {
        String username = panel.userNameTextField.getText();
        if (!HgModuleConfig.getDefault().isUserNameValid(username)) {
            JOptionPane.showMessageDialog(null,
                                          NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_USER_NAME_TEXT"), // NOI18N
                                          NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_FIELD_TITLE"), // NOI18N
                                          JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String execpath = panel.executablePathTextField.getText();
        if (!HgModuleConfig.getDefault().isExecPathValid(execpath)) {
            JOptionPane.showMessageDialog(null,
                                          NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_EXEC_PATH_TEXT"), // NOI18N
                                          NbBundle.getMessage(MercurialPanel.class, "MSG_WARN_FIELD_TITLE"), // NOI18N
                                          JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private void onExportFilenameBrowseClick() {
        File oldFile = getExecutableFile();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(MercurialOptionsPanelController.class, "ACSD_ExportBrowseFolder"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(MercurialOptionsPanelController.class, "ExportBrowse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        FileFilter[] old = fileChooser.getChoosableFileFilters();
        for (int i = 0; i < old.length; i++) {
            FileFilter fileFilter = old[i];
            fileChooser.removeChoosableFileFilter(fileFilter);
        }
        fileChooser.showDialog(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.exportFilenameTextField.setText(f.getAbsolutePath());
        }
    }

    private void onExecPathBrowseClick() {
        File oldFile = getExportFile();
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(MercurialOptionsPanelController.class, "ACSD_BrowseFolder"), oldFile);   // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(MercurialOptionsPanelController.class, "Browse_title"));                                            // NOI18N
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
                return NbBundle.getMessage(MercurialOptionsPanelController.class, "MercurialExec");// NOI18N
            }
        });
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            panel.executablePathTextField.setText(f.getAbsolutePath());
        }
    }

    private MercurialPanel getPanel() {
        if (panel == null) {
            panel = new MercurialPanel(this);
        }
        return panel;
    }
    
    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
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
        List<LabelVariable> variables = new ArrayList<LabelVariable>(MercurialAnnotator.LABELS.length);
        for (int i = 0; i < MercurialAnnotator.LABELS.length; i++) {   
            LabelVariable variable = new LabelVariable(
                    MercurialAnnotator.LABELS[i], 
                    "{" + MercurialAnnotator.LABELS[i] + "} - " + NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.label." + MercurialAnnotator.LABELS[i]) // NOI18N
            );
            variables.add(variable);   
        }       
        labelsPanel.labelsList.setListData(variables.toArray(new LabelVariable[variables.size()]));                
                
        String title = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.labelVariables.title"); // NOI18N
        String acsd = NbBundle.getMessage(MercurialPanel.class, "MercurialPanel.labelVariables.acsd"); // NOI18N

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
            
            String variable = ""; // NOI18N
            for (int i = 0; i < selection.length; i++) {
                variable += "{" + ((LabelVariable)selection[i]).getVariable() + "}"; // NOI18N
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
    
    private void onManageClick() {
        final PropertiesPanel panel = new PropertiesPanel();

        final PropertiesTable propTable;

        propTable = new PropertiesTable(PropertiesTable.PROPERTIES_COLUMNS, new
String[] { PropertiesTableModel.COLUMN_NAME_VALUE});

        panel.setPropertiesTable(propTable);

        JComponent component = propTable.getComponent();

        panel.propsPanel.setLayout(new BorderLayout());

        panel.propsPanel.add(component, BorderLayout.CENTER);

        HgExtProperties hgProperties = new HgExtProperties(panel, propTable, null) ;

        DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_PropertiesDialog_Title", null), true, null); // NOI18N
        final JButton okButton =  new JButton(NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_Properties_Action_OK")); // NOI18N
        dd.setOptions(new Object[] {okButton,
                                    NbBundle.getMessage(MercurialOptionsPanelController.class, "CTL_Properties_Action_Cancel")}); // NOI18N
        dd.setHelpCtx(new HelpCtx(MercurialOptionsPanelController.class));
        panel.putClientProperty("contentTitle", null);  // NOI18N
        panel.putClientProperty("DialogDescriptor", dd); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.pack();
        dialog.setVisible(true);
        if (dd.getValue() == okButton) {
            hgProperties.setProperties();
        }
    }
}
