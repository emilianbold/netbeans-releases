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

import org.netbeans.modules.j2ee.websphere6.dd.beans.ExtendedServletsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.MarkupLanguagesType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webext.WSWebExtDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.openide.util.NbBundle;

/*
 *
 * @author  dlm198383
 */
public class WSExtendedServletPanel extends /*javax.swing.JPanel*/ SectionInnerPanel implements java.awt.event.ItemListener, javax.swing.event.ChangeListener {
    
    //private WSWebExtRootCustomizer masterPanel;
    ExtendedServletsType extendedServlet;
    WSWebExtDataObject dObj;
    SectionView view;
    
    private javax.swing.JCheckBox localTransactionCheckBox;
    private javax.swing.JTextField transactionNameField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel unresolvedLabel;
    private javax.swing.JComboBox unresolvedActionComboBox;
    private javax.swing.JCheckBox resolverCheckBox;
    private javax.swing.JComboBox resolverComboBox;
    private javax.swing.JCheckBox boundaryCheckBox;
    private javax.swing.JComboBox boundaryComboBox;
    
    
    public WSExtendedServletPanel(SectionView view, WSWebExtDataObject dObj,  ExtendedServletsType extendedServlet) {
        super(view);
        this.view=view;
        this.dObj=dObj;
        this.extendedServlet=extendedServlet;
        initComponents();
        
        bindLocalTransactionComponents();
        
        initLocalTransactionComponents();
        
        ((LocalTransactionPanel)containerPanel).setEnabledComponents();
        
        
        nameField.setText(extendedServlet.getXmiId());
        hrefField.setText(extendedServlet.getHref());
        addModifier(nameField);
        addModifier(hrefField);
        addValidatee(nameField);
        addValidatee(hrefField);
        
        getSectionView().getErrorPanel().clearError();
        
        
        int size=extendedServlet.sizeMarkupLanguages();
        MarkupLanguagesType [] markupLanguages = extendedServlet.getMarkupLanguages();
        for(int i=0;i<size;i++) {
            markupLanguagesTabPanel.addTab(/*markupLanguages[i].getName()*/""+(i+1),new WSMarkupLanguagesPanel(view,dObj,markupLanguages[i],extendedServlet,markupLanguagesTabPanel));
        }
        
    }
    
    private void bindLocalTransactionComponents(){
        LocalTransactionPanel localTransactionPanel=(LocalTransactionPanel)containerPanel;
        
        localTransactionCheckBox=localTransactionPanel.getLocalTransactionCheckBox();
        transactionNameField=localTransactionPanel.getTransactionNameField();
        unresolvedActionComboBox=localTransactionPanel.getUnresolvedActionComboBox();
        resolverCheckBox=localTransactionPanel.getResolverCheckBox();
        resolverComboBox=localTransactionPanel.getResolverComboBox();
        boundaryCheckBox=localTransactionPanel.getBoundaryCheckBox();
        boundaryComboBox=localTransactionPanel.getBoundaryComboBox();
        nameLabel=localTransactionPanel.getNameLabel();
        unresolvedLabel=localTransactionPanel.getUnresolvedActionLable();
        localTransactionPanel.setComponentsBackground(SectionVisualTheme.getSectionActiveBackgroundColor());
    }
    
    
    public void initLocalTransactionComponents() {        
        addModifier(transactionNameField);
        addValidatee(transactionNameField);
        boolean localTransactionEnabled=(extendedServlet.getLocalTransaction()==null)?false:true;
        localTransactionCheckBox.setSelected(localTransactionEnabled);
        
        if(localTransactionEnabled) {
            transactionNameField.setText(extendedServlet.getLocalTransactionXmiId());
            
            unresolvedActionComboBox.setSelectedItem(extendedServlet.getLocalTransactionUnresolvedAction());
            String str=extendedServlet.getLocalTransactionResolver();
            if(str==null) {
                resolverCheckBox.setSelected(false);
            } else {
                resolverCheckBox.setSelected(true);
                resolverComboBox.setSelectedItem(str);
            }
            
            str=extendedServlet.getLocalTransactionBoundary();
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
            extendedServlet.setXmiId((String)value);
        } else if (source==hrefField) {
            extendedServlet.setHref((String)value);
        } else if(source==transactionNameField) {
            extendedServlet.setLocalTransactionXmiId((String)value);
        }
        
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        //webext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
        dObj.modelUpdatedFromUI();
    }
    
    
    public void changeLocalTransactionState() {
        ((LocalTransactionPanel)containerPanel).setEnabledComponents();
        if(localTransactionCheckBox.isSelected()) {            
            extendedServlet.setLocalTransaction("");
            extendedServlet.setLocalTransactionXmiId(transactionNameField.getText());
            
            extendedServlet.setLocalTransactionUnresolvedAction(
                    unresolvedActionComboBox.getSelectedItem().toString());
            
            extendedServlet.setLocalTransactionResolver(
                    resolverCheckBox.isSelected()?
                        resolverComboBox.getSelectedItem().toString():
                        null);
            extendedServlet.setLocalTransactionBoundary(
                    boundaryCheckBox.isSelected()?
                        boundaryComboBox.getSelectedItem().toString():
                        null);
        } else {
            extendedServlet.setLocalTransaction(null);
            //extendedServlet.setLocalTransactionXmiId(null);
        }
        
    }
    
    
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        // TODO add your handling code here:
	dObj.setChangedFromUI(true);
        extendedServlet.setXmiId(nameField.getText());
        extendedServlet.setHref(hrefField.getText());
        changeLocalTransactionState();        
        dObj.modelUpdatedFromUI();
	dObj.setChangedFromUI(false);
    }
    
    
    public javax.swing.JTextField getNameField() {
        return nameField;
    }
    public javax.swing.JTextField getHrefField() {
        return hrefField;
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==nameField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Extended Servlet Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==hrefField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Extended Servlet HREF", comp));
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
            nameField.setText(extendedServlet.getXmiId());
        }
        if (hrefField==source) {
            hrefField.setText(extendedServlet.getHref());
        }
        if (transactionNameField==source) {
            transactionNameField.setText(extendedServlet.getLocalTransactionXmiId());
        }
        
    }
    /*
    protected void signalUIChange() {
        dObj.modelUpdatedFromUI();
    }*/
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("Extended Servlet Name".equals(errorId)) return nameField;
        if ("Extended Servlet HREF".equals(errorId)) return hrefField;
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

        jLabel1 = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        markupLanguagesTabPanel = new javax.swing.JTabbedPane();
        addMarkupLanguagesButton = new javax.swing.JButton();
        containerPanel = new LocalTransactionPanel();

        jLabel1.setText("HREF:");

        jLabel2.setText("Name:");

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle"); // NOI18N
        jLabel5.setText(bundle.getString("LBL_MarkupLanguages")); // NOI18N

        addMarkupLanguagesButton.setText(bundle.getString("LBL_AddMarkupLanguage")); // NOI18N
        addMarkupLanguagesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMarkupLanguagesButtonActionPerformed(evt);
            }
        });

        containerPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(hrefField, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(10, 10, 10))
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(markupLanguagesTabPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 577, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 242, Short.MAX_VALUE)
                        .addComponent(addMarkupLanguagesButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(hrefField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(6, 6, 6)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(containerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(addMarkupLanguagesButton))
                .addGap(11, 11, 11)
                .addComponent(markupLanguagesTabPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void addMarkupLanguagesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMarkupLanguagesButtonActionPerformed
         
        final MarkupLanguagePanel dialogPanel=new MarkupLanguagePanel();
        final EditDialog dialog = new EditDialog(dialogPanel,NbBundle.getMessage(WSExtendedServletPanel.class,"TTL_MarkupLanguages"),true) {
            protected String validate() {
                String id = dialogPanel.getIdField().getText().trim();
                int size=extendedServlet.sizeMarkupLanguages();
                
                for(int i=0;i<size;i++) {
                    MarkupLanguagesType ml=extendedServlet.getMarkupLanguages(i);
                    if(ml.getXmiId().equals(id)) {
                        return NbBundle.getMessage(WSExtendedServletPanel.class,"TXT_CurrentIdExists");
                    }
                }
                return null;
            }
        };
        dialog.setValid(false); // disable OK button
        
        
        javax.swing.event.DocumentListener docListener = new EditDialog.DocListener(dialog);
        dialogPanel.getIdField().getDocument().addDocumentListener(docListener);
        dialogPanel.getNameComboBox().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.checkValues();
            }
        });
        dialogPanel.getMimeTypeComboBox().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dialog.checkValues();
            }
        });
        
        
        java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
        d.setVisible(true);
        dialogPanel.getIdField().getDocument().removeDocumentListener(docListener);
        
        if (dialog.getValue().equals(EditDialog.OK_OPTION)) {
	    dObj.setChangedFromUI(true);
            MarkupLanguagesType markupLang=new MarkupLanguagesType();
            markupLang.setXmiId(dialogPanel.getIdField().getText().trim());
            markupLang.setName(((String) dialogPanel.getNameComboBox().getSelectedItem()));
            markupLang.setMimeType(((String) dialogPanel.getMimeTypeComboBox().getSelectedItem()));
            markupLang.setErrorPage("");
            markupLang.setDefaultPage("");
            extendedServlet.addMarkupLanguages(markupLang);
            int count=markupLanguagesTabPanel.getTabCount();
            markupLanguagesTabPanel.addTab(""+(count+1),new WSMarkupLanguagesPanel(view,dObj,markupLang,extendedServlet,markupLanguagesTabPanel));
            markupLanguagesTabPanel.setSelectedIndex(count);
            markupLanguagesTabPanel.getTitleAt(count);
            
            dObj.modelUpdatedFromUI();
            dObj.setChangedFromUI(false);
        }
        
    }//GEN-LAST:event_addMarkupLanguagesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMarkupLanguagesButton;
    private javax.swing.JPanel containerPanel;
    private javax.swing.JTextField hrefField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JTabbedPane markupLanguagesTabPanel;
    private javax.swing.JTextField nameField;
    // End of variables declaration//GEN-END:variables
    
}
