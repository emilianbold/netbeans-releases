/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.xml.wizard;

import java.io.File;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import org.netbeans.modules.xml.util.Util;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author  Sonali
 */
public class XMLContentPanel extends AbstractPanel {
    private DefaultComboBoxModel rootModel;
    private boolean visible= false;
    SchemaParser.SchemaInfo schemaInfo;
    
    /** Creates new form XMLContentPanel */
    public XMLContentPanel() {
        initComponents(); 
        initAccessibility();
    }
        
    public XMLContentPanel(boolean value) {
        this.visible = value;
        initComponents(); 
        initAccessibility();
    }

    private void initAccessibility() {
        Util util = Util.THIS;
        getAccessibleContext().setAccessibleDescription(titleLabel.getText());
        attributes.setMnemonic(util.getChar(
                XMLContentPanel.class, "XMLContentPanel.attributes.mne"));
        elements.setMnemonic(util.getChar(
                XMLContentPanel.class, "XMLContentPanel.elements.mne"));
        
    }
     
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        attributes = new javax.swing.JCheckBox();
        attributes.setSelected(true);
        elements = new javax.swing.JCheckBox();
        elements.setSelected(true);
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        occurSpinner = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        depthSpinner = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel7 = new javax.swing.JLabel();
        jLabel7.setVisible(visible);
        rootElementComboBox = new javax.swing.JComboBox();
        rootElementComboBox.setVisible(visible);

        setName(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "PROP_xml_content_panel_name")); // NOI18N

        titleLabel.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.titleLabel.text")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.jLabel1.text")); // NOI18N

        attributes.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.attributes.text")); // NOI18N
        attributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                attributesActionPerformed(evt);
            }
        });

        elements.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.elements.text")); // NOI18N
        elements.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                elementsActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.jLabel2.text")); // NOI18N

        jLabel3.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.jLabel3.text")); // NOI18N

        jLabel4.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.jLabel4.text")); // NOI18N

        jLabel5.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.jLabel5.text")); // NOI18N

        jLabel6.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.jLabel6.text")); // NOI18N

        jLabel7.setText(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "LBL_SchemaPanel_Root_Element")); // NOI18N

        rootElementComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        rootElementComboBox.setMinimumSize(new java.awt.Dimension(60, 60));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(attributes, javax.swing.GroupLayout.PREFERRED_SIZE, 191, Short.MAX_VALUE)
                                .addGap(523, 523, 523))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(elements, javax.swing.GroupLayout.PREFERRED_SIZE, 187, Short.MAX_VALUE)
                                .addGap(527, 527, 527))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 79, Short.MAX_VALUE)
                                        .addGap(46, 46, 46))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 95, Short.MAX_VALUE)
                                        .addGap(30, 30, 30))
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 125, Short.MAX_VALUE))
                                .addGap(7, 7, 7)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(17, 17, 17)
                                        .addComponent(rootElementComboBox, 0, 65, Short.MAX_VALUE)
                                        .addGap(500, 500, 500))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(42, 42, 42)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(depthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                            .addComponent(occurSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE))
                                        .addGap(66, 66, 66)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 398, Short.MAX_VALUE)
                                                .addGap(14, 14, 14))
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 412, Short.MAX_VALUE)))))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(titleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 373, Short.MAX_VALUE)
                                .addGap(341, 341, 341))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE)
                                .addGap(623, 623, 623))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 113, Short.MAX_VALUE)
                                .addGap(601, 601, 601)))
                        .addGap(12, 12, 12))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(titleLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(rootElementComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(jLabel1)
                .addGap(9, 9, 9)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(elements, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(attributes)
                .addGap(26, 26, 26)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(occurSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(depthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel6))
                .addContainerGap(72, Short.MAX_VALUE))
        );

        attributes.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.attributes.text")); // NOI18N
        attributes.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.attributes.text")); // NOI18N
        elements.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.elements.text")); // NOI18N
        elements.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(XMLContentPanel.class, "XMLContentPanel.elements.text")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void attributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_attributesActionPerformed
        boolean attr = attributes.isSelected();
}//GEN-LAST:event_attributesActionPerformed

    private void elementsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_elementsActionPerformed
       boolean elem = elements.isSelected();
    }//GEN-LAST:event_elementsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox attributes;
    private javax.swing.JSpinner depthSpinner;
    private javax.swing.JCheckBox elements;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSpinner occurSpinner;
    private javax.swing.JComboBox rootElementComboBox;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    protected void updateModel() {
       XMLContentAttributes contentAttr = new XMLContentAttributes(model.getPrefix());
       contentAttr.setOptionalAttributes(attributes.isSelected());
       contentAttr.setOptionalElements(elements.isSelected());
       
       contentAttr.setPreferredOccurences(((SpinnerNumberModel)occurSpinner.getModel()).getNumber().intValue());
       contentAttr.setDepthPreferrence(((SpinnerNumberModel)depthSpinner.getModel()).getNumber().intValue());
       
       model.setXMLContentAttributes(contentAttr);
       if(visible) {
           Object root = rootElementComboBox.getSelectedItem();
           model.setRoot(root == null ? null : root.toString());
       }
    }

    @Override
    protected void initView() {
       attributes.setSelected(true);
       elements.setSelected(true);
       
       occurencesModel = new SpinnerNumberModel(3, 0, 10, 1);
       occurSpinner.setModel(occurencesModel);
       
       depthModel = new SpinnerNumberModel(2, 0, 10, 1);
       depthSpinner.setModel(depthModel);
       
        rootModel = new DefaultComboBoxModel();
        rootElementComboBox.setModel(rootModel);       
        
        if(getSchemaInfo() == null)
            return;
        
        if(schemaInfo.roots.size() ==0){
            //TODO: should have some error message
            //String errMsg =  NbBundle.getMessage(XMLContentPanel.class, "MSG_XMLContentPanel_No_Root");
            //templateWizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, errMsg);
            return;
        }
        Iterator it = schemaInfo.roots.iterator();
            while (it.hasNext()) {
                 String next = (String) it.next();
                 rootModel.addElement(next);
            }    
    }
    
    private SchemaParser.SchemaInfo getSchemaInfo() {
        if(schemaInfo != null)
            return schemaInfo;
        
        File f = new File(model.getPrimarySchema());
        if (f == null ) return null;

        // fix for issue #172121 - IllegalArgumentException: Parameter file was not normalized.
        f = f.getAbsoluteFile();
        
        // a note for http based xsd files:
        // this combo box in this panel is only visible
        // for files on disk, so ignore http based xsd files

        if (! f.exists() ) {
           return null;
        } 
        FileObject fobj = FileUtil.toFileObject(f);
        schemaInfo = SchemaParser.getRootElements(fobj);
        return schemaInfo;
    }

    @Override
    protected void updateView() {
        
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(XMLContentPanel.class, "PROP_xml_content_panel_name");//noi18n
    }
    
    public boolean isPanelValid() {
        //valid if root selected in previous panel
        if(model.getRoot() != null)
            return true;
        
        if(getSchemaInfo() == null)
            return false;
        
        if(schemaInfo.roots.size() == 0){
            //no root elements
            return false;
        }
        
        return true;
    }    
    
    SpinnerModel occurencesModel;
    SpinnerModel depthModel;
}
