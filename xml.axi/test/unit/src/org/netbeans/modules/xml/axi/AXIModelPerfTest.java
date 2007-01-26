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

package org.netbeans.modules.xml.axi;

import junit.framework.*;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

        
/**
 * The test measures the performance by creating an AXI tree for all
 * elements in the OTA schema. See reverseEngineer().
 * 1. Run it by making AXIModelBuilder.makeSharable as false.
 * 2. Run it by making AXIModelBuilder.makeSharable as true.
 * See the difference in numbers.
 *
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelPerfTest extends AbstractTestCase {
            
    public static final String TEST_XSD  = "resources/OTA_TravelItinerary.xsd";
    public static final String CYCLE_XSD  = "resources/cycle.xsd";
        
    /**
     * AXIModelPerfTest
     */
    public AXIModelPerfTest(String testName) {
        super(testName, TEST_XSD, null);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AXIModelPerfTest.class);
        
        return suite;
    }
    
    public void testPerformance() throws Exception {
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        long startTime = System.currentTimeMillis();
        visitor.visit(getAXIModel().getRoot());
        long endTime = System.currentTimeMillis();
        PerfVisitor visitor1 = new PerfVisitor();
        visitor1.visit(getAXIModel().getRoot());
        assert(visitor1.getComponentCount() == 
               getAXIModel().getComponentFactory().getComponentCount());
        print("Time taken to create AXI model for OTA: " + (endTime - startTime));
        print(getAXIModel().getComponentFactory().toString());
    }
            
    public void testCyclicSchema() throws Exception {
        AXIModel cyclicModel = getModel(CYCLE_XSD);
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        long startTime = System.currentTimeMillis();
        visitor.visit(cyclicModel.getRoot());
        long endTime = System.currentTimeMillis();
        print("Time taken to deep visit cyclic schema: " + (endTime - startTime));
    }
        
    private class PerfVisitor extends DeepAXITreeVisitor {
        long componentCount = 0;
        public void traverse(AXIDocument document) {
            document.accept(this);
        }
        
        public long getComponentCount() {
            return componentCount;
        }

        protected void visitChildren(AXIComponent component) {
            componentCount++;
            ComponentType type = component.getComponentType();
            AXIComponent original = component.getOriginal();
            switch(type) {
                case PROXY:
                    assert(component.isShared());
                    assert(original != component);
                    if(original.getComponentType() == ComponentType.REFERENCE)
                        assert(original.isShared());
                    else
                        assert(!original.isShared());
                    //assert(component.getContentModel() != null);
                    break;
            
                case REFERENCE:
                    assert(component.isShared());
                    break;
                
                case SHARED:
                    assert(!component.isShared());
                    assert(component.getParent() instanceof AXIDocument);
                    break;
                
                case LOCAL:
                    assert(!component.isShared());
                    assert(original == component);
                    break;
                    
                default:
                    assert(false);
            }
            super.visitChildren(component);
        }
    }
    
}
