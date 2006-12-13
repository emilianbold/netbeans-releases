/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerManager;

//import org.netbeans.api.debugger.jpda.LineBreakpoint;
//import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
//import org.netbeans.modules.debugger.jpda.ui.FilteredKeymap;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.Controller;

import java.net.URI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 * Panel for customizing function breakpoints.
 *
 * @author  Maros Sandor (original code)
 */

// Implement HelpCtx.Provider interface to provide help ids for help system
// public class FunctionBreakpointPanel extends JPanel implements Controller {
//
public class FunctionBreakpointPanel extends JPanel implements Controller, org.openide.util.HelpCtx.Provider {
    
    private ActionsPanel                actionsPanel; 
    private FunctionBreakpoint          breakpoint;
    private boolean                     createBreakpoint = false;
    
    
    private static FunctionBreakpoint createBreakpoint () {
        FunctionBreakpoint mb = FunctionBreakpoint.create (
            // EditorContextBridge.getCurrentFunction ()
            "main" // DEBUG
        );
        mb.setPrintText (
            NbBundle.getBundle (FunctionBreakpointPanel.class).getString 
                ("CTL_Function_Breakpoint_Print_Text")
        );
        return mb;
    }
    
    
    /** 
     * Creates new form FunctionBreakpointPanel
     */
    public FunctionBreakpointPanel () {
        this (createBreakpoint ());
        createBreakpoint = true;
    }
    
    /** 
     * Creates new form FunctionBreakpointPanel
     */
    public FunctionBreakpointPanel (FunctionBreakpoint b) {
        breakpoint = b;
        initComponents ();

        String url = b.getURL();
        // Textfield File Name is not used
        //try {
        //    URI uri = new URI(url);
        //    tfFileName.setText(uri.getPath());
        //} catch (Exception e) {
        //    tfFileName.setText(url);
        //}
        //tfLineNumber.setText(Integer.toString(b.getLineNumber()));
        tfCondition.setText (b.getCondition ());
        setupConditionPane();
        
        actionsPanel = new ActionsPanel (b);
        pActions.add (actionsPanel, "Center");
    }
    
    private static int findNumLines(String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL(url));
        } catch (MalformedURLException e) {
            return 0;
        }
        if (file == null) return 0;
        DataObject dataObject;
        try {
            dataObject = DataObject.find (file);
        } catch (DataObjectNotFoundException ex) {
            return 0;
        }
        EditorCookie ec = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (ec == null) return 0;
        ec.prepareDocument().waitFinished();
        Document d = ec.getDocument();
        if (!(d instanceof StyledDocument)) return 0;
        StyledDocument sd = (StyledDocument) d;
        return NbDocument.findLineNumber(sd, sd.getLength());
    }
    
    private void setupConditionPane() {
        /* Not implemented yet
        tfCondition.setKeymap(new FilteredKeymap(tfCondition.getKeymap()));
        String url = breakpoint.getURL();
        DataObject dobj = null;
        FileObject file;
        try {
            file = URLMapper.findFileObject (new URL (url));
            if (file != null) {
                try {
                    dobj = DataObject.find (file);
                } catch (DataObjectNotFoundException ex) {
                    // null dobj
                }
            }
        } catch (MalformedURLException e) {
            // null dobj
        }
        tfCondition.getDocument().putProperty(javax.swing.text.Document.StreamDescriptionProperty, dobj);
        */
    }
    
    /** 
     * Implement getHelpCtx() with the correct helpID
    */
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerBreakpointLineJPDA"); // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        tfLineNumber = new javax.swing.JTextField();
        spCondition = new javax.swing.JScrollPane();
        tfCondition = new javax.swing.JEditorPane();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        pSettings.setLayout(new java.awt.GridBagLayout());

        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("L_Function_Breakpoint_BorderTitle")));
        jLabel5.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Function_Breakpoint_Condition").charAt(0));
        jLabel5.setLabelFor(tfCondition);
        jLabel5.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("L_Function_Breakpoint_Condition"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_L_Function_Breakpoint_Condition"));

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Function_Breakpoint_Function_Name").charAt(0));
        jLabel1.setLabelFor(tfLineNumber);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("L_Function_Breakpoint_Function_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_L_Function_Breakpoint_Function_Name"));

        tfLineNumber.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("TTT_TF_Function_Breakpoint_Function_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfLineNumber, gridBagConstraints);
        tfLineNumber.getAccessibleContext().setAccessibleName("Line number");
        tfLineNumber.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_TF_Function_Breakpoint_Function_Name"));

        spCondition.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spCondition.setToolTipText(org.openide.util.NbBundle.getMessage(FunctionBreakpointPanel.class, "ACSD_TF_Function_Breakpoint_Condition"));
        tfCondition.setEditable(false);
        tfCondition.setText("Not implemented yet.");
        tfCondition.setToolTipText(org.openide.util.NbBundle.getMessage(FunctionBreakpointPanel.class, "HINT_UnimplementedCondition"));
        tfCondition.setContentType("text/x-java");
        spCondition.setViewportView(tfCondition);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(spCondition, gridBagConstraints);

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

    
    // Controller implementation ...............................................
    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok () {
        String msg = valiadateMsg();
        if (msg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return false;
        }
        actionsPanel.ok ();
        String functionName = tfLineNumber.getText().trim();
        String Condition = tfCondition.getText();
        //breakpoint.setLineNumber(Integer.parseInt(tfLineNumber.getText().trim()));
        breakpoint.setFunctionName(functionName);
        breakpoint.setCondition (Condition);
        // Check if this breakpoint already set
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        Breakpoint[] bs = dm.getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
            if (bs[i] instanceof FunctionBreakpoint) {
                FunctionBreakpoint fb = (FunctionBreakpoint) bs[i];
                if (functionName.equals(fb.getFunctionName())) {
                    // Compare conditions
                    String condition = breakpoint.getCondition();
                    if (condition != null) {
                        if (!condition.equals(fb.getCondition())) {
                            continue;
                        }
                    } else {
                        if (fb.getCondition() != null) {
                            continue;
                        }
                    }
                    // Check if this breakpoint is enabled
                    if (!fb.isEnabled())
                        bs[i].enable();
                    return true;
                }
            }
        }
        // Create a new breakpoint
        if (createBreakpoint)
            dm.addBreakpoint(breakpoint);
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
    
    private String valiadateMsg () {
        String function = tfLineNumber.getText().trim();
        // Empty string is not a valid function name
        if ((function == null) || (function.equals(""))) {
            return NbBundle.getBundle (FunctionBreakpointPanel.class).getString 
                ("MSG_No_Function_Name_Spec"); // NOI18N
        }
        return null;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JScrollPane spCondition;
    private javax.swing.JEditorPane tfCondition;
    private javax.swing.JTextField tfLineNumber;
    // End of variables declaration//GEN-END:variables
    
}
