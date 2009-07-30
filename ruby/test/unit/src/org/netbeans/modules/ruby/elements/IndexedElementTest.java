/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.elements;

import java.util.List;
import org.jrubyparser.ast.Node;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.modules.ruby.RubyIndex;
import org.netbeans.modules.ruby.RubyTestBase;

/**
 * @author Tor Norbye
 */
public class IndexedElementTest extends RubyTestBase {
    public IndexedElementTest(String name) {
        super(name);
    }

    //public void testCreateAll() throws Exception {
    //    checkCreate("testfiles/dump.Ruby");
    //}

    public void testCreate0() throws Exception {
        checkCreate("testfiles/map0.map");
    }

    public void testCreate1() throws Exception {
        checkCreate("testfiles/map1.map");
    }

    public void testCreate2() throws Exception {
        checkCreate("testfiles/map2.map");
    }

    public void testCreate3() throws Exception {
        checkCreate("testfiles/map3.map");
    }

    public void testCreate4() throws Exception {
        checkCreate("testfiles/map4.map");
    }

    public void testCreate5() throws Exception {
        checkCreate("testfiles/map5.map");
    }

    public void testCreate6() throws Exception {
        checkCreate("testfiles/map6.map");
    }

    public void testCreate7() throws Exception {
        checkCreate("testfiles/map7.map");
    }

    public void testCreate8() throws Exception {
        checkCreate("testfiles/map8.map");
    }

    public void testCreate9() throws Exception {
        checkCreate("testfiles/map9.map");
    }

    public void testCreate10() throws Exception {
        checkCreate("testfiles/map10.map");
    }

    public void testCreate11() throws Exception {
        checkCreate("testfiles/map11.map");
    }

    public void testCreate12() throws Exception {
        checkCreate("testfiles/map12.map");
    }

    public void testCreate13() throws Exception {
        checkCreate("testfiles/resolv.rb.indexed");
    }

    public void testCreate14() throws Exception {
        checkCreate("testfiles/top_level.rb.indexed");
    }

    public void testCreate15() throws Exception {
        checkCreate("testfiles/twoclasses.rb.indexed");
    }

    public void testCreate16() throws Exception {
        checkCreate("testfiles/unused.rb.indexed");
    }

    public void testCreate17() throws Exception {
        checkCreate("testfiles/postgresql_adapter.rb.indexed");
    }

    public void testCreate18() throws Exception {
        checkCreate("testfiles/option_parser_spec.rb.indexed");
    }

    public void testCreate19() throws Exception {
        checkCreate("testfiles/method_definer_test.rb.indexed");
    }

    public void testCreate20() throws Exception {
        checkCreate("testfiles/globals.rb.indexed");
    }

    public void testCreate21() throws Exception {
        checkCreate("testfiles/globals2.rb.indexed");
    }

    public void testCreate22() throws Exception {
        checkCreate("testfiles/empty.rb.indexed");
    }

    public void testCreate23() throws Exception {
        checkCreate("testfiles/classvar.rb.indexed");
    }

    public void testCreate24() throws Exception {
        checkCreate("testfiles/constants.rb.indexed");
    }

    // XXX - Parsing API
    public void checkCreate(String testFile) throws Exception {
        System.err.println("SKIPPING " + getName() + " - not migrated to CSL yet");
//        List<SearchResult> maps;
//        if (testFile.endsWith(".indexed")) {
//            maps = createTestMapsFromIndexFile(getTestFile(testFile));
//        } else {
//            maps = createTestMaps(getTestFile(testFile));
//        }
//
//        RubyIndex rubyIndex = RubyIndex.get(null, null);
//
//        int mapNo = -1;
//        for (SearchResult map : maps) {
//            mapNo++;
//
//            //if (mapNo < 1853) {
//            //    continue;
//            //}
//
//            String url = map.getPersistentUrl();
//            assertNotNull(url);
//
//            String clz = map.getValue("class");
//            if (clz != null) {
//                String fqn = map.getValue("fqn");
//                String attrs = map.getValue("attrs");
//
//                String originalAttrs = attrs;
//                if (attrs != null) {
//                    int flags = IndexedClass.stringToFlags(attrs);
//                    if (flags != 0) {
//                        int begin = attrs.indexOf("|");
//                        assertTrue(begin != -1);
//                        int end = attrs.indexOf(';', begin);
//                        if (end == -1) {
//                            end = attrs.length();
//                        }
//                        attrs = attrs.substring(0, begin) + IndexedMethod.flagToString(flags) + attrs.substring(end);
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
//                    Node node = AstUtilities.getForeignNode(cm, compilationInfoHolder);
//                    assertNotNull("Map " + mapNo + ", url=" + url + ":" + fqn, node);
//                } // else: Lots of problems with lowerclass class names - comes from anonymous classes assigned to variables
//            }
//
//            String[] methods = map.getValues("method");
//            if (methods != null) {
//                System.out.println("Checking url " + url + ", " + methods.length + " methods");
//                int methodCount = -1;
//                for (String signature : methods) {
//                    methodCount++;
//
//                    if (methodCount < 14) {
//                        continue;
//                    }
//
//                    String originalSignature = signature;
//                    int flags = IndexedMethod.stringToFlags(signature);
//                    if (flags != 0) {
//                        int begin = signature.indexOf("|",1); //1: "|" is a valid method name
//                        assertTrue(begin != -1);
//                        int end = signature.indexOf(';', begin);
//                        if (end == -1) {
//                            end = signature.length();
//                        }
//                        signature = signature.substring(0, begin) + IndexedMethod.flagToString(flags) + signature.substring(end);
//                    }
//                    IndexedMethod method = rubyIndex.createMethod(signature, map, false);
//                    assertEquals(url, method.getFileUrl());
//
//                    if (originalSignature.indexOf("|STATIC") != -1) {
//                        assertTrue(originalSignature, method.isStatic());
//                    }
//                    if (originalSignature.indexOf("|DEPRECATED") != -1) {
//                        assertTrue(originalSignature, method.isDeprecated());
//                    }
//                    if (originalSignature.indexOf("|NODOC") != -1) {
//                        assertTrue(originalSignature, method.isNoDoc());
//                    }
//                    if (originalSignature.indexOf("|BLOCK_OPTIONAL") != -1) {
//                        assertTrue(originalSignature, method.isBlockOptional());
//                    }
//                    if (originalSignature.indexOf("|TOP_LEVEL") != -1) {
//                        assertTrue(originalSignature, method.isTopLevel());
//                    }
//                    if (originalSignature.indexOf("|DOCUMENTED") != -1) {
//                        assertTrue(originalSignature, method.isDocumented());
//                    }
//                    if (originalSignature.indexOf("|PRIVATE") != -1) {
//                        assertTrue(originalSignature, method.isPrivate());
//                    }
//                    if (originalSignature.indexOf("|PROTECTED") != -1) {
//                        assertTrue(originalSignature, method.isProtected());
//                    }
//
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
//
//                    assertNotNull(method.getFileUrl(), method.getFileObject());
//                    CompilationInfo[] compilationInfoHolder = new CompilationInfo[1];
//                    Node node = AstUtilities.getForeignNode(method, compilationInfoHolder);
//                    assertNotNull("Map " + mapNo + ":" + methodCount + ", url=" + url + ":" + signature + "; " + method.getSignature(), node);
//                }
//            }
//
//            String[] fields = map.getValues("field");
//            if (fields != null) {
//                System.out.println("Checking url " + url + ", " + fields.length + " fields");
//                for (String signature : fields) {
//                    String originalSignature = signature;
//                    boolean isInstance = signature.indexOf("|STATIC") == -1;
//                    int flags = IndexedField.stringToFlags(signature);
//                    if (flags != 0) {
//                        int begin = signature.indexOf("|");
//                        assertTrue(begin != -1);
//                        int end = signature.indexOf(';', begin);
//                        if (end == -1) {
//                            end = signature.length();
//                        }
//                        signature = signature.substring(0, begin) + IndexedField.flagToString(flags) + signature.substring(end);
//                    }
//                    IndexedField field = rubyIndex.createField(signature, map, isInstance, false);
//                    assertEquals(url, field.getFileUrl());
//
//                    if (originalSignature.indexOf("|STATIC") != -1) {
//                        assertTrue(originalSignature, field.isStatic());
//                    }
//                    if (originalSignature.indexOf("|NODOC") != -1) {
//                        assertTrue(originalSignature, field.isNoDoc());
//                    }
//                    if (originalSignature.indexOf("|TOP_LEVEL") != -1) {
//                        assertTrue(originalSignature, field.isTopLevel());
//                    }
//                    if (originalSignature.indexOf("|DOCUMENTED") != -1) {
//                        assertTrue(originalSignature, field.isDocumented());
//                    }
//                    if (originalSignature.indexOf("|PRIVATE") != -1) {
//                        assertTrue(originalSignature, field.isPrivate());
//                    }
//                    if (originalSignature.indexOf("|PROTECTED") != -1) {
//                        assertTrue(originalSignature, field.isProtected());
//                    }
//
//
//                    assertNotNull(field.getFileUrl(), field.getFileObject());
//                    CompilationInfo[] compilationInfoHolder = new CompilationInfo[1];
//                    Node node = AstUtilities.getForeignNode(field, compilationInfoHolder);
//                    assertNotNull("Map " + mapNo + ", url=" + url + ":" + signature, node);
//                }
//            }
//        }
    }
}
