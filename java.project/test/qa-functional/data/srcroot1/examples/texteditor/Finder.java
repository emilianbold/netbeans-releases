/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package examples.texteditor;

public class Finder extends javax.swing.JDialog {

    /** Initializes the Form */
    public Finder(java.awt.Frame parent, javax.swing.JTextArea textEditor) {
        super(parent, true);
        this.textEditor = textEditor;
        initComponents();
        pack();
        findField.requestFocus();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        findField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        findButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setTitle("Find");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        getContentPane().add(findField, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        findButton.setText("Find");
        findButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findButtonActionPerformed(evt);
            }
        });

        jPanel1.add(findButton);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        jPanel1.add(closeButton);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    private void findButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        // Add your handling code here:
        String text = textEditor.getText ();
        String textToFind = findField.getText ();
        if (!"".equals (textToFind)) {
            int index = text.indexOf (textToFind);
            if (index != -1) {
                textEditor.setCaretPosition (index);
                closeDialog (null);
            } else {
                java.awt.Toolkit.getDefaultToolkit ().beep ();
            }
        }
    }//GEN-LAST:event_findButtonActionPerformed

    private void closeButtonActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_closeButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible (false);
        dispose ();
    }//GEN-LAST:event_closeDialog


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton findButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JTextField findField;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTextArea textEditor;


}
