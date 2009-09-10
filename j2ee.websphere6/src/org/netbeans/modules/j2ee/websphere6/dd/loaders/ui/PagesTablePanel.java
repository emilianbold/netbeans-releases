/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import org.netbeans.modules.j2ee.websphere6.dd.beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;

import org.netbeans.modules.xml.multiview.ui.*;
import org.openide.util.NbBundle;
import javax.swing.JComboBox;
/**
 * PagesTablePanel - panel containing table for EJB references
 *
 *
 * @author dlm198383
 */
public class PagesTablePanel extends DefaultTablePanel {
    private PageTableModel model;
    private MarkupLanguagesType markupLanguage;
    private WSMultiViewDataObject dObj;
    private final JComboBox errorPageComboBox;
    private final JComboBox defaultPageComboBox;
    /** Creates new form ContextParamPanel */
    public PagesTablePanel(final WSMultiViewDataObject dObj, final PageTableModel model,final JComboBox errorPageComboBox,final JComboBox defaultPageComboBox) {
        super(model);
        this.model=model;
        this.dObj=dObj;
        this.errorPageComboBox=errorPageComboBox;
        this.defaultPageComboBox=defaultPageComboBox;
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dObj.setChangedFromUI(true);
                int row = getTable().getSelectedRow();
                
                String selectedItem=(String)errorPageComboBox.getSelectedItem();
                String deleteItem=(String)getModel().getValueAt(row,0);
                
                
                errorPageComboBox.removeItem(deleteItem);
                if(selectedItem != null && selectedItem.equals(deleteItem)) {
                    errorPageComboBox.setSelectedIndex(-1);
                }
                errorPageComboBox.updateUI();
                
                selectedItem=(String)defaultPageComboBox.getSelectedItem();
                defaultPageComboBox.removeItem(getModel().getValueAt(row,0));
                if(selectedItem.equals(deleteItem)) {
                    defaultPageComboBox.setSelectedIndex(-1);
                }
                defaultPageComboBox.updateUI();
                model.removeRow(row);
                dObj.modelUpdatedFromUI();
                //dObj.setChangedFromUI(true);
                dObj.setChangedFromUI(false);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }
    
    void setModel(MarkupLanguagesType markupLanguage, PageType []params) {
        this.markupLanguage=markupLanguage;
        model.setData(markupLanguage,params);
    }
    
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        TableActionListener(boolean add) {
            this.add=add;
        }
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            
            final int row = (add?-1:getTable().getSelectedRow());
            
            final PagePanel dialogPanel = new PagePanel();
            if(!add) {
                dialogPanel.getIdField().setText((String)model.getValueAt(row,0));
                dialogPanel.getNameField().setText((String)model.getValueAt(row,1));
                dialogPanel.getUriField().setText((String)model.getValueAt(row,2));
            }
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(PagesTablePanel.class,"TTL_PageParam"),add) {
                protected String validate() {
                    String id = dialogPanel.getIdField().getText().trim();
                    String name = dialogPanel.getNameField().getText().trim();
                    String uri = dialogPanel.getUriField().getText().trim();
                    if (id.length()==0) {
                        return NbBundle.getMessage(PagesTablePanel.class,"TXT_EmptyPageId");
                    } else {
                        PageType[] params = markupLanguage.getPages();
                        boolean exists=false;
                        for (int i=0;i<params.length;i++) {
                            if (row!=i && id.equals(params[i].getXmiId())) {
                                exists=true;
                                break;
                            }
                        }
                        if (exists) {
                            return NbBundle.getMessage(PagesTablePanel.class,"TXT_PageIdExists",name);
                        }
                    }
                    
                    if (name.length()==0) {
                        return NbBundle.getMessage(PagesTablePanel.class,"TXT_EmptyPageName");
                    }
                    
                    if (uri.length()==0) {
                        return NbBundle.getMessage(PagesTablePanel.class,"TXT_EmptyPageUri");
                    }
                    return null;
                }
            };
            
            if (add) dialog.setValid(false); // disable OK button
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getNameField().getDocument().addDocumentListener(docListener);
            dialogPanel.getIdField().getDocument().addDocumentListener(docListener);
            dialogPanel.getUriField().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getNameField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getIdField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getUriField().getDocument().removeDocumentListener(docListener);
            
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                
                
                String id = dialogPanel.getIdField().getText().trim();
                String name = dialogPanel.getNameField().getText().trim();
                String uri = dialogPanel.getUriField().getText().trim();
                dObj.setChangedFromUI(true);
                if (add) {
                    model.addRow(new String[]{id,name,uri});
                    int selectedIndex=errorPageComboBox.getSelectedIndex();
                    errorPageComboBox.addItem(id);
                    errorPageComboBox.setSelectedIndex(selectedIndex);
                    selectedIndex=defaultPageComboBox.getSelectedIndex();
                    defaultPageComboBox.addItem(id);
                    defaultPageComboBox.setSelectedIndex(selectedIndex);
                } else {
                    String oldPageId=(String)model.getValueAt(row,0);
                    model.editRow(row,new String[]{id,name,uri});
                    for(int i=0;i<errorPageComboBox.getItemCount();i++) {
                        if(errorPageComboBox.getItemAt(i).equals(oldPageId)) {
                            int selectedItem=errorPageComboBox.getSelectedIndex();
                            errorPageComboBox.removeItemAt(i);
                            errorPageComboBox.insertItemAt(id,i);
                            errorPageComboBox.setSelectedIndex(selectedItem);
                            errorPageComboBox.updateUI();
                            
                            selectedItem=defaultPageComboBox.getSelectedIndex();
                            defaultPageComboBox.removeItemAt(i);
                            defaultPageComboBox.insertItemAt(id,i);
                            defaultPageComboBox.setSelectedIndex(selectedItem);
                            defaultPageComboBox.updateUI();
                            break;
                        }
                    }
                }
                dObj.modelUpdatedFromUI();
                //dObj.setChangedFromUI(true);
                dObj.setChangedFromUI(false);
            }
        }
    }
}
