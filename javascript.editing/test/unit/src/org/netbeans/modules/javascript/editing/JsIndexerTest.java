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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.javascript.editing;

import java.io.IOException;
import java.util.Collections;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.IndexDocument;
import org.netbeans.modules.gsf.api.IndexDocumentFactory;

/**
 * @author Tor Norbye
 */
public class JsIndexerTest extends JsTestBase {
    
    public JsIndexerTest(String testName) {
        super(testName);
        initializeRegistry();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

   private String sortCommaList(String s) {
        String[] items = s.split(",");
        Arrays.sort(items);
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(item);
        }

        return sb.toString();
    }
    
    private String prettyPrintValue(String key, String value) {
        if (value == null) {
            return value;
        }
        if (JsIndexer.FIELD_BASE.equals(key) ||
                JsIndexer.FIELD_FQN.equals(key)) {
            // Decode the attributes
            int attributeIndex = 0;
            for (int i = 0; i < IndexedElement.FLAG_INDEX; i++) {
                attributeIndex = value.indexOf(';', attributeIndex+1);
            }
            int flags = IndexedElement.decode(value, attributeIndex+1,0);
            String desc = IndexedElement.decodeFlags(flags);
            value = value.substring(0, attributeIndex) + desc + value.substring(attributeIndex+3);
        }

        return value;
    }

    public String prettyPrint(String fileUrl, List<IndexDocument> documents, String localUrl) throws IOException {
        List<String> nonEmptyDocuments = new ArrayList<String>();
        List<String> emptyDocuments = new ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        sb.append("Delete:");
        sb.append("  ");
        sb.append("source");
        sb.append(" : ");
        sb.append(fileUrl);
        sb.append("\n");
        sb.append("\n");  
        nonEmptyDocuments.add(sb.toString());
        

        if (documents != null) {
            for (IndexDocument d : documents) {
                IndexDocumentImpl doc = (IndexDocumentImpl)d;

                sb = new StringBuilder();
                sb.append("Indexed:");
                sb.append("\n");
                List<String> strings = new ArrayList<String>();

                List<String> keys = doc.indexedKeys;
                List<String> values = doc.indexedValues;
                for (int i = 0, n = keys.size(); i < n; i++) {
                    String key = keys.get(i);
                    String value = values.get(i);
                    strings.add(key + " : " + prettyPrintValue(key, value));
                }
                Collections.sort(strings);
                for (String string : strings) {
                    sb.append("  ");
                    sb.append(string);
                    sb.append("\n");
                }

                sb.append("\n");
                sb.append("Not Indexed:");
                sb.append("\n");
                strings = new ArrayList<String>();
                keys = doc.unindexedKeys;
                values = doc.unindexedValues;
                for (int i = 0, n = keys.size(); i < n; i++) {
                    String key = keys.get(i);
                    String value = prettyPrintValue(key, values.get(i));
                    if (value.indexOf(',') != -1) {
                        value = sortCommaList(value);
                    }
                    strings.add(key + " : " + value);
                }

                Collections.sort(strings);
                for (String string : strings) {
                    sb.append("  ");
                    sb.append(string);
                    sb.append("\n");
                }

                String s = sb.toString();
                if (doc.indexedKeys.size() == 0 && doc.unindexedKeys.size() == 0) {
                    emptyDocuments.add(s);
                } else {
                    nonEmptyDocuments.add(s);
                }
            }
        }

        Collections.sort(emptyDocuments);
        Collections.sort(nonEmptyDocuments);
        sb = new StringBuilder();
        int documentNumber = 0;
        for (String s : emptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }

        for (String s : nonEmptyDocuments) {
            sb.append("\n\nDocument ");
            sb.append(Integer.toString(documentNumber++));
            sb.append("\n");
            sb.append(s);
        }


        return sb.toString().replace(localUrl, "<TESTURL>");
    }
        
        
    private class IndexDocumentImpl implements IndexDocument {
        private Index index;
        private List<String> indexedKeys = new ArrayList<String>();
        private List<String> indexedValues = new ArrayList<String>();
        private List<String> unindexedKeys = new ArrayList<String>();
        private List<String> unindexedValues = new ArrayList<String>();

        IndexDocumentImpl(Index index) {
            this.index = index;
        }
        
        public void addPair(String key, String value, boolean indexed) {
            if (indexed) {
                indexedKeys.add(key);
                indexedValues.add(value);
            } else {
                unindexedKeys.add(key);
                unindexedValues.add(value);
            }
        }
    }

    private class IndexDocumentFactoryImpl implements IndexDocumentFactory {
        Index index;
        IndexDocumentFactoryImpl(Index index) {
            this.index = index;
        }

        public IndexDocument createDocument(int initialPairs) {
            return new IndexDocumentImpl(index);
        }
    }
    
    private List<IndexDocument> indexFile(String relFilePath) throws Exception {
        CompilationInfo info = getInfo(relFilePath);
        JsParseResult rpr = AstUtilities.getParseResult(info);

        JsIndexer indexer = new JsIndexer();
        JsIndex.setClusterUrl("file:/bogus"); // No translation
        IndexDocumentFactory factory = new IndexDocumentFactoryImpl(info.getIndex(JsMimeResolver.JAVASCRIPT_MIME_TYPE));
        List<IndexDocument> result = indexer.index(rpr, factory);
        
        return result;
    }
    
    private void checkIndexer(String relFilePath) throws Exception {
        File jsFile = new File(getDataDir(), relFilePath);
        String fileUrl = jsFile.toURI().toURL().toExternalForm();
        String localUrl = fileUrl;
        int index = localUrl.lastIndexOf('/');
        if (index != -1) {
            localUrl = localUrl.substring(0, index);
        }
        
        List<IndexDocument> result = indexFile(relFilePath);
        String annotatedSource = prettyPrint(fileUrl, result, localUrl);

        assertDescriptionMatches(relFilePath, annotatedSource, false, ".indexed");
    }
    
    private IndexedElement findElement(List<IndexDocument> documents, String key, String valuePrefix, JsIndex index) {
        for (IndexDocument document : documents) {
            IndexDocumentImpl doc = (IndexDocumentImpl)document;
            for (int i = 0, n = doc.indexedKeys.size(); i < n; i++) {
                String k = doc.indexedKeys.get(i);
                if (k.equals(key)) {
                    String v = doc.indexedValues.get(i);
                    if (v.startsWith(valuePrefix)) {
                        return IndexedElement.create(valuePrefix, v, "file:/bogus", index, false);
                    }
                }
            }
        }
        
        return null;
    }
    
    public void testIndex0() throws Exception {
        checkIndexer("testfiles/prototype.js");
    }

    public void testIndex1() throws Exception {
        checkIndexer("testfiles/SpryEffects.js");
    }

    public void testIndex2() throws Exception {
        checkIndexer("testfiles/dragdrop.js");
    }

    public void testIndex3() throws Exception {
        checkIndexer("testfiles/dojo.js.uncompressed.js");
    }

    public void testSimple() throws Exception {
        checkIndexer("testfiles/simple.js");
    }

    public void testElement() throws Exception {
        checkIndexer("testfiles/stub_Element.js");
    }

    public void testWindow() throws Exception {
        checkIndexer("testfiles/stub_dom_Window.js");
    }
    
    public void testRestore1() throws Exception {
        List<IndexDocument> docs = indexFile("testfiles/stub_dom_Window.js");
        assertTrue(docs.size() > 0);
        JsIndex index = JsIndex.get(((IndexDocumentImpl)docs.get(0)).index);
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "window.opendialog", index);
        assertNotNull(element);
        assertEquals("Window.openDialog", element.getName());
        IndexedFunction f = (IndexedFunction)element;
        String[] args = f.getArgs();
        String[] expected = new String[] { "url","name","features","arg1","arg2" };
        assertEquals(expected.length,args.length);
        for (int i = 0; i < args.length; i++) {
            assertEquals(expected[i],args[i]);
        }
    }

    public void testRestore2() throws Exception {
        List<IndexDocument> docs = indexFile("testfiles/stub_dom_Window.js");
        JsIndex index = JsIndex.get(((IndexDocumentImpl)docs.get(0)).index);
        IndexedElement element = findElement(docs, JsIndexer.FIELD_BASE, "opendialog", index);
        assertNotNull(element);
        assertEquals("openDialog", element.getName());
        assertEquals("Window", element.getIn());
        IndexedFunction f = (IndexedFunction)element;
        String[] args = f.getArgs();
        String[] expected = new String[] { "url","name","features","arg1","arg2" };
        assertEquals(expected.length,args.length);
        for (int i = 0; i < args.length; i++) {
            assertEquals(expected[i],args[i]);
        }
    }

    public void testRestore3() throws Exception {
        List<IndexDocument> docs = indexFile("testfiles/stub_dom_Window.js");
        JsIndex index = JsIndex.get(((IndexDocumentImpl)docs.get(0)).index);
        IndexedElement element = findElement(docs, JsIndexer.FIELD_BASE, "opendialog", index);
        assertNotNull(element);
        assertEquals("openDialog", element.getName());
        assertEquals("Window", element.getIn());
        IndexedFunction f = (IndexedFunction)element;
        String[] args = f.getArgs();
        String[] expected = new String[] { "url","name","features","arg1","arg2" };
        assertEquals(expected.length,args.length);
        for (int i = 0; i < args.length; i++) {
            assertEquals(expected[i],args[i]);
        }
    }

    public void testRestore4() throws Exception {
        List<IndexDocument> docs = indexFile("testfiles/simple.js");
        JsIndex index = JsIndex.get(((IndexDocumentImpl)docs.get(0)).index);
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "donal", index);
        assertNotNull(element);
        assertEquals("DonaldDuck", element.getName());
    }

    public void testRestore5() throws Exception {
        List<IndexDocument> docs = indexFile("testfiles/simple.js");
        JsIndex index = JsIndex.get(((IndexDocumentImpl)docs.get(0)).index);
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "donaldduck.m", index);
        assertNotNull(element);
        // TODO - transfer logic from JsIndex.getFqn logic into IndexElement.create
        // so that I get "Mickey" here
        assertEquals("DonaldDuck.Mickey", element.getName());
    }

    public void testRestore6() throws Exception {
        List<IndexDocument> docs = indexFile("testfiles/simple.js");
        JsIndex index = JsIndex.get(((IndexDocumentImpl)docs.get(0)).index);
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "donaldduck.mickey.b", index);
        assertNotNull(element);
        // TODO - transfer logic from JsIndex.getFqn logic into IndexElement.create
        // so that I get "Baz" here
        assertEquals("DonaldDuck.Mickey.Baz", element.getName());
    }

    public void testRestore7() throws Exception {
        List<IndexDocument> docs = indexFile("testfiles/dojo.js.uncompressed.js");
        JsIndex index = JsIndex.get(((IndexDocumentImpl)docs.get(0)).index);
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "dojo.deferred", index);
        assertNotNull(element);
        // TODO - transfer logic from JsIndex.getFqn logic into IndexElement.create
        // so that I get "Baz" here
        assertEquals("dojo.Deferred", element.getName());
        assertEquals(ElementKind.CONSTRUCTOR, element.getKind());
        IndexedFunction func = (IndexedFunction)element;
        assertEquals(1, func.getArgs().length);
        assertEquals("canceller", func.getArgs()[0]);
    }
    
    public void testOldPrototypes() throws Exception {
        checkIndexer("testfiles/oldstyle-prototype.js");
    }

    public void testNewPrototypes() throws Exception {
        checkIndexer("testfiles/newstyle-prototype.js");
    }

    public void testFunctionStyle() throws Exception {
        checkIndexer("testfiles/class-via-function.js");
    }
    
    public void testExtStyle() throws Exception {
        checkIndexer("testfiles/class-inheritance-ext.js");
    }

    public void testIndexEvent() throws Exception {
        checkIndexer("testfiles/simple2.js");
    }

    public void testDomNode() throws Exception {
        checkIndexer("testfiles/stub_dom2_Node.js");
    }

    public void testEvents() throws Exception {
        checkIndexer("testfiles/events.js");
    }

    public void testYahoo() throws Exception {
        checkIndexer("testfiles/yui.js");
    }
}
