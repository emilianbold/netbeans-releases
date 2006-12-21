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