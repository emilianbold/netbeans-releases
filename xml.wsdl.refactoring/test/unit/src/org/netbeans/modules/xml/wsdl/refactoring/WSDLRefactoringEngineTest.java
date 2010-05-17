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

package org.netbeans.modules.xml.wsdl.refactoring;

import java.io.IOException;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.netbeans.modules.xml.refactoring.XMLRefactoringTransaction;
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
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Referenceable;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Nam Nguyen
 */
public class WSDLRefactoringEngineTest extends TestCase {

//    private WSDLRefactoringEngine engine;
//    private ChangeExecutor exec;
    
    public WSDLRefactoringEngineTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
       // exec = new WSDLChangeExecutor();
       // engine = new WSDLRefactoringEngine();
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WSDLRefactoringEngineTest.class);
        return suite;
    }
    
    private void createRenameRequestAndExecute(Nameable<WSDLComponent> target, WSDLModel model) throws IOException {
        //RenameRequest rr = new RenameRequest(target, "renamed");
        RenameRefactoring refactoring = new RenameRefactoring(Lookups.singleton(target));
        XMLRefactoringTransaction transaction = new XMLRefactoringTransaction((Referenceable)target, refactoring);
        refactoring.getContext().add(transaction);
        refactoring.getContext().add(target.getName());
        refactoring.getContext().add((Component)model.getDefinitions());
        RefactoringSession session = RefactoringSession.create("Test rename");
        refactoring.setNewName("renamed");
        assertNull(refactoring.prepare(session));
        session.doRefactoring(true);
        
   }
    
    public void testMessage() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/stockquote.wsdl");
        Message target = (Message) Util.findByXpath(model, "/definitions/message[@name='GetLastTradePriceInput']");
        createRenameRequestAndExecute(target, model);
        
        Input fixed1 = (Input) Util.findByXpath(model, "/definitions/portType/operation/input");
        assertEquals("tns:renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("message"));
        
        // BindingInput.getInput is just implicit and should not be changed
        BindingInput bi = (BindingInput) Util.findByXpath(model, "/definitions/binding/operation/input");
        assertNull(((AbstractDocumentComponent)bi).getPeer().getAttributeNode("name"));
    }
    
    public void testBinding() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService.wsdl");
        Binding target = (Binding) Util.findByXpath(model, "/definitions/binding[@name='TravelReservationSoapBinding']");
           
        createRenameRequestAndExecute(target, model);

        Port fixed1 = (Port) Util.findByXpath(model, "/definitions/service/port");
        assertEquals("tns:renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("binding"));
    }

    public void testFault() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService.wsdl");
        Fault target = (Fault) Util.findByXpath(model, 
                "/definitions/portType[@name='TravelReservationPortType']/operation/fault[@name='itineraryProblem']");
              
        createRenameRequestAndExecute(target, model);

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
        createRenameRequestAndExecute(target,model);

        BindingOperation fixed1 = (BindingOperation) Util.findByXpath(model, "/definitions/binding/operation");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("name"));
    }
    
    public void testPart() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/uddi.wsdl");
        WSDLRefactoringEngine engine = new WSDLRefactoringEngine();
        Part target = (Part) Util.findByXpath(model, 
                "/definitions/message[@name='bindingDetail']/part[@name='body']");
        createRenameRequestAndExecute(target, model);

        SOAPBody fixed1 = (SOAPBody) Util.findByXpath(model, "/definitions/binding/operation[@name='find_binding']/output/soap:body");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("parts"));
        SOAPBody fixed2 = (SOAPBody) Util.findByXpath(model, "/definitions/binding/operation[@name='get_bindingDetail']/output/soap:body");
        assertEquals("renamed", ((AbstractDocumentComponent)fixed2).getPeer().getAttribute("parts"));
        
    }
    
    public void testInput() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/uddi.wsdl");
        Input target = (Input) Util.findByXpath(model, 
                "/definitions/portType/operation[@name='find_binding']/input");
        createRenameRequestAndExecute(target, model);

        BindingInput fixed1 = (BindingInput) Util.findByXpath(model, "/definitions/binding/operation[@name='find_binding']/input");
        // Still have binding/input@name implicit.
        assertNull(((AbstractDocumentComponent)fixed1).getPeer().getAttributeNode("name"));
    }
    
    public void testOutput() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/uddi.wsdl");
        Output target = (Output) Util.findByXpath(model, 
                "/definitions/portType/operation[@name='find_binding']/output");
             
        createRenameRequestAndExecute(target, model);

        BindingOutput fixed1 = (BindingOutput) Util.findByXpath(model, "/definitions/binding/operation[@name='find_binding']/output");
        // Still have binding/output@name implicit.
        assertNull(((AbstractDocumentComponent)fixed1).getPeer().getAttributeNode("name"));
    }

    public void testPortType() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/TravelReservationService.wsdl");
        PortType target = (PortType) Util.findByXpath(model, 
                "/definitions/portType[@name='TravelReservationPortType']");
               
        createRenameRequestAndExecute(target, model);

        Binding fixed1 = (Binding) Util.findByXpath(model, "/definitions/binding");
        assertEquals("tns:renamed", ((AbstractDocumentComponent)fixed1).getPeer().getAttribute("type"));
    }
    
}
