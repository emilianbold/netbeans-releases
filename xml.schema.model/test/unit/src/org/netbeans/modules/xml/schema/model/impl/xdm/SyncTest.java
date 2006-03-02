/*
 * MergeTest.java
 * JUnit based test
 *
 * Created on October 28, 2005, 3:40 PM
 */

package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.diff.util.Debug;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.impl.GlobalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.GlobalSimpleTypeImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalComplexTypeImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.schema.model.impl.SequenceImpl;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author ajit
 */
public class SyncTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    public static final String TEST_XSD_OP     = "resources/PurchaseOrderSyncTest.xsd";
    
    public SyncTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    public void testUpdateAtSchemaRoot() throws Exception {
        model = Util.loadSchemaModel(TEST_XSD);
        Document doc = AbstractModel.class.cast(model).getBaseDocument();
        
        Util.setDocumentContentTo(doc, TEST_XSD_OP);
        model.sync();
        
        assertEquals(3,model.getSchema().getElements().size());
        GlobalElement ge = (GlobalElement)model.getSchema().getElements().toArray()[2];
        assertEquals("comment2",ge.getName());
        
        assertEquals(2,model.getSchema().getComplexTypes().size());
        Iterator<GlobalComplexType> ctIter = model.getSchema().getComplexTypes().iterator();
        while(ctIter.hasNext()) {
            GlobalComplexType poType = ctIter.next();
            Sequence poTypeSeq = (Sequence)poType.getDefinition();
            if(poTypeSeq.getChildren().size()==3) {
                ElementReference poComment = (ElementReference)poTypeSeq.getChildren().get(2);
                assertEquals(1,poComment.getMinOccurs().intValue());
            }
        }
        
        assertEquals(1,model.getSchema().getSimpleTypes().size());
    }
    
    public void testUpdateDirectParentOnly() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/SyncTestNonGlobal_before.xsd");
        Document doc = AbstractModel.class.cast(model).getBaseDocument();
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
        Node elementNode = gei.getPeer();
        LocalComplexTypeImpl lcti = (LocalComplexTypeImpl) gei.getInlineType();
        Node lctiNode = lcti.getPeer();
        SequenceImpl seq = (SequenceImpl) lcti.getDefinition();
        Node seqNode = seq.getPeer();
        LocalElementImpl leOffice = (LocalElementImpl) seq.getContent().get(1);
        
        Util.setDocumentContentTo(doc, "resources/SyncTestNonGlobal_after.xsd");
        model.sync();
        
        //make sure elements and nodes on the path before sequence is same 
        assertTrue("testUpdateElementOnly.schema", schema == model.getSchema());
        assertTrue("testUpdateElementOnly.schema.node", schemaNode == schema.getPeer());
        assertTrue("testUpdateElementOnly.element", gei == schema.getElements().iterator().next());
        assertTrue("testUpdateElementOnly.element.node", elementNode == gei.getPeer());
        assertTrue("testUpdateElementOnly.element.type", lcti == gei.getInlineType());
        assertTrue("testUpdateElementOnly.element.type.node", lctiNode == lcti.getPeer());
        assertTrue("parent component should be the same", seq == lcti.getDefinition());
        seq = (SequenceImpl) lcti.getDefinition();
        assertEquals("testUpdateElementOnly.element.type.seq.count", 3, seq.getContent().size());
        assertEquals("testUpdateElementOnly.element.type.seq.element2", "Office", ((LocalElement)seq.getContent().get(2)).getName());
        assertEquals("testUpdateElementOnly.element.type.seq.element1", "Branch", ((LocalElement)seq.getContent().get(1)).getName());
    }
    
    public void testRemoveChildOfGlobalElement() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/SyncTestGlobal_before.xsd");
        Document doc = AbstractModel.class.cast(model).getBaseDocument();
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalSimpleTypeImpl gst = (GlobalSimpleTypeImpl) schema.getSimpleTypes().iterator().next();
        Node elementNode = gst.getPeer();
        
        Util.setDocumentContentTo(doc, "resources/SyncTestGlobal_after.xsd");
        model.sync();
        
        //make sure elements and nodes on the path before sequence is same 
        assertTrue("testRemoveChildOfGlobalElement.schema", schema == model.getSchema());
        assertTrue("testRemoveChildOfGlobalElement.schema.node", schemaNode == schema.getPeer());
        assertTrue("parent component should be same as before sync", gst == schema.getSimpleTypes().iterator().next());
        assertTrue("testRemoveChildOfGlobalElement.gst.node", elementNode == gst.getPeer());
        assertNull("Annotation should have been remove", gst.getAnnotation());
        assertEquals("Attribute changed to new value", "allNNI", gst.getName());
    }
    
    public void testChangeAttributeOnly() throws Exception {
		Debug.enable(Debug.LEVEL.WARNING);
        SchemaModel model = Util.loadSchemaModel("resources/SyncTestGlobal_before.xsd");
        Document doc = AbstractModel.class.cast(model).getBaseDocument();
        Schema schema = model.getSchema();
        Element schemaNode = schema.getPeer();
        GlobalSimpleTypeImpl gst = (GlobalSimpleTypeImpl) schema.getSimpleTypes().iterator().next();
        Node elementNode = gst.getPeer();
        
        Util.setDocumentContentTo(doc, "resources/SyncTestGlobal_after2.xsd");
        model.sync();
        
        //make sure elements and nodes on the path before sequence is same 
        assertTrue("testRemoveChildOfGlobalElement.schema", schema == model.getSchema());
	assertEquals("parent component should be same as before sync", "allNNI-changed", schema.getSimpleTypes().iterator().next().getName());
    }
  
    public void testDocumentationText() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/loanApplication.xsd");
        Annotation ann = model.getSchema().getElements().iterator().next().getAnnotation();
        Iterator<Documentation> it = ann.getDocumentations().iterator();
        Documentation textDoc = it.next();
        Documentation htmlDoc = it.next();
        AppInfo appinfo = ann.getAppInfos().iterator().next();
        
        Util.setDocumentContentTo(model, "resources/loanApplication_annotationChanged.xsd");
        model.sync();
        
        assertEquals("text documentation sync", "A CHANGED loan application", textDoc.getContent());
        NodeList nl = htmlDoc.getDocumentationElement().getChildNodes();
        Element n = (Element) nl.item(1);
        n = (Element) n.getChildNodes().item(1);
        Text textNode = (Text) n.getChildNodes().item(0);
        assertEquals("html documentation sync", "Testing CHANGED documenation elemnent", textNode.getNodeValue());
        
        n = (Element) appinfo.getAppInfoElement().getChildNodes().item(1);
        textNode = (Text) n.getChildNodes().item(0);
        assertEquals("appinfo element sync", "checkForPrimesCHANGED", textNode.getNodeValue());
        n = (Element) appinfo.getAppInfoElement().getChildNodes().item(3);
        textNode = (Text) n.getChildNodes().item(0);
        assertEquals("appinfo element sync", "checkForPrimesADDED", textNode.getNodeValue());
    }
    
    public void testLocalElementReferenceTransform() throws Exception  {
        SchemaModel model = Util.loadSchemaModel("resources/PurchaseOrder.xsd");
        GlobalComplexType gct = model.getSchema().getComplexTypes().iterator().next();
        Sequence seq = (Sequence) gct.getDefinition();
        assertEquals("setup", "PurchaseOrderType", gct.getName());
        assertTrue("setup PurchaseOrderType.seqence[2]", seq.getContent().get(2) instanceof ElementReference);
        
        Util.setDocumentContentTo(model, "resources/PurchaseOrder_SyncElementRef.xsd");
        model.sync();
    
        LocalElement e = (LocalElement) seq.getContent().get(2);
        assertEquals("element ref transformed to local", "comment", e.getName());
        assertEquals("element ref transformed to local", "string", e.getType().get().getName());

        Util.setDocumentContentTo(model, "resources/PurchaseOrder.xsd");
        model.sync();
    
        ElementReference er = (ElementReference) seq.getContent().get(2);
        assertEquals("element ref transformed to local", "comment", er.getRef().get().getName());
    }
    
    public void testMultipleAdd() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/SyncTestNonGlobal_before.xsd");
        GlobalElement ge = model.getSchema().getElements().iterator().next();
        LocalComplexType lct = (LocalComplexType) ge.getInlineType();
        Sequence seq = (Sequence) lct.getDefinition();
        java.util.List<SequenceDefinition> sdl = seq.getContent();
        assertEquals("setup", 2, sdl.size());
        
        Util.setDocumentContentTo(model, "resources/SyncTestNonGlobal_multiple_adds.xsd");
        model.sync();
        
        assertEquals("multiple add to sequence", 5, seq.getContent().size());
    }
    
    private Document sd;
    private SchemaModel model;
    
}
