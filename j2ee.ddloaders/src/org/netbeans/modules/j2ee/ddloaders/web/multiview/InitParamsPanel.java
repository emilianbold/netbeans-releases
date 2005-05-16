/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel;
import org.openide.util.NbBundle;

/**
 *
 * @author  mk115033
 * Created on October 1, 2002, 3:52 PM
 */
public class InitParamsPanel extends DefaultTablePanel {
    private InitParamTableModel model;
    private Servlet servlet;
    private DDDataObject dObj;
    
    /** Creates new form InitParamsPanel */
    public InitParamsPanel(final DDDataObject dObj, final InitParamTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                int row = getTable().getSelectedRow();
                dObj.modelUpdatedFromUI();
                model.removeRow(row);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }

    void setModel(Servlet servlet, InitParam[] params) {
        model.setData(servlet,params);
        this.servlet=servlet;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            String[] labels = new String[]{
                NbBundle.getMessage(InitParamsPanel.class,"LBL_initParamName"),
                NbBundle.getMessage(InitParamsPanel.class,"LBL_initParamValue"),
                NbBundle.getMessage(InitParamsPanel.class,"LBL_description")
            };
            char[] mnem = new char[] {
                NbBundle.getMessage(InitParamsPanel.class,"LBL_initParamName_mnem").charAt(0),
                NbBundle.getMessage(InitParamsPanel.class,"LBL_initParamValue_mnem").charAt(0),
                NbBundle.getMessage(InitParamsPanel.class,"LBL_description_mnem").charAt(0)
            };
            SimpleDialogPanel.DialogDescriptor descriptor = new SimpleDialogPanel.DialogDescriptor(labels);
            if (!add) {
                String[] initValues = new String[] {
                    (String)model.getValueAt(row,0),
                    (String)model.getValueAt(row,1),
                    (String)model.getValueAt(row,2)
                };
                descriptor.setInitValues(initValues);
            }
            descriptor.setMnemonics(mnem);
            descriptor.setTextField(new boolean[]{true,true,false});
            final SimpleDialogPanel dialogPanel = new SimpleDialogPanel(descriptor);
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(InitParamsPanel.class,"TTL_InitParam"),add) {
                protected String validate() {
                    String[] values = dialogPanel.getValues();
                    String name = values[0];
                    String value = values[1];
                    if (name.length()==0) {
                        return NbBundle.getMessage(InitParamsPanel.class,"TXT_EmptyInitParamName");
                    } else {
                        InitParam[] params = servlet.getInitParam();
                        boolean exists=false;
                        for (int i=0;i<params.length;i++) {
                            if (row!=i && name.equals(params[i].getParamName())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(InitParamsPanel.class,"TXT_InitParamNameExists",name);
                        }
                    }
                    if (value.length()==0) {
                        return NbBundle.getMessage(InitParamsPanel.class,"TXT_EmptyInitParamValue");
                    }
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getTextComponents()[0].getDocument().addDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.show();
            dialogPanel.getTextComponents()[0].getDocument().removeDocumentListener(docListener);
            dialogPanel.getTextComponents()[1].getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                String[] values = dialogPanel.getValues();
                String name = values[0];
                String value = values[1];
                String description = values[2];
                if (add) model.addRow(new String[]{name,value,description});
                else model.editRow(row,new String[]{name,value,description});
            }
        }
    }
}
