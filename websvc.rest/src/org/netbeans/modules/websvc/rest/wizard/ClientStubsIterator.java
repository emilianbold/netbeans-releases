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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.codegen.ClientStubsGenerator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Nam Nguyen
 */
public final class ClientStubsIterator implements WizardDescriptor.InstantiatingIterator {
    
    private int index;
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private RequestProcessor.Task generatorTask;
  
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            String name = NbBundle.getMessage(ClientStubsSetupPanel.class, "LBL_SelectRestServiceProjects");
            panels = new WizardDescriptor.Panel[] {
                new ClientStubsSetupPanel(name, wizard)
            
            };
            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }
    
    public Set instantiate() throws IOException {
        final FileObject stubRoot = (FileObject) wizard.getProperty(WizardProperties.STUB_ROOT_FOLDER);
        final Project[] projectsToStub = (Project[]) wizard.getProperty(WizardProperties.PROJECTS_TO_STUB);
        final boolean overwrite = (Boolean) wizard.getProperty(WizardProperties.OVERWRITE_EXISTING);
        final Set<FileObject> result = new HashSet<FileObject>();
        
        try {
            final ProgressDialog dialog = new ProgressDialog(NbBundle.getMessage(
                    ClientStubsIterator.class, "LBL_ClientStubsProgress"));
            
            generatorTask = RequestProcessor.getDefault().create(new Runnable() {
                public void run() {
                    ProgressHandle pHandle = dialog.getProgressHandle();
                    pHandle.start();
                    
                    try {
                        for (Project project : projectsToStub) {
                            result.addAll(new ClientStubsGenerator(stubRoot, project, overwrite).generate(pHandle));
                        }
                    } catch(Exception iox) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "instantiate", iox);
                    } finally {
                        dialog.close();
                        pHandle.finish();
                    }
                }
            });
            generatorTask.schedule(50);
            dialog.open();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "instantiate", ex);
        }
        return result;
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizard.putProperty("NewFileWizard_Title", name());       //NOI18N
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return NbBundle.getMessage(ClientStubsIterator.class, "Templates/WebServices/RestClientStubs");
    }
    
    public boolean hasNext() {
        return index < getPanels().length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}

    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        
        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }
        
        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }
}
