/*
 * WLMModelCommonTest.java
 * JUnit based test
 *
 * Created on May 31, 2007, 3:10 PM
 */

package org.netbeans.modules.wlm.model.common;

import java.io.InputStream;
import java.net.URI;

import junit.framework.TestCase;

import org.netbeans.modules.wlm.model.api.TAssignment;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMComponent;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMModelProvider;
import org.netbeans.modules.wlm.model.impl.TaskImpl;
import org.netbeans.modules.wlm.model.spi.OperationReference;
import org.netbeans.modules.wlm.model.utl.XmlUtil;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author mei
 */
public class WLMModelCommonTest extends TestCase {
    
    public WLMModelCommonTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
 
    }
    
    public void testImport () throws Exception {
       	URI wfFile = getClass().getResource("data/ApprovePurchase.wf").toURI();
    	InputStream is = getClass().getResourceAsStream("data/EmptyWf.wf");
    	WLMModelProvider provider = new WLMModelProviderInsideIde ();
    	WLMModel model = provider.getWLMModel(wfFile);
        //
    	TTask task = model.getTask();
    	model.startTransaction();
        try {
            model.setTask(task);
            assertNull(task.getOperationAsString());
            WSDLModel newModel = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.PURCHASE_ORDER);
            Operation opt = newModel.getDefinitions().getPortTypes().iterator().next().getOperations().iterator().next();
            task.setOperation(new OperationReference(opt, TaskImpl.class.cast (task)));
            assertEquals("ns0:ApprovePurchase", task.getOperationAsString());
        } finally {
          	model.endTransaction();
        }
    	assertEquals(1,  task.getImports().size());

    }
    
    public void testCreateModel()  throws Exception{
    	URI wfFile = getClass().getResource("data/ApprovePurchase.wf").toURI();
    	InputStream is = getClass().getResourceAsStream("data/ApprovePurchase.wf");
    	InputSource source = new InputSource (is);
    	Element rootEl = XmlUtil.createDocument(true, source).getDocumentElement();
    	WLMModelProvider provider = new WLMModelProviderInsideIde ();
    	WLMModel model = provider.getWLMModel(wfFile);
       	model.sync();
    	assertNotNull(model);
    	WLMComponent root = ((AbstractDocumentModel <WLMComponent>) model).createRootComponent(rootEl);
    	TTask task = model.getTask();
        assertNotNull(task);
    	assertEquals("http://jbi.com.sun/wfse/wsdl/WorkflowApp2/ApprovePurchase", task.getTargetNamespace());
    	TImport importEl = task.getImports().iterator().next();
    	assertNotNull (importEl.getImportedWSDLModel());
    	assertEquals(1, task.getTimeouts().size());
    	assertNull(task.getTimeouts().get(0).getDuration());
    	assertNotNull(task.getTimeouts().get(0).getDeadline());
    	assertEquals("2006-12-01T23:00:00", task.getTimeouts().get(0).getDeadline().getContent());
    	assertEquals(1, task.getEscalations().size());
    	TAssignment assign = task.getEscalations().get(0).getAssignment();
    	assertEquals(1,assign.getUsers().size());
    	assertEquals(0,assign.getGroups().size());
    	assertEquals(1, assign.getUsers().size());
    	assertEquals("rwaldorf", assign.getUsers().get(0).getContent());
    	assertEquals(1, task.getNotifications().size());
    	assertTrue(task.getOperation().isResolved());
    	assertEquals(1, task.getImports().size());
    	
    } 
    
}
