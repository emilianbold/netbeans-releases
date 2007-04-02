/*
 * CasaWrapperModelTest.java
 * JUnit based test
 *
 * Created on March 27, 2007, 3:14 PM
 */

package org.netbeans.modules.compapp.casaeditor.model.casa;

import java.beans.PropertyChangeEvent;
import junit.framework.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.util.*;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Identification;
import org.netbeans.modules.compapp.casaeditor.model.jbi.ServiceUnit;
import org.netbeans.modules.compapp.casaeditor.model.jbi.Target;
import org.netbeans.modules.xml.wsdl.model.*;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jqian
 */
public class CasaWrapperModelTest extends TestCase {
    
    private CasaWrapperModel casaWrapperModel;
    PropertyListener propertyListener;
    TestComponentListener componentListener;
    
    
    
    static class PropertyListener implements PropertyChangeListener {
        List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
        public void assertEvent(String propertyName, Object old, Object now) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
                    if (old != null && ! old.equals(e.getOldValue()) ||
                            old == null && e.getOldValue() != null) {
                        continue;
                    }
                    if (now != null && ! now.equals(e.getNewValue()) ||
                            now == null && e.getNewValue() != null) {
                        continue;
                    }
                    return; //matched
                }
            }
            assertTrue("Expect property change event on " + propertyName +
                    " with " + old + " and " + now, false);
        }
    }
    
    static class TestComponentListener implements ComponentListener {
        List<ComponentEvent> accu = new ArrayList<ComponentEvent>();
        public void valueChanged(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenAdded(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenDeleted(ComponentEvent evt) {
            accu.add(evt);
        }
        public void reset() { accu.clear(); }
        public int getEventCount() { return accu.size(); }
        public List<ComponentEvent> getEvents() { return accu; }
        
        private void assertEvent(ComponentEvent.EventType type, Component source) {
            for (ComponentEvent e : accu) {
                if (e.getEventType().equals(type) &&
                        e.getSource() == source) {
                    return;
                }
            }
            assertTrue("Expect component change event " + type +" on source " + source +
                    ". Instead received: " + accu, false);
        }
    }
    
    public CasaWrapperModelTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        URI uri = CasaWrapperModelTest.class.getResource(
                "resources/SynchronousSampleApplication/conf/SynchronousSampleApplication.casa").toURI();
        File casaFile = new File(uri);
        FileObject casaFileObject = FileUtil.toFileObject(casaFile);
        ModelSource modelSource = TestCatalogModel.getDefault().createModelSource(casaFileObject, true);
        casaWrapperModel = new CasaWrapperModel(modelSource);
        
        URI casaWSDLUri = CasaWrapperModelTest.class.getResource(
                "resources/SynchronousSampleApplication/jbiasa/casa.wsdl").toURI();
        File casaWSDLFile = new File(casaWSDLUri);
        FileObject casaWSDLFileObject = FileUtil.toFileObject(casaWSDLFile);
        ModelSource casaWSDLModelSource = TestCatalogModel.getDefault().createModelSource(casaWSDLFileObject, true);
        
        componentListener = new TestComponentListener();
        propertyListener = new PropertyListener();
        casaWrapperModel.addComponentListener(componentListener);
        casaWrapperModel.addPropertyChangeListener(propertyListener);
    }

    protected void tearDown() throws Exception {
        casaWrapperModel.removePropertyChangeListener(propertyListener);
        casaWrapperModel.removeComponentListener(componentListener);
    }

    /**
     * Test of removePropertyChangeListener method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testRemovePropertyChangeListener() {
        System.out.println("removePropertyChangeListener");
        
        PropertyChangeListener pcl = null;
        CasaWrapperModel instance = null;
        
        instance.removePropertyChangeListener(pcl);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addPropertyChangeListener method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddPropertyChangeListener() {
        System.out.println("addPropertyChangeListener");
        
        PropertyChangeListener pcl = null;
        CasaWrapperModel instance = null;
        
        instance.addPropertyChangeListener(pcl);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBindingComponentServiceUnits method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetBindingComponentServiceUnits() {
        System.out.println("getBindingComponentServiceUnits");
                
        List<CasaBindingComponentServiceUnit> bcSUs = casaWrapperModel.getBindingComponentServiceUnits();
        assertEquals(2, bcSUs.size());
        
        // Test file binding
        CasaBindingComponentServiceUnit bcSU = bcSUs.get(0);
        assertEquals("sun-file-binding", bcSU.getComponentName());
        
        List<CasaPort> casaPorts = bcSU.getPorts().getPorts();
        assertEquals(1, casaPorts.size());
        
        CasaPort casaPort = casaPorts.get(0);        
        assertEquals("../jbiasa/casa.wsdl#xpointer(/definitions/service[@name='casaService1']/port[@name='casaPort1'])", casaPort.getLink().getHref());
               
        CasaEndpoint cEndpoint = casaPort.getConsumes().getEndpoint().get();
        CasaEndpoint pEndpoint = casaPort.getProvides().getEndpoint().get();
        assertEquals(cEndpoint, pEndpoint);
        assertEquals("{http://localhost/SynchronousSample/SynchronousSample}portType1", 
                cEndpoint.getInterfaceQName().toString());
        assertEquals("{http://whatever}casaService1", 
                cEndpoint.getServiceQName().toString());
        assertEquals("casaPort1", cEndpoint.getEndpointName());
        
         // Test file binding
        bcSU = bcSUs.get(1);
        assertEquals("sun-http-binding", bcSU.getComponentName());
        
        casaPorts = bcSU.getPorts().getPorts();
        assertEquals(1, casaPorts.size());
        
        casaPort = casaPorts.get(0);        
        assertEquals("../jbiServiceUnits/SynchronousSample/SynchronousSample.wsdl#xpointer(/definitions/service[@name='service1']/port[@name='port1'])", casaPort.getLink().getHref());
               
        cEndpoint = casaPort.getConsumes().getEndpoint().get();
        pEndpoint = casaPort.getProvides().getEndpoint().get();
        assertEquals(cEndpoint, pEndpoint);
        assertEquals("{http://localhost/SynchronousSample/SynchronousSample}portType1", 
                cEndpoint.getInterfaceQName().toString());
        assertEquals("{http://localhost/SynchronousSample/SynchronousSample}service1", 
                cEndpoint.getServiceQName().toString());
        assertEquals("port1", cEndpoint.getEndpointName());
    }

    /**
     * Test of getServiceEngineServiceUnits method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetServiceEngineServiceUnits() {
        System.out.println("getServiceEngineServiceUnits");
        
        List<CasaServiceEngineServiceUnit> seSUs = casaWrapperModel.getServiceEngineServiceUnits();
        assertEquals(1, seSUs.size());
        
        CasaServiceEngineServiceUnit seSU = seSUs.get(0);
        assertEquals("sun-bpel-engine", seSU.getComponentName());
        
        // Test casa endpoint
        assertEquals(0, seSU.getConsumes().size());
        assertEquals(1, seSU.getProvides().size());
        
        CasaEndpoint pEndpoint = seSU.getProvides().get(0).getEndpoint().get();          
        assertEquals("{http://localhost/SynchronousSample/SynchronousSample}portType1", 
                pEndpoint.getInterfaceQName().toString());
        assertEquals("{http://enterprise.netbeans.org/bpel/SynchronousSample/SynchronousSample_1}SynchronousSample", 
                pEndpoint.getServiceQName().toString());
        assertEquals("partnerlinktyperole1_myRole", pEndpoint.getEndpointName());
    }

    /**
     * Test of existingServiceUnit method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testExistingServiceEngineServiceUnit() {
        System.out.println("existingServiceEngineServiceUnit");
        
        assertTrue(casaWrapperModel.existingServiceEngineServiceUnit("SynchronousSample")); // test unit-name
        assertFalse(casaWrapperModel.existingServiceEngineServiceUnit("SynchronousSampleApplication-SynchronousSample")); // test name    
    }

    /**
     * Test of getCasaPorts method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaPorts() {
        System.out.println("getCasaPorts");
        
        List<CasaPort> casaPorts = casaWrapperModel.getCasaPorts();
        assertEquals(2, casaPorts.size());
        
        CasaEndpoint cEndpoint = casaPorts.get(0).getConsumes().getEndpoint().get(); 
        assertEquals("{http://localhost/SynchronousSample/SynchronousSample}portType1", 
                cEndpoint.getInterfaceQName().toString());
        assertEquals("{http://whatever}casaService1", 
                cEndpoint.getServiceQName().toString());
        assertEquals("casaPort1", cEndpoint.getEndpointName());
        
        cEndpoint = casaPorts.get(1).getConsumes().getEndpoint().get();          
        assertEquals("{http://localhost/SynchronousSample/SynchronousSample}portType1", 
                cEndpoint.getInterfaceQName().toString());
        assertEquals("{http://localhost/SynchronousSample/SynchronousSample}service1", 
                cEndpoint.getServiceQName().toString());
        assertEquals("port1", cEndpoint.getEndpointName());
    }

    /**
     * Test of getBindingComponentName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetBindingComponentName() {
        System.out.println("getBindingComponentName");
        
        CasaPort casaPort = casaWrapperModel.getCasaPorts().get(0);
        assertEquals("sun-file-binding", casaWrapperModel.getBindingComponentName(casaPort));
        
        casaPort = casaWrapperModel.getCasaPorts().get(1);
        assertEquals("sun-http-binding", casaWrapperModel.getBindingComponentName(casaPort));
    }

    /**
     * Test of setEndpointName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetEndpointName() {
        System.out.println("setEndpointName");
        
        // file binding
        CasaPort casaPort = casaWrapperModel.getCasaPorts().get(0);
        casaWrapperModel.setEndpointName(casaPort, "FOO");
        assertEquals("FOO", casaPort.getEndpointName());
        
        // http binding
        casaPort = casaWrapperModel.getCasaPorts().get(1);
        casaWrapperModel.setEndpointName(casaPort, "FOO");
        assertEquals("FOO", casaPort.getEndpointName());
    }

    /**
     * Test of getLinkedWSDLPort method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetLinkedWSDLPort() {
        System.out.println("getLinkedWSDLPort");
        
        CasaPort casaPort = null;
        CasaWrapperModel instance = null;
        
        Port expResult = null;
        Port result = instance.getLinkedWSDLPort(casaPort);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCasaEndpointRef method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaEndpointRef() {
        System.out.println("getCasaEndpointRef");
        
        CasaConnection firstConnection = 
                casaWrapperModel.getCasaConnectionList(true).get(0);
        
        CasaEndpointRef consumes = 
                casaWrapperModel.getCasaEndpointRef(firstConnection, true);
        assertEquals("port1", consumes.getEndpointName());
        
        CasaEndpointRef provides = 
                casaWrapperModel.getCasaEndpointRef(firstConnection, true);
        assertEquals("partnerlinktyperole1_myRole", provides.getEndpointName());
    }

    /**
     * Test of setServiceEngineServiceUnitLocation method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetServiceEngineServiceUnitLocation() {
        System.out.println("setServiceEngineServiceUnitLocation");
        
        int x = 1000;
        int y = 1000;
        
        CasaServiceEngineServiceUnit seSU = 
                casaWrapperModel.getServiceEngineServiceUnits().get(0);        
        casaWrapperModel.setServiceEngineServiceUnitLocation(seSU, x, y);
        
        assertEquals(x, seSU.getX());
        assertEquals(y, seSU.getY());
    }

    /**
     * Test of setCasaPortLocation method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetCasaPortLocation() {
        System.out.println("setCasaPortLocation");
        
        int x = 1000;
        int y = 1000;
        
        CasaBindingComponentServiceUnit bcSU = 
                casaWrapperModel.getBindingComponentServiceUnits().get(0);
        CasaPort casaPort = bcSU.getPorts().getPorts().get(0);
        
        casaWrapperModel.setCasaPortLocation(casaPort, x, y);
        assertEquals(x, casaPort.getX());
        assertEquals(y, casaPort.getY());
    }

    /**
     * Test of addConnection method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddConnection() {
        System.out.println("addConnection");
        
        CasaConnection firstConnection = 
                casaWrapperModel.getCasaConnectionList(true).get(0);
        CasaConsumes consumes = null;// firstConnection.getConsumer();
        CasaProvides provides = null;
        
        
        CasaConnection expResult = null;
        CasaConnection result = casaWrapperModel.addConnection(consumes, provides);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeConnection method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testRemoveConnection_CompApp() {
        System.out.println("removeConnection: from CompApp");
        
        CasaConnection firstConnection = 
                casaWrapperModel.getCasaConnectionList(true).get(0);
        casaWrapperModel.removeConnection(firstConnection);
        assertEquals(1, casaWrapperModel.getCasaConnectionList(false).size());
        assertEquals(2, casaWrapperModel.getCasaConnectionList(true).size());
        assertEquals("deleted", casaWrapperModel.getCasaConnectionList(true).get(0).getState());
    }
    
    /**
     * Test of removeConnection method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testRemoveConnection_UserDefined() {
        System.out.println("removeConnection: user-defined");
        CasaConnection secondConnection = 
                casaWrapperModel.getCasaConnectionList(true).get(1);
        casaWrapperModel.removeConnection(secondConnection);
        assertEquals(1, casaWrapperModel.getCasaConnectionList(false).size());
        assertEquals(1, casaWrapperModel.getCasaConnectionList(true).size());
    }

    /**
     * Test of canConnect method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testCanConnect() {
        System.out.println("canConnect");
        
        CasaEndpointRef endpointRef1 = null;
        CasaEndpointRef endpointRef2 = null;
        CasaWrapperModel instance = null;
        
        boolean expResult = true;
        boolean result = instance.canConnect(endpointRef1, endpointRef2);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUnConnectableReason method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetUnConnectableReason() {
        System.out.println("getUnConnectableReason");
        
        CasaEndpointRef endpointRef1 = null;
        CasaEndpointRef endpointRef2 = null;
        CasaWrapperModel instance = null;
        
        String expResult = "";
        String result = instance.getUnConnectableReason(endpointRef1, endpointRef2);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCasaConnectionList method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaConnectionList() {
        System.out.println("getCasaConnectionList");
        
        boolean includeDeleted = true;
        CasaWrapperModel instance = null;
        
        List<CasaConnection> expResult = null;
        List<CasaConnection> result = instance.getCasaConnectionList(includeDeleted);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCasaPortType method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaPortType() {
        System.out.println("getCasaPortType");
        
        CasaPort casaPort = null;
        CasaWrapperModel instance = null;
        
        PortType expResult = null;
        PortType result = instance.getCasaPortType(casaPort);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addServiceEngineServiceUnit method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddServiceEngineServiceUnit() {
        System.out.println("addServiceEngineServiceUnit");
        
        boolean internal = true;
        int x = 0;
        int y = 0;
        CasaWrapperModel instance = null;
        
        CasaServiceEngineServiceUnit expResult = null;
        CasaServiceEngineServiceUnit result = instance.addServiceEngineServiceUnit(internal, x, y);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addCasaPort method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddCasaPort() {
        System.out.println("addCasaPort");
        
        String componentName = "";
        int x = 0;
        int y = 0;
        CasaWrapperModel instance = null;
        
        CasaPort expResult = null;
        CasaPort result = instance.addCasaPort(componentName, x, y);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addCasaPortFromWsdlPort method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddCasaPortFromWsdlPort() {
        System.out.println("addCasaPortFromWsdlPort");
        
        Port port = null;
        File wsdlFile = null;
        CasaWrapperModel instance = null;
        
        CasaPort expResult = null;
        CasaPort result = instance.addCasaPortFromWsdlPort(port, wsdlFile);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeCasaPort method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testRemoveCasaPort() {
        System.out.println("removeCasaPort");
        
        CasaPort casaPort = null;
        CasaWrapperModel instance = null;
        
        instance.removeCasaPort(casaPort);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeEndpoint method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testRemoveEndpoint() {
        System.out.println("removeEndpoint");
        
        CasaEndpointRef endpointRef = null;
        CasaWrapperModel instance = null;
        
        instance.removeEndpoint(endpointRef);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCasaPort method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaPort() {
        System.out.println("getCasaPort");
        
        CasaEndpointRef endpointRef = null;
        CasaWrapperModel instance = null;
        
        CasaPort expResult = null;
        CasaPort result = instance.getCasaPort(endpointRef);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCasaEngineServiceUnit method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaEngineServiceUnit() {
        System.out.println("getCasaEngineServiceUnit");
        
        CasaEndpointRef endpointRef = null;
        CasaWrapperModel instance = null;
        
        CasaServiceEngineServiceUnit expResult = null;
        CasaServiceEngineServiceUnit result = instance.getCasaEngineServiceUnit(endpointRef);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of saveDocument method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSaveDocument() {
        System.out.println("saveDocument");
        
        CasaWrapperModel instance = null;
        
        instance.saveDocument();
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of discardRelatedDataObjects method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testDiscardRelatedDataObjects() {
        System.out.println("discardRelatedDataObjects");
        
        CasaWrapperModel instance = null;
        
        instance.discardRelatedDataObjects();
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addEndpointToServiceEngineServiceUnit method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddEndpointToServiceEngineServiceUnit() {
        System.out.println("addEndpointToServiceEngineServiceUnit");
        
        CasaServiceEngineServiceUnit seSU = null;
        boolean isConsumes = true;
        CasaWrapperModel instance = null;
        
        CasaEndpointRef expResult = null;
        CasaEndpointRef result = instance.addEndpointToServiceEngineServiceUnit(seSU, isConsumes);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addEndpointsToServiceEngineServiceUnit method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddEndpointsToServiceEngineServiceUnit() {
        System.out.println("addEndpointsToServiceEngineServiceUnit");
        
        JBIServiceUnitTransferObject suTransfer = null;
        CasaServiceEngineServiceUnit seSU = null;
        CasaWrapperModel instance = null;
        
        instance.addEndpointsToServiceEngineServiceUnit(suTransfer, seSU);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addInternalJBIModule method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddInternalJBIModule() {
        System.out.println("addInternalJBIModule");
        
        Project project = null;
        String type = "";
        int x = 0;
        int y = 0;
        CasaWrapperModel instance = null;
        
        instance.addInternalJBIModule(project, type, x, y);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeServiceEngineServiceUnit method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testRemoveServiceEngineServiceUnit() {
        System.out.println("removeServiceEngineServiceUnit");
        
        CasaServiceEngineServiceUnit seSU = null;
        CasaWrapperModel instance = null;
        
        instance.removeServiceEngineServiceUnit(seSU);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEditable method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testIsEditable() {
        System.out.println("isEditable");
        
        CasaServiceEngineServiceUnit seSU = 
                casaWrapperModel.getServiceEngineServiceUnits().get(0);        
        assertFalse(casaWrapperModel.isEditable(seSU));
        
        CasaBindingComponentServiceUnit fileBCSU = 
                casaWrapperModel.getBindingComponentServiceUnits().get(0);        
        assertFalse(casaWrapperModel.isEditable(fileBCSU));
        assertTrue(casaWrapperModel.isEditable(fileBCSU.getPorts().getPorts().get(0)));
        
        CasaBindingComponentServiceUnit soapBCSU = 
                casaWrapperModel.getBindingComponentServiceUnits().get(1);        
        assertFalse(casaWrapperModel.isEditable(soapBCSU));        
        assertFalse(casaWrapperModel.isEditable(soapBCSU.getPorts().getPorts().get(0)));      
        
    }

    /**
     * Test of isDeletable method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testIsDeletable() {
        System.out.println("isDeletable");
        
        CasaServiceEngineServiceUnit seSU = null;
        CasaWrapperModel instance = null;
        
        boolean expResult = true;
        boolean result = instance.isDeletable(seSU);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEndpointInterfaceQName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetEndpointInterfaceQName() {
        System.out.println("setEndpointInterfaceQName");
        
        CasaEndpointRef endpointRef = null;
        QName interfaceQName = null;
        CasaWrapperModel instance = null;
        
        instance.setEndpointInterfaceQName(endpointRef, interfaceQName);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEndpointServiceQName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetEndpointServiceQName() {
        System.out.println("setEndpointServiceQName");
        
        CasaEndpointRef endpointRef = null;
        QName serviceQName = null;
        CasaWrapperModel instance = null;
        
        instance.setEndpointServiceQName(endpointRef, serviceQName);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getCasaRegion method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaRegion() {
        System.out.println("getCasaRegion");
        
        CasaRegion region = casaWrapperModel.getCasaRegion(CasaRegion.Name.WSDL_ENDPOINTS);        
        assertEquals(CasaRegion.Name.WSDL_ENDPOINTS.getName(), region.getName());
        assertEquals(200, region.getWidth());
        
        region = casaWrapperModel.getCasaRegion(CasaRegion.Name.JBI_MODULES);
        assertEquals(CasaRegion.Name.JBI_MODULES.getName(), region.getName());
        assertEquals(500, region.getWidth());
        
        region = casaWrapperModel.getCasaRegion(CasaRegion.Name.EXTERNAL_MODULES);
        assertEquals(CasaRegion.Name.EXTERNAL_MODULES.getName(), region.getName());
        assertEquals(200, region.getWidth());
    }

    /**
     * Test of setCasaRegionWidth method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetCasaRegionWidth() {
        System.out.println("setCasaRegionWidth");
        
        int width = 1000;
        
        CasaRegion region = casaWrapperModel.getCasaRegion(CasaRegion.Name.WSDL_ENDPOINTS);   
        casaWrapperModel.setCasaRegionWidth(region, width);
        assertEquals(width, region.getWidth());
    }

    /**
     * Test of setUnitName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetUnitName() {
        System.out.println("setUnitName");
        
        String unitName = "FOO";
        
        CasaServiceEngineServiceUnit seSU = 
                casaWrapperModel.getServiceEngineServiceUnits().get(0);
        try {
            casaWrapperModel.setUnitName(seSU, unitName);
            assertTrue(false);
        } catch (Exception e) {
            ; // expected
        }
        
        CasaBindingComponentServiceUnit bcSU = 
                casaWrapperModel.getBindingComponentServiceUnits().get(0);
        try {
            casaWrapperModel.setUnitName(bcSU, unitName);
            assertTrue(false);
        } catch (Exception e) {
            ; // expected
        }
    }

    /**
     * Test of getNamespaces method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetNamespaces() {
        System.out.println("getNamespaces");
        
        Map<String, String> namespaces = casaWrapperModel.getNamespaces();
        
        assertEquals("http://enterprise.netbeans.org/bpel/SynchronousSample/SynchronousSample_1", namespaces.get("ns1"));
        assertEquals("http://localhost/SynchronousSample/SynchronousSample", namespaces.get("ns2"));
        assertEquals("http://whatever", namespaces.get("ns"));        
    }

    /**
     * Test of sync method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSync() throws Exception {
        System.out.println("sync");
        
        CasaWrapperModel instance = null;
        
        instance.sync();
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
