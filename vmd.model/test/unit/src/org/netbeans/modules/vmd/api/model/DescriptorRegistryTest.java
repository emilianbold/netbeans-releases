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

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

/**
 *
 * @author Karol Harezlak
 */
public class DescriptorRegistryTest extends TestCase {

    private DesignDocument document;

    public DescriptorRegistryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DescriptorRegistryTest.class);
        
        return suite;
    }

    /**
    * Test of readAccess method, of class org.netbeans.modules.vmd.api.model.DescriptorRegistry.
    */
    public void testReadAccessGetComponentDescriptor() {
        String methodName = "readAccess, getComponentDescriptor"; // NOI18N
        System.out.println(methodName);

        document.getDescriptorRegistry().readAccess(new Runnable() {
            public void run() {
               document.getDescriptorRegistry().getComponentDescriptor(FirstCD.TYPEID_CLASS);
               System.out.println(document.getDescriptorRegistry().getComponentDescriptor(FirstCD.TYPEID_CLASS));
            }
        });
    }

    /**
     * Test of assertComponentDescriptors method, of class org.netbeans.modules.vmd.api.model.DescriptorRegistry.
     */
    //TODO This method ( DescriptorRegistry -> assertComponentDescriptors) not finished yet. 
    public void testAssertComponentDescriptors() {
        System.out.println("assertComponentDescriptors"); // NOI18N
        
         HashSet<TypeID> typeids = new HashSet<TypeID> ();
         typeids.add(FirstCD.TYPEID_CLASS);
         document.getDescriptorRegistry().assertComponentDescriptors (typeids);   
    }
}
