/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.versioning.system.cvss.ui.actions.project;

import org.openide.util.actions.NodeAction;
import org.openide.util.*;
import org.openide.nodes.Node;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.netbeans.api.project.*;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.wizards.RepositoryStep;
import org.netbeans.modules.versioning.system.cvss.ui.wizards.AbstractStep;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.ModuleSelector;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.ExecutorGroup;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.io.*;
import java.util.*;
import java.text.MessageFormat;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Imports folder into CVS repository. It's enabled on Nodes that represent:
 * <ul>
 * <li>project root directory, parent of all necessary
 * project data and metadata.
 * <li>folders that are not a part on any project
 * </ul>
 * It's minimalitics attempt to assure
 * that the project can be reopend after checkout.
 * It also simplifies implemenattion avoiding huge
 * import mapping wizard for projects with external
 * data folders.
 *
 * <p>Before actual CVS <tt>import</tt> it recursively scans
 * imported context and prepares <tt>.cvsignore</tt> files.
 * After <tt>import</tt> it optionally turns imported context
 * into versioned using <tt>checkout</tt> and copying respective
 * CVS metadata.
 *
 * @author Petr Kuzel
 */
public final class AddToRepositoryAction extends NodeAction implements ChangeListener {

    private static final String RECENT_ROOTS = "addToRepositoryAction.recentRoots";

    private WizardDescriptor wizard;

    private WizardDescriptor.Iterator wizardIterator;

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

    protected boolean enable(Node[] nodes) {        
        if (nodes.length == 1) {
            FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
            File dir = lookupImportDirectory(nodes[0]);
            if (dir != null && dir.isDirectory()) {
                FileInformation status = cache.getCachedStatus(dir);
                // mutually exclusive enablement logic with commit
                if (!CvsVersioningSystem.isManaged(dir) && (status == null || (status.getStatus() & FileInformation.STATUS_MANAGED) == 0)) {
                    // do not allow to import partial/nonatomic project, all must lie under imported common root
                    FileObject fo = FileUtil.toFileObject(dir);
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p == null) {
                        return true;
                    }
                    FileObject projectDir = p.getProjectDirectory();
                    return FileUtil.isParentOf(projectDir, fo) == false;
                }
            }
        }
        return false;
    }

    protected void performAction(Node[] nodes) {
        Utils.logVCSActionEvent("CVS");        
        if (nodes.length == 1) {
            final File importDirectory = lookupImportDirectory(nodes[0]);
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
                        err.annotate(e, NbBundle.getMessage(AddToRepositoryAction.class, "BK0016"));
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
                        err.annotate(e, NbBundle.getMessage(AddToRepositoryAction.class, "BK0017"));
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
                    prefModule = cvsRepository + "/" + importDirectory.getName();  // NOI18N
                } else {
                    prefModule = importDirectory.getName();
                }

                wizardIterator = panelIterator(prefRoot, prefModule, importDirectory.getAbsolutePath());
                wizard = new WizardDescriptor(wizardIterator);
                wizard.putProperty(WizardDescriptor.PROP_CONTENT_DATA,  // NOI18N
                        new String[] {
                            NbBundle.getMessage(AddToRepositoryAction.class, "BK0015"),
                            NbBundle.getMessage(AddToRepositoryAction.class, "BK0014")
                        }
                );
                wizard.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);  // NOI18N
                wizard.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);  // NOI18N
                wizard.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);  // NOI18N
                wizard.setTitleFormat(new MessageFormat("{0}"));  // NOI18N
                String title = NbBundle.getMessage(AddToRepositoryAction.class, "BK0007");
                wizard.setTitle(title);

                Object result = DialogDisplayer.getDefault().notify(wizard);
                if (result == DialogDescriptor.OK_OPTION) {
                    CvsVersioningSystem.getInstance().getParallelRequestProcessor().post(new Runnable() {
                        public void run() {
                            async(importDirectory);
                        }
                    });
                }
            }
        }
    }

    private void async(File importDirectory) {
        boolean checkout = importStep.getCheckout();
        String logMessage = importStep.getMessage();
        String module = importStep.getModule();
        String vendorTag = "default_vendor"; // NOI18N
        String releaseTag = "default_release"; // NOI18N
        String selectedRoot = repositoryStep.getCvsRoot();
        String folder = importStep.getFolder();
        File dir = new File(folder);

        org.netbeans.modules.versioning.util.Utils.insert(CvsModuleConfig.getDefault().getPreferences(), RECENT_ROOTS, selectedRoot, 8);

        ExecutorGroup group = new ExecutorGroup(NbBundle.getMessage(AddToRepositoryAction.class, "BK0019"));
        try {
            group.progress(NbBundle.getMessage(AddToRepositoryAction.class, "BK0020"));
            group.addCancellable(new Cancellable() {
                public boolean cancel() {
                    return true;
                }
            });
            prepareIgnore(dir);
        } catch (IOException e) {
            group.executed();
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, NbBundle.getMessage(AddToRepositoryAction.class, "BK0021"));
            err.notify(e);
            return;
        }

        GlobalOptions gtx = CvsVersioningSystem.createGlobalOptions();
        gtx.setCVSRoot(selectedRoot);
        ImportCommand importCommand = new ImportCommand();
        importCommand.setModule(module);
        importCommand.setLogMessage(logMessage);
        importCommand.setVendorTag(vendorTag);
        importCommand.setReleaseTag(releaseTag);
        importCommand.setImportDirectory(importDirectory.getPath());

        new ImportExecutor(importCommand, gtx, checkout, folder, group); // joins the group
        group.execute();
    }

    public boolean cancel() {

        return true;
    }

    private File lookupImportDirectory(Node node) {
        File importDirectory = null;
        Project project = (Project) node.getLookup().lookup(Project.class);
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            if (groups.length == 1) {
                FileObject root = groups[0].getRootFolder();
                importDirectory = FileUtil.toFile(root);
            } else {
                importDirectory = FileUtil.toFile(project.getProjectDirectory());
            }
        } else {
            FileObject fo = null;
            Collection fileObjects = node.getLookup().lookup(new Lookup.Template(FileObject.class)).allInstances();
            if (fileObjects.size() > 0) {
                fo = (FileObject) fileObjects.iterator().next();
            } else {
                DataObject dataObject = (DataObject) node.getCookie(DataObject.class);
                if (dataObject instanceof DataShadow) {
                    dataObject = ((DataShadow) dataObject).getOriginal();
                }
                if (dataObject != null) {
                    fo = dataObject.getPrimaryFile();
                }
            }

            if (fo != null) {
                File f = FileUtil.toFile(fo);
                if (f != null && f.isDirectory()) {
                    importDirectory = f;
                }
            }
        }
        return importDirectory;
    }

    private WizardDescriptor.Iterator panelIterator(String root, String module, String folder) {
        repositoryStep = new RepositoryStep(RepositoryStep.IMPORT_HELP_ID);
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
                        wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));  // NOI18N
                    }
                }
                return ret;
            }
        };
        return ret;
    }

    private void setErrorMessage(String msg) {
        if (wizard != null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); // NOI18N
        }
    }

    public void stateChanged(ChangeEvent e) {
        AbstractStep step = (AbstractStep) wizardIterator.current();
        setErrorMessage(step.getErrorMessage());
    }

    class ImportStep extends AbstractStep implements ActionListener {
        private final String module;
        private final String folder;
        private ImportPanel importPanel;

        public ImportStep(String module, String folder) {
            this.module = module;
            this.folder = folder;
        }

        public HelpCtx getHelp() {
            return new HelpCtx(ImportStep.class);
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
            importPanel.folderButton.addActionListener(this);
            importPanel.moduleButton.addActionListener(this);

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

        /**
         * Returns file to be initaly used.
         * <ul>
         * <li>first is takes text in workTextField
         * <li>then recent project folder
         * <li>finally <tt>user.home</tt>
         * <ul>
         */
        private File defaultWorkingDirectory() {
            File defaultDir = null;
            String current = importPanel.folderTextField.getText();
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
                defaultDir = new File(System.getProperty("user.home"));  // NOI18N
            }

            return defaultDir;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == importPanel.folderButton) {
                File defaultDir = defaultWorkingDirectory();
                JFileChooser fileChooser = new JFileChooser(defaultDir);
                fileChooser.setDialogTitle(NbBundle.getMessage(AddToRepositoryAction.class, "BK1017"));
                fileChooser.setMultiSelectionEnabled(false);
                javax.swing.filechooser.FileFilter[] old = fileChooser.getChoosableFileFilters();
                for (int i = 0; i < old.length; i++) {
                    javax.swing.filechooser.FileFilter fileFilter = old[i];
                    fileChooser.removeChoosableFileFilter(fileFilter);

                }
                fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                    public String getDescription() {
                        return NbBundle.getMessage(AddToRepositoryAction.class, "BK1018");
                    }
                });
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showDialog(importPanel, NbBundle.getMessage(AddToRepositoryAction.class, "BK1019"));
                File f = fileChooser.getSelectedFile();
                if (f != null) {
                    importPanel.folderTextField.setText(f.getAbsolutePath());
                }
            } else if (e.getSource() == importPanel.moduleButton) {
                ModuleSelector selector = new ModuleSelector();
                CVSRoot root = CVSRoot.parse(repositoryStep.getCvsRoot());
                String path = selector.selectRepositoryPath(root);
                if (path != null) {
                    if (!path.endsWith(module)) {
                        path += "/" + module;
                    }
                    importPanel.moduleTextField.setText(path);
                }
            }
        }
    }

    private static String checkInput(ImportPanel importPanel) {
        boolean valid = true;

        File file = new File(importPanel.folderTextField.getText());
        valid &= file.isDirectory();
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0022");

        valid &= (new File(file, "CVS").exists()) == false; // NOI18N
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0023");

        valid &= importPanel.commentTextArea.getText().trim().length() > 0;
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0024");

        String module = importPanel.moduleTextField.getText().trim();
        valid &= module.length() > 0;
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0025");
        valid &= module.indexOf(" ") == -1;  // NOI18N // NOI18N
        valid &= ".".equals(module.trim()) == false;  // NOI18N
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0026");

        return null;
    }

    /**
     * @return false on Thread.interrupted i.e. user cancel.
     */
    private boolean prepareIgnore(File dir) throws IOException {
        File[] projectMeta = dir.listFiles();
        if (projectMeta == null) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "{0} dir is not a directory", dir); //NOI18N
            return false;
        }
        Set ignored = new HashSet();
        for (int i = 0; i < projectMeta.length; i++) {
            if (Thread.interrupted()) {
                return false;
            }
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
        return true;
    }

    protected boolean asynchronous() {
        return false;
    }

}
