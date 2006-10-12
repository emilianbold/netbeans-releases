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

import org.netbeans.modules.j2ee.dd.api.common.CommonDDBean;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.SecurityRole;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.multiview.ui.EditDialog;
import org.openide.util.NbBundle;

/**
 * SecurityRoleTablePanel.java
 *
 * Panel for displaying the security role table.
 *
 * @author ptliu
 */
public class SecurityRoleTablePanel extends DefaultTablePanel {
    private SecurityRoleTableModel model;
    private WebApp webApp;
    private DDDataObject dObj;
    
    /** Creates new form ContextParamPanel */
    public SecurityRoleTablePanel(final DDDataObject dObj, final SecurityRoleTableModel model) {
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

    void setModel(WebApp webApp, SecurityRole[] roles) {
        model.setData(webApp, roles);
        this.webApp=webApp;
    }
    
    private class TableActionListener implements java.awt.event.ActionListener {
        private boolean add;
        
        TableActionListener(boolean add) {
            this.add=add;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            final int row = (add?-1:getTable().getSelectedRow());
            final SecurityRolePanel dialogPanel = new SecurityRolePanel();
            final String currentRoleName = null;
            SecurityRole role = null;
            
            if (!add) {
                role = model.getSecurityRole(row);
                dialogPanel.setRoleName(role.getRoleName());
                dialogPanel.setDescription(role.getDefaultDescription());
            }
            
            final SecurityRole currentRole = role;
            
            EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(EjbRefsTablePanel.class,"TTL_SecurityRole"),add) {
                protected String validate() {
                    String name = dialogPanel.getRoleName().trim();
            
                    if (name.length()==0) {
                        return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_EmptySecurityRoleName");
                    } else {
                        SecurityRole[] roles = webApp.getSecurityRole();
                        boolean exists=false;
                        
                        for (int i = 0; i < roles.length; i++) {
                            if (name.equals(roles[i].getRoleName()) &&
                                    roles[i] != currentRole) {
                                return NbBundle.getMessage(SecurityRoleTablePanel.class,"TXT_SecurityRoleNameExists",name);
                            }
                        }
                    }
                    
                    return null;
                }
            };
       
            if (add) 
                dialog.setValid(false); // disable OK button
  
            javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
            dialogPanel.getRoleNameTF().getDocument().addDocumentListener(docListener);
            dialogPanel.getDescriptionTA().getDocument().addDocumentListener(docListener);
            
            java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
            d.setVisible(true);
            
            dialogPanel.getRoleNameTF().getDocument().removeDocumentListener(docListener);
            dialogPanel.getDescriptionTA().getDocument().removeDocumentListener(docListener);
        
            if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(true);
                
                String roleName = dialogPanel.getRoleName();
                String description = dialogPanel.getDescription();
         
                if (add) 
                    model.addRow(new String[]{roleName, description});
                else 
                    model.editRow(row, new String[]{roleName, description});
                
                dObj.setChangedFromUI(false);
            }
        }   
    }
 
}
