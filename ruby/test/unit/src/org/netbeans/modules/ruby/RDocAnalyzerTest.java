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
package org.netbeans.modules.ruby;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RDocAnalyzerTest extends RubyTestBase {

    public RDocAnalyzerTest(String name) {
        super(name);
    }

    private void assertTypes(String expectedTypes, String... comment) {
        assertTypes(new String[]{expectedTypes}, comment);
    }

    private void assertTypes(String[] expectedTypes, String... comment) {
        RubyType actualTypes = RDocAnalyzer.collectTypesFromComment(Arrays.asList(comment));
        assertEquals("Got correct tyeps", new RubyType(expectedTypes), actualTypes);
    }

    protected void checkTypesForComments(final String commentRelFilePath) throws Exception {
        File commentFile = new File(getDataDir(), commentRelFilePath);
        String fileUrl = commentFile.toURI().toURL().toExternalForm();
        String localUrl = fileUrl;
        int index = localUrl.lastIndexOf('/');
        if (index != -1) {
            localUrl = localUrl.substring(0, index);
        }

        String s = readFile(commentFile);
        StringBuilder sb = new StringBuilder();
        for (String line : s.split("\n")) {
            RubyType types = RDocAnalyzer.collectTypesFromComment(Collections.singletonList(line));
            sb.append(line + "\n  ==> [" + types.asString(", ") + "]\n\n");
        }
        assertDescriptionMatches(commentRelFilePath, sb.toString(), false, ".types");
    }

    public void testTypesFromComment() {
        assertTypes(new String[]{"Array"}, "#  enten tenten  => [0, 1, 3]");
        assertTypes(new String[]{"Fixnum", "FalseClass"}, "#  teelikamenten => 77 or false");
        assertTypes(new String[]{"MyClass"}, "#  hissun kissun => MyClass");
        assertTypes(new String[]{"Hash"}, "#  vaapula vissun => 1, \"qux\" => 2 }     #=> {\"baz\"=>1, \"bar\"=>2}");

        assertTypes(new String[]{"Fixnum"}, "#  eelin keelin => -98");
        assertTypes(new String[]{"Float"}, "#  klot => -5.555");
        assertTypes(new String[]{"Float"}, "#  viipula => 5.555");
        assertTypes(new String[]{"Fixnum"}, "#  vaapula => -1, 0, +1");
    }

    public void testCollectTypesFromComment() {
        assertTypes("Array",
                "#     mod.ancestors -> array",
                "#",
                "#",
                "# Returns a list of modules included in <i>mod</i> (including",
                "# <i>mod</i> itself).",
                "# ...");

        assertTypes("NilClass", "  #     attr(symbol, writable=false)    => nil");

        assertTypes("Module", "  #     attr(symbol, writable=false)    => mod");

        assertTypes("Object",
                "  #     mod.class_eval(string [, filename [, lineno]])  => obj",
                "  #     mod.module_eval {|| block }                     => obj");

        assertTypes(new String[]{"TrueClass", "FalseClass"},
                "  #     obj.class_variable_defined?(symbol)    => true or false");

        assertTypes(new String[]{}, "  #     mod.freeze");
    }

    public void testCollectTypesFromCommentIgnoresNonTypesLines() {
        assertTypes("Array",
                "#     mod.ancestors -> array",
                "#",
                "#",
                "# Returns a list of modules included in <i>mod</i> (including",
                "# <i>mod</i> itself).",
                "#",
                "#    module Mod",
                "#      include Math",
                "#      include Comparable",
                "#    end",
                "#",
                "#    Mod.ancestors    #=> [Mod, Comparable, Math]",
                "#    Math.ancestors   #=> [Math]",
                "#",
                "#");
    }

    /** all_stubs.rb was generated with:
     * <pre>
     *   findf | grep stub_ | xargs cat >> ~/tmp/all_stubs.rb.comments
     *   gvim:
     *     g!/.*#  .* => .*&#47;d
     *     by macro convert each line to:
     *       #  stub => str
     *   cat ~/tmp/all_stubs.rb.comments | sort -k 12 | uniq > all_stubs.rb
     * </pre>
     */
    public void testAllStubs() throws Exception {
        checkTypesForComments("testfiles/all_stubs.rb.comments");
    }

    public void testGetStandardNameVariants() {
        String type = "my_type";
        List<String> result = RDocAnalyzer.getStandardNameVariants(type);
        assertEquals(9, result.size());
        assertTrue(result.contains("a_my_type"));
        assertTrue(result.contains("my_type"));
        assertTrue(result.contains("MyType"));
        assertTrue(result.contains("aMyType"));
    }

    public void testResolveType() {
        String type = "aString";
        assertEquals("String", RDocAnalyzer.resolveType(type));

        type = "string";
        assertEquals("String", RDocAnalyzer.resolveType(type));

        type = "a_string";
        assertEquals("String", RDocAnalyzer.resolveType(type));

        type = "String";
        assertEquals("String", RDocAnalyzer.resolveType(type));

        type = "anIdObject";
        assertEquals("IdObject", RDocAnalyzer.resolveType(type));

        type = "an_id_object";
        assertEquals("IdObject", RDocAnalyzer.resolveType(type));

        type = "id_object";
        assertEquals("IdObject", RDocAnalyzer.resolveType(type));

        type = "77";
        assertNull(RDocAnalyzer.resolveType(type));

        type = "";
        assertNull(RDocAnalyzer.resolveType(type));

        type = "&)(";
        assertNull(RDocAnalyzer.resolveType(type));
    }

    public void testReturnTypeAssertions() {
        assertTypes(new String[]{"Fixnum"}, "#:return:=> Fixnum");
        assertTypes(new String[]{"Fixnum", "String"}, "#:return:=> Fixnum or String");
        assertTypes(new String[]{"SomeClass", "Fixnum", "File"}, "#:return:=> SomeClass, -1 or File");
    }

    public void testParamTypeAssertions() {
        RDocAnalyzer.TypeForSymbol tfs = RDocAnalyzer.paramTypesFromTypeAssertion("#:arg:param1 => String");
        assertEquals(1, tfs.getTypes().size());
        assertEquals("param1", tfs.getName());
        assertEquals("String", tfs.getTypes().get(0));

        tfs = RDocAnalyzer.paramTypesFromTypeAssertion("#:arg: param2=>Fixnum  ");
        assertEquals(1, tfs.getTypes().size());
        assertEquals("param2", tfs.getName());
        assertEquals("Fixnum", tfs.getTypes().get(0));

        tfs = RDocAnalyzer.paramTypesFromTypeAssertion("#:arg: param2=>Fixnum, SomeClass");
        assertEquals(2, tfs.getTypes().size());
        assertEquals("param2", tfs.getName());
        assertEquals("Fixnum", tfs.getTypes().get(0));
        assertEquals("SomeClass", tfs.getTypes().get(1));

        tfs = RDocAnalyzer.paramTypesFromTypeAssertion("#:arg: param2 =>Hash or Foo");
        assertEquals(2, tfs.getTypes().size());
        assertEquals("param2", tfs.getName());
        assertEquals("Hash", tfs.getTypes().get(0));
        assertEquals("Foo", tfs.getTypes().get(1));

        // assert invalid entries not recognized
        tfs = RDocAnalyzer.paramTypesFromTypeAssertion("#arg param2 =>Hash or Foo");
        assertNull(tfs);
    }
}

