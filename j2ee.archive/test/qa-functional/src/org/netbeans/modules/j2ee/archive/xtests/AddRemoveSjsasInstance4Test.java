/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.archive.xtests;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.j2ee.archive.wizard.*;
import org.netbeans.modules.j2ee.deployment.impl.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.sun.ide.j2ee.PlatformValidator;
import org.netbeans.modules.j2ee.sun.ide.j2ee.ui.AddDomainWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;

/**
 *
 * @author Michal Mocnak
 */
public class AddRemoveSjsasInstance4Test extends NbTestCase {
    
    private final int SLEEP = 10000;
    
    public AddRemoveSjsasInstance4Test(String testName) {
        super(testName);
    }
    
    public void addSjsasInstance() {
        try {
            AddDomainWizardIterator inst = new AddDomainWizardIterator(new PlatformValidator());
            WizardDescriptor wizard = new WizardDescriptor(new Panel[] {});
            wizard.putProperty(TestUtil.PLATFORM_LOCATION, new File(TestUtil._PLATFORM_LOCATION));
            wizard.putProperty(TestUtil.INSTALL_LOCATION, TestUtil._INSTALL_LOCATION);
            wizard.putProperty(TestUtil.PROP_DISPLAY_NAME, TestUtil._DISPLAY_NAME);
            wizard.putProperty(TestUtil.HOST, TestUtil._HOST);
            wizard.putProperty(TestUtil.PORT, TestUtil._PORT);
            wizard.putProperty(TestUtil.DOMAIN, TestUtil._DOMAIN);
            wizard.putProperty(TestUtil.USER_NAME, TestUtil._USER_NAME);
            wizard.putProperty(TestUtil.PASSWORD, TestUtil._PASSWORD);
            
            inst.initialize(wizard);
            inst.instantiate();
            
            ServerRegistry.getInstance().checkInstanceExists(TestUtil._URL);
            
            TestUtil.sleep(SLEEP);
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }
    
    public void removeSjsasInstance() {
        try {
            TestUtil.sleep(SLEEP);
            
            ServerInstance inst = ServerRegistry.getInstance().getServerInstance(TestUtil._URL);
            inst.remove();
            
            try {
                ServerRegistry.getInstance().checkInstanceExists(TestUtil._URL);
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
        suite.addTest(new AddRemoveSjsasInstance4Test("addSjsasInstance"));        
        suite.addTest(new AddRemoveSjsasInstance4Test("removeSjsasInstance"));        
        return suite;
    }
}
