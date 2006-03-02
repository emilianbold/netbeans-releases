package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.util.ArrayList;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.*;

/**
 *
 * @author Administrator
 */
public class SyncUpdateVisitorTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    private Schema schema;
    private SchemaModel model;

    public SyncUpdateVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }
    
    protected void setUp1() throws Exception {
        model = (SchemaModel)Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SyncUpdateVisitorTest.class);
        
        return suite;
    }

    /**
     * Test of visit method, of class org.netbeans.modules.xml.schema.model.impl.xdm.SyncUpdateVisitor.
     * Tries to perform delete operation.
     */
    public void testVisitDelete() throws Exception {
        setUp1();
        System.out.println("visitDelete");
        SyncUpdateVisitor instance = new SyncUpdateVisitor();
        
        int size = schema.getComplexTypes().size();
        GlobalComplexType gct = schema.getComplexTypes().iterator().next();
        model.startTransaction();
        instance.update(schema, gct ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getComplexTypes().size());

        size = schema.getSimpleTypes().size();
        GlobalSimpleType gst = schema.getSimpleTypes().iterator().next();
        model.startTransaction();
        instance.update(schema, gst ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getSimpleTypes().size());

        size = schema.getElements().size();
        GlobalElement ge = schema.getElements().iterator().next();
        model.startTransaction();
        instance.update(schema, ge ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getElements().size());
    }
    
    /**
     * Test of visit method, of class org.netbeans.modules.xml.schema.model.impl.xdm.SyncUpdateVisitor.
     * Tries to perform add operation.
     */
    public void testVisitAdd() throws Exception{
        setUp1();
        System.out.println("visitAdd");
        SyncUpdateVisitor instance = new SyncUpdateVisitor();
        
        int size = schema.getComplexTypes().size();
        schema.getSchemaModel().getFactory().createGlobalComplexType();
        GlobalComplexType gct = schema.getSchemaModel().getFactory().createGlobalComplexType();
        model.startTransaction();
        instance.update(schema, gct ,SyncUpdateVisitor.Operation.ADD);
        model.endTransaction();
        assertEquals(size+1,schema.getComplexTypes().size());

        size = schema.getSchemaReferences().size();
        SchemaComponent sc = schema.getSchemaModel().getFactory().createImport();
        model.startTransaction();
        instance.update(schema, sc ,SyncUpdateVisitor.Operation.ADD);
        model.endTransaction();
        assertEquals(size+1,schema.getSchemaReferences().size());
    }
    
    public void testRemoveAllPurchaseOrder() throws Exception {
        setUp1();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }
    
    //TODO: debug mysterious StackOverflowError on UnmodifiableCollection.iterator
    /*public void testRemoveAllOTA() throws Exception {
        model = TestResolver.getDefault().getSchemaModel(NamespaceLocation.OTA);
        schema = model.getSchema();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }*/
    
    public void testRemoveAllLoanApp() throws Exception {
        model = TestResolver.getDefault().getSchemaModel(NamespaceLocation.LOANAPP);
        schema = model.getSchema();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }
    
    public static void recursiveRemoveChildren(SchemaComponent target) {
        SchemaModel model = target.getSchemaModel();
        ArrayList<SchemaComponent> children = new ArrayList<SchemaComponent>(target.getChildren());
        for (SchemaComponent child : children) {
            recursiveRemoveChildren(child);
        }
        if (target.getParent() != null) {
            model.removeChildComponent(target);
        }
    }
}
