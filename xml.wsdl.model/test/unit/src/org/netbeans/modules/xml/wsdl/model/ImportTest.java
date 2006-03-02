package org.netbeans.modules.xml.wsdl.model;

import java.io.File;
import java.net.URI;
import junit.framework.*;
import org.netbeans.modules.xml.xam.locator.api.ModelSource;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author nn136682
 */
public class ImportTest extends TestCase {
    
    public ImportTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ImportTest.class);
        
        return suite;
    }

    public void testReadPortBinding() throws Exception {
        WSDLModel travelModel = TestResolver.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        Definitions root = travelModel.getDefinitions();
        String xpath = "/definitions/service[1]/port[2]";
        Port airlinePort = FindWSDLComponent.findComponent(Port.class, root, xpath);
        Binding airlineBinding = airlinePort.getBinding().get();
        assertEquals("testPortBinding", "AirlineReservationCallbackSoapBinding", airlineBinding.getName());
    }
    
    public void testWritePortBinding() throws Exception {
        WSDLModel travelModel = TestResolver.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        Definitions root = travelModel.getDefinitions();
        
        travelModel.startTransaction();
        Service s = travelModel.getFactory().createService();
        root.addService(s);
        s.setName("testWritePortBinding");
        Port p = travelModel.getFactory().createPort(); s.addPort(p);
        p.setName("TestPort");
        WSDLModel vehModel = TestResolver.getDefault().getWSDLModel(NamespaceLocation.VEHICLE);
        Binding b = FindWSDLComponent.findComponent(Binding.class, vehModel.getDefinitions(), "/definitions/binding[1]");
        p.setBinding(p.createReferenceTo(b, Binding.class));
        travelModel.endTransaction();
        
        travelModel = Util.dumpAndReloadModel(travelModel.getModelSource().getDocument());
        root = travelModel.getDefinitions();
        String xpath = "/definitions/service[@name='testWritePortBinding']/port[@name='TestPort']";
        Port airlinePort = FindWSDLComponent.findComponent(Port.class, root, xpath);
        Binding b2 = airlinePort.getBinding().get();
        assertEquals("testPortBinding", "VehicleReservationSoapBinding", b2.getName());
    }
}
