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

import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerModel;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSWebExt;
import org.netbeans.modules.xml.multiview.*;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.webext.*;

/**
 *
 * @author  dlm198383
 */
public class WSWebExtAttributesPanel extends  SectionInnerPanel  implements java.awt.event.ItemListener, javax.swing.event.ChangeListener {
    
    WSWebExt webext;
    WSWebExtDataObject dObj;
    
    /** Creates new form WSWebExtAttributesPanel */
    public WSWebExtAttributesPanel(SectionView view, WSWebExtDataObject dObj,  WSWebExt webext) {
        super(view);
        this.dObj=dObj;
        this.webext=webext;
        initComponents();
        fileServingCheckBox.setSelected(webext.getFileServingEnabled());        
        serveServletsCheckBox.setSelected(webext.getServeServletsByClassname());
        directoryBrowsingCheckBox.setSelected(webext.getDirectoryBrowsing());
        reloadIntervalCheckBox.setSelected(webext.getReload());
        SpinnerModel sm=new javax.swing.SpinnerNumberModel(0,0,600,1);
        reloadIntervalSpinner.setModel(sm);
        reloadIntervalSpinner.setValue(new Integer(webext.getReloadInterval()));
        precompileJPSCheckBox.setSelected(webext.getPrecompileJSPs());
        autoRequestEncCheckBox.setSelected(webext.getAutoRequestEncoding());
        autoResponseEncCheckBox.setSelected(webext.getAutoResponseEncoding());
        autoLoadFiltersCheckBox.setSelected(webext.getAutoLoadFilters());
        additionalClassPathField.setText(webext.getAdditionalClassPath());
        defaultErrorPageField.setText(webext.getDefaultErrorPage());
        hrefField.setText(webext.getWebApplicationHref());
        nameField.setText(webext.getXmiId());
        
        fileServingCheckBox.addItemListener(this);
        serveServletsCheckBox.addItemListener(this);
        directoryBrowsingCheckBox.addItemListener(this);
        reloadIntervalCheckBox.addItemListener(this);
        precompileJPSCheckBox.addItemListener(this);
        autoRequestEncCheckBox.addItemListener(this);
        autoResponseEncCheckBox.addItemListener(this);
        autoLoadFiltersCheckBox.addItemListener(this);
        reloadIntervalSpinner.addChangeListener(this);
        addModifier(additionalClassPathField);        
        addModifier(defaultErrorPageField);
        addModifier(hrefField);
        addModifier(nameField);
        //addModifier(virtualHostField);
        //getSectionView().getErrorPanel().clearError();
    }
    public void setValue(javax.swing.JComponent source,Object value) {
        if (source==additionalClassPathField) {
            webext.setAdditionalClassPath((String)value);
        } else if (source==defaultErrorPageField) {
            webext.setDefaultErrorPage((String)value);
        } else if(source==hrefField) {
            webext.setWebApplicationHref((String)value);
        }
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        //if ("name".equals(errorId)) return nameField;
        //if ("vhn".equals(errorId)) return virtualHostField;
        return null;
    }
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        webext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
        dObj.modelUpdatedFromUI();
    }
    
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        // TODO add your handling code here:
        dObj.setChangedFromUI(true);
        webext.setPrecompileJSPs(precompileJPSCheckBox.isSelected());
        webext.setServeServletsByClassname(serveServletsCheckBox.isSelected());
        webext.setDirectoryBrowsing(directoryBrowsingCheckBox.isSelected());
        webext.setAutoRequestEncoding(autoRequestEncCheckBox.isSelected());
        webext.setAutoResponseEncoding(autoResponseEncCheckBox.isSelected());
        webext.setAutoLoadFilters(autoLoadFiltersCheckBox.isSelected());
        webext.setFileServingEnabled(fileServingCheckBox.isSelected());
        webext.setReload(reloadIntervalCheckBox.isSelected());
        webext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
        dObj.modelUpdatedFromUI();
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

        reloadIntervalCheckBox = new javax.swing.JCheckBox();
        reloadIntervalSpinner = new javax.swing.JSpinner();
        additionalClassPathLabel = new javax.swing.JLabel();
        additionalClassPathField = new javax.swing.JTextField();
        fileServingCheckBox = new javax.swing.JCheckBox();
        directoryBrowsingCheckBox = new javax.swing.JCheckBox();
        serveServletsCheckBox = new javax.swing.JCheckBox();
        precompileJPSCheckBox = new javax.swing.JCheckBox();
        autoRequestEncCheckBox = new javax.swing.JCheckBox();
        autoResponseEncCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        defaultErrorPageField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        nameField = new javax.swing.JTextField();
        autoLoadFiltersCheckBox = new javax.swing.JCheckBox();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle"); // NOI18N
        reloadIntervalCheckBox.setText(bundle.getString("LBL_ReloadInterval")); // NOI18N
        reloadIntervalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reloadIntervalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        reloadIntervalSpinner.setFont(new java.awt.Font("Courier", 0, 12));

        additionalClassPathLabel.setText("Additional Class Path:");

        fileServingCheckBox.setText("File Serving");
        fileServingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileServingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        directoryBrowsingCheckBox.setText("Directory Browsing");
        directoryBrowsingCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        directoryBrowsingCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        serveServletsCheckBox.setText("Serve Servlets by Classname");
        serveServletsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        serveServletsCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        precompileJPSCheckBox.setText("Precompile JSPs");
        precompileJPSCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        precompileJPSCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        autoRequestEncCheckBox.setText("Auto Request Encoding");
        autoRequestEncCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoRequestEncCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        autoResponseEncCheckBox.setText("Auto Response Encoding");
        autoResponseEncCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoResponseEncCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setText("Default Error Page:");

        jLabel2.setText("Name in Web.xml:");

        jLabel3.setText("Name:");

        autoLoadFiltersCheckBox.setText("Auto Load Filters");
        autoLoadFiltersCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoLoadFiltersCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serveServletsCheckBox)
                    .addComponent(fileServingCheckBox)
                    .addComponent(directoryBrowsingCheckBox)
                    .addComponent(precompileJPSCheckBox)
                    .addComponent(autoRequestEncCheckBox)
                    .addComponent(autoResponseEncCheckBox)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(reloadIntervalCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reloadIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(additionalClassPathLabel)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameField, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .addComponent(hrefField, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .addComponent(additionalClassPathField, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
                            .addComponent(defaultErrorPageField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)))
                    .addComponent(autoLoadFiltersCheckBox))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fileServingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serveServletsCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(directoryBrowsingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(reloadIntervalCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(precompileJPSCheckBox))
                    .addComponent(reloadIntervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoRequestEncCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoResponseEncCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoLoadFiltersCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(additionalClassPathLabel)
                    .addComponent(additionalClassPathField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(defaultErrorPageField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(hrefField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField additionalClassPathField;
    private javax.swing.JLabel additionalClassPathLabel;
    private javax.swing.JCheckBox autoLoadFiltersCheckBox;
    private javax.swing.JCheckBox autoRequestEncCheckBox;
    private javax.swing.JCheckBox autoResponseEncCheckBox;
    private javax.swing.JTextField defaultErrorPageField;
    private javax.swing.JCheckBox directoryBrowsingCheckBox;
    private javax.swing.JCheckBox fileServingCheckBox;
    private javax.swing.JTextField hrefField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JTextField nameField;
    private javax.swing.JCheckBox precompileJPSCheckBox;
    private javax.swing.JCheckBox reloadIntervalCheckBox;
    private javax.swing.JSpinner reloadIntervalSpinner;
    private javax.swing.JCheckBox serveServletsCheckBox;
    // End of variables declaration//GEN-END:variables
    /*
    public javax.swing.JTextField getAdditionalClassPathField() {
        return additionalClassPathField;
    }
    public javax.swing.JCheckBox getAutoRequestEncCheckBox() {
        return autoRequestEncCheckBox;
    }
    public javax.swing.JCheckBox getAutoResponseEncCheckBox() {
        return autoResponseEncCheckBox;
    }
    public javax.swing.JCheckBox getDirectoryBrowsingCheckBox() {
        return directoryBrowsingCheckBox;
    }
    public javax.swing.JCheckBox getFileServingCheckBox(){
        return fileServingCheckBox;
    }
    public javax.swing.JCheckBox getPrecompileJPSCheckBox(){
        return precompileJPSCheckBox;
    }
    public javax.swing.JCheckBox getReloadIntervalCheckBox(){
        return reloadIntervalCheckBox;
    }
    public javax.swing.JSpinner getReloadIntervalSpinner(){
        return reloadIntervalSpinner;
    }
    public javax.swing.JCheckBox getServeServletsCheckBox(){
        return  serveServletsCheckBox;
    }*/
    
}
