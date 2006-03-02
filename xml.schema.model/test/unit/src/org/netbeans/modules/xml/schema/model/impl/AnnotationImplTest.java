/*
 * AnnotationImplTest.java
 * JUnit based test
 *
 * Created on October 31, 2005, 11:17 AM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.Element;

/**
 *
 * @author Vidhya Narayanan
 */
public class AnnotationImplTest extends TestCase {

    public static final String TEST_XSD = "resources/loanApplication.xsd";
    Schema schema = null;
    
    public AnnotationImplTest(String testName) {
	super(testName);
    }
    
    protected void setUp() throws Exception {
	SchemaModel model = Util.loadSchemaModel(TEST_XSD);
	schema = model.getSchema();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AnnotationImplTest.class);
        
        return suite;
    }

    /**
     * Test of getDocumentations method, of class org.netbeans.modules.xml.schema.model.impl.AnnotationImpl.
     */
    public void testDocumentationInternalDetail() throws IOException {
        String xpath = "/schema/element[1]/annotation";
        Annotation ann = FindSchemaComponentFromDOM.find(Annotation.class, schema, xpath);
        assertEquals("getDocumentations().size()", 2, ann.getDocumentations().size());
        Documentation doc = ann.getDocumentations().iterator().next();
        assertEquals("en", doc.getLanguage());
        String txt = "A loan application";
        assertEquals("documentation read text", txt, doc.getContent());
        
        String txt2 = "testDocumentation.write";
        schema.getSchemaModel().startTransaction();
        doc.setContent(txt2);
        schema.getSchemaModel().endTransaction();
        assertEquals("testDocumentation.write", "testDocumentation.write", doc.getContent());
        
        schema.getSchemaModel().startTransaction();
        Element parentPeer = ((AbstractComponent)ann).getPeer();
        Element docElement = doc.getDocumentationElement();
        assertTrue("documentation element cloned", docElement != ((AbstractComponent)doc).getPeer());
        assertFalse("cloned element not in tree", ((org.netbeans.modules.xml.xdm.nodes.Node)docElement).isInTree());
        Element docMyTag = schema.getSchemaModel().getDocument().createElement("mytag");
        docMyTag.setAttribute("attribute1", "value1");
        docElement.appendChild(docMyTag);
        doc.setDocumentationElement(docElement);
        schema.getSchemaModel().endTransaction();
        assertTrue("parent component not changed", ann == doc.getParent());
        assertTrue("parent peer node updated", parentPeer != ((AbstractComponent)ann).getPeer());
        assertTrue("peer == documentation element used", docElement == ((AbstractComponent)doc).getPeer());
        assertTrue("documentation element child", docElement.getLastChild() == docMyTag);
        Text txtNode = (Text)docElement.getFirstChild();
        assertEquals("documentation element text child", txt2, txtNode.getText());
        assertTrue("updated element now in tree", ((org.netbeans.modules.xml.xdm.nodes.Node)docElement).isInTree());
    }
    
    public void testDocumentationReadElement() throws IOException {
        String xpath = "/schema/element[1]/annotation/documentation[2]";
        Documentation doc = FindSchemaComponentFromDOM.find(Documentation.class, schema, xpath);
        assertEquals("documentation content when no-text", "", doc.getContent().trim());
        Element doce = doc.getDocumentationElement();
        assertEquals("documentation element count", 3, doce.getChildNodes().getLength());
        Element html = (Element) doce.getChildNodes().item(1);
        assertEquals("documentation element tag", "html", html.getNodeName());
        Element a = (Element) html.getChildNodes().item(1);
        String aText = "Testing documenation elemnent";
        assertEquals("doc element children", aText, ((Text)a.getFirstChild()).getText());
    }
    
    public void testAppInfo() throws IOException {
        String xpath = "/schema/element[1]/annotation";
        Annotation ann = FindSchemaComponentFromDOM.find(Annotation.class, schema, xpath);
        AppInfo info = ann.getAppInfos().iterator().next();
        assertEquals("appinfo source", "http://www.aloan.com/loanApp", info.getURI());
        Element infoE = info.getAppInfoElement();
        Element handlingE = (Element)infoE.getChildNodes().item(1);
        Text textnode = (Text)handlingE.getFirstChild();
        assertEquals("appinfo element child", "checkForPrimes", textnode.getText());

        ann.getSchemaModel().startTransaction();
        ann.removeAppInfo(info);
        AppInfo info2 = ann.getSchemaModel().getFactory().createAppInfo();
        textnode.setText("checkIfUpdated");
        
    }
    
}
 