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
        ((JDialog) progressDialog).setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        progressPanel.start(wizard.getNumberOfImportedProject());
        
        // progress timer for periodically update progress
        final Timer progressTimer = new Timer(50, null);
        progressTimer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                progressPanel.setProgress(importer.getNOfProcessed(), importer.getProgressInfo());
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
                    if (importer.getProjects().length > 0) {
                        OpenProjects.getDefault().setMainProject(importer.getProjects()[0]);
                    }
                }
            }
        });
        importer.startImporting(); // runs importing in separate thread
        progressTimer.start();
        progressDialog.setVisible(true);
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
