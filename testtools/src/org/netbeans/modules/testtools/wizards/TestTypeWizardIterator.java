/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * TestTypeWizardIterator.java
 *
 * Created on April 11, 2002, 11:46 AM
 */

import java.io.File;
import java.util.Set;
import java.util.Vector;
import java.util.HashSet;
import java.io.IOException;

import org.openide.TopManager;
import org.openide.WizardDescriptor;
import org.openide.src.MethodElement;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.LocalFileSystem;


/** Test Type Wizard Iterator class
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestTypeWizardIterator extends WizardIterator {
    
    private static TestTypeWizardIterator iterator;
    
    /** Creates a new instance of TestTypeWizardIterator */
    public TestTypeWizardIterator() {
    }
    
    /** singleton method
     * @return TestSuiteWizardIterator singleton instance */    
    public static synchronized TestTypeWizardIterator singleton() {
        if (iterator==null)
            iterator=new TestTypeWizardIterator();
        return iterator;
    }

    /** perform initialization of Wizard Iterator
     * @param wizard TemplateWizard instance requested Wizard Iterator */    
    public void initialize(TemplateWizard wizard) {
        this.wizard=wizard;
        WizardSettings set=new WizardSettings();
        set.typeTemplate=wizard.getTemplate();
        set.startFromType=true;
        set.readTypeSettings();
        set.store(wizard);
        panels=new WizardDescriptor.Panel[] {
            wizard.targetChooser(),
            new TestTypeSettingsPanel(),
            new TestTypeAdvancedSettingsPanel(),
            new TestBagSettingsPanel(),
            new TestSuiteTargetPanel(),
            new TestCasesPanel()
        };
        names = new String[] {
            "Test Type "+wizard.targetChooser().getComponent().getName(),
            "Test Type Settings",
            "Test Type Advanced Settings",
            "Test Bag Settings",
            "Test Suite Template and Target Location",
            "Create Test Cases"
        };
        for (int i=0; i<panels.length; i++) {
            ((javax.swing.JComponent)panels[i]).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
            ((javax.swing.JComponent)panels[i]).setName(names[i]);
        }
        ((javax.swing.JComponent)panels[0]).putClientProperty("WizardPanel_contentData", names); 
        current=0;
    }
    
    /** perform instantiation of templates
     * @param wizard TemplateWizard instance requested Wizard Iterator
     * @throws IOException when some IO problems
     * @return Set of newly created Data Objects */    
    public Set instantiate(TemplateWizard wizard) throws IOException {
        WizardSettings set=WizardSettings.get(wizard);
        set.typeTarget=wizard.getTargetFolder();
        set.typeName=wizard.getTargetName();
        set.createSuite=!hasNext();
        return instantiateTestType(set);
    }
    
}
