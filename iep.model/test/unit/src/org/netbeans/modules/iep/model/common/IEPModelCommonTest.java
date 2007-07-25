/*
 * WLMModelCommonTest.java
 * JUnit based test
 *
 * Created on May 31, 2007, 3:10 PM
 */

package org.netbeans.modules.iep.model.common;

import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import org.netbeans.modules.iep.model.Component;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.impl.IEPComponentFactoryImpl;
import org.netbeans.modules.iep.model.util.XmlUtil;

import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * 
 */
public class IEPModelCommonTest extends TestCase {
    
    
    
    public IEPModelCommonTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
 
    }
    
    public void testModification () throws Exception {
       	URI wfFile = getClass().getResource("data/test.iep").toURI();
    	InputStream is = getClass().getResourceAsStream("data/test.iep");
    	IEPModelProvider provider = new IEPModelProviderInsideIde ();
    	IEPModel model = provider.getWLMModel(wfFile);
    	
    	IEPComponentFactory factory = new IEPComponentFactoryImpl (model);

        /*
    	TTasks tasks =factory.createTasks(model);
    	model.setTasks(tasks);
    	TTask task = factory.createTask(model);
    	model.startTransaction();
    	tasks.addTask(task);
    	assertNull(task.getOperationAsString());
    	WSDLModel newModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.PURCHASE_ORDER);
    	Operation opt = newModel.getDefinitions().getPortTypes().iterator().next().getOperations().iterator().next();
    	task.setOperation(new OperationReference(opt, TaskImpl.class.cast (task)));
    	assertEquals("ns0:ApprovePurchase", task.getOperationAsString());
    	model.endTransaction();
    	assertEquals(1,  tasks.getImports().size());
        */
    }
    
    
    
    public void testCreateModel()  throws Exception{
    	URI wfFile = getClass().getResource("data/test.iep").toURI();
    	InputStream is = getClass().getResourceAsStream("data/test.iep");
    	InputSource source = new InputSource (is);
    	Element rootEl = XmlUtil.createDocument(true, source).getDocumentElement();
    	IEPModelProvider provider = new IEPModelProviderInsideIde ();
    	IEPModel model = provider.getWLMModel(wfFile);
       	model.sync();
    	assertNotNull(model);
    	IEPComponent root = ((AbstractDocumentModel <IEPComponent>) model).createRootComponent(rootEl);
        
        //assert properites on the root component
        assertEquals(1, model.getComponent().getProperties().size());

        //assert child components on the root component
        assertEquals(4, model.getComponent().getChildComponents().size());
        
        List<Component> childComponents = model.getComponent().getChildComponents();
        
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
        assertEquals("true", property1_1.getValue());
        
        Component schemaComponent  = childComponents.get(1);
        assertEquals("Schemas", schemaComponent.getName());
        assertEquals("Schemas", schemaComponent.getTitle());
        assertEquals("/IEP/Model/Plan|Schemas", schemaComponent.getType());
        /*
        List<Component> schemaComponentChildren = schemaComponent.getChildComponents();
        assertEquals(2, schemaComponentChildren.size());
        
        Component componentOperators  = schemaComponentChildren.get(0);
        assertEquals("Operators", componentOperators.getName());
        assertEquals("Operators", componentOperators.getTitle());
        assertEquals("/IEP/Model/Plan|Operators", componentOperators.getType());
        
        List<Component> operatorComponentChildren = componentOperators.getChildComponents();
        assertEquals(16, operatorComponentChildren.size());
        
        Component componentRelationAggregatorOperator  = operatorComponentChildren.get(0);
        assertEquals("o0", componentRelationAggregatorOperator.getName());
        assertEquals("o0", componentRelationAggregatorOperator.getTitle());
        assertEquals("/IEP/Operator/RelationAggregator", componentRelationAggregatorOperator.getType());
        */
        
    } 
    
}
