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

import org.openide.*;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.system.cvss.settings.HistorySettings;
import org.netbeans.modules.versioning.system.cvss.settings.CvsRootSettings;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ModuleSelector;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;
import org.netbeans.lib.cvsclient.connection.*;
import org.netbeans.lib.cvsclient.CVSRoot;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.net.SocketFactory;
import java.util.*;
import java.util.List;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.lang.reflect.InvocationTargetException;

import org.netbeans.modules.versioning.system.cvss.ui.selectors.ProxySelector;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ProxyDescriptor;
import org.netbeans.modules.versioning.system.cvss.ClientRuntime;
import org.netbeans.modules.versioning.system.cvss.SSHConnection;
import org.netbeans.modules.proxy.ClientSocketFactory;
import org.netbeans.modules.proxy.ConnectivitySettings;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

/**
 * Checkout wizard controller with input validation.
 *
 * @author Petr Kuzel
 */
public final class CheckoutWizard {

    private WizardDescriptor wizard;

    private String errorMessage;

    private WizardDescriptor.Iterator wizardIterator;

    private ModulePanel modulePanel;

    private RepositoryPanel repositoryPanel;

    // output data

    private String scrambledPassword;

    private ProxyDescriptor proxyDescriptor;

    private String initialCvsRoot;

    private String initialModule;

    /** Creates a new instance of CheckoutWizard */
    public CheckoutWizard() {
    }

    public CheckoutWizard(String cvsRoot, String module) {
        initialCvsRoot = cvsRoot;
        initialModule = module;
    }

    public boolean show() {
        wizardIterator = panelIterator();
        wizard = new WizardDescriptor(wizardIterator);
        wizard.putProperty("WizardPanel_contentData",  // NOI18N
                new String[] {
                    NbBundle.getMessage(CheckoutWizard.class, "BK0006"),
                    NbBundle.getMessage(CheckoutWizard.class, "BK2009")
                }
        );
        wizard.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE);  // NOI18N
        wizard.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);  // NOi18N
        wizard.putProperty("WizardPanel_contentNumbered", Boolean.TRUE);  // NOi18N
        wizard.setTitleFormat(new MessageFormat("{0}"));
        wizard.setTitle(NbBundle.getMessage(CheckoutWizard.class, "BK0007"));
        Object result = DialogDisplayer.getDefault().notify(wizard);
        boolean finished = NotifyDescriptor.OK_OPTION.equals(result);
        if (finished) {
            onFinished();
        }
        return finished;
    }

    /** Called on sucessfull finish. */
    private void onFinished() {
        String checkout = (String) modulePanel.workTextField.getText();
        HistorySettings.addRecent(HistorySettings.PROP_CHECKOUT_DIRECTORY, checkout);
    }

    /** Tells invalidation reason never <code>null</code>, */
    String getErrorMessage() {
        String value;
        if (wizard != null) {
            value = (String) wizard.getProperty("WizardPanel_errorMessage");  // NOI18N
        } else {
            value = errorMessage;
        }
        if (value == null) value = ""; // NOI18N
        return value;
    }

    private void setErrorMessage(String msg) {
        errorMessage = msg;
        if (wizard != null) {
            wizard.putProperty("WizardPanel_errorMessage", msg); // NOI18N
        }
    }

    private WizardDescriptor.Iterator panelIterator() {
        WizardDescriptor.Panel repositoryPanel = new RepositoryStep();
        WizardDescriptor.Panel modulePanel = new ModuleStep();

        final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[2];
        panels[0] = repositoryPanel;
        panels[1] = modulePanel;

        WizardDescriptor.ArrayIterator ret = new WizardDescriptor.ArrayIterator(panels) {
            public WizardDescriptor.Panel current() {
                WizardDescriptor.Panel ret = super.current();
                for (int i = 0; i<panels.length; i++) {
                    if (panels[i] == ret) {
                        wizard.putProperty("WizardPanel_contentSelectedIndex", new Integer(i));  // NOI18N
                    }
                }
                return ret;
            }
        };
        return ret;
    }

    public String getModules() {
        return modulePanel.moduleTextField.getText().trim();
    }

    public String getTag() {
        return modulePanel.tagTextField.getText().trim();
    }

    public String getWorkingDir() {
        return modulePanel.workTextField.getText();
    }

    /** Password scrambled by standard scramler. */
    public String getScrambledPassword() {
        if (scrambledPassword == null) {
            String plainPassword = new String(repositoryPanel.passwordTextField.getPassword());
            scrambledPassword = StandardScrambler.getInstance().scramble(plainPassword);
        }
        return scrambledPassword;
    }

    public String getCvsRoot() {
        return (String) repositoryPanel.rootComboBox.getSelectedItem();
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
    }

    private class ModuleStep extends AbstractStep implements DocumentListener, FocusListener, ActionListener {

        protected JComponent createComponent() {
            modulePanel = new ModulePanel();

            if (initialModule != null) {
                modulePanel.moduleTextField.setText(initialModule);
            }

            String path = defaultWorkingDirectory().getPath();
            modulePanel.workTextField.setText(path);
            modulePanel.workTextField.getDocument().addDocumentListener(this);
            modulePanel.workTextField.addFocusListener(this);
            modulePanel.workTextField.addActionListener(this);
            validateUserInput(true);

            modulePanel.moduleButton.addActionListener(this);
            modulePanel.tagButton.addActionListener(this);
            modulePanel.workButton.addActionListener(this);
            return modulePanel;
        }

        protected void validateBeforeNext() {
            if (validateUserInput(true)) {
                String text = modulePanel.workTextField.getText();
                File file = new File(text);
                if (file.exists() == false) {
                    boolean done = file.mkdirs();
                    if (done == false) {
                        invalid("Can not create folder " + file.getPath());
                    }
                }
            }
        }

        private boolean validateUserInput(boolean full) {
            String text = modulePanel.workTextField.getText();
            if (text == null || text.length() == 0) {
                invalid("Local working directory must be specified.");
                return false;
            }

            String errorMessage = null;
            if (full) {
                File file = new File(text);
                if (file.exists() == false) {
                    // it's automaticaly create later on, check for permisions here
                    File parent = file.getParentFile();
                    while (parent != null) {
                        if (parent.exists()) {
                            if (parent.canWrite() == false) {
                                errorMessage = "Can not write into " + parent.getPath();
                            }
                            break;
                        }

                        parent = parent.getParentFile();
                    }
                } else {
                    if (file.isFile()) {
                        errorMessage = "Path points to file but directory is expected";
                    }
                }
            }

            if (errorMessage == null) {
                valid();
            } else {
                invalid(errorMessage);
            }

            return errorMessage == null;
        }

        public void changedUpdate(DocumentEvent e) {
        }

        public void insertUpdate(DocumentEvent e) {
            validateUserInput(false);
        }

        public void removeUpdate(DocumentEvent e) {
            validateUserInput(false);
        }

        public void focusGained(FocusEvent e) {
        }

        public void focusLost(FocusEvent e) {
            validateUserInput(true);
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == modulePanel.moduleButton) {
                ModuleSelector selector = new ModuleSelector();
                String rootString = (String) repositoryPanel.rootComboBox.getSelectedItem();
                CVSRoot root = CVSRoot.parse(rootString);
                Set modules = selector.selectModules(root, proxyDescriptor);
                StringBuffer buf = new StringBuffer();
                String separator = "";  // NOI18N
                Iterator it = modules.iterator();
                while (it.hasNext()) {
                    String module = (String) it.next();
                    buf.append(separator).append(module);
                    separator = ",";   // NOI18N
                }
                modulePanel.moduleTextField.setText(buf.toString());
            } else if (e.getSource() == modulePanel.tagButton) {
                BranchSelector selector = new BranchSelector();
                String rootString = (String) repositoryPanel.rootComboBox.getSelectedItem();
                CVSRoot root = CVSRoot.parse(rootString);
                String s = modulePanel.moduleTextField.getText();
                if (s.trim().length() == 0) {
                    s = ".";  // NOI18N
                }
                String module = new StringTokenizer(s, ", ").nextToken();
                String tag = selector.selectTag(root, module, proxyDescriptor);
                if (tag != null) {
                    modulePanel.tagTextField.setText(tag);
                }
            } else if (e.getSource() == modulePanel.workButton) {

                File defaultDir = defaultWorkingDirectory();
                JFileChooser fileChooser = new JFileChooser(defaultDir);
                fileChooser.setDialogTitle(NbBundle.getMessage(CheckoutWizard.class, "BK0010"));
                fileChooser.setMultiSelectionEnabled(false);
                FileFilter[] old = fileChooser.getChoosableFileFilters();
                for (int i = 0; i < old.length; i++) {
                    FileFilter fileFilter = old[i];
                    fileChooser.removeChoosableFileFilter(fileFilter);

                }
                fileChooser.addChoosableFileFilter(new FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                    public String getDescription() {
                        return NbBundle.getMessage(CheckoutWizard.class, "BK0008");
                    }
                });
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showDialog(modulePanel, NbBundle.getMessage(CheckoutWizard.class, "BK0009"));
                File f = fileChooser.getSelectedFile();
                if (f != null) {
                    modulePanel.workTextField.setText(f.getAbsolutePath());
                }
            } else {
                validateUserInput(true);
            }
        }

        /**
         * Returns file to be initaly used.
         * <ul>
         * <li>first is takes text in workTextField
         * <li>then recent project folder
         * <li>then recent checkout folder
         * <li>finally <tt>user.home</tt>
         * <ul>
         */
        private File defaultWorkingDirectory() {
            File defaultDir = null;
            String current = modulePanel.workTextField.getText();
            if (current != null && !(current.trim().equals(""))) {  // NOI18N
                File currentFile = new File(current);
                while (currentFile != null && currentFile.exists() == false) {
                    currentFile = currentFile.getParentFile();
                }
                if (currentFile != null) {
                    if (currentFile.isFile()) {
                        defaultDir = currentFile.getParentFile();
                    } else {
                        defaultDir = currentFile;
                    }
                }
            }

            if (defaultDir == null) {
                File projectFolder = ProjectChooser.getProjectsFolder();
                if (projectFolder.exists() && projectFolder.isDirectory()) {
                    defaultDir = projectFolder;
                }
            }

            if (defaultDir == null) {
                List recent = HistorySettings.getRecent(HistorySettings.PROP_CHECKOUT_DIRECTORY);
                Iterator it = recent.iterator();

                while (it.hasNext()) {
                    String path = (String) it.next();
                    File file = new File(path);
                    File parent = file.getParentFile();
                    if (parent != null && parent.exists() && parent.isDirectory()) {
                        defaultDir = parent;
                        break;
                    }
                }
            }

            if (defaultDir == null) {
                defaultDir = new File(System.getProperty("user.home"));  // NOI18N
            }

            return defaultDir;
        }
    }

    /**
     * Setups CvsRootSettings for some root. All data
     * are taken directly from UI. On the step
     * completion are all data available in CvsRootSettings.
     */
    class RepositoryStep extends AbstractStep implements WizardDescriptor.AsynchronousValidatingPanel, ActionListener, DocumentListener {

        private RequestProcessor.Task updatePasswordTask;
        private volatile boolean passwordExpected;

        private boolean userVisitedProxySettings;

        private ProgressHandle progress;
        private JComponent progressComponent;
        private volatile boolean internalDocumentChange;
        private Thread backgroundValidationThread;

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
                if (wizardIterator != null && wizardIterator.current() == RepositoryStep.this) {
                    String msg = NbBundle.getMessage(CheckoutWizard.class, "BK1001", host);
                    invalid(msg);
                }
            } catch (AuthenticationException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.annotate(e, "Connection authentification verification failed.");  // NOI18N
                err.notify(ErrorManager.INFORMATIONAL, e);
                if (wizardIterator != null && wizardIterator.current() == RepositoryStep.this) {
                    String msg = NbBundle.getMessage(CheckoutWizard.class, "BK1002");
                    invalid(msg);
                }
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
    }

    private abstract class AbstractStep implements WizardDescriptor.ValidatingPanel {

        private List listeners = new LinkedList();
        private boolean valid;
        private JComponent panel;
        private volatile boolean underConstruction;

        public synchronized Component getComponent() {
            if (panel == null) {
                try {
                    underConstruction = true;
                    panel = createComponent();

                    if (wizard != null) {
                        // 3:2 size for all wizard panels (without resizing aftre next>)
                        JTextArea template = new JTextArea();
                        template.setColumns(60);
                        template.setRows(25);
                        panel.setPreferredSize(template.getPreferredSize());
                    }
                } finally {
                    underConstruction = false;
                    fireChange();
                }
            }
            return panel;
        }

        protected abstract JComponent createComponent();

        public HelpCtx getHelp() {
            return null;
        }

        public void readSettings(Object settings) {
        }

        public void storeSettings(Object settings) {
        }

        protected final void valid() {
            setValid(true, null);
        }

        protected final void invalid(String message) {
            setValid(false, message);
        }

        private void setValid(boolean valid, String errorMessage) {
            boolean fire = AbstractStep.this.valid != valid;
            AbstractStep.this.valid = valid;
            if (valid) {
                setErrorMessage(null);
            } else {
                setErrorMessage(errorMessage);
            }
            if (fire) {
                fireChange();
            }
        }

        private void fireChange() {
            if (underConstruction) return;
            List clone;
            synchronized(listeners) {
                clone = new ArrayList(listeners);
            }
            Iterator it = clone.iterator();
            ChangeEvent event = new ChangeEvent(this);
            while (it.hasNext()) {
                ChangeListener listener = (ChangeListener) it.next();
                listener.stateChanged(event);
            }
        }

        // come son next or finish
        public void validate () throws WizardValidationException {
            validateBeforeNext();
            if (isValid() == false) {
                throw new WizardValidationException (
                    panel,
                    (String) wizard.getProperty("WizardPanel_errorMessage"),  // NOI18N
                    (String) wizard.getProperty("WizardPanel_errorMessage")   // NOI18N
                );
            }
        }

        protected abstract void validateBeforeNext();

        public boolean isValid() {
            return valid;
        }


        public void addChangeListener(ChangeListener l) {
            synchronized(listeners) {
                listeners.add(l);
            }
        }

        public void removeChangeListener(ChangeListener l) {
            synchronized(listeners) {
                listeners.remove(l);
            }
        }
    }
}
