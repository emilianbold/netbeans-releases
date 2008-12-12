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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public final class RDocAnalyzerTest extends RubyTestBase {

    public RDocAnalyzerTest(String name) {
        super(name);
    }

    private void assertTypes(String expectedTypes, String... comment) {
        assertTypes(new String[]{expectedTypes}, comment);
    }

    private void assertTypes(String[] expectedTypes, String... comment) {
        Set<? extends String> actualTypes = RDocAnalyzer.collectTypesFromComment(Arrays.asList(comment));
        assertEquals("Got correct tyeps", Arrays.asList(expectedTypes), new ArrayList<String>(actualTypes));
    }

    private void testPrint(Set<? extends String> actualTypes, String[] comment) {
        System.out.println("assertTypes(\"" + actualTypes + "\", \"" + comment[0] + "\");");
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
            Set<? extends String> types = RDocAnalyzer.collectTypesFromComment(Collections.singletonList(line));
            sb.append(line + "\n  ==> " + types + "\n\n");
        }
        assertDescriptionMatches(commentRelFilePath, sb.toString(), false, ".types");
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

    public void testModuleStub() throws Exception {
        checkTypesForComments("testfiles/stub_module.rb.comments");
    }

    public void testStringStub() throws Exception {
        checkTypesForComments("testfiles/stub_string.rb.comments");
    }

    public void testFixnumStub() throws Exception {
        checkTypesForComments("testfiles/stub_fixnum.rb.comments");
    }

}

