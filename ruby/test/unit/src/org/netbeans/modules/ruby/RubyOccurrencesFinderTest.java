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

package org.netbeans.modules.ruby;

/**
 * Test the "mark occurrences" feature in Ruby
 *
 * @author Tor Norbye
 */
public class RubyOccurrencesFinderTest extends RubyTestBase {

    public RubyOccurrencesFinderTest(String testName) {
        super(testName);
    }

    public void testApeParams() throws Exception {
        String caretLine = "  def initialize(ar^gs)";
        checkOccurrences("testfiles/ape.rb", caretLine, true);
    }

    public void testApeMethodDef() throws Exception {
        String caretLine = "def te^st_entry_posts(entry_collection)";
        checkOccurrences("testfiles/ape.rb", caretLine, false);
    }

    public void testApeMethodRef() throws Exception {
        String caretLine = "test_entry_p^osts entry_coll";
        // Not symmetric because when going from the call to the method definition,
        // the occurrences listed will be all the exit points
        checkOccurrences("testfiles/ape.rb", caretLine, false);
    }

    public void testApeSymbol() throws Exception {
        String caretLine = "@@debugging = args[:de^bug]";
        checkOccurrences("testfiles/ape.rb", caretLine, true);
    }

    public void testApeClassVar() throws Exception {
        String caretLine = "@@deb^ugging = args[:debug]";
        checkOccurrences("testfiles/ape.rb", caretLine, true);
    }

    public void testApeInstanceVar() throws Exception {
        String caretLine = "@st^eps[-1] << message";
        checkOccurrences("testfiles/ape.rb", caretLine, true);
    }

    public void testApeExitPoints() throws Exception {
        String caretLine = "d^ef might_fail(uri, requested_e_coll = nil, requested_m_coll = nil)";
        checkOccurrences("testfiles/ape.rb", caretLine, false);
    }

    public void testUnusedExitPoints() throws Exception {
        String caretLine = "d^ef foo(unusedparam, unusedparam2, usedparam)";
        checkOccurrences("testfiles/unused.rb", caretLine, false);
    }

    public void testUnusedParams() throws Exception {
        String caretLine = "def foo(un^usedparam, unusedparam2, usedparam)";
        checkOccurrences("testfiles/unused.rb", caretLine, true);
    }

    public void testUnusedParams2() throws Exception {
        String caretLine = "def foo(unusedparam, unusedparam2, us^edparam)";
        checkOccurrences("testfiles/unused.rb", caretLine, true);
    }

    public void testUnusedParams3() throws Exception {
        String caretLine = "x.each { |unusedblockvar1, usedbl^ockvar2|";
        checkOccurrences("testfiles/unused.rb", caretLine, true);
    }

    public void testYieldNode() throws Exception {
        String caretLine = "def exit_t^est";
        checkOccurrences("testfiles/yieldnode.rb", caretLine, false);
    }

    public void testNestedBlocks() throws Exception {
        checkOccurrences("testfiles/nestedblocks.rb", "[4,5,6].each { |ou^ter|", true);
    }

    public void testNestedBlocks2() throws Exception {
        checkOccurrences("testfiles/nestedblocks2.rb", "arg.each_pair do |fo^o, val|", true);
    }

    public void testParellelBlocks() throws Exception {
        checkOccurrences("testfiles/parallelblocks.rb", "foo.each { |i^| puts i } #1", true);
    }

    // Doesn't work yet
    //public void testHereDoc() throws Exception {
    //    checkOccurrences("testfiles/postgresql_adapter.rb", "table_name, na^me = nil", true);
    //}
    
    public void testEmpty1() throws Exception {
        checkOccurrences("testfiles/empty.rb", "^", true);
    }
}
