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

package org.netbeans.modules.projectimport.eclipse;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.projectimport.eclipse.wizard.ProgressPanel;
import org.netbeans.modules.projectimport.eclipse.wizard.ProjectImporterWizard;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Runs EclipseProject Importer.
 *
 * @author mkrauskopf
 */
public class ImportProjectAction extends CallableSystemAction {
    
    /** Creates a new instance of ImportProjectAction */
    public ImportProjectAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }
    
    public void performAction() {
        ProjectImporterWizard wizard = new ProjectImporterWizard();
        wizard.start();
        Set eclProjects = wizard.getProjects();
        String destination = wizard.getDestination();
        if (wizard.isCancelled() || eclProjects == null || destination == null) {
            return;
        }
        
        final Importer importer = new Importer(eclProjects, destination, 
                wizard.getRecursively());
        
        // prepare progress dialog
        final ProgressPanel progressPanel = new ProgressPanel();
        DialogDescriptor desc = new DialogDescriptor(progressPanel,
                NbBundle.getMessage(ImportProjectAction.class, "CTL_ProgressDialogTitle"),
                true, new Object[]{}, null, 0, null, null);
        desc.setClosingOptions(new Object[]{});
        final Dialog progressDialog = DialogDisplayer.getDefault().createDialog(desc);
        ((JDialog) progressDialog).setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        progressPanel.setNumberOfSteps(wizard.getNumberOfImportedProject());
        
        // progress timer for periodically update progress
        final Timer progressTimer = new Timer(50, null);
        progressTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progressPanel.setCurrentStep(importer.getNOfProcessed());
                progressPanel.setInfo(importer.getProgressInfo());
                if (importer.isDone()) {
                    progressTimer.stop();
                    progressDialog.setVisible(false);
                    progressDialog.dispose();
                    Collection warnings = importer.getWarnings();
                    if (warnings != null) {
                        StringBuffer messages = new StringBuffer(
                                NbBundle.getMessage(ImportProjectAction.class,
                                "MSG_ProblemsOccured")); // NOI18N
                        messages.append("\n\n"); // NOI18N
                        for (Iterator it = warnings.iterator(); it.hasNext(); ) {
                            String message = (String) it.next();
                            messages.append(" - " + message + "\n"); // NOI18N
                        }
                        NotifyDescriptor d = new DialogDescriptor.Message(
                                messages.toString(), NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                    }
                    // open created projects when importing finished
                    OpenProjects.getDefault().open(importer.getProjects(), true);
                }
            }
        });
        importer.startImporting(); // runs importing in separate thread
        progressTimer.start();
        progressDialog.setVisible(true);
        //        OpenProjectList.getDefault().open(project, true);
        //        OpenProjectList.getDefault().setMainProject( project );
        //        final ProjectTab ptLogial  = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
        //        ProjectUtilities.openProjectFiles(project);
        //        Node root = ptLogial.getExplorerManager().getRootContext();
        //        Node projNode = root.getChildren().findChild(project.getProjectDirectory().getName());
        //        try {
        //            ptLogial.getExplorerManager().setSelectedNodes(new Node[] {projNode});
        //            ptLogial.open();
        //            ptLogial.requestActive();
        //        } catch (Exception ignore) {
        //            // may ignore it
        //        }
    }
    
    public String getName() {
        return NbBundle.getMessage(ImportProjectAction.class, "CTL_MenuItem"); // NOI18N
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    protected boolean asynchronous() {
        return false;
    }
}
