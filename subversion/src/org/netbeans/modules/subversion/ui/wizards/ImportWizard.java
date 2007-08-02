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
package org.netbeans.modules.subversion.ui.wizards;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.text.MessageFormat;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.subversion.ui.browser.BrowserAction;
import org.netbeans.modules.subversion.ui.browser.CreateFolderAction;
import org.netbeans.modules.subversion.ui.wizards.importstep.ImportPreviewStep;
import org.netbeans.modules.subversion.ui.wizards.importstep.ImportStep;
import org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep;
import org.netbeans.modules.subversion.util.Context;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/*
 *
 *
 * @author Tomas Stupka
 */
public final class ImportWizard implements ChangeListener {
    
    private WizardDescriptor.Panel[] panels;
    private RepositoryStep repositoryStep;
    private ImportStep importStep;
    private ImportPreviewStep importPreviewStep;

    private String errorMessage;
    private WizardDescriptor wizardDescriptor;
    private PanelsIterator wizardIterator;
    
    private final Context context;
    
    public ImportWizard(Context context) {
        this.context = context;
    }
    
    public boolean show() {
        wizardIterator = new PanelsIterator();
        wizardDescriptor = new WizardDescriptor(wizardIterator);
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(org.openide.util.NbBundle.getMessage(ImportWizard.class, "CTL_Import")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ImportWizard.class, "CTL_Import"));
        dialog.setVisible(true);
        dialog.toFront();
        Object value = wizardDescriptor.getValue();
        boolean finnished = value == WizardDescriptor.FINISH_OPTION;
        
        if(!finnished) {
            // wizard wasn't properly finnished ...
            if(value == WizardDescriptor.CLOSED_OPTION || 
               value == WizardDescriptor.CANCEL_OPTION ) 
            {
                // wizard was closed or canceled -> reset all steps & kill all running tasks                
                repositoryStep.stop();
                importStep.stop();
            }            
        } else if (value == WizardDescriptor.FINISH_OPTION) {
            if(wizardIterator.current() == importStep) {
                setupImportPreviewStep();
            } else if (wizardIterator.current() == importPreviewStep) {
                importPreviewStep.storeTableSorter();
            }            
        }
        return finnished;
    }    

    private void setupImportPreviewStep() {
        // must be initialized so we may retrieve the commitFiles for the ImportAction
        String repositoryUrl = repositoryStep.getRepositoryFile().getRepositoryUrl().toString();
        String repositoryFolderUrl = importStep.getRepositoryFolderUrl().toString();
        String localPath = context.getRootFiles()[0].getAbsolutePath();            
        importPreviewStep.setup(repositoryFolderUrl.substring(repositoryUrl.length()), localPath);
    }
    
    private void setErrorMessage(String msg) {
        errorMessage = msg;
        if (wizardDescriptor != null) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", msg); // NOI18N
        }
    }

    public void stateChanged(ChangeEvent e) {
        if(wizardIterator==null) {
            return;
        }
        AbstractStep step = (AbstractStep) wizardIterator.current();
        if(step==null) {
            return;
        }
        setErrorMessage(step.getErrorMessage());
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private class PanelsIterator extends WizardDescriptor.ArrayIterator {                

        PanelsIterator() {            
        }

        protected WizardDescriptor.Panel[] initializePanels() {
            WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[3];            

            repositoryStep = new RepositoryStep(RepositoryStep.IMPORT_HELP_ID);
            repositoryStep.addChangeListener(ImportWizard.this);

            File file = context.getRootFiles()[0];
            importStep = new ImportStep(new BrowserAction[] { new CreateFolderAction(file.getName())}, file);
            importStep.addChangeListener(ImportWizard.this);

            importPreviewStep = new ImportPreviewStep(context);

            panels = new  WizardDescriptor.Panel[] {repositoryStep, importStep, importPreviewStep};

            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
                }
            }
            return panels;
        }

        public void previousPanel() {
            if(current() == importStep) {
                // just a dummy to force setting the message in
                // through importStep.validateUserInput(); in  nextPanel()
                importStep.invalid(null);
            }
            super.previousPanel();
        }

        public void nextPanel() {            
            if(current() == repositoryStep) {
                File file = context.getRootFiles()[0];
                importStep.setup(repositoryStep.getRepositoryFile().appendPath(file.getName()));
            } else if(current() == importStep) {
                setupImportPreviewStep();
            }
            super.nextPanel();
            if(current() == importStep) {                                                            
                importStep.validateUserInput();
            }
        }

    }

    public SVNUrl getRepositoryUrl() {
        return repositoryStep.getRepositoryFile().getRepositoryUrl(); 
    }

    public String getMessage() {
        return importStep.getImportMessage();
    }

    public SVNUrl getRepositoryFolderUrl() {
        return importStep.getRepositoryFolderUrl();
    }

    public Map getCommitFiles() {
        return importPreviewStep.getCommitFiles();
    }

}

