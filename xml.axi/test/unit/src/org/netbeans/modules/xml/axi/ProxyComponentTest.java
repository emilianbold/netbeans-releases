/*
 * CheckParentTest.java
 *
 * Created on June 1, 2006, 10:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.axi;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.modules.xml.axi.AXIComponent.ComponentType;
import org.netbeans.modules.xml.axi.visitor.DeepAXITreeVisitor;

/**
 * This test traverses the AXI model for address2.xsd schema file
 * and for each proxy component, verifies the number of indirection
 * to the original component.
 *
 * @author Samaresh
 */
public class ProxyComponentTest extends AbstractTestCase {
    public static final String TEST_XSD         = "resources/address2.xsd";
    
    public ProxyComponentTest(String testName) {
        super(testName, TEST_XSD, null);
    }
        
    public static Test suite() {
        TestSuite suite = new TestSuite(ProxyComponentTest.class);
        return suite;
    }
    
    public void testProxyComponents() {
        DeepModelVisitor visitor = new DeepModelVisitor();
        getAXIModel().getRoot().accept(visitor);
    }
        
    private class DeepModelVisitor extends DeepAXITreeVisitor {
        private int counter = 0;

        protected void visitChildren(AXIComponent component) {
            if(component.getComponentType() == ComponentType.PROXY) {
                //reset the indirection level, 
                //the call getOriginal will set it.
                proxyIndirection = 0;
                AXIComponent o = getOriginal(component);
                assert(proxyIndirectionTest == proxyIndirection);
            }
            
            if(component instanceof ContentModel) {
                ContentModel cm = (ContentModel)component;
                if(cm.getName().equals("group")) {
                    checkProxyIndirection = true;
                    proxyIndirectionTest = 0;
                }
                if(cm.getName().equals("attr-group")) {
                    checkProxyIndirection = true;
                    proxyIndirectionTest = 0;
                }
                if(cm.getName().equals("USAddress")) {
                    checkProxyIndirection = true;
                    proxyIndirectionTest = 1;
                }
            }
                        
            if(component instanceof Element) {
                Element e = (Element)component;
                if(e.getName().equals("address")) {
                    checkProxyIndirection = true;
                    proxyIndirectionTest = 2;
                } else
                    checkProxyIndirection = false;
            }
            super.visitChildren(component);
        }
    }
    
    /**
     * Returns the original from a proxy. A proxy may have multiple
     * levels of indirection to an original.
     */
    private AXIComponent getOriginal(AXIComponent component) {
        AXIComponent shared = component.getSharedComponent();
        if(shared == null)
            return component;
        
        proxyIndirection++;
        if(shared.getComponentType() != ComponentType.PROXY) {
            return shared;
        }
        
        return getOriginal(shared);
    }
    
    private int proxyIndirectionTest = 0;
    private int proxyIndirection = 0;
    private boolean checkProxyIndirection = false;
}
