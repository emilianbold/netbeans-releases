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

import java.util.Collection;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.descriptors.SecondCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

/**
 *
 * @author Karol Harezlak
 */
public class DesignDocumentTest extends TestCase {

    private  DesignDocument document;

    public DesignDocumentTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DesignDocumentTest.class);
        
        return suite;
    }
    
    /**
     * Test of getDescriptorRegistry method, of class org.netbeans.modules.vmd.api.model.DesignDocument.
     */
    public void testGetDescriptorRegistry() {
        System.out.println("getDescriptorRegistry"); // NOI18N
        
        final DescriptorRegistry result = document.getDescriptorRegistry();
        
        assertNotNull(result);
    }
    
    /**
     * Test of getListenerManager method, of class org.netbeans.modules.vmd.api.model.DesignDocument.
     */
    public void testGetListenerManager() {
        System.out.println("getListenerManager"); // NOI18N
        
        ListenerManager listenerManager = document.getListenerManager();
        assertNotNull(listenerManager);
        DesignListener listener = new DesignListener(){
            public void designChanged(DesignEvent event) {
                //TODO Fill it with code
            }
        };
        
        assertNotNull(listenerManager);
        
        //Additional test
        //TODO Expand this part of some real situatuon with listeneres, documents and components
        listenerManager.addDesignListener(listener,new DesignEventFilter());
        listenerManager.removeDesignListener(listener);
        
    }
    
    /**
     * Test of getTransactionManager method, of class org.netbeans.modules.vmd.api.model.DesignDocument.
     */
    public void testGetTransactionManager() {
        System.out.println("getTransactionManager"); // NOI18N1
        
        final long compID = 0;
        TransactionManager result = document.getTransactionManager();
        assertNotNull(result);
        result.writeAccess(new Runnable() {
            public void run() {
                document.createComponent(FirstCD.TYPEID_CLASS);
            }
        });
        result.readAccess(new Runnable() {
            public void run() {
                document.getComponentByUID(compID);
            }
        });
    }
    
    /**
     * Test of getDeleteComponent method, of class org.netbeans.modules.vmd.api.model.DesignDocument.
     */
    
    @SuppressWarnings("deprecation") // NOI18N
    public void testDeleteComponent(){
        //TODO Right now there is no way to tested if document is deleted or not. This test only check if there is any exception rise when component is deleted
        System.out.println( "deleteComponent"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                DesignComponent comp = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(SecondCD.TYPEID_CLASS);
                document.setRootComponent(comp);
                comp.addComponent(comp2);
                
                document.deleteComponent(comp2);
            }
        });
    }
    
    
    /**
     * Test of getSelectedComponents method, of class org.netbeans.modules.vmd.api.model.DesignDocument.
     */
    public void testGetSetSelectedComponents() {
        System.out.println("getSetSelectedComponents, setSelectedComponents, getSelectionSourceID"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                String sourceID = "TestSorceId"; // NOI18N
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(SecondCD.TYPEID_CLASS);
                
                Collection<DesignComponent> selectedComponents = new HashSet<DesignComponent>();
                selectedComponents.add(comp1);
                selectedComponents.add(comp2);
                document.setSelectedComponents(sourceID,selectedComponents); // NOI18N
                for (DesignComponent comp: selectedComponents){
                    boolean result = document.getSelectedComponents().contains(comp);
                    assertTrue(result);
                }
                String result = document.getSelectionSourceID();
                String expResult = sourceID;
                assertEquals(expResult,result);
            }
        });
    }
    
    /**
     * Complex test (addDocument, addComponent, getRootComponent, setRootComponent, createComponent, of class org.netbeans.modules.vmd.api.model.DesignDocument.
     */
    public void testComplex() {
        System.out.println("addDocument, addComponent, getRootComponent, setRootComponent," + //NOI18N
                " createComponent,getComponentByID" + "getComponentByID"); // NOI18N
        
        final DesignDocument instance = ModelTestUtil.createTestDesignDocument(ModelTestUtil.PROJECT_ID);
        
        instance.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = instance.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = instance.createComponent(SecondCD.TYPEID_CLASS);
                
                //setRooComponent, addComponent
                instance.setRootComponent(comp1);
                comp1.addComponent(comp2);
                //getComponentByID
                DesignComponent resultComp2ByID = instance.getComponentByUID(comp2.getComponentID());
                DesignComponent expComp2 = comp2;
                assertEquals(expComp2,resultComp2ByID);
                //setRootComponent getRootComponent
                DesignComponent expGetComp = comp1;
                DesignComponent resultGetComp = instance.getRootComponent();
                assertEquals(expGetComp,resultGetComp);
                // writeProperty to Component
                comp1.writeProperty(FirstCD.PROPERTY_REFERENCE, PropertyValue.createComponentReference(comp2));
            }
        });
    }
}
