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

package org.netbeans.modules.versioning.system.cvss.ui.actions.project;

import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.*;
import org.openide.xml.XMLUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.versioning.system.cvss.settings.HistorySettings;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.ui.wizards.RepositoryStep;
import org.netbeans.modules.versioning.system.cvss.ui.wizards.AbstractStep;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.Kit;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.io.*;
import java.util.*;
import java.text.MessageFormat;

/**
 * Imports project into CVS repository.
 *
 * @author Petr Kuzel
 */
public final class AddToRepositoryAction extends NodeAction implements ChangeListener {


    private WizardDescriptor wizard;

    private WizardDescriptor.Iterator wizardIterator;

    private String errorMessage;

    private RepositoryStep repositoryStep;
    private ImportStep importStep;

    public AddToRepositoryAction() {
        setIcon(null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getMessage(AddToRepositoryAction.class, "BK0006");
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    protected void performAction(Node[] nodes) {
        if (nodes.length == 1) {
            Project project = (Project) nodes[0].getLookup().lookup(Project.class);
            if (project != null) {
                Sources sources = (Sources) project.getLookup().lookup(Sources.class);
                if (sources != null) {
                    SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
                    if (groups.length == 1) {
                        FileObject root = groups[0].getRootFolder();
                        File importDirectory = FileUtil.toFile(root);
                        if (importDirectory != null) {

                            // try to detect some resonable defaults for cvs root and repositoryStep

                            File parent = importDirectory.getParentFile();
                            File parent_cvsRoot = new File(parent, "CVS/Root");  // NOI18N
                            File parent_cvsRepo = new File(parent, "CVS/Repository");  // NOI18N
                            String cvsRoot = null;
                            String cvsRepository = null;
                            if (parent_cvsRepo.isFile() && parent_cvsRoot.isFile()) {
                                BufferedReader r = null;
                                try {
                                    r = new BufferedReader((new FileReader(parent_cvsRoot)));
                                    cvsRoot = r.readLine();
                                } catch (IOException e) {
                                    ErrorManager err = ErrorManager.getDefault();
                                    err.annotate(e, "Cannot read CVS/Root");
                                    err.notify(e);
                                } finally {
                                    if (r != null) {
                                        try {
                                            r.close();
                                        } catch (IOException alreadyClosed) {
                                        }
                                    }
                                }

                                try {
                                    r = new BufferedReader((new FileReader(parent_cvsRepo)));
                                    cvsRepository = r.readLine();
                                } catch (IOException e) {
                                    ErrorManager err = ErrorManager.getDefault();
                                    err.annotate(e, "Cannot read CVS/Repository");
                                    err.notify(e);
                                } finally {
                                    if (r != null) {
                                        try {
                                            r.close();
                                        } catch (IOException alreadyClosed) {
                                        }
                                    }
                                }
                            }

                            String prefRoot;
                            if (cvsRoot != null) {
                                prefRoot = cvsRoot;
                            } else {
                                prefRoot = NbBundle.getMessage(AddToRepositoryAction.class, "BK0008");
                            }

                            String prefModule;
                            if (cvsRepository != null) {
                                prefModule = cvsRepository + "/" + root.getName();
                            } else {
                                prefModule = root.getName();
                            }

                            wizardIterator = panelIterator(prefRoot, prefModule, importDirectory.getAbsolutePath());
                            wizard = new WizardDescriptor(wizardIterator);
                            wizard.putProperty("WizardPanel_contentData",  // NOI18N
                                    new String[] {
                                        "CVS Root",
                                        "Folder to Import"
                                    }
                            );
                            wizard.putProperty("WizardPanel_contentDisplayed", Boolean.TRUE);  // NOI18N
                            wizard.putProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);  // NOi18N
                            wizard.putProperty("WizardPanel_contentNumbered", Boolean.TRUE);  // NOi18N
                            wizard.setTitleFormat(new MessageFormat("{0}"));
                            String title = NbBundle.getMessage(AddToRepositoryAction.class, "BK0007");
                            wizard.setTitle(title);

                            Object result = DialogDisplayer.getDefault().notify(wizard);
                            if (result == DialogDescriptor.OK_OPTION) {

                                boolean checkout = importStep.getCheckout();
                                String logMessage = importStep.getMessage();
                                String module = importStep.getModule();
                                String vendorTag = "default_vendor";
                                String releaseTag = "default_release";
                                String selectedRoot = repositoryStep.getCvsRoot();
                                String folder = importStep.getFolder();
                                File dir = new File(folder);

                                HistorySettings.addRecent(HistorySettings.PROP_CVS_ROOTS, selectedRoot);

                                try {
                                    prepareIgnore(dir);
                                } catch (IOException e) {
                                    ErrorManager err = ErrorManager.getDefault();
                                    err.annotate(e, "Can not generate .cvsignore for unshareable files!");
                                    err.notify(e);
                                }

                                GlobalOptions gtx = new GlobalOptions();
                                gtx.setCVSRoot(selectedRoot);
                                ImportCommand importCommand = new ImportCommand();
                                importCommand.setModule(module);
                                importCommand.setLogMessage(logMessage);
                                importCommand.setVendorTag(vendorTag);
                                importCommand.setReleaseTag(releaseTag);
                                importCommand.setImportDirectory(importDirectory.getPath());

                                ImportExecutor executor = new ImportExecutor(importCommand, gtx, checkout, folder);
                                executor.execute();
                            }

                        }
                    } else {
                        StringBuffer paths = new StringBuffer();
                        paths.append("<dl>"); // NOi18N
                        for (int i = 0; i < groups.length; i++) {
                            SourceGroup group = groups[i];
                            FileObject fo = group.getRootFolder();
                            String name = group.getDisplayName();
                            paths.append("<dt>").append(escape(name)).append("</dt>");  // NOi18N
                            paths.append("<dd>").append(escape(fo.getPath())).append("</dd>");  // NOI18N
                        }
                        paths.append("</dl>"); // NOi18N
                        String msg = NbBundle.getMessage(AddToRepositoryAction.class, "BK0004", paths.toString());
                        NotifyDescriptor desc = new NotifyDescriptor.Message(msg);
                        DialogDisplayer.getDefault().notify(desc);

                        // implementation sketch ...
                        // MetadataAttic.scheduleFolder();
                    }
                }
            }
        }
    }

    private WizardDescriptor.Iterator panelIterator(String root, String module, String folder) {
        repositoryStep = new RepositoryStep();
        repositoryStep.initPreferedCvsRoot(root);
        repositoryStep.addChangeListener(this);
        importStep = new ImportStep(module, folder);
        importStep.addChangeListener(this);

        final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[2];
        panels[0] = repositoryStep;
        panels[1] = importStep;

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

    class ImportStep extends AbstractStep {
        private final String module;
        private final String folder;
        private ImportPanel importPanel;

        public ImportStep(String module, String folder) {
            this.module = module;
            this.folder = folder;
        }

        protected JComponent createComponent() {
            importPanel = new ImportPanel();
            importPanel.moduleTextField.setText(module);
            importPanel.folderTextField.setText(folder);

            // user input validation
            DocumentListener validation = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }
                public void insertUpdate(DocumentEvent e) {
                    String s = checkInput(importPanel);
                    if (s == null) {
                        valid();
                    } else {
                        invalid(s);
                    }
                }
                public void removeUpdate(DocumentEvent e) {
                    String s = checkInput(importPanel);
                    if (s == null) {
                        valid();
                    } else {
                        invalid(s);
                    }
                }
            };
            importPanel.moduleTextField.getDocument().addDocumentListener(validation);
            importPanel.commentTextArea.getDocument().addDocumentListener(validation);
            importPanel.folderTextField.getDocument().addDocumentListener(validation);

            String s = checkInput(importPanel);
            if (s == null) {
                valid();
            } else {
                invalid(s);
            }

            return importPanel;
        }

        protected void validateBeforeNext() {
        }

        public boolean getCheckout() {
            return importPanel.checkoutCheckBox.isSelected();
        }

        public String getMessage() {
            return importPanel.commentTextArea.getText();
        }

        public String getModule() {
            return importPanel.moduleTextField.getText();
        }

        public String getFolder() {
            return importPanel.folderTextField.getText();
        }

    }

    private static String checkInput(ImportPanel importPanel) {
        boolean valid = true;

        File file = new File(importPanel.folderTextField.getText());
        valid &= file.isDirectory();
        if (!valid) return "Folder must exist";

        valid &= importPanel.commentTextArea.getText().trim().length() > 0;
        if (!valid) return "Message required";

        String module = importPanel.moduleTextField.getText().trim();
        valid &= module.length() > 0;
        if (!valid) return "Specify module";
        valid &= module.indexOf(" ") == -1;  // NOI18N
        if (!valid) return "Invalid module name";

        return null;
    }

    private static String escape(String path) {
        try {
            return XMLUtil.toElementContent(path);
        } catch (CharConversionException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Can not HTML escape '" + path + "'");  // NOI18N
            err.notify(e);
        }
        return NbBundle.getMessage(AddToRepositoryAction.class, "BK0005");
    }

    private void prepareIgnore(File dir) throws IOException {
        File[] projectMeta = dir.listFiles();
        Set ignored = new HashSet();
        for (int i = 0; i < projectMeta.length; i++) {
            File file = projectMeta[i];
            String name = file.getName();
            int sharability = SharabilityQuery.getSharability(file);
            if (sharability == SharabilityQuery.NOT_SHARABLE) {
                if (".cvsignore".equals(name) == false) {  // NOI18N
                    ignored.add(name);
                }
            } else if (sharability == SharabilityQuery.MIXED) {
                assert file.isDirectory() : file;
                prepareIgnore(file);
            }
        }

        if (ignored.size() > 0) {
            File cvsIgnore = new File(dir, ".cvsignore"); // NOI18N
            OutputStream out = null;
            try {
                out = new FileOutputStream(cvsIgnore);
                PrintWriter pw = new PrintWriter(out);
                Iterator it = ignored.iterator();
                while (it.hasNext()) {
                    String name = (String) it.next();
                    pw.println(name);
                }
                pw.close();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException alreadyClosed) {
                    }
                }
            }
        }

    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] nodes) {
        if (nodes.length > 0) {
            for (int i = 0; i < nodes.length; i++) {
                Node node = nodes[i];
                if (node.getLookup().lookup(Project.class) == null) return false;
                if (Utils.isVersionedProject(node)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
