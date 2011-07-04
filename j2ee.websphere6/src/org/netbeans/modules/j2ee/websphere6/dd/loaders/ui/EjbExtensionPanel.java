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

import java.awt.event.ItemListener;
import javax.swing.DefaultComboBoxModel;
import org.netbeans.modules.j2ee.websphere6.dd.beans.*;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.ejbext.WSEjbExtDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;

/**
 *
 * @author  dlm198383
 */
public class EjbExtensionPanel extends SectionInnerPanel implements java.awt.event.ItemListener {
    EjbExtensionsType ejbExtension;
    WSEjbExtDataObject dObj;
    
    private javax.swing.JCheckBox localTransactionCheckBox;
    private javax.swing.JTextField transactionNameField;
    
    
    private javax.swing.JComboBox unresolvedActionComboBox;
    private javax.swing.JCheckBox resolverCheckBox;
    private javax.swing.JComboBox resolverComboBox;
    private javax.swing.JCheckBox boundaryCheckBox;
    private javax.swing.JComboBox boundaryComboBox;
    
    
    private static String [] Types=new String [] {
        NbBundle.getMessage(EjbExtensionPanel.class,"LBL_TypeSession"),
        NbBundle.getMessage(EjbExtensionPanel.class,"LBL_TypeEntity"),
        NbBundle.getMessage(EjbExtensionPanel.class,"LBL_TypeMessageDriven")};
    /** Creates new form WSResRefBindingsPanel */
    public EjbExtensionPanel(SectionView view, WSEjbExtDataObject dObj,  EjbExtensionsType ejbExtension) {
        
        super(view);
        this.dObj=dObj;
        this.ejbExtension=ejbExtension;
        initComponents();
        bindLocalTransactionComponents();
        
        initLocalTransactionComponents();
        
        ((LocalTransactionPanel)jPanel1).setEnabledComponents();
        
        typeComboBox.setModel(new DefaultComboBoxModel(Types));
        
        nameField.setText(ejbExtension.getXmiName());
        idField.setText(ejbExtension.getXmiId());
        beanIdField.setText(ejbExtension.getHref());
        
        addModifier(nameField);
        addModifier(idField);
        addModifier(beanIdField);
        
        addValidatee(nameField);
        addValidatee(idField);
        addValidatee(beanIdField);
        
        
        String xmiType=ejbExtension.getXmiType();
        if(xmiType!=null) {
            if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_SESSION)) {
                typeComboBox.setSelectedIndex(0);
            } else if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_ENTITY)) {
                typeComboBox.setSelectedIndex(1);
            } else if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN)) {
                typeComboBox.setSelectedIndex(2);
            } else {
                typeComboBox.setSelectedIndex(-1);
            }
        } else {
            typeComboBox.setSelectedIndex(-1);
        }
        typeComboBox.addItemListener(this);
    }
    
    private void bindLocalTransactionComponents(){
        LocalTransactionPanel localTransactionPanel=(LocalTransactionPanel)jPanel1;
        
        localTransactionCheckBox=localTransactionPanel.getLocalTransactionCheckBox();
        transactionNameField=localTransactionPanel.getTransactionNameField();
        unresolvedActionComboBox=localTransactionPanel.getUnresolvedActionComboBox();
        resolverCheckBox=localTransactionPanel.getResolverCheckBox();
        resolverComboBox=localTransactionPanel.getResolverComboBox();
        boundaryCheckBox=localTransactionPanel.getBoundaryCheckBox();
        boundaryComboBox=localTransactionPanel.getBoundaryComboBox();
        localTransactionPanel.setComponentsBackground(SectionVisualTheme.getSectionActiveBackgroundColor());
        
    }
    
    public void initLocalTransactionComponents() {
        addModifier(nameField);
        addModifier(transactionNameField);
        addValidatee(transactionNameField);
        boolean localTransactionEnabled=(ejbExtension.getLocalTransaction()==null)?false:true;
        localTransactionCheckBox.setSelected(localTransactionEnabled);
        
        if(localTransactionEnabled) {
            transactionNameField.setText(ejbExtension.getLocalTransactionXmiId());
            
            unresolvedActionComboBox.setSelectedItem(ejbExtension.getLocalTransactionUnresolvedAction());
            String str=ejbExtension.getLocalTransactionResolver();
            if(str==null) {
                resolverCheckBox.setSelected(false);
            } else {
                resolverCheckBox.setSelected(true);
                resolverComboBox.setSelectedItem(str);
            }
            
            str=ejbExtension.getLocalTransactionBoundary();
            if(str==null) {
                boundaryCheckBox.setSelected(false);
            } else {
                boundaryCheckBox.setSelected(true);
                boundaryComboBox.setSelectedItem(str);
            }
        }
        
        
        localTransactionCheckBox.addItemListener(this);
        unresolvedActionComboBox.addItemListener(this);
        
        resolverCheckBox.addItemListener(this);
        resolverComboBox.addItemListener(this);
        
        boundaryCheckBox.addItemListener(this);
        boundaryComboBox.addItemListener(this);
    }
    
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==nameField) {
            ejbExtension.setXmiName((String)value);
        }
        if (source==idField) {
            ejbExtension.setXmiId((String)value);
            
        }
        if (source==beanIdField) {
            ejbExtension.setHref((String)value);
        }
        
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
	dObj.setChangedFromUI(true);
        String selectedString=(String)typeComboBox.getSelectedItem();
        if(selectedString!=null) {
            if(selectedString.equals(Types[0])) { //session
                ejbExtension.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_SESSION);
                ejbExtension.setEjbExtensionsType(DDXmiConstants.EJB_EXTENSIONS_TYPE_SESSION);
            } else if(selectedString.equals(Types[1])) { //entity
                ejbExtension.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_ENTITY);
                ejbExtension.setEjbExtensionsType(DDXmiConstants.EJB_EXTENSIONS_TYPE_ENTITY);
            } else if(selectedString.equals(Types[2])) { //message driven
                ejbExtension.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN);
                ejbExtension.setEjbExtensionsType(DDXmiConstants.EJB_EXTENSIONS_TYPE_MESSAGEDRIVEN);
            } else {
                ejbExtension.setXmiType(null);
                ejbExtension.setEjbExtensionsType(null);
            }
        }
        changeLocalTransactionState();
        dObj.modelUpdatedFromUI();
        //dObj.setChangedFromUI(true);
        dObj.setChangedFromUI(false);
    }
    
    public void changeLocalTransactionState() {
        if(localTransactionCheckBox.isSelected()) {
            ejbExtension.setLocalTransaction("");
            ejbExtension.setLocalTransactionXmiId(transactionNameField.getText());
            
            ejbExtension.setLocalTransactionUnresolvedAction(
                    unresolvedActionComboBox.getSelectedItem().toString());
            
            ejbExtension.setLocalTransactionResolver(
                    resolverCheckBox.isSelected()?
                        resolverComboBox.getSelectedItem().toString():
                        null);
            ejbExtension.setLocalTransactionBoundary(
                    boundaryCheckBox.isSelected()?
                        boundaryComboBox.getSelectedItem().toString():
                        null);
        } else {
            ejbExtension.setLocalTransaction(null);
            //ejbExtension.setLocalTransactionXmiId(null);
        }
        ((LocalTransactionPanel)jPanel1).setEnabledComponents();
    }
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==nameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==idField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==beanIdField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Bean Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==transactionNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Local transaction name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (nameField==source) {
            nameField.setText(ejbExtension.getXmiName());
        }
        if (idField==source) {
            idField.setText(ejbExtension.getXmiId());
        }
        if (beanIdField==source) {
            beanIdField.setText(ejbExtension.getHref());
        }
        if (transactionNameField==source) {
            transactionNameField.setText(ejbExtension.getLocalTransactionXmiId());
        }
        
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("Name".equals(errorId)) return nameField;
        if ("ID".equals(errorId)) return idField;
        if ("Bean Name".equals(errorId)) return beanIdField;
        if("Local transaction name".equals(errorId)) return transactionNameField;
        return null;
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        idField = new javax.swing.JTextField();
        beanIdField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        jPanel1 = new LocalTransactionPanel();
        jSeparator1 = new javax.swing.JSeparator();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle"); // NOI18N
        nameLabel.setText(bundle.getString("LBL_Name")); // NOI18N

        jLabel2.setText(bundle.getString("LBL_Id")); // NOI18N

        jLabel3.setText(bundle.getString("LBL_BeanName")); // NOI18N

        jLabel4.setText(bundle.getString("LBL_BeanType")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(nameLabel)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addComponent(jLabel4))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(idField, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .addComponent(beanIdField, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                            .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 516, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 538, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(beanIdField, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField beanIdField;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField nameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JComboBox typeComboBox;
    // End of variables declaration//GEN-END:variables
    
}
