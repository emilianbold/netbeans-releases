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

import org.netbeans.modules.vmd.api.model.TypeID;
import org.netbeans.modules.vmd.api.model.TypeID.Kind;

/**
 *
 * @author Karol Harezlak
 */
public class TypeIDTest extends TestCase {

    /* Enum ID represents values of following variables
     *  PRIMITIVE_ID = 'P'
     *  ENUM_ID = 'E';
     *  COMPONENT_ID = 'C';
     */
    private enum ID {
        P, E, C
    }
    //Environment
    private Kind kind ;
    private String string;
    private String decode;
    private String encode;
    private String encodedString ;
    private String id ;
    private int dimension;
    
    private TypeID instance = null;
    
    public TypeIDTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        kind = TypeID.Kind.COMPONENT;
        encodedString = "C#Root"; // NOI18N
        string = "#Root"; // NOI18N
        encode = "1CC"; // NOI18N
        id = "C"; // NOI18N
        decode = "C"; // NOI18N
        dimension =1;  // NOI18N
        instance = new TypeID(kind,id,dimension);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(TypeIDTest.class);
        return suite;
    }
    
    /**
     * Test of constructor, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testTypeIDStringConstructor(){
        System.out.println("TypeID Constructor (String string)"); // NOI18N
        
        TypeID _instance = new TypeID(TypeID.Kind.COMPONENT, encodedString);
        assertNotNull(_instance);
    }
    
    /**
     * Test of constructor, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testTypeIDConstructors(){
        System.out.println("TypeID Constructors (Kind kind, String id, int dimension) and (Kind kind, String id)"); // NOI18N
        
        int _dimension = 2; // Counter from 0 dimension value
        TypeID _instance =null;
        
        for (TypeID.Kind _kind: TypeID.Kind.values()){
            for (ID _id: ID.values() ){
                for (int i=0;i<=_dimension;i++){
                    _instance = new TypeID(_kind,_id.toString(),i);
                    assertNotNull(_instance);
                }
            }
        }
        
    }
    
    /**
     * Test of getKind method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testGetKind() {
        System.out.println("getKind"); // NOI18N
        
        TypeID.Kind result= instance.getKind();
        TypeID.Kind expResult = kind;
        
        assertEquals(result,expResult);
    }
    
    /**
     * Test of getString method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testGetString() {
        System.out.println("getString"); // NOI18N
        
        String result = instance.getString();
        String expResult = decode;
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getDimension method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testGetDimension() {
        System.out.println("getDimension"); // NOI18N
        
        int expResult = dimension;
        int result = instance.getDimension();
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getEncoded method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testGetEncoded() {
        System.out.println("getEncoded"); // NOI18N
        
        String expResult = encode;
        String result = instance.getEncoded();
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getComponentTypeID method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testGetComponentType() {
        System.out.println("getComponentTypeID"); // NOI18N
        
        TypeID expResult= new TypeID(kind,id,dimension - 1);
        TypeID result = instance.getComponentType();
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of getArrayType method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testGetArrayType() {
        System.out.println("getArrayType"); // NOI18N
        
        TypeID expResult= new TypeID(kind,id,dimension + 1);
        TypeID result = instance.getArrayType();
        
        assertEquals(expResult, result);
    }
    
    /**
     * Test of equals method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testEquals() {
        System.out.println("equals"); // NOI18N
        
        TypeID result = new TypeID(TypeID.Kind.COMPONENT, string);
        TypeID expResult = new TypeID(TypeID.Kind.COMPONENT,string,0);
        result.equals(null);
        
        assertTrue(result.equals(expResult));
    }
    
    /**
     * Test of hashCode method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testHashCode() {
        System.out.println("hashCode"); // NOI18N
        
        TypeID result = new TypeID(TypeID.Kind.COMPONENT, string);
        TypeID expResult = new TypeID(TypeID.Kind.COMPONENT,string,0);
        
        assertTrue(result.hashCode() == expResult.hashCode());
    }
    
    /**
     * Test of toString method, of class org.netbeans.modules.vmd.api.model.TypeID.
     */
    public void testToString() {
        System.out.println("toString"); // NOI18N
        
        TypeID result = new TypeID(TypeID.Kind.COMPONENT, string);
        TypeID expResult = new TypeID(TypeID.Kind.COMPONENT, string, 0);
        
        assertTrue(result.getString().equals(expResult.getString()));
    }
    
}
