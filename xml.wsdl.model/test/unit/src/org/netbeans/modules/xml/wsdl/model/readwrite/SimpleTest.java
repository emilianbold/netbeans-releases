package org.netbeans.modules.xml.wsdl.model.readwrite;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPQName;
import org.netbeans.modules.xml.wsdl.model.visitor.FindWSDLComponent;
import org.netbeans.modules.xml.xam.AbstractComponent;

/**
 *
 * @author Nam Nguyen
 */
public class SimpleTest extends TestCase implements TestReadWrite {
    
    public SimpleTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testWrite() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/empty.wsdl");
        model.startTransaction();
        Definitions d = model.getDefinitions();
        d.setName("HelloService");
        d.setTargetNamespace("urn:HelloService/wsdl");
        WSDLComponentFactory fact = d.getWSDLModel().getFactory();
        
        Message m1 = fact.createMessage();
        Message m2 = fact.createMessage();
        d.addMessage(m1); d.addMessage(m2);        
        m1.setName("HelloServiceSEI_sayHello");
        m2.setName("HelloServiceSEI_sayHelloResponse");
        Part p = fact.createPart();
        p.setName("String_1"); //TODO test setType
        m1.addPart(p);
        p = fact.createPart();
        p.setName("String_2"); 
        m1.addPart(p);
        p = fact.createPart();
        p.setName("result"); 
        m2.addPart(p);

        PortType pt = fact.createPortType();
        d.addPortType(pt);
        pt.setName("HelloServiceSEI");
        RequestResponseOperation op = fact.createRequestResponseOperation();
        pt.addOperation(op);
        op.setName("sayHello");
        op.setParameterOrder(Arrays.asList(new String[] {"String_1", "String_2"}));
        Input in = fact.createInput();
        op.setInput(in);
        in.setMessage(in.createReferenceTo(m1, Message.class));
        Output out = fact.createOutput();
        op.setOutput(out);
        out.setMessage(out.createReferenceTo(m2, Message.class));
        
        Binding b = fact.createBinding();
        d.addBinding(b);
        b.setName("HelloServiceSEIBinding");
        b.setType(b.createReferenceTo(pt, PortType.class));
        SOAPBinding sb = fact.createSOAPBinding();
        sb.setTransportURI("http://schemas.xmlsoap.org/soap/http");
        sb.setStyle(SOAPBinding.Style.RPC);
        b.addExtensibilityElement(sb);
        BindingOperation bo = fact.createBindingOperation();
        b.addBindingOperation(bo);
        bo.setName("sayHello");
        SOAPOperation soo = fact.createSOAPOperation();
        bo.addExtensibilityElement(soo);
        soo.setSoapAction("");
        
        BindingInput bin = fact.createBindingInput();
        bo.setBindingInput(bin);
        SOAPBody body = fact.createSOAPBody();
        bin.addExtensibilityElement(body);
        body.setUse(SOAPMessageBase.Use.LITERAL);
        body.setNamespaceURI("urn:HelloService/wsdl");
        
        BindingOutput bout = fact.createBindingOutput();
        bo.setBindingOutput(bout);
        body = fact.createSOAPBody();
        bout.addExtensibilityElement(body);
        body.setUse(SOAPMessageBase.Use.LITERAL);
        body.setNamespaceURI("urn:HelloService/wsdl");
        
        Service service = fact.createService();
        d.addService(service);
        service.setName("HelloService");
        Port port = fact.createPort();
        service.addPort(port);
        port.setName("HelloServiceSEIPort");
        port.setBinding(port.createReferenceTo(b, Binding.class));
        SOAPAddress sad = fact.createSOAPAddress();
        port.addExtensibilityElement(sad);
        sad.setLocation("REPLACE_WITH_ACTUAL_URL");
        model.endTransaction();
        
        readAndCheck(model);
    }

    public void testRead() throws Exception {
        WSDLModel model = Util.loadWSDLModel(getTestResourcePath());
        readAndCheck(model);
    }
    
    private void readAndCheck(WSDLModel model) {
        Definitions d = model.getDefinitions();
        Collection<Message> messages = d.getMessages();
        assertEquals("read.message.count", 2, messages.size());
        Iterator<Message> it = messages.iterator(); it.next();
        Message m = it.next();
        AbstractComponent acm = (AbstractComponent) m;
        String prefix = acm.getPeer().getPrefix();
        assertTrue("wsdl prefix is not null or empty: "+prefix, prefix == null || prefix.equals(""));
        assertEquals("read.message.name", "HelloServiceSEI_sayHelloResponse", m.getName());
        Collection<Part> parts = m.getParts();
        assertEquals("read.message.part.name", "result", parts.iterator().next().getName());

        Collection<PortType> porttypes = d.getPortTypes();
        assertEquals("read.portType", 1, porttypes.size());
        PortType pt = porttypes.iterator().next();
        assertEquals("read.portType.name", "HelloServiceSEI", pt.getName());
        Operation op = pt.getOperations().iterator().next();
        assertTrue("read.portType.operation", op instanceof RequestResponseOperation);
        assertEquals("read.portType.operation.parameterOrder", "[String_1, String_2]", op.getParameterOrder().toString());
        
        Message m1 = d.getMessages().iterator().next();
        assertEquals("message[1].name", "HelloServiceSEI_sayHello", m1.getName());
        assertEquals("message[1].parts.count=2", 2, m1.getParts().size());

        String xpath0 = "/definitions/message[1]";
        WSDLComponent found = (WSDLComponent) new FindWSDLComponent().findComponent(model.getRootComponent(), xpath0);
        assertTrue(xpath0, found instanceof Message);
        Message m1x = (Message) found;
        assertEquals("write.xpath", m1, m1x);

        RequestResponseOperation rro = (RequestResponseOperation) op;
        assertEquals("operation.name", "sayHello", rro.getName());
        Input in = rro.getInput();
        assertEquals("portType.operation.input.message", messages.iterator().next(), in.getMessage().get());

        Output out = rro.getOutput();
        assertEquals("portType.operation.output.message", m.getName(), out.getMessage().get().getName());
        
        Binding b = d.getBindings().iterator().next();
        Collection<SOAPBinding> soapB = b.getExtensibilityElements(SOAPBinding.class);
        SOAPBinding sb = soapB.iterator().next();
        assertEquals("binding.soap.style", SOAPBinding.Style.RPC, sb.getStyle());
        assertEquals("binding.soap.uri", "http://schemas.xmlsoap.org/soap/http", sb.getTransportURI());

        BindingOperation bo = b.getBindingOperations().iterator().next();
        Collection<SOAPOperation> soapOps = bo.getExtensibilityElements(SOAPOperation.class);
        assertEquals("binding.soap.style", SOAPBinding.Style.RPC, soapOps.iterator().next().getStyle());
        assertEquals("binding.soap.uri", "", soapOps.iterator().next().getSoapAction());

        assertEquals("binding.type", pt, b.getType().get());
        assertEquals("binding.name", "HelloServiceSEIBinding", b.getName());
        SOAPBody body = (SOAPBody) bo.getBindingInput().getExtensibilityElements().iterator().next();
        assertEquals("binding.operation.input", SOAPBody.Use.LITERAL, body.getUse());
        SOAPBody body2 = (SOAPBody) bo.getBindingOutput().getExtensibilityElements().iterator().next();
        assertEquals("binding.operation.output", "urn:HelloService/wsdl", body2.getNamespaceURI());
        
        Service s = d.getServices().iterator().next();
        assertEquals("serice.name", "HelloService", s.getName());
        Port p = s.getPorts().iterator().next();
        assertEquals("service.port.name", "HelloServiceSEIPort", p.getName());
        Binding binding = p.getBinding().get();
        assertEquals("service.port.binding", b, binding);
        SOAPAddress soapAddress = p.getExtensibilityElements(SOAPAddress.class).iterator().next();
        assertEquals("service.port.soapAddress", "REPLACE_WITH_ACTUAL_URL", soapAddress.getLocation());
        
    }

    public String getTestResourcePath() {
        return "resources/HelloService.wsdl";
    }
    
}
