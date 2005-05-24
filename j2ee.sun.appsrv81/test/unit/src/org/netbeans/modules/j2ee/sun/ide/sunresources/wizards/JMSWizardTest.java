/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * JMSWizardTest.java
 * JUnit based test
 *
 * Created on May 5, 2004, 1:02 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;
import org.openide.WizardDescriptor;

import junit.framework.*;

/**
 *
 * @author vkraemer
 */
public class JMSWizardTest extends TestCase {
     
    private WizardDescriptor wiz;
    private WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[2];
    
    public void testCreate() {
        JMSWizard s1 = new JMSWizard().create();
        //JMSWizard.JMSWizardIterator s1 = (JMSWizard.JMSWizardIterator) JMSWizard.singleton();
        //wiz.getPanel(0);
        //wiz.getResourceConfigHelper();
        //s1.stateChange(new javax.swing.event.ChangeEvent() {
            
        //});
        //}
        wiz = new WizardDescriptor(panels);
        s1.initialize(wiz);
        
        assertNotNull("ResourceConfigHelper was created ", s1.getResourceConfigHelper());
        
        s1.getResourceConfigHelper().getData().addProperty("foo", "bar");
        s1.getResourceConfigHelper().getData().setTargetFile("JMSWizardTestFile");
        s1.getWizardInfo();
        s1.hasNext();
        s1.hasPrevious();
        s1.name();
        s1.nextPanel();
        s1.previousPanel();
        s1.setResourceConfigHelper(s1.getResourceConfigHelper());
        s1.instantiate();
    }
    
    public JMSWizardTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(JMSWizardTest.class);
        return suite;
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    
}
