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

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.descriptors.SecondCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @auth Karol Harezlak
 */
public class DesignComponentTest extends TestCase {

    private DesignDocument document;

    private final long comp1ID = 0;
    private final long comp2ID = 1;
    
    public static final String PROPERTY1_VALUE_STRING = "VALUE1"; // NOI18N
    public static final Integer PROPERTY2_VALUE_INT = new Integer(2); // NOI18N
    public static final String PROPERTY3_VALUE_STRING = "VALUE3"; // NOI18N
    public static final String PROPERTY4_VALUE_STRING = "VALUE4"; // NOI18N
    public static final String PROJECT_TYPE = "testProject"; //NOI18N
    
    public DesignComponentTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DesignComponentTest.class);
        
        return suite;
    }
    
    /**
     * Test of getComponentDescriptor method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testGetComponentDescriptor() {
        System.out.println("getComponentDescriptor"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                ComponentDescriptor result = document.createComponent(FirstCD.TYPEID_CLASS).getComponentDescriptor();
                
                assertNotNull(result);
                assertEquals(FirstCD.TYPEID_CLASS.toString(),result.getTypeDescriptor().getThisType().toString());
            }
        });
        
    }
    
    /**
     * Test of getDocument method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testGetDocument() {
        System.out.println("getDocument"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignDocument result = document.createComponent(FirstCD.TYPEID_CLASS).getDocument();
                DesignDocument expResult = document;
                assertEquals(expResult, result);
            }
        });
    }
    
    /**
     * Test of getComponentID method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testGetComponentID() {
        System.out.println("getComponentID"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                long result = document.createComponent(FirstCD.TYPEID_CLASS).getComponentID();
                long expResult  = comp1ID;
                assertEquals(expResult, result);
            }
        });
    }
    
    /**
     * Test of getType method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testGetType() {
        System.out.println("getType"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                TypeID  result = document.createComponent(FirstCD.TYPEID_CLASS).getType();
                TypeID expResult  = FirstCD.TYPEID_CLASS;
                assertEquals(expResult, result);
            }
        });
    }
    
    /**
     * Test of getParentComponent method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testGetParentComponent() {
        System.out.println("getParentComponent"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(SecondCD.TYPEID_CLASS);
                DesignComponent result = comp1.getParentComponent();
                
                assertNull(result);
                comp1.addComponent(comp2);
                result = comp2.getParentComponent();
                DesignComponent  expResult = comp1;
                assertEquals(expResult,result);
            }
        });
    }
    
    /**
     * Test of removeComponent method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    //TODO Strange you can add component to itself !!
    public void testAddtRemoveComponent() {
        System.out.println("addComponent, removeComponent"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(SecondCD.TYPEID_CLASS);
                
                //addComponent
                comp1.addComponent(comp2);
                DesignComponent result = comp2.getParentComponent();
                DesignComponent expResult = comp1;
                assertEquals(result,expResult);
                comp1.removeComponent(comp2);
                //Check if comp2 has been remeved from comp1 by checking if he has paerent componet.
                result = comp2.getParentComponent();
                assertNull(result);
                //Check if comp1 has any components
                assertTrue(comp1.getComponents().size()==0);
            }
        });
    }
    
    /**
     * Test of getComponents method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testGetComponents() {
        System.out.println("getComponents"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                final int numberOfComp = 1;
                
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(SecondCD.TYPEID_CLASS);
                comp1.addComponent(comp2);
                //Check if comp1 has added component
                assertTrue(comp1.getComponents().size()==numberOfComp);
            }
        });
    }
    
    /**
     * Test of writeProperty method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testReadWriteProperty() {
        System.out.println("readProperty, writeProperty"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(SecondCD.TYPEID_CLASS);
                
                // writeProperty to Component
                comp1.writeProperty(FirstCD.PROPERTY_TEST, PropertyValue.createValue(PrimitiveDescriptorFactoryRegistry.getDescriptor(PROJECT_TYPE, TYPEID_JAVA_LANG_STRING), TYPEID_JAVA_LANG_STRING, PROPERTY1_VALUE_STRING));
                comp2.writeProperty(SecondCD.PROPERTY_INT, PropertyValue.createValue(PrimitiveDescriptorFactoryRegistry.getDescriptor(PROJECT_TYPE, TYPEID_INT), TYPEID_INT, PROPERTY2_VALUE_INT));
                
            }
        });
        
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                String result1 = (String) document.getComponentByUID(comp1ID).readProperty(FirstCD.PROPERTY_TEST).getValue();
                String expResult1 = PROPERTY1_VALUE_STRING;
                
                assertEquals(expResult1,result1);
                Integer result2 = (Integer) document.getComponentByUID(comp2ID).readProperty(SecondCD.PROPERTY_INT).getValue();
                Integer expResult2 = PROPERTY2_VALUE_INT;
                assertEquals(expResult2,result2);
            }
        });
    }
    
    /**
     * Test of isDefaultValue method, of class org.netbeans.modules.vmd.api.model.DesignComponent.
     */
    public void testIsDefaultValueResetToDefault() {
        System.out.println("isDefaultValue, resetToDefault"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1.writeProperty(FirstCD.PROPERTY_TEST, PropertyValue.createValue(PrimitiveDescriptorFactoryRegistry.getDescriptor(PROJECT_TYPE, TYPEID_JAVA_LANG_STRING), TYPEID_JAVA_LANG_STRING,"someDifferentValue")); // NOI18N
                
                boolean result = false;
                try {
                    result = comp1.isDefaultValue(FirstCD.PROPERTY_TEST);
                } catch(Exception ex){
                    ex.printStackTrace();
                }
                boolean expResult = false;
                
                assertEquals(expResult,result);
                comp1.resetToDefault(FirstCD.PROPERTY_TEST);
                result = comp1.isDefaultValue(FirstCD.PROPERTY_TEST);
                expResult = true;
                assertEquals(expResult,result);
            }
        });
    }
}
