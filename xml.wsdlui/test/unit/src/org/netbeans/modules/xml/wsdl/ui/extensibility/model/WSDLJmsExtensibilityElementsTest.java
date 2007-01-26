/*
 * WSDLJmsExtensibilityElementsTest.java
 *
 * Created on January 23, 2007, 4:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import java.util.List;
import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.ui.TestLookup;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author radval
 */
public class WSDLJmsExtensibilityElementsTest extends TestCase {
    
    static {
        try {
           System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
           Lookup l = Lookup.getDefault();
           if(l instanceof TestLookup) {
               XMLFileSystem x = new XMLFileSystem(WSDLExtensibilityElementsFactoryTest.class.getResource("/org/netbeans/modules/wsdlextensions/jms/resources/layer.xml"));
              
               ((TestLookup) l).setup(x);
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /** Creates a new instance of WSDLJmsExtensibilityElementsTest */
    public WSDLJmsExtensibilityElementsTest() {
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
        assertTrue(result.getAllExtensionSchemas().length == 1);
        
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
        assertEquals(1, exInfos.size());
        
        WSDLExtensibilityElementInfo exInfo = exInfos.get(0);
        assertNotNull(exInfo.getElement());
        assertNotNull(exInfo.getPrefix());
        assertNotNull(exInfo.getSchema());
        assertEquals("binding", exInfo.getElementName());
        assertEquals("JMSBinding", exInfo.getDataObject().getName());
        
        QName jmsBinding = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "binding");
        WSDLExtensibilityElementInfo jmsBindingExInfo = bindingElement.getWSDLExtensibilityElementInfos(jmsBinding);
        assertNotNull(jmsBindingExInfo);
        
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
        assertEquals(1, bindingOperationExInfos.size());
        
        WSDLExtensibilityElementInfo bindingOperationExInfo = bindingOperationExInfos.get(0);
        assertNotNull(bindingOperationExInfo.getElement());
        assertNotNull(bindingOperationExInfo.getPrefix());
        assertNotNull(bindingOperationExInfo.getSchema());
        assertEquals("operation", bindingOperationExInfo.getElementName());
        assertEquals("JMSBindingOperation", bindingOperationExInfo.getDataObject().getName());
        
        QName jmsBindingOperation = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "operation");
        WSDLExtensibilityElementInfo jmsBindingOperationExInfo = bindingOperationElement.getWSDLExtensibilityElementInfos(jmsBindingOperation);
        assertNotNull(jmsBindingOperationExInfo);

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
        assertEquals(1, bindingOperationInputExInfos.size());
        
        WSDLExtensibilityElementInfo bindingOperationInputExInfo = bindingOperationInputExInfos.get(0);
        assertNotNull(bindingOperationInputExInfo.getElement());
        assertNotNull(bindingOperationInputExInfo.getPrefix());
        assertNotNull(bindingOperationInputExInfo.getSchema());
        assertEquals("message", bindingOperationInputExInfo.getElementName());
        assertEquals("JMSBindingOperationInput", bindingOperationInputExInfo.getDataObject().getName());
        
        QName jmsBindingOperationInput = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "message");
        WSDLExtensibilityElementInfo jmsBindingOperationInputExInfo = bindingOperationInputElement.getWSDLExtensibilityElementInfos(jmsBindingOperationInput);
        assertNotNull(jmsBindingOperationInputExInfo);

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
        assertEquals(1, bindingOperationOutputExInfos.size());
        
        WSDLExtensibilityElementInfo bindingOperationOutputExInfo = bindingOperationOutputExInfos.get(0);
        assertNotNull(bindingOperationOutputExInfo.getElement());
        assertNotNull(bindingOperationOutputExInfo.getPrefix());
        assertNotNull(bindingOperationOutputExInfo.getSchema());
        assertEquals("message", bindingOperationOutputExInfo.getElementName());
        assertEquals("JMSBindingOperationOutput", bindingOperationOutputExInfo.getDataObject().getName());
        
        QName jmsBindingOperationInput = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "message");
        WSDLExtensibilityElementInfo jmsBindingOperationInputExInfo = bindingOperationOutputElement.getWSDLExtensibilityElementInfos(jmsBindingOperationInput);
        assertNotNull(jmsBindingOperationInputExInfo);

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
        assertEquals(1, servicePortExInfos.size());
        
        WSDLExtensibilityElementInfo servicePortExInfo = servicePortExInfos.get(0);
        assertNotNull(servicePortExInfo.getElement());
        assertNotNull(servicePortExInfo.getPrefix());
        assertNotNull(servicePortExInfo.getSchema());
        assertEquals("address", servicePortExInfo.getElementName());
        assertEquals("JMSServicePort", servicePortExInfo.getDataObject().getName());
        
        QName jmsBindingOperationInput = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "address");
        WSDLExtensibilityElementInfo jmsBindingOperationInputExInfo = servicePortElement.getWSDLExtensibilityElementInfos(jmsBindingOperationInput);
        assertNotNull(jmsBindingOperationInputExInfo);

    }
    
}
