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
