/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

                    if (id.length() == 0) {
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
                    model.editRow(row,new String[]{id,href,altRoot,altBindings,altExtensions,type});
                }
                dObj.modelUpdatedFromUI();
                //dObj.setChangedFromUI(true);
                dObj.setChangedFromUI(false);
            }
        }
    }
}
