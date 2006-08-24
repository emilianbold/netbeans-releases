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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import org.netbeans.modules.j2ee.websphere6.dd.beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;

import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.util.NbBundle;
import javax.swing.JComboBox;
/**
 * ModuleExtensionTablePanel - panel containing table for EJB references
 *
 *
 * @author dlm198383
 */
public class ModuleExtensionTablePanel extends DefaultTablePanel {
    private ModuleExtensionTableModel model;
    private WSAppExt appext;
    private WSMultiViewDataObject dObj;
    
    /** Creates new form ContextParamPanel */
    public ModuleExtensionTablePanel(final WSMultiViewDataObject dObj, final ModuleExtensionTableModel model) {
        super(model);
        this.model=model;
        this.dObj=dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
		dObj.setChangedFromUI(true);
                model.removeRow(getTable().getSelectedRow());
                dObj.modelUpdatedFromUI();
                //dObj.setChangedFromUI(true);
                dObj.setChangedFromUI(false);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }
    
    void setModel(WSAppExt appext, ModuleExtensionsType []params) {
        this.appext=appext;
        model.setData(appext,params);
    }
    
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            final int row = (add?-1:getTable().getSelectedRow());
            
            final ModuleExtensionPanel dialogPanel = new ModuleExtensionPanel();
            if(!add) {
                dialogPanel.getIdField().setText((String)model.getValueAt(row,0));
                dialogPanel.getHrefField().setText((String)model.getValueAt(row,1));
                dialogPanel.getAltRootField().setText((String)model.getValueAt(row,2));
                dialogPanel.getAltBindingsField().setText((String)model.getValueAt(row,3));
                dialogPanel.getAltExtensionsField().setText((String)model.getValueAt(row,4));
                dialogPanel.getTypeComboBox().setSelectedItem((String)model.getValueAt(row,5));
            }
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(ModuleExtensionTablePanel.class,"TTL_PageParam"),add) {
                protected String validate() {
                    String id = dialogPanel.getIdField().getText().trim();
                    String href = dialogPanel.getHrefField().getText().trim();
                    String altRoot = dialogPanel.getAltRootField().getText().trim();
                    String altBindings = dialogPanel.getAltBindingsField().getText().trim();
                    String altExtensions = dialogPanel.getAltExtensionsField().getText().trim();
                    String type = (String) dialogPanel.getTypeComboBox().getSelectedItem();
                    
                    if (id.length()==0) {
                        return NbBundle.getMessage(ModuleExtensionTablePanel.class,"TXT_EmptyModuleExtensionId");
                    } else {
                        ModuleExtensionsType[] params = appext.getModuleExtensions();
                        boolean exists=false;
                        for (int i=0;i<params.length;i++) {
                            if (row!=i && id.equals(params[i].getXmiId())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(ModuleExtensionTablePanel.class,"TXT_ModuleExtensionIdExists");
                        }
                    }
                    
                    if (href.length()==0) {
                        return NbBundle.getMessage(ModuleExtensionTablePanel.class,"TXT_EmptyModuleName");
                    }
                    
                    
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getIdField().getDocument().addDocumentListener(docListener);
            dialogPanel.getHrefField().getDocument().addDocumentListener(docListener);
            dialogPanel.getAltRootField().getDocument().addDocumentListener(docListener);
            dialogPanel.getAltBindingsField().getDocument().addDocumentListener(docListener);
            dialogPanel.getAltExtensionsField().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getIdField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getHrefField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getAltRootField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getAltBindingsField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getAltExtensionsField().getDocument().removeDocumentListener(docListener);
            
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                
                
                String id = dialogPanel.getIdField().getText().trim();
                String href = dialogPanel.getHrefField().getText().trim();
                String altRoot = dialogPanel.getAltRootField().getText().trim();
                String altBindings = dialogPanel.getAltBindingsField().getText().trim();
                String altExtensions = dialogPanel.getAltExtensionsField().getText().trim();
                String type = (String) dialogPanel.getTypeComboBox().getSelectedItem();
                
                dObj.setChangedFromUI(true);
                if (add) {
                    model.addRow(new String[]{id,href,altRoot,altBindings,altExtensions,type});
                } else {
                    String oldPageId=(String)model.getValueAt(row,0);
                    model.editRow(row,new String[]{id,href,altRoot,altBindings,altExtensions,type});
                }
                dObj.modelUpdatedFromUI();
                //dObj.setChangedFromUI(true);
                dObj.setChangedFromUI(false);
            }
        }
    }
}
