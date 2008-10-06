/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.websvc.project.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URI;
import org.netbeans.modules.websvc.project.spi.*;
import org.netbeans.junit.NbTestCase;
import org.openide.nodes.Node;

/**
 *
 * @author mkuchtiak
 */
public class WSProjectApiTest extends NbTestCase {

    public WSProjectApiTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /** Test service model for AddNumbers service
     */
    public void testServiceModel() throws IOException {
        // TODO code application logic here
        final SOAP soap = new SOAP("http://localhost:8080/HelloService/HelloService.wsdl",
                                   "xml-resources/web-service-references/HelloService/wsdl/HelloService.wsdl");

        final ServiceDescriptorImplementation desc = new ServiceDescriptorImplementation() {


            public URL getRuntimeLocation() {
                try {
                    return new URL(soap.getRuntime());
                } catch (MalformedURLException ex) {
                    return null;
                }
            }

            public URI getRelativeURI() {
                try {
                    return new URI(soap.getRelative());
                } catch (URISyntaxException ex) {
                    return null;
                }
            }

            public String getIdentifier() {
                return "Test";
            }
        };

        WebServiceImplementation serviceImpl = new WebServiceImplementation() {

            public boolean isServiceProvider() {
                return true;
            }

            public WebService.Type getServiceType() {
                return WebService.Type.SOAP;
            }

            public ServiceDescriptor getServiceDescriptor() {
                return WebServiceFactory.createWebServiceDescriptor(desc);
            }

            public Node getNode() {
                return null;
            }

        };
        WebService service = WebServiceFactory.createWebService(serviceImpl);
        assertNotNull(service);
        assertEquals(WebService.Type.SOAP, service.getServiceType());
        assertTrue(service.isServiceProvider());
        assertEquals("http://localhost:8080/HelloService/HelloService.wsdl",service.getServiceDescriptor().getRuntimeLocation().toString());
    }
    
    class SOAP {
    String runtime, relative;
    
    public SOAP(String runtime, String relative) {
        this.runtime = runtime;
        this.relative = relative;
    }

    public String getRelative() {
        return relative;
    }

    public String getRuntime() {
        return runtime;
    }

}
}
    
