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

/*
 * WSDLMultipleExtensibilityElementsTest.java
 *
 * Created on January 23, 2007, 4:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.wsdl.ui.TestLookup;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

import junit.framework.*;
/**
 *
 * @author radval
 */
public class WSDLMultipleExtensibilityElementsTest extends TestCase {
    
    static {
        try {
           System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
           Lookup l = Lookup.getDefault();
           if(l instanceof TestLookup) {
               XMLFileSystem x = new XMLFileSystem();
               URL url1 = WSDLMultipleExtensibilityElementsTest.class.getResource("/org/netbeans/modules/wsdlextensions/ftp/resources/layer.xml");
               URL url2 = WSDLMultipleExtensibilityElementsTest.class.getResource("/org/netbeans/modules/wsdlextensions/jms/resources/layer.xml");
               x.setXmlUrls(new URL[]{url1, url2});
               
               ((TestLookup) l).setup(x);
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** Creates a new instance of WSDLMultipleExtensibilityElementsTest */
    public WSDLMultipleExtensibilityElementsTest() {
    }
    
    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testGetWSDLExtensibilityElements() throws Exception {
        System.out.println("getWSDLExtensibilityElements");
        
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
        assertNotNull(result);
        
        assertNotNull(result.getAllExtensionSchemas());
        assertTrue(result.getAllExtensionSchemas().length == 2);
        
    }
    
    public void testBinding() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
        
        //binding
        WSDLExtensibilityElement bindingElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING);
        assertEquals(WSDLExtensibilityElements.ELEMENT_BINDING, bindingElement.getName());
        
        List<WSDLExtensibilityElementInfoContainer> containers =  bindingElement.getAllWSDLExtensibilityElementInfoContainers();
        assertEquals(0, containers.size());
        
        List<WSDLExtensibilityElementInfo> exInfos = bindingElement.getAllWSDLExtensibilityElementInfos();
        assertEquals(2, exInfos.size());
        
        WSDLExtensibilityElementInfo ftpBindingExInfo = exInfos.get(0);
        assertNotNull(ftpBindingExInfo.getElement());
        assertNotNull(ftpBindingExInfo.getPrefix());
        assertNotNull(ftpBindingExInfo.getSchema());
        assertEquals("binding", ftpBindingExInfo.getElementName());
        assertEquals("FTPBinding", ftpBindingExInfo.getDataObject().getName());
        
        QName ftpBinding = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "binding");
        WSDLExtensibilityElementInfo ftpBindingElmentExInfo = bindingElement.getWSDLExtensibilityElementInfos(ftpBinding);
        assertNotNull(ftpBindingElmentExInfo);
        
        WSDLExtensibilityElementInfo jmsBindingExInfo = exInfos.get(1);
        assertNotNull(jmsBindingExInfo.getElement());
        assertNotNull(jmsBindingExInfo.getPrefix());
        assertNotNull(jmsBindingExInfo.getSchema());
        assertEquals("binding", jmsBindingExInfo.getElementName());
        assertEquals("JMSBinding", jmsBindingExInfo.getDataObject().getName());
        
        QName jmsBinding = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "binding");
        WSDLExtensibilityElementInfo jmsBindingElementExInfo = bindingElement.getWSDLExtensibilityElementInfos(jmsBinding);
        assertNotNull(jmsBindingElementExInfo);
        
    }
    
    public void testBindingOperation() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
        
        //binding operation
        WSDLExtensibilityElement bindingOperationElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION);
        assertEquals(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION, bindingOperationElement.getName());
        
        List<WSDLExtensibilityElementInfoContainer> bindingOperationElementContainers =  bindingOperationElement.getAllWSDLExtensibilityElementInfoContainers();
        assertEquals(0, bindingOperationElementContainers.size());
        
        List<WSDLExtensibilityElementInfo> bindingOperationExInfos = bindingOperationElement.getAllWSDLExtensibilityElementInfos();
        assertEquals(2, bindingOperationExInfos.size());
        
        WSDLExtensibilityElementInfo ftpBindingOperationExInfo = bindingOperationExInfos.get(0);
        assertNotNull(ftpBindingOperationExInfo.getElement());
        assertNotNull(ftpBindingOperationExInfo.getPrefix());
        assertNotNull(ftpBindingOperationExInfo.getSchema());
        assertEquals("operation", ftpBindingOperationExInfo.getElementName());
        assertEquals("FTPBindingOperation", ftpBindingOperationExInfo.getDataObject().getName());
        
        QName ftpBindingOperation = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "operation");
        WSDLExtensibilityElementInfo ftpBindingOperationElementExInfo = bindingOperationElement.getWSDLExtensibilityElementInfos(ftpBindingOperation);
        assertNotNull(ftpBindingOperationElementExInfo);


        WSDLExtensibilityElementInfo jmsBindingOperationExInfo = bindingOperationExInfos.get(1);
        assertNotNull(jmsBindingOperationExInfo.getElement());
        assertNotNull(jmsBindingOperationExInfo.getPrefix());
        assertNotNull(jmsBindingOperationExInfo.getSchema());
        assertEquals("operation", jmsBindingOperationExInfo.getElementName());
        assertEquals("JMSBindingOperation", jmsBindingOperationExInfo.getDataObject().getName());
        
        QName jmsBindingOperation = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "operation");
        WSDLExtensibilityElementInfo jmsBindingOperationElementExInfo = bindingOperationElement.getWSDLExtensibilityElementInfos(jmsBindingOperation);
        assertNotNull(jmsBindingOperationElementExInfo);

    }
    
    public void testBindingOpearationInput() throws Exception {
        
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
        
        //binding operation input
        WSDLExtensibilityElement bindingOperationInputElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_INPUT);
        assertEquals(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_INPUT, bindingOperationInputElement.getName());
        
        List<WSDLExtensibilityElementInfoContainer> bindingOperationInputElementContainers =  bindingOperationInputElement.getAllWSDLExtensibilityElementInfoContainers();
        assertEquals(0, bindingOperationInputElementContainers.size());
        
        List<WSDLExtensibilityElementInfo> bindingOperationInputExInfos = bindingOperationInputElement.getAllWSDLExtensibilityElementInfos();
        assertEquals(4, bindingOperationInputExInfos.size());
        
        WSDLExtensibilityElementInfo ftpBindingOperationInputExInfo1 = bindingOperationInputExInfos.get(0);
        assertNotNull(ftpBindingOperationInputExInfo1.getElement());
        assertNotNull(ftpBindingOperationInputExInfo1.getPrefix());
        assertNotNull(ftpBindingOperationInputExInfo1.getSchema());
        assertEquals("message", ftpBindingOperationInputExInfo1.getElementName());
        assertEquals("FTPBindingMessage", ftpBindingOperationInputExInfo1.getDataObject().getName());
        
        QName ftpBindingOperationInput1 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "message");
        WSDLExtensibilityElementInfo ftpBindingOperationInputElementExInfo1 = bindingOperationInputElement.getWSDLExtensibilityElementInfos(ftpBindingOperationInput1);
        assertNotNull(ftpBindingOperationInputElementExInfo1);
        
        WSDLExtensibilityElementInfo ftpBindingOperationInputExInfo2 = bindingOperationInputExInfos.get(1);
        assertNotNull(ftpBindingOperationInputExInfo2.getElement());
        assertNotNull(ftpBindingOperationInputExInfo2.getPrefix());
        assertNotNull(ftpBindingOperationInputExInfo2.getSchema());
        assertEquals("messageActivePassive", ftpBindingOperationInputExInfo2.getElementName());
        assertEquals("FTPBindingMessageActivePassive", ftpBindingOperationInputExInfo2.getDataObject().getName());
        
        QName ftpBindingOperationInput2 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "messageActivePassive");
        WSDLExtensibilityElementInfo ftpBindingOperationInputElementExInfo2 = bindingOperationInputElement.getWSDLExtensibilityElementInfos(ftpBindingOperationInput2);
        assertNotNull(ftpBindingOperationInputElementExInfo2);
        
        WSDLExtensibilityElementInfo ftpBindingOperationInputExInfo3 = bindingOperationInputExInfos.get(2);
        assertNotNull(ftpBindingOperationInputExInfo3.getElement());
        assertNotNull(ftpBindingOperationInputExInfo3.getPrefix());
        assertNotNull(ftpBindingOperationInputExInfo3.getSchema());
        assertEquals("transfer", ftpBindingOperationInputExInfo3.getElementName());
        assertEquals("FTPBindingTransfer", ftpBindingOperationInputExInfo3.getDataObject().getName());
        
        QName ftpBindingOperationInput3 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "transfer");
        WSDLExtensibilityElementInfo ftpBindingOperationInputElementExInfo3 = bindingOperationInputElement.getWSDLExtensibilityElementInfos(ftpBindingOperationInput3);
        assertNotNull(ftpBindingOperationInputElementExInfo3);
        
        WSDLExtensibilityElementInfo jmsBindingOperationInputExInfo1 = bindingOperationInputExInfos.get(3);
        assertNotNull(jmsBindingOperationInputExInfo1.getElement());
        assertNotNull(jmsBindingOperationInputExInfo1.getPrefix());
        assertNotNull(jmsBindingOperationInputExInfo1.getSchema());
        assertEquals("message", jmsBindingOperationInputExInfo1.getElementName());
        assertEquals("JMSBindingOperationInput", jmsBindingOperationInputExInfo1.getDataObject().getName());
        
        QName jmsBindingOperationInput1 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "message");
        WSDLExtensibilityElementInfo jmsBindingOperationInputElementExInfo1 = bindingOperationInputElement.getWSDLExtensibilityElementInfos(jmsBindingOperationInput1);
        assertNotNull(jmsBindingOperationInputElementExInfo1);
        
    }
    
    public void testBindingOperationOutput() throws Exception {
        WSDLExtensibilityElementsFactory instance = WSDLExtensibilityElementsFactory.getInstance();
        WSDLExtensibilityElements result = instance.getWSDLExtensibilityElements();
        
        //binding operation output
        WSDLExtensibilityElement bindingOperationOutputElement = result.getWSDLExtensibilityElement(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_OUTPUT);
        assertEquals(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION_OUTPUT, bindingOperationOutputElement.getName());
        
        List<WSDLExtensibilityElementInfoContainer> bindingOperationOutputElementContainers =  bindingOperationOutputElement.getAllWSDLExtensibilityElementInfoContainers();
        assertEquals(0, bindingOperationOutputElementContainers.size());
        
        List<WSDLExtensibilityElementInfo> bindingOperationOutputExInfos = bindingOperationOutputElement.getAllWSDLExtensibilityElementInfos();
        assertEquals(4, bindingOperationOutputExInfos.size());
        
        WSDLExtensibilityElementInfo ftpBindingOperationInputExInfo1 = bindingOperationOutputExInfos.get(0);
        assertNotNull(ftpBindingOperationInputExInfo1.getElement());
        assertNotNull(ftpBindingOperationInputExInfo1.getPrefix());
        assertNotNull(ftpBindingOperationInputExInfo1.getSchema());
        assertEquals("message", ftpBindingOperationInputExInfo1.getElementName());
        assertEquals("FTPBindingMessage", ftpBindingOperationInputExInfo1.getDataObject().getName());
        
        QName ftpBindingOperationInput1 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "message");
        WSDLExtensibilityElementInfo ftpBindingOperationInputElementExInfo1 = bindingOperationOutputElement.getWSDLExtensibilityElementInfos(ftpBindingOperationInput1);
        assertNotNull(ftpBindingOperationInputElementExInfo1);
        
        WSDLExtensibilityElementInfo ftpBindingOperationInputExInfo2 = bindingOperationOutputExInfos.get(1);
        assertNotNull(ftpBindingOperationInputExInfo2.getElement());
        assertNotNull(ftpBindingOperationInputExInfo2.getPrefix());
        assertNotNull(ftpBindingOperationInputExInfo2.getSchema());
        assertEquals("messageActivePassive", ftpBindingOperationInputExInfo2.getElementName());
        assertEquals("FTPBindingMessageActivePassive", ftpBindingOperationInputExInfo2.getDataObject().getName());
        
        QName ftpBindingOperationInput2 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "messageActivePassive");
        WSDLExtensibilityElementInfo ftpBindingOperationInputElementExInfo2 = bindingOperationOutputElement.getWSDLExtensibilityElementInfos(ftpBindingOperationInput2);
        assertNotNull(ftpBindingOperationInputElementExInfo2);
        
        WSDLExtensibilityElementInfo ftpBindingOperationInputExInfo3 = bindingOperationOutputExInfos.get(2);
        assertNotNull(ftpBindingOperationInputExInfo3.getElement());
        assertNotNull(ftpBindingOperationInputExInfo3.getPrefix());
        assertNotNull(ftpBindingOperationInputExInfo3.getSchema());
        assertEquals("transfer", ftpBindingOperationInputExInfo3.getElementName());
        assertEquals("FTPBindingTransfer", ftpBindingOperationInputExInfo3.getDataObject().getName());
        
        QName ftpBindingOperationInput3 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "transfer");
        WSDLExtensibilityElementInfo ftpBindingOperationInputElementExInfo3 = bindingOperationOutputElement.getWSDLExtensibilityElementInfos(ftpBindingOperationInput3);
        assertNotNull(ftpBindingOperationInputElementExInfo3);
        
        WSDLExtensibilityElementInfo jmsBindingOperationInputExInfo1 = bindingOperationOutputExInfos.get(3);
        assertNotNull(jmsBindingOperationInputExInfo1.getElement());
        assertNotNull(jmsBindingOperationInputExInfo1.getPrefix());
        assertNotNull(jmsBindingOperationInputExInfo1.getSchema());
        assertEquals("message", jmsBindingOperationInputExInfo1.getElementName());
        assertEquals("JMSBindingOperationOutput", jmsBindingOperationInputExInfo1.getDataObject().getName());
        
        QName jmsBindingOperationInput1 = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "message");
        WSDLExtensibilityElementInfo jmsBindingOperationInputElementExInfo1 = bindingOperationOutputElement.getWSDLExtensibilityElementInfos(jmsBindingOperationInput1);
        assertNotNull(jmsBindingOperationInputElementExInfo1);
        
//        WSDLExtensibilityElementInfo bindingOperationOutputExInfo = bindingOperationOutputExInfos.get(0);
//        assertNotNull(bindingOperationOutputExInfo.getElement());
//        assertNotNull(bindingOperationOutputExInfo.getPrefix());
//        assertNotNull(bindingOperationOutputExInfo.getSchema());
//        assertEquals("message", bindingOperationOutputExInfo.getElementName());
//        assertEquals("JMSBindingOperationOutput", bindingOperationOutputExInfo.getDataObject().getName());
//        
//        QName jmsBindingOperationInput = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "message");
//        WSDLExtensibilityElementInfo jmsBindingOperationInputExInfo = bindingOperationOutputElement.getWSDLExtensibilityElementInfos(jmsBindingOperationInput);
//        assertNotNull(jmsBindingOperationInputExInfo);

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
        assertEquals(WSDLExtensibilityElements.ELEMENT_SERVICE_PORT, servicePortElement.getName());
        
        List<WSDLExtensibilityElementInfoContainer> servicePortElementContainers =  servicePortElement.getAllWSDLExtensibilityElementInfoContainers();
        assertEquals(0, servicePortElementContainers.size());
        
        List<WSDLExtensibilityElementInfo> servicePortExInfos = servicePortElement.getAllWSDLExtensibilityElementInfos();
        assertEquals(2, servicePortExInfos.size());
        
        WSDLExtensibilityElementInfo ftpServicePortExInfo = servicePortExInfos.get(0);
        assertNotNull(ftpServicePortExInfo.getElement());
        assertNotNull(ftpServicePortExInfo.getPrefix());
        assertNotNull(ftpServicePortExInfo.getSchema());
        assertEquals("address", ftpServicePortExInfo.getElementName());
        assertEquals("FTPServicePort", ftpServicePortExInfo.getDataObject().getName());
        
        QName ftpServicePort = new QName("http://schemas.sun.com/jbi/wsdl-extensions/ftp/", "address");
        WSDLExtensibilityElementInfo ftpServicePortElementExInfo = servicePortElement.getWSDLExtensibilityElementInfos(ftpServicePort);
        assertNotNull(ftpServicePortElementExInfo);
        
        WSDLExtensibilityElementInfo servicePortExInfo = servicePortExInfos.get(1);
        assertNotNull(servicePortExInfo.getElement());
        assertNotNull(servicePortExInfo.getPrefix());
        assertNotNull(servicePortExInfo.getSchema());
        assertEquals("address", servicePortExInfo.getElementName());
        assertEquals("JMSServicePort", servicePortExInfo.getDataObject().getName());
        
        QName jmsServicePort = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "address");
        WSDLExtensibilityElementInfo jmsServicePortElementExInfo = servicePortElement.getWSDLExtensibilityElementInfos(jmsServicePort);
        assertNotNull(jmsServicePortElementExInfo);

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
