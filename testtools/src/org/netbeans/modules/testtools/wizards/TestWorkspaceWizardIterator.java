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
 * WTestWorkspaceWizardIterator.java
 *
 * Created on April 11, 2002, 11:46 AM
 */

import org.openide.loaders.TemplateWizard;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataObject;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import java.util.HashSet;
import org.w3c.dom.Document;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestWorkspaceWizardIterator extends WizardIterator {
    
    private static TestWorkspaceWizardIterator iterator;
    
    /** Creates a new instance of WorkspaceWizardIterator */
    public TestWorkspaceWizardIterator() {
    }
    
    public static synchronized TestWorkspaceWizardIterator singleton() {
        if (iterator==null)
            iterator=new TestWorkspaceWizardIterator();
        return iterator;
    }

    public void initialize(TemplateWizard wizard) {
        this.wizard=wizard;
        panels=new WizardDescriptor.Panel[] {
            wizard.targetChooser(),
            new TestWorkspaceSettingsPanel(),
            new TestTypeTemplatePanel(),
            new TestTypeSettingsPanel(),
            new TestBagSettingsPanel(),
            new TestSuiteTargetPanel(),
            new TestCasesPanel()
        };
        names = new String[] {
            "Test Workspace "+wizard.targetChooser().getComponent().getName(),
            "Test Workspace Settings",
            "Test Type Name and Template",
            "Test Type Settings",
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
        
        Document doc=getDOM(wizard.getTemplate());
        wizard.putProperty(TESTWORKSPACE_TYPE_PROPERTY ,getProperty(doc, "xtest.testtype", "value"));
        wizard.putProperty(TESTWORKSPACE_ATTRIBUTES_PROPERTY ,getProperty(doc, "xtest.attribs", "value"));
        wizard.putProperty(TESTWORKSPACE_SOURCE_PROPERTY ,getProperty(doc, "xtest.source.location", "value"));

    }
    
    public java.util.Set instantiate(TemplateWizard wizard) throws java.io.IOException {
        wizard.putProperty(CREATE_TESTBAG_PROPERTY, new Boolean(current>3));
        wizard.putProperty(CREATE_TESTTYPE_PROPERTY, new Boolean(current>2));
        wizard.putProperty(CREATE_SUITE_PROPERTY, new Boolean(!hasNext()));
        return instantiateTestWorkspace(wizard);
    }
    
}
