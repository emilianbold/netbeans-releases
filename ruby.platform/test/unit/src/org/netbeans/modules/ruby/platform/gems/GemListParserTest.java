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
package org.netbeans.modules.ruby.platform.gems;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.ruby.platform.RubyTestBase;

public class GemListParserTest extends RubyTestBase {

    public GemListParserTest(String testName) {
        super(testName);
    }

    public void testParseWithDescritions() throws IOException {
        String output = slurp(getGoldenFile());
        List<String> outputL = Arrays.asList(output.split("\\n"));
        List<Gem> local = GemListParser.parseLocal(outputL);
        assertSame("local parsed", 5, local.size());
    }

    public void testParseNoDescritions() throws IOException {
        String output = slurp(getGoldenFile());
        List<String> outputL = Arrays.asList(output.split("\\n"));
        List<Gem> local = GemListParser.parseLocal(outputL);
        assertSame("local parsed", 5, local.size());
    }

    public void testParseNoDescritionsAllVersions() throws IOException {
        String output = slurp(getGoldenFile());
        List<String> outputL = Arrays.asList(output.split("\\n"));
        List<Gem> local = GemListParser.parseLocal(outputL);
        assertSame("local parsed", 5, local.size());
        for (Gem gem : local) {
            if (!gem.getName().equals("sources")) {
                assertTrue(gem.getName() + " gem has more version", gem.getInstalledVersionsAsString().contains(","));
            }
        }
    }

    public void testParseWithoutToken() throws IOException {
        String output = slurp(getGoldenFile());
        List<String> outputL = Arrays.asList(output.split("\\n"));
        List<Gem> gems = GemListParser.parseLocal(outputL);
        assertSame("local parsed", 5, gems.size());
    }

    public void testParseDescription() throws IOException {
        String output = slurp(getGoldenFile());
        List<String> outputL = Arrays.asList(output.split("\\n"));
        List<Gem> gems = GemListParser.parseLocal(outputL);
        assertSame("local parsed", 1, gems.size());
        String expected = "Author: Ryan Davis\n" +
                "Rubyforge: http://rubyforge.org/projects/parsetree\n" +
                "Homepage: http://rubyforge.org/projects/parsetree/\n" +
                "\n" +
                "ParseTree is a C extension (using RubyInline) that extracts the\n" +
                "parse tree for an entire class or a specific method and returns it\n" +
                "as a s-expression (aka sexp) using ruby's arrays, strings, symbols,\n" +
                "and integers";
        assertEquals("right desctiption", expected, gems.get(0).getDescription());
    }
}
