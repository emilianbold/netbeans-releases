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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
