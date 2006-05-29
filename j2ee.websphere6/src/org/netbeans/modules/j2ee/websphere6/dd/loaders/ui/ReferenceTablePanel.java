/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import org.netbeans.modules.j2ee.websphere6.dd.beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;

import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.util.NbBundle;
import javax.swing.JComboBox;
/**
 * ReferenceTablePanel - panel containing table for EJB references
 *
 *
 * @author dlm198383
 */
public class ReferenceTablePanel extends DefaultTablePanel implements DDXmiConstants{
    private ReferencesTableModel model;
    private EjbBindingsType ejbBinding;
    private WSMultiViewDataObject dObj;
    
    
    /** Creates new form ContextParamPanel */
    public ReferenceTablePanel(final WSMultiViewDataObject dObj, final ReferencesTableModel model) {
        super(model);
        this.model=model;
        this.dObj=dObj;
    
        
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                dObj.modelUpdatedFromUI();
                //dObj.setChangedFromUI(true);
                dObj.setChangedFromUI(false);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }
    
    void setModel(EjbBindingsType ejbBinding, CommonRef []params) {
        this.ejbBinding=ejbBinding;
        model.setData(ejbBinding,params);
    }
    
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            final int row = (add?-1:getTable().getSelectedRow());
            
            final ReferencePanel dialogPanel = new ReferencePanel();
            if(!add) {
                String type=(String)model.getValueAt(row,0);
                if(type.equals(BINDING_REFERENCE_TYPES[0])) {
                    dialogPanel.getTypeComboBox().setSelectedIndex(0);
                }else if(type.equals(BINDING_REFERENCE_TYPES[1])) {
                    dialogPanel.getTypeComboBox().setSelectedIndex(1);
                } else if(type.equals(BINDING_REFERENCE_TYPES[2])) {
                    dialogPanel.getTypeComboBox().setSelectedIndex(2);
                }
                dialogPanel.getIdField().setText((String)model.getValueAt(row,1));
                dialogPanel.getJndiNameField().setText((String)model.getValueAt(row,2));
                dialogPanel.getHrefField().setText((String)model.getValueAt(row,3));
                dialogPanel.getTypeComboBox().setEnabled(false);
            }
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ReferenceTablePanel.class,"TTL_PageParam"),add) {
                protected String validate() {
                    String id = dialogPanel.getIdField().getText().trim();
                    String jndiName = dialogPanel.getJndiNameField().getText().trim();
                    String href = dialogPanel.getHrefField().getText().trim();
                    if (id.length()==0) {
                        return NbBundle.getMessage(ReferenceTablePanel.class,"TXT_EmptyReferenceId");
                    } else {
                        CommonRef[] params = ejbBinding.getReferences();
                        boolean exists=false;
                        for (int i=0;i<params.length;i++) {
                            if (row!=i && id.equals(params[i].getXmiId())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(ReferenceTablePanel.class,"TXT_ReferenceIdExists");
                        }
                    }
                    
                    if (jndiName.length()==0) {
                        return NbBundle.getMessage(ReferenceTablePanel.class,"TXT_EmptyReferenceJndiName");
                    }
                    
                    if (href.length()==0) {
                        return NbBundle.getMessage(ReferenceTablePanel.class,"TXT_EmptyReferenceHref");
                    }
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getJndiNameField().getDocument().addDocumentListener(docListener);
            dialogPanel.getIdField().getDocument().addDocumentListener(docListener);
            dialogPanel.getHrefField().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getJndiNameField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getIdField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getHrefField().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                
                
                String id = dialogPanel.getIdField().getText().trim();
                String jndiName = dialogPanel.getJndiNameField().getText().trim();
                String href = dialogPanel.getHrefField().getText().trim();
                String type=(String)dialogPanel.getTypeComboBox().getSelectedItem();
                        
                if (add) {
                    model.addRow(new String[]{type,id,jndiName,href});
                } else {
                    model.editRow(row,new String[]{type,id,jndiName,href});
                }
                dObj.modelUpdatedFromUI();
                //dObj.setChangedFromUI(true);
                dObj.setChangedFromUI(false);
            }
        }
    }
}
