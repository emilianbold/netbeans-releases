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
import org.netbeans.modules.vmd.api.model.common.TypesSupport;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter;
import org.netbeans.modules.vmd.api.model.presenters.TestPresenter1;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @author Karol Harezlak
 */
public class PresenterTest extends TestCase {
    
    private DesignDocument document;
    
    public PresenterTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PresenterTest.class);
        
        return suite;
    }
    
    /**
     * Test of getEventFilter method, of class org.netbeans.modules.vmd.api.model.Presenter.
     */
    public void testGetEventFilter() {
        System.out.println("getEventFilter"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(0);
                
                assertNotNull(comp1.getPresenter(TestPresenter1.class).getEventFilter());
                assertTrue(comp1.getPresenter(TestPresenter1.class).getEventFilter() instanceof DesignEventFilter);
            }
        });
    }
    
    /**
     * Test of designChanged method, of class org.netbeans.modules.vmd.api.model.Presenter.
     */
    public void testDesignChanged() {
        System.out.println("designChanged"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(0);
                TestPresenter presenter = (TestPresenter) comp1.getPresenter(TestPresenter1.class);
                
                assertTrue(presenter.isDesignChangedFlag());
            }
        });
    }
    
    /**
     * Test of presenterChanged method, of class org.netbeans.modules.vmd.api.model.Presenter.
     */
    public void testPresenterChanged() {
        System.out.println("presenterChanged"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(0);
                TestPresenter presenter = comp1.getPresenter(TestPresenter1.class);
                
                assertTrue(presenter.isPresenterChangedFlag());
            }
        });
    }
}
