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

import org.openide.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ModuleSelector;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;
import org.netbeans.lib.cvsclient.CVSRoot;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.io.File;
import java.text.MessageFormat;

import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.spi.project.ui.support.ProjectChooser;

/**
 * Checkout wizard controller with input validation.
 *
 * @author Petr Kuzel
 */
public final class CheckoutWizard implements ChangeListener{

    private static final String RECENT_DIRECTORY = "checkout.recentDirectory";
    private static final String CHECKOUT_RECENT_MODULE = "checkout.recentModule";
    private static final String CHECKOUT_RECENT_TAG = "checkout.recentTag";

    private WizardDescriptor wizard;

    private String errorMessage;

    private WizardDescriptor.Iterator wizardIterator;

    private ModulePanel modulePanel;

    private RepositoryStep repositoryStep;

    // output data

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
        wizard.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);  // NOI18N
        wizard.putProperty("WizardPanel_contentNumbered", Boolean.TRUE);  // NOI18N
        wizard.setTitleFormat(new MessageFormat("{0}"));  // NOI18N
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
        String checkout = modulePanel.workTextField.getText();
        CvsModuleConfig.getDefault().getPreferences().put(CHECKOUT_RECENT_MODULE, modulePanel.moduleTextField.getText());
        CvsModuleConfig.getDefault().getPreferences().put(CHECKOUT_RECENT_TAG, modulePanel.tagTextField.getText());
        Utils.insert(CvsModuleConfig.getDefault().getPreferences(), RECENT_DIRECTORY, checkout, 8);
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

    public void stateChanged(ChangeEvent e) {
        AbstractStep step = (AbstractStep) wizardIterator.current();
        setErrorMessage(step.getErrorMessage());
    }

    private WizardDescriptor.Iterator panelIterator() {
        repositoryStep = new RepositoryStep(initialCvsRoot, RepositoryStep.CHECKOUT_HELP_ID);
        repositoryStep.addChangeListener(this);
        WizardDescriptor.Panel modulePanel = new ModuleStep();
        modulePanel.addChangeListener(this);

        final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[2];
        panels[0] = repositoryStep;
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

    /**
     * @return normalized file path
     */
    public String getWorkingDir() {
        String path = modulePanel.workTextField.getText();
        return FileUtil.normalizeFile(new File(path)).getAbsolutePath();
    }

    /** Password scrambled by standard scramler. */
    public String getScrambledPassword() {
        return repositoryStep.getScrambledPassword();
    }

    public String getCvsRoot() {
        return repositoryStep.getCvsRoot();
    }

    private class ModuleStep extends AbstractStep implements DocumentListener, FocusListener, ActionListener {

        protected JComponent createComponent() {
            modulePanel = new ModulePanel();

            if (initialModule != null) {
                modulePanel.moduleTextField.setText(initialModule);
            }

            modulePanel.moduleTextField.setText(CvsModuleConfig.getDefault().getPreferences().get(CHECKOUT_RECENT_MODULE, ""));
            modulePanel.tagTextField.setText(CvsModuleConfig.getDefault().getPreferences().get(CHECKOUT_RECENT_TAG, ""));

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

        public HelpCtx getHelp() {
            return new HelpCtx(ModuleStep.class);
        }

        protected void validateBeforeNext() {
            if (validateUserInput(true)) {
                String text = modulePanel.workTextField.getText();
                File file = new File(text);
                if (file.exists() == false) {
                    boolean done = file.mkdirs();
                    if (done == false) {
                        invalid(org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "BK2013") + file.getPath());
                    }
                }
            }
        }

        private boolean validateUserInput(boolean full) {
            String text = modulePanel.workTextField.getText();
            if (text == null || text.length() == 0) {
                invalid(org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "BK2014"));
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
                                errorMessage = org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "BK2016") + parent.getPath();
                            }
                            break;
                        }

                        parent = parent.getParentFile();
                    }
                } else {
                    if (file.isFile()) {
                        errorMessage = org.openide.util.NbBundle.getMessage(CheckoutWizard.class, "BK2017");
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
                String rootString = repositoryStep.getCvsRoot();
                CVSRoot root = CVSRoot.parse(rootString);
                Set modules = selector.selectModules(root);
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
                String rootString = repositoryStep.getCvsRoot();
                CVSRoot root = CVSRoot.parse(rootString);
                String s = modulePanel.moduleTextField.getText();
                if (s.trim().length() == 0) {
                    s = ".";  // NOI18N
                }
                String module = new StringTokenizer(s, ", ").nextToken(); // NOI18N
                String tag = selector.selectTag(root, module);
                if (tag != null) {
                    modulePanel.tagTextField.setText(tag);
                }
            } else if (e.getSource() == modulePanel.workButton) {

                File defaultDir = defaultWorkingDirectory();
                JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(CheckoutWizard.class, "ACSD_BrowseFolder"), defaultDir);
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
                List recent = Utils.getStringList(CvsModuleConfig.getDefault().getPreferences(), RECENT_DIRECTORY);
                Iterator it = recent.iterator();

                while (it.hasNext()) {
                    String path = (String) it.next();
                    File file = new File(path);
                    File parent = file.getParentFile();
                    if (parent != null && parent.exists() && parent.isDirectory()) {
                        defaultDir = file;
                        break;
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
                defaultDir = new File(System.getProperty("user.home"));  // NOI18N
            }

            return defaultDir;
        }
    }

}
