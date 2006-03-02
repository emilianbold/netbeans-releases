/*
 * CommonSchemaComponentImplTest.java
 * JUnit based test
 *
 * Created on October 14, 2005, 7:19 AM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import junit.framework.*;
import java.util.Collection;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponentFactory;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xam.AbstractComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * 
 * @author Vidhya Narayanan
 * @author Nam Nguyen
 */
public class SchemaComponentImplTest extends TestCase {
    
    public SchemaComponentImplTest(String testName) {
        super(testName);
    }
    
    public static final String TEST_XSD = "resources/PurchaseOrder.xsd";
    Schema schema = null;
    SchemaModelImpl model = null;
    
    protected void setUp() throws Exception {
        SchemaModel model2 = Util.loadSchemaModel(TEST_XSD);
        schema = model2.getSchema();
    }

    public void testFromSameModel() throws Exception {
        System.out.println("fromSameModel");
        
        SchemaModel cur_model = ((SchemaComponentImpl)schema).getSchemaModel();
        SchemaModel new_model = Util.loadSchemaModel(TEST_XSD);
        boolean modelcheck = ((SchemaComponentImpl)schema).fromSameModel(new_model.getSchema());
        assertEquals("model is different", false, modelcheck);
        Collection<SchemaComponent> comps = schema.getChildren();
        modelcheck = ((SchemaComponentImpl)schema).fromSameModel(comps.iterator().next());
        assertEquals("model is the same", true, modelcheck);
    }

    public void testSetAnnotation() throws IOException  {
        SchemaModel model = ((SchemaComponentImpl)schema).getSchemaModel();
        System.out.println("addAnnotation");
        Collection<SchemaComponent> comps = schema.getChildren();
        assertEquals("# children for schema", 5, comps.size());
        Annotation a = schema.getSchemaModel().getFactory().createAnnotation();
        model.startTransaction();
        ((SchemaComponentImpl)schema).setAnnotation(a);
        model.endTransaction();
        comps = schema.getChildren();
        assertEquals("# children for schema", 6, comps.size());
        java.util.Iterator i = comps.iterator();
        Annotation a1 = (Annotation)i.next();
        int index = ((java.util.List)comps).indexOf(a1);
        assertEquals("added annotation as first child", 0, index);
        assertEquals("added annotation is same as newly created", a, a1);
        
        //Check if annotation has also been added in the DOM
        //SchemaModel model = ((SchemaComponentImpl)schema).getSchemaModel();
        Document doc = ((SchemaModelImpl)model).getDocument();
        Node root = doc.getFirstChild();
        NodeList nl = root.getChildNodes();
        Node ann = nl.item(0);
        for (int j=0; j<nl.getLength(); j++) {
            if (nl.item(j) instanceof Element) {
                ann = nl.item(j);
                break;
            }
        }
        assertEquals("#1 child for schema DOM is annotation", "annotation", ann.getLocalName());
    }
    
    public void testGetAnnotations() throws IOException {
        System.out.println("getAnnotations");
        SchemaModel model = ((SchemaComponentImpl)schema).getSchemaModel();
        Annotation a = model.getFactory().createAnnotation();
        model.startTransaction();
        ((SchemaComponentImpl)schema).setAnnotation(a);
        model.endTransaction();
        Annotation ann = schema.getAnnotation();
        assertNotNull("only one annotation should be present", ann);
    }
    
    public void testSetGlobalReference() throws Exception {
        SchemaModel mod = Util.loadSchemaModel("resources/ipo.xsd");
        Schema schema = mod.getSchema();
        SchemaComponentFactory fact = mod.getFactory();
        
        mod.startTransaction();
        GlobalAttributeGroup gap = fact.createGlobalAttributeGroup();
        schema.addAttributeGroup(gap);
        gap.setName("myAttrGroup2");
        LocalAttribute ga = fact.createLocalAttribute();
        gap.addLocalAttribute(ga);
        ga.setName("ga");
        GlobalSimpleType gst = FindSchemaComponentFromDOM.find(
                GlobalSimpleType.class, schema, "/schema/simpleType[@name='Sku']");
        ga.setType(ga.createReferenceTo(gst, GlobalSimpleType.class));

        mod.endTransaction();
        
        String v = ((AbstractComponent)ga).getPeer().getAttribute("type");
        assertEquals("ref should have prefix", "ipo:Sku", v);
        
        mod.startTransaction();
        /*
        <complexType name="myCT">
            <sequence>
                <simpleType name="productName" type="xsd:string"/>
            <attributeGroup ref="ipo:myAttrGroup2"/>
        </complexType>
         */
        GlobalComplexType gct = fact.createGlobalComplexType();
        schema.addComplexType(gct);
        gct.setName("myCT");
        Sequence seq = Util.createSequence(mod, gct);
        LocalElement le = Util.createLocalElement(mod, seq, "productName", 0);
        le.setType(le.createReferenceTo(Util.getPrimitiveType("string"), GlobalSimpleType.class));
        
        AttributeGroupReference agr = fact.createAttributeGroupReference();
        gct.addAttributeGroupReference(agr);
        agr.setGroup(agr.createReferenceTo(gap, GlobalAttributeGroup.class));

        mod.endTransaction();
        
        v = ((AbstractComponent)agr).getPeer().getAttribute("ref");
        assertEquals("ref should have prefix", "ipo:myAttrGroup2", v);
    }
    
    public void testSetAndGetID() throws Exception {
        assertNull("id attribute is optional", schema.getId());
        schema.getSchemaModel().startTransaction();
        String v = "testSEtAndGetID";
        schema.setId(v);
        schema.getSchemaModel().endTransaction();
        assertEquals("testSetAndGetID.setID", v, schema.getId());
    }
}
