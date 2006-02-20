/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.copy;

import org.netbeans.modules.subversion.ui.wizards.*;

/**
 *
 * @author  Petr Kuzel
 */
public class CreateCopyPanel extends javax.swing.JPanel {
    
    /** Creates new form WorkdirPanel */
    public CreateCopyPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jLabel4 = new javax.swing.JLabel();

        setName("Message");
        jLabel1.setText("Describe the copy purpose:");

        messageTextArea.setColumns(20);
        messageTextArea.setRows(5);
        jScrollPane1.setViewportView(messageTextArea);

        jLabel4.setText("Repository Folder:");

        browseRepositoryButton.setText("Browse...");

        urlComboBox.setEditable(true);

        warningLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/subversion/ui/resources/warning.png")));
        warningLabel.setText("Warning - there are localy modified files!");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 615, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(jLabel4)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(urlComboBox, 0, 395, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseRepositoryButton))
                    .add(org.jdesktop.layout.GroupLayout.LEADING, warningLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(browseRepositoryButton)
                    .add(urlComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
                .add(16, 16, 16)
                .add(warningLabel)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    final javax.swing.JButton browseRepositoryButton = new javax.swing.JButton();
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollPane jScrollPane1;
    final javax.swing.JTextArea messageTextArea = new javax.swing.JTextArea();
    final javax.swing.JComboBox urlComboBox = new javax.swing.JComboBox();
    final javax.swing.JLabel warningLabel = new javax.swing.JLabel();
    // End of variables declaration//GEN-END:variables
    
}
