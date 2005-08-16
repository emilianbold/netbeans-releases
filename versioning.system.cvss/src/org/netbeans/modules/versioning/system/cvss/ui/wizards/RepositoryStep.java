/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.wizards;

import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.modules.versioning.system.cvss.settings.HistorySettings;
import org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.modules.versioning.system.cvss.SSHConnection;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ProxySelector;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ProxyDescriptor;
import org.netbeans.modules.proxy.ConnectivitySettings;
import org.netbeans.modules.proxy.ClientSocketFactory;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.net.SocketFactory;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.util.Vector;
import java.util.Iterator;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * UI for CvsRootSettings. After initialization data
 * are taken directly from UI. These are propagated
 * into CvsRootSettings on {@link #storeValidValues()}.
 *
 * @author Petr Kuzel
 */
class RepositoryStep extends AbstractStep implements WizardDescriptor.AsynchronousValidatingPanel, ActionListener, DocumentListener {

    private RequestProcessor.Task updatePasswordTask;
    private volatile boolean passwordExpected;

    private boolean userVisitedProxySettings;

    private ProgressHandle progress;
    private JComponent progressComponent;
    private volatile boolean internalDocumentChange;
    private Thread backgroundValidationThread;
    private RepositoryPanel repositoryPanel;
    private ProxyDescriptor proxyDescriptor;
    private String scrambledPassword;
    private final String initialCvsRoot;

    public RepositoryStep(String root) {
        initialCvsRoot = root;
    }

    protected JComponent createComponent() {
        repositoryPanel = new RepositoryPanel();

        // password field, features automatic fill from ~/.cvspass

        repositoryPanel.passwordTextField.getDocument().addDocumentListener(this);
        RequestProcessor requestProcessor = new RequestProcessor();
        updatePasswordTask = requestProcessor.create(new Runnable() {
            public void run() {
                String cvsRoot = selectedCvsRoot();
                String password = PasswordsFile.findPassword(cvsRoot);
                if (password != null && passwordExpected) {
                    String fakePasswordWithProperLen = new String(password).substring(1);
                    scrambledPassword = password;
                    internalDocumentChange = true;
                    repositoryPanel.passwordTextField.setText(fakePasswordWithProperLen);
                    internalDocumentChange = false;
                    cancelPasswordUpdate();
                }
            }
        });

        // roots combo setup, keeping history

        Vector recentRoots = new Vector(HistorySettings.getRecent(HistorySettings.PROP_CVS_ROOTS));
        if (initialCvsRoot != null) {
            // it's first => initially selected
            recentRoots.add(initialCvsRoot);
        }
        Iterator cvsPassRoots = PasswordsFile.listRoots(":pserver:").iterator();  // NOI18N
        while (cvsPassRoots.hasNext()) {
            String next = (String) cvsPassRoots.next();
            if (recentRoots.contains(next) == false) {
                recentRoots.add(next);
            }
        }
        // templates for supported connection methods
        String user = System.getProperty("user.name", ""); // NOI18N
        if (user.length() > 0) user += "@"; // NOI18N
        recentRoots.add(":pserver:" + user);  // NOI18N
        recentRoots.add(":ext:" + user); // NOI18N
        recentRoots.add(":fork:"); // NOI18N
        recentRoots.add(":local:"); // NOI18N

        ComboBoxModel rootsModel = new DefaultComboBoxModel(recentRoots);
        repositoryPanel.rootComboBox.setModel(rootsModel);
        repositoryPanel.rootComboBox.addActionListener(this);
        Component editor = repositoryPanel.rootComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        if (recentRoots.size() == 0) {
            textEditor.setText(":pserver:" + user);    // NOI18N
        } else {
            validateCvsRoot();
            CVSRoot root = getCVSRoot();
            proxyDescriptor = CvsRootSettings.getProxyFor(root);
            schedulePasswordUpdate();
        }
        textEditor.selectAll();
        textEditor.getDocument().addDocumentListener(this);

        // proxy config button
        // it must not be accesible if Java environment defines socksProxyHost
        // property because it influences ALL new Socket() that crashes
        // our ClientSocketFactory. It may not be able to connect
        // SOCKS (general rule failure) or HTTP (ruleset failure)
        // proxy via Java platform defined SOCKS proxy.

        String hostName = System.getProperty("socksProxyHost");  // NOI18N
        repositoryPanel.proxyConfigurationButton.setEnabled(hostName == null);
        repositoryPanel.proxyConfigurationButton.addActionListener(this);

        valid();
        onCvsRootChange();

        return repositoryPanel;
    }

    /**
     * Heavy validation over network.
     * Sets wizard as invalid to disable next button
     * and starts. It's invoked in background validation thread.
     */
    protected void validateBeforeNext() {

        CVSRoot root = getCVSRoot();
        if (root == null) return;
        String host = root.getHostName();
        String userName = root.getUserName();
        int port = root.getPort();
        Socket sock = null;
        Connection connection = null;
        try {

            backgroundValidationThread = Thread.currentThread();

            if (root.isLocal()) {
                LocalConnection lconnection = new LocalConnection();
                lconnection.setRepository(root.getRepository());
                lconnection.verify();
            } else {
                invalid(NbBundle.getMessage(CheckoutWizard.class, "BK2011"));
                SocketFactory factory = SocketFactory.getDefault();
                if (proxyDescriptor != null && proxyDescriptor.isEffective()) {
                    ConnectivitySettings connectivitySettings = ClientRuntime.toConnectivitySettings(proxyDescriptor);
                    factory = new ClientSocketFactory(connectivitySettings);
                }

                // check raw network reachability

                if (CVSRoot.METHOD_PSERVER.equals(root.getMethod())) {
                    port = port == 0 ? 2401 : port;  // default port

                    SocketAddress target = new InetSocketAddress(host, port);
                    sock = factory.createSocket();
                    sock.connect(target, 5000);
                    sock.close();

                    // try to login
                    invalid(NbBundle.getMessage(CheckoutWizard.class, "BK2010"));
                    PServerConnection pconnection = new PServerConnection(root, factory);
                    String password = getScrambledPassword();
                    pconnection.setEncodedPassword(password);
                    pconnection.verify();
                } else if (CVSRoot.METHOD_EXT.equals(root.getMethod())) {
                    if (repositoryPanel.internalSshRadioButton.isSelected()) {
                        port = port == 0 ? 22 : port;  // default port
                        String password = repositoryPanel.extPasswordField.getText();
                        SSHConnection sshConnection = new SSHConnection(factory, host, port, userName, password);
                        sshConnection.setRepository(root.getRepository());
                        sshConnection.verify();
                    } else {
                        String command = repositoryPanel.extCommandTextField.getText();
                        String cvs_server = System.getProperty("Env-CVS_SERVER", "cvs") + " server";  // NOI18N
                        command += " " + host + " -l" + userName + " " + cvs_server; // NOI18N
                        ExtConnection econnection = new ExtConnection(command);
                        econnection.setRepository(root.getRepository());
                        econnection.verify();
                    }
                } else {
                    assert false : "Login check implemented only for pserver";  // NOI18N
                }
            }

            // SUCCESS
            valid();
            storeValidValues();
        } catch (IOException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Test connection failed, suggesting to use a proxy."); // NOi18N
            err.notify(ErrorManager.INFORMATIONAL, e);
            String msg = NbBundle.getMessage(CheckoutWizard.class, "BK1001", host);
            invalid(msg);
        } catch (AuthenticationException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Connection authentification verification failed.");  // NOI18N
            err.notify(ErrorManager.INFORMATIONAL, e);
            String msg = NbBundle.getMessage(CheckoutWizard.class, "BK1002");
            invalid(msg);
        } finally {
            if (sock != null) {
                try {
                    sock.close();
                } catch (IOException e) {
                    // already closed
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    // already closed
                }
            }

            backgroundValidationThread = null;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    validationDone();
                }
            });
        }

    }

    private void validationDone() {
        progress.finish();
        repositoryPanel.jPanel1.remove(progressComponent);
        repositoryPanel.jPanel1.revalidate();
        repositoryPanel.jPanel1.repaint();
        editable(true);
    }

    private void editable(boolean editable) {
        repositoryPanel.rootComboBox.setEditable(editable);
        repositoryPanel.passwordTextField.setEditable(editable);
        repositoryPanel.extCommandTextField.setEditable(editable);
        repositoryPanel.extPasswordField.setEditable(editable);

        repositoryPanel.proxyConfigurationButton.setEnabled(editable);
        repositoryPanel.extREmemberPasswordCheckBox.setEnabled(editable);
        repositoryPanel.internalSshRadioButton.setEnabled(editable);
        repositoryPanel.extSshRadioButton.setEnabled(editable);
    }


    void storeValidValues() {
        String root = selectedCvsRoot();
        CVSRoot cvsRoot = CVSRoot.parse(root);
        boolean storeProxySettings = false;
        if (root.startsWith(":pserver:")) { // NOI18N
            storeProxySettings = true;
            try {
                // CVSclient library reads password directly from .cvspass file
                // store it here into the file. It's potentionally necessary for
                // next step branch and module browsers

                PasswordsFile.storePassword(root, getScrambledPassword());
            } catch (IOException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Can not write password file. Unfortunately further actions need to read from it.");
                err.notify(e);
            }
        } else if (root.startsWith(":ext:")) {  // NOI18N
            boolean internalSsh = repositoryPanel.internalSshRadioButton.isSelected();
            storeProxySettings = internalSsh;
            CvsRootSettings.ExtSettings extSettings = new CvsRootSettings.ExtSettings();
            extSettings.extUseInternalSsh = internalSsh;
            if (internalSsh) {
                extSettings.extPassword = repositoryPanel.extPasswordField.getText();
                extSettings.extRememberPassword = repositoryPanel.extREmemberPasswordCheckBox.isSelected();
            } else {
                extSettings.extCommand = repositoryPanel.extCommandTextField.getText();
            }
            CvsRootSettings.setExtSettingsFor(cvsRoot, extSettings);
        }

        if (storeProxySettings) {
            CvsRootSettings.setProxyFor(cvsRoot, getProxyDescriptor());
        }

        HistorySettings.addRecent(HistorySettings.PROP_CVS_ROOTS, root);
    }

    /**
     * Fast root syntax check. It can invalidate whole step
     * but neder set it as valid.
     */
    private boolean validateCvsRoot() {
        String cvsRoot = selectedCvsRoot();
        String errorMessage = null;
        boolean supportedMethod = cvsRoot.startsWith(":pserver:"); // NOI18N
        supportedMethod |= cvsRoot.startsWith(":local:"); // NOI18N
        supportedMethod |= cvsRoot.startsWith(":fork:"); // NOI18N
        supportedMethod |= cvsRoot.startsWith(":ext:"); // NOI18N
        if (cvsRoot == null || supportedMethod == false ) {
            errorMessage = NbBundle.getMessage(CheckoutWizard.class, "BK1000");
        } else {
            try {
                CVSRoot root = CVSRoot.parse(cvsRoot);
                // XXX typical windows cvs program does not support fork
                // cvslibrary needs fork to emulate local
                // here we bet on fact that on Windows all paths starct with drive letter
                if (root.isLocal() && root.getRepository().length() > 0) {
                    if (root.getRepository().startsWith("/") == false) {  // NOI18N
                        errorMessage = NbBundle.getMessage(CheckoutWizard.class, "BK1003");
                    }
                }
            } catch (IllegalArgumentException ex) {
                errorMessage = "Invalid CVS Root: " + ex.getLocalizedMessage();
            }
        }
        if (errorMessage != null) {
            invalid(errorMessage);
        }
        return errorMessage == null;
    }

    /**
     * On valid CVS root loads UI fields from CvsRootSettings.
     * Always updates UI fields visibility.
     */
    private void onCvsRootChange() {
        if (validateCvsRoot()) {
            valid();
            CVSRoot root = getCVSRoot();
            if (userVisitedProxySettings == false) {
                // load  proxy from history
                proxyDescriptor = CvsRootSettings.getProxyFor(root);
            }
            if (CVSRoot.METHOD_EXT.equals(root.getMethod())) {
                CvsRootSettings.ExtSettings extSettings = CvsRootSettings.getExtSettingsFor(root);
                repositoryPanel.internalSshRadioButton.setSelected(extSettings.extUseInternalSsh);
                repositoryPanel.extPasswordField.setText(extSettings.extPassword);
                repositoryPanel.extREmemberPasswordCheckBox.setSelected(extSettings.extRememberPassword);
                repositoryPanel.extCommandTextField.setText(extSettings.extCommand);
            }
            schedulePasswordUpdate();
        }
        updateVisibility();
        updateLabel();
    }

    private void updateLabel() {
        String cvsRoot = selectedCvsRoot();
        if (cvsRoot.startsWith(":pserver:")) { // NOI18N
            repositoryPanel.descLabel.setText("( :pserver:username@hostname:/repository_path )");  // NOI18N
        } else if (cvsRoot.startsWith(":local:")) { // NOI18N
            repositoryPanel.descLabel.setText("( :local:/repository_path )");  // NOI18N
        } else if (cvsRoot.startsWith(":fork:")) { // NOI18N
            repositoryPanel.descLabel.setText("( :fork:/repository_path )");  // NOI18N
        } else if (cvsRoot.startsWith(":ext:")) { // NOI18N
            repositoryPanel.descLabel.setText("( :ext:username@hostname:/repository_path )");  // NOI18N
        } else {
            repositoryPanel.descLabel.setText(NbBundle.getMessage(CheckoutWizard.class, "BK1014"));
        }

    }

    /** Shows proper fields depending on CVS root connection method. */
    private void updateVisibility() {
        String root = selectedCvsRoot();
        boolean showPserverFields = root.startsWith(":pserver:");
        boolean showExtFields = root.startsWith(":ext:");

        repositoryPanel.passwordTextField.setVisible(showPserverFields);
        repositoryPanel.pPaswordLabel.setVisible(showPserverFields);

        repositoryPanel.internalSshRadioButton.setVisible(showExtFields);
        repositoryPanel.extSshRadioButton.setVisible(showExtFields);

        repositoryPanel.extPasswordLabel5.setVisible(showExtFields);
        repositoryPanel.extPasswordField.setVisible(showExtFields);
        repositoryPanel.extREmemberPasswordCheckBox.setVisible(showExtFields);

        repositoryPanel.extCommandLabel.setVisible(showExtFields);
        repositoryPanel.extCommandTextField.setVisible(showExtFields);

        repositoryPanel.proxyConfigurationButton.setVisible(showPserverFields || showExtFields);
    }

    /**
     * Load selected root from Swing structures (from arbitrary thread).
     * @return null on failure
     */
    private String selectedCvsRoot() {
        if (initialCvsRoot != null) {
            return initialCvsRoot;
        }
        final String cvsRoot[] = new String[1];
        try {
            Runnable awt = new Runnable() {
                public void run() {
                    cvsRoot[0] = (String) repositoryPanel.rootComboBox.getEditor().getItem();
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                awt.run();
            } else {
                SwingUtilities.invokeAndWait(awt);
            }
            return cvsRoot[0];
        } catch (InterruptedException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        } catch (InvocationTargetException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        }
        return null;
    }

    private CVSRoot getCVSRoot() {
        try {
            String root = selectedCvsRoot();
            return CVSRoot.parse(root);
        } catch (IllegalArgumentException e) {
            // expected, it means invalid root
        }
        return null;
    }

    /**
     * Visually notifies user about password length
     */
    private void schedulePasswordUpdate() {
        String root = selectedCvsRoot();
        if (root.startsWith(":pserver:")) { // NOI18N
            passwordExpected = true;
            updatePasswordTask.schedule(10);
        }
    }

    private void cancelPasswordUpdate() {
        passwordExpected = false;
    }

    private void onPasswordChange() {
        cancelPasswordUpdate();
        scrambledPassword = null;
        valid();
    }

    private void onProxyConfiguration() {
        ProxySelector selector = new ProxySelector();
        selector.setProxyDescriptor(proxyDescriptor);
        ProxyDescriptor pd = selector.selectProxy();
        if (pd != null) {
            proxyDescriptor = pd;
            userVisitedProxySettings = true;
            valid();
        }
    }

    // hooks

    public void actionPerformed(ActionEvent e) {
        if (repositoryPanel.proxyConfigurationButton == e.getSource()) {
            onProxyConfiguration();
        } else if (repositoryPanel.rootComboBox == e.getSource()) {
            onCvsRootChange();
        } else {
            assert false : "Unexpected event source: " + e.getSource();
        }
    }

    public void changedUpdate(DocumentEvent e) {
    }

    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content from selectedCvsRoot
        if (internalDocumentChange) return;
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == repositoryPanel.passwordTextField.getDocument()) {
                    onPasswordChange();
                } else {   // combo
                    onCvsRootChange();
                }
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    public void prepareValidation() {
        progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(CheckoutWizard.class, "BK2012"));
        JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
        JButton stopButton = new JButton("Stop");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (backgroundValidationThread != null) {
                    backgroundValidationThread.interrupt();
                }
            }
        });
        progressComponent = new JPanel();
        progressComponent.setLayout(new BorderLayout());
        progressComponent.add(bar, BorderLayout.CENTER);
        progressComponent.add(stopButton, BorderLayout.LINE_END);
        progress.start(/*2, 5*/);
        repositoryPanel.jPanel1.setLayout(new BorderLayout());
        repositoryPanel.jPanel1.add(progressComponent, BorderLayout.SOUTH);
        repositoryPanel.jPanel1.revalidate();

        editable(false);
    }

    private String getPassword() {
        return new String(repositoryPanel.passwordTextField.getPassword());
    }

    public String getCvsRoot() {
        return (String) repositoryPanel.rootComboBox.getSelectedItem();
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
    }

    public String getScrambledPassword() {
        if (scrambledPassword == null) {
            String plainPassword = getPassword();
            scrambledPassword = StandardScrambler.getInstance().scramble(plainPassword);
        }
        return scrambledPassword;
    }
}
