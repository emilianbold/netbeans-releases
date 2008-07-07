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

/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.sql.project.ui.wizards;

//import org.netbeans.modules.compapp.projects.base.ui.wizards.NewIcanproProjectWizardIterator;
import static org.netbeans.modules.sql.project.SQLproConstants.*;
import org.netbeans.modules.sql.project.SQLproProjectGenerator;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;



/**
 * Wizard to create a new Web project.
 * @author Jesse Glick
 */
public class NewSQLproProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wizardDescriptor;
    
    public NewSQLproProjectWizardIterator() {
    }
    public Set<Object> instantiate() throws IOException {
        Set<Object> resultSet = new HashSet<Object>();
        File dirF = (File) wizardDescriptor.getProperty(PROJECT_DIR);
        String name = (String) wizardDescriptor.getProperty(NAME);
        String j2eeLevel = (String) wizardDescriptor.getProperty(J2EE_LEVEL);
                 
        AntProjectHelper h = SQLproProjectGenerator.createProject(dirF, name, j2eeLevel);
                
        FileObject dir = FileUtil.toFileObject(dirF);
        
        resultSet.add(dir);
        
        return resultSet;
    }
    public void initialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
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
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor.putProperty(PROJECT_DIR,null);
        this.wizardDescriptor.putProperty(NAME,null);
        this.wizardDescriptor = null;
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return MessageFormat.format(
                NbBundle.getMessage(NewSQLproProjectWizardIterator.class,"LBL_WizardStepsCount"), //NOI18N
                new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}
        );
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

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
        		new PanelConfigureProject(getDefaultName()),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(NewSQLproProjectWizardIterator.class, "LBL_NPW1_ProjectTitleName"), //NOI18N
        };
    }
    protected String getDefaultName() {
        return NbBundle.getMessage(NewSQLproProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName"); //NOI18N
    }
}
