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


import java.io.IOException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.websvc.manager.WebServiceManager;


import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.*;

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
    
    private String URL_WSDL_MSG = NbBundle.getMessage(AddWebServiceDlg.class, "URL_WSDL_MSG");
    private String LOCAL_WSDL_MSG = NbBundle.getMessage(AddWebServiceDlg.class, "LOCAL_WSDL_MSG");
    private  final FileFilter WSDL_FILE_FILTER = new WsdlFileFilter();
    
    
    private int returnStatus = RET_CANCEL;
 
    private JButton cancelButton = new JButton();
    private JButton addButton = new JButton();
    private JPopupMenu resultsPopup = new JPopupMenu();
    
    private String groupId;
    
    
    public AddWebServiceDlg() {
        this(WebServiceListModel.DEFAULT_GROUP);
    }
    
    
    public AddWebServiceDlg(String groupId) {
        initComponents();
        myInitComponents();
        this.groupId = groupId;
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
                doAddButtonUpdate(jTxtLocalFilename.getText());
            }
            
            public void removeUpdate(DocumentEvent e) {
                doAddButtonUpdate(jTxtLocalFilename.getText());
            }
            
            public void changedUpdate(DocumentEvent e) {
                doAddButtonUpdate(jTxtLocalFilename.getText());
            }
        });
        
        
        jTxtWsdlURL.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                doAddButtonUpdate(jTxtWsdlURL.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                doAddButtonUpdate(jTxtWsdlURL.getText());
            }

            public void changedUpdate(DocumentEvent e) {
                doAddButtonUpdate(jTxtWsdlURL.getText());
            }
        });
        
        enableControls();
        
        setDefaults();
        
        
    }
    
        private void doAddButtonUpdate(String text) {
        if (text != null && text.trim().length() > 0) {
            addButtonEnable(true);
        } else {
            addButtonEnable(false);
        }
    }
    
    public void displayDialog(){
        
        dlg = new DialogDescriptor(this, NbBundle.getMessage(AddWebServiceDlg.class, "ADD_WEB_SERVICE"),
                false, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.CANCEL_OPTION,
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
        addButton.setEnabled(false);
        jRbnUrl.setSelected(true);
        jRbnFilesystem.setSelected(false);
//        displayInfo("<BR><BR><BR><BR><B>" +NbBundle.getMessage(AddWebServiceDlg.class, "INSTRUCTIONS") + "</B>");
        enableControls();
    }
    
    private void addButtonEnable(boolean value){
        addButton.setEnabled(value);
    }
    
    private void enableControls(){
        boolean enabled = false;
        
        enabled = jRbnUrl.isSelected();
        jTxtWsdlURL.setEnabled(enabled);
        
        String jTxtWsdlString = jTxtWsdlURL.getText();
        if (enabled && jTxtWsdlString != null && jTxtWsdlString.trim().length() > 0) {
            addButton.setEnabled(true);
        }else if (enabled) {
            addButton.setEnabled(false);
        }
        
        enabled = jRbnFilesystem.isSelected();
        jTxtLocalFilename.setEnabled(enabled);
        
        String jTxtLocalString = jTxtLocalFilename.getText();
        if (enabled && jTxtLocalString != null && jTxtLocalString.trim().length() > 0) {
            addButton.setEnabled(true);
        }else if (enabled) {
            addButton.setEnabled(false);
        }
    }
    
    
    private String fixFileURL(String inFileURL) {
        String returnFileURL = inFileURL;
        if(returnFileURL.substring(0,1).equalsIgnoreCase("/")) {
            returnFileURL = "file://" + returnFileURL;
        } else {
            returnFileURL = "file:///" + returnFileURL;
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
        java.awt.GridBagConstraints gridBagConstraints;

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

        setLayout(new java.awt.GridBagLayout());

        jLblChooseSource.setText(NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSource")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 12, 10);
        add(jLblChooseSource, gridBagConstraints);

        buttonGroup1.add(jRbnFilesystem);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnFilesystem, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlSourceFilesystem")); // NOI18N
        jRbnFilesystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnFilesystemActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 6, 0);
        add(jRbnFilesystem, gridBagConstraints);
        jRbnFilesystem.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFilelRadioButton.ACC_desc"));

        org.openide.awt.Mnemonics.setLocalizedText(jBtnBrowse, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_Browse")); // NOI18N
        jBtnBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnBrowseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 10);
        add(jBtnBrowse, gridBagConstraints);
        jBtnBrowse.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileButton.ACC_desc"));

        jTxtWsdlURL.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 6);
        add(jTxtWsdlURL, gridBagConstraints);
        jTxtWsdlURL.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_name"));
        jTxtWsdlURL.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlComboBox.ACC_desc"));

        org.openide.awt.Mnemonics.setLocalizedText(jBtnProxy, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_ProxySettings")); // NOI18N
        jBtnProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnProxyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 12, 10);
        add(jBtnProxy, gridBagConstraints);
        jBtnProxy.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.httpProxyButton.ACC_desc"));

        jTxtLocalFilename.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        add(jTxtLocalFilename, gridBagConstraints);
        jTxtLocalFilename.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_name"));
        jTxtLocalFilename.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.localFileComboBox.ACC_desc"));

        buttonGroup1.add(jRbnUrl);
        jRbnUrl.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(jRbnUrl, org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "LBL_WsdlUrl")); // NOI18N
        jRbnUrl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRbnUrlActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 12, 0);
        add(jRbnUrl, gridBagConstraints);
        jRbnUrl.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.urlRadioButton.ACC_desc"));

        jLabel1.setLabelFor(jTxtpackageName);
        jLabel1.setText(org.openide.util.NbBundle.getMessage(AddWebServiceDlg.class, "PACKAGE_LABEL")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 10, 12, 1);
        add(jLabel1, gridBagConstraints);

        jTxtpackageName.setColumns(20);
        jTxtpackageName.setText("websvc");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(8, 6, 12, 6);
        add(jTxtpackageName, gridBagConstraints);
        jTxtpackageName.getAccessibleContext().setAccessibleName(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_name"));
        jTxtpackageName.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(AddWebServiceDlg.class, "AddWebServiceDlg.packageTextField.ACC_desc"));

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
    // 		System.out.println("browse for wsdl file...");
    
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
