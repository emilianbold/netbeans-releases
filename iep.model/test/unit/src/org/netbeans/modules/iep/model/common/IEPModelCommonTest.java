/*
 * WLMModelCommonTest.java
 * JUnit based test
 *
 * Created on May 31, 2007, 3:10 PM
 */

package org.netbeans.modules.iep.model.common;

import java.io.InputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPComponentFactory;
import org.netbeans.modules.iep.model.IEPModel;
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

        /*
        assertEquals(1, model.getTasks().getTasks ().size());
        TTasks tasks = model.getTasks();
    	TTask task = tasks.getTasks().get(0);
    	assertEquals("http://jbi.com.sun/wfse/wsdl/WorkflowApp2/ApprovePurchase", tasks.getTargetNamespace());
    	TImport importEl = tasks.getImports().iterator().next();
    	assertNotNull (importEl.getImportedWSDLModel());
    	assertEquals(1, task.getTimeouts().size());
    	assertNull(task.getTimeouts().get(0).getDuration());
    	assertNotNull(task.getTimeouts().get(0).getDeadline());
    	assertEquals("2006-12-01T23:00:00", task.getTimeouts().get(0).getDeadline().getContent());
    	assertEquals(1, task.getEscalations().size());
    	TAssignment assign = task.getEscalations().get(0).getAssignment();
    	assertEquals(1,assign.getUsers().size());
    	assertEquals(0,assign.getGroups().size());
    	assertEquals(0,assign.getRoles().size());
    	assertEquals(1, assign.getUsers().size());
    	assertEquals("rwaldorf", assign.getUsers().get(0).getContent());
    	assertEquals(1, task.getNotifications().size());
    	assertTrue(task.getOperation().isResolved());
    	assertEquals(1, tasks.getImports().size());
    	*/
    } 
    
}
