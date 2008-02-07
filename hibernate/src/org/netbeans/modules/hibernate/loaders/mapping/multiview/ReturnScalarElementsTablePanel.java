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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate.loaders.mapping.multiview;

import org.netbeans.modules.hibernate.loaders.cfg.multiview.*;
import org.netbeans.modules.hibernate.loaders.cfg.*;
import org.netbeans.modules.hibernate.loaders.mapping.HibernateMappingDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;

/**
 *
 * @author Dongmei Cao
 */
public class ReturnScalarElementsTablePanel extends DefaultTablePanel {
    
    private ReturnScalarElementsTableModel model;
    private HibernateMappingDataObject mappingDataObject;

    /** Creates new form EventTablePanel */
    public ReturnScalarElementsTablePanel(final HibernateMappingDataObject dObj, final ReturnScalarElementsTableModel model) {
    	super(model);
    	this.model=model;
        this.mappingDataObject=dObj;
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mappingDataObject.modelUpdatedFromUI();
                int row = getTable().getSelectedRow();
                model.removeRow(row);
            }
        });
        editButton.addActionListener(new TableActionListener(false));
        addButton.addActionListener(new TableActionListener(true));
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {

        private boolean add;

        TableActionListener(boolean add) {
            this.add = add;
        }

        public void actionPerformed(java.awt.event.ActionEvent evt) {
            /*
            final int row = (add ? -1 : getTable().getSelectedRow());
            final MetaDataDetailPanel dialogPanel = new MetaDataDetailPanel();

            if (!add) {
                String classname = (String) model.getValueAt(row, 0);
                String rename = (String)model.getValueAt(row, 1 );
                dialogPanel.initValues(classname, rename);
            }
            
            EditDialog dialog = new EditDialog(dialogPanel, NbBundle.getMessage(ImportElementsTablePanel.class, "LBL_Meta"), add) {

                protected String validate() {
                    // TODO: more validation code later
                    String attribute = dialogPanel.getAttribute();
                    String value = dialogPanel.getValue();
             
                    if (attribute.length() == 0) {
                        return NbBundle.getMessage(ImportElementsTablePanel.class, "TXT_Meta_Attribute_Empty");
                    } 
                    
                    if( value.length() == 0 ) {
                        return NbBundle.getMessage(ImportElementsTablePanel.class, "TXT_Meta_Value_Empty");
                    }
                    
                    return null;
                }
            };

            if (add) {
                dialog.setValid(false);
            } // disable OK button
            
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getAttributeTextField().getDocument().addDocumentListener(docListener);
            dialogPanel.getValueTextField().getDocument().addDocumentListener(docListener);

            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            dialogPanel.getAttributeTextField().getDocument().removeDocumentListener(docListener);
            dialogPanel.getValueTextField().getDocument().removeDocumentListener(docListener);

            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                mappingDataObject.modelUpdatedFromUI();
                 String attribute = dialogPanel.getAttribute();
                    String value = dialogPanel.getValue();
                    String inherit = dialogPanel.getInherit();
                if (add) {
                    model.addRow(attribute, inherit, value);
                } else {
                    model.editRow(row, attribute, inherit, value);
                }
            }
            */
        }
    }

    
}