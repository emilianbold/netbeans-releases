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

/*
 * BindingSchemaBundleTest.java
 * JUnit based test
 *
 * Created on February 5, 2007, 1:14 PM
 */

package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import java.io.File;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.ui.TestLookup;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class BindingSchemaBundleTest extends TestCase {
    
    static {
        try {
           System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
           Lookup l = Lookup.getDefault();
           if(l instanceof TestLookup) {
               XMLFileSystem x = new XMLFileSystem(BindingSchemaBundleTest.class.getResource("/org/netbeans/modules/wsdlextensions/jms/resources/layer.xml"));
              
               ((TestLookup) l).setup(x);
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public BindingSchemaBundleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    public void testBundleGenerator() throws Exception {
//        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
//        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
//        
//        //binding
//        WSDLExtensibilityElement bindingElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING);
//        assertEquals(WSDLExtensibilityElements.ELEMENT_BINDING, bindingElement.getName());
//        
//        List<WSDLExtensibilityElementInfoContainer> containers =  bindingElement.getAllWSDLExtensibilityElementInfoContainers();
//        assertEquals(0, containers.size());
//        
//        List<WSDLExtensibilityElementInfo> exInfos = bindingElement.getAllWSDLExtensibilityElementInfos();
//        assertEquals(1, exInfos.size());
//        
//        WSDLExtensibilityElementInfo exInfo = exInfos.get(0);
//        assertNotNull(exInfo.getElement());
//        assertNotNull(exInfo.getPrefix());
//        assertNotNull(exInfo.getSchema());
//        
//        SchemaBundleGenerator sbg = new SchemaBundleGenerator(new File("C:\\Sun\\jse\\test.properties"), exInfo.getSchema());
//        sbg.generate();
    }
}
