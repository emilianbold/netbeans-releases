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

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

/**
 * This test traverses the AXI model for a given schema and
 * checks the parent component at each level.
 *
 * @author Samaresh
 */
public class ContentModelTest extends AbstractTestCase {
    public static final String TEST_XSD         = "resources/po.xsd";
    
    public ContentModelTest(String testName) {
        super(testName, TEST_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(ContentModelTest.class);
        return suite;
    }
        
    public void testContentModels() {        
        axiModel = getAXIModel();
        
        //traverse entire tree, so that the model gets fully initialized.
        DeepAXITreeVisitor visitor = new DeepAXITreeVisitor();
        axiModel.getRoot().accept(visitor);
        
        //check all content models
        ContentModelVisitor cmv = new ContentModelVisitor();
        cmv.checkContentModels(getAXIModel());
    }
        
    private class ContentModelVisitor extends DeepAXITreeVisitor {
        private int refCount = 0;
        private ContentModel contentModel;
        private AXIModel axiModel;
        
        public void checkContentModels(AXIModel model) {
            axiModel = getAXIModel();
            for(ContentModel cm : model.getRoot().getContentModels()) {
                //must belong to the same model
                assert(axiModel == cm.getModel());
                
                contentModel = cm;
                print("checking ContentModel: " + 
                        cm.getName() + " Type: " + cm.getType() + ".....");
                if(cm.getRefSet() == null)
                    continue;
                
                refCount = cm.getRefSet().size();
                cm.accept(this);
            }
        }

        protected void visitChildren(AXIComponent component) {
            if(component instanceof ContentModel) {
                super.visitChildren(component);
                return;
            }
            print("Component: " + component + " type: " + component.getComponentType());
            assert(component.getRefSet().size() == refCount);
            for(AXIComponent ref : component.getRefSet()) {
                AXIComponent original = ref.getSharedComponent();
                assert(original == component);
            }
            super.visitChildren(component);
        }
    }
}
