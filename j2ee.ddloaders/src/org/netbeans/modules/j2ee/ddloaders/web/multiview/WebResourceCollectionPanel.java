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

import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.JTextField;

/**
 * WebResourceCollectionPanel.java
 *
 * Panel for adding and editing the web-resource-collection element of the web
 * deployment descriptor.
 * 
 * @author  ptliu
 */
public class WebResourceCollectionPanel extends javax.swing.JPanel {
    private static String GET = "GET";          //NOI18N
    private static String POST = "POST";        //NOI18N
    private static String HEAD = "HEAD";        //NOI18N
    private static String PUT = "PUT";          //NOI18N
    private static String OPTIONS = "OPTIONS";  //NOI18N
    private static String TRACE = "TRACE";      //NOI18N
    private static String DELETE = "DELETE";    //NOI18N
    private static String[] allMethods = {GET, POST, HEAD, PUT, OPTIONS, TRACE,
                                            DELETE};

    /** Creates new form WebResourceCollectionPanel */
    public WebResourceCollectionPanel() {
        initComponents();
    }
    
    public String getResourceName() {
        return resourceNameTF.getText();
    }
    
    public void setResourceName(String name) {
        resourceNameTF.setText(name);
    }
    
    public String[] getUrlPatterns() {
        StringTokenizer tokenizer = new StringTokenizer(urlPatternsTF.getText(),
                ","); //NOI18N
        
        ArrayList list = new ArrayList();
        while (tokenizer.hasMoreTokens()) {
            String pattern = tokenizer.nextToken().trim();
            if (pattern.length() > 0)
                list.add(pattern);
        }
        
        String[] result = new String[list.size()];
        
        return (String[]) list.toArray(result);
    }
    
    public void setUrlPatterns(String[] patterns) {
        urlPatternsTF.setText(WebResourceCollectionTableModel.getCommaSeparatedString(patterns));
    }
    
    public String getDescription() {
        return descriptionTF.getText();
    }
    
    public void setDescription(String description) {
        descriptionTF.setText(description);
    }
    
    public void setHttpMethods(String[] methods) {
        boolean allSelected = true;
        
        for (int i = 0; i < allMethods.length; i++) {
            boolean found = false;
            String method = allMethods[i];
            
            for (int j = 0; j < methods.length; j++) {
                if (method.equals(methods[j])) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                allSelected = false;
                break;
            }
        }
        
        if (allSelected) {
            allHttpMethodsRB.setSelected(true);
        } else {
            subsetHttpMethodsRB.setSelected(true);
            for (int i =0; i < methods.length; i++) {
                String method = methods[i];
                
                if (method.equals(GET)) {
                    getCB.setSelected(true);
                } else if (method.equals(PUT)) {
                    putCB.setSelected(true);
                } else if (method.equals(HEAD)) {
                    headCB.setSelected(true);
                } else if (method.equals(POST)) {
                    postCB.setSelected(true);
                } else if (method.equals(TRACE)) {
                    traceCB.setSelected(true);
                } else if (method.equals(DELETE)) {
                    deleteCB.setSelected(true);
                } else if (method.equals(OPTIONS)) {
                    optionsCB.setSelected(true);
                }
            }
        }
        
        updateVisualState();
    }
                  
    public String[] getSelectedHttpMethods() {
        if (allHttpMethodsRB.isSelected()) {
            return allMethods;
        } else if (subsetHttpMethodsRB.isSelected()) {
            ArrayList list = new ArrayList();
            
            if (getCB.isSelected()) {
                list.add(GET);
            }
            
            if (putCB.isSelected()) {
                list.add(PUT);
            }
            
            if (headCB.isSelected()) {
                list.add(HEAD);
            }
            
            if (postCB.isSelected()) {
                list.add(POST);
            }
            
            if (optionsCB.isSelected()) {
                list.add(OPTIONS);
            }
            
            if (traceCB.isSelected()) {
                list.add(TRACE);
            }
            
            if (deleteCB.isSelected()) {
                list.add(DELETE);
            }
            
            String[] results = new String[list.size()];
            return (String[]) list.toArray(results);
        }
        
        return new String[] {};
    }
    
    private void updateVisualState() {
        if (subsetHttpMethodsRB.isSelected()) {
            getCB.setEnabled(true);
            putCB.setEnabled(true);
            headCB.setEnabled(true);
            postCB.setEnabled(true);
            optionsCB.setEnabled(true);
            traceCB.setEnabled(true);
            deleteCB.setEnabled(true);
        } else {
            getCB.setEnabled(false);
            putCB.setEnabled(false);
            headCB.setEnabled(false);
            postCB.setEnabled(false);
            optionsCB.setEnabled(false);
            traceCB.setEnabled(false);
            deleteCB.setEnabled(false);
        }
    }
    
    public JTextField getResourceNameTF() {
        return resourceNameTF;
    }
    
    public JTextField getDescriptionTF() {
        return descriptionTF;
    }
    
    public JTextField getUrlPatternsTF() {
        return urlPatternsTF;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        resourceNameLabel = new javax.swing.JLabel();
        resourceNameTF = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTF = new javax.swing.JTextField();
        urlPatternsLabel = new javax.swing.JLabel();
        urlPatternsTF = new javax.swing.JTextField();
        httpMethodLabel = new javax.swing.JLabel();
        getCB = new javax.swing.JCheckBox();
        postCB = new javax.swing.JCheckBox();
        headCB = new javax.swing.JCheckBox();
        putCB = new javax.swing.JCheckBox();
        optionsCB = new javax.swing.JCheckBox();
        traceCB = new javax.swing.JCheckBox();
        deleteCB = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        allHttpMethodsRB = new javax.swing.JRadioButton();
        subsetHttpMethodsRB = new javax.swing.JRadioButton();

        resourceNameLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_resourceName_mnem").charAt(0));
        resourceNameLabel.setLabelFor(resourceNameTF);
        resourceNameLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_WebResourceCollectionName"));

        descriptionLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_description_mnem").charAt(0));
        descriptionLabel.setLabelFor(descriptionTF);
        descriptionLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_WebResourceCollectionDescription"));

        urlPatternsLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_urlPatterns_mnem2").charAt(0));
        urlPatternsLabel.setLabelFor(urlPatternsTF);
        urlPatternsLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_WebResourceCollectionUrlPatterns"));

        httpMethodLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_WebResourceCollectionHttpMethods"));

        getCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_get_mnem").charAt(0));
        getCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_GET"));
        getCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        getCB.setEnabled(false);
        getCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        postCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_post_mnem").charAt(0));
        postCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_POST"));
        postCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        postCB.setEnabled(false);
        postCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        headCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_head_mnem").charAt(0));
        headCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_HEAD"));
        headCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        headCB.setEnabled(false);
        headCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        putCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_put_mnem").charAt(0));
        putCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_PUT"));
        putCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        putCB.setEnabled(false);
        putCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        optionsCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_options_mnem").charAt(0));
        optionsCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_OPTIONS"));
        optionsCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optionsCB.setEnabled(false);
        optionsCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        traceCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_trace_mnem").charAt(0));
        traceCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_TRACE"));
        traceCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        traceCB.setEnabled(false);
        traceCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        deleteCB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_delete_mnem").charAt(0));
        deleteCB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_DELETE"));
        deleteCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        deleteCB.setEnabled(false);
        deleteCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel5.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("HINT_urlPatterns"));

        buttonGroup1.add(allHttpMethodsRB);
        allHttpMethodsRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_allHttpMethods_mnem").charAt(0));
        allHttpMethodsRB.setSelected(true);
        allHttpMethodsRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_AllHttpMethods"));
        allHttpMethodsRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allHttpMethodsRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allHttpMethodsRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allHttpMethodsRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(subsetHttpMethodsRB);
        subsetHttpMethodsRB.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_subsetHttpMethods_mnem").charAt(0));
        subsetHttpMethodsRB.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle").getString("LBL_SubsetOfHttpMethods"));
        subsetHttpMethodsRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        subsetHttpMethodsRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        subsetHttpMethodsRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subsetHttpMethodsRBActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resourceNameLabel)
                    .add(descriptionLabel)
                    .add(urlPatternsLabel)
                    .add(httpMethodLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(urlPatternsTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .add(descriptionTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .add(resourceNameTF, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 366, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jLabel5)
                            .add(allHttpMethodsRB)
                            .add(subsetHttpMethodsRB)))
                    .add(layout.createSequentialGroup()
                        .add(25, 25, 25)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(optionsCB)
                            .add(getCB)
                            .add(headCB)
                            .add(deleteCB))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(postCB)
                            .add(traceCB)
                            .add(putCB))
                        .add(163, 163, 163)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resourceNameLabel)
                    .add(resourceNameTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(descriptionLabel)
                    .add(descriptionTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlPatternsLabel)
                    .add(urlPatternsTF, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(httpMethodLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(allHttpMethodsRB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(subsetHttpMethodsRB)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(getCB)
                    .add(postCB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(headCB)
                    .add(putCB, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(optionsCB)
                    .add(traceCB))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(deleteCB)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void subsetHttpMethodsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subsetHttpMethodsRBActionPerformed
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_subsetHttpMethodsRBActionPerformed

    private void allHttpMethodsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allHttpMethodsRBActionPerformed
// TODO add your handling code here:
        updateVisualState();
    }//GEN-LAST:event_allHttpMethodsRBActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton allHttpMethodsRB;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox deleteCB;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTF;
    private javax.swing.JCheckBox getCB;
    private javax.swing.JCheckBox headCB;
    private javax.swing.JLabel httpMethodLabel;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JCheckBox optionsCB;
    private javax.swing.JCheckBox postCB;
    private javax.swing.JCheckBox putCB;
    private javax.swing.JLabel resourceNameLabel;
    private javax.swing.JTextField resourceNameTF;
    private javax.swing.JRadioButton subsetHttpMethodsRB;
    private javax.swing.JCheckBox traceCB;
    private javax.swing.JLabel urlPatternsLabel;
    private javax.swing.JTextField urlPatternsTF;
    // End of variables declaration//GEN-END:variables
    
}
