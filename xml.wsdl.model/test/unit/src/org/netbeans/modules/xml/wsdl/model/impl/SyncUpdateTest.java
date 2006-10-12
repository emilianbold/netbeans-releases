package org.netbeans.modules.xml.wsdl.model.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import junit.framework.TestCase;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.w3c.dom.Element;

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
                if (e.getEventType() == ComponentEvent.EventType.VALUE_CHANGED &&
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
        
        public void assertEventCount(int count) {
            assertEquals("Event count", count, events.size());
        }
        
        public void reset() { events.clear(); events = null; }
        
    }
    
    protected void setUp() throws Exception {
    }
    
    private WSDLModel setup(NamespaceLocation wsdl) throws Exception {
        WSDLModel m = TestCatalogModel.getDefault().getWSDLModel(wsdl);
        listener = new TestComponentListener();
        m.addComponentListener(listener);
        return m;
    }
    
    protected void tearDown() throws Exception {
        if (listener != null) listener.reset();
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public <T extends ReferenceableWSDLComponent> T find(WSDLModel model, String name, Class<T> type) {
        return new FindReferencedVisitor<T>(model.getDefinitions()).find(name, type);
    }

    public void testOperations() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TestOperations.wsdl");
        listener = new TestComponentListener();
        model.addComponentListener(listener);
     
        Definitions d = model.getDefinitions();
        PortType pt = d.getPortTypes().iterator().next();
     
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
        assertEquals("schema.targetNamespace", "http://www.sun.com/javaone/05/TravelReservationService", schema.getTargetNamespace());
     
        GlobalElement fault = schema.getElements().iterator().next();
        Util.setDocumentContentTo(model, "resources/TravelReservationService_schemaChanged.wsdl");
        model.sync();
     
        assertEquals("embedded attribute changed", "xs:long", fault.getType().getRefString());
        listener.assertChildAddedEvent(schema);
        assertEquals("embedded element changed", 2, schema.getElements().size());
     
        Util.setDocumentContentTo(model, "resources/TravelReservationService_1.wsdl");
        model.sync();
     
        assertEquals("added element", 1, schema.getElements().size());
    }
    
     public void testFind() throws Exception{
        WSDLModel model = Util.loadWSDLModel("resources/HelloService.wsdl");
        listener = new TestComponentListener();
        model.addComponentListener(listener);
        Message message = find(model, "HelloServiceSEI_sayHello", Message.class);
        this.assertNotNull("Could not find message HelloServiceSEI_sayHello", message);
        Part part = find(model, "String_1", Part.class);
        assertNotNull("Could not find part String_1", part);
        NamedComponentReference<GlobalType> gtRef = part.getType();
        assertNotNull("could not find part type reference object", gtRef);
        GlobalType gt = gtRef.get();
        assertNotNull("could not find part simple type", gt);
        PortType portType = find(model, "HelloServiceSEI", PortType.class);
        this.assertNotNull("Could not find portType HelloServiceSEI", portType);
        RequestResponseOperation operation = find(model, "sayHello", RequestResponseOperation.class);
        assertNotNull("Could not find operation sayHello", operation);
        Binding binding = find(model, "HelloServiceSEIBinding", Binding.class);
        this.assertNotNull("Could not find binding HelloServiceSEIBindin", binding);
        Service service = find(model, "HelloService", Service.class);
        this.assertNotNull("Could not find service HelloService", service);
        Port port = find(model, "HelloServiceSEIPort", Port.class);
        assertNotNull("Could not find port HelloServiceSEIPort", port);
    }
     
     public void testWholeFileSync() throws Exception {
        WSDLModel model = setup(NamespaceLocation.ECHOCONCAT);
        
        Util.setDocumentContentTo(model, "resources/ParkingLotManager.wsdl");
        model.sync();
     
        listener.assertEventCount(6);
        listener.assertChangeEvent(model.getDefinitions());
        listener.assertChildAddedEvent(model.getDefinitions());
        listener.assertChildRemovedEvent(model.getDefinitions());
        Schema schema = model.getDefinitions().getTypes().getSchemas().iterator().next();
        listener.assertChangeEvent(schema);
        listener.assertChildAddedEvent(schema);
        listener.assertChildRemovedEvent(schema);
     }
     
     public void testRenameInput() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/synctest1.wsdl");
        
        Util.setDocumentContentTo(model, "resources/synctest1_renamedInput.wsdl");
        model.sync();
        PortType pt = model.findComponentByName("portType_0", PortType.class);
        assertEquals(1, pt.getOperations().size());
        assertEquals("input1", pt.getOperations().iterator().next().getInput().getName());
     }
     
     public void testUpdatePeerOnInlineSchema() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/template.wsdl");
        WSDLSchema schema = model.getDefinitions().getTypes().getExtensibilityElements(WSDLSchema.class).get(0);
        Element saved = schema.getPeer();
        
        Util.setDocumentContentTo(model, "resources/simple-exit.wsdl");
        model.sync();
        
        Document doc = model.getBaseDocument();
        String wsdlText = doc.getText(0, doc.getLength());
        schema = model.getDefinitions().getTypes().getExtensibilityElements(WSDLSchema.class).get(0);
        assertNotSame("Embedded schema node should be updated", saved, schema.getPeer());
        String schemaText = schema.getContentFragment();
        assertTrue(schemaText, wsdlText.indexOf(schemaText) > -1);
     }
     
     public void testChangedXsdImportNS() throws Exception {
         WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService_badImportNS.wsdl");
         WSDLSchema schema = model.getDefinitions().getTypes().getExtensibilityElements(WSDLSchema.class).get(0);
         Element saved = schema.getPeer();

        org.netbeans.modules.xml.schema.model.Import xsdImport = 
             schema.getSchemaModel().getSchema().getImports().iterator().next();
         assertEquals("blah", xsdImport.getNamespace());
         
         Util.setDocumentContentTo(model, "resources/TravelReservationService_schemaChanged.wsdl");
         model.sync();
         assertEquals("http://www.opentravel.org/OTA/2003/05", xsdImport.getNamespace());
         
         assertNotSame("Embedded schema node should be updated", saved, schema.getPeer());
         Document doc = model.getBaseDocument();
         String wsdlText = doc.getText(0, doc.getLength());
         String schemaText = schema.getContentFragment();
         assertTrue(schemaText, wsdlText.indexOf(schemaText) > -1);
     }

     public void testTwoTypes() throws Exception {
         WSDLModel model = Util.loadWSDLModel("resources/empty.wsdl");
         Definitions def = model.getDefinitions();
         
         Util.setDocumentContentTo(model, "resources/TwoTypes.wsdl");
         model.sync();
         assertEquals("generic getChildren returns all", 2, def.getChildren(Types.class).size());
         Types types = def.getTypes();
         assertEquals("foo", types.getSchemas().iterator().next().getTargetNamespace());
         types = new ArrayList<Types>(def.getChildren(Types.class)).get(1);
         assertEquals("bar", types.getSchemas().iterator().next().getTargetNamespace());
         
         Util.setDocumentContentTo(model, "resources/OneTypes.wsdl");
         model.sync();
         assertEquals(1, def.getChildren(Types.class).size());
         types = def.getTypes();
         assertEquals("foo", types.getSchemas().iterator().next().getTargetNamespace());
     }

     public void testEmptyOperation() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TestOperations.wsdl");
        Definitions d = model.getDefinitions();

        try {
            Util.setDocumentContentTo(model, "resources/TestEmptyOperation.wsdl");
            model.sync();
            assertFalse("Should get IOException on empty operation", true);
        } catch(IOException e) {
            // good
            assertEquals(Model.State.NOT_WELL_FORMED, model.getState());
        }
     }
}
