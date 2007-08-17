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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx.Provider;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.openide.util.HelpCtx;
import org.openide.util.Utilities;

/**
 * Panel for customizing line breakpoints.
 * This panel is a part of "New Breakpoint" dialog.
 *
 * @author Nik Molchanov (copied and modified from JDPA debugger).
 */

// Implement HelpCtx.Provider interface to provide help ids for help system
// public class LineBreakpointPanel extends JPanel implements Controller {
//
public class LineBreakpointPanel extends JPanel implements Controller, HelpCtx.Provider {
    
    private ActionsPanel                actionsPanel; 
    private LineBreakpoint              breakpoint;
    private boolean                     createBreakpoint = false;
    
    /** 
     * Creates new form LineBreakpointPanel
     */
    public LineBreakpointPanel() {
        this(createBreakpoint());
        createBreakpoint = true;
    }
    
    /** 
     * Creates new form LineBreakpointPanel
     */
    public LineBreakpointPanel(LineBreakpoint b) {
	String mime = null;
	String url = b.getURL();
	
        breakpoint = b;
        initComponents();

        try {
	    FileObject fo = URLMapper.findFileObject(new URL(url));
	    if (fo != null) {
		mime = fo.getMIMEType();
	    }
	} catch (MalformedURLException mue) {
	}
	
	/*
	 * If we have a valid url and mime type then seed the dialog text field
	 * with its path. If not (this could happen if we were adding a Line breakpoint
	 * while a Java file had focus in the editor) then make the text field empty
	 * but writable.
	 */
	if (url.length() > 0 && mime != null &&
		    (mime.equals("text/x-c++") || mime.equals("text/x-c") || mime.equals("text/x-fortran"))) { // NOI18N
	    try {
		URI uri = new URI(url);
		String path = uri.getPath();
		if (Utilities.isWindows() && path.charAt(0) == '/') {
		    path = path.substring(1);
		}
		tfFileName.setText(path);
	    } catch (Exception e) {
		tfFileName.setText(url);
	    }
	} else {
	    tfFileName.setEditable(true);
	}
	tfLineNumber.setText(Integer.toString(b.getLineNumber()));
        tfCondition.setText(b.getCondition());
        setupConditionPane();
        
        actionsPanel = new ActionsPanel(b);
        pActions.add(actionsPanel, "Center");  // NOI18N
    }
    
    private static LineBreakpoint createBreakpoint() {
	String url = EditorContextBridge.getMostRecentURL();
	int lnum = EditorContextBridge.getMostRecentLineNumber();
	
	LineBreakpoint lb = LineBreakpoint.create(url, lnum);
        lb.setPrintText(NbBundle.getBundle(LineBreakpointPanel.class).getString("CTL_Line_Breakpoint_Print_Text")); // NOI18N
        return lb;
    }
    
    private static int findNumLines(String url) {
        FileObject file;
        try {
            file = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
            return 0;
        }
        if (file == null) {
	    return 0;
	}
        DataObject dataObject;
        try {
            dataObject = DataObject.find(file);
        } catch (DataObjectNotFoundException ex) {
            return 0;
        }
        EditorCookie ec = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (ec == null) {
	    return 0;
	}
        ec.prepareDocument().waitFinished();
        Document d = ec.getDocument();
        if (!(d instanceof StyledDocument)) {
	    return 0;
	}
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
        return new HelpCtx("NetbeansDebuggerBreakpointLineGDB"); // NOI18N
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
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        tfFileName = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        tfLineNumber = new javax.swing.JTextField();
        spCondition = new javax.swing.JScrollPane();
        tfCondition = new javax.swing.JEditorPane();
        pActions = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        pSettings.setLayout(new java.awt.GridBagLayout());

        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("L_Line_Breakpoint_BorderTitle")));
        pSettings.setMinimumSize(new java.awt.Dimension(249, 105));
        pSettings.setPreferredSize(new java.awt.Dimension(144, 105));
        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Line_Breakpoint_File_Name").charAt(0));
        jLabel3.setLabelFor(tfFileName);
        jLabel3.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("L_Line_Breakpoint_File_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_L_Line_Breakpoint_File_Name"));

        jLabel5.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Line_Breakpoint_Condition").charAt(0));
        jLabel5.setLabelFor(tfCondition);
        jLabel5.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("L_Line_Breakpoint_Condition"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_L_Line_Breakpoint_Condition"));

        tfFileName.setEditable(false);
        tfFileName.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("TTT_TF_Line_Breakpoint_File_Name"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfFileName, gridBagConstraints);
        tfFileName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_TF_Line_Breakpoint_File_Name"));
        tfFileName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_TF_Line_Breakpoint_File_Name"));

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Line_Breakpoint_Line_Number").charAt(0));
        jLabel1.setLabelFor(tfLineNumber);
        jLabel1.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("L_Line_Breakpoint_Line_Number"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_L_Line_Breakpoint_Line_Number"));

        tfLineNumber.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("TTT_TF_Line_Breakpoint_Line_Number"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfLineNumber, gridBagConstraints);
        tfLineNumber.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_TF_Line_Breakpoint_Line_Number"));
        tfLineNumber.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_TF_Line_Breakpoint_Line_Number"));

        spCondition.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spCondition.setToolTipText(org.openide.util.NbBundle.getMessage(LineBreakpointPanel.class, "ACSD_TF_Line_Breakpoint_Condition"));
        spCondition.setMinimumSize(new java.awt.Dimension(11, 19));
        tfCondition.setEditable(false);
        tfCondition.setToolTipText(org.openide.util.NbBundle.getMessage(LineBreakpointPanel.class, "HINT_UnimplementedCondition"));
        tfCondition.setMinimumSize(new java.awt.Dimension(116, 17));
        tfCondition.setPreferredSize(new java.awt.Dimension(11, 19));
        spCondition.setViewportView(tfCondition);
        tfCondition.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_TF_Line_Breakpoint_Condition"));
        tfCondition.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("ACSD_TF_Line_Breakpoint_Condition"));

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
    public boolean ok() {
        String msg = validateBreakpoint();
        if (msg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return false;
        }
        actionsPanel.ok();
        breakpoint.setLineNumber(Integer.parseInt(tfLineNumber.getText().trim()));
        breakpoint.setCondition(tfCondition.getText());
        // Check if this breakpoint is already set
        DebuggerManager dm = DebuggerManager.getDebuggerManager ();
        Breakpoint[] bs = dm.getBreakpoints ();
        int i, k = bs.length;
        for (i = 0; i < k; i++) {
            if (bs[i] instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) bs[i];
                // Compare line numbers
                if (breakpoint.getLineNumber() != lb.getLineNumber()) {
                    continue;
                }
                // Compare file names
                String url = breakpoint.getURL();
                if (!url.equals(lb.getURL())) {
                    continue;
                }
                // Compare conditions
                String condition = breakpoint.getCondition();
                if (condition != null) {
                    if (!condition.equals(lb.getCondition())) {
                        continue;
                    }
                } else {
                    if (lb.getCondition() != null) {
                        continue;
                    }
                }
                // Check if this breakpoint is enabled
                if (!lb.isEnabled()) {
                    bs[i].enable();
                }
                return true;
            }
        }
        // Create a new breakpoint
        if (createBreakpoint) {
	    DebuggerManager.getDebuggerManager().addBreakpoint(breakpoint);
	}
        return true;
    }
    
    /**
     * Called when "Cancel" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean cancel() {
        return true;
    }
    
    /**
     * Return <code>true</code> whether value of this customizer 
     * is valid (and OK button can be enabled).
     *
     * @return <code>true</code> whether value of this customizer 
     * is valid
     */
    public boolean isValid() {
        return true;
    }
    
    private String validateBreakpoint() {
	String path = tfFileName.getText();
	File file;
	
	// First, validate the path
	if (path == null || path.length() == 0) {
	    return NbBundle.getMessage(LineBreakpointPanel.class, "MSG_No_File_Name_Spec"); // NOI18N
	}
	if (path.charAt(0) == '/' || path.charAt(0) == '\'') { // Can't rely on direction of slash on Windows
	    file = new File(path);
	    if (!file.exists()) {
		return NbBundle.getMessage(LineBreakpointPanel.class, "MSG_File_Name_Does_Not_Exist"); // NOI18N
	    }
	    // need a way to find the active GdbDebugger to do the following validation...
//	} else {
//	    if (debugger != null) {
//		String rundir = debugger.getRunDirectory();
//		file = new File(rundir, path);
//		if (!file.exists()) {
//		    return NbBundle.getMessage(LineBreakpointPanel.class, "MSG_File_Name_Does_Not_Exist"); // NOI18N
//		}
//	    }
	}
	
	// Now validate the line number
        try {
            int line = Integer.parseInt(tfLineNumber.getText().trim());
            if (line <= 0) {
                return NbBundle.getMessage(LineBreakpointPanel.class, "MSG_NonPositive_Line_Number_Spec"); // NOI18N
            }
            int maxLine = findNumLines(breakpoint.getURL());
            if (maxLine == 0) { // Not found
                maxLine = Integer.MAX_VALUE; // Not to bother the user when we did not find it
            }
            if (line > maxLine) {
                return NbBundle.getMessage(LineBreakpointPanel.class, "MSG_TooBig_Line_Number_Spec", // NOI18N
                        Integer.toString(line), Integer.toString(maxLine));
            }
        } catch (NumberFormatException e) {
            return NbBundle.getMessage(LineBreakpointPanel.class, "MSG_No_Line_Number_Spec"); // NOI18N
        }
	
	// No validation for the (currently unsupported) condition
        return null;
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel pActions;
    private javax.swing.JPanel pSettings;
    private javax.swing.JScrollPane spCondition;
    private javax.swing.JEditorPane tfCondition;
    private javax.swing.JTextField tfFileName;
    private javax.swing.JTextField tfLineNumber;
    // End of variables declaration//GEN-END:variables
    
}
