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
package org.netbeans.modules.compapp.projects.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.CatalogWSDLSerializer;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.Entry;
import org.netbeans.modules.compapp.projects.common.CatalogWSDL.EntryType;
import org.openide.filesystems.FileObject;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author chikkala
 */
public class CatalogWSDLTest extends NbTestCase {

    public static final String TEST_WSDL_NS = "http://test.com/wsdl/test";
    public static final String TEST_WSDL_LOC = "xyz/wsdl/test";
    public static final String TEST_XSD_NS = "http://test.com/xsd/test";
    public static final String TEST_XSD_LOC = "123/xsd/test";
    FileObject rootDir;

    public CatalogWSDLTest(String testName) {
        super(testName);
    }

    /** Creates suite from particular test cases. You can define order of testcases here. */
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new CatalogWSDLTest("testGetEntries"));
        suite.addTest(new CatalogWSDLTest("testAddAllEntries"));
        suite.addTest(new CatalogWSDLTest("testClear"));
        suite.addTest(new CatalogWSDLTest("testCreateEmptyCatalogWSDLFile"));
        suite.addTest(new CatalogWSDLTest("testLoadCatalogWSDL"));
        suite.addTest(new CatalogWSDLTest("testSaveCatalogWSDL"));
        suite.addTest(new CatalogWSDLTest("testModifyCatalogWSDL"));
        return suite;
    }

    /* Method allowing test execution directly from the IDE. */
    public static void main(java.lang.String[] args) {
        // run whole suite
        junit.textui.TestRunner.run(suite());
    // run only selected test case
    //junit.textui.TestRunner.run(new MyTestCaseTest("test1"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // CatalogWSDL.setModelSourceProvider(new TestModelSourceProvider());

        clearWorkDir();
        LocalFileSystem fs = new LocalFileSystem();
        fs.setRootDirectory(getWorkDir());
        rootDir = fs.getRoot();
    // org.openide.util.test.MockLookup lookup;
    // FileUtil.setMIMEType("xml", "text/xml");        
    // FileUtil.setMIMEType("wsdl", "text/x-wsdl+xml");
    // FileUtil.setMIMEType("xml", "text/x-wsdl+xml");
    //   System.out.println("JUNIT Data Dir " + getDataDir());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void printFileObject(FileObject fo) {
        if (true) {
            return;
        }
        InputStream in = null;
        try {
            in = fo.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            BufferedReader buff = new BufferedReader(reader);
            String line = null;
            while ((line = buff.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    /**
     * Test of getEntries method, of class CatalogWSDL.
     */
    public void testGetEntries() {
        System.out.println("getEntriesEmpty");
        CatalogWSDL instance = new CatalogWSDL();
        List<Entry> expResult = new ArrayList<Entry>();
        List<Entry> result = instance.getEntries();
        assertEquals(expResult.size(), result.size());
        instance.addEntry(Entry.createXSDEntry("http://x/y.xsd", "/x/y.xsd"));
        instance.addEntry(Entry.createWSDLEntry("http://x/y.wsdl", "/x/y.wsdl"));
        result = instance.getEntries();
        assertEquals(result.size(), 2);
    }

    /**
     * Test of addAllEntries method, of class CatalogWSDL.
     */
    public void testAddAllEntries() {
        System.out.println("addAllEntries");
        CatalogWSDL instance = new CatalogWSDL();
        List<Entry> expResult = new ArrayList<Entry>();
        List<Entry> result = instance.getEntries();
        assertEquals(expResult.size(), result.size());
        instance.addEntry(Entry.createXSDEntry("http://x/y.xsd", "/x/y.xsd"));
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(Entry.createWSDLEntry("http://x/y.wsdl", "/x/y.wsdl"));
        entries.add(Entry.createXSDEntry(TEST_XSD_NS + 1, TEST_XSD_LOC + 1));
        instance.addAllEntries(entries);
        result = instance.getEntries();
        assertEquals(result.size(), 3);
    }

    /**
     * Test of addEntry method, of class CatalogWSDL.
     */
    public void testAddEntry() {
        System.out.println("addEntry");
        Entry entry = null;
        CatalogWSDL instance = new CatalogWSDL();
        boolean expResult = false;
        boolean result = instance.addEntry(entry);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of removeEntry method, of class CatalogWSDL.
     */
    public void testRemoveEntry() {
        System.out.println("removeEntry");
        Entry entry = null;
        CatalogWSDL instance = new CatalogWSDL();
        boolean expResult = false;
        boolean result = instance.removeEntry(entry);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of clear method, of class CatalogWSDL.
     */
    public void testClear() {
        System.out.println("clear");
        CatalogWSDL instance = new CatalogWSDL();
        instance.addEntry(Entry.createXSDEntry("http://x/y.xsd", "/x/y.xsd"));
        instance.addEntry(Entry.createWSDLEntry("http://x/y.wsdl", "/x/y.wsdl"));
        List<Entry> result = instance.getEntries();
        assertEquals(result.size(), 2);
        instance.clear();
        result = instance.getEntries();
        assertEquals(result.size(), 0);
    }

    /**
     * Test of testCreateEmptyCatalogWSDLFile method, of class CatalogWSDL.
     */
    public void testCreateEmptyCatalogWSDLFile() throws Exception {
        System.out.println("createCatalogWSDLFile");
        FileObject catDir = rootDir;
        FileObject result = CatalogWSDLSerializer.createCatalogWSDLFile(catDir, true);
        assertNotNull(result);
    }

    /**
     * Test of loadCatalogWSDL method, of class CatalogWSDL.
     */
    public void testLoadCatalogWSDL() throws Exception {
// *** DataObject org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLDataObject
// *** Document typeorg.netbeans.modules.editor.NbEditorDocument
////        FileObject xmlFSRoot = FileUtil.getConfigRoot();
////        assertNotNull(xmlFSRoot);
////        System.out.println("#### Default FileSystem path " + xmlFSRoot.getName());
////        Enumeration folders = xmlFSRoot.getFolders(true);
////        for (;folders.hasMoreElements();) {
////            FileObject fo = (FileObject)folders.nextElement();
////            System.out.println(fo.getPath());
////        }

        System.out.println("loadCatalogWSDL");
        FileObject catDir = rootDir;
        FileObject catFO = CatalogWSDLSerializer.createCatalogWSDLFile(catDir, true);
////        DataObject dObj = DataObject.find(catFO);
////        System.out.println("DataObject " + dObj.getClass().getName());
////        assertNotNull(dObj);
////        EditorCookie ec = (EditorCookie) dObj.getCookie(EditorCookie.class);
////        Document doc = ec.openDocument();
////        assertNotNull(doc);
////        if (!(doc instanceof BaseDocument)) {
////            fail("Document return is not a BaseDocument" + doc.getClass().getName());
////        }
        CatalogWSDL result = CatalogWSDL.loadCatalogWSDL(catDir);
        assertNotNull(result);
        assertEquals(result.getEntries().size(), 0);
    }

    /**
     * Test of saveCatalogWSDL method, of class CatalogWSDL.
     */
    public void testSaveCatalogWSDL() throws Exception {
        System.out.println("saveCatalogWSDL");

        FileObject catDir = rootDir;

        CatalogWSDL catalog = new CatalogWSDL();

        catalog.addEntry(Entry.createWSDLEntry(TEST_WSDL_NS + 1, TEST_WSDL_LOC + 1));
        catalog.addEntry(Entry.createXSDEntry(TEST_XSD_NS + 1, TEST_XSD_LOC + 1));

        CatalogWSDL.saveCatalogWSDL(catalog, catDir);

        CatalogWSDL saveCat1 = CatalogWSDL.loadCatalogWSDL(rootDir);

        assertEquals(saveCat1.getEntries().size(), 2);

        assertEquals(saveCat1.getEntries(EntryType.WSDL).size(), 1);

        assertEquals(saveCat1.getEntries(EntryType.XSD).size(), 1);

        catalog.addEntry(Entry.createWSDLEntry(TEST_WSDL_NS + 2, TEST_WSDL_LOC + 2));
        catalog.addEntry(Entry.createXSDEntry(TEST_XSD_NS + 2, TEST_XSD_LOC + 2));




////        FileObject saveDir =  FileUtil.createFolder(rootDir, "save");
////        CatalogWSDL result = CatalogWSDL.loadCatalogWSDL(catDir);
////        assertNotNull(result);
//////        for ( Entry entry : result.getEntries() ) {
//////            System.out.println(entry);
//////        }
////        
////        CatalogWSDL loadCat1 = CatalogWSDL.loadCatalogWSDL(catDir);
////        System.out.println("LoadCat1 Catalog WSDL Entries  " + loadCat1.getEntries().size());
//////        for ( Entry entry : loadCat1.getEntries() ) {
//////            System.out.println(entry);
//////        }        
////        loadCat1.addEntry(Entry.createXSDEntry("http://example/xsd/c", "xyz/123/c.xsd"));
////        System.out.println("LoadCat1 Catalog WSDL Entries " + loadCat1.getEntries().size());
////        
////        CatalogWSDL.saveCatalogWSDL(loadCat1, catDir);
////        printFileObject(catDir.getFileObject(CatalogWSDL.CATALOG_WSDL_FILE));
////
////        CatalogWSDL loadCat2 = CatalogWSDL.loadCatalogWSDL(catDir);
////        System.out.println("loadCat2 Catalog WSDL Entries " + loadCat2.getEntries().size());
////        printFileObject(catDir.getFileObject(CatalogWSDL.CATALOG_WSDL_FILE));
////        
////        assertEquals(result.getEntries().size(), 0);
    }

    /**
     * Test Modify CatalogWSDL .
     */
    public void testModifyCatalogWSDL() throws Exception {
        System.out.println("ModifyCatalogWSDL");

        FileObject catDir = rootDir;

        CatalogWSDL catalog = new CatalogWSDL();

        catalog.addEntry(Entry.createWSDLEntry(TEST_WSDL_NS + 1, TEST_WSDL_LOC + 1));
        CatalogWSDL.saveCatalogWSDL(catalog, catDir);
        CatalogWSDL loadCat1 = CatalogWSDL.loadCatalogWSDL(rootDir);
        assertEquals(loadCat1.getEntries().size(), 1);
        assertEquals(loadCat1.getEntries(EntryType.WSDL).size(), 1);
        assertEquals(loadCat1.getEntries(EntryType.XSD).size(), 0);


        loadCat1.addEntry(Entry.createXSDEntry(TEST_XSD_NS + 1, TEST_XSD_LOC + 1));
        CatalogWSDL.saveCatalogWSDL(loadCat1, catDir);
        CatalogWSDL loadCat2 = CatalogWSDL.loadCatalogWSDL(rootDir);
        assertEquals(loadCat2.getEntries().size(), 2);
        assertEquals(loadCat2.getEntries(EntryType.WSDL).size(), 1);
        assertEquals(loadCat2.getEntries(EntryType.XSD).size(), 1);

        loadCat2.addEntry(Entry.createWSDLEntry(TEST_WSDL_NS + 2, TEST_WSDL_LOC + 2));
        loadCat2.addEntry(Entry.createXSDEntry(TEST_XSD_NS + 2, TEST_XSD_LOC + 2));
        CatalogWSDL.saveCatalogWSDL(loadCat2, catDir);
        CatalogWSDL loadCat3 = CatalogWSDL.loadCatalogWSDL(rootDir);
        assertEquals(loadCat3.getEntries().size(), 4);
        assertEquals(loadCat3.getEntries(EntryType.WSDL).size(), 2);
        assertEquals(loadCat3.getEntries(EntryType.XSD).size(), 2);

    }

////    public static void printDocument(Document doc) {
////        try {
////            System.out.println(doc.getText(0, doc.getLength()));
////        } catch (BadLocationException ex) {
////            ex.printStackTrace();
////        }
////    }
////
////    public void printCatalogWSDL(CatalogWSDL cat) {
////        System.out.println("BEGIN print catalog entries");
////        for (Entry entry : cat.getEntries()) {
////            System.out.println(entry.toString());
////        }
////        System.out.println("END print catalog entries");
////    }
////    
////    public static void printCatalogWSDL(Definitions defs) {
////
////        System.out.println("**** BEGIN Printing WSDL Model ****");
////        System.out.println("Printing wsdl imports");
////        Collection<Import> imports = defs.getImports();
////        for (Import imp : imports) {
////            System.out.println(imp);
////        }
////        System.out.println("Printing schema imports");
////        Collection<Schema> schemas = defs.getTypes().getSchemas();
////        Schema schema = null;
////        if (schemas != null && schemas.size() > 0) {
////            schema = schemas.iterator().next();
////        }
////
////        if (schema != null) {
////            Collection<org.netbeans.modules.xml.schema.model.Import> schemaImports =
////                    schema.getImports();
////            for (Import imp : imports) {
////                System.out.println(imp);
////            }
////        }
////        System.out.println("**** END Printing WSDL Model ****");
////    }
////
////    public static void printDocumentOfWSDLModel(WSDLModel wsdlModel) {
////        printCatalogWSDL(wsdlModel.getDefinitions());
////        System.out.println("@@@@@ ModelAccess Class " + wsdlModel.getAccess().getClass().getName());
////        XDMAccess access = (XDMAccess) wsdlModel.getAccess();
////        XDMModel xdmModel = access.getReferenceModel();
////        // Document xdmSwingDoc = xdmModel.get
////        FlushVisitor flushvisitor = new FlushVisitor();
////        // String newXMLText = flushvisitor.flushModel(xdmModel.getDocument());
////        String newXMLText = xdmModel.getCurrentDocumentText();
////        System.out.println("####### XMD XML TEXT #### ");
////        System.out.println(newXMLText);
//////        Document myDoc = sModelSourceProvider.loadSwingDocument(catalogFO);
//////        try {
//////            Utils.replaceDocument(myDoc, newXMLText);
//////        } catch (Exception ex) {
//////            ex.printStackTrace();
//////        }
////        System.out.println("####### SWING DOC TEXT #### ");
//////        printDocument(myDoc);
////
////    }
}
