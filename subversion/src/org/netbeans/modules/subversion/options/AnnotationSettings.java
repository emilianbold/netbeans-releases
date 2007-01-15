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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.subversion.Annotator;
import org.netbeans.modules.subversion.SvnModuleConfig;
import java.util.regex.Pattern;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class AnnotationSettings implements ActionListener, TableModelListener {
    
    private final AnnotationSettingsPanel panel; 
    private DialogDescriptor dialogDescriptor;
    private boolean valid;
    
    public AnnotationSettings() {
        panel = new AnnotationSettingsPanel();
                
        String tooltip = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettingsPanel.annotationTextField.toolTipText", Annotator.LABELS);               
        panel.annotationTextField.setToolTipText(tooltip);        
        
        panel.labelsButton.addActionListener(this); 
        panel.upButton.addActionListener(this); 
        panel.downButton.addActionListener(this); 
        panel.newButton.addActionListener(this); 
        panel.removeButton.addActionListener(this); 
        panel.resetButton.addActionListener(this); 
        
        panel.warningLabel.setVisible(false);
        
        getModel().addTableModelListener(this); 
    }
 
    JPanel getPanel() {
        return panel;
    }
        
    void show() {
        
        String title = NbBundle.getMessage(SvnOptionsController.class, "CTL_ManageLabels");
        String accesibleDescription = NbBundle.getMessage(SvnOptionsController.class, "ACSD_ManageLabels");
        HelpCtx helpCtx = new HelpCtx(AnnotationSettings.class);
        
        dialogDescriptor = new DialogDescriptor(panel, title);
        dialogDescriptor.setModal(false);
        dialogDescriptor.setHelpCtx(helpCtx);
        dialogDescriptor.setValid(valid);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(accesibleDescription);
        //dialog.setModal(false);
        dialog.setAlwaysOnTop(false);
        dialog.setVisible(true);                
    }
    
    void update() {
        reset(SvnModuleConfig.getDefault().getAnnotationFormat(), SvnModuleConfig.getDefault().getAnnotationExpresions());
    }

    void applyChanges() {
        SvnModuleConfig.getDefault().setAnnotationFormat(panel.annotationTextField.getText());                                     
        
        TableModel model = panel.expresionsTable.getModel();
        List<AnnotationExpression> exps = new ArrayList<AnnotationExpression>(model.getRowCount());        
        for (int r = 0; r < model.getRowCount(); r++) {
            String urlExp = (String) model.getValueAt(r, 0);
            if(urlExp.trim().equals("")) {
                continue;
            }
            String annotationExp = (String) model.getValueAt(r, 1);            
            exps.add(new AnnotationExpression(urlExp, annotationExp));
        }
        SvnModuleConfig.getDefault().setAnnotationExpresions(exps);        
    }    
    
    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == panel.labelsButton) {
            onLabelsClick();
        } else if (evt.getSource() == panel.upButton) {
            onUpClick();
        } else if (evt.getSource() == panel.downButton) {
            onDownClick();
        } else if (evt.getSource() == panel.newButton) {
            onNewClick();
        } else  if (evt.getSource() == panel.removeButton) {
            onRemoveClick();
        } else if (evt.getSource() == panel.resetButton) {
            onResetClick();
        }
    }   
    
    private void onUpClick() {
        ListSelectionModel listSelectionModel = getSelectionModel();
        int r = listSelectionModel.getMinSelectionIndex();        
        if(r > 0) {
            DefaultTableModel model = getModel();
            int rNew = r - 1;
            model.moveRow(r, r, rNew) ;
            listSelectionModel.setSelectionInterval(rNew, rNew);
        }
    }
    
    private void onDownClick() {
        ListSelectionModel listSelectionModel = getSelectionModel();
        int r = listSelectionModel.getMinSelectionIndex();                
        DefaultTableModel model = getModel();
        if(r > -1 && r < model.getRowCount() - 1) {     
           int rNew = r + 1;
           model.moveRow(r, r, rNew) ;
           listSelectionModel.setSelectionInterval(rNew, rNew);
        }        
    }
    
    private void onNewClick() {
         int r = getSelectionModel().getMinSelectionIndex();    
         if(r < 0) {
             getModel().addRow(      new String[] {"", ""});
         } else {
             getModel().insertRow(r, new String[] {"", ""});
         }
    }    
    
    private void onRemoveClick() {        
        ListSelectionModel selectionModel = getSelectionModel();
        int r = selectionModel.getMinSelectionIndex();
        if(r > -1) {
            getModel().removeRow(r);
        }
        int size = getModel().getRowCount();
        if(size > 0) {            
            if (r > size - 1) {
                r = size - 1;
            } 
            selectionModel.setSelectionInterval(r, r);    
        }
    }
        
    private void onResetClick() {
        reset(SvnModuleConfig.getDefault().getDefaultAnnotationFormat(), SvnModuleConfig.getDefault().getDefaultAnnotationExpresions());               
    }
    
    private void reset(String annotationformat, List<AnnotationExpression> exps) {
        panel.annotationTextField.setText(annotationformat);        
                
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel model = getModel();
        model.setColumnCount(2);
        model.setRowCount(exps.size());
        int r = -1;
        for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
            AnnotationExpression annotationExpression = it.next();                
            r++;
            model.setValueAt(annotationExpression.getUrlExp(),        r, 0);
            model.setValueAt(annotationExpression.getAnnotationExp(), r, 1);
        }        
    }
    
    private DefaultTableModel getModel() {
        return (DefaultTableModel) panel.expresionsTable.getModel();
    }

    private ListSelectionModel getSelectionModel() {
        return panel.expresionsTable.getSelectionModel();
    }
    
    private void onLabelsClick() {
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
        
        if(showDialog(labelsPanel, title, acsd)) {
            
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

    public void tableChanged(TableModelEvent evt) {
        if (evt.getType() == TableModelEvent.UPDATE) {
            validateTable(evt.getFirstRow(), evt.getColumn());
        }
    }

    private void validateTable(int r, int c) {
        
        if(r < 0 || c != 0) {
            return;
        }
        
        valid = true;     
        String pattern = (String) getModel().getValueAt(r, c);
        try {
            Pattern.compile(pattern);                                                       
        } catch (Exception e) {
            valid = false;
        }
        
        if(valid) {
            panel.warningLabel.setVisible(false);                
        } else {
            String label = NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettingsPanel.warningLabel.text", pattern);
            panel.warningLabel.setText(label);
            panel.warningLabel.setVisible(true);            
        }
        if(dialogDescriptor != null) {
            dialogDescriptor.setValid(valid);
        }
    }
    
    private boolean showDialog(JPanel panel, String title, String accesibleDescription) {
        DialogDescriptor dialogDescriptor = new DialogDescriptor(panel, title);
        dialogDescriptor.setModal(true);
        dialogDescriptor.setValid(true);
        
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(accesibleDescription);
        dialog.setVisible(true);
        
        return DialogDescriptor.OK_OPTION.equals(dialogDescriptor.getValue());
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
}
