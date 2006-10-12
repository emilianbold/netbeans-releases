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

package org.netbeans.modules.j2ee.sun.test;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author Michal Mocnak
 */
public class AddRemoveSjsasInstanceTest extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public AddRemoveSjsasInstanceTest(String testName) {
        super(testName);
    }
    
    public void addSjsasInstance() {
        try {
            AddDomainWizardIterator inst = new AddDomainWizardIterator();
            WizardDescriptor wizard = new WizardDescriptor(new Panel[] {});
            wizard.putProperty(Util.PLATFORM_LOCATION, new File(Util._PLATFORM_LOCATION));
            wizard.putProperty(Util.INSTALL_LOCATION, Util._INSTALL_LOCATION);
            wizard.putProperty(Util.PROP_DISPLAY_NAME, Util._DISPLAY_NAME);
            wizard.putProperty(Util.HOST, Util._HOST);
            wizard.putProperty(Util.PORT, Util._PORT);
            wizard.putProperty(Util.DOMAIN, Util._DOMAIN);
            wizard.putProperty(Util.USER_NAME, Util._USER_NAME);
            wizard.putProperty(Util.PASSWORD, Util._PASSWORD);
            
            inst.initialize(wizard);
            inst.instantiate();
            
            ServerRegistry.getInstance().checkInstanceExists(Util._URL);
            
            Util.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void removeSjsasInstance() {
        try {
            Util.sleep(SLEEP);
            
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(Util._URL);
            inst.remove();
            
            try {
                ServerRegistry.getInstance().checkInstanceExists(Util._URL);
            } catch(Exception e) {
                return;
            }
            
            fail("Sjsas instance still exists !");
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite("AddRemoveSjsasInstanceTest");
        suite.addTest(new AddRemoveSjsasInstanceTest("addSjsasInstance"));        
        suite.addTest(new AddRemoveSjsasInstanceTest("removeSjsasInstance"));        
        return suite;
    }
}