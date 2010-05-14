/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.iep.model.importTests;

import org.netbeans.modules.iep.model.common.*;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.Import;
import org.netbeans.modules.iep.model.WsOperatorComponent;
import org.netbeans.modules.iep.model.Property;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class WsdlReferenceTest extends TestCase {
    
    
    
    public WsdlReferenceTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
 
    }
    
    public void testImportWSDL()  throws Exception{
        URL url = ImportWSDLTest.class.getResource("data/PurchaseOrderEventProcess.iep");
        URI wfFile = url.toURI();
        IEPModelProvider provider = new IEPModelProviderInsideIde ();
        IEPModel model = provider.getIEPModel(wfFile);
           model.sync();
        assertNotNull(model);

        //test for import element
        assertEquals(1, model.getPlanComponent().getImports().size());
        
        Import imp = model.getPlanComponent().getImports().get(0);
        assertEquals("http://schemas.xmlsoap.org/wsdl/", imp.getImportType());
        assertEquals("http://j2ee.netbeans.org/wsdl/PurchaseOrder", imp.getNamespace());
        assertEquals("PurchaseOrder.wsdl", imp.getLocation());
        
        WSDLModel wsdlModel = imp.getImportedWSDLModel();
        assertNotNull(wsdlModel);
        
        
        //assert properites on the root component
        assertEquals(1, model.getPlanComponent().getProperties().size());

        //assert child components on the root component
        assertEquals(4, model.getPlanComponent().getChildComponents().size());
        
        List<Component> childComponents = model.getPlanComponent().getChildComponents();
        
        Component metadataComponent  = childComponents.get(0);
        assertEquals("Metadata", metadataComponent.getName());
        assertEquals("Metadata", metadataComponent.getTitle());
        assertEquals("/IEP/Model/Plan|Metadata", metadataComponent.getType());
        
        List<Component> metadataComponentChildren = metadataComponent.getChildComponents();
        assertEquals(1, metadataComponentChildren.size());
        
        Component viewComponent  = metadataComponentChildren.get(0);
        assertEquals("View", viewComponent.getName());
        assertEquals("View", viewComponent.getTitle());
        assertEquals("/IEP/Model/Plan|Metadata|View", viewComponent.getType());
        
        List<Property> viewComponentProperties = viewComponent.getProperties();
        assertEquals(1,  viewComponentProperties.size());
 
        Property property1_1 = viewComponentProperties.get(0);
        assertEquals("orthoflow", property1_1.getName());
        assertEquals("false", property1_1.getValue());
        
        
        
//        Component schemaComponent  = childComponents.get(1);
//        assertEquals("Schemas", schemaComponent.getName());
//        assertEquals("Schemas", schemaComponent.getTitle());
//        assertEquals("/IEP/Model/Plan|Schemas", schemaComponent.getType());
//        List<Component> schemaComponentChildren = schemaComponent.getChildComponents();
//        assertEquals(6, schemaComponentChildren.size());
//        
//        
        Component componentOperators  = childComponents.get(2);
        assertEquals("Operators", componentOperators.getName());
        assertEquals("Operators", componentOperators.getTitle());
        assertEquals("/IEP/Model/Plan|Operators", componentOperators.getType());
        
        List<Component> operatorComponentChildren = componentOperators.getChildComponents();
        assertEquals(1, operatorComponentChildren.size());
        
        WsOperatorComponent streamInput  = (WsOperatorComponent) operatorComponentChildren.get(0);
        assertEquals("o0", streamInput.getName());
        assertEquals("o0", streamInput.getTitle());
        assertEquals("/IEP/Input/StreamInput", streamInput.getType());
        
        //now set portType, operation and message reference
        Collection<PortType> pts = wsdlModel.getDefinitions().getPortTypes();
        PortType pt = pts.iterator().next();
        assertNotNull(pt);
        
        Collection<Operation> ops = pt.getOperations();
        Operation op = ops.iterator().next();
        assertNotNull(ops);
        
        NamedComponentReference<Message> msgRef = op.getInput().getMessage();
        assertNotNull(msgRef);
        
        
        //now set ref to WsOperatorComponent
        model.startTransaction();
        
        streamInput.setPortType(pt.createReferenceTo(pt, PortType.class));
        streamInput.setOperation(op.createReferenceTo(op, Operation.class));
        streamInput.setMessage(msgRef);

        model.endTransaction();
        
        //now check if reference can be obtained by
        //getter
        NamedComponentReference<PortType> ptGRef = streamInput.getPortType();
        PortType ptg = ptGRef.get();
        
        assertEquals(pt, ptg);
        
        
        NamedComponentReference<Operation> opGRef = streamInput.getOperation();
        Operation opg = opGRef.get();
        
        assertEquals(op, opg);
        
        
        NamedComponentReference<Message> msgGRef = streamInput.getMessage();
        Message msgg = msgGRef.get();
        
        assertEquals(msgRef.get(), msgg);
        
    } 
    
    
    public void testExistingOperatorImplementingWSDL()  throws Exception{
        URL url = ImportWSDLTest.class.getResource("data/PurchaseOrderEventProcessImplementWsdl.iep");
        URI wfFile = url.toURI();
        IEPModelProvider provider = new IEPModelProviderInsideIde ();
        IEPModel model = provider.getIEPModel(wfFile);
           model.sync();
        assertNotNull(model);

        //test for import element
        assertEquals(1, model.getPlanComponent().getImports().size());
        
        Import imp = model.getPlanComponent().getImports().get(0);
        assertEquals("http://schemas.xmlsoap.org/wsdl/", imp.getImportType());
        assertEquals("http://j2ee.netbeans.org/wsdl/PurchaseOrder", imp.getNamespace());
        assertEquals("PurchaseOrder.wsdl", imp.getLocation());
        
        WSDLModel wsdlModel = imp.getImportedWSDLModel();
        assertNotNull(wsdlModel);
        
        
        //assert properites on the root component
        assertEquals(1, model.getPlanComponent().getProperties().size());

        //assert child components on the root component
        assertEquals(4, model.getPlanComponent().getChildComponents().size());
        
        List<Component> childComponents = model.getPlanComponent().getChildComponents();
        
        Component metadataComponent  = childComponents.get(0);
        assertEquals("Metadata", metadataComponent.getName());
        assertEquals("Metadata", metadataComponent.getTitle());
        assertEquals("/IEP/Model/Plan|Metadata", metadataComponent.getType());
        
        List<Component> metadataComponentChildren = metadataComponent.getChildComponents();
        assertEquals(1, metadataComponentChildren.size());
        
        Component viewComponent  = metadataComponentChildren.get(0);
        assertEquals("View", viewComponent.getName());
        assertEquals("View", viewComponent.getTitle());
        assertEquals("/IEP/Model/Plan|Metadata|View", viewComponent.getType());
        
        List<Property> viewComponentProperties = viewComponent.getProperties();
        assertEquals(1,  viewComponentProperties.size());
 
        Property property1_1 = viewComponentProperties.get(0);
        assertEquals("orthoflow", property1_1.getName());
        assertEquals("false", property1_1.getValue());
        
        
        
//        Component schemaComponent  = childComponents.get(1);
//        assertEquals("Schemas", schemaComponent.getName());
//        assertEquals("Schemas", schemaComponent.getTitle());
//        assertEquals("/IEP/Model/Plan|Schemas", schemaComponent.getType());
//        List<Component> schemaComponentChildren = schemaComponent.getChildComponents();
//        assertEquals(6, schemaComponentChildren.size());
//        
//        
        Component componentOperators  = childComponents.get(2);
        assertEquals("Operators", componentOperators.getName());
        assertEquals("Operators", componentOperators.getTitle());
        assertEquals("/IEP/Model/Plan|Operators", componentOperators.getType());
        
        List<Component> operatorComponentChildren = componentOperators.getChildComponents();
        assertEquals(1, operatorComponentChildren.size());
        
        WsOperatorComponent streamInput  = (WsOperatorComponent) operatorComponentChildren.get(0);
        assertEquals("o0", streamInput.getName());
        assertEquals("o0", streamInput.getTitle());
        assertEquals("/IEP/Input/StreamInput", streamInput.getType());
        
        
        //now check if reference can be obtained by
        //getter
        NamedComponentReference<PortType> ptGRef = streamInput.getPortType();
        PortType ptg = ptGRef.get();
        
        assertNotNull(ptg);
        
        
        NamedComponentReference<Operation> opGRef = streamInput.getOperation();
        Operation opg = opGRef.get();
        
        assertNotNull(opg);
        
        
        NamedComponentReference<Message> msgGRef = streamInput.getMessage();
        Message msgg = msgGRef.get();
        
        assertNotNull(msgg);
        
    } 
}

