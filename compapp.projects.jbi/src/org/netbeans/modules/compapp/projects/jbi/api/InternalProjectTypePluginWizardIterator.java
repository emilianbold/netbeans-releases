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
package org.netbeans.modules.compapp.projects.jbi.api;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.jbi.ui.wizards.WizardProperties;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author jsandusky
 */
public abstract class InternalProjectTypePluginWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    private transient List<WizardDescriptor.Panel> mAddedPanels;
    private transient List<String> mAddedSteps;
    
    private transient FileObject mProjectFileObject;
    
    
    protected abstract void createProject(File dirF, String name, String j2eeLevel) throws IOException;

    
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJECT_DIR, null);
        this.wiz.putProperty(WizardProperties.NAME, null);
        this.wiz = null;
        panels = null;
    }
    
    protected String getDefaultName() {
        return NbBundle.getMessage(getClass(), "LBL_NPW1_DefaultProjectName"); //NOI18N
    }
    
    protected String getDefaultTitle() {
        return NbBundle.getMessage(getClass(), "TXT_NewWebApp"); //NOI18N
    }
    
    
    public final String name() {
        return MessageFormat.format(
                NbBundle.getMessage(getClass(), "LBL_WizardStepsCount"),  //NOI18N
                new String[] {
                    (new Integer(index + 1)).toString(), 
                    (new Integer(panels.length)).toString() });
    }
    
    public final boolean hasNext() {
        return index < panels.length - 1;
    }
    public final boolean hasPrevious() {
        return index > 0;
    }
    public final void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public final void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public final WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public final Set instantiate() throws IOException {
        Set resultSet = new HashSet();
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        
        createProject(dirF, name, j2eeLevel);
        mProjectFileObject = FileUtil.toFileObject(dirF);
        
        resultSet.add(mProjectFileObject);
        
        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        return resultSet;
    }
    
    public final Project getProject() throws IOException {
        if (mProjectFileObject != null) {
            return ProjectManager.getDefault().findProject(mProjectFileObject);
        }
        return null;
    }
    
    public final boolean hasContent() {
        return mAddedPanels != null && mAddedSteps != null;
    }
    
    public final void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}

    
    protected final void addPanel(WizardDescriptor.Panel panel) {
        if (mAddedPanels == null) {
            mAddedPanels = new ArrayList<WizardDescriptor.Panel>();
        }
        mAddedPanels.add(panel);
    }
    
    protected final void addStep(String step) {
        if (mAddedSteps == null) {
            mAddedSteps = new ArrayList<String>();
        }
        mAddedSteps.add(step);
    }
    
    
    private WizardDescriptor.Panel[] createPanels() {
        if (mAddedPanels != null) {
            return mAddedPanels.toArray(new WizardDescriptor.Panel[mAddedPanels.size()]);
        }
        return new WizardDescriptor.Panel[] {};
    }
    
    private String[] createSteps() {
        if (mAddedSteps != null) {
            return mAddedSteps.toArray(new String[mAddedSteps.size()]);
        }
        return new String[] {};
    }
}
