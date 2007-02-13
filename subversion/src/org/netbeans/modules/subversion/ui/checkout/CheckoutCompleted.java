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
package org.netbeans.modules.subversion.ui.checkout;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Stupka
 */
public class CheckoutCompleted implements ActionListener {
       
    private final File workingFolder;
    private final boolean openProject;
    private String[] checkedOutFolders;
    
    private CheckoutCompletedPanel panel;
    private Dialog dialog;
    private Project projectToBeOpened;

    public CheckoutCompleted(File workingFolder, String[] checkedOutFolders, boolean openProject) {
        this.openProject = openProject;
        this.checkedOutFolders = checkedOutFolders;
        this.workingFolder = workingFolder;
    }

    public void scanForProjects(SvnProgressSupport support) {

        List<Project> checkedOutProjects = new LinkedList<Project>();
        File normalizedWorkingFolder = FileUtil.normalizeFile(workingFolder);
        // checkout creates new folders and cache must be aware of them
        SvnUtils.refreshRecursively(normalizedWorkingFolder);
        FileObject fo = FileUtil.toFileObject(normalizedWorkingFolder);
        if (fo != null) {            
            for (int i = 0; i < checkedOutFolders.length; i++) {
                if(support!=null && support.isCanceled()) {
                    return;
                }
                String module = checkedOutFolders[i];
                if (".".equals(module)) {  // NOI18N
                    checkedOutProjects = ProjectUtilities.scanForProjects(fo);
                    break;
                } else {
                    FileObject subfolder = fo.getFileObject(module);
                    if (subfolder != null) {
                        checkedOutProjects.addAll(ProjectUtilities.scanForProjects(subfolder));
                    }
                }
            }
        }

        panel = new CheckoutCompletedPanel();
        panel.openButton.addActionListener(this);
        panel.createButton.addActionListener(this);
        panel.closeButton.addActionListener(this);
        panel.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));
        panel.againCheckBox.setVisible(openProject == false);
        String title = NbBundle.getMessage(CheckoutAction.class, "BK3008"); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(panel, title);
        descriptor.setModal(true);

        // move buttons from dialog to descriptor
        panel.remove(panel.openButton);
        panel.remove(panel.createButton);
        panel.remove(panel.closeButton);

        Object[] options = null;
        if (checkedOutProjects.size() > 1) {
            String msg = NbBundle.getMessage(CheckoutAction.class, "BK3009", new Integer(checkedOutProjects.size())); // NOI18N
            panel.jLabel1.setText(msg);
            options = new Object[] {
                panel.openButton,
                panel.closeButton
            };
        } else if (checkedOutProjects.size() == 1) {
            Project project = (Project) checkedOutProjects.iterator().next();
            projectToBeOpened = project;
            ProjectInformation projectInformation = ProjectUtils.getInformation(project);
            String projectName = projectInformation.getDisplayName();
            String msg = NbBundle.getMessage(CheckoutAction.class, "BK3011", projectName); // NOI18N
            panel.jLabel1.setText(msg);
            panel.openButton.setText(NbBundle.getMessage(CheckoutAction.class, "BK3012")); // NOI18N
            options = new Object[] {
                panel.openButton,
                panel.closeButton
            };
        } else {
            String msg = NbBundle.getMessage(CheckoutAction.class, "BK3010"); // NOI18N
            panel.jLabel1.setText(msg);
            options = new Object[] {
                panel.createButton,
                panel.closeButton
            };

        }

        descriptor.setMessageType(DialogDescriptor.INFORMATION_MESSAGE);
        descriptor.setOptions(options);
        descriptor.setClosingOptions(options);
        descriptor.setHelpCtx(new HelpCtx(CheckoutCompletedPanel.class));
        dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(CheckoutAction.class, "ACSD_CheckoutCompleted_Dialog")); // NOI18N

        if(support!=null && support.isCanceled()) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setVisible(true);
            }
        });
    }
    
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        dialog.setVisible(false);
        if (panel.openButton.equals(src)) {
            // show project chooser
            if (projectToBeOpened == null) {
                // et tu, svn? (see #77438)
                Action a = findAction( "Actions/Project/org-netbeans-modules-project-ui-OpenProject.instance" ); // NOI18N
                if( null != a ) {
                    a.actionPerformed( e );
                }                
            } else {
                if (projectToBeOpened == null) return; 
                openProject(projectToBeOpened);
            }

        } else if (panel.createButton.equals(src)) {
            ProjectUtilities.newProjectWizard(workingFolder);
        }        
    }
    
    public static Action findAction( String key ) {
        FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(key);
        
        if (fo != null && fo.isValid()) {
            try {
                DataObject dob = DataObject.find(fo);
                InstanceCookie ic = (InstanceCookie) dob.getCookie(InstanceCookie.class);
                
                if (ic != null) {
                    Object instance = ic.instanceCreate();
                    if (instance instanceof Action) {
                        Action a = (Action) instance;
                        return a;
                    }
                }
            } catch (Exception e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
                return null;
            }
        }
        return null;
    }
    
    private void openProject(Project p) {
        Project[] projects = new Project[] {p};
        OpenProjects.getDefault().open(projects, false);

        // set as main project and expand
        ContextAwareAction action = (ContextAwareAction) CommonProjectActions.setAsMainProjectAction();
        Lookup ctx = Lookups.singleton(p);
        Action ctxAction = action.createContextAwareInstance(ctx);
        ctxAction.actionPerformed(new ActionEvent(this, 0, ""));
        ProjectUtilities.selectAndExpandProject(p);
    }
}
