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

package org.netbeans.modules.versioning.system.cvss.ui.wizards;

import java.awt.Dialog;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeListener;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;

/**
 * UI that allows to configure selected root
 * and manage preconfigured roots pool.
 *
 * <p>It also allows to edit CVS root field by field.
 *
 * @author Petr Kuzel
 */
public final class RootWizard implements ActionListener, DocumentListener {

    private final RepositoryStep repositoryStep;
    private final CvsRootPanel rootPanel;
    private DialogDescriptor dd;
    
    private RootWizard(RepositoryStep step) {
        this.repositoryStep = step;
        rootPanel = null;
    }

    private RootWizard(CvsRootPanel rootPanel) {
        repositoryStep = null;
        this.rootPanel = rootPanel;
    }
    
    /**
     * Creates root configuration wizard with UI
     * that allows to set password, proxy, external
     * command, etc. (depends on root type).
     *
     * @return RootWizard
     */
    public static RootWizard configureRoot(String root) {
        RepositoryStep step = new RepositoryStep(root, RepositoryStep.ROOT_CONF_HELP_ID);
        step.applyStandaloneLayout();

        return new RootWizard(step);
    }

    /**
     * Shows field by filed CVS root customizer.
     *
     * @return customized value or null on cancel.
     */
    public static String editCvsRoot(String root) {
        CvsRootPanel rootPanel = new CvsRootPanel();
        RootWizard wizard = new RootWizard(rootPanel);
        return wizard.customizeRoot(root);        
    }

    private String customizeRoot(String root) {
        String access = "pserver"; // NOI18N
        String host = ""; // NOI18N
        String port = ""; // NOI18N
        String user = System.getProperty("user.name"); // NOI18N
        String repository = "";  // NOI18N
        try {
            CVSRoot cvsRoot = CVSRoot.parse(root);
            access = cvsRoot.getMethod();
            host = cvsRoot.getHostName();
            int portG = cvsRoot.getPort();
            if (portG > 0) {
                port = "" + portG;  // NOI18N
            }            
            user = cvsRoot.getUserName();
            repository = cvsRoot.getRepository();
        } catch (IllegalArgumentException ex) {
            // use defaults
        }
        rootPanel.accessComboBox.setSelectedItem(access);
        rootPanel.hostTextField.setText(host);
        rootPanel.portTextField.setText(port);
        rootPanel.userTextField.setText(user);
        rootPanel.repositoryTextField.setText(repository);
        
        rootPanel.accessComboBox.addActionListener(this);
        rootPanel.userTextField.getDocument().addDocumentListener(this);
        rootPanel.hostTextField.getDocument().addDocumentListener(this);
        rootPanel.userTextField.getDocument().addDocumentListener(this);
        rootPanel.portTextField.getDocument().addDocumentListener(this);
        rootPanel.repositoryTextField.getDocument().addDocumentListener(this);
                
        // workaround DD bug, 
        rootPanel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        dd = new DialogDescriptor(rootPanel, org.openide.util.NbBundle.getMessage(RootWizard.class, "BK2024"));
        dd.setHelpCtx(new HelpCtx(CvsRootPanel.class));
        dd.setModal(true);
        // all components visible
        rootPanel.setPreferredSize(rootPanel.getPreferredSize());
        updateVisibility();        
        checkInput();

        Dialog d = DialogDisplayer.getDefault().createDialog(dd);
        d.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(RootWizard.class, "ACSD_CvsRootPanel"));
        d.setVisible(true);
        
        if (DialogDescriptor.OK_OPTION.equals(dd.getValue())) {
            try {            
                String ret = collectRoot();
                CVSRoot cvsRoot = CVSRoot.parse(ret);
                return ret;
            } catch (IllegalArgumentException ex) {
                // use defaults
            }
        }

        return null;
    }
    
    private void updateVisibility() {
        String access = (String) rootPanel.accessComboBox.getSelectedItem();
        boolean hostVisible = ("pserver".equals(access) || "ext".equals(access));  // NOI18N
        rootPanel.userLabel.setVisible(hostVisible);
        rootPanel.userTextField.setVisible(hostVisible);
        rootPanel.hostLabel.setVisible(hostVisible);
        rootPanel.hostTextField.setVisible(hostVisible);
        rootPanel.portLabel.setVisible(hostVisible);
        rootPanel.portTextField.setVisible(hostVisible);
    }
    
    private String collectRoot() throws IllegalArgumentException {
        String method = (String) rootPanel.accessComboBox.getSelectedItem();
        boolean hasHost = ("pserver".equals(method) || "ext".equals(method)); // NOI18N

        StringBuffer sb = new StringBuffer(":"); // NOI18N
        sb.append(method);
        sb.append(":"); // NOI18N
        if (hasHost) {
            String s = rootPanel.userTextField.getText();
            if ("".equals(s.trim())) throw new IllegalArgumentException(); // NOI18N
            sb.append(s);
            sb.append("@"); // NOI18N
            s = rootPanel.hostTextField.getText();
            if ("".equals(s.trim())) throw new IllegalArgumentException(); // NOI18N
            sb.append(s);            
            sb.append(":"); // NOI18N
            String portS = rootPanel.portTextField.getText();
            if ("".equals(portS.trim()) == false) {  // NOI18N
                int portp = Integer.parseInt(portS);  // raise NFE
                if (portp > 0) {
                    sb.append(portS);
                }
            }
        }
        String s = rootPanel.repositoryTextField.getText();
        if ("".equals(s.trim())) throw new IllegalArgumentException(); // NOI18N
        sb.append(s);            

        return sb.toString();
    }
    
    private void checkInput() {
        try {            
            String ret = collectRoot();
            CVSRoot cvsRoot = CVSRoot.parse(ret);
            dd.setValid(true);
        } catch (IllegalArgumentException ex) {
            dd.setValid(false);
        }
        
    }
    
    /**
     * Gets UI panel representing RootWizard.
     */
    public JPanel getPanel() {
        RepositoryPanel repositoryPanel = (RepositoryPanel) repositoryStep.getComponent();
        return repositoryPanel;
    }

    /**
     * Propagates configuration changes
     * from UI into {@link org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings}.
     *
     * @param validate on true only valid values are commited
     * @return <code>null</codE> on successfull commit otherwise error message.
     */
    public String commit(boolean validate) {
        if (validate) {
            repositoryStep.prepareValidation();
            repositoryStep.validateBeforeNext();
            if (repositoryStep.isValid() == false) {
                return repositoryStep.getErrorMessage();
            }
        }
        repositoryStep.storeValidValues();
        return null;

    }

    /** Return result of light-weight validation.*/
    public boolean isValid() {
        return repositoryStep.isValid();
    }

    /** Allows to listen on valid. */
    public void addChangeListener(ChangeListener l) {
        repositoryStep.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        repositoryStep.removeChangeListener(l);
    }

    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
        updateVisibility();
        checkInput();
    }

    public void changedUpdate(javax.swing.event.DocumentEvent documentEvent) {
    }

    public void insertUpdate(javax.swing.event.DocumentEvent documentEvent) {
        checkInput();
    }

    public void removeUpdate(javax.swing.event.DocumentEvent documentEvent) {
        checkInput();
    }
}
