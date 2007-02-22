/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.Usage;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.refactoring.UsageSet;
import org.netbeans.modules.xml.refactoring.spi.ChangeExecutor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPFault;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author Nam Nguyen
 */
public class WSDLRefactoringEngineTest extends TestCase {

    private WSDLRefactoringEngine engine;
    private ChangeExecutor exec;
    
    public WSDLRefactoringEngineTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        exec = new WSDLChangeExecutor();
        engine = new WSDLRefactoringEngine();
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WSDLRefactoringEngineTest.class);
        return suite;
    }
    
    private void createRenameRequestAndExecute(Nameable<WSDLComponent> target, List<UsageGroup> usages) throws IOException {
        RenameRequest rr = new RenameRequest(target, "renamed");
        exec.doChange(rr);
        assertTrue(rr.confirmChangePerformed());
        UsageSet usageSet = new UsageSet((Referenceable)target);
        usageSet.addUsages(usages);
        rr.setUsages(usageSet);
        engine.precheck(rr);
        engine.refactorUsages(rr);
    }
    
    public void testMessage() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/stockquote.wsdl");
        Message target = (Message) Util.findByXpath(model, "/definitions/message[@name='GetLastTradePriceInput']");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(2, items.size());
        assertEquals(target, ((Input)items.get(0).getComponent()).getMessage().get());

        createRenameRequestAndExecute(target, usages);
        
        Input fixed1 = (Input) Util.findByXpath(model, "/definitions/portType/operation/input");
        assertEquals("tns:renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("message"));
        
        // BindingInput.getInput is just implicit and should not be changed
        BindingInput bi = (BindingInput) Util.findByXpath(model, "/definitions/binding/operation/input");
        assertNull(((AbstractDocumentComponent)bi).getPeer().getAttribute("name"));
    }
    
    public void testBinding() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService.wsdl");
        Binding target = (Binding) Util.findByXpath(model, "/definitions/binding[@name='TravelReservationSoapBinding']");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(1, items.size());
        assertEquals(target, ((Port)items.get(0).getComponent()).getBinding().get());
        
        createRenameRequestAndExecute(target, usages);

        Port fixed1 = (Port) Util.findByXpath(model, "/definitions/service/port");
        assertEquals("tns:renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("binding"));
    }

    public void testFault() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService.wsdl");
        Fault target = (Fault) Util.findByXpath(model, 
                "/definitions/portType[@name='TravelReservationPortType']/operation/fault[@name='itineraryProblem']");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(2, items.size());
        assertEquals(target, ((BindingFault)items.get(0).getComponent()).getFault().get());
        assertEquals(target, ((SOAPFault)items.get(1).getComponent()).getFault().get());
        
        createRenameRequestAndExecute(target, usages);

        BindingFault fixed1 = (BindingFault) Util.findByXpath(model, "/definitions/binding/operation/fault");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("name"));
        SOAPFault fixed2 = (SOAPFault) Util.findByXpath(model, "/definitions/binding/operation/fault/soap:fault");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed2).getPeer().getAttribute("name"));
    }
    
    public void testOperation() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService.wsdl");
        WSDLRefactoringEngine engine = new WSDLRefactoringEngine();
        Operation target = (Operation) Util.findByXpath(model, 
                "/definitions/portType[@name='TravelReservationPortType']/operation[@name='buildItinerary']");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(1, items.size());
        assertEquals(target, ((BindingOperation) items.get(0).getComponent()).getOperation().get());

        createRenameRequestAndExecute(target, usages);

        BindingOperation fixed1 = (BindingOperation) Util.findByXpath(model, "/definitions/binding/operation");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("name"));
    }
    
    public void testPart() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/uddi.wsdl");
        WSDLRefactoringEngine engine = new WSDLRefactoringEngine();
        Part target = (Part) Util.findByXpath(model, 
                "/definitions/message[@name='bindingDetail']/part[@name='body']");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(2, items.size());
        assertTrue(((SOAPBody) items.get(1).getComponent()).getParts().contains("body"));

        createRenameRequestAndExecute(target, usages);

        SOAPBody fixed1 = (SOAPBody) Util.findByXpath(model, "/definitions/binding/operation[@name='find_binding']/output/soap:body");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("parts"));
        SOAPBody fixed2 = (SOAPBody) Util.findByXpath(model, "/definitions/binding/operation[@name='get_bindingDetail']/output/soap:body");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed2).getPeer().getAttribute("parts"));
        
    }
    
    public void testInput() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/uddi.wsdl");
        WSDLRefactoringEngine engine = new WSDLRefactoringEngine();
        Input target = (Input) Util.findByXpath(model, 
                "/definitions/portType/operation[@name='find_binding']/input");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(1, items.size());
        assertEquals(target, ((BindingInput) items.get(0).getComponent()).getInput().get());

        createRenameRequestAndExecute(target, usages);

        BindingInput fixed1 = (BindingInput) Util.findByXpath(model, "/definitions/binding/operation[@name='find_binding']/input");
        // Still have binding/input@name implicit.
        assertNull(((AbstractDocumentComponent)fixed1).getPeer().getAttribute("name"));
    }
    
    public void testOutput() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/uddi.wsdl");
        WSDLRefactoringEngine engine = new WSDLRefactoringEngine();
        Output target = (Output) Util.findByXpath(model, 
                "/definitions/portType/operation[@name='find_binding']/output");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(1, items.size());
        assertEquals(target, ((BindingOutput) items.get(0).getComponent()).getOutput().get());
        
        createRenameRequestAndExecute(target, usages);

        BindingOutput fixed1 = (BindingOutput) Util.findByXpath(model, "/definitions/binding/operation[@name='find_binding']/output");
        // Still have binding/output@name implicit.
        assertNull(((AbstractDocumentComponent)fixed1).getPeer().getAttribute("name"));
    }

    public void testPortType() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService.wsdl");
        WSDLRefactoringEngine engine = new WSDLRefactoringEngine();
        PortType target = (PortType) Util.findByXpath(model, 
                "/definitions/portType[@name='TravelReservationPortType']");
        List<UsageGroup> usages = engine.findUsages(target, model.getDefinitions());
        List<Usage> items = usages.get(0).getItems();
        assertEquals(1, items.size());
        assertEquals(target, ((Binding)items.get(0).getComponent()).getType().get());
        
        createRenameRequestAndExecute(target, usages);

        Binding fixed1 = (Binding) Util.findByXpath(model, "/definitions/binding");
        assertEquals("tns:renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("type"));
    }
    
}
