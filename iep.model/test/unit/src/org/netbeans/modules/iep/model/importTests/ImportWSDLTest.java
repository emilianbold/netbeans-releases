/*
 * ImportWSDLTest.java
 * JUnit based test
 *
 * Created on May 31, 2007, 3:10 PM
 */

package org.netbeans.modules.iep.model.importTests;

import org.netbeans.modules.iep.model.common.*;
import java.net.URI;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.Import;
import org.netbeans.modules.iep.model.Property;

import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author radval
 */
public class ImportWSDLTest extends TestCase {
    
    
    
    public ImportWSDLTest(String testName) {
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
        
        //FileObject wsdlFileObject = ModelHelper.resolveWSDLFileObject(model, imp);
        //ModelSource source = TestCatalogModel.getDefault().getModelSource(wsdlFileObject.getURL().toURI());
        //WSDLModel wsdlModel = ModelHelper.getWSDLModel(source);
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
//        Component componentOperators  = childComponents.get(2);
//        assertEquals("Operators", componentOperators.getName());
//        assertEquals("Operators", componentOperators.getTitle());
//        assertEquals("/IEP/Model/Plan|Operators", componentOperators.getType());
//        
//        List<Component> operatorComponentChildren = componentOperators.getChildComponents();
//        assertEquals(9, operatorComponentChildren.size());
//        
//        Component componentRelationAggregatorOperator  = operatorComponentChildren.get(0);
//        assertEquals("o0", componentRelationAggregatorOperator.getName());
//        assertEquals("o0", componentRelationAggregatorOperator.getTitle());
//        assertEquals("/IEP/Operator/RelationAggregator", componentRelationAggregatorOperator.getType());
//        
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
