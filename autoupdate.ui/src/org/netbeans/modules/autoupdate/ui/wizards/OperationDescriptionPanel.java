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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.ui.wizards;

import org.jdesktop.layout.GroupLayout;

/**
 *
 * @author  Jiri Rechtacek
 */
public class OperationDescriptionPanel extends javax.swing.JPanel {
    private String tpPrimaryTitleText;
    private String tpPrimaryPluginsText;
    private String tpDependingTitleText;
    private String tpDependingPluginsText;
    /** Creates new form OperationDescriptionPanel */
    public OperationDescriptionPanel (String primary, String primaryU, String depending, String dependingU, boolean hasRequired) {
        this.tpPrimaryTitleText = primary;
        this.tpPrimaryPluginsText = primaryU;
        this.tpDependingTitleText = depending;
        this.tpDependingPluginsText = dependingU;
        customInitComponents ();
        if (! hasRequired) {
            tpDependingTitle.setVisible (false);
            tpDependingPlugins.setVisible (false);
        }
    }
    
    // XXX: cannot be designed by mattise
    private void customInitComponents () {
        tpPrimaryTitle = new javax.swing.JTextPane();
        tpPrimaryPlugins = new javax.swing.JTextPane();
        tpDependingTitle = new javax.swing.JTextPane();
        tpDependingPlugins = new javax.swing.JTextPane();

        tpPrimaryTitle.setContentType("text/html"); // NOI18N
        tpPrimaryTitle.setEditable(false);
        tpDependingTitle.setOpaque (false);

        tpPrimaryPlugins.setContentType ("text/html"); // NOI18N
        tpPrimaryPlugins.setEditable(false);
        tpPrimaryPlugins.setOpaque (false);

        tpDependingTitle.setContentType ("text/html"); // NOI18N
        tpDependingTitle.setEditable(false);
        tpDependingTitle.setOpaque (false);

        tpDependingPlugins.setContentType ("text/html"); // NOI18N
        tpDependingPlugins.setEditable(false);
        tpDependingPlugins.setOpaque (false);

        tpPrimaryTitle.setText(tpPrimaryTitleText);
        tpPrimaryPlugins.setText(tpPrimaryPluginsText);
        tpDependingTitle.setText(tpDependingTitleText);
        tpDependingPlugins.setText(tpDependingPluginsText);
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(tpDependingPlugins, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tpDependingTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(tpPrimaryPlugins, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tpPrimaryTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup (GroupLayout.PREFERRED_SIZE)
            .add(layout.createSequentialGroup()
                //.add (tpPrimaryTitle, GroupLayout.PREFERRED_SIZE, tpPrimaryTitle.getPreferredSize ().height, GroupLayout.PREFERRED_SIZE)
                .add (tpPrimaryTitle, GroupLayout.DEFAULT_SIZE, 40, 40)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add (tpPrimaryPlugins, GroupLayout.PREFERRED_SIZE, tpPrimaryPlugins.getPreferredSize ().height, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add (0, 30, 30)
                .add (tpDependingTitle, GroupLayout.DEFAULT_SIZE, 80, 80)
                //.add (tpDependingTitle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add (tpDependingPlugins, GroupLayout.PREFERRED_SIZE, tpDependingPlugins.getPreferredSize ().height, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                //.add(tpDependingPlugins, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                //.add(90, 90, 90)
                )
        );
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        tpPrimaryTitle = new javax.swing.JTextPane();
        tpPrimaryPlugins = new javax.swing.JTextPane();
        tpDependingTitle = new javax.swing.JTextPane();
        tpDependingPlugins = new javax.swing.JTextPane();

        tpPrimaryTitle.setContentType(org.openide.util.NbBundle.getMessage(OperationDescriptionPanel.class, "OperationDescriptionPanel.tpPrimaryTitle.contentType")); // NOI18N
        tpPrimaryTitle.setEditable(false);

        tpPrimaryPlugins.setContentType(org.openide.util.NbBundle.getMessage(OperationDescriptionPanel.class, "OperationDescriptionPanel.tpPrimaryPlugins.contentType")); // NOI18N
        tpPrimaryPlugins.setEditable(false);

        tpDependingTitle.setContentType(org.openide.util.NbBundle.getMessage(OperationDescriptionPanel.class, "OperationDescriptionPanel.tpDependingTitle.contentType")); // NOI18N
        tpDependingTitle.setEditable(false);

        tpDependingPlugins.setContentType(org.openide.util.NbBundle.getMessage(OperationDescriptionPanel.class, "OperationDescriptionPanel.tpDependingPlugins.contentType")); // NOI18N
        tpDependingPlugins.setEditable(false);

        tpPrimaryTitle.setText(tpPrimaryTitleText);
        tpPrimaryPlugins.setText(tpPrimaryPluginsText);
        tpDependingTitle.setText(tpDependingTitleText);
        tpDependingPlugins.setText(tpDependingPluginsText);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(tpDependingPlugins, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tpDependingTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(49, 49, 49)
                        .add(tpPrimaryPlugins, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .addContainerGap()
                        .add(tpPrimaryTitle, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(tpPrimaryTitle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpPrimaryPlugins)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpDependingTitle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(tpDependingPlugins, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(90, 90, 90))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane tpDependingPlugins;
    private javax.swing.JTextPane tpDependingTitle;
    private javax.swing.JTextPane tpPrimaryPlugins;
    private javax.swing.JTextPane tpPrimaryTitle;
    // End of variables declaration//GEN-END:variables
    
}
