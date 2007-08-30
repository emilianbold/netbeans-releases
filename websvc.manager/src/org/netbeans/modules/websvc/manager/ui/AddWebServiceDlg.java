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

package org.netbeans.modules.websvc.manager.ui;


import java.awt.Component;
import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.websvc.manager.api.WebServiceManager;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.filechooser.FileFilter;
import javax.swing.*;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
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
    
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    
    private DialogDescriptor dlg = null;
    private String addString =  NbBundle.getMessage(AddWebServiceDlg.class, "Add");
    private String cancelString =  NbBundle.getMessage(AddWebServiceDlg.class, "CANCEL");
    
    private Dialog dialog;
    
    private static String previousDirectory = null;
    private static JFileChooser wsdlFileChooser;
    
    private  final FileFilter WSDL_FILE_FILTER = new WsdlFileFilter();
    
    
    private int returnStatus = RET_CANCEL;
 
    private JButton cancelButton = new JButton();
    private JButton addButton = new JButton();
    private JPopupMenu resultsPopup = new JPopupMenu();
    
    private String groupId;

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
    
    
    public AddWebServiceDlg() {
        this(WebServiceListModel.DEFAULT_GROUP);
    }
    
    
    public AddWebServiceDlg(String groupId) {
        initComponents();
        myInitComponents();
        this.groupId = groupId;
    }


    
    private static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.length() == 0 || !Character.isJavaIdentifierStart(packageName.charAt(0))) {
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
        // Check the package name
        final String packageName = jTxtpackageName.getText().trim();
        if (changedComponent == jTxtpackageName && !isValidPackageName(packageName)) {
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
                setErrorMessage("");
                addButton.setEnabled(true);
            }
        }else if (jTxtWsdlURL.isEnabled()) {
            String urlText = jTxtWsdlURL.getText().trim();
            if (urlText.length() == 0) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "EMPTY_URL"));
                addButton.setEnabled(false);
                return;                
            }

            try {
                URL url = new URL(urlText);
                setErrorMessage("");
                addButton.setEnabled(true);
            }catch (MalformedURLException ex) {
                setErrorMessage(NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_URL"));
                addButton.setEnabled(false);
            }
        }else {
            setErrorMessage("");
            addButton.setEnabled(true);
        }
    }
    
    
    private void myInitComponents() {
        
        wsdlFileChooser = new JFileChooser();
        WsdlFileFilter myFilter = new WsdlFileFilter();
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
        
        
        jTxtWsdlURL.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtWsdlURL);
            }

            public void removeUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtWsdlURL);
            }

            public void changedUpdate(DocumentEvent e) {
                updateAddButtonState(jTxtWsdlURL);
            }
        });
        
        enableControls();
        
        setDefaults();
        
        
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
    
    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    private void cancelButtonAction(ActionEvent evt) {
        returnStatus = RET_CANCEL;
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
            jTxtWsdlURL.setEnabled(true);
            jTxtLocalFilename.setEnabled(false);
            updateAddButtonState(jTxtWsdlURL);
        }else if (jRbnFilesystem.isSelected()) {
            jTxtLocalFilename.setEnabled(true);
            jTxtWsdlURL.setEnabled(false);
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
    
    private String fixWsdlURL(String inURL) {
        String returnWsdlURL = inURL;
        if (!returnWsdlURL.toLowerCase().endsWith("wsdl")) { // NOI18N
            /**
             * If the user has left the ending withoug WSDL, they are pointing to the
             * web service representation on a web which will if suffixed by a ?WSDL
             * will return the WSDL.  This is true for web services created with JWSDP
             * - David Botterill 3/25/2004
             */
            returnWsdlURL += "?WSDL";
        }
        
        return returnWsdlURL;
    }
    
    
    /**
     * This represents the event on the "Add" button
     */
    private void addButtonAction(ActionEvent evt) {
        if ( (jTxtWsdlURL.getText() == null ) && (jTxtLocalFilename.getText() == null))
            return;
        
        final String wsdl;
        if (jRbnUrl.isSelected()) {
            wsdl = fixWsdlURL(jTxtWsdlURL.getText().trim());
        } else {
            wsdl = fixFileURL(jTxtLocalFilename.getText().trim());
        }
        final String packageName = jTxtpackageName.getText().trim();

        dialog.setVisible(false);
        dialog.dispose();
        dialog = null;
        
        // Run the add W/S asynchronously
        Runnable addWsRunnable = new Runnable() {
            public void run() {
                boolean addError = false;
                Exception exc = null;
                try {
                    WebServiceManager.getInstance().addWebService(wsdl, packageName, groupId);
                } catch (IOException ex) {
                    addError = true;
                    exc = ex;
                }

                final Exception exception = exc;
                if (addError) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (exception instanceof FileNotFoundException) {
                                String errorMessage = NbBundle.getMessage(AddWebServiceDlg.class, "INVALID_URL");
                                NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                                DialogDisplayer.getDefault().notify(d);
                            } else {
                                String cause = (exception != null) ? exception.getLocalizedMessage() : null;
                                String excString = (exception != null) ? exception.getClass().getName() + " - " + cause : null;

                                String errorMessage = NbBundle.getMessage(AddWebServiceDlg.class, "WS_ADD_ERROR") + "\n\n" + excString; // NOI18N
                                NotifyDescriptor d = new NotifyDescriptor.Message(errorMessage);
                                DialogDisplayer.getDefault().notify(d);
                            }
                        }
                    });
                }
            }
        };
        
        WebServiceManager.getInstance().getRequestProcessor().post(addWsRunnable);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLblChooseSource = new javax.swing.JLabel();
        jRbnFilesystem = new javax.swing.JRadioButton();
        jBtnBrowse = new javax.swing.JButton();
        jTxtWsdlURL = new javax.swing.JTextField();
        jBtnProxy = new javax.swing.JButton();
        jTxtLocalFilename = new javax.swing.JTextField();
        jRbnUrl = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
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

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_Browse")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });

        jTxtWsdlURL.setColumns(20);

        org.openide.awt.Mnemonics.setLocalizedText(jBtnProxy, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_ProxySettings")); // NOI18N
        jBtnProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnProxyActionPerformed(evt);
            }
        });

        jTxtLocalFilename.setColumns(20);

        buttonGroup1.add(jRbnUrl);
        jRbnUrl.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnUrl, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlUrl")); // NOI18N
        jRbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnUrlActionPerformed(evt);
            }
        });

        jLabel1.setLabelFor(jTxtpackageName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "PACKAGE_LABEL")); // NOI18N

        jTxtpackageName.setColumns(20);
        jTxtpackageName.setText("websvc");
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

        errorLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/websvc/manager/resources/warning.png"))); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(errorLabel))
                    .add(jLblChooseSource, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 589, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .add(jRbnFilesystem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(jTxtLocalFilename, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(12, 12, 12)
                        .add(jBtnBrowse, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 115, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(jRbnUrl)
                        .add(6, 6, 6)
                        .add(jTxtWsdlURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(12, 12, 12)
                        .add(jBtnProxy))
                    .add(layout.createSequentialGroup()
                        .add(jLabel1)
                        .add(8, 8, 8)
                        .add(jTxtpackageName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 345, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jLblChooseSource)
                .add(12, 12, 12)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jRbnFilesystem)
                    .add(layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(jTxtLocalFilename, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jBtnBrowse))
                .add(6, 6, 6)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jRbnUrl)
                    .add(layout.createSequentialGroup()
                        .add(1, 1, 1)
                        .add(jTxtWsdlURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jBtnProxy))
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(4, 4, 4)
                        .add(jLabel1))
                    .add(jTxtpackageName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(errorLabel)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        jRbnFilesystem.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFilelRadioButton.ACC_desc"));
        jBtnBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileButton.ACC_desc"));
        jTxtWsdlURL.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_name"));
        jTxtWsdlURL.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_desc"));
        jBtnProxy.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.httpProxyButton.ACC_desc"));
        jTxtLocalFilename.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_name"));
        jTxtLocalFilename.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_desc"));
        jRbnUrl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlRadioButton.ACC_desc"));
        jTxtpackageName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_name"));
        jTxtpackageName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_desc"));
        errorLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.errorLabel.ACC_name"));

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.main.ACC_name")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.main.ACC_desc")); // NOI18N
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



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel errorLabel;
    private javax.swing.JButton jBtnBrowse;
    private javax.swing.JButton jBtnProxy;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLblChooseSource;
    private javax.swing.JRadioButton jRbnFilesystem;
    private javax.swing.JRadioButton jRbnUrl;
    private javax.swing.JTextField jTxtLocalFilename;
    private javax.swing.JTextField jTxtWsdlURL;
    private javax.swing.JTextField jTxtpackageName;
    // End of variables declaration//GEN-END:variables
    
    
    
    private static class WsdlFileFilter extends  javax.swing.filechooser.FileFilter {
        public boolean accept(File f) {
            boolean result;
            if(f.isDirectory() || "wsdl".equalsIgnoreCase(FileUtil.getExtension(f.getName()))) { // NOI18N
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
