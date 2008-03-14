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
import org.netbeans.modules.iep.model.InputOperatorComponent;
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
    	IEPModel model = provider.getWLMModel(wfFile);
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
        
        InputOperatorComponent streamInput  = (InputOperatorComponent) operatorComponentChildren.get(0);
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
        
        
        //now set ref to InputOperatorComponent
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
        
//        //RelationAggregator Operator properites
//         List<Property> relationAggregatorComponentProperties = componentRelationAggregatorOperator.getProperties();
//         
//         //1
//         Property relationAggregatorComponentProperty1 = relationAggregatorComponentProperties.get(0);
//         assertEquals("x", relationAggregatorComponentProperty1.getName());
//         assertEquals("285", relationAggregatorComponentProperty1.getValue());
//       
//         //2
//         Property relationAggregatorComponentProperty2 = relationAggregatorComponentProperties.get(1);
//         assertEquals("y", relationAggregatorComponentProperty2.getName());
//         assertEquals("46", relationAggregatorComponentProperty2.getValue());
//         
//         //3
//         Property relationAggregatorComponentProperty3 = relationAggregatorComponentProperties.get(2);
//         assertEquals("z", relationAggregatorComponentProperty3.getName());
//         assertEquals("0", relationAggregatorComponentProperty3.getValue());
//         
//         //4
//         Property relationAggregatorComponentProperty4 = relationAggregatorComponentProperties.get(3);
//         assertEquals("id", relationAggregatorComponentProperty4.getName());
//         assertEquals("o0", relationAggregatorComponentProperty4.getValue());
//         
//         //5
//         Property relationAggregatorComponentProperty5 = relationAggregatorComponentProperties.get(4);
//         assertEquals("name", relationAggregatorComponentProperty5.getName());
//         assertEquals("RelationAggregator0", relationAggregatorComponentProperty5.getValue());
//         
//         //6
//         Property relationAggregatorComponentProperty6 = relationAggregatorComponentProperties.get(5);
//         assertEquals("inputSchemaIdList", relationAggregatorComponentProperty6.getName());
//         assertEquals("schema1", relationAggregatorComponentProperty6.getValue());
//         
//         //7
//         Property relationAggregatorComponentProperty7 = relationAggregatorComponentProperties.get(6);
//         assertEquals("outputSchemaId", relationAggregatorComponentProperty7.getName());
//         assertEquals("schema2", relationAggregatorComponentProperty7.getValue());
//         
//         //8
//         Property relationAggregatorComponentProperty8 = relationAggregatorComponentProperties.get(7);
//         assertEquals("description", relationAggregatorComponentProperty8.getName());
//         assertEquals("", relationAggregatorComponentProperty8.getValue());
//         
//         //9
//         Property relationAggregatorComponentProperty9 = relationAggregatorComponentProperties.get(8);
//         assertEquals("topoScore", relationAggregatorComponentProperty9.getName());
//         assertEquals("2", relationAggregatorComponentProperty9.getValue());
//         
//         //10
//         Property relationAggregatorComponentProperty10 = relationAggregatorComponentProperties.get(9);
//         assertEquals("inputType", relationAggregatorComponentProperty10.getName());
//         assertEquals("i18n.IEP.IOType.relation", relationAggregatorComponentProperty10.getValue());
//         
//         //11
//         Property relationAggregatorComponentProperty11 = relationAggregatorComponentProperties.get(10);
//         assertEquals("inputIdList", relationAggregatorComponentProperty11.getName());
//         assertEquals("o3", relationAggregatorComponentProperty11.getValue());
//         
//         //12
//         Property relationAggregatorComponentProperty12 = relationAggregatorComponentProperties.get(11);
//         assertEquals("staticInputIdList", relationAggregatorComponentProperty12.getName());
//         assertEquals("", relationAggregatorComponentProperty12.getValue());
//         
//         //13
//         Property relationAggregatorComponentProperty13 = relationAggregatorComponentProperties.get(12);
//         assertEquals("outputType", relationAggregatorComponentProperty13.getName());
//         assertEquals("i18n.IEP.IOType.relation", relationAggregatorComponentProperty13.getValue());
//         
//         //14
//         Property relationAggregatorComponentProperty14 = relationAggregatorComponentProperties.get(13);
//         assertEquals("isGlobal", relationAggregatorComponentProperty14.getName());
//         assertEquals("false", relationAggregatorComponentProperty14.getValue());
//         
//         //15
//         Property relationAggregatorComponentProperty15 = relationAggregatorComponentProperties.get(14);
//         assertEquals("globalId", relationAggregatorComponentProperty15.getName());
//         assertEquals("", relationAggregatorComponentProperty15.getValue());
//         
//         //16
//         Property relationAggregatorComponentProperty16 = relationAggregatorComponentProperties.get(15);
//         assertEquals("batchMode", relationAggregatorComponentProperty16.getName());
//         assertEquals("false", relationAggregatorComponentProperty16.getValue());
//         
//         //17
//         Property relationAggregatorComponentProperty17 = relationAggregatorComponentProperties.get(16);
//         assertEquals("fromColumnList", relationAggregatorComponentProperty17.getName());
//         assertEquals("GapWindow0.PRICE", relationAggregatorComponentProperty17.getValue());
//         
//         //18
//         Property relationAggregatorComponentProperty18 = relationAggregatorComponentProperties.get(17);
//         assertEquals("toColumnList", relationAggregatorComponentProperty18.getName());
//         assertEquals("PRICE", relationAggregatorComponentProperty18.getValue());
//         
//         //19
//         Property relationAggregatorComponentProperty19 = relationAggregatorComponentProperties.get(18);
//         assertEquals("groupByColumnList", relationAggregatorComponentProperty19.getName());
//         assertEquals("GapWindow0.PRICE", relationAggregatorComponentProperty19.getValue());
//         
//         //20
//         Property relationAggregatorComponentProperty20 = relationAggregatorComponentProperties.get(19);
//         assertEquals("whereClause", relationAggregatorComponentProperty20.getName());
//         assertEquals("", relationAggregatorComponentProperty20.getValue());
//         
//         //links
//         
//        Component linksComponent  = childComponents.get(3);
//        assertEquals("Links", linksComponent.getName());
//        assertEquals("Links", linksComponent.getTitle());
//        assertEquals("/IEP/Model/Plan|Links", linksComponent.getType());
//        
//        List<Component> linksComponentChildren = linksComponent.getChildComponents();
//        assertEquals(7, linksComponentChildren.size());
//        
//        Component linksComponentLink0  = linksComponentChildren.get(0);
//        assertEquals("link0", linksComponentLink0.getName());
//        assertEquals("link0", linksComponentLink0.getTitle());
//        assertEquals("/IEP/Model/Link", linksComponentLink0.getType());
//        
//        //link0 properites
//         List<Property> linksComponentLink0Properties = linksComponentLink0.getProperties();
//         
//         //1
//         Property linksComponentLink0Property1 = linksComponentLink0Properties.get(0);
//         assertEquals("name", linksComponentLink0Property1.getName());
//         assertEquals("link0", linksComponentLink0Property1.getValue());
//       
//        //2
//         Property linksComponentLink0Property2 = linksComponentLink0Properties.get(1);
//         assertEquals("from", linksComponentLink0Property2.getName());
//         assertEquals("o1", linksComponentLink0Property2.getValue());
//       
//         //3
//         Property linksComponentLink0Property3 = linksComponentLink0Properties.get(2);
//         assertEquals("to", linksComponentLink0Property3.getName());
//         assertEquals("o3", linksComponentLink0Property3.getValue());
//       
    } 
    
}

