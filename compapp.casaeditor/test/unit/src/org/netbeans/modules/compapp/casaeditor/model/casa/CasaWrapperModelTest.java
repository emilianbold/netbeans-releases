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
                "../resources/SynchronousSampleApplication/SynchronousSampleApplication.casa").toURI();
        File casaFile = new File(uri);
        FileObject casaFileObject = FileUtil.toFileObject(casaFile);
        ModelSource modelSource = TestCatalogModel.getDefault().createModelSource(casaFileObject, true);
        casaWrapperModel = new CasaWrapperModel(modelSource);
        
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
        assertEquals(1, bcSUs.size());
        
        CasaBindingComponentServiceUnit bcSU = bcSUs.get(0);
        assertEquals("sun-http-binding", bcSU.getComponentName());
        
        // Test casa port link
        List<CasaPort> casaPorts = bcSU.getPorts().getPorts();
        assertEquals(1, casaPorts.size());
        
        CasaPort casaPort = casaPorts.get(0);        
        assertEquals("../jbiServiceUnits/SynchronousSample/SynchronousSample.wsdl#xpointer(/definitions/service[@name='service1']/port[@name='port1'])", casaPort.getLink().getHref());
               
        // Test casa port endpoint
        CasaEndpoint cEndpoint = casaPort.getConsumes().getEndpoint().get();
        CasaEndpoint pEndpoint = casaPort.getProvides().getEndpoint().get();
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
        
        assertTrue(casaWrapperModel.existingServiceEngineServiceUnit("SynchronousSample"));
        assertFalse(casaWrapperModel.existingServiceEngineServiceUnit("SynchronousSampleApplication-SynchronousSample"));        
    }

    /**
     * Test of getCasaPorts method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetCasaPorts() {
        System.out.println("getCasaPorts");
        
        CasaWrapperModel instance = null;
        
        List<CasaPort> expResult = null;
        List<CasaPort> result = instance.getCasaPorts();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getBindingComponentName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetBindingComponentName() {
        System.out.println("getBindingComponentName");
        
        CasaPort casaPort = null;
        CasaWrapperModel instance = null;
        
        String expResult = "";
        String result = instance.getBindingComponentName(casaPort);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setEndpointName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetEndpointName() {
        System.out.println("setEndpointName");
        
        CasaPort casaPort = null;
        String endpointName = "";
        CasaWrapperModel instance = null;
        
        instance.setEndpointName(casaPort, endpointName);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
        
        CasaConnection casaConnection = null;
        boolean isConsumes = true;
        CasaWrapperModel instance = null;
        
        CasaEndpointRef expResult = null;
        CasaEndpointRef result = instance.getCasaEndpointRef(casaConnection, isConsumes);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setServiceEngineServiceUnitLocation method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetServiceEngineServiceUnitLocation() {
        System.out.println("setServiceEngineServiceUnitLocation");
        
        CasaServiceEngineServiceUnit seSU = null;
        int x = 0;
        int y = 0;
        CasaWrapperModel instance = null;
        
        instance.setServiceEngineServiceUnitLocation(seSU, x, y);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCasaPortLocation method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetCasaPortLocation() {
        System.out.println("setCasaPortLocation");
        
        CasaPort casaPort = null;
        int x = 0;
        int y = 0;
        CasaWrapperModel instance = null;
        
        instance.setCasaPortLocation(casaPort, x, y);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addConnection method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testAddConnection() {
        System.out.println("addConnection");
        
        CasaConsumes consumes = null;
        CasaProvides provides = null;
        CasaWrapperModel instance = null;
        
        CasaConnection expResult = null;
        CasaConnection result = instance.addConnection(consumes, provides);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeConnection method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testRemoveConnection() {
        System.out.println("removeConnection");
        
        CasaConnection connection = null;
        CasaWrapperModel instance = null;
        
        instance.removeConnection(connection);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
     * Test of getDefaultBindingComponents method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetDefaultBindingComponents() {
        System.out.println("getDefaultBindingComponents");
        
        CasaWrapperModel instance = null;
        
        Map<String, String> expResult = null;
        Map<String, String> result = instance.getDefaultBindingComponents();
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
        
        String componentType = "";
        String componentName = "";
        int x = 0;
        int y = 0;
        CasaWrapperModel instance = null;
        
        CasaPort expResult = null;
        CasaPort result = instance.addCasaPort(componentType, componentName, x, y);
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
     * Test of getJBIProject method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetJBIProject() throws Exception {
        System.out.println("getJBIProject");
        
        CasaWrapperModel instance = null;
        
        Project expResult = null;
        Project result = instance.getJBIProject();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buildCompApp method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testBuildCompApp() {
        System.out.println("buildCompApp");
        
        CasaWrapperModel instance = null;
        
        instance.buildCompApp();
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isEditable method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testIsEditable() {
        System.out.println("isEditable");
        
        CasaServiceEngineServiceUnit seSU = null;
        CasaWrapperModel instance = null;
        
        boolean expResult = true;
        boolean result = instance.isEditable(seSU);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
        
        CasaRegion.Name regionName = null;
        CasaWrapperModel instance = null;
        
        CasaRegion expResult = null;
        CasaRegion result = instance.getCasaRegion(regionName);
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setCasaRegionWidth method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetCasaRegionWidth() {
        System.out.println("setCasaRegionWidth");
        
        CasaRegion casaRegion = null;
        int width = 0;
        CasaWrapperModel instance = null;
        
        instance.setCasaRegionWidth(casaRegion, width);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setUnitName method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testSetUnitName() {
        System.out.println("setUnitName");
        
        CasaServiceUnit su = null;
        String unitName = "";
        CasaWrapperModel instance = null;
        
        instance.setUnitName(su, unitName);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNamespaces method, of class org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel.
     */
    public void testGetNamespaces() {
        System.out.println("getNamespaces");
        
        CasaWrapperModel instance = null;
        
        Map<String, String> expResult = null;
        Map<String, String> result = instance.getNamespaces();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
