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

import org.netbeans.modules.vmd.api.model.descriptors.CantDeriveCD;
import org.netbeans.modules.vmd.api.model.descriptors.CantInstantiateCD;
import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.descriptors.SuperFirstCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

/**
 *
 * @author Karol Harezlak
 */
public class TypeDescriptorTest extends TestCase {
    
    private DesignDocument document;
    
    public TypeDescriptorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TypeDescriptorTest.class);
        
        return suite;
    }
    
    /**
     * Test of getSuperType method, of class org.netbeans.modules.vmd.api.model.TypeDescriptor.
     */
    public void testGetSuperType() {
        System.out.println("getSuperType"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                assertEquals(SuperFirstCD.TYPEID_CLASS,comp1.getComponentDescriptor().getTypeDescriptor().getSuperType());
            }
        });
    }
    
    /**
     * Test of getThisType method, of class org.netbeans.modules.vmd.api.model.TypeDescriptor.
     */
    public void testGetThisType() {
        System.out.println("getThisType"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                assertNotNull(comp1.getComponentDescriptor().getTypeDescriptor().getThisType());
                assertEquals(FirstCD.TYPEID_CLASS,comp1.getComponentDescriptor().getTypeDescriptor().getThisType());
            }
        });
    }
    
    /**
     * Test of isCanInstantiate method, of class org.netbeans.modules.vmd.api.model.TypeDescriptor.
     */
    public void testIsCanInstantiate() {
        System.out.println("isCanInstantiate"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp2 = null;
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                assertNotNull(comp1);
                assertTrue(comp1.getComponentDescriptor().getTypeDescriptor().isCanInstantiate());
                try {
                    comp2 = document.createComponent(CantInstantiateCD.TYPEID_CLASS);
                }catch(AssertionError as){
                    //TODO Empty
                }
                assertNull(comp2);
            }
        });
    }
    
    /**
     * Test of isCanDerive method, of class org.netbeans.modules.vmd.api.model.TypeDescriptor.
     * NOTE: Also check standart output for warnings!
     */
    public void testIsCanDerive() {
        System.out.println("isCanDerive"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = null;
                try {
                    comp1 = document.createComponent(CantDeriveCD.TYPEID_CLASS);
                }catch(AssertionError as){
                    as.printStackTrace();
                }
                assertNull(comp1);
            }
        });
    }
}
