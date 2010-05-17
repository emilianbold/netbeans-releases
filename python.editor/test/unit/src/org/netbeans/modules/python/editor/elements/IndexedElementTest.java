/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.python.editor.elements;

import java.net.URL;
import java.util.List;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.python.editor.PythonAstUtils;
import org.netbeans.modules.python.editor.PythonIndexer;
import org.netbeans.modules.python.editor.PythonTestBase;
import org.netbeans.modules.python.editor.RstFormatter;
import org.python.antlr.PythonTree;

/**
 *
 * @author Tor Norbye
 */
public class IndexedElementTest extends PythonTestBase {

    public IndexedElementTest(String name) {
        super(name);
    }

    @Override
    protected List<URL> getExtraCpUrls() {
        // I'm overriding various Jython classes here for tests which causes
        // confusion when it's trying to locate classes and finds it in multiple places
//        if (!skipJython) {
//            return super.getExtraCpUrls();
//        }

        return null;
    }

    public void checkCreate(String testFile) throws Exception {
        List<SearchResult> maps;
        if (testFile.endsWith(".indexed")) {
            maps = createTestMapsFromIndexFile(getTestFile(testFile));
        } else {
            maps = createTestMaps(getTestFile(testFile));
        }

//        PythonIndex pythonIndex = PythonIndex.get(null, null);

        int mapNo = -1;
        for (SearchResult map : maps) {
            mapNo++;

            //if (mapNo < 1853) {
            //    continue;
            //}

            String url = map.getPersistentUrl();
            assertNotNull(url);

//            String clz = map.getValue("class");
//            if (clz != null) {
//                String fqn = map.getValue("fqn");
//                String attrs = map.getValue("clzattrs");
//
//                String originalAttrs = attrs;
//                if (attrs != null) {
//                    int flags = IndexedElement.stringToFlags(attrs);
//                    if (flags != 0) {
//                        int begin = attrs.indexOf("|");
//                        assertTrue(begin != -1);
//                        int end = attrs.indexOf(';', begin);
//                        if (end == -1) {
//                            end = attrs.length();
//                        }
//                        attrs = attrs.substring(0, begin) + IndexedElement.encode(flags) + attrs.substring(end);
//                        ((TestSearchResult)map).setValue("attrs", attrs);
//
//                    }
//                }
//
//                IndexedClass cm = rubyIndex.createClass(fqn, clz, map);
//
//                if (originalAttrs != null) {
//                    if (originalAttrs.indexOf("|STATIC") != -1) {
//                        assertTrue(fqn+";"+originalAttrs, cm.isStatic());
//                    }
//                    if (originalAttrs.indexOf("|NODOC") != -1) {
//                        assertTrue(fqn+";"+originalAttrs, cm.isNoDoc());
//                    }
//                    if (originalAttrs.indexOf("|TOP_LEVEL") != -1) {
//                        assertTrue(fqn+";"+originalAttrs, cm.isTopLevel());
//                    }
//                    if (originalAttrs.indexOf("|DOCUMENTED") != -1) {
//                        assertTrue(fqn+";"+originalAttrs, cm.isDocumented());
//                    }
//                    if (originalAttrs.indexOf("|PRIVATE") != -1) {
//                        assertTrue(fqn+";"+originalAttrs, cm.isPrivate());
//                    }
//                    if (originalAttrs.indexOf("|PROTECTED") != -1) {
//                        assertTrue(fqn+";"+originalAttrs, cm.isProtected());
//                    }
//                }
//
//                boolean skip = false;
//                // Skip known problems
//                skip = !Character.isUpperCase(cm.getName().charAt(0));
//                if (url.endsWith("/action_controller.rb") || url.endsWith("/active_record.rb") || url.endsWith("/action_mailer.rb") || url.endsWith("/action_view.rb")) {
//                    // These classes are faked up by RubyIndexer
//                    skip = true;
//                }
//                if (fqn.equals("Object")) {
//                    // Top level methods may not specify Object
//                    skip = true;
//                }
//
//                if (!skip) {
//                    assertEquals(url, cm.getFileUrl());
//                    assertNotNull(cm.getFileUrl(), cm.getFileObject());
//                    CompilationInfo[] compilationInfoHolder = new CompilationInfo[1];
//                    PythonTree node = AstUtilities.getForeignNode(cm, compilationInfoHolder);
//                    assertNotNull("Map " + mapNo + ", url=" + url + ":" + fqn, node);
//                } // else: Lots of problems with lowerclass class names - comes from anonymous classes assigned to variables
//            }

            String[] methods = map.getValues("member");
            if (methods != null) {
                //System.err.println("Checking url " + url + ", " + methods.length + " methods");
                int methodCount = -1;
                for (String signature : methods) {
                    methodCount++;
                    //String methodName = signature.substring(0, signature.indexOf('('));
                    String originalSignature = signature;
                    int flags = IndexedMethod.stringToFlags(signature);
                    if (flags != 0) {
                        int begin = signature.indexOf("|",1); //1: "|" is a valid method name
                        assertTrue(begin != -1);
                        int end = signature.indexOf(';', begin);
                        if (end == -1) {
                            end = signature.length();
                        }
                        signature = signature.substring(0, begin) + IndexedElement.encode(flags) + signature.substring(end);
                    }

                    String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
                    String module = map.getValue(PythonIndexer.FIELD_IN);
                    IndexedElement method = IndexedElement.create(signature, module, url, clz);

                    assertEquals(url, method.getFilenameUrl());

                    if (originalSignature.indexOf("|STATIC") != -1) {
                        assertTrue(originalSignature, method.isStatic());
                    }
                    if (originalSignature.indexOf("|DEPRECATED") != -1) {
                        assertTrue(originalSignature, method.isDeprecated());
                    }
                    if (originalSignature.indexOf("|NODOC") != -1) {
                        assertTrue(originalSignature, method.isNoDoc());
                    }
//                    if (originalSignature.indexOf("|BLOCK_OPTIONAL") != -1) {
//                        assertTrue(originalSignature, method.isBlockOptional());
//                    }
//                    if (originalSignature.indexOf("|TOP_LEVEL") != -1) {
//                        assertTrue(originalSignature, method.isTopLevel());
//                    }
                    if (originalSignature.indexOf("|DOCUMENTED") != -1) {
                        assertTrue(originalSignature, method.isDocumented());
                    }
                    if (originalSignature.indexOf("|PRIVATE") != -1) {
                        assertTrue(originalSignature, method.isPrivate());
                    }
//                    if (originalSignature.indexOf("|PROTECTED") != -1) {
//                        assertTrue(originalSignature, method.isProtected());
//                    }

//                    // Known exceptions
//                    if (url.endsWith("/schema_definitions.rb")) {
//                        // These are generated dynamically, no actual AST node
//                        continue;
//                    }
//                    // The bug here is that there is a sessions_key local variable we store
//                    // a class on -- but this isn't a class named session_key!!
//                    // Too late to mess with that now
//                    if (url.endsWith("/scenario_runner.rb")) {
//                        continue;
//                    }
//                    if (url.endsWith("/drb_server.rb")) {
//                        continue;
//                    }
//                    if (url.endsWith("/set.rb")) {
//                        // Another innerclass
//                        continue;
//                    }
//                    if (url.endsWith("/testrunner.rb")) {
//                        // Another innerclass
//                        continue;
//                    }
//                    if (signature.indexOf("hyphenate_to") != -1) {
//                        continue;
//                    }

                    assertNotNull(method.getFilenameUrl(), method.getFileObject());
                    if (method.isDocumented()) {
                        String doc = RstFormatter.getDocumentation(method);
                        //CompilationInfo[] compilationInfoHolder = new CompilationInfo[1];
                        //PythonTree node = PythonAstUtils.getForeignNode(method, compilationInfoHolder);
                        //assertNotNull("Map " + mapNo + ":" + methodCount + ", url=" + url + ":" + signature + "; " + method.getSignature(), node);
                        assertTrue(method.attributes, doc != null && doc.length() > 0);
                    }
                    if (method.isDocOnly()) {
                        assertTrue(method.getFilenameUrl().indexOf(".rst") != -1);
                    }
                    if (!method.getFilenameUrl().endsWith(".rst")) {
                        CompilationInfo[] compilationInfoHolder = new CompilationInfo[1];
                        PythonTree node = PythonAstUtils.getForeignNode(method, compilationInfoHolder);
                        assertNotNull("Map " + mapNo + ":" + methodCount + ", url=" + url + ":" + signature + "; " + method.getSignature(), node);
                    }
                }
            }

            String[] items = map.getValues("items");
            if (items != null) {
                //System.err.println("Checking url " + url + ", " + items.length + " methods");
                int methodCount = -1;
                for (String signature : items) {
                    methodCount++;
                    String originalSignature = signature;
                    int flags = IndexedMethod.stringToFlags(signature);
                    if (flags != 0) {
                        int begin = signature.indexOf("|",1); //1: "|" is a valid method name
                        assertTrue(begin != -1);
                        int end = signature.indexOf(';', begin);
                        if (end == -1) {
                            end = signature.length();
                        }
                        signature = signature.substring(0, begin) + IndexedElement.encode(flags) + signature.substring(end);
                    }

                    String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
                    IndexedElement method = IndexedElement.create(signature, module, url, null);

                    assertEquals(url, method.getFilenameUrl());

                    if (originalSignature.indexOf("|STATIC") != -1) {
                        assertTrue(originalSignature, method.isStatic());
                    }
                    if (originalSignature.indexOf("|DEPRECATED") != -1) {
                        assertTrue(originalSignature, method.isDeprecated());
                    }
                    if (originalSignature.indexOf("|NODOC") != -1) {
                        assertTrue(originalSignature, method.isNoDoc());
                    }
//                    if (originalSignature.indexOf("|BLOCK_OPTIONAL") != -1) {
//                        assertTrue(originalSignature, method.isBlockOptional());
//                    }
//                    if (originalSignature.indexOf("|TOP_LEVEL") != -1) {
//                        assertTrue(originalSignature, method.isTopLevel());
//                    }
                    if (originalSignature.indexOf("|DOCUMENTED") != -1) {
                        assertTrue(originalSignature, method.isDocumented());
                    }
                    if (originalSignature.indexOf("|PRIVATE") != -1) {
                        assertTrue(originalSignature, method.isPrivate());
                    }
//                    if (originalSignature.indexOf("|PROTECTED") != -1) {
//                        assertTrue(originalSignature, method.isProtected());
//                    }

//                    // Known exceptions
//                    if (url.endsWith("/schema_definitions.rb")) {
//                        // These are generated dynamically, no actual AST node
//                        continue;
//                    }
//                    // The bug here is that there is a sessions_key local variable we store
//                    // a class on -- but this isn't a class named session_key!!
//                    // Too late to mess with that now
//                    if (url.endsWith("/scenario_runner.rb")) {
//                        continue;
//                    }
//                    if (url.endsWith("/drb_server.rb")) {
//                        continue;
//                    }
//                    if (url.endsWith("/set.rb")) {
//                        // Another innerclass
//                        continue;
//                    }
//                    if (url.endsWith("/testrunner.rb")) {
//                        // Another innerclass
//                        continue;
//                    }
//                    if (signature.indexOf("hyphenate_to") != -1) {
//                        continue;
//                    }

                    assertNotNull(method.getFilenameUrl(), method.getFileObject());
                    if (method.isDocumented()) {
                        String doc = RstFormatter.getDocumentation(method);
                        assertTrue(doc != null && doc.length() > 0);
                    }
                    if (method.isDocOnly()) {
                        assertTrue(method.getFilenameUrl().indexOf(".rst") != -1);
                    }
                    
                    if (!method.getFilenameUrl().endsWith(".rst")) {
                        CompilationInfo[] compilationInfoHolder = new CompilationInfo[1];
                        PythonTree node = PythonAstUtils.getForeignNode(method, compilationInfoHolder);
                        assertNotNull("Map " + mapNo + ":" + methodCount + ", url=" + url + ":" + signature + "; " + method.getSignature(), node);
                    }
                }
            }

        }
    }

    public void testIndex1() throws Exception {
        checkCreate("testfiles/ConfigParser.py.indexed");
    }

    public void testIndex2() throws Exception {
        checkCreate("testfiles/datetime.py.indexed");
    }

    public void testIndex3() throws Exception {
        checkCreate("testfiles/doc.py.indexed");
    }

    public void testIndex4() throws Exception {
        checkCreate("testfiles/md5.py.indexed");
    }

    public void testIndex5() throws Exception {
        checkCreate("testfiles/scope.py.indexed");
    }

    public void testIndex6() throws Exception {
        checkCreate("testfiles/httplib.py.indexed");
    }

    public void testIndex7() throws Exception {
        checkCreate("testfiles/minicompat.py.indexed");
    }

    public void testIndex8() throws Exception {
        checkCreate("testfiles/socket.py.indexed");
    }

    public void testIndex9() throws Exception {
        checkCreate("testfiles/jreload.py.indexed");
    }

    public void testIndex10() throws Exception {
        checkCreate("testfiles/doctest.py.indexed");
    }

    public void testIndex11() throws Exception {
        checkCreate("testfiles/zipfile.py");
    }

    public void testIndex12() throws Exception {
        checkCreate("testfiles/os.py");
    }

    public void testIndex13() throws Exception {
        checkCreate("testfiles/unittest.py");
    }

    public void testIndex14() throws Exception {
        checkCreate("testfiles/properties.py");
    }

    public void testIndex15() throws Exception {
        checkCreate("testfiles/tarfile.py");
    }

    public void testIndex16() throws Exception {
        checkCreate("testfiles/rst/pickle.rst");
    }

    public void testRstIndex1() throws Exception {
        checkCreate("testfiles/rst/zipfile.rst.indexed");
    }

    // Known fail -- I'm not properly indexing the set < other and set > other stuff
    // in the stdtypes.rst file!
    //public void testRstIndex2() throws Exception {
    //    checkCreate("testfiles/rst/stdtypes.rst.indexed");
    //}

    public void testRstIndex3() throws Exception {
        checkCreate("testfiles/rst/platform.rst.indexed");
    }

    public void testRstIndex4() throws Exception {
        checkCreate("testfiles/rst/smtpd.rst.indexed");
    }

    public void testRstIndex5() throws Exception {
        checkCreate("testfiles/rst/exceptions.rst.indexed");
    }

    public void testRstIndex6() throws Exception {
        checkCreate("testfiles/rst/logging.rst.indexed");
    }

     public void testRstIndex7() throws Exception {
        checkCreate("testfiles/rst/string.rst");
    }

    public void testRstIndex8() throws Exception {
        checkCreate("testfiles/rst/bz2.rst");
    }

    public void testRstIndex9() throws Exception {
        checkCreate("testfiles/rst/constants.rst");
    }

    public void testRstIndex10() throws Exception {
        checkCreate("testfiles/rst/operator.rst");
    }

    public void testRstIndex11() throws Exception {
        checkCreate("testfiles/rst/collections.rst");
    }

    public void testRstIndex12() throws Exception {
        checkCreate("testfiles/rst/ctypes.rst");
    }

    public void testRstIndex13() throws Exception {
        checkCreate("testfiles/rst/stub_missing.rst");
    }

//   public void testIndexEgg() throws Exception {
//        checkCreate("testfiles/antlr_python_runtime-3.1.1-py2.5.egg");
//    }

}
