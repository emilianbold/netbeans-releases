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

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.util.NbBundle;


/**
 * @author  Jan Jancura
 */
// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class ExceptionBreakpointPanel extends JPanel implements Controller {
// ====
public class ExceptionBreakpointPanel extends JPanel implements Controller, org.openide.util.HelpCtx.Provider {
// </RAVE>
    
    private ActionsPanel                actionsPanel; 
    private ExceptionBreakpoint         breakpoint;
    private boolean                     createBreakpoint = false;
    private static Map                  exceptions = new TreeMap ();
    
    static {
        exceptions.put ("ArrayIndexOutOfBoundsException", "java.lang");
        exceptions.put ("AssertionError", "java.lang");
        exceptions.put ("ClassCastException", "java.lang");
        exceptions.put ("ClassNotFoundException", "java.lang");
        exceptions.put ("IllegalAccessException", "java.lang");
        exceptions.put ("IllegalArgumentException", "java.lang");
        exceptions.put ("IndexOutOfBoundsException", "java.lang");
        exceptions.put ("NullPointerException", "java.lang");
        exceptions.put ("RuntimeException", "java.lang");
        exceptions.put ("SecurityException", "java.lang");
        exceptions.put ("StringIndexOutOfBoundsException", "java.lang");
        exceptions.put ("UnsupportedOperationException", "java.lang");
        exceptions.put ("IOException", "java.io");
    }
    
    private static ExceptionBreakpoint creteBreakpoint () {
        ExceptionBreakpoint mb = ExceptionBreakpoint.create (
            EditorContextBridge.getCurrentClassName (),
            ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED_UNCATCHED
        );
        mb.setPrintText (
            NbBundle.getBundle (ExceptionBreakpointPanel.class).getString 
                ("CTL_Exception_Breakpoint_Print_Text")
        );
        return mb;
    }
    
    
    /** Creates new form LineBreakpointPanel */
    public ExceptionBreakpointPanel () {
        this (creteBreakpoint ());
        createBreakpoint = true;
    }
    
    /** Creates new form LineBreakpointPanel */
    public ExceptionBreakpointPanel (ExceptionBreakpoint b) {
        breakpoint = b;
        initComponents ();
        Iterator it = exceptions.keySet ().iterator ();
        while (it.hasNext ())
            cbExceptionClassName.addItem (it.next ());
        
        String className = b.getExceptionClassName ();
        int i = className.lastIndexOf ('.');
        if (i < 0) {
            tfPackageName.setText ("");
            cbExceptionClassName.setSelectedItem (className);
        } else {
            tfPackageName.setText (className.substring (0, i));
            cbExceptionClassName.setSelectedItem (className.substring (i + 1, className.length ()));
        }
        cbBreakpointType.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_Exception_Breakpoint_Type_Catched"));
        cbBreakpointType.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_Exception_Breakpoint_Type_Uncatched"));
        cbBreakpointType.addItem (java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle").getString("LBL_Exception_Breakpoint_Type_Catched_or_Uncatched"));
        switch (b.getCatchType ()) {
            case ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED:
                cbBreakpointType.setSelectedIndex (0);
                break;
            case ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED:
                cbBreakpointType.setSelectedIndex (1);
                break;
            case ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED_UNCATCHED:
                cbBreakpointType.setSelectedIndex (2);
                break;
        }
        tfCondition.setText (b.getCondition ());
        
        actionsPanel = new ActionsPanel (b);
        pActions.add (actionsPanel, "Center");
        // <RAVE>
        // The help IDs for the AddBreakpointPanel panels have to be different from the
        // values returned by getHelpCtx() because they provide different help
        // in the 'Add Breakpoint' dialog and when invoked in the 'Breakpoints' view
        putClientProperty("HelpID_AddBreakpointPanel", "debug.add.breakpoint.java.exception"); // NOI18N
        // </RAVE>
    }
    
    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerBreakpointExceptionJPDA"); // NOI18N
    }
    // </RAVE>
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbBreakpointType = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        tfCondition = new javax.swing.JTextField();
        cbExceptionClassName = new javax.swing.JComboBox();
        tfPackageName = new javax.swing.JTextField();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        pSettings.setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Exception_Breakpoint_BorderTitle"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("L_Exception_Breakpoint_filter_hint")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Exception_Breakpoint_filter_hint")); // NOI18N

        jLabel2.setLabelFor(tfPackageName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, bundle.getString("L_Exception_Breakpoint_Package_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel2, gridBagConstraints);
        jLabel2.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Exception_Breakpoint_Package_Name")); // NOI18N

        jLabel3.setLabelFor(cbExceptionClassName);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, bundle.getString("L_Exception_Breakpoint_Class_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Exception_Breakpoint_Class_Name")); // NOI18N

        jLabel4.setLabelFor(cbBreakpointType);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, bundle.getString("L_Exception_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel4, gridBagConstraints);
        jLabel4.getAccessibleContext().setAccessibleDescription(bundle.getString("ASCD_L_Exception_Breakpoint_Type")); // NOI18N

        cbBreakpointType.setToolTipText(bundle.getString("TTT_CB_Exception_Breakpoint_Type")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(cbBreakpointType, gridBagConstraints);
        cbBreakpointType.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CB_Exception_Breakpoint_Type")); // NOI18N

        jLabel5.setLabelFor(tfCondition);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, bundle.getString("L_Exception_Breakpoint_Condition")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Exception_Breakpoint_Condition")); // NOI18N

        tfCondition.setToolTipText(bundle.getString("TTT_TF_Exception_Breakpoint_Condition")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfCondition, gridBagConstraints);
        tfCondition.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Exception_Breakpoint_Condition")); // NOI18N

        cbExceptionClassName.setEditable(true);
        cbExceptionClassName.setToolTipText(bundle.getString("TTT_CB_Exception_Breakpoint_Class_Name")); // NOI18N
        cbExceptionClassName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbExceptionClassNameActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(cbExceptionClassName, gridBagConstraints);
        cbExceptionClassName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_CB_Exception_Breakpoint_Class_Name")); // NOI18N

        tfPackageName.setToolTipText(bundle.getString("TTT_CB_Exception_Breakpoint_Package_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfPackageName, gridBagConstraints);
        tfPackageName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Exception_Breakpoint_Package_Name")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pSettings, gridBagConstraints);

        pActions.setLayout(new java.awt.BorderLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(pActions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void cbExceptionClassNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbExceptionClassNameActionPerformed
        // TODO add your handling code here:
        String pkg = (String) exceptions.get (cbExceptionClassName.getSelectedItem ());
        if (pkg != null)
            tfPackageName.setText (pkg);
    }//GEN-LAST:event_cbExceptionClassNameActionPerformed

    
    // Controller implementation ...............................................
    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok () {
        if (! isFilled()) {
            JOptionPane.showMessageDialog(this,
                java.util.ResourceBundle.getBundle("org/netbeans/modules/debugger/jpda/ui/breakpoints/Bundle")
                    .getString("MSG_No_Exception_Class_Name_Spec"));
            return false;
        }
        actionsPanel.ok ();
        String className = ((String) tfPackageName.getText ()).trim ();
        if (className.length () > 0)
            className += '.';
        className += ((String) cbExceptionClassName.getSelectedItem ()).trim ();
        breakpoint.setExceptionClassName (className);
        
        switch (cbBreakpointType.getSelectedIndex ()) {
            case 0:
                breakpoint.setCatchType (ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED);
                break;
            case 1:
                breakpoint.setCatchType (ExceptionBreakpoint.TYPE_EXCEPTION_UNCATCHED);
                break;
            case 2:
                breakpoint.setCatchType (ExceptionBreakpoint.TYPE_EXCEPTION_CATCHED_UNCATCHED);
                break;
        }
        breakpoint.setCondition (tfCondition.getText ());
        
        if (createBreakpoint) 
            DebuggerManager.getDebuggerManager ().addBreakpoint (breakpoint);
        return true;
    }
    
    /**
     * Called when "Cancel" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean cancel () {
        return true;
    }
    
    /**
     * Return <code>true</code> whether value of this customizer 
     * is valid (and OK button can be enabled).
     *
     * @return <code>true</code> whether value of this customizer 
     * is valid
     */
    public boolean isValid () {
        return true;
    }
    
    boolean isFilled () {
        if (((String) cbExceptionClassName.getSelectedItem ())
            .trim ().length() > 0)
            return true;
        return false;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cbBreakpointType;
    private javax.swing.JComboBox cbExceptionClassName;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JTextField tfCondition;
    private javax.swing.JTextField tfPackageName;
    // End of variables declaration//GEN-END:variables
    
}
