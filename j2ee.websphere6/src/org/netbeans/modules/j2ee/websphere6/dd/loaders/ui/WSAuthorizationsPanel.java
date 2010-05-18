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

import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.j2ee.websphere6.dd.beans.AuthorizationsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
import org.netbeans.modules.j2ee.websphere6.dd.beans.SpecialSubjectType;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  dlm198383
 */
public class WSAuthorizationsPanel extends SectionInnerPanel
        implements DDXmiConstants,
        java.awt.event.ActionListener,
        java.awt.event.ItemListener{
    
    WSMultiViewDataObject dObj;
    AuthorizationsType auth;
    private static String [] Types=SPECIAL_SUBJECTS_TYPES;
    
    /** Creates new form WSAuthorizationsPanel */
    public WSAuthorizationsPanel(SectionView view, WSMultiViewDataObject dObj,AuthorizationsType auth) {
        super(view);
        initComponents();
        this.auth=auth;
        this.dObj=dObj;
        
        specialTypeComboBox.setModel(new DefaultComboBoxModel(Types));
        
        usergroupRadioButton.addActionListener(this);
        specialTypesRadioButton.addActionListener(this);
        specialTypeComboBox.addItemListener(this);
        
        authNameField.setText(auth.getXmiId());
        roleNameField.setText(auth.getRoleHref());
        
        if(auth.getSpecialSubjects()!=null) {
            specialTypeComboBox.setSelectedItem(auth.getSpecialSubjects().getType());
            specialTypeIdField.setText(auth.getSpecialSubjects().getXmiId());
            specialTypeNameField.setText(auth.getSpecialSubjects().getName());
            specialTypesRadioButton.setSelected(true);
        }
        if(auth.getUsers()!=null) {
            usersIdField.setText(auth.getUsersXmiId());
            usersNameField.setText(auth.getUsersName());
            usergroupRadioButton.setSelected(true);
        }
        if(auth.getGroups()!=null) {
            groupsIdField.setText(auth.getGroupsXmiId());
            groupsNameField.setText(auth.getGroupsName());
            usergroupRadioButton.setSelected(true);
        }
        addModifier(authNameField);
        addModifier(roleNameField);
        addModifier(usersIdField);
        addModifier(usersNameField);
        addModifier(groupsIdField);
        addModifier(groupsNameField);
        addModifier(specialTypeIdField);
        addModifier(specialTypeNameField);
        
        addValidatee(authNameField);
        addValidatee(roleNameField);
        
        addValidatee(usersIdField);
        addValidatee(usersNameField);
        
        addValidatee(groupsIdField);
        addValidatee(groupsNameField);
        
        addValidatee(specialTypeIdField);
        addValidatee(specialTypeNameField);
        
        setEnabledComponents();
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==authNameField) {
            auth.setXmiId((String)value);
        } else if (source==roleNameField) {
            auth.setRoleHref((String)value);
        } else if (source==groupsIdField) {
            auth.setGroupsXmiId((String)value);
        } else if (source==groupsNameField) {
            auth.setGroupsName((String)value);
        } else if (source==usersIdField) {
            auth.setUsersXmiId((String)value);
        } else if (source==usersNameField) {
            auth.setUsersName((String)value);
        } else if (source==specialTypeIdField) {
            auth.getSpecialSubjects().setXmiId((String)value);
        } else if (source==specialTypeNameField) {
            auth.getSpecialSubjects().setName((String)value);
        }
        
        
    }
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String time_id="_"+java.lang.System.currentTimeMillis();
        
        if(usergroupRadioButton.isSelected()) {
            auth.setSpecialSubjects(null);
            if(usersNameField.getText().equals("")) {
                usersIdField.setText(USERS_NAME+time_id);
                usersNameField.setText(USERS_DEFAULT_NAME);
            }
            if(groupsNameField.getText().equals("")) {
                groupsIdField.setText(GROUPS_NAME+time_id);
                groupsNameField.setText(GROUPS_DEFAULT_NAME);
            }
            
            auth.setUsersName(usersNameField.getText());
            auth.setUsersXmiId(usersIdField.getText());
            auth.setGroupsName(groupsNameField.getText());
            auth.setGroupsXmiId(groupsIdField.getText());
        } else if(specialTypesRadioButton.isSelected()) {
            auth.setUsers(null);
            auth.setGroups(null);
            SpecialSubjectType sst=new SpecialSubjectType();
            if(specialTypeNameField.getText().equals("")){
                specialTypeNameField.setText(SPECIAL_SUBJECTS_TYPE_EVERYONE);
            }
            if(specialTypeIdField.getText().equals("")) {
                specialTypeIdField.setText(SPECIAL_SUBJECTS_TYPE_EVERYONE+time_id);
            }
            sst.setName(specialTypeNameField.getText());
            sst.setXmiId(specialTypeIdField.getText());
            sst.setType((String)specialTypeComboBox.getSelectedItem());
            auth.setSpecialSubjects(sst);
        }
        setEnabledComponents();
        dObj.modelUpdatedFromUI();
    }
    
    public void itemStateChanged(java.awt.event.ItemEvent e) {
	dObj.setChangedFromUI(true);
        if(e.getSource()==specialTypeComboBox) {
            SpecialSubjectType sst=auth.getSpecialSubjects();
            String selectedItem=(String)specialTypeComboBox.getSelectedItem();
            String replacedString="";
            sst.setType(selectedItem);
            
            for(int j=0;j<Types.length;j++) {
                if(Types[j]!=selectedItem) {
                    if(sst.getName().lastIndexOf(Types[j])!=-1) {
                        replacedString=sst.getName().replaceAll(Types[j],selectedItem);
                        sst.setName(replacedString);
                        specialTypeNameField.setText(replacedString);
                    }
                    if(sst.getXmiId().lastIndexOf(Types[j])!=-1) {
                        replacedString=sst.getXmiId().replaceAll(Types[j],selectedItem);
                        sst.setXmiId(replacedString);
                        specialTypeIdField.setText(replacedString);
                    }
                }
            }
        }
        dObj.modelUpdatedFromUI();
	dObj.setChangedFromUI(false);
    }
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if(comp==authNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Auth Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==roleNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Role Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==usersNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Users Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==usersIdField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Users Id", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==groupsNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Groups Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==groupsIdField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Groups Id", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==specialTypeIdField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Special Type Id", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==specialTypeNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Special Type Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        
        
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if("Auth Name".equals(errorId)) return authNameField;
        if("Role Name".equals(errorId)) return roleNameField;
        if("Users Name".equals(errorId)) return usersNameField;
        if("Users Id".equals(errorId)) return usersIdField;
        if("Groups Name".equals(errorId)) return groupsNameField;
        if("Groups Id".equals(errorId)) return groupsIdField;
        if("Special Type Name".equals(errorId)) return specialTypeNameField;
        if("Special Type Id".equals(errorId)) return specialTypeIdField;
        return null;
    }
    
    private void setEnabledComponents() {
        boolean stateUserGroup=usergroupRadioButton.isSelected();
        boolean stateSpecialType=specialTypesRadioButton.isSelected();
        
        
        if((stateUserGroup && !stateSpecialType) || ((!stateUserGroup && stateSpecialType))) {
            jLabel3.setEnabled(stateUserGroup);
            jLabel4.setEnabled(stateUserGroup);
            jLabel7.setEnabled(stateUserGroup);
            jLabel8.setEnabled(stateUserGroup);
            usersIdField.setEnabled(stateUserGroup);
            usersNameField.setEnabled(stateUserGroup);
            groupsIdField.setEnabled(stateUserGroup);
            groupsNameField.setEnabled(stateUserGroup);
            
            jLabel9.setEnabled(stateSpecialType);
            jLabel10.setEnabled(stateSpecialType);
            jLabel11.setEnabled(stateSpecialType);
            specialTypeIdField.setEnabled(stateSpecialType);
            specialTypeNameField.setEnabled(stateSpecialType);
            specialTypeComboBox.setEnabled(stateSpecialType);
            
            
        }
        
    }
    /** This will be called before model is changed from this panel
     */
    protected void startUIChange() {
        dObj.setChangedFromUI(true);
    }
    
    /** This will be called after model is changed from this panel
     */
    protected void endUIChange() {
        dObj.modelUpdatedFromUI();
        dObj.setChangedFromUI(false);
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        typeGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        authNameField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        roleNameField = new javax.swing.JTextField();
        usergroupRadioButton = new javax.swing.JRadioButton();
        specialTypesRadioButton = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        usersNameField = new javax.swing.JTextField();
        usersIdField = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        groupsNameField = new javax.swing.JTextField();
        groupsIdField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        specialTypeIdField = new javax.swing.JTextField();
        specialTypeNameField = new javax.swing.JTextField();
        specialTypeComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_AuthorizationName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 5);
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(authNameField, gridBagConstraints);

        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_RoleName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 7, 5);
        add(jLabel2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(roleNameField, gridBagConstraints);

        typeGroup.add(usergroupRadioButton);
        usergroupRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_UsersGroups"));
        usergroupRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        usergroupRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(7, 12, 2, 0);
        add(usergroupRadioButton, gridBagConstraints);

        typeGroup.add(specialTypesRadioButton);
        specialTypesRadioButton.setSelected(true);
        specialTypesRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_SpecialType"));
        specialTypesRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        specialTypesRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 12, 0, 0);
        add(specialTypesRadioButton, gridBagConstraints);

        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_UsersName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel3, gridBagConstraints);

        jLabel4.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_UsersId"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(usersNameField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(usersIdField, gridBagConstraints);

        jLabel7.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_GroupsName"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 0, 0);
        add(jLabel7, gridBagConstraints);

        jLabel8.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_GroupsId"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(7, 5, 0, 10);
        add(groupsNameField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(groupsIdField, gridBagConstraints);

        jLabel9.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Id"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel9, gridBagConstraints);

        jLabel10.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel10, gridBagConstraints);

        jLabel11.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_Type"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jLabel11, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(specialTypeIdField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(specialTypeNameField, gridBagConstraints);

        specialTypeComboBox.setMinimumSize(new java.awt.Dimension(150, 20));
        specialTypeComboBox.setPreferredSize(new java.awt.Dimension(150, 22));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 15;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 10);
        add(specialTypeComboBox, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField authNameField;
    private javax.swing.JTextField groupsIdField;
    private javax.swing.JTextField groupsNameField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JTextField roleNameField;
    private javax.swing.JComboBox specialTypeComboBox;
    private javax.swing.JTextField specialTypeIdField;
    private javax.swing.JTextField specialTypeNameField;
    private javax.swing.JRadioButton specialTypesRadioButton;
    private javax.swing.ButtonGroup typeGroup;
    private javax.swing.JRadioButton usergroupRadioButton;
    private javax.swing.JTextField usersIdField;
    private javax.swing.JTextField usersNameField;
    // End of variables declaration//GEN-END:variables
    
}
