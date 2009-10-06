/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.schema.model;
import java.beans.PropertyChangeEvent;
import junit.framework.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.Model.State;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
/*
 * SchemaModelTest.java
 * JUnit based test
 *
 * Created on October 3, 2005, 3:51 PM
 */

/**
 *
 * @author nn136682
 */
public class SchemaModelTest extends TestCase {
    
    private static String TEST_XSD = "resources/testInclude.xsd";
    private static String TEST_BAD_XSD = "resources/testBad.xsd";
    private static String TEST_BAD_INCLUDE_XSD = "resources/testBadInclude.xsd";
    private static String TEST_TYPES_XSD = "resources/testTypes.xsd";
    private static String TEST_FAKE_XSD = "resources/fakeSchema.xsd";
    
    public SchemaModelTest(String testName) {
        super(testName);
    }
    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new SchemaModelTest("testGetSchema"));
        suite.addTest(new SchemaModelTest("testGetState"));
        suite.addTest(new SchemaModelTest("testTypes"));
        suite.addTest(new SchemaModelTest("testVersionChangedListener"));
        suite.addTest(new SchemaModelTest("testFakeSchema"));
        suite.addTest(new SchemaModelTest("testBadInclude"));
        suite.addTest(new SchemaModelTest("testCircularInclude"));        
        //resolve1 must be the first in the following test cases
        suite.addTest(new SchemaModelTest("testResolve1"));
        suite.addTest(new SchemaModelTest("testResolve2"));
        suite.addTest(new SchemaModelTest("testResolve3"));
        suite.addTest(new SchemaModelTest("testResolve4"));
        suite.addTest(new SchemaModelTest("testResolve5"));
        suite.addTest(new SchemaModelTest("testResolve6"));
        suite.addTest(new SchemaModelTest("testFlushDumpThenReload"));                
        return suite;
    }

    /**
     * Test of flush method, of class org.netbeans.modules.xml.schema.model.api.SchemaModel.
     */
    public void testFlushDumpThenReload() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        assertNotNull(sm);
        Document doc = AbstractDocumentModel.class.cast(sm).getBaseDocument();
        assertNotNull(doc);
        Schema schema = sm.getSchema();
        assertNotNull(schema);
        String current = schema.getVersion();
        sm.startTransaction();
        schema.setVersion("1.3");
        sm.endTransaction();
	assertEquals("1.3", sm.getSchema().getVersion());
	Document d = (Document) sm.getModelSource().getLookup().lookup(Document.class);
	assertSame(doc,d);
	
	//System.out.println(d.getText(0, d.getLength()));
        SchemaModel sm2 = Util.dumpAndReloadModel(sm);
	d = (Document) sm2.getModelSource().getLookup().lookup(Document.class);
        //System.out.println(d.getText(0, d.getLength()));
       
        assertFalse("testSync", sm2.getSchema().getVersion().equals(current));
        assertEquals("1.3", sm2.getSchema().getVersion());
    }

    /**
     * Test of getSchema method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testGetSchema() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        assertNotNull(sm);
        assert(!sm.isEmbedded());
    	String uri = sm.getSchema().getTargetNamespace();
        String expectUri = "http://www.example.com/testInclude";
        assertEquals(expectUri, uri);
    }

    /**
     * Test of getState method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testGetState() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        State s1 = sm.getState();
        State expResult1 = State.VALID;
        assertEquals(expResult1, s1);

        SchemaModel sm2 = Util.loadSchemaModel(TEST_BAD_XSD);
        assertEquals("Expect not well-formed source", State.NOT_WELL_FORMED, sm2.getState());
    }
	
    /**
     * Test of getState method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testBadInclude() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        State s1 = sm.getState();
        State expResult1 = State.VALID;
        assertEquals(expResult1, s1);
        SchemaModel sm2 = Util.loadSchemaModel(TEST_BAD_INCLUDE_XSD);
        assertEquals("Expect not well-formed source", State.NOT_WELL_FORMED, sm2.getState());
    }

    private class Listener implements PropertyChangeListener {
        private String expectedEvent;
        private boolean gotIt = false;
        
        public Listener(String expected) {
            expectedEvent = expected;
            
        }
        public void propertyChange(PropertyChangeEvent evt) {
            assertNotNull(evt);
            System.out.println("Got " + evt.getPropertyName());
            assertEquals(expectedEvent, evt.getPropertyName());
            gotIt = true;
        }
        public boolean gotIt() { return gotIt; }
        public void resetGotIt() { gotIt = false; }
    }
    
    /**
     * Test of addPropertyChangeListener method, of class org.netbeans.modules.xmlschema.api.SchemaModel.
     */
    public void testVersionChangedListener() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_XSD);
        Listener pcl = new Listener(Schema.VERSION_PROPERTY);
        sm.addPropertyChangeListener(pcl);
        sm.startTransaction();
        sm.getSchema().setVersion("1.0");
        sm.endTransaction();
        assertTrue("test VersionChanged event", pcl.gotIt());
        
        pcl.resetGotIt();
        sm.removePropertyChangeListener(pcl);
        sm.startTransaction();
        sm.getSchema().setVersion("1.1");
        sm.endTransaction();
        assertFalse("test remove listener, no more events", pcl.gotIt());
    }
    
    public void testCircularInclude() throws Exception {
            SchemaModel sm = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.TEST_INCLUDE);
            if (! NamespaceLocation.SOMEFILE.getResourceFile().exists()) {
                NamespaceLocation.SOMEFILE.refreshResourceFile();
            }
            Collection<Schema> schemas = sm.findSchemas("http://www.example.com/testInclude");
            ArrayList<Schema> list = new ArrayList<Schema>(schemas);
            assertEquals("circular include is ok", 2, schemas.size());
            assertTrue("2 distinct schema", list.get(1) != list.get(0));
            assertEquals("http://www.example.com/testInclude", list.get(0).getTargetNamespace());
    }
    
    public void testTypes() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_TYPES_XSD);
        GlobalComplexType gct = (GlobalComplexType)sm.getSchema().getChildren().get(0);
        ComplexTypeDefinition ctd = gct.getDefinition();
        ComplexExtension ce = (ComplexExtension)ctd.getChildren().get(0);
        NamedComponentReference<GlobalType> ncr = ce.getBase();
        GlobalType type = ncr.get();
        assert(type != null && type instanceof GlobalSimpleType);
        GlobalSimpleType gst = (GlobalSimpleType)type;
        assert(gst.getName() != null && gst.getName().equals("anyType"));
    }

    public void testFakeSchema() throws Exception {
        SchemaModel sm = Util.loadSchemaModel(TEST_FAKE_XSD);
        assert(sm.getState() == State.NOT_WELL_FORMED);
    }
    
    /**
     * C & D do not know anything about each other.
     * In this use-case, we'll explore components in C and it'll NOT resolve types
     * from D.
     */
    public void testResolve1() throws Exception {
        SchemaModel sm = Util.loadSchemaModel("resources/C.xsd");
        assert(sm.getState() == State.VALID);
        GlobalComplexType gct = (GlobalComplexType)sm.getSchema().getChildren().get(2);
        assert("C2".equals(gct.getName()));
        LocalElement e1 = (LocalElement)gct.getChildren().get(0).getChildren().get(0);
        assert(e1 != null && e1.getName().equals("C21"));
        NamedComponentReference ncr = e1.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/B:D1".equals(name));
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assert(gct == null);
    }
    
    /**
     * A imports B, B includes C. An element in A uses a complex type
     * defined in C. See http://www.netbeans.org/issues/show_bug.cgi?id=134861.
     */
    public void testResolve2() throws Exception {
        SchemaModel sm = Util.loadSchemaModel("resources/A.xsd");        
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(2);
        assert("A1".equals(ge.getName()));
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/B:C1".equals(name));
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assert(gct != null);
    }
    
    /**
     * A imports B, B includes C. An element in A uses a complex type
     * defined in C. See http://www.netbeans.org/issues/show_bug.cgi?id=134861.
     */
    public void testResolve3() throws Exception {
        SchemaModel sm = Util.loadSchemaModel("resources/A.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(3);
        assert("A2".equals(ge.getName()));
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/B:D1".equals(name));
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assert(gct != null);
    }
    
    /**
     * B includes C & D. B uses types defined in C. C uses types defined in D.
     * C & D do not know anything about each other.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=122836.
     * In this use-case, if you expand from B, it'll resolve types from D in C.
     */
    public void testResolve4() throws Exception {
        SchemaModel sm = Util.loadSchemaModel("resources/B.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(3);
        assert("B2".equals(ge.getName()));
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/B:C2".equals(name));
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assert(gct != null && gct.getName().equals("C2"));
        LocalElement e1 = (LocalElement)gct.getChildren().get(0).getChildren().get(0);
        assert(e1 != null && e1.getName().equals("C21"));
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/B:D1".equals(name));
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assert(gct != null && gct.getName().equals("D1"));        
    }

    /**
     * Another one test related to the issue #122836
     * See http://www.netbeans.org/issues/show_bug.cgi?id=122836.
     * It tests more deep inclusions. The first solution has supported only
     * simple case, like is testein by testResolve4()
     *
     * B includes C & D; C includes E; D includes F.
     * B uses types defined in E. E uses types defined in F.
     * E & F do not know anything about each other.
     * In this use-case, if you expand from B, it'll resolve types from 
     * all included schema: C, D, E, F.
     */
    public void testResolve5() throws Exception {
        SchemaModel sm = Util.loadSchemaModel("resources/B.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(4);
        assert("B3".equals(ge.getName()));
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/B:E2".equals(name));
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assert(gct != null && gct.getName().equals("E2"));
        LocalElement e1 = (LocalElement)gct.getChildren().get(0).getChildren().get(0);
        assert(e1 != null && e1.getName().equals("E21"));
        ncr = e1.getType();
        name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/B:F1".equals(name));
        //this is when it'll try to resolve
        gct = (GlobalComplexType)ncr.get();
        assert(gct != null && gct.getName().equals("F1"));
    }

    /**
     * A imports B, B imports G. An element in A uses a complex type defined in G.
     * It has to be not accessible.
     */
    public void testResolve6() throws Exception {
        SchemaModel sm = Util.loadSchemaModel("resources/A.xsd");
        assert(sm.getState() == State.VALID);
        GlobalElement ge = (GlobalElement)sm.getSchema().getChildren().get(4);
        assert("G1".equals(ge.getName()));
        NamedComponentReference ncr = ge.getType();
        String name = ncr.getQName().getNamespaceURI() + ":" + ncr.getQName().getLocalPart();
        assert("http://xml.netbeans.org/schema/G:G1".equals(name));
        //this is when it'll try to resolve
        GlobalComplexType gct = (GlobalComplexType)ncr.get();
        assert(gct != null);
        //
        Import gImport = (Import)sm.getSchema().getChildren().get(1);
        assert "G.xsd".equals(gImport.getSchemaLocation());
        //
        sm.startTransaction();
        try {
            sm.getSchema().removeExternalReference(gImport);
        } finally {
            sm.endTransaction();
        }
        //
        // Try resolve G1 type again
        gct = (GlobalComplexType)ncr.get();
    }

}
