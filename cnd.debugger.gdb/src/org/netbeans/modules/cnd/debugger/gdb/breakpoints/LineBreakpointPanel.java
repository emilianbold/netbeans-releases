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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import javax.swing.JPanel;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.openide.filesystems.FileUtil;
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
    
    private ActionsPanel        actionsPanel; 
    private LineBreakpoint      breakpoint;
    private boolean             createBreakpoint = false;
    
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
        if (b.getLineNumber() > 0) {
            tfLineNumber.setText(Integer.toString(b.getLineNumber()));
        }
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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Line_Breakpoint_BorderTitle"))); // NOI18N
        pSettings.setMinimumSize(new java.awt.Dimension(249, 105));
        pSettings.setPreferredSize(new java.awt.Dimension(144, 105));
        pSettings.setLayout(new java.awt.GridBagLayout());

        jLabel3.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Line_Breakpoint_File_Name").charAt(0));
        jLabel3.setLabelFor(tfFileName);
        jLabel3.setText(bundle.getString("L_Line_Breakpoint_File_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel3, gridBagConstraints);
        jLabel3.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Line_Breakpoint_File_Name")); // NOI18N

        jLabel5.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Line_Breakpoint_Condition").charAt(0));
        jLabel5.setLabelFor(tfCondition);
        jLabel5.setText(bundle.getString("L_Line_Breakpoint_Condition")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel5, gridBagConstraints);
        jLabel5.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Line_Breakpoint_Condition")); // NOI18N

        tfFileName.setEditable(false);
        tfFileName.setToolTipText(bundle.getString("TTT_TF_Line_Breakpoint_File_Name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfFileName, gridBagConstraints);
        tfFileName.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_TF_Line_Breakpoint_File_Name")); // NOI18N
        tfFileName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Line_Breakpoint_File_Name")); // NOI18N

        jLabel1.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/gdb/breakpoints/Bundle").getString("MN_L_Line_Breakpoint_Line_Number").charAt(0));
        jLabel1.setLabelFor(tfLineNumber);
        jLabel1.setText(bundle.getString("L_Line_Breakpoint_Line_Number")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(jLabel1, gridBagConstraints);
        jLabel1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Line_Breakpoint_Line_Number")); // NOI18N

        tfLineNumber.setToolTipText(bundle.getString("TTT_TF_Line_Breakpoint_Line_Number")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 3, 3);
        pSettings.add(tfLineNumber, gridBagConstraints);
        tfLineNumber.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_TF_Line_Breakpoint_Line_Number")); // NOI18N
        tfLineNumber.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Line_Breakpoint_Line_Number")); // NOI18N

        spCondition.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        spCondition.setToolTipText(org.openide.util.NbBundle.getMessage(LineBreakpointPanel.class, "ACSD_TF_Line_Breakpoint_Condition")); // NOI18N
        spCondition.setMinimumSize(new java.awt.Dimension(11, 19));

        tfCondition.setBackground(new java.awt.Color(238, 238, 238));
        tfCondition.setEditable(false);
        tfCondition.setToolTipText(org.openide.util.NbBundle.getMessage(LineBreakpointPanel.class, "HINT_UnimplementedCondition")); // NOI18N
        tfCondition.setEnabled(false);
        tfCondition.setMinimumSize(new java.awt.Dimension(116, 17));
        tfCondition.setPreferredSize(new java.awt.Dimension(11, 19));
        spCondition.setViewportView(tfCondition);
        tfCondition.getAccessibleContext().setAccessibleName(bundle.getString("ACSD_TF_Line_Breakpoint_Condition")); // NOI18N
        tfCondition.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Line_Breakpoint_Condition")); // NOI18N

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
        String lnum = tfLineNumber.getText().trim();
        if (lnum.length() > 0) {
            breakpoint.setLineNumber(Integer.parseInt(lnum));
        }
        breakpoint.setCondition(tfCondition.getText());
        // Check if this breakpoint is already set
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        Breakpoint[] bs = dm.getBreakpoints();
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
    @Override
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
        file = new File(path);
        if (!file.isAbsolute()) {
            file = new File(getRunDirectory(), path);
        }
        if (!file.exists()) {
            return NbBundle.getMessage(LineBreakpointPanel.class, "MSG_File_Name_Does_Not_Exist"); // NOI18N
        }
        breakpoint.setURL(file.getAbsolutePath());
	
	// No validation for the (currently unsupported) condition
        return null;
    }
    
    private String getRunDirectory() {
        try {
            return FileUtil.toFile(OpenProjects.getDefault().getMainProject().getProjectDirectory()).getAbsolutePath();
        } catch (Exception ex) {
            return "."; // NOI18N
        }
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
