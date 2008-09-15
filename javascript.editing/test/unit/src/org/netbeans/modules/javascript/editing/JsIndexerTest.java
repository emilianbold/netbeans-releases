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

import java.util.List;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.IndexDocument;

/**
 * @author Tor Norbye
 */
public class JsIndexerTest extends JsTestBase {
    
    public JsIndexerTest(String testName) {
        super(testName);
    }

    @Override
    public String prettyPrintValue(String key, String value) {
        if (value == null) {
            return value;
        }
        int index = -1;
        if (JsIndexer.FIELD_BASE.equals(key) ||JsIndexer.FIELD_FQN.equals(key)) {
            index = IndexedElement.FLAG_INDEX;
        } else if (JsIndexer.FIELD_CLASS.equals(key)) {
            index = 1;
        }
        if (index != -1) {
            // Decode the attributes
            int attributeIndex = 0;
            for (int i = 0; i < index; i++) {
                attributeIndex = value.indexOf(';', attributeIndex+1);
            }
            int flags = IndexedElement.decode(value, attributeIndex+1,0);
            String desc = IndexedElement.decodeFlags(flags);
            value = value.substring(0, attributeIndex) + ";" + desc + value.substring(value.indexOf(';', attributeIndex+1));
        }

        return value;
    }

    protected IndexedElement findElement(List<IndexDocument> documents, String key, String valuePrefix, JsIndex index) {
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
    
    public void testIsIndexable1() throws Exception {
        checkIsIndexable("testfiles/indexable/lib.js", true);
    }
    
    public void testIsIndexable2() throws Exception {
        checkIsIndexable("testfiles/indexable/data.json", false);
    }
    
    public void testIsIndexable3() throws Exception {
        checkIsIndexable("testfiles/indexable/ext-all-debug.js", true);
        checkIsIndexable("testfiles/indexable/ext-all.js", false);
    }
    
    public void testIsIndexable4() throws Exception {
        checkIsIndexable("testfiles/indexable/yui.js", false);
        checkIsIndexable("testfiles/indexable/yui-min.js", false);
        checkIsIndexable("testfiles/indexable/yui-debug.js", true);
    }
    
    public void testIsIndexable5() throws Exception {
        checkIsIndexable("testfiles/indexable/index.html", true);
        checkIsIndexable("testfiles/indexable/servlet.jsp", true);
        checkIsIndexable("testfiles/indexable/view2.php", true);
        checkIsIndexable("testfiles/indexable/view3.rhtml", true);
    }
    
    public void testIsIndexable6() throws Exception {
        checkIsIndexable("testfiles/indexable/dojo.js", false);
        checkIsIndexable("testfiles/indexable/dojo.uncompressed.js", true);
    }
    
    public void testIsIndexable7() throws Exception {
        checkIsIndexable("testfiles/indexable/foo.js", true);
        checkIsIndexable("testfiles/indexable/foo.min.js", false);
    }
    
    public void testIsIndexable8() throws Exception {
        checkIsIndexable("testfiles/indexable/doc.sdoc", true);
    }
    
    public void testIsIndexableDeletedFiles() throws Exception {
        // isIndexable should return true for files that have been deleted as well
        
        checkIsIndexable("testfiles/indexable/lib.js", true);
    }
    
    public void testIsIndexableEverythingSdoc1() throws Exception {
        checkIsIndexable("testfiles/indexable/sdoconly/everything.sdoc", true);
    }

    public void testIsIndexableEverythingSdoc2() throws Exception {
        checkIsIndexable("testfiles/indexable/sdoconly/foo.js", false);
    }
    
    // Not yet hooked up
    //public void testIsIndexable9() throws Exception {
    //    checkIsIndexable("testfiles/indexable/view.erb", true);
    //}
    
    public void testQueryPath() throws Exception {
        JsIndexer indexer = new JsIndexer();
        assertTrue(indexer.acceptQueryPath("/foo/bar/baz"));
        assertFalse(indexer.acceptQueryPath("/foo/jruby/lib/ruby/gems/1.8/gems"));
        assertFalse(indexer.acceptQueryPath("/foo/netbeans/ruby2/rubystubs/0.2"));
    }
    
    public void testIndex0() throws Exception {
        checkIndexer("testfiles/prototype.js");
    }

    public void testIndexPrototypeNew() throws Exception {
        checkIndexer("testfiles/prototype-new.js");
    }
    
    public void testIndex1() throws Exception {
        checkIndexer("testfiles/SpryEffects.js");
    }

    public void testIndex2() throws Exception {
        checkIndexer("testfiles/dragdrop.js");
    }

    public void testIndex3() throws Exception {
        checkIndexer("testfiles/orig-dojo.js.uncompressed.js");
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
        initializeRegistry();
        JsIndex index = JsIndex.get(getInfo("testfiles/stub_dom_Window.js").getIndex(getPreferredMimeType()));
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
        initializeRegistry();
        JsIndex index = JsIndex.get(getInfo("testfiles/stub_dom_Window.js").getIndex(getPreferredMimeType()));
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
        String name = "testfiles/stub_dom_Window.js";
        List<IndexDocument> docs = indexFile(name);
        initializeRegistry();
        JsIndex index = JsIndex.get(getInfo(name).getIndex(getPreferredMimeType()));
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
        initializeRegistry();
        JsIndex index = JsIndex.get(getInfo("testfiles/stub_dom_Window.js").getIndex(getPreferredMimeType()));
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "donal", index);
        assertNotNull(element);
        assertEquals("DonaldDuck", element.getName());
    }

    public void testRestore5() throws Exception {
        String name = "testfiles/simple.js";
        List<IndexDocument> docs = indexFile(name);
        initializeRegistry();
        JsIndex index = JsIndex.get(getInfo(name).getIndex(getPreferredMimeType()));
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "donaldduck.m", index);
        assertNotNull(element);
        // TODO - transfer logic from JsIndex.getFqn logic into IndexElement.create
        // so that I get "Mickey" here
        assertEquals("DonaldDuck.Mickey", element.getName());
    }

    public void testRestore6() throws Exception {
        String name = "testfiles/simple.js";
        List<IndexDocument> docs = indexFile(name);
        initializeRegistry();
        JsIndex index = JsIndex.get(getInfo(name).getIndex(getPreferredMimeType()));
        IndexedElement element = findElement(docs, JsIndexer.FIELD_FQN, "donaldduck.mickey.b", index);
        assertNotNull(element);
        // TODO - transfer logic from JsIndex.getFqn logic into IndexElement.create
        // so that I get "Baz" here
        assertEquals("DonaldDuck.Mickey.Baz", element.getName());
    }

    public void testRestore7() throws Exception {
        String name = "testfiles/orig-dojo.js.uncompressed.js";
        List<IndexDocument> docs = indexFile(name);
        initializeRegistry();
        JsIndex index = JsIndex.get(getInfo(name).getIndex(getPreferredMimeType()));
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
    
    public void testYahooAnim() throws Exception {
        checkIndexer("testfiles/yui-anim.js");
    }
    
    public void testTypes2() throws Exception {
        checkIndexer("testfiles/types2.js");
    }

    public void testReturnTypes() throws Exception {
        checkIndexer("testfiles/returntypes.js");
    }

    public void testScriptDoc() throws Exception {
        checkIndexer("testfiles/jquery.sdoc");
    }

    public void testScriptDoc2() throws Exception {
        checkIndexer("testfiles/yui.sdoc");
    }

    public void testScriptDoc3() throws Exception {
        checkIndexer("testfiles/jquery2.sdoc");
    }

    public void testTwoNames() throws Exception {
        checkIndexer("testfiles/two-names.js");
    }

    public void testWoodStock() throws Exception {
        checkIndexer("testfiles/woodstock.sdoc");
    }

    public void testWoodStock2() throws Exception {
        checkIndexer("testfiles/woodstock2.js");
    }

    public void testWoodStock3() throws Exception {
        checkIndexer("testfiles/woodstock-body.js");
    }

    public void testDojoExtend() throws Exception {
        checkIndexer("testfiles/dnd.js");
    }

    public void testClassProps() throws Exception {
        checkIndexer("testfiles/classprops.js");
    }

    public void testWebui() throws Exception {
        checkIndexer("testfiles/bubble.js");
    }

    public void testXHR() throws Exception {
        checkIndexer("testfiles/stub_dom_XMLHttpRequest.js");
    }
}
