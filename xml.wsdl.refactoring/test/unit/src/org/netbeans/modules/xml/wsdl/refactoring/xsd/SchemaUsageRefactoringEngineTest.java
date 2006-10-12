/*
 * SchemaUsageRefactoringEngineTest.java
 * JUnit based test
 *
 * Created on June 1, 2006, 4:16 PM
 */

package org.netbeans.modules.xml.wsdl.refactoring.xsd;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.refactoring.RefactorRequest;
import org.netbeans.modules.xml.refactoring.RenameRequest;
import org.netbeans.modules.xml.refactoring.Usage;
import org.netbeans.modules.xml.refactoring.UsageGroup;
import org.netbeans.modules.xml.refactoring.UsageSet;
import org.netbeans.modules.xml.refactoring.spi.ChangeExecutor;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.refactoring.NamespaceLocation;
import org.netbeans.modules.xml.wsdl.refactoring.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.refactoring.Util;
import org.openide.util.Lookup;

/**
 *
 * @author nn136682
 */
public class SchemaUsageRefactoringEngineTest extends TestCase {
    SchemaUsageRefactoringEngine instance = new SchemaUsageRefactoringEngine();
    
    public SchemaUsageRefactoringEngineTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaUsageRefactoringEngineTest.class);
        
        return suite;
    }
    
    public void testFindUsages() throws Exception {
        SchemaModel schemaMod = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.OTA);
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        GlobalElement el = (GlobalElement) Util.findGlobalComponentByName(schemaMod.getSchema(), "TravelItinerary");
        
        List<UsageGroup> result = instance.findUsages(el, model.getDefinitions());
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getItems().size());
        Usage usage0 =  (Usage) result.get(0).getItems().get(0);
        assertEquals("itinerary", ((Part)usage0.getComponent()).getName());
    }
    
    private void executeChange(RefactorRequest request) throws Exception {
        Lookup.Result results = Lookup.getDefault().lookup(
                new Lookup.Template(ChangeExecutor.class));
        for (Object service : results.allInstances()){
            ChangeExecutor exec = (ChangeExecutor)service;
            if (exec.canChange(RenameRequest.class, request.getTarget())) {
                exec.doChange(request);
                return;
            }
        }
        
        fail("Failed to find a change executor for schema component");
    }
    
    public void testRefactorUsages() throws Exception {
        SchemaModel schemaMod = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.OTA);
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.TRAVEL);
        GlobalElement el = (GlobalElement) Util.findGlobalComponentByName(schemaMod.getSchema(), "TravelItinerary");
        List<UsageGroup> usageList = instance.findUsages(el, model.getDefinitions());
        UsageSet usages = new UsageSet(el);
        usages.addUsages(usageList);
        String newName = "myItirenary";
        RenameRequest request = new RenameRequest(el, newName);
        request.setUsages(usages);
        executeChange(request);
        assertTrue(request.confirmChangePerformed());
        instance.refactorUsages(request);
        
        Part part = model.findComponentByName("itinerary", Part.class);
        assertEquals(newName, part.getElement().getQName().getLocalPart());
        usageList = instance.findUsages((GlobalElement)request.getRenamedTarget(), model.getDefinitions());
        assertEquals(2, usageList.get(0).getItems().size());
    }
    
}
