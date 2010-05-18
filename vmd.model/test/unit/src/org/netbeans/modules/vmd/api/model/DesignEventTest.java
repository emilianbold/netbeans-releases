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
import org.netbeans.modules.vmd.api.model.common.TypesSupport;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.listeners.TestDesignListener;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @author Karol Harezlak
 */
public class DesignEventTest extends TestCase {
    private DesignDocument document;
    private Long comp1ID = null;
    private Long comp2ID = null;
    private Long comp3ID = null;
    private Long comp4ID = null;
    
    public DesignEventTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DesignEventTest.class);
        return suite;
    }
    
    /**
     * Test of getEventID method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    public void testGetEventID() {
        System.out.println("getEventID"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                final DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                document.getListenerManager().addDesignListener(new DesignListener() {
                    public void designChanged(DesignEvent event) {
                        boolean result = event.getEventID()>=0;
                        assertTrue(result);
                    }
                },new DesignEventFilter().addComponentFilter(comp1,false));
                // rising events
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY1_VALUE_STRING)); // NOI18N
            }
        });
    }
    
    /**
     * Test of getFullyAffectedComponents method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    public void testGetFullyAffectedComponents() {
        final String methodName = ("getFullyAffectedComponents"); // NOI18N
        System.out.println(methodName);
        final TestDesignListener designListener = new TestDesignListener(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //create components
                final DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1ID = comp1.getComponentID();
                final DesignComponent comp2 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp2ID = comp2.getComponentID();
                document.getListenerManager().addDesignListener(designListener,new DesignEventFilter().addComponentFilter(comp1,false));
                // rising events
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY1_VALUE_STRING));
                comp2.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING));
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        //there should be two fully affected components during this writeAccess
        assertTrue(designListener.getEvent().getFullyAffectedComponents().size()==2);
        //Event shouldntbe rised after following writeProperty method
        designListener.setDesignChangeFlag(false);
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //checking if comp1 is in the collection of fully affected components
                assertTrue(designListener.getEvent().getFullyAffectedComponents().contains(document.getComponentByUID(comp1ID)));                
                DesignComponent comp2 = document.getComponentByUID(comp2ID);
                //Event shouldnbe rised after following writeProperty method
                comp2.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY4_VALUE_STRING));
            }
        });
        //Check if event has been rised shouldn be in last writeAccess!
        assertFalse(designListener.isDesignChangeFlag());
        cleanUpCompIDs();
    }

    /**
     * Test of getFullyAffectedHierarchies method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    public void testGetFullyAffectedHierarchies_getPartlyAffectedHierarchies() {
        final String methodName =  "getFullyAffectedHierarchies, getPartlyAffectedHierarchies"; // NOI18N
        final TestDesignListener designListener = new TestDesignListener(methodName);
       
        System.out.println(methodName);
        
        //PART I Checking fully affected hierahy
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                final DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1ID = comp1.getComponentID();
                final DesignComponent comp2 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp2ID = comp2.getComponentID();
                final DesignComponent comp3 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp3ID = comp3.getComponentID();
                final DesignComponent comp4 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp4ID = comp4.getComponentID();
                
                document.setRootComponent(comp1);
                document.getListenerManager().addDesignListener(designListener,new DesignEventFilter().addHierarchyFilter(comp1,true));
                // rising events
                comp1.addComponent(comp2);
                comp2.addComponent(comp3);
                comp3.addComponent(comp4);
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        //Testing fully affected components
        assertTrue(designListener.getEvent().getFullyAffectedHierarchies().size() == 4);
        //PART II Checking partly affected hierahy
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                designListener.setDesignChangeFlag(false);
                DesignComponent comp1 = document.getComponentByUID(comp1ID);
                DesignComponent comp2 = document.getComponentByUID(comp2ID);
                //checking if comp1 is in the collection of fully affected components
                comp1.removeComponent(comp2);
                Debug.dumpDocument(document);
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        //Testing fully affected components
        assertTrue(designListener.getEvent().getFullyAffectedHierarchies().size() == 2);
        //Testing fully affected components
        assertTrue(designListener.getEvent().getPartlyAffectedHierarchies().size() == 4);
        cleanUpCompIDs();
    }
    
    /**
     * Test of getPartlyAffectedComponents method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    public void testGetPartlyAffectedComponents() {
        final String methodName = "getPartlyAffectedComponents"; // NOI18N
        final TestDesignListener designListener = new TestDesignListener(methodName);
        
        System.out.println(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1ID = comp1.getComponentID();
                DesignComponent comp2 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp2ID = comp2.getComponentID();
                document.getListenerManager().addDesignListener(designListener,new DesignEventFilter().addComponentFilter(comp2,true));
                document.setRootComponent(comp1);
                comp1.addComponent(comp2);
                // rising event
                comp2.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING)); // NOI18N
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        //Checking size of collection
        assertTrue(designListener.getEvent().getPartlyAffectedComponents().size()==2);
        //there should be only one fully affected component during this writeAccess
        assertTrue(designListener.getEvent().getFullyAffectedComponents().size()==1);
        //Event shouldnt be rised after following writeProperty method
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(comp1ID);
                DesignComponent comp2 = document.getComponentByUID(comp2ID);
                
                //checking if comp1 is in the collection of partly affected components
                assertTrue(designListener.getEvent().getPartlyAffectedComponents().contains(comp1));
                //Checking fully affected components
                assertTrue(designListener.getEvent().getFullyAffectedComponents().contains(comp2));
                //clean up designListener
                designListener.setDesignChangeFlag(false);
                //rising new events
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING)); // NOI18N
            }
        });
        //check if event has been raised, it shouldnt be cause it's only partly affected component
        assertFalse(designListener.isDesignChangeFlag());
        cleanUpCompIDs();
    }
    
    /**
     * Test of getDescriptorChangedComponents method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    public void testGetDescriptorChangedComponents() {
        final String methodName = "getDescriptorChangedComponents"; // NOI18N
        final TestDesignListener designListener = new TestDesignListener(methodName);
        
        System.out.println(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                // rising events
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(FirstCD.TYPEID_CLASS);
                
                document.getListenerManager().addDesignListener(designListener,new DesignEventFilter().addDescriptorFilter(comp1));
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        //Checking getDescriptorChangedComponents()
        assertTrue(designListener.getEvent().getDescriptorChangedComponents().size()==2);
    }
    
    /**
     * Test of getOldPropertyValue method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    
    public void testGetOldPropertyValue() {
        String methodName = "getOldPropertyValue"; // NOI18N
        final TestDesignListener designListener = new TestDesignListener(methodName);
        
        System.out.println(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1ID = comp1.getComponentID();
                document.getListenerManager().addDesignListener(designListener,new DesignEventFilter().addComponentFilter(comp1,true));
                // rising events
                comp1.writeProperty(FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY4_VALUE_STRING));
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                DesignComponent comp1 = document.getComponentByUID(comp1ID);
                //Check value
                assertTrue(comp1.readProperty(FirstCD.PROPERTY_TEST).getValue() == DesignComponentTest.PROPERTY4_VALUE_STRING); 
                comp1.resetToDefault(FirstCD.PROPERTY_TEST);
                //Checking if oldValue equals deafult value of instance Name property
                assertTrue(comp1.readProperty(FirstCD.PROPERTY_TEST).getValue() == 
                        designListener.getEvent().getOldPropertyValue(comp1,FirstCD.PROPERTY_TEST).getValue()); 
            }
        });
        cleanUpCompIDs();
    }
    
    /**
     * Test of isComponentPropertyChanged method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     * Using DescentFilter and ComponentFilter
     */
    public void testIsComponentPropertyChanged() {
        String methodName = "isComponentPropertyChanged"; // NOI18N
        final TestDesignListener listenerComponentFilter = new TestDesignListener(methodName);
        final TestDesignListener listenerDescentFilter = new TestDesignListener(methodName);
        
        System.out.println(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp1ID = comp1.getComponentID();
                DesignComponent comp2 = document.createComponent(FirstCD.TYPEID_CLASS);
                comp2ID = comp2.getComponentID();
                document.getListenerManager().addDesignListener(listenerComponentFilter,
                        new DesignEventFilter().addComponentFilter(comp1,true));           
                //TODO event for DescentFilter doesnt work
                document.getListenerManager().addDesignListener(listenerDescentFilter,
                        new DesignEventFilter().addDescentFilter(comp1,FirstCD.PROPERTY_TEST)); 
               comp1.writeProperty (FirstCD.PROPERTY_TEST, TypesSupport.createStringValue(DesignComponentTest.PROPERTY3_VALUE_STRING)); 
            }
        });
        //Check if event has been rised
        assertTrue(listenerComponentFilter.isDesignChangeFlag());
        assertTrue(listenerDescentFilter.isDesignChangeFlag());
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                DesignComponent comp1 = document.getComponentByUID(comp1ID);
                DesignComponent comp2 = document.getComponentByUID(comp2ID);
                
                //check if property of comp1 has been changed
                assertTrue(listenerComponentFilter.getEvent().isComponentPropertyChanged(comp1,FirstCD.PROPERTY_TEST));
                //check if property of comp2 has NOT been changed
                assertFalse(listenerComponentFilter.getEvent().isComponentPropertyChanged(comp2,FirstCD.PROPERTY_TEST)); 
                //check if property of comp1 has been changed
                assertTrue(listenerDescentFilter.getEvent().isComponentPropertyChanged(comp1,FirstCD.PROPERTY_TEST)); 
                //check if property of comp2 has NOT been changed
                assertFalse(listenerDescentFilter.getEvent().isComponentPropertyChanged(comp2,FirstCD.PROPERTY_TEST)); 
            }
        });
        cleanUpCompIDs();
    }
    
    /**
     * Test of isSelectionChanged method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    public void testIsSelectionChanged() {
        final String methodName = "isSelectionChanged"; // NOI18N
        final TestDesignListener designListener = new TestDesignListener(methodName);
        
        System.out.println(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(FirstCD.TYPEID_CLASS);

                document.getListenerManager().addDesignListener(designListener,new DesignEventFilter().setSelection(true));
                // rising events
                Collection<DesignComponent> selectedComponents = new HashSet<DesignComponent>();
                selectedComponents.add(comp1);
                document.setSelectedComponents("Test_Source_ID",selectedComponents); // NOI18N
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        //Check if selection changed
        assertTrue(designListener.getEvent().isSelectionChanged());
    }
    
    /**
     * Test of isStructureChanged method, of class org.netbeans.modules.vmd.api.model.DesignEvent.
     */
    public void testIsStructureChanged() {
        final String methodName = "   isStructureChanged"; // NOI18N
        final TestDesignListener designListener = new TestDesignListener(methodName);
        
        System.out.println(methodName);
        
        document.getTransactionManager().writeAccess(new Runnable() {
            public void run() {
                //createComponent
                DesignComponent comp1 = document.createComponent(FirstCD.TYPEID_CLASS);
                DesignComponent comp2 = document.createComponent(FirstCD.TYPEID_CLASS);
                document.getListenerManager().addDesignListener(designListener, new DesignEventFilter().setGlobal(true));
                // rising events
                document.setRootComponent(comp2);
                comp2.addComponent(comp1);
            }
        });
        //Check if event has been rised
        assertTrue(designListener.isDesignChangeFlag());
        //Check if selection changed
        assertTrue(designListener.getEvent().isStructureChanged());
    }
    
    private void cleanUpCompIDs(){
        comp1ID = null;
        comp2ID = null;
        comp3ID = null;
        comp4ID = null;
    }
}
