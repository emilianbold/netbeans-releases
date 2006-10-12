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
import org.openide.filesystems.FileObject;

        
/**
 * This unit test extends AXIModelTest and tests components from multiple
 * namespaces and their models. Ensures that the components from different
 * namespaces belong to appropriate AXIModels.
 *
 * - Checks that components coming from diff namespaces are read-only.
 * - Checks that children components for a reference should be proxies.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelExTest extends AXIModelTest {
            
    public static final String TEST_XSD         = "resources/multifilePO.xsd";
    
    private static String PO_TNS = "http://xml.netbeans.org/examples/targetNS/PO";
    private static String ITEMS_TNS = "http://xml.netbeans.org/examples/targetNS/Items";
    private static String ADDR_TNS = "http://xml.netbeans.org/examples/targetNS/Address";
    private AXIModel aModelPO;
    
    /**
     * AXIModelExTest
     */
    public AXIModelExTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AXIModelExTest.class);
        return suite;
    }
    
    public void testModel() {
        aModelPO = getAXIModel();
        assertEquals(getAXIModel().getRoot().getTargetNamespace(), PO_TNS);
        CheckTargetNamespaceVisitor visitor = new CheckTargetNamespaceVisitor();
        visitor.checkNamespace(getAXIModel().getRoot());
    }
    
    private class CheckTargetNamespaceVisitor extends DeepAXITreeVisitor {
        
        public void checkNamespace(AXIDocument document) {
            document.accept(this);
        }

        protected void visitChildren(AXIComponent component) {
            String ns = component.getTargetNamespace();
            
            //components must belong to one of three namespaces
            if( !ns.equals(PO_TNS) &&
                !ns.equals(ADDR_TNS) &&
                !ns.equals(ITEMS_TNS)) {
                assert(false);
            }
            
            //components that come from PO namespaces, must all
            //have the same AXIModel
            if(component.getTargetNamespace().equals(PO_TNS)) {
                assert(aModelPO == component.getModel());
            }
            
            //if a component is a reference, its children has to be proxies
            if(component.getComponentType() == ComponentType.REFERENCE) {
                for(AXIComponent c: component.getChildren()) {
                    assert(c.getComponentType() == ComponentType.PROXY);
                }                
            }            
            
            //components that come from other namespaces, must all
            //have the same AXIModel, but diff from the ones from PO namespace.
            if(!component.getTargetNamespace().equals(PO_TNS)) {
                doValidate(component, component.getOriginal().getModel());
            }
            
            super.visitChildren(component);
        }        
        
        private void doValidate(AXIComponent component, AXIModel otherModel) {
            //must be a proxy and the proxy's model must be the same as PO model.
            assert(component.getComponentType() == ComponentType.PROXY);
            assert(component.getModel() == aModelPO);
            assert(component.isReadOnly());
            
            //find the original for this proxy component
            //and the original must belong to a diff model.
            AXIComponent original = component.getOriginal();
            assert(otherModel != aModelPO);
            assert(otherModel == original.getModel());
            
            //also find the FileObject from the two models
            //and they should be different
            FileObject fPO = (FileObject)aModelPO.getModelSource().
                    getLookup().lookup(FileObject.class);
            FileObject otherFO = (FileObject)otherModel.getModelSource().
                    getLookup().lookup(FileObject.class);
            assert(fPO != null);
            assert(otherFO != null);
            assert(fPO != otherFO);            
        }
    }
    
}
