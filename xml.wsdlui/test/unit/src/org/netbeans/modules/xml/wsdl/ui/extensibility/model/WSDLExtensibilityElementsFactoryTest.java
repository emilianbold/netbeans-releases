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
package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.ui.TestLookup;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.impl.WSDLExtensibilityElementsFactoryImpl;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
/*
 * WSDLExtensibilityElementsFactoryTest.java
 * JUnit based test
 *
 * Created on January 22, 2007, 11:58 AM
 */

/**
 *
 * @author radval
 */
public class WSDLExtensibilityElementsFactoryTest extends TestCase {

    static {
        try {
            System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
            Lookup l = Lookup.getDefault();
            if (l instanceof TestLookup) {
                XMLFileSystem x = new XMLFileSystem(WSDLExtensibilityElementsFactoryTest.class.getResource("/org/netbeans/modules/wsdlextensions/dummy/resources/layer.xml"));

                ((TestLookup) l).setup(x);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public WSDLExtensibilityElementsFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of getInstance method, of class org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory.
     */
    public void testGetInstance() throws Exception {
        System.out.println("getInstance");

        WSDLExtensibilityElementsFactory result = WSDLExtensibilityElementsFactory.getInstance();
        assertNotNull(result);

    }

    /**
     * Test of getWSDLExtensibilityElements method, of class org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory.
     */
    public void testGetWSDLExtensibilityElements() throws Exception {
        System.out.println("getWSDLExtensibilityElements");

        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
        assertNotNull(result);

        assertNotNull(result.getAllExtensionSchemas());
        assertNotNull(result.getAllExtensionSchemas().length == 0);
    }

    public void testBinding() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();

        //binding
        WSDLExtensibilityElement bindingElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING);
        assertNull(bindingElement);
    }

    public void testBindingOperation() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();

        //binding operation
        WSDLExtensibilityElement bindingOperationElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION);
        assertNull(bindingOperationElement);
    }

    public void testBindingOpearationInput() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();

        //binding operation input
        WSDLExtensibilityElement bindingOperationInputElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_INPUT);
        assertNull(bindingOperationInputElement);
    }

    public void testBindingOperationOutput() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();

        //binding operation output
        WSDLExtensibilityElement bindingOperationOutputElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_OUTPUT);
        assertNull(bindingOperationOutputElement);
    }

    public void testBindingOperationFault() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();

        //binding operation fault
        WSDLExtensibilityElement bindingOperationFaultElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_FAULT);
        assertNull(bindingOperationFaultElement);

    }

    public void testServicePort() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();

        //binding operation output
        WSDLExtensibilityElement servicePortElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_SERVICE_PORT);
        assertNull(servicePortElement);

    }

    public void testUndefinedExtensibilityElements126150() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();

        WSDLExtensibilityElement messageElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_MESSAGE);
        assertNull(messageElement);

        WSDLExtensibilityElement definitionsElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_DEFINITIONS);
        assertNull(definitionsElement);

        WSDLExtensibilityElement portTypeOperationElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_PORTTYPE_OPERATION);
        assertNull(portTypeOperationElement);
    }
}
