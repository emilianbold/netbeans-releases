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
package org.netbeans.modules.j2ee.websphere6.dd.loaders.ui;

import org.netbeans.modules.j2ee.websphere6.dd.beans.DDXmiConstants;
import org.netbeans.modules.j2ee.websphere6.dd.beans.WSAppExt;
import org.netbeans.modules.j2ee.websphere6.dd.loaders.WSMultiViewDataObject;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.Error;

/**
 *
 * @author  dlm198383
 */
public class WSAppExtAttributesPanel extends SectionInnerPanel
        implements java.awt.event.ItemListener, javax.swing.event.ChangeListener, DDXmiConstants{
    
    WSAppExt appext;
    WSMultiViewDataObject dObj;
    
    /** Creates new form WSAppExtAttributesPanel */
    public WSAppExtAttributesPanel(SectionView view, WSMultiViewDataObject dObj,  WSAppExt appext)  {
        super(view);
        this.dObj=dObj;
        this.appext=appext;
        initComponents();
        idField.setText(appext.getXmiId());
        hrefField.setText(appext.getApplicationHref());
        reloadIntervalCheckBox.setSelected(appext.getReload());
        shareSessionCheckBox.setSelected(appext.getSharedSession());
        reloadIntervalSpinner.setModel(new javax.swing.SpinnerNumberModel(0,0,600,1));
        reloadIntervalSpinner.setValue(new Integer(appext.getReloadInterval()));
        reloadIntervalCheckBox.addItemListener(this);
        shareSessionCheckBox.addItemListener(this);
        reloadIntervalSpinner.addChangeListener(this);
        addModifier(hrefField);
        addModifier(idField);
        
        addValidatee(hrefField);
        addValidatee(idField);
        
        
        ModuleExtensionTableModel model = new ModuleExtensionTableModel(dObj.getModelSynchronizer());
        ModuleExtensionTablePanel ptp= new ModuleExtensionTablePanel(dObj, model);
        ptp.setModel(appext,appext.getModuleExtensions());
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
    public void setValue(javax.swing.JComponent source,Object value) {
        if(source==hrefField) {
            appext.setApplicationHref((String)value);
        }
        if(source==idField) {
            appext.setXmiId((String)value);
        }
    }
    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }
    
    public javax.swing.JComponent getErrorComponent(String errorId) {
        if("Name".equals(errorId)) return idField;
        if("Application Id".equals(errorId)) return hrefField;
        return null;
    }
    public void stateChanged(javax.swing.event.ChangeEvent evt) {
        appext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
        dObj.modelUpdatedFromUI();
    }
    public void itemStateChanged(java.awt.event.ItemEvent evt) {
        if(evt.getSource()==reloadIntervalCheckBox) {
            appext.setReload(reloadIntervalCheckBox.isSelected());
            if(reloadIntervalCheckBox.isSelected()) {
                appext.setReloadInterval(reloadIntervalSpinner.getValue().toString());
                reloadIntervalSpinner.setEnabled(true);
            } else {
                appext.setReloadInterval(null);
                reloadIntervalSpinner.setEnabled(false);
            }
        } else if(evt.getSource()==shareSessionCheckBox) {
            appext.setSharedSession(shareSessionCheckBox.isSelected());
        }
        dObj.modelUpdatedFromUI();
    }
    
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        if (comp==idField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Name", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
        if (comp==hrefField) {
            String val = (String)value;
            if (val.length()==0) {
                getSectionView()
                .getErrorPanel()
                .setError(new Error(Error.MISSING_VALUE_MESSAGE, "Application Id", comp));
                return;
            }
            getSectionView().getErrorPanel().clearError();
        }
    }
    
    public void rollbackValue(javax.swing.text.JTextComponent source) {
        if (idField==source) {
            idField.setText(appext.getXmiId());
        } else if(hrefField==source) {
            hrefField.setText(appext.getApplicationHref());
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
        jLabel1 = new javax.swing.JLabel();
        idField = new javax.swing.JTextField();
        shareSessionCheckBox = new javax.swing.JCheckBox();
        reloadIntervalCheckBox = new javax.swing.JCheckBox();
        reloadIntervalSpinner = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        hrefField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();

        jLabel1.setText("Name:");

        shareSessionCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_SharedSession"));
        shareSessionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        shareSessionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        reloadIntervalCheckBox.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_ReloadInterval"));
        reloadIntervalCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reloadIntervalCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        reloadIntervalSpinner.setFont(new java.awt.Font("Courier", 0, 12));

        jLabel2.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_ApplicationId"));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/websphere6/dd/loaders/ui/Bundle").getString("LBL_ModuleExtensions"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(shareSessionCheckBox)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                    .add(jLabel2)
                                    .add(jLabel1)
                                    .add(reloadIntervalCheckBox))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(hrefField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                                    .add(reloadIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(idField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 347, Short.MAX_VALUE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))))))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLabel3)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(idField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(hrefField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(reloadIntervalCheckBox)
                    .add(reloadIntervalSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(11, 11, 11)
                .add(shareSessionCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField hrefField;
    private javax.swing.JTextField idField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox reloadIntervalCheckBox;
    private javax.swing.JSpinner reloadIntervalSpinner;
    private javax.swing.JCheckBox shareSessionCheckBox;
    // End of variables declaration//GEN-END:variables
    
}
