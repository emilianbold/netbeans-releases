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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.Timer;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.projectimport.eclipse.wizard.ProgressDialog;
import org.netbeans.modules.projectimport.eclipse.wizard.ProjectImporterWizard;
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
        
        final Importer importer = new Importer(eclProjects, destination);
        
        // prepare progress dialog
        final ProgressDialog progress = new ProgressDialog(true);
        progress.setNumberOfSteps(wizard.getNumberOfImportedProject());
        
        // progress timer for periodically update progress
        final Timer progressTimer = new Timer(50, null);
        progressTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progress.setCurrentStep(importer.getNOfProcessed());
                progress.setInfo(importer.getProgressInfo());
                if (importer.isDone()) {
                    progressTimer.stop();
                    progress.setVisible(false);
                    progress.dispose();
                    // open created projects when importing finished
                    OpenProjects.getDefault().open(importer.getProjects(), true);
                }
            }
        });
        importer.startImporting(); // runs importing in separate thread
        progressTimer.start();
        progress.setVisible(true);
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
