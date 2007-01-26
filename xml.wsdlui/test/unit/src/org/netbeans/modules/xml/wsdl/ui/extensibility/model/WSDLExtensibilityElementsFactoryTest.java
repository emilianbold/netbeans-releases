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
           if(l instanceof TestLookup) {
               XMLFileSystem x = new XMLFileSystem(WSDLExtensibilityElementsFactoryTest.class.getResource("/org/netbeans/modules/wsdlextensions/dummy/resources/layer.xml"));
              
               ((TestLookup) l).setup(x);
           }
        } catch(Exception ex) {
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
}
