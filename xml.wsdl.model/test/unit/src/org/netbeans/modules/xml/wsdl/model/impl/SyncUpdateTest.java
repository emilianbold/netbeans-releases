package org.netbeans.modules.xml.wsdl.model.impl;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.TestResolver;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PartnerLinkType;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.Role;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.RolePortType;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

/**
 *
 * @author Nam Nguyen
 */
public class SyncUpdateTest extends TestCase {
    private TestComponentListener listener;
    
    public SyncUpdateTest(String testName) {
        super(testName);
    }

    public static class TestComponentListener implements ComponentListener {
        ArrayList<ComponentEvent> events = new ArrayList<ComponentEvent>();
        
        public void valueChanged(ComponentEvent evt) {
            events.add(evt);
        }
        public void childrenDeleted(ComponentEvent evt) {
            events.add(evt);
        }
        public void childrenAdded(ComponentEvent evt) {
            events.add(evt);
        }

        public void assertChangeEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.ATTRIBUTE &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive ATTRIBUTE event on " + source, true);
        }
        
        public void assertChildAddedEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.CHILD_ADDED &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive CHILD_ADDED event on " + source, true);
        }

        public void assertChildRemovedEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.CHILD_REMOVED &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive CHILD_REMOVED event on " + source, true);
        }
        
        public void reset() { events.clear(); events = null; }
    }
    
    protected void setUp() throws Exception {
    }

    private WSDLModel setup(NamespaceLocation wsdl) throws Exception {
        WSDLModel m = TestResolver.getDefault().getWSDLModel(wsdl);
        listener = new TestComponentListener();
        m.addComponentListener(listener);
        return m;
    }

    protected void tearDown() throws Exception {
        listener.reset();
    }

    public <T extends ReferenceableWSDLComponent> T find(WSDLModel model, String name, Class<T> type) {
        return new FindReferencedVisitor<T>(model.getDefinitions()).find(name, type);
    }
    
    public void testSyncAddRemoveBPEL() throws Exception {
        WSDLModel model = setup(NamespaceLocation.VEHICLE);
        
        PartnerLinkType plt = find(model, "VehicleReservationPartnerLinkType", PartnerLinkType.class);
        Role role1 = plt.getRole1();
        Role role2 = plt.getRole2();
        
        Document doc = AbstractModel.class.cast(model).getBaseDocument();
        Util.setDocumentContentTo(doc, "/org/netbeans/modules/xml/wsdl/model/resources/Vehicle_PartnerLinkChanged.wsdl");
        model.sync();
        
        listener.assertChildRemovedEvent(plt);
        assertNull("role2 removed", plt.getRole2());
        
        // now do the reverse
        Util.setDocumentContentTo(doc, "/org/netbeans/modules/xml/wsdl/model/resources/VehicleReservationService.wsdl");
        model.sync();
        
        listener.assertChildAddedEvent(plt);
        assertEquals("role2 added", "VehicleReservationCallbackServiceRole", plt.getRole2().getName());
    }

    public void testSyncUpdateRenameAndReferences() throws Exception {
        WSDLModel model = setup(NamespaceLocation.VEHICLE);
        
        PartnerLinkType plt = find(model, "VehicleReservationPartnerLinkType", PartnerLinkType.class);
        Role role1 = plt.getRole1();
        RolePortType rpt = role1.getPortType();
        
        Util.setDocumentContentTo(model, "resources/Vehicle_PartnerLinkChanged2.wsdl");
        model.sync();
        
        listener.assertChildAddedEvent(role1);
        listener.assertChildRemovedEvent(role1);
        assertEquals("VehicleReservationPortType2", role1.getPortType().getPortType().get().getName());
    }

    public void testOperations() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TestOperations.wsdl");
        listener = new TestComponentListener();
        model.addComponentListener(listener);
        
        Definitions d = model.getDefinitions();
        PortType pt = d.getPortTypes().iterator().next();
        //FIXME XDM 
        // after sync element.getNamspaceURI return null
        // work-around for now is expand all operations children before sync
        for (Operation o : pt.getOperations()) {
            o.getChildren();
        }
        
        Util.setDocumentContentTo(model, "resources/TestOperations_after.wsdl");
        model.sync();
        
        List<Operation> ops = new ArrayList<Operation>(pt.getOperations());
        Operation op1 = null, op2 = null, op3 = null, op4 = null;
        for (Operation op : ops) {
            if (op.getName().equals("op1")) {
                op1 = op;
            } else if (op.getName().equals("op2")) {
                op2 = op;
            } else if (op.getName().equals("op3")) {
                op3 = op;
            } else if (op.getName().equals("op4")) {
                op4 = op;
            }
        }
        
        assertTrue("op1 transformed to notification "+op1, op1 instanceof NotificationOperation);
        assertTrue("op2 transformed to solicit-response "+op2, op2 instanceof SolicitResponseOperation);
        assertTrue("op3 transformed to request-response "+op3, op3 instanceof RequestResponseOperation);
        assertTrue("op4 transformed to one-way "+op4, op4 instanceof OneWayOperation);
    }
    
    public void testSyncUpdateSchema() throws Exception {
        WSDLModel model = setup(NamespaceLocation.TRAVEL);
        Schema schema = model.getDefinitions().getTypes().getSchemas().iterator().next();
        Util.setDocumentContentTo(model, "resources/TravelReservationService_1.wsdl");
        model.sync();
        
        listener.assertChangeEvent(schema);
        assertEquals("schema.targetNamespace", "http://www.sun.com/javaone/06/TravelReservationService", schema.getTargetNamespace());
        
        GlobalElement fault = schema.getElements().iterator().next();
        Util.setDocumentContentTo(model, "resources/TravelReservationService_schemaChanged.wsdl");
        model.sync();

        listener.assertChangeEvent(fault);
        assertEquals("import namespace", "xs:long", fault.getType().getDeclaredURI());
        listener.assertChildAddedEvent(schema);
        assertEquals("added element", 2, schema.getElements().size());

        Util.setDocumentContentTo(model, "resources/TravelReservationService_1.wsdl");
        model.sync();
        
        listener.assertChildRemovedEvent(schema);
        assertEquals("added element", 1, schema.getElements().size());
    }
}
