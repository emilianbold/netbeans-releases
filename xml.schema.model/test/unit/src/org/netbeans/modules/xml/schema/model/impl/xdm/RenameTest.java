package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.diff.util.Debug;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.impl.GlobalElementImpl;
import org.netbeans.modules.xml.schema.model.impl.SchemaImpl;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.w3c.dom.Node;

/**
 *
 * @author Ayub Khan
 */
public class RenameTest extends TestCase {
    
    public RenameTest(String testName) {
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
     * Test of rename operation
     */
    public void testRenameGlobalElement() throws Exception {
		Debug.enable(Debug.LEVEL.ERROR);
        SchemaModel model = Util.loadSchemaModel("resources/CutPasteTest_before.xsd");
        Document doc = AbstractModel.class.cast(model).getBaseDocument();
        SchemaImpl schema = (SchemaImpl) model.getSchema();
        Node schemaNode = schema.getPeer();
        GlobalElementImpl gei = (GlobalElementImpl) schema.getElements().iterator().next();
		
        assertEquals("testRenameGlobalElement.schema", 1, schema.getChildren().size());
		assertEquals("testRenameGlobalElement.schema.node", 3, schemaNode.getChildNodes().getLength());
		
		//Debug.log(Debug.LEVEL.ERROR, "Initial Document: ");
		//Debug.logDocument(Debug.LEVEL.ERROR, schemaNode.getOwnerDocument());		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, schemaNode);		
		
		model.startTransaction();
		gei.setName("NewName");
		model.endTransaction();	
		
		model.sync();
		
        SchemaImpl changedSchema = (SchemaImpl) model.getSchema();
        Node changedSchemaNode = changedSchema.getPeer();
        GlobalElementImpl changedGei = (GlobalElementImpl) changedSchema.getElements().iterator().next();
		Node changedGeiNode = changedGei.getPeer();
				
		assertEquals("testRenameGlobalElement.firstRename.Gei", "NewName", changedGei.getName());
		assertEquals("testRenameGlobalElement.firstRename.GeiNode", "NewName", 
				changedGeiNode.getAttributes().item(0).getNodeValue());		
		
		Debug.log(Debug.LEVEL.ERROR, "After rename first time: ");
		Debug.logDocument(Debug.LEVEL.ERROR, changedSchemaNode.getOwnerDocument());	
		//TODO - fix, prints children from old document
		//child nodes: Text@2, Element@3(xsd:element, OrgChart), Text@28		
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSchemaNode);	
		
		model.startTransaction();
		changedGei.setName("NewName2");
		model.endTransaction();	
		
		model.sync();		
		
        SchemaImpl changedSchema2 = (SchemaImpl) model.getSchema();
        Node changedSchemaNode2 = changedSchema2.getPeer();
        GlobalElementImpl changedGei2 = (GlobalElementImpl) changedSchema2.getElements().iterator().next();
		Node changedGeiNode2 = changedGei2.getPeer();
		
		assertEquals("testRenameGlobalElement.secondRename.Gei2", "NewName2", changedGei2.getName());
		assertEquals("testRenameGlobalElement.secondRename.Geinode2", "NewName2", 
				changedGeiNode2.getAttributes().item(0).getNodeValue());
		
		Debug.log(Debug.LEVEL.ERROR, "After rename second time: ");
		Debug.logDocument(Debug.LEVEL.ERROR, changedSchemaNode2.getOwnerDocument());	
		//TODO - fix, prints children from old document
		//child nodes: Text@2, Element@3(xsd:element, OrgChart), Text@28
		//XDMModel.printChildren(Debug.LEVEL.ERROR, changedSchemaNode2);		
      }
	
    private Document sd;
    private SchemaModel model;
    
}
