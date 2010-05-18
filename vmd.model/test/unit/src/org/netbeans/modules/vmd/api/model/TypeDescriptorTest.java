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
