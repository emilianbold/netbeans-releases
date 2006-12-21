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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.vmd.api.model.common.PrimitiveDescriptorSupport;
import org.netbeans.modules.vmd.api.model.common.TypesSupport;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyValueTest extends TestCase {

    //This values have to equals values from PropertyValue class
    private static final char USER_CODE_ID = 'U'; // NOI18N

    /*
    private static final char NULL_ID = 'N'; // NOI18N
    private static final char REFERENCE_ID = 'R'; // NOI18N
    private static final char VALUE_ID = 'V'; // NOI18N
    private static final char ENUM_ID = 'E'; // NOI18N
    private static final char ARRAY_ID = 'A'; // NOI18N
    private static final char ARRAY_SIZE_SEPARATOR = ':'; // NOI18N
    private static final char ENCODED_LENGTH_SEPARATOR = '_'; // NOI18N
    */
    private DesignDocument document;
    private PrimitiveDescriptor primitveDescritor = new DefaultPrimitiveDescriptor();
        
    
    public PropertyValueTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PropertyValueTest.class);
        
        return suite;
    }
    
    /**
     * Test of createUserCode method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testCreateUserCode() {
        System.out.println("createUserCode"); // NOI18N
        
        String _userCode = " testing user code"; // NOI18N
        PropertyValue.Kind _kind = PropertyValue.Kind.USERCODE;
        String expResult = String.valueOf(this.USER_CODE_ID) + _userCode;
        PropertyValue result = PropertyValue.createUserCode(_userCode);
        
        assertEquals(_kind,result.getKind());
        assertTrue(expResult.equals(result.toString()));
    }
    
    /**
     * Test of createComponentReference method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testCreateComponentReference() {
        System.out.println("createComponentReference"); // NOI18N
       
        final DesignDocument document = ModelTestUtil.createTestDesignDocument("TEST_PROJECT"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent expResult = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent result  = PropertyValue.createComponentReference(expResult).getComponent();
                assertEquals(expResult,result);
            }
        });
    }
    
    /**
     * Test of createNull method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testCreateNull() {
        System.out.println("createNull");// NOI18N
        
        PropertyValue result = PropertyValue.createNull();
        
        assertNotNull(result);
        assertEquals(result.getKind(),PropertyValue.Kind.NULL);
    }
    
    /**
     * Test of createValue method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testCreateValue() {
        System.out.println("createValue"); // NOI18N
 
        TypeID type = new TypeID(TypeID.Kind.PRIMITIVE, "javacode");// NOI18N
        String value = "Test value";// NOI18N
        PropertyValue result = PropertyValue.createValue(new PrimitiveDescriptorSupport().getDescriptorForTypeIDString(TypesSupport.TYPEID_JAVA_LANG_STRING.getString()), type, value);
        assertEquals(PropertyValue.Kind.VALUE,result.getKind());
        assertEquals(type,result.getType());
        assertEquals(value,result.getValue());
    }
    
    /**
     * Test of createArray method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testCreateArray() {
        System.out.println("createArray");// NOI18N
        
        TypeID type = new TypeID(TypeID.Kind.PRIMITIVE, "javacode");// NOI18N
        PrimitiveDescriptor arrayDescriptor = new PrimitiveDescriptor() {
            public Object deserialize(String serialized) {
                return "array"; //NOI18N
            }
            public boolean isValidInstance(Object object) {
                return true;
            }
            public String serialize(Object value) {
                return "array";
            }
        };
        
        PropertyValue arrayPropertyValue = PropertyValue.createValue( arrayDescriptor, type, DesignComponentTest.PROPERTY3_VALUE_STRING);// NOI18N
        List<PropertyValue> array = new ArrayList();
        array.add(arrayPropertyValue);
        
        PropertyValue result = PropertyValue.createArray(type, array);
        
        assertEquals(PropertyValue.Kind.ARRAY, result.getKind());
        type = type.getArrayType();
        assertEquals(type,result.getType());
        assertNotNull(result.getArray());
    }
    
    /**
     * Test of createEmptyArray method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testCreateEmptyArray() {
        System.out.println("createEmptyArray"); // NOI18N
        
        TypeID componentType = new TypeID(TypeID.Kind.COMPONENT,"Root"); // NOI18N
        
        PropertyValue result = PropertyValue.createEmptyArray(componentType);
        List<PropertyValue> expResult = new ArrayList<PropertyValue> (0);
        
        assertEquals(expResult, result.getArray());
    }
    
    /**
     * Test of getKind method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testGetKind() {
        System.out.println("getKind"); // NOI18N
        
        PropertyValue.Kind expResult = PropertyValue.Kind.VALUE;
        PropertyValue.Kind result;
        String value = "Test value"; // NOI18N
        TypeID type = new TypeID(TypeID.Kind.PRIMITIVE, "javacode");// NOI18N
        PrimitiveDescriptor javacodeDescriptor = new PrimitiveDescriptor() {
            public Object deserialize(String serialized) {
                return "javacode"; //NOI18N
            }
            public boolean isValidInstance(Object object) {
                return true;
            }
            public String serialize(Object value) {
                return "javacode"; //NOI18N
            }
        };
        result = PropertyValue.createValue(javacodeDescriptor, type, value).getKind();
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getType method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testGetType() {
        System.out.println("getType"); // NOI18N
        
        TypeID expResult;
        TypeID result;
        String value = "Test value"; // NOI18N
        TypeID type = new TypeID(TypeID.Kind.PRIMITIVE, "javacode");// NOI18N
        
        result = PropertyValue.createValue(primitveDescritor, type,value).getType();
        expResult = type;
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getUserCode method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testGetUserCode() {
        System.out.println("getUserCode"); // NOI18N
        
        String userCode = "User Code;"; // NOI18N
        String result = PropertyValue.createUserCode(userCode).getUserCode();
        String expResult = userCode;
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getComponent method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testGetComponent() {
        System.out.println("getComponent"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent result = PropertyValue.createComponentReference(comp).getComponent();
                DesignComponent expResult = comp;
                
                assertEquals(expResult,result);
            }
        });
        
    }
    
    /**
     * Test of getValue method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testGetValue() {
        System.out.println("getValue"); // NOI18N
        
        PropertyValue result;
        TypeID type = new TypeID(TypeID.Kind.PRIMITIVE, "javacode");// NOI18N
        String value = "Test value";
        
        result = PropertyValue.createValue(primitveDescritor, type, value);
        
        assertEquals(PropertyValue.Kind.VALUE,result.getKind());
        assertEquals(type,result.getType());
        assertEquals(value,result.getValue());
    }
    
    /**
     * Test of getArray method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testGetArray() {
        System.out.println("getArray"); // NOI18N
        
        TypeID type = new TypeID(TypeID.Kind.PRIMITIVE, "javacode");// NOI18N
        PropertyValue arrayPropertyValue = PropertyValue.createValue(primitveDescritor, type, DesignComponentTest.PROPERTY1_VALUE_STRING); // NOI18N
        List<PropertyValue> array = new ArrayList();
        
        array.add(arrayPropertyValue);
        
        PropertyValue result = PropertyValue.createArray(type,array);
        
        type = type.getArrayType();
        assertEquals(type,result.getType());
        assertNotNull(result.getArray());
    }
    
    
    /**
     * Test of isCompatible method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    public void testIsCompatible() {
        System.out.println("isCompatible"); // NOI18N
        
        TypeID requiredType = TYPEID_JAVA_LANG_STRING; // NOI18N
        PropertyValue instance = TypesSupport.createStringValue(FirstCD.PROPERTY_TEST); // NOI18N
        boolean expResult = true;
        boolean result = instance.isCompatible(requiredType);
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of serialize method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    //TODO Improve this test right know testing only if serialization works, doesn't test particular values
    public void testSerialize() {
        System.out.println("serialize and toString"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp = document.createComponent(FirstCD.TYPEID_CLASS);
                
                document.setRootComponent(comp);
                String result = comp.readProperty(FirstCD.PROPERTY_TEST).getValue().toString(); // NOI18N
                assertNotNull(result);
            }
        });
        
    }
    
    /**
     * Test of deserialize method, of class org.netbeans.modules.vmd.api.model.PropertyValue.
     */
    //TODO Improve this test right know testing only if desarialization works, doesn't test all Kinds
    public void testDeserialize() {
        System.out.println("deserialize"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp = document.createComponent(FirstCD.TYPEID_CLASS);
                
                document.setRootComponent(comp);
                
                String result = PropertyValue.deserialize(comp.getReferenceValue().serialize(),document,comp.getType()).serialize();
                String expResult = comp.getReferenceValue().serialize();
                
                assertNotNull(result);
                assertEquals(expResult,result);
            }
        });
    }
    
    private class DefaultPrimitiveDescriptor implements PrimitiveDescriptor {
        public String serialize(Object value) {
            return "default"; //NOI18N
        }

        public Object deserialize(String serialized) {
            return "default"; //NOI18N
        }

        public boolean isValidInstance(Object object) {
            return true; 
        }
        
    }
}
