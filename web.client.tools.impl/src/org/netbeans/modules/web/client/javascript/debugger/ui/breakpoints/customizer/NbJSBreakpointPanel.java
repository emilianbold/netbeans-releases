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

package org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.customizer;

import java.awt.Dimension;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.impl.JSFactory;
import org.netbeans.modules.web.client.javascript.debugger.ui.NbJSEditorUtil;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSBreakpointManager;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSFileObjectBreakpoint;
import org.netbeans.modules.web.client.javascript.debugger.ui.breakpoints.NbJSURIBreakpoint;
import org.netbeans.spi.debugger.ui.Controller;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Panel for customizing line breakpoints.
 *
 * @author  Joelle Lam
 */
// <RAVE>
// Implement HelpCtx.Provider interface to provide help ids for help system
// public class LineBreakpointPanel extends JPanel implements Controller {
//
public class NbJSBreakpointPanel extends JPanel implements Controller, org.openide.util.HelpCtx.Provider {
// </RAVE>

    
    private static final long serialVersionUID = 1L;

    private NbJSBreakpointConditionsPanel conditionsPanel;
//    private NbJSBreakpointActionsPanel    actionsPanel;
    private NbJSBreakpoint breakpoint = null;
    private String orFileName;
    private final int orLineNum;
    private final String orResolvedLoc;

    private boolean createBreakpoint = false;



    /** Creates new form NbJSBreakpointPanel */
    public NbJSBreakpointPanel() {
        orFileName = "";
        orLineNum = 0;
        createBreakpoint = true;
        orResolvedLoc ="";
        initBreakpointPanel( orFileName, orResolvedLoc, orLineNum );
    }

 

    /** Creates new form NbJSBreakpointPanel */
    public NbJSBreakpointPanel (final NbJSBreakpoint b) {
        assert b != null;
        breakpoint = b;
        orFileName = null;
        if( b instanceof NbJSFileObjectBreakpoint ){
            orFileName = FileUtil.getFileDisplayName(b.getFileObject());
        } else if (b instanceof NbJSURIBreakpoint ){
            NbJSURIBreakpoint uriBp = (NbJSURIBreakpoint)b;
            orFileName = uriBp.getLocation().getURI().toString();
        } 
        
        orLineNum = b.getLineNumber();
        orResolvedLoc = ( b.getResolvedLocation() != null ) ? b.getResolvedLocation() : "";
        initBreakpointPanel( orFileName, orResolvedLoc, orLineNum );
    }
    
    private void initBreakpointPanel( String fileName, String resolvedLocation, int lineNum ){

        initComponents ();
        assert orFileName != null;
        
        tfFileName.setText(orFileName);
        tfFileName.setPreferredSize(new Dimension(tfFileName.getFontMetrics(tfFileName.getFont()).charWidth('W'),
            tfFileName.getPreferredSize().height));
        tfURL.setText(resolvedLocation);
        tfLineNumber.setText(Integer.toString(orLineNum));
        if ( breakpoint != null ){
            conditionsPanel = new NbJSBreakpointConditionsPanel(breakpoint);
        } else {
            conditionsPanel = new NbJSBreakpointConditionsPanel();
        }
        cPanel.add(conditionsPanel, "Center");
    }

    private static int getMaxLineNumber(final FileObject fileObject) {
        final DataObject dataObject;
        if (fileObject == null) return 0;
        try {
            dataObject = DataObject.find (fileObject);
        } catch (final DataObjectNotFoundException ex) {
            return 0;
        }
        final EditorCookie ec = (EditorCookie) dataObject.getCookie(EditorCookie.class);
        if (ec == null) return 0;
        ec.prepareDocument().waitFinished();
        final Document d = ec.getDocument();
        if (!(d instanceof StyledDocument)) return 0;
        final StyledDocument sd = (StyledDocument) d;
        return NbDocument.findLineNumber(sd, sd.getLength()) + 1;
    }



    private void setupConditionPane() {
        
        if ( breakpoint == null ){
            return;
        }
        
        FileObject fo = breakpoint.getFileObject();
        if( fo == null ) {
            return;
        }
        conditionsPanel.setupConditionPaneContext(fo.getPath(), breakpoint.getLineNumber());
            
    }

    // <RAVE>
    // Implement getHelpCtx() with the correct helpID
    //
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("NetbeansDebuggerBreakpointLine"); // NOI18N
    }
    // </RAVE>

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pSettings = new javax.swing.JPanel();
        fileLabel = new javax.swing.JLabel();
        tfFileName = new javax.swing.JTextField();
        lineNumLabel = new javax.swing.JLabel();
        tfLineNumber = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        tfURL = new javax.swing.JTextField();
        cPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(300, 10));
        setName("NbJSPBreakpointPanel"); // NOI18N
        setLayout(new java.awt.GridBagLayout());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/web/client/javascript/debugger/ui/breakpoints/customizer/Bundle"); // NOI18N
        pSettings.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("L_Line_Breakpoint_BorderTitle"))); // NOI18N

        fileLabel.setLabelFor(tfFileName);
        org.openide.awt.Mnemonics.setLocalizedText(fileLabel, bundle.getString("L_Line_Breakpoint_File_Name")); // NOI18N
        fileLabel.setToolTipText("");

        tfFileName.setColumns(60);
        tfFileName.setToolTipText(bundle.getString("TTT_TF_Line_Breakpoint_File_Name")); // NOI18N

        lineNumLabel.setLabelFor(tfLineNumber);
        org.openide.awt.Mnemonics.setLocalizedText(lineNumLabel, bundle.getString("L_Line_Breakpoint_Line_Number")); // NOI18N
        lineNumLabel.setToolTipText("");

        tfLineNumber.setToolTipText(bundle.getString("TTT_TF_Line_Breakpoint_Line_Number")); // NOI18N

        urlLabel.setLabelFor(tfLineNumber);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, bundle.getString("L_Line_Breakpoint_URL")); // NOI18N
        urlLabel.setToolTipText("");

        tfURL.setColumns(60);
        tfURL.setEditable(false);
        tfURL.setToolTipText(bundle.getString("TTT_TF_Resolved_URL")); // NOI18N

        org.jdesktop.layout.GroupLayout pSettingsLayout = new org.jdesktop.layout.GroupLayout(pSettings);
        pSettings.setLayout(pSettingsLayout);
        pSettingsLayout.setHorizontalGroup(
            pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pSettingsLayout.createSequentialGroup()
                .add(pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pSettingsLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(fileLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(tfFileName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))
                    .add(pSettingsLayout.createSequentialGroup()
                        .add(pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(pSettingsLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(urlLabel))
                            .add(pSettingsLayout.createSequentialGroup()
                                .add(9, 9, 9)
                                .add(lineNumLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 87, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(tfLineNumber, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                            .add(tfURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE))))
                .addContainerGap())
        );

        pSettingsLayout.linkSize(new java.awt.Component[] {fileLabel, lineNumLabel, urlLabel}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        pSettingsLayout.setVerticalGroup(
            pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(pSettingsLayout.createSequentialGroup()
                .add(pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(fileLabel)
                    .add(tfFileName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(8, 8, 8)
                .add(pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(urlLabel)
                    .add(tfURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(pSettingsLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lineNumLabel)
                    .add(tfLineNumber, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        fileLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "L_Line_Breakpoint_File_Name")); // NOI18N
        fileLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_L_Line_Breakpoint_File_Name")); // NOI18N
        tfFileName.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "A11y_TF_ File")); // NOI18N
        tfFileName.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_TF_Line_Breakpoint_File_Name_Description")); // NOI18N
        lineNumLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "ACSD_L_Line_Breakpoint_Line_Number")); // NOI18N
        lineNumLabel.getAccessibleContext().setAccessibleDescription("null");
        tfLineNumber.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "A11y_TF_ LineNumber")); // NOI18N
        tfLineNumber.getAccessibleContext().setAccessibleDescription(bundle.getString("A11Y_TF_LineNumber_Desc")); // NOI18N
        tfURL.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "A11y_TF_ ResolvedURL")); // NOI18N
        tfURL.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "A11y_TF_ ResolvedURL_TF_Desc")); // NOI18N

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(pSettings, gridBagConstraints);
        pSettings.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "L_Line_Breakpoint_BorderTitle")); // NOI18N

        cPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(cPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NbJSBreakpointPanel.class, "ACSN_LineBreakpoint")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents


    // Controller implementation ...............................................

    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok () {
        String msg = valiadateMsg();
        if (msg == null) {
            msg = conditionsPanel.valiadateMsg();
        }
        if (msg != null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
            return false;
        }
        
        final String newFileName = tfFileName.getText().trim();
        final int newLineNum = Integer.parseInt(tfLineNumber.getText().trim()) - 1;
        
        
        boolean updated = false;
        
        if (createBreakpoint || (orLineNum - 1) != newLineNum || !newFileName.equals(orFileName)) {
            final Line line = NbJSEditorUtil.getLine(newFileName, newLineNum);
            
            if (createBreakpoint) {
                breakpoint = NbJSBreakpointManager.addBreakpoint(line);
                if( conditionsPanel != null ){
                    conditionsPanel.setBreakpoint(breakpoint);
                }
            }
            
            if ( breakpoint instanceof NbJSFileObjectBreakpoint ){
                breakpoint.setLine(line);
            } else if( breakpoint instanceof NbJSURIBreakpoint ){
                NbJSBreakpointManager.removeBreakpoint(breakpoint);
                NbJSBreakpointManager.addURIBreakpoint(newFileName, newLineNum);
            }
        }
        
        if(updated) {
            breakpoint.notifyUpdated(this);
        }
        
//      actionsPanel.ok();
        conditionsPanel.ok();
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
        final String sourceName = tfFileName.getText().trim();
        int lineNum;
        try {
            lineNum = Integer.parseInt(tfLineNumber.getText().trim()) - 1;
        } catch (final NumberFormatException e) {
            return NbBundle.getMessage(NbJSBreakpointPanel.class, "MSG_No_Line_Number_Spec");
        }
        if (lineNum < 0) {
            return NbBundle.getMessage(NbJSBreakpointPanel.class, "MSG_NonPositive_Line_Number_Spec");
        }
        
        File file = new File(sourceName);
        if(file == null || !file.exists() || file.isDirectory()) {
            return NbBundle.getMessage(NbJSBreakpointPanel.class, "MSG_Invalid_File");
        }
        
        if (breakpoint != null ){
            if ( breakpoint instanceof NbJSURIBreakpoint)
                return validateURIMsg( sourceName, lineNum);
            else if (breakpoint instanceof NbJSBreakpoint )
                return validateFileNameMsg(sourceName, lineNum);
        } else if ( breakpoint == null ){
            return validateFileNameMsg(sourceName, lineNum);
        }
        return null;
    }
    
    private String validateURIMsg(String uri, int lineNum) {
        FileObject fileObject = (breakpoint != null ) ? breakpoint.getFileObject() : null;
        if ( fileObject == null ){
            DebuggerEngine engine = DebuggerManager.getDebuggerManager().getCurrentEngine();
            if ( engine != null ){
                NbJSDebugger debugger = (NbJSDebugger)engine.lookupFirst(null, NbJSDebugger.class);
                if(debugger != null) {
                    fileObject = debugger.getFileObjectForSource(JSFactory.createJSSource(uri));
                }
            }
        }
        
        if (fileObject != null ){
            int maxLine = getMaxLineNumber(fileObject);
            if (maxLine == 0) { // Not found
                maxLine = Integer.MAX_VALUE; // Not to bother the user when we did not find it
            }
            if (lineNum > maxLine) {
                return NbBundle.getMessage(NbJSBreakpointPanel.class, "MSG_TooBig_Line_Number_Spec",
                        Integer.toString(lineNum), Integer.toString(maxLine + 1));
            }
        }
        return null;
    }

    private String validateFileNameMsg(String fileName, int lineNum) {
        if (fileName == null || fileName.equals("")){
            return NbBundle.getMessage(NbJSBreakpointPanel.class, "MSG_NotAFILE");
        }

        FileObject fileObject = (breakpoint != null ) ? breakpoint.getFileObject() : null;
        
        if (fileObject == null ){
            try {
                fileObject = URLMapper.findFileObject(new URL("file:" + fileName));
            } catch (MalformedURLException e) {
                Exceptions.printStackTrace(e);
            } catch ( IllegalArgumentException iae){
                return NbBundle.getMessage(NbJSBreakpointPanel.class, "MSG_NotAFILE");
            }
        }
        
        if ( fileObject != null ) {
            int maxLine = getMaxLineNumber(fileObject);
            if (maxLine == 0) { // Not found
                maxLine = Integer.MAX_VALUE; // Not to bother the user when we did not find it
            }
            if (lineNum > maxLine) {
                return NbBundle.getMessage(NbJSBreakpointPanel.class, "MSG_TooBig_Line_Number_Spec",
                        Integer.toString(lineNum), Integer.toString(maxLine + 1));
            }
        }
        return null;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel cPanel;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JLabel lineNumLabel;
    private javax.swing.JPanel pSettings;
    private javax.swing.JTextField tfFileName;
    private javax.swing.JTextField tfLineNumber;
    private javax.swing.JTextField tfURL;
    private javax.swing.JLabel urlLabel;
    // End of variables declaration//GEN-END:variables

}
