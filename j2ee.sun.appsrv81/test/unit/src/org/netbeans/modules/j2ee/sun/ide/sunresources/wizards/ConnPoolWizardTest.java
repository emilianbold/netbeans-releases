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

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import junit.framework.TestCase;
import org.openide.WizardDescriptor;

/**
 *
 * @author vkraemer
 */
public class ConnPoolWizardTest extends TestCase {
    private WizardDescriptor wiz;
    private WizardDescriptor.Panel panels[] = new WizardDescriptor.Panel[3];

    public void testCreate() {
        ConnPoolWizard s1 = new ConnPoolWizard().create();
        wiz = new WizardDescriptor(panels);
        s1.initialize(wiz);

        assertNotNull("ResourceConfigHelper was created ", s1.getResourceConfigHelper());
        
        //wiz.getPanel(0);
        //wiz.getResourceConfigHelper();
        //s1.stateChange(new ChangeEvent() {
            
        //});
        //}
        s1.getResourceConfigHelper().getData().addProperty("foo", "bar");
        s1.getResourceConfigHelper().getData().setTargetFile("CPWizardTestFile");
        s1.getWizardInfo();
        s1.hasNext();
        s1.hasPrevious();
        s1.name();
        s1.nextPanel();
        s1.previousPanel();
        s1.setResourceConfigHelper(s1.getResourceConfigHelper());
        s1.instantiate();
    }
    
    public ConnPoolWizardTest(String testName) {
        super(testName);
    }
    
}
