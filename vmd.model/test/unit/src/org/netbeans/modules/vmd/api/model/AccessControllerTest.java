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
                System.out.println("Document dump:");
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
        
        System.out.println("Notify Event Fire Test "); // NOI18
        
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
