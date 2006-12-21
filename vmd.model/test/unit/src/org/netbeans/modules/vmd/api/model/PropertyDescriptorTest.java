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
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyDescriptorTest extends TestCase {

    private static final TypeID typeID = TYPEID_JAVA_LANG_STRING;
    private static final String name = "testName"; // NOI18N
    
    private PropertyValue propertyValue = TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING);
    private DesignDocument document;
    
    public PropertyDescriptorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PropertyDescriptorTest.class);
        
        return suite;
    }
    
    /**
     * Test of getName method, of class org.netbeans.modules.vmd.api.model.PropertyDescriptor.
     */
    public void testGetNameGetType() {
        System.out.println("getName, getType"); // NOI18N
        
        PropertyDescriptor instance = new PropertyDescriptor(name, typeID, propertyValue, true, true, Versionable.FOREVER);
        // getName
        assertEquals(name,instance.getName());
        // getTypeID
        assertEquals(typeID,instance.getType());
    }
    
    /**
     * Test of isReadOnly method, of class org.netbeans.modules.vmd.api.model.PropertyDescriptor.
     */
    //TODO If you try to write property on readOnly property it should throw exception
    public void testIsReadOnly() {
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                //checking if property descriptor is ReadOnly
                assertTrue(comp1.getComponentDescriptor().getPropertyDescriptor(FirstCD.DEFAULT_VALUE_READ_ONLY).isReadOnly());
                //trying to write on read only property
                String defaultValue = comp1.readProperty(FirstCD.DEFAULT_VALUE_READ_ONLY).getValue().toString();
                try {
                    comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
                }catch(AssertionError as){
                    //TODO Empty
                }
                assertEquals(defaultValue,comp1.readProperty(FirstCD.PROPERTY_READ_ONLY).getValue().toString());
            }
        });
    }
    
    /**
     * Test of createDefaultValue method, of class org.netbeans.modules.vmd.api.model.PropertyDescriptor.
     */
    //TODO Test for this method not done yet, method createDefaultValue not fully functional
    public void testCreateDefaultValue() {
        System.out.println("createDefaultValue"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
    }
}