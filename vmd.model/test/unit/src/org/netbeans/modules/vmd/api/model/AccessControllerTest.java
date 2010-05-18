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
package org.netbeans.modules.vmd.api.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netbeans.modules.vmd.api.model.common.TestAccessController;
import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

/**
 *
 * @author Karol Harezlak
 */
public class AccessControllerTest extends TestCase {

    private DesignDocument document ;

    public AccessControllerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AccessControllerTest.class);
        
        return suite;
    }
    
    /**
     * Test of notifyEventFiring method, of class org.netbeans.modules.vmd.api.model.AccessController.
     */
    public void testComplex() {
        System.out.println("Complex test"); // NOI18
        
        final int componentsNumber = 2;
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                System.out.println("Document dump:"); // NOI18N
                Debug.dumpDocument(document);
                for (int i=0;i<componentsNumber;i++){
                    document.createComponent(FirstCD.TYPEID_CLASS);
                }
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                TestAccessController controller = document.getListenerManager().getAccessController(TestAccessController.class);
                assertEquals(componentsNumber,controller.getCreatedComponents().size());
                assertTrue(controller.isNotifyEventFiringFlag());
                assertTrue(controller.isNotifyComponentsCreated());
                assertTrue(controller.isNotifyEventFiredFlag());
            }
        });
    }
    
    /**
     * Test of notifyEventFired method, of class org.netbeans.modules.vmd.api.model.AccessController.
     */
    //TODO not ready
    public void testNotifyEventFired() {
        
        System.out.println("Notify Event Fire Test "); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                document.createComponent(FirstCD.TYPEID_CLASS).getDocument();
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                TestAccessController controller = document.getListenerManager().getAccessController(TestAccessController.class);
                
                assertTrue(controller.isNotifyEventFiredFlag());
            }
        });
    }
}
