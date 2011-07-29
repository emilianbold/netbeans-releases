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
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;
/**
 *
 * @author  dlm198383
 */
public class WSEjbBindingsPanel extends SectionInnerPanel implements java.awt.event.ItemListener,DDXmiConstants{
    EjbBindingsType ejbBinding;
    WSMultiViewDataObject dObj;
    private static String [] Types=new String [] {
        NbBundle.getMessage(WSEjbBindingsPanel.class,"LBL_TypeSession"),
        NbBundle.getMessage(WSEjbBindingsPanel.class,"LBL_TypeEntity"),
        NbBundle.getMessage(WSEjbBindingsPanel.class,"LBL_TypeContainerManagedEntity"),
        NbBundle.getMessage(WSEjbBindingsPanel.class,"LBL_TypeMessageDriven")
    };
    
    private javax.swing.JTextField cmpConnectionFactoryNameField;
    private javax.swing.JTextField cmpConnectionFactoryJndiNameField;
    private javax.swing.JCheckBox  cmpConnectionFactoryCheckBox;
    private javax.swing.JComboBox  cmpConnectionFactoryAuthType;
    private static final String FACTORY_NAME="CMP Connection Factory Name"; //NOI18N
    private static final String FACTORY_JNDI_NAME="CMP Connection Factory JNDI Name"; //NOI18N
    
    /** Creates new form WSResRefBindingsPanel */
    public WSEjbBindingsPanel(SectionView view,WSMultiViewDataObject dObj,EjbBindingsType ejbBinding) {
        super(view);
        this.dObj=dObj;
        this.ejbBinding=ejbBinding;
        initComponents();
        bindCmpConnectionFactoryComponents();
        initCmpConnectionFactoryComponents();
        
        
        
        
        idField.setText(ejbBinding.getXmiId());
        jndiNameField.setText(ejbBinding.getJndiName());
        hrefField.setText(ejbBinding.getHref());
        addModifier(hrefField);
        addModifier(jndiNameField);
        addModifier(idField);
        beanTypeComboBox.setModel(new DefaultComboBoxModel(Types));
        String xmiType=ejbBinding.getXmiType();
        if(xmiType!=null) {
            if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_SESSION)) {
                beanTypeComboBox.setSelectedIndex(0);
            } else if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_ENTITY)) {
                beanTypeComboBox.setSelectedIndex(1);
            } else if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_CONTAINER_MANAGED_ENTITY)) {
                beanTypeComboBox.setSelectedIndex(2);
            } else if(xmiType.equals(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN)) {
                beanTypeComboBox.setSelectedIndex(3);
            } else {
                beanTypeComboBox.setSelectedIndex(-1);
            }
        } else {
            beanTypeComboBox.setSelectedIndex(-1);
        }
        beanTypeComboBox.addItemListener(this);
        getSectionView().getErrorPanel().clearError();
        
        ReferencesTableModel model = new ReferencesTableModel(dObj.getModelSynchronizer());
        model.setHrefType(EJB_JAR);
        ReferenceTablePanel ptp= new ReferenceTablePanel(dObj, model);
        ptp.setModel(ejbBinding,ejbBinding.getReferences());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        //gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        gridBagConstraints.weightx = 1.0;
        jPanel1.add(ptp,gridBagConstraints);
        
    }
    private void bindCmpConnectionFactoryComponents() {
        CMPConnectionFactory panel=(CMPConnectionFactory)cmpConnectionFactoryPanel;
        cmpConnectionFactoryNameField=panel.getNameField();
        cmpConnectionFactoryJndiNameField=panel.getJndiNameField();
        cmpConnectionFactoryCheckBox=panel.getFactoryCheckBox();
        cmpConnectionFactoryAuthType=panel.getAuthTypeComboBox();
        panel.setComponentsBackground(SectionVisualTheme.getSectionActiveBackgroundColor());
        panel.setEnabledComponents();
    }
    
    private void initCmpConnectionFactoryComponents(){
        addModifier(cmpConnectionFactoryNameField);
        addModifier(cmpConnectionFactoryJndiNameField);
        addValidatee(cmpConnectionFactoryNameField);
        addValidatee(cmpConnectionFactoryJndiNameField);
        
        boolean factoryEnabled=(ejbBinding.getCmpConnectionFactory()==null)?false:true;
        cmpConnectionFactoryCheckBox.setSelected(factoryEnabled);
        
        if(factoryEnabled) {
            
            cmpConnectionFactoryNameField.setText(
                    ejbBinding.getCmpConnectionFactoryXmiId());
            
            cmpConnectionFactoryJndiNameField.setText(
                    ejbBinding.getCmpConnectionFactoryJndiName());
            
            cmpConnectionFactoryAuthType.setSelectedItem(
                    ejbBinding.getCmpConnectionFactoryResAuth());
            
        }
        cmpConnectionFactoryCheckBox.addItemListener(this);        
        cmpConnectionFactoryAuthType.addItemListener(this);
        ((CMPConnectionFactory)cmpConnectionFactoryPanel).setEnabledComponents();
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==idField) {
            ejbBinding.setXmiId((String)value);
        } else if(source==jndiNameField) {
            ejbBinding.setJndiName((String)value);
        } else if(source==hrefField) {
            ejbBinding.setHref((String)value);
        } else if(source==cmpConnectionFactoryNameField) {
            ejbBinding.setCmpConnectionFactoryXmiId((String)value);
        } else if(source==cmpConnectionFactoryJndiNameField) {
            ejbBinding.setCmpConnectionFactoryJndiName((String)value);
        }
        
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==idField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==jndiNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "JNDI Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==hrefField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, "Bean Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==cmpConnectionFactoryNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, FACTORY_NAME, comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if(comp==cmpConnectionFactoryJndiNameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView().getErrorPanel().setError(new Error(Error.MISSING_VALUE_MESSAGE, FACTORY_JNDI_NAME, comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (idField==source) {
            idField.setText(ejbBinding.getXmiId());
        }
        if (jndiNameField==source) {
            jndiNameField.setText(ejbBinding.getJndiName());
        }
        if (hrefField==source) {
            hrefField.setText(ejbBinding.getHref());
        }
        if (cmpConnectionFactoryNameField==source) {
            cmpConnectionFactoryNameField.setText(ejbBinding.getCmpConnectionFactoryXmiId());
        }
        if (cmpConnectionFactoryJndiNameField==source) {
            cmpConnectionFactoryNameField.setText(ejbBinding.getCmpConnectionFactoryJndiName());
        }
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("ID".equals(errorId)) return idField;
        if ("JNDI Name".equals(errorId)) return jndiNameField;
        if ("Bean Name".equals(errorId)) return hrefField;
        if (FACTORY_NAME.equals(errorId)) return cmpConnectionFactoryNameField;
        if (FACTORY_JNDI_NAME.equals(errorId)) return cmpConnectionFactoryJndiNameField;
        return null;
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        dObj.setChangedFromUI(true);
        String selectedString=(String)beanTypeComboBox.getSelectedItem();
        if(selectedString!=null) {
            if(selectedString.equals(Types[0])) { //session
                ejbBinding.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_SESSION);
            } else if(selectedString.equals(Types[1])) { //entity
                ejbBinding.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_ENTITY);
            } else if(selectedString.equals(Types[2])) { //entity
                ejbBinding.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_CONTAINER_MANAGED_ENTITY);
            } else if(selectedString.equals(Types[3])) { //message driven
                ejbBinding.setXmiType(DDXmiConstants.EJB_ENTERPRISE_BEAN_TYPE_MESSAGEDRIVEN);
            } else {
                ejbBinding.setXmiType(null);
                
            }
        }
        changeCmpConnectionFactoryState();
        // TODO add your handling code here:
        dObj.modelUpdatedFromUI();
        //dObj.setChangedFromUI(true);
        dObj.setChangedFromUI(false);
    }
    
    private void changeCmpConnectionFactoryState() {
        if(cmpConnectionFactoryCheckBox.isSelected()) {
            ejbBinding.setCmpConnectionFactory("");
            ejbBinding.setCmpConnectionFactoryXmiId(cmpConnectionFactoryNameField.getText());
            ejbBinding.setCmpConnectionFactoryResAuth(
                    cmpConnectionFactoryAuthType.getSelectedItem().toString());
        } else {
            ejbBinding.setCmpConnectionFactory(null);            
        }
        ((CMPConnectionFactory)cmpConnectionFactoryPanel).setEnabledComponents();
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

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        jndiNameField = new javax.swing.JTextField();
        hrefField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        beanTypeComboBox = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        cmpConnectionFactoryPanel = new CMPConnectionFactory(); ;
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();

        jLabel1.setText("Id:");

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle"); // NOI18N
        jLabel2.setText(bundle.getString("LBL_JndiName")); // NOI18N

        jLabel3.setText(bundle.getString("LBL_BeanName")); // NOI18N

        typeLabel.setText(bundle.getString("LBL_BeanType")); // NOI18N

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel4.setText(bundle.getString("LBL_ReferenceBindings")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cmpConnectionFactoryPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(typeLabel)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1))
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(idField, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                            .addComponent(hrefField, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                            .addComponent(jndiNameField, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
                            .addComponent(beanTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel4)
                .addContainerGap(370, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jndiNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(hrefField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(beanTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmpConnectionFactoryPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox beanTypeComboBox;
    private javax.swing.JPanel cmpConnectionFactoryPanel;
    private javax.swing.JTextField hrefField;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jndiNameField;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
    
}
