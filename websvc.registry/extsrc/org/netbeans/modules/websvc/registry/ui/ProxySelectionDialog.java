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

package org.netbeans.modules.websvc.registry.ui;

import org.netbeans.modules.websvc.registry.util.WebProxySetter;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Font;
import java.lang.ClassLoader;
import java.lang.reflect.Method;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.NbBundle;

/**
 *
 * @author  Winston Prakash
 */
public class ProxySelectionDialog extends javax.swing.JPanel implements ActionListener {
    
    DialogDescriptor dlg = null;
    Dialog dialog = null;
    
    private javax.swing.JLabel headerLabel;
    private javax.swing.JTextField portText;
    private javax.swing.JLabel portlabel;
    private javax.swing.JTextField serverText;
    private javax.swing.JLabel serverlabel;
    
    private JButton okButton;
    private JButton cancelButton;
    
    private String okString = NbBundle.getMessage(ProxySelectionDialog.class, "OPTION_OK");
    private String cancelString = NbBundle.getMessage(ProxySelectionDialog.class, "OPTION_CANCEL");
    
    public ProxySelectionDialog() {
        initComponents();
    }
    
    public void show(){
        dlg = new DialogDescriptor(this, NbBundle.getMessage(ProxySelectionDialog.class, "SET_PROXY"),
        true, NotifyDescriptor.OK_CANCEL_OPTION, DialogDescriptor.CANCEL_OPTION,this);
        dlg.setOptions(new Object[] {okButton, cancelButton});
        dialog = DialogDisplayer.getDefault().createDialog(dlg);
        dialog.setVisible(true);
        
        /**
         * After the window is opened, set the focus to the Get information button.
         */
        
        final JPanel thisPanel = this;
        dialog.addWindowListener( new WindowAdapter(){
            public void windowOpened( WindowEvent e ){
                SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        okButton.requestFocus();
                        thisPanel.getRootPane().setDefaultButton(okButton);
                    }
                });
            }
        });
        
        
        /*JDialog dialog = new JDialog();
        dialog.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.setProperty("http.proxyHost", serverText.getText());
                System.out.println(System.getProperty("http.proxyHost"));
                System.setProperty("http.proxyPort", portText.getText());
                System.out.println(System.getProperty("http.proxyPort"));
            }
        });
        dialog.getContentPane().add(this);
        dialog.pack();
        dialog.show();*/
    }
    
    private void initComponents()  {
        java.awt.GridBagConstraints gridBagConstraints;
        
        serverlabel = new javax.swing.JLabel();
        serverText = new javax.swing.JTextField();
        portlabel = new javax.swing.JLabel();
        portText = new javax.swing.JTextField();
        headerLabel = new javax.swing.JLabel();
        
        okButton = new JButton(okString);
        cancelButton = new JButton(cancelString);
        
        setLayout(new java.awt.GridBagLayout());
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 10, 10, 10)));
        serverlabel.setText(NbBundle.getMessage(ProxySelectionDialog.class, "LBL_HTTP_PROXY_SERVER"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 1, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(serverlabel, gridBagConstraints);
        
        serverText.setText(WebProxySetter.getInstance().getProxyHost());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        gridBagConstraints.weightx = 1.0;
        add(serverText, gridBagConstraints);
        
        portlabel.setText(NbBundle.getMessage(ProxySelectionDialog.class, "LBL_HTTP_PROXY_PORT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 6, 1);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(portlabel, gridBagConstraints);
        
        portText.setText(WebProxySetter.getInstance().getProxyPort());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        gridBagConstraints.weightx = 1.0;
        add(portText, gridBagConstraints);
        
        headerLabel.setText(NbBundle.getMessage(ProxySelectionDialog.class, "LBL_MANUAL_PROXY_SETTING"));
        Font currentFont = headerLabel.getFont();
        Font boldFont = currentFont.deriveFont(Font.BOLD);
        headerLabel.setFont(boldFont);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.CENTER;
        add(headerLabel, gridBagConstraints);
        
        
    }
    
    public void actionPerformed(ActionEvent evt) {
        String actionCommand = evt.getActionCommand();
        if(actionCommand.equalsIgnoreCase(okString)) {
            WebProxySetter.getInstance().setProxyConfiguration(serverText.getText().trim(), portText.getText().trim());
            dialog.dispose();  
        } else if(actionCommand.equalsIgnoreCase(cancelString)) {
            dialog.dispose();  
        }
        
    }
    
}
