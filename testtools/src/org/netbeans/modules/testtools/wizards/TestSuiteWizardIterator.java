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
 * TestSuiteWizardIterator.java
 *
 * Created on April 11, 2002, 11:46 AM
 */

import java.util.Set;
import java.util.Vector;
import java.util.HashSet;

import org.openide.TopManager;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.src.MethodElement;
import org.openide.src.SourceException;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

import org.netbeans.modules.java.JavaDataObject;

/** Test Suite Wizard Iterator class
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class TestSuiteWizardIterator extends WizardIterator {
    
    static final long serialVersionUID = -3466297489095246515L;
    
    private static TestSuiteWizardIterator iterator;
    
    /** Creates a new instance of TestSuiteWizardIterator */
    public TestSuiteWizardIterator() {
    }
    
    /** singleton method
     * @return TestSuiteWizardIterator singleton instance */    
    public static synchronized TestSuiteWizardIterator singleton() {
        if (iterator==null)
            iterator=new TestSuiteWizardIterator();
        return iterator;
    }

    /** perform initialization of Wizard Iterator
     * @param wizard TemplateWizard instance requested Wizard Iterator */    
    public void initialize(TemplateWizard wizard) {
        this.wizard=wizard;
        WizardSettings set=new WizardSettings();
        set.suiteTemplate=wizard.getTemplate();
        set.templateMethods=getTemplateMethods((JavaDataObject)set.suiteTemplate);
        set.startFromSuite=true;
        set.store(wizard);
        
        panels=new WizardDescriptor.Panel[] {
            wizard.targetChooser(),
            new TestCasesPanel()
        };
        names = new String[panels.length];
        for (int i=0; i<panels.length; i++) {
            ((javax.swing.JComponent)panels[i]).putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
            names[i]=((javax.swing.JComponent)panels[i]).getName();
        }
        ((javax.swing.JComponent)panels[0]).putClientProperty("WizardPanel_contentData", names);  // NOI18N
        current=0;
    }
    
    /** perform instantiation of templates
     * @param wizard TemplateWizard instance requested Wizard Iterator
     * @throws IOException when some IO problems
     * @return Set of newly created Data Objects */    
    public Set instantiate(TemplateWizard wizard) throws java.io.IOException {
        WizardSettings set=WizardSettings.get(wizard);
        set.suiteTarget=wizard.getTargetFolder();
        set.suiteName=wizard.getTargetName();
        return instantiateTestSuite(set);
    }
    
}
