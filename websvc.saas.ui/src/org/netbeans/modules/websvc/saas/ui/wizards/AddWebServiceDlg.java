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

package org.netbeans.modules.websvc.saas.ui.wizards;


import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.*;
import java.net.MalformedURLException;

import javax.swing.filechooser.FileFilter;
import javax.swing.*;
import org.netbeans.modules.websvc.saas.model.SaasGroup;
import org.netbeans.modules.websvc.saas.model.SaasServicesModel;
import org.netbeans.modules.websvc.saas.util.WsdlUtil;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Enables searching for Web Services, via an URL, on the local file system
 * or in some uddiRegistry (UDDI)
 * @author Winston Prakash, cao
 */
public class AddWebServiceDlg extends JPanel  implements ActionListener {
    
    public static final String DEFAULT_PACKAGE_HOLDER = NbBundle.getMessage(AddWebServiceDlg.class, "MSG_ClickToOverride"); // NOI18N

    private DialogDescriptor dlg = null;
    private String addString =  NbBundle.getMessage(AddWebServiceDlg.class, "Add");
    private String cancelString =  NbBundle.getMessage(AddWebServiceDlg.class, "CANCEL");
    
    private Dialog dialog;
    
    private static String previousDirectory = null;
    private static JFileChooser wsdlFileChooser;
    
    private  final FileFilter WSDL_FILE_FILTER = new ServiceFileFilter();
 
    private JButton cancelButton = new JButton();
    private JButton addButton = new JButton();
    
    private SaasGroup group;
    private final boolean jaxRPCAvailable;

    private static final String[] KEYWORDS = 
    {
      "abstract", "continue", "for",        "new",       "switch",  // NOI18N
      "assert",   "default",  "if",         "package",   "synchronized", // NOI18N
      "boolean",  "do",       "goto",       "private",   "this", // NOI18N
      "break",    "double",   "implements", "protected", "throw", // NOI18N
      "byte",     "else",     "import",     "public",    "throws", // NOI18N
      "case",     "enum",     "instanceof", "return",    "transient", // NOI18N
      "catch",    "extends",  "int",        "short",     "try", // NOI18N
      "char",     "final",    "interface",  "static",    "void", // NOI18N
      "class",    "finally",  "long",       "strictfp",  "volatile", // NOI18N
      "const",    "float",    "native",     "super",     "while", // NOI18N
      
      "true",     "false",    "null" // NOI18N
    };
    
    private static final Set<String> KEYWORD_SET = new HashSet<String>(KEYWORDS.length * 2);
    
    static {
        for (int i = 0; i < KEYWORDS.length; i++) {
            KEYWORD_SET.add(KEYWORDS[i]);
        }
    }
    
    
    public AddWebServiceDlg(SaasGroup group) {
        initComponents();
        myInitComponents();
        this.group = group;
        jaxRPCAvailable = WsdlUtil.isJAXRPCAvailable();
    }
    
    private static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.length() == 0) { // let jaxws pick package name
            return true;
        } else if (!Character.isJavaIdentifierStart(packageName.charAt(0))) {
            return false;
        }else {
            java.util.StringTokenizer pkgIds = new java.util.StringTokenizer(packageName, "."); // NOI18N
            while (pkgIds.hasMoreTokens()) {
                String nextIdStr = pkgIds.nextToken();
                if (KEYWORD_SET.contains(nextIdStr)) {
                    return false;
                }
                
                char[] nextId = nextIdStr.toCharArray();
                if (!Character.isJavaIdentifierStart(nextId[0])) {
                    return false;
                }
                
                for (int i = 1; i < nextId.length; i++) {
                    if (!Character.isJavaIdentifierPart(nextId[i])) {
                        return false;
                    }
                }
            }
            
            boolean lastDot = false;
            for (int i = 0; i < packageName.length(); i++) {
                boolean isDot = packageName.charAt(i) == '.';
                if (isDot && lastDot) {
                    return false;
                }
                lastDot = isDot;
            }
            
            if (packageName.endsWith(".")) { // NOI18N
                return false;
            }
            
            return true;
        }
    }
    
    private void setErrorMessage(String msg) {
        if (msg == null || msg.length() == 0) {
            errorLabel.setVisible(false);
        }else {
            errorLabel.setVisible(true);
            errorLabel.setText(msg);
        }
    }
    
    private void updateAddButtonState(Component changedComponent) {
        String defaultMsg = jaxRPCAvailable ? "" : NbBundle.getMessage(AddWebServiceDlg.class, "WARNING_JAXRPC_UNAVAILABLE");
        
        // Check the package name
        final String packageName = jTxtpackageName.getText().trim();
        boolean defaultPackage = DEFAULT_PACKAGE_HOLDER.equals(packageName) || packageName.length() == 0;
        if (!defaultPackage && !isValidPackageName(packageName)) {
            setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_PACKAGE"));
            addButton.setEnabled(false);
        }else if (jTxtLocalFilename.isEnabled()) {
            String localText = jTxtLocalFilename.getText().trim();
            if (localText.length() == 0) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "EMPTY_FILE"));
                addButton.setEnabled(false);
                return;
            }
            
            File f = new File(localText);
            if (!f.exists()) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_FILE_NOT_FOUND"));
                addButton.setEnabled(false);
                return;
            }else if (!f.isFile()) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_FILE_NOT_FILE"));
                addButton.setEnabled(false);
                return; 
            }else {
                setErrorMessage(defaultMsg);
                addButton.setEnabled(true);
            }
        }else if (jTxServiceURL.isEnabled()) {
            String urlText = jTxServiceURL.getText().trim();
            if (urlText.length() == 0) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "EMPTY_URL"));
                addButton.setEnabled(false);
                return;                
            }

            try {
                URL url = new URL(urlText);
                setErrorMessage(defaultMsg);
                addButton.setEnabled(true);
            }catch (MalformedURLException ex) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_URL"));
                addButton.setEnabled(false);
            }
        }else {
            setErrorMessage(defaultMsg);
            addButton.setEnabled(true);
        }
    }
    
    
    private void myInitComponents() {
        
        wsdlFileChooser = new JFileChooser();
        ServiceFileFilter myFilter = new ServiceFileFilter();
        wsdlFileChooser.setFileFilter(myFilter);
        addButton.setText(NbBundle.getMessage(this.getClass(), "Add"));
        addButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.addButton.ACC_name"));
        addButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.addButton.ACC_desc"));
        addButton.setMnemonic(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.addButton.ACC_mnemonic").charAt(0));
        cancelButton.setText(NbBundle.getMessage(this.getClass(), "CANCEL"));
        cancelButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.cancelButton.ACC_name"));
        cancelButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.cancelButton.ACC_desc"));
        cancelButton.setMnemonic(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.cancelButton.ACC_mnemonic").charAt(0));
        
        jTxtLocalFilename.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtLocalFilename);
            }
            
            public void removeUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtLocalFilename);
            }
            
            public void changedUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtLocalFilename);
            }
        });
        
        
        jTxServiceURL.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateAddButtonState(jTxServiceURL);
            }

            public void removeUpdate(DocumentEvent e) {
                updateAddButtonState(jTxServiceURL);
            }

            public void changedUpdate(DocumentEvent e) {
                updateAddButtonState(jTxServiceURL);
            }
        });
        
        enableControls();
        
        setDefaults();
        
        jTxtpackageName.setText(DEFAULT_PACKAGE_HOLDER);
        jTxtpackageName.setForeground(Color.GRAY);
    }
    
    public void displayDialog(){
        
        dlg = new DialogDescriptor(this, NbBundle.getMessage(AddWebServiceDlg.class, "ADD_WEB_SERVICE"),
                true, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.CANCEL_OPTION,
                DialogDescriptor.DEFAULT_ALIGN, this.getHelpCtx(), this);
        addButton.setEnabled(false);
        dlg.setOptions(new Object[] { addButton, cancelButton });
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setVisible(true);
    }
    
    private void cancelButtonAction(ActionEvent evt) {
        closeDialog();
    }
    
    private void closeDialog() {
        
        dialog.dispose();
        
    }
    
    
    /** XXX once we implement context sensitive help, change the return */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("projrave_ui_elements_server_nav_add_websvcdb");
    }
    
    
    private void setDefaults() {
        jRbnUrl.setSelected(true);
        jRbnFilesystem.setSelected(false);
//        displayInfo("<BR><BR><BR><BR><B>" +NbBundle.getMessage(AddWebServiceDlg.class, "INSTRUCTIONS") + "</B>");
        enableControls();
    }
    
    private void enableControls(){
        if (jRbnUrl.isSelected()) {
            jTxServiceURL.setEnabled(true);
            jTxtLocalFilename.setEnabled(false);
            updateAddButtonState(jTxServiceURL);
        }else if (jRbnFilesystem.isSelected()) {
            jTxtLocalFilename.setEnabled(true);
            jTxServiceURL.setEnabled(false);
            updateAddButtonState(jTxtLocalFilename);
        }
    }
    
    
    private String fixFileURL(String inFileURL) {
        String returnFileURL = inFileURL;
        
        try {
            File f = new File(returnFileURL);
            return f.toURI().toURL().toString();
        }catch (Exception ex) {
            if (returnFileURL.substring(0, 1).equalsIgnoreCase("/")) {
                returnFileURL = "file://" + returnFileURL;
            } else {
                returnFileURL = "file:///" + returnFileURL;
            }
        }
        return returnFileURL;
    }
    
    /**
     * This represents the event on the "Add" button
     */
    private void addButtonAction(ActionEvent evt) {
        if ( (jTxServiceURL.getText() == null ) && (jTxtLocalFilename.getText() == null))
            return;
        
        final String url;
        if (jRbnUrl.isSelected()) {
            url = jTxServiceURL.getText().trim();
        } else {
            url = fixFileURL(jTxtLocalFilename.getText().trim());
        }
        String packageName = jTxtpackageName.getText().trim();
        if (packageName.equals(NbBundle.getMessage(AddWebServiceDlg.class, "MSG_ClickToOverride"))) {
            packageName = ""; //NOI18N
        }

        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
        
        // Run the add W/S asynchronously
        String checking = url.toLowerCase();
        if (checking.endsWith("wsdl")) { //NOI18N
            SaasServicesModel.getInstance().createWsdlService(group, url, packageName);
        } else if (checking.endsWith("wadl")) { //NOI18N
            SaasServicesModel.getInstance().createWadlService(group, url, packageName);
        }
    }    
    
    public void actionPerformed(ActionEvent evt) {
        String actionCommand = evt.getActionCommand();
        if(actionCommand.equalsIgnoreCase(addString)) {
            addButtonAction(evt);
        } else if(actionCommand.equalsIgnoreCase(cancelString)) {
            cancelButtonAction(evt);
        }
        
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLblChooseSource = new javax.swing.JLabel();
        jRbnFilesystem = new javax.swing.JRadioButton();
        jTxtLocalFilename = new javax.swing.JTextField();
        jBtnBrowse = new javax.swing.JButton();
        jRbnUrl = new javax.swing.JRadioButton();
        jTxServiceURL = new javax.swing.JTextField();
        jBtnProxy = new javax.swing.JButton();
        pkgNameLbl = new javax.swing.JLabel();
        jTxtpackageName = new javax.swing.JTextField();
        errorLabel = new javax.swing.JLabel();
        errorLabel.setVisible(false);

        jLblChooseSource.setText(NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSource")); // NOI18N

        buttonGroup1.add(jRbnFilesystem);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnFilesystem, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSourceFilesystem")); // NOI18N
        jRbnFilesystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnFilesystemActionPerformed(evt);
            }
        });

        jTxtLocalFilename.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_Browse")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRbnUrl);
        jRbnUrl.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnUrl, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlUrl")); // NOI18N
        jRbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnUrlActionPerformed(evt);
            }
        });

        jTxServiceURL.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(jBtnProxy, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_ProxySettings")); // NOI18N
        jBtnProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnProxyActionPerformed(evt);
            }
        });

        pkgNameLbl.setLabelFor(jTxtpackageName);
        pkgNameLbl.setText(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "PACKAGE_LABEL")); // NOI18N

        jTxtpackageName.setColumns(20);
        jTxtpackageName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTxtpackageNameMouseClicked(evt);
            }
        });
        jTxtpackageName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtpackageName);
            }

            public void removeUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtpackageName);
            }

            public void changedUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtpackageName);
            }
        });

        errorLabel.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jRbnFilesystem)
                            .add(jRbnUrl)
                            .add(pkgNameLbl))
                        .add(29, 29, 29)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jTxtpackageName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                            .add(jTxServiceURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jTxtLocalFilename, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 503, Short.MAX_VALUE))
                        .add(6, 6, 6)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jBtnProxy, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jBtnBrowse, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)))
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(jLblChooseSource))
                    .add(layout.createSequentialGroup()
                        .add(25, 25, 25)
                        .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 739, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLblChooseSource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jTxtLocalFilename, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jBtnBrowse))
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(jRbnUrl)
                            .add(jTxServiceURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jBtnProxy)))
                    .add(jRbnFilesystem))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pkgNameLbl)
                    .add(jTxtpackageName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(errorLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLblChooseSource.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSource")); // NOI18N
        jRbnFilesystem.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFilelRadioButton.ACC_desc"));
        jTxtLocalFilename.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_name"));
        jTxtLocalFilename.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_desc"));
        jBtnBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileButton.ACC_desc"));
        jRbnUrl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlRadioButton.ACC_desc"));
        jTxServiceURL.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_name"));
        jTxServiceURL.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_desc"));
        jBtnProxy.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.httpProxyButton.ACC_desc"));
        jTxtpackageName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_name"));
        jTxtpackageName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_desc"));
        errorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.errorLabel.ACC_name"));

        getAccessibleContext().setAccessibleName("null");
        getAccessibleContext().setAccessibleDescription("null");
    }// </editor-fold>//GEN-END:initComponents
    
private void jRbnUrlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnUrlActionPerformed
    // TODO add your handling code here:
    enableControls();
    
}//GEN-LAST:event_jRbnUrlActionPerformed

private void jBtnProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnProxyActionPerformed
        OptionsDisplayer.getDefault().open( "General" );//NOI18N
}//GEN-LAST:event_jBtnProxyActionPerformed

private void jBtnBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnBrowseActionPerformed

    jRbnFilesystem.setSelected(false);
    jRbnFilesystem.setSelected(true);
    enableControls();
    
    JFileChooser chooser = new JFileChooser(previousDirectory);
    chooser.setMultiSelectionEnabled(false);
    chooser.setAcceptAllFileFilterUsed(false);
    chooser.addChoosableFileFilter(WSDL_FILE_FILTER);
    chooser.setFileFilter(WSDL_FILE_FILTER);
    
    if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        File wsdlFile = chooser.getSelectedFile();
        jTxtLocalFilename.setText(wsdlFile.getAbsolutePath());
        previousDirectory = wsdlFile.getPath();
    }
}//GEN-LAST:event_jBtnBrowseActionPerformed

private void jRbnFilesystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRbnFilesystemActionPerformed
    
    enableControls();
}//GEN-LAST:event_jRbnFilesystemActionPerformed

private void jTxtpackageNameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTxtpackageNameMouseClicked
    jTxtpackageName.selectAll();
    jTxtpackageName.setForeground(Color.BLACK);
}//GEN-LAST:event_jTxtpackageNameMouseClicked



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton jBtnBrowse;
    private javax.swing.JButton jBtnProxy;
    private javax.swing.JLabel jLblChooseSource;
    private javax.swing.JRadioButton jRbnFilesystem;
    private javax.swing.JRadioButton jRbnUrl;
    private javax.swing.JTextField jTxServiceURL;
    private javax.swing.JTextField jTxtLocalFilename;
    private javax.swing.JTextField jTxtpackageName;
    private javax.swing.JLabel pkgNameLbl;
    // End of variables declaration//GEN-END:variables
    
    
    
    private static class ServiceFileFilter extends  javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            boolean result;
            if(f.isDirectory() || 
               "wsdl".equalsIgnoreCase(FileUtil.getExtension(f.getName())) ||
               "wadl".equalsIgnoreCase(FileUtil.getExtension(f.getName()))
               ) { // NOI18N
                result = true;
            } else {
                result = false;
            }
            return result;
        }
        public String getDescription() {
            return NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlFilterDescription"); // NOI18N
        }
        
    }
}
