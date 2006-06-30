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

package org.netbeans.modules.testtools.wizards;

/*
 * TestWorkspaceWizardIterator.java
 *
 * Created on April 11, 2002, 11:46 AM
 */

import java.util.Set;
import java.util.HashSet;

import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileObject;

/** Test Workspace Wizard Iterator class
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestWorkspaceWizardIterator extends WizardIterator {
    
    static final long serialVersionUID = 4086623721825821549L;
    
    private static TestWorkspaceWizardIterator iterator;
    
    /** Creates a new instance of TestWorkspaceWizardIterator */
    public TestWorkspaceWizardIterator() {
    }
    
    /** singleton method
     * @return TestSuiteWizardIterator singleton instance */    
    public static synchronized TestWorkspaceWizardIterator singleton() {
        if (iterator==null)
            iterator=new TestWorkspaceWizardIterator();
        return iterator;
    }

    /** perform initialization of Wizard Iterator
     * @param wizard TemplateWizard instance requested Wizard Iterator */    
    public void initialize(TemplateWizard wizard) {
        this.wizard=wizard;
        WizardSettings set=new WizardSettings();
        set.workspaceTemplate=wizard.getTemplate();
        set.readWorkspaceSettings();
        set.startFromWorkspace=true;
        set.store(wizard);
        panels=new WizardDescriptor.Panel[] {
            wizard.targetChooser(),
            new TestWorkspaceSettingsPanel().panel,
            new TestTypeTemplatePanel().panel,
            new TestTypeSettingsPanel().panel,
            new TestTypeAdvancedSettingsPanel().panel,
            new TestBagSettingsPanel().panel,
            new TestSuiteTargetPanel().panel,
            new TestCasesPanel().panel
        };
        names = new String[panels.length];
        for (int i=0; i<panels.length; i++) {
            ((javax.swing.JComponent)panels[i].getComponent()).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            names[i]=((javax.swing.JComponent)panels[i].getComponent()).getName();
        }
        ((javax.swing.JComponent)panels[0].getComponent()).putClientProperty("WizardPanel_contentData", names);  // NOI18N
        current=0;
        

    }
    
    /** perform instantiation of templates
     * @param wizard TemplateWizard instance requested Wizard Iterator
     * @throws IOException when some IO problems
     * @return Set of newly created Data Objects */    
    public Set instantiate(TemplateWizard wizard) throws java.io.IOException {
        WizardSettings set=WizardSettings.get(wizard);
        set.workspaceTarget=wizard.getTargetFolder();
        set.workspaceName=wizard.getTargetName();
        set.createType=current>4;
        set.createSuite=!hasNext();
        return instantiateTestWorkspace(set);
    }
    
}
