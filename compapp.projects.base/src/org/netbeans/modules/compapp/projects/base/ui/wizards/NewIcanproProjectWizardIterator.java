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


package org.netbeans.modules.compapp.projects.base.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;

import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.compapp.projects.base.IcanproProjectGenerator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.NbBundle;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick
 */
public class NewIcanproProjectWizardIterator 
    implements WizardDescriptor.InstantiatingIterator,
               org.netbeans.modules.compapp.projects.base.IcanproConstants {
    
    private static final long serialVersionUID = 1L;
    
    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new PanelConfigureProject(getDefaultName(), getDefaultTitle()),
            //need this after EA1
            ///new PanelConfigureProjectApp(),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getBundle(WIZARD_BUNDLE).getString("LBL_NWP1_ProjectTitleName"), //NOI18N
            //need this after EA1
            ///NbBundle.getBundle("WIZARD_BUNDLE).getString("LBL_NWP1_ProjectAppName") //NOI18N
        };
    }

    protected String getDefaultName() {
        return NbBundle.getBundle(WIZARD_BUNDLE).getString("LBL_NPW1_DefaultProjectName");
    }
    
    protected String getDefaultTitle() {
        return NbBundle.getBundle(WIZARD_BUNDLE).getString("TXT_NewWebApp");
    }
    
    protected void createProject(File dirF, String name, String j2eeLevel) throws IOException {
        IcanproProjectGenerator.createProject(dirF, name, j2eeLevel);
    }

    public Set instantiate() throws IOException {
        Set resultSet = new HashSet();
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        
        createProject(dirF, name, j2eeLevel);
        FileObject dir = FileUtil.toFileObject(dirF);
        
        resultSet.add(dir);
        
        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
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
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJECT_DIR,null);
        this.wiz.putProperty(WizardProperties.NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getBundle(WIZARD_BUNDLE).getString("LBL_WizardStepsCount"), new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
