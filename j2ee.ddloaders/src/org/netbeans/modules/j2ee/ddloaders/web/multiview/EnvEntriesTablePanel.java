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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.netbeans.modules.xml.multiview.ui.SimpleDialogPanel;
import org.openide.util.NbBundle;

/** EnvEntriesTablePanel - panel containing table for env-entries
 *
 * @author  mk115033
 * Created on April 11, 2005
 */
public class EnvEntriesTablePanel extends DefaultTablePanel {
    private EnvEntryTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ContextParamPanel */
    public EnvEntriesTablePanel(final DDDataObject dObj, final EnvEntryTableModel model) {
    	super(model);
    	this.model=model;
        this.dObj=dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                int row = getTable().getSelectedRow();
                model.removeRow(row);
                dObj.setChangedFromUI(false);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }

    void setModel(WebApp webApp, EnvEntry[] params) {
        model.setData(webApp,params);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            final int row = (add?-1:getTable().getSelectedRow());
            final EnvEntryPanel dialogPanel = new EnvEntryPanel();
            if (!add) {
                EnvEntry envEntry = model.getEnvEntry(row);
                dialogPanel.setEnvEntryName(envEntry.getEnvEntryName());
                dialogPanel.setEnvEntryType(envEntry.getEnvEntryType());
                dialogPanel.setEnvEntryValue(envEntry.getEnvEntryValue());
                dialogPanel.setDescription(envEntry.getDefaultDescription());
            }
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EnvEntriesTablePanel.class,"TTL_EnvEntry"),add) {
                protected String validate() {
                    String name = dialogPanel.getEnvEntryName().trim();
                    if (name.length()==0) {
                        return NbBundle.getMessage(EnvEntriesTablePanel.class,"TXT_EmptyEnvEntryName");
                    } else {
                        EnvEntry[] params = webApp.getEnvEntry();
                        boolean exists=false;
                        for (int i=0;i<params.length;i++) {
                            if (row!=i && name.equals(params[i].getEnvEntryName())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(EnvEntriesTablePanel.class,"TXT_EnvEntryNameExists",name);
                        }
                    }
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getNameTF().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.show();
            dialogPanel.getNameTF().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                String name = dialogPanel.getEnvEntryName().trim();
                String type = dialogPanel.getEnvEntryType();
                String value = dialogPanel.getEnvEntryValue().trim();
                String description = dialogPanel.getDescription();
                if (add) model.addRow(new String[]{name,type,value,description});
                else model.editRow(row,new String[]{name,type,value,description});
                dObj.setChangedFromUI(false);
            }
        }
    }
}
