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

package org.netbeans.modules.j2ee.dd.impl.webservices.annotation;

import java.io.IOException;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.webservices.PortComponent;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;

/**
 *
 * @author Milan Kuchtiak
 */
public class WebServicesMetadataModelTest extends WebServicesTestCase {
    
    public WebServicesMetadataModelTest(String testName) {
        super(testName);
    }

    public void testModel() throws IOException, InterruptedException {
        
        TestUtilities.copyStringToFileObject(srcFO, "foo/Hello.java",
                "package foo;" +
                "@javax.jws.WebService()" +
                "public class Hello {" +
                "   public String hello() {" +
                "       return \"hello\"" +
                "   }" +               
                "}");
        TestUtilities.copyStringToFileObject(srcFO, "foo/Hello1.java",
                "package foo;" +
                "@javax.jws.WebService(serviceName=\"helloS\", portName = \"helloP\", name=\"hi\", targetNamespace=\"http://www.netbeans.org/hello\")" +
                "public class Hello1 {" +
                "   public String hello() {" +
                "       return \"hello\"" +
                "   }" +               
                "}");
        
        final int expectedResult = 2;
        
        Integer result = createModel().runReadAction(new MetadataModelAction<WebservicesMetadata, Integer>() {
            public Integer run(WebservicesMetadata metadata) {
                
                WebserviceDescription[] wsDesc = metadata.getRoot().getWebserviceDescription();
                assertNotNull(wsDesc);
                
                WebserviceDescription ws1 = metadata.findWebserviceByName("HelloService");
                assertNotNull(ws1);
                assertEquals(1,ws1.sizePortComponent());
                assertEquals("HelloService", ws1.getWebserviceDescriptionName());
                assertEquals("HelloService", ws1.getDisplayName());
                PortComponent port1 = ws1.getPortComponent(0);
                assertEquals("Hello",port1.getPortComponentName());
                try {
                    assertEquals("http://foo/",port1.getWsdlService().getNamespaceURI());
                    assertEquals("HelloService",port1.getWsdlService().getLocalPart());
                } catch (VersionNotSupportedException ex) {
                    throw new AssertionError(ex);
                }
                assertEquals("http://foo/",port1.getWsdlPort().getNamespaceURI());
                assertEquals("HelloPort",port1.getWsdlPort().getLocalPart());
                assertEquals("foo.Hello",port1.getServiceEndpointInterface());  
                assertEquals("foo.Hello",port1.getServiceEndpointInterface());
                assertEquals("Hello",port1.getServiceImplBean().getServletLink());
                
                
                WebserviceDescription ws2 = metadata.findWebserviceByName("helloS");
                assertNotNull(ws2);
                assertEquals(1,ws2.sizePortComponent());
                assertEquals("helloS", ws2.getWebserviceDescriptionName());
                PortComponent port2 = ws2.getPortComponent(0);
                assertEquals("hi",port2.getPortComponentName());
                 try {
                    assertEquals("http://www.netbeans.org/hello",port2.getWsdlService().getNamespaceURI());
                    assertEquals("helloS",port2.getWsdlService().getLocalPart());
                } catch (VersionNotSupportedException ex) {
                    throw new AssertionError(ex);
                }
                assertEquals("http://www.netbeans.org/hello",port2.getWsdlPort().getNamespaceURI());
                assertEquals("helloP",port2.getWsdlPort().getLocalPart());
                assertEquals("foo.Hello1",port2.getServiceEndpointInterface());
                assertEquals("hi",port2.getServiceImplBean().getServletLink());
                
                
                return Integer.valueOf(metadata.getRoot().sizeWebserviceDescription());
            }
        });
        
        assertSame(expectedResult, result);
    }
    
}
