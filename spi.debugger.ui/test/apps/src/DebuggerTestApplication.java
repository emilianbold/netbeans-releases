/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * The Original Code is NetBeans.
 * The Initial Developer of the Original Code is Sun Microsystems, Inc.
 * Portions created by Sun Microsystems, Inc. are Copyright (C) 2003
 * All Rights Reserved.
 *
 * Contributor(s): Sun Microsystems, Inc.
 */

public class DebuggerTestApplication extends javax.swing.JFrame {
    
    /** Creates new form TestApp */
    public DebuggerTestApplication() {
        this.setTitle("Debugger Test Application");
        initComponents();
        
        counterThread = new Thread(new Runnable() {
            public void run() {
                Thread thisThread = Thread.currentThread();
                while (counterThread == thisThread) {
                    updateCounter();
                    try {
                        Thread.currentThread().sleep(1000);
                        if (counterThreadSuspended) {
                            synchronized(counterThread) {
                                while (counterThreadSuspended) {
                                    counterThread.wait();
                                }
                            }
                        }
                        
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        });
        counterThread.setName("counterThread");
        counterThread.start();
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jProgressBar1 = new javax.swing.JProgressBar();
        jProgressBar1.setMaximum(MAX_COUNT);

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                DebuggerTestApplication.this.exitForm(evt);
            }
        });

        jButton1.setText("Stop");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DebuggerTestApplication.this.jButton1ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton1);

        jButton2.setText("Clear");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DebuggerTestApplication.this.jButton2ActionPerformed(evt);
            }
        });

        jPanel1.add(jButton2);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 36));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("jLabel1");
        getContentPane().add(jLabel1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jProgressBar1, java.awt.BorderLayout.NORTH);

        pack();
    }//GEN-END:initComponents
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (!counterThreadSuspended) {
            counterThreadSuspended = !counterThreadSuspended;
            jButton1.setText("Start");
        } else {
            synchronized(counterThread) {
                counterThreadSuspended = !counterThreadSuspended;
                counterThread.notify();
                jButton1.setText("Stop");
            }
            
        }
    }//GEN-LAST:event_jButton1ActionPerformed
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        counter = 0;        
        jLabel1.setText(String.valueOf(counter));
    }//GEN-LAST:event_jButton2ActionPerformed
    
    private void updateCounter() {
        counter = (counter < MAX_COUNT) ? ++counter : 0 ;
        jLabel1.setText(String.valueOf(counter));
        jProgressBar1.setValue(counter);
    }
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new DebuggerTestApplication().show();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton jButton1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JProgressBar jProgressBar1;
    // End of variables declaration//GEN-END:variables
    private Thread counterThread;
    public volatile boolean  counterThreadSuspended = false;
    private int counter = 0;
    
    private static final int MAX_COUNT = 13;
    
}
