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
import org.netbeans.modules.xml.schema.model.impl.LocalComplexTypeImpl;
import org.netbeans.modules.xml.schema.model.impl.LocalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.schema.model.impl.SequenceImpl;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.w3c.dom.Node;

/**
 *
 * @author Ayub Khan
 */
public class CutPasteTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    public static final String TEST_XSD_OP     = "resources/PurchaseOrderSyncTest.xsd";
    
    public CutPasteTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    private String readFile(String filename) throws IOException {
        URL url = getClass().getResource(filename);
        BufferedReader br =  new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8"));
        StringBuffer sbuf = new StringBuffer();
        try {
            int c = 0;
            while((c = br.read()) != -1) {
                sbuf.append((char)c);
            }
        } finally {
            br.close();
        }
        return sbuf.toString();
    }
	
    /**
     * Test of cut/paste operation
     */
    public void testCutPasteLocalElement() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/CutPasteTest_before.xsd");
        Document doc = AbstractModel.class.cast(model).getBaseDocument();
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
        LocalComplexTypeImpl lcti = (LocalComplexTypeImpl) gei.getInlineType();
        SequenceImpl seq = (SequenceImpl) lcti.getDefinition();
        Node seqNode = seq.getPeer();
		LocalElementImpl leti = (LocalElementImpl) seq.getContent().get(1);
		
        assertEquals("testCutPasteByDocSync.schema.node", 2, seq.getChildren().size());
		assertEquals("testCutPasteByDocSync.schema.node", 5, seqNode.getChildNodes().getLength());
		
		//Debug.log(Debug.LEVEL.ERROR, "Initial Document: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, schemaNode.getOwnerDocument());		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, seqNode);		
		
		model.startTransaction();
		seq.removeContent(leti);
		model.endTransaction();	
		
        SchemaImpl changedSchema = (SchemaImpl) model.getSchema();
        Node changedSchemaNode = changedSchema.getPeer();
        GlobalElementImpl changedGei = (GlobalElementImpl) changedSchema.getElements().iterator().next();
        LocalComplexTypeImpl changedLcti = (LocalComplexTypeImpl) changedGei.getInlineType();
        SequenceImpl changedSeq = (SequenceImpl) changedLcti.getDefinition();
        Node changedSeqNode = changedSeq.getPeer();
				
		//Debug.log(Debug.LEVEL.ERROR, "After remove: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, changedSchemaNode.getOwnerDocument());		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSeqNode);				
		
        //make sure elements and nodes on the path before sequence is same 
        assertTrue("testCutPasteByRemove.schema", schema == changedSchema);
        assertTrue("testCutPasteByRemove.schema.node", schemaNode == changedSchemaNode);
		assertTrue("testCutPasteByRemove.schema.node", seqNode != changedSeqNode);		
		
		assertEquals("testCutPasteByDocSync.schema.node", 1, changedSeq.getChildren().size());		
		assertEquals("testCutPasteByDocSync.schema.node", 4, changedSeqNode.getChildNodes().getLength());
		
		model.startTransaction();
		LocalElementImpl clonedLeti = (LocalElementImpl) leti.copy(seq);
		assertTrue("testCutPasteByDocSync.localElement", clonedLeti!=null);
		seq.addContent(clonedLeti, 1);
		model.endTransaction();	
		
        SchemaImpl changedSchema1 = (SchemaImpl) model.getSchema();
        Node changedSchemaNode1 = changedSchema1.getPeer();
        GlobalElementImpl changedGei1 = (GlobalElementImpl) changedSchema1.getElements().iterator().next();
        LocalComplexTypeImpl changedLcti1 = (LocalComplexTypeImpl) changedGei1.getInlineType();
        SequenceImpl changedSeq1 = (SequenceImpl) changedLcti1.getDefinition();
        Node changedSeqNode1 = changedSeq1.getPeer();		
		
		//Debug.log(Debug.LEVEL.ERROR, "After add: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, changedSchemaNode1.getOwnerDocument());		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSeqNode1);			
		
        assertEquals("testCutPasteByDocSync.schema.node", 2, changedSeq1.getChildren().size());
		assertEquals("testCutPasteByDocSync.schema.node", 6, changedSeqNode1.getChildNodes().getLength());		
		assertTrue("testCutPasteByDocSync.localElement.isSame", leti.getName().equals(clonedLeti.getName()));
		assertTrue("testCutPasteByDocSync.localElement.isIdNotSame", 
			((NodeImpl)leti.getPeer()).getId()!=((NodeImpl)clonedLeti.getPeer()).getId());
		Iterator it=changedSeq1.getChildren().iterator();
		it.next();
		LocalElementImpl le2=(LocalElementImpl) it.next();
		assertTrue("testCutPasteByDocSync.localElement.isSame", leti.getName().equals(le2.getName()));
		assertTrue("testCutPasteByDocSync.localElement.isIdNotSame", 
			((NodeImpl)leti.getPeer()).getId()!=((NodeImpl)le2.getPeer()).getId());
		
    }

    private Document sd;
    private SchemaModel model;
    
}
