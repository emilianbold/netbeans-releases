/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
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

package org.netbeans.modules.jmx.managerwizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.netbeans.modules.jmx.common.WizardConstants;
import org.netbeans.modules.jmx.mbeanwizard.listener.TextFieldFocusListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.openide.util.NbBundle;
import org.openide.awt.Mnemonics;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

/**
 *
 * @author  an156382
 */
public class ManagerPopup extends javax.swing.JPanel implements DocumentListener,
    FocusListener {
    
    private ResourceBundle bundle;
    private JButton okJButton;
    private JPanel panel;
    
    /**
     * Creates new form ManagerPopup 
     */
    public ManagerPopup(JPanel ancestorPanel) {
        this.panel = ancestorPanel;
        bundle = NbBundle.getBundle(ManagerPopup.class);
        initComponents();
        
        okJButton = new JButton(bundle.getString("LBL_okButton.text")); // NOI18N
        rmiHostJTextField.setText(NbBundle.getBundle("org.netbeans.modules.jmx.managerwizard.Bundle_noi18n").getString("TXT_host"));// NOI18N
        rmiPortJTextField.setText(NbBundle.getBundle("org.netbeans.modules.jmx.managerwizard.Bundle_noi18n").getString("TXT_port"));// NOI18N
        
        protocolJComboBox.addItem(bundle.getString("TXT_protocol"));// NOI18N
        
        Mnemonics.setLocalizedText(protocolJLabel,bundle.getString("LBL_protocol.text"));// NOI18N
        Mnemonics.setLocalizedText(rmiHostJLabel,bundle.getString("LBL_host.text"));// NOI18N
        Mnemonics.setLocalizedText(rmiPortJLabel,bundle.getString("LBL_port.text"));// NOI18N
        Mnemonics.setLocalizedText(okJButton,bundle.getString("LBL_okButton.text"));// NOI18N
        Mnemonics.setLocalizedText(urlJLabel,bundle.getString("LBL_url.text"));// NOI18N
        
        // Accessibility
        urlJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_URLPATH")); // NOI18N
        urlJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_URLPATH_DESCRIPTION")); // NOI18N
        rmiHostJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RMI_HOST")); // NOI18N
        rmiHostJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RMI_HOST_DESCRIPTION")); // NOI18N
        rmiPortJTextField.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_RMI_PORT")); // NOI18N
        rmiPortJTextField.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_RMI_PORT_DESCRIPTION")); // NOI18N
        okJButton.getAccessibleContext().setAccessibleName(
                bundle.getString("ACCESS_OK")); // NOI18N
        okJButton.getAccessibleContext().setAccessibleDescription(
                bundle.getString("ACCESS_OK_DESCRIPTION")); // NOI18N
        
        setName("ManagerPopup");// NOI18N
        
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_PANEL"));// NOI18N
        
        String urlText = getPanel().getJmxURLJTextField().getText();
        if (urlText.equals(WizardConstants.EMPTYSTRING))
            updateURLPathWithHostAndPort();
        else
            parseURL();
        
        addListeners();
        
        configure();
        protocolJComboBox.requestFocus();
    }
    
    private AgentURLPanel getPanel() {
        return (AgentURLPanel)panel;
    }
    
    /**
     * Displays a configuration dialog and updates Register MBean options
     * according to the user's settings.
     * @return <CODE>boolean</CODE> true only if user clicks on Ok button.
     */
    public boolean configure() {
        
        // create and display the dialog:
        String title = bundle.getString("LBL_RMIAgentURL_Popup"); // NOI18N
        
        Object returned = DialogDisplayer.getDefault().notify(
                new DialogDescriptor(
                this,
                title,
                true,                       //modal
                new Object[] {okJButton, DialogDescriptor.CANCEL_OPTION},
                okJButton,                      //initial value
                        DialogDescriptor.DEFAULT_ALIGN,
                        null,
                        (ActionListener) null
                        ));
                
                if (returned == okJButton) {
                    return true;
                }
                return false;
    }
    
    public String getHostFieldText() {
        return rmiHostJTextField.getText();
    }
    
    public String getPortFieldText() {
        return rmiPortJTextField.getText();
    }
    
    private void addListeners() {
        rmiHostJTextField.getDocument().addDocumentListener(this);
        rmiPortJTextField.getDocument().addDocumentListener(this); 
        rmiHostJTextField.addFocusListener(this);
        rmiPortJTextField.addFocusListener(this);
        
        rmiPortJTextField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!(Character.isDigit(c) ||
                        c == KeyEvent.VK_BACK_SPACE ||
                        c == KeyEvent.VK_DELETE)) {
                    getToolkit().beep();
                    e.consume();
                }
            }
            
            public void keyPressed(KeyEvent e) {
            }
            
            public void keyReleased(KeyEvent e) {
            }
        });
        
        okJButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                okButtonAction();
            }
        });
        
        // documentlistener for the combobox
        ((JTextField) protocolJComboBox.getEditor().getEditorComponent()).
                getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                updateURLPath(e.getDocument());
            }
            
            public void removeUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                updateURLPath(e.getDocument());
            }
            
            public void changedUpdate(DocumentEvent e) {
                Document doc = e.getDocument();
                updateURLPath(e.getDocument());
            }
        });
        
        protocolJComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent evt) {
                updateURLPath(null);
            }
        });
    }
    
    public void updateURLPath(Document doc) {
        boolean isRmi = true;
        
        if (doc != null) {
            try {
                String txt = doc.getText(0, doc.getLength());
                isRmi = txt.equals(bundle.getString("TXT_protocol"));// NOI18N
            } catch (javax.swing.text.BadLocationException e) {
                e.printStackTrace();
            }
        } else {
            isRmi = protocolJComboBox.getSelectedItem().equals(bundle.getString("TXT_protocol"));// NOI18N
        }
        
        if (isRmi) {
            urlJTextField.setText(bundle.getString("TXT_JNDIRMI") + // NOI18N
                    getHostFieldText()+":"+getPortFieldText()+// NOI18N
                    bundle.getString("TXT_JMXRMI"));// NOI18N
        } else {
            urlJTextField.setText(WizardConstants.EMPTYSTRING);
        }
        urlJTextField.setEditable(!isRmi);
        
    }
    
    private void okButtonAction() {
        String protocol = WizardConstants.EMPTYSTRING;
        if (protocolJComboBox.getSelectedItem().equals(bundle.getString("TXT_protocol")))// NOI18N
            protocol = bundle.getString("TXT_RMI");// NOI18N
        else
            protocol = ((String)protocolJComboBox.getSelectedItem());
        
        String completePort = rmiPortJTextField.getText();
        if (!completePort.equals(WizardConstants.EMPTYSTRING))
            completePort = ":"+rmiPortJTextField.getText();// NOI18N
        
        String suffix = urlJTextField.getText();
        if (!suffix.equals(WizardConstants.EMPTYSTRING)) {
            if (!suffix.startsWith("/"))// NOI18N
                suffix = "/" + suffix;// NOI18N
        }
        
        getPanel().getJmxURLJTextField().setText(bundle.getString("TXT_SERVICEJMX") + protocol +// NOI18N
                "://" + rmiHostJTextField.getText() +  completePort +// NOI18N
                suffix);
        
        // save host, port and suffix for future needs
        getPanel().setProtocol((String)protocolJComboBox.getSelectedItem());
        getPanel().setHost(rmiHostJTextField.getText());
        getPanel().setPort(rmiPortJTextField.getText());
        getPanel().setSuffix(urlJTextField.getText());
    }
    
    public void insertUpdate(DocumentEvent e) {
        updateURLPathWithHostAndPort();
    }
    
    public void removeUpdate(DocumentEvent e) {
        updateURLPathWithHostAndPort();
    }
    
    public void changedUpdate(DocumentEvent e) {
        updateURLPathWithHostAndPort();
    }
    
    public void focusGained(FocusEvent evt) {
        ((JTextField)evt.getSource()).selectAll();
    }
    
    public void focusLost(FocusEvent evt) {
        ((JTextField)evt.getSource()).select(0,0);
    }
    
    private void updateURLPathWithHostAndPort() {
        Object protocol = protocolJComboBox.getSelectedItem();
        boolean isRmi = protocol.equals(bundle.getString("TXT_protocol"));// NOI18N
        String completePort = rmiPortJTextField.getText();
        
        if (!completePort.equals(WizardConstants.EMPTYSTRING))
            completePort = (":" + completePort);// NOI18N
        
        String suffix = bundle.getString("TXT_JNDIRMI");// NOI18N
        if (!suffix.startsWith("/"))// NOI18N
            suffix = "/" + suffix;// NOI18N
        
        if (isRmi) {
            urlJTextField.setText(
                    suffix + rmiHostJTextField.getText() +// NOI18N
                    completePort + bundle.getString("TXT_JMXRMI"));// NOI18N
        }
    }
    
    private void parseURL() {
        
        protocolJComboBox.setSelectedItem(getPanel().getProtocol());
        rmiHostJTextField.setText(getPanel().getHost());
        rmiPortJTextField.setText(getPanel().getPort());
        
        if (!getPanel().getProtocol().equals(bundle.getString("TXT_protocol")))// NOI18N
            urlJTextField.setEditable(true);
        
        if (!getPanel().getSuffix().equals(WizardConstants.EMPTYSTRING))
            urlJTextField.setText(getPanel().getSuffix());// NOI18N
    }
    
    // regular expression which matches rmi protocol with jndi suffix
    public static String JNDI = "jndi/rmi://[a-zA-Z0-9.-_:]*:[0-9]*/jmxrmi";// NOI18N
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        rmiParameterPanel = new javax.swing.JPanel();
        rmiHostJLabel = new javax.swing.JLabel();
        rmiPortJLabel = new javax.swing.JLabel();
        rmiHostJTextField = new javax.swing.JTextField();
        rmiPortJTextField = new javax.swing.JTextField();
        protocolJComboBox = new javax.swing.JComboBox();
        urlJLabel = new javax.swing.JLabel();
        urlJTextField = new javax.swing.JTextField();
        protocolJLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        rmiParameterPanel.setLayout(new java.awt.GridBagLayout());

        rmiHostJLabel.setLabelFor(rmiHostJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        rmiParameterPanel.add(rmiHostJLabel, gridBagConstraints);

        rmiPortJLabel.setLabelFor(rmiPortJTextField);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 11, 0, 0);
        rmiParameterPanel.add(rmiPortJLabel, gridBagConstraints);

        rmiHostJTextField.setName("hostJTextField");
        rmiHostJTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 11);
        rmiParameterPanel.add(rmiHostJTextField, gridBagConstraints);

        rmiPortJTextField.setName("portJTextField");
        rmiPortJTextField.setPreferredSize(new java.awt.Dimension(150, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 0, 11);
        rmiParameterPanel.add(rmiPortJTextField, gridBagConstraints);

        protocolJComboBox.setEditable(true);
        protocolJComboBox.setName("protocolComboBox");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 0);
        rmiParameterPanel.add(protocolJComboBox, gridBagConstraints);

        urlJLabel.setLabelFor(urlJTextField);
        urlJLabel.setName("urlLabel");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        rmiParameterPanel.add(urlJLabel, gridBagConstraints);

        urlJTextField.setEditable(false);
        urlJTextField.setName("urlTextField");
        urlJTextField.setPreferredSize(new java.awt.Dimension(250, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 5, 0, 11);
        rmiParameterPanel.add(urlJTextField, gridBagConstraints);

        protocolJLabel.setLabelFor(protocolJComboBox);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        rmiParameterPanel.add(protocolJLabel, gridBagConstraints);

        add(rmiParameterPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox protocolJComboBox;
    private javax.swing.JLabel protocolJLabel;
    protected javax.swing.JLabel rmiHostJLabel;
    protected javax.swing.JTextField rmiHostJTextField;
    private javax.swing.JPanel rmiParameterPanel;
    protected javax.swing.JLabel rmiPortJLabel;
    protected javax.swing.JTextField rmiPortJTextField;
    private javax.swing.JLabel urlJLabel;
    private javax.swing.JTextField urlJTextField;
    // End of variables declaration//GEN-END:variables
    
}
