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

import java.awt.event.ActionEvent;
import org.netbeans.modules.j2ee.websphere6.dd.beans.ExtendedServletsType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.MarkupLanguagesType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.PageType;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webext.WSWebExtDataObject;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
/**
 *
 * @author  dlm198383
 */
public class WSMarkupLanguagesPanel extends SectionInnerPanel implements java.awt.event.ItemListener{
    
    
    ExtendedServletsType extendedServlet;
    MarkupLanguagesType markupLanguage;
    WSWebExtDataObject dObj;
    javax.swing.JTabbedPane markupLanguagesTabPanel;
    
    
    public WSMarkupLanguagesPanel(SectionView view, final WSWebExtDataObject dObj,  final MarkupLanguagesType markupLanguage,final ExtendedServletsType extendedServlet,final javax.swing.JTabbedPane markupLanguagesTabPanel) {
        super(view);
        this.dObj=dObj;        
        this.markupLanguage=markupLanguage;
        this.markupLanguagesTabPanel=markupLanguagesTabPanel;
        this.extendedServlet=extendedServlet;
        initComponents();
        nameComboBox.setModel(new javax.swing.DefaultComboBoxModel(MarkupLanguagesType.AVALIABLE_NAMES));
        mimeTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(MarkupLanguagesType.AVALIABLE_MIME_TYPES));
        
        idField.setText(markupLanguage.getXmiId());
        nameComboBox.setSelectedItem(markupLanguage.getName());
        mimeTypeComboBox.setSelectedItem(markupLanguage.getMimeType());
        
        addModifier(idField);
        nameComboBox.addItemListener(this);
        mimeTypeComboBox.addItemListener(this);
        errorPageComboBox.addItemListener(this);
        defaultPageComboBox.addItemListener(this);
        
        setComboBoxModels();
        
        PageTableModel model = new PageTableModel(dObj.getModelSynchronizer());
        PagesTablePanel ptp= new PagesTablePanel(dObj, model,errorPageComboBox,defaultPageComboBox);
        ptp.setModel(markupLanguage,markupLanguage.getPages());
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        //gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 0, 0);
        gridBagConstraints.weightx = 1.0;
        pagesContainerPanel.add(ptp,gridBagConstraints);
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
		dObj.setChangedFromUI(true);
                int selectedTab=markupLanguagesTabPanel.getSelectedIndex();
                markupLanguagesTabPanel.removeTabAt(selectedTab);
                for(int i=0;i<markupLanguagesTabPanel.getTabCount();i++) {
                 markupLanguagesTabPanel.setTitleAt(i,""+(i+1));   
                }                
                extendedServlet.removeMarkupLanguages(markupLanguage);
                dObj.modelUpdatedFromUI();
                dObj.setChangedFromUI(false);
            }
        });
    }
    public void setComboBoxModels() {
        int pagesNumber=markupLanguage.getPages().length;
        if(pagesNumber!=0) {
            String [] pagesNames=new String[pagesNumber];
            for(int i=0;i<pagesNumber;i++) {
                pagesNames[i]=markupLanguage.getPages(i).getXmiId();
            }
            javax.swing.DefaultComboBoxModel modelError=new javax.swing.DefaultComboBoxModel(pagesNames);
            errorPageComboBox.setModel(modelError);
            errorPageComboBox.setSelectedItem(markupLanguage.getErrorPage());
            
            javax.swing.DefaultComboBoxModel modelDefault=new javax.swing.DefaultComboBoxModel(pagesNames);
            defaultPageComboBox.setModel(modelDefault);
            defaultPageComboBox.setSelectedItem(markupLanguage.getDefaultPage());
        }
    }
    
    public void setValue(javax.swing.JComponent source, Object value) {
        if (source==idField) {
            markupLanguage.setXmiId(idField.getText().trim());
        } 
    }
    
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==idField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "ID", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {        
        if (idField==source) {
            idField.setText(markupLanguage.getXmiId());
        }
    }
    /*
    protected void signalUIChange() {
        dObj.modelUpdatedFromUI();
    }*/
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if ("ID".equals(errorId)) return idField;
        return null;
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
	dObj.setChangedFromUI(true);
        if(evt.getSource()==nameComboBox) {
            markupLanguage.setName((String)nameComboBox.getSelectedItem());
            
        } else if(evt.getSource()==mimeTypeComboBox) {
            markupLanguage.setMimeType((String)mimeTypeComboBox.getSelectedItem());
        } else if(evt.getSource()==errorPageComboBox) {
            markupLanguage.setErrorPage((String)errorPageComboBox.getSelectedItem());
        } else if(evt.getSource()==defaultPageComboBox) {
            markupLanguage.setDefaultPage((String)defaultPageComboBox.getSelectedItem());
        }
        // TODO add your handling code here:
        dObj.modelUpdatedFromUI();
        //dObj.setChangedFromUI(true);
        dObj.setChangedFromUI(false);
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
        idField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        mimeTypeComboBox = new javax.swing.JComboBox();
        errorPageComboBox = new javax.swing.JComboBox();
        nameComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        defaultPageComboBox = new javax.swing.JComboBox();
        pagesContainerPanel = new javax.swing.JPanel();
        deleteButton = new javax.swing.JButton();

        jLabel1.setText("Name:");

        jLabel2.setText("ID:");

        jLabel4.setText("MIME Type:");

        jLabel3.setText("Error Page:");

        jLabel6.setText("Default Page:");

        defaultPageComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defaultPageComboBoxActionPerformed(evt);
            }
        });

        pagesContainerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        pagesContainerPanel.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle"); // NOI18N
        deleteButton.setText(bundle.getString("LBL_DeleteMarkupLanguage")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pagesContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel4)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(idField, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(nameComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(defaultPageComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(errorPageComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(mimeTypeComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 112, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                                .addComponent(deleteButton)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(nameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(mimeTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(errorPageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(defaultPageComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pagesContainerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 84, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void defaultPageComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_defaultPageComboBoxActionPerformed
            }//GEN-LAST:event_defaultPageComboBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox defaultPageComboBox;
    private javax.swing.JButton deleteButton;
    private javax.swing.JComboBox errorPageComboBox;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JComboBox mimeTypeComboBox;
    private javax.swing.JComboBox nameComboBox;
    private javax.swing.JPanel pagesContainerPanel;
    // End of variables declaration//GEN-END:variables
    
}
