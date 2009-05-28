/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        resourceNameLabel.setLabelFor(resourceNameTF);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/ddloaders/web/multiview/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(resourceNameLabel, bundle.getString("LBL_WebResourceCollectionName")); // NOI18N

        descriptionLabel.setLabelFor(descriptionTF);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, bundle.getString("LBL_WebResourceCollectionDescription")); // NOI18N

        urlPatternsLabel.setLabelFor(urlPatternsTF);
        org.openide.awt.Mnemonics.setLocalizedText(urlPatternsLabel, bundle.getString("LBL_WebResourceCollectionUrlPatterns")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(httpMethodLabel, bundle.getString("LBL_WebResourceCollectionHttpMethods")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(getCB, bundle.getString("LBL_GET")); // NOI18N
        getCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        getCB.setEnabled(false);
        getCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(postCB, bundle.getString("LBL_POST")); // NOI18N
        postCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        postCB.setEnabled(false);
        postCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(headCB, bundle.getString("LBL_HEAD")); // NOI18N
        headCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        headCB.setEnabled(false);
        headCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(putCB, bundle.getString("LBL_PUT")); // NOI18N
        putCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        putCB.setEnabled(false);
        putCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(optionsCB, bundle.getString("LBL_OPTIONS")); // NOI18N
        optionsCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        optionsCB.setEnabled(false);
        optionsCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(traceCB, bundle.getString("LBL_TRACE")); // NOI18N
        traceCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        traceCB.setEnabled(false);
        traceCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(deleteCB, bundle.getString("LBL_DELETE")); // NOI18N
        deleteCB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        deleteCB.setEnabled(false);
        deleteCB.setMargin(new java.awt.Insets(0, 0, 0, 0));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, bundle.getString("HINT_urlPatterns")); // NOI18N

        buttonGroup1.add(allHttpMethodsRB);
        allHttpMethodsRB.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(allHttpMethodsRB, bundle.getString("LBL_AllHttpMethods")); // NOI18N
        allHttpMethodsRB.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        allHttpMethodsRB.setMargin(new java.awt.Insets(0, 0, 0, 0));
        allHttpMethodsRB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allHttpMethodsRBActionPerformed(evt);
            }
        });

        buttonGroup1.add(subsetHttpMethodsRB);
        org.openide.awt.Mnemonics.setLocalizedText(subsetHttpMethodsRB, bundle.getString("LBL_SubsetOfHttpMethods")); // NOI18N
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
                    .add(jLabel5)
                    .add(allHttpMethodsRB)
                    .add(subsetHttpMethodsRB)
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
        updateVisualState();
    }//GEN-LAST:event_subsetHttpMethodsRBActionPerformed

    private void allHttpMethodsRBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allHttpMethodsRBActionPerformed
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
