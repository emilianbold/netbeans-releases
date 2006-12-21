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
import org.netbeans.modules.vmd.api.model.common.TypesSupport;
import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.descriptors.SecondCD;
import org.netbeans.modules.vmd.api.model.listeners.TestPresenterListener;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter1;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter2;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter3;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter4;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @author Karol Harezlak
 */
public class ListenerManagerTest extends TestCase {
    
    private DesignDocument document;
    private Long compID = null;
    
    public ListenerManagerTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ListenerManagerTest.class);
        //TestSuite suite = new TestSuite();
        //suite.addTest(new ListenerManagerTest("testAddRemovePresenterListener") ); // NOI18N
        
        return suite;
    }
    
    public void testGetDocumentState() {
        System.out.println("getDocumentState"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                long expResult = 0;
                
                assertEquals(expResult,document.getListenerManager().getDocumentState());
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                long expResult = 1;
                
                assertEquals(expResult,document.getListenerManager().getDocumentState());
            }
        });
    }
    
    public void testGetAccessControllerByID() {
        final String methodName = "getAccessControllerByID"; // NOI18N
        
        System.out.println(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                document.createComponent(FirstCD.TYPEID_CLASS);
                document.createComponent(FirstCD.TYPEID_CLASS);
                
                assertEquals(TestAccessController.CONTROLER_ID, document.getListenerManager().getAccessController(
                    TestAccessController.class).getControllerID());
            }
        });
    }
    
    /*
     * Those two methods below are tested in DesignEventTest
     * public void testAddDesignListener(){}
     * public void testRemoveDesignListener(){}
     */
    
    /**
     * Test of addPresenterListener, removePresenterListener method, of class org.netbeans.modules.vmd.api.model.ListenerManager.
     * Note: Look also at Standart Output of test for more information
     */
    public void testAddRemovePresenterListener() {
        
        System.out.println("addPresenterListener, removePresenterListener"); // NOI18N
        
        final TestPresenterListener presenterListener1 = new TestPresenterListener(ModelTestUtil.PRESENTER_1_ID);
        final TestPresenterListener presenterListener2 = new TestPresenterListener(ModelTestUtil.PRESENTER_2_ID);
        final TestPresenterListener presenterListener3 = new TestPresenterListener(ModelTestUtil.PRESENTER_3_ID);
        final TestPresenterListener presenterListener4 = new TestPresenterListener(ModelTestUtil.PRESENTER_4_ID);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(SecondCD.TYPEID_CLASS);
                
                document.getListenerManager().addPresenterListener(comp1, TestPresenter1.class, presenterListener1);
                document.getListenerManager().addPresenterListener(comp1, TestPresenter2.class, presenterListener2);
                document.getListenerManager().addPresenterListener(comp1, TestPresenter3.class, presenterListener3);
                document.getListenerManager().addPresenterListener(comp2, TestPresenter4.class, presenterListener4);
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
                compID = comp1.getComponentID();
                comp2.writeProperty(SecondCD.PROPERTY_INT, TypesSupport.createIntegerValue(DesignComponentTest.PROPERTY2_VALUE_INT));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                assertTrue(presenterListener1.isPresenterChangedFlag());
                presenterListener1.setPresenterChangedFlag(false);
                DesignComponent component = document.getComponentByUID(compID);
                document.getListenerManager().removePresenterListener(component , TestPresenter1.class, presenterListener1);
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                document.getComponentByUID(compID).writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY4_VALUE_STRING)); // NOI18N
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                assertFalse(presenterListener1.isPresenterChangedFlag());
            }
        });
    }
}