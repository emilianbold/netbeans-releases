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

package org.netbeans.modules.versioning.system.cvss.ui.actions.checkout;

import org.netbeans.modules.versioning.system.cvss.ui.wizards.CheckoutWizard;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.settings.HistorySettings;
import org.netbeans.modules.versioning.system.cvss.executor.CheckoutExecutor;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.ErrorManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Shows checkout wizard, performs the checkout,
 * detects checked out projects and optionally
 * shows open/create project dialog.
 * 
 * @author Petr Kuzel
 */
public final class CheckoutAction extends SystemAction {

    // avoid instance fields, it's singleton
    
    public CheckoutAction() {
        setIcon(null);        
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public String getName() {
        return NbBundle.getBundle(CheckoutAction.class).getString("CTL_MenuItem_Checkout_Label");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CheckoutAction.class);
    }

    /**
     * Shows interactive chekcout wizard.
     */
    public void actionPerformed(ActionEvent ev) {
        CheckoutWizard wizard = new CheckoutWizard();
        if (wizard.show() == false) return;

        String tag = wizard.getTag();
        String modules = wizard.getModules();
        String workDir = wizard.getWorkingDir();
        String cvsRoot = wizard.getCvsRoot();
        checkout(cvsRoot, modules, tag, workDir, true);
    }


    /**
     * Perform asynchronous checkout action with preconfigured values.
     * On succesfull finish shows open project dialog.
     *
     * @param modules comma separated list of modules
     * @param tag branch name of <code>null</code> for trunk
     * @param workingDir target directory
     * @param scanProject if true scan folder for projects and show UI (subject of global setting)
     *
     * @return async executor
     */
    public CheckoutExecutor checkout(String cvsRoot, String modules, String tag, String workingDir, boolean scanProject) {
        CheckoutCommand cmd = new CheckoutCommand();

        String moduleString = modules;
        if (moduleString == null || moduleString.length() == 0) {
            moduleString = ".";  // NOI18N
        }
        StringTokenizer tokenizer = new StringTokenizer(moduleString, ",;");  // NOi18N
        if (tokenizer.countTokens() == 1) {
            cmd.setModule(moduleString);
        } else {
            List moduleList = new ArrayList();
            while( tokenizer.hasMoreTokens()) {
                String s = tokenizer.nextToken().trim();
                moduleList.add(s);
            }
            String[] modules2 = (String[]) moduleList.toArray(new String[moduleList.size()]);
            cmd.setModules(modules2);
        }

        cmd.setDisplayName(NbBundle.getMessage(CheckoutAction.class, "BK1006"));

        if (tag != null) {
            cmd.setCheckoutByRevision(tag);
        } else {
            cmd.setResetStickyOnes(true);
        }
        cmd.setPruneDirectories(true);
        cmd.setRecursive(true);

        File workingFolder = new File(workingDir);
        File[] files = new File[] {workingFolder};
        cmd.setFiles(files);

        CvsVersioningSystem cvs = CvsVersioningSystem.getInstance();
        GlobalOptions gtx = new GlobalOptions();
        gtx.setCVSRoot(cvsRoot);
        CheckoutExecutor executor = new CheckoutExecutor(cvs, cmd, gtx);

        executor.execute();
        if (HistorySettings.getFlag(HistorySettings.PROP_SHOW_CHECKOUT_COMPLETED, -1) != 0 && scanProject) {
            executor.addTaskListener(new CheckoutCompletedController(executor, workingFolder, scanProject));
        }

        return executor;
    }

    /** On task finish shows next steps UI.*/
    private class CheckoutCompletedController implements TaskListener, ActionListener {

        private final CheckoutExecutor executor;
        private final File workingFolder;
        private final boolean openProject;

        private CheckoutCompletedPanel panel;
        private Dialog dialog;
        private Project projectToBeOpened;

        public CheckoutCompletedController(CheckoutExecutor executor, File workingFolder, boolean openProject) {
            this.executor = executor;
            this.workingFolder = workingFolder;
            this.openProject = openProject;
        }

        public void taskFinished(Task task) {

            // XXX does it catch user errors such as incorrect module name spec?
            Throwable t = executor.getFailure();
            executor.removeTaskListener(this);
            if (t != null) {
                ErrorManager.getDefault().notify(ErrorManager.USER, t); // show dialog without stack trace
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);  // log to console, too
                return;
            }

            List checkedOutProjects = Collections.EMPTY_LIST;
            FileObject fo = FileUtil.toFileObject(workingFolder);
            if (fo != null) {
                String name = NbBundle.getMessage(CheckoutAction.class, "BK1007");
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle(name);
                try {
                    progressHandle.start();
                    checkedOutProjects = ProjectUtilities.scanForProjects(fo);
                } finally {
                    progressHandle.finish();
                }
            }

            panel = new CheckoutCompletedPanel();
            panel.openButton.addActionListener(this);
            panel.createButton.addActionListener(this);
            panel.closeButton.addActionListener(this);
            panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
            panel.againCheckBox.setVisible(openProject == false);
            String title = NbBundle.getMessage(CheckoutAction.class, "BK1008");
            DialogDescriptor descriptor = new DialogDescriptor(panel, title);
            descriptor.setModal(true);

            // move buttons from dialog to descriptor
            panel.remove(panel.openButton);
            panel.remove(panel.createButton);
            panel.remove(panel.closeButton);

            Object[] options = null;
            if (checkedOutProjects.size() > 1) {
                String msg = NbBundle.getMessage(CheckoutAction.class, "BK1009", new Integer(checkedOutProjects.size()));
                panel.jLabel1.setText(msg);
                options = new Object[] {
                    panel.openButton,
                    panel.closeButton
                };
            } else if (checkedOutProjects.size() == 1) {
                Project project = (Project) checkedOutProjects.iterator().next();
                projectToBeOpened = project;
                ProjectInformation projectInformation = (ProjectInformation) project.getLookup().lookup(ProjectInformation.class);
                String projectName;
                if (projectInformation != null) {
                    projectName = projectInformation.getDisplayName();
                } else {
                    projectName = NbBundle.getMessage(CheckoutAction.class, "BK1013");
                }
                String msg = NbBundle.getMessage(CheckoutAction.class, "BK1011", projectName);
                panel.jLabel1.setText(msg);
                panel.openButton.setText(NbBundle.getMessage(CheckoutAction.class, "BK1012"));
                options = new Object[] {
                    panel.openButton,
                    panel.closeButton
                };
            } else {
                String msg = NbBundle.getMessage(CheckoutAction.class, "BK1010");
                panel.jLabel1.setText(msg);
                options = new Object[] {
                    panel.createButton,
                    panel.closeButton
                };

            }

            descriptor.setMessageType(DialogDescriptor.INFORMATION_MESSAGE);
            descriptor.setOptions(options);
            descriptor.setClosingOptions(options);

            dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setVisible(true);
        }

        public void actionPerformed(ActionEvent e) {
            Object src = e.getSource();
            dialog.setVisible(false);
            if (panel.openButton.equals(src)) {
                Project p = projectToBeOpened;

                // show project chooser
                if (projectToBeOpened == null) {
                    JFileChooser chooser = ProjectChooser.projectChooser();
                    chooser.setCurrentDirectory(workingFolder);
                    chooser.showOpenDialog(null);
                    File projectDir = chooser.getSelectedFile();
                    if (projectDir != null) {
                        FileObject projectFolder = FileUtil.toFileObject(projectDir);
                        if (projectFolder != null) {
                            try {
                                p = ProjectManager.getDefault().findProject(projectFolder);
                            } catch (IOException e1) {
                                ErrorManager err = ErrorManager.getDefault();
                                err.annotate(e1, "Can not find project for " + projectFolder);
                                err.notify(e1);
                            }
                        }
                    }
                }

                if (p == null) return;

                openProject(p);

            } else if (panel.createButton.equals(src)) {
                ProjectUtilities.newProjectWizard(workingFolder);
            }
            if (panel.againCheckBox.isSelected()) {
               HistorySettings.setFlag(HistorySettings.PROP_SHOW_CHECKOUT_COMPLETED, 0);
            }
        }

        private void openProject(Project p) {
            Project[] projects = new Project[] {p};
            OpenProjects.getDefault().open(projects, true);

            // set as main project and expand
            ContextAwareAction action = (ContextAwareAction) CommonProjectActions.setAsMainProjectAction();
            Lookup ctx = Lookups.singleton(p);
            Action ctxAction = action.createContextAwareInstance(ctx);
            ctxAction.actionPerformed(null);
            ProjectUtilities.selectAndExpandProject(p);
        }
    }
}
