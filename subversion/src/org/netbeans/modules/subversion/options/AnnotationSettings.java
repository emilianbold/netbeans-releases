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

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import org.netbeans.modules.subversion.Annotator;
import org.netbeans.modules.subversion.SvnModuleConfig;

/**
 *
 * @author Tomas Stupka
 */
public class AnnotationSettings implements ActionListener, AWTEventListener, ListSelectionListener {
    
    private final AnnotationSettingsPanel panel; 
    private JWindow labelsWindow;
    private LabelsPanel labelsPanel;     
    
    /** Creates a new instance of LabelsSettings */
    public AnnotationSettings() {
        panel = new AnnotationSettingsPanel();
                
        String tooltip = org.openide.util.NbBundle.getMessage(AnnotationSettings.class, "AnnotationSettingsPanel.annotationTextField.toolTipText", Annotator.LABELS);               
        panel.annotationTextField.setToolTipText(tooltip);        
        
        panel.labelsButton.addActionListener(this); 
        panel.upButton.addActionListener(this); 
        panel.downButton.addActionListener(this); 
        panel.newButton.addActionListener(this); 
        panel.removeButton.addActionListener(this); 
        panel.resetButton.addActionListener(this); 
    }
 
    JPanel getPanel() {
        return panel;
    }
    
    void update() {
        panel.annotationTextField.setText(SvnModuleConfig.getDefault().getAnnotationFormat());
        
        List<AnnotationExpression> exps = SvnModuleConfig.getDefault().getAnnotationExpresions();        
        
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableModel model = getModel();
        model.setColumnCount(2);
        if(exps.size() > 0) {
            model.setRowCount(exps.size());
            int r = -1;
            for (Iterator<AnnotationExpression> it = exps.iterator(); it.hasNext();) {
                AnnotationExpression annotationExpression = it.next();                
                r++;
                model.setValueAt(annotationExpression.getUrlExp(),        r, 0);
                model.setValueAt(annotationExpression.getAnnotationExp(), r, 1);
            }
        } else {
            onResetClick();
        }
    }

    void applyChanges() {
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
           Object urlExp        = model.getValueAt(r, 0);            
           Object annotationExp = model.getValueAt(r, 1);            
           
           int rNew = r - 1;
           model.setValueAt(model.getValueAt(rNew, 0), r, 0);            
           model.setValueAt(model.getValueAt(rNew, 1), r, 1);            
           
           model.setValueAt(urlExp,        rNew, 0);            
           model.setValueAt(annotationExp, rNew, 1);            
           
           listSelectionModel.setSelectionInterval(rNew, rNew);
        }
    }
    
    private void onDownClick() {
        ListSelectionModel listSelectionModel = getSelectionModel();
        int r = listSelectionModel.getMinSelectionIndex();                
        DefaultTableModel model = getModel();
        if(r > -1 && r < model.getRowCount() - 1) {                      
           Object urlExp =        model.getValueAt(r, 0);            
           Object annotationExp = model.getValueAt(r, 1);            
           
           int rNew = r + 1;
           model.setValueAt(model.getValueAt(rNew, 0), r, 0);            
           model.setValueAt(model.getValueAt(rNew, 1), r, 1);            
           
           model.setValueAt(urlExp,        rNew, 0);            
           model.setValueAt(annotationExp, rNew, 1);            
           
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
        DefaultTableModel model = getModel();
        model.setRowCount(1);
        model.setValueAt(".*/(branches|tags)/(.+?)/.*", 0, 0);
        model.setValueAt("\\2",                         0, 1);                    
    }
    
    private DefaultTableModel getModel() {
        return (DefaultTableModel) panel.expresionsTable.getModel();
    }

    private ListSelectionModel getSelectionModel() {
        return panel.expresionsTable.getSelectionModel();
    }
    
    private void onLabelsClick() {
        Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_EVENT_MASK);        
        if(labelsWindow  == null) {
            labelsPanel = new LabelsPanel();
            labelsWindow = new JWindow();
            DefaultListModel model = new DefaultListModel();    
        
            for (int i = 0; i < Annotator.LABELS.length; i++) {            
                model.addElement(Annotator.LABELS[i]);   
            }       
            labelsPanel.labelsList.setModel(model);        

            labelsWindow.add(labelsPanel);
            labelsWindow.pack();
            Point loc = panel.labelsButton.getLocationOnScreen();        
            labelsWindow.setLocation(new Point((int)loc.getX(), (int) (loc.getY() + panel.labelsButton.getHeight())));            
        
            labelsPanel.labelsList.getSelectionModel().addListSelectionListener(this);        
        }                
        labelsWindow.setVisible(true);              
    }

    public void eventDispatched(AWTEvent evt) {
        if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
            onClick(evt);
        }              
    }

    private void onClick(AWTEvent event) {
        Component component = (Component) event.getSource();
        Window w = SwingUtilities.windowForComponent(component);
        if (w != labelsWindow) shutdown();
    }

    private void shutdown() {
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        if(labelsWindow!=null) {
            labelsWindow.dispose();
            //labelsWindow = null;
        }        
    }

    String getLabelsFormat() {
        return panel.annotationTextField.getText();                     
    }
    
    public void valueChanged(ListSelectionEvent evt) {
        int idx = evt.getFirstIndex();
        String selection = (String) labelsPanel.labelsList.getModel().getElementAt(idx);
        
        shutdown(); 
        
        selection = "{" + selection + "}";
        
        String annotation = panel.annotationTextField.getText();
        int pos = panel.annotationTextField.getCaretPosition();
        if(pos < 0) pos = annotation.length();
        
        StringBuffer sb = new StringBuffer(annotation.length() + selection.length());
        sb.append(annotation.substring(0, pos));
        sb.append(selection);
        if(pos < annotation.length()) {
            sb.append(annotation.substring(pos, annotation.length()));
        }
        panel.annotationTextField.setText(sb.toString());
        panel.annotationTextField.requestFocus();
        panel.annotationTextField.setCaretPosition(pos + selection.length());
    }
    
}
