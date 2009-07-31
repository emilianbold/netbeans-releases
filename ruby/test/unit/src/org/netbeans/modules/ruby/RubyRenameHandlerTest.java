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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby;

/**
 *
 * @author Tor Norbye
 */
public class RubyRenameHandlerTest extends RubyTestBase {
    
    public RubyRenameHandlerTest(String testName) {
        super(testName);
    }

    public void testRename1() throws Exception {
        checkRenameSections("testfiles/postgresql_adapter.rb", "  def indexes(tabl^e_name, name = nil) #:nodoc:");
    }

    public void testRename2() throws Exception {
        checkRenameSections("testfiles/postgresql_adapter.rb", "  def indexes(table_name, na^me = nil) #:nodoc:");
    }

    public void testRename3() throws Exception {
        checkRenameSections("testfiles/ape.rb", "      step[1 .. -1].each { |l^i| report_li(nil, nil, li) }");
    }

    public void testRename3b() throws Exception {
        checkRenameSections("testfiles/ape.rb", "      step[1 .. -1].each { |li| report_li(nil, nil, l^i) }");
    }

    public void testRename4() throws Exception {
        checkRenameSections("testfiles/resolv.rb", "    def each_address(name, &p^roc)");
    }

    public void testRename5() throws Exception {
        checkRenameSections("testfiles/resolv.rb", "        @name2addr[name].each(&p^roc)");
    }

    public void testRename6() throws Exception {
        checkRenameSections("testfiles/resolv.rb", "              add^r, hostname, *aliases = line.split(/\\s+/)");
    }

    public void testRename7() throws Exception {
        checkRenameSections("testfiles/nestedblocks.rb", "[4,5,6].each { |ou^ter|");
    }

    public void testRename8() throws Exception {
        checkRenameSections("testfiles/parallelblocks.rb", "foo.each { |i^| puts i } #1");
    }

    public void testRename9() throws Exception {
        checkRenameSections("testfiles/nestedblocks2.rb", "arg.each_pair do |fo^o, val|");
    }

    public void testEmpty1() throws Exception {
        checkRenameSections("testfiles/empty.rb", "^");
    }

    public void testRenameDefaultArgs() throws Exception {
        // Issue 141872
        checkRenameSections("testfiles/rename1.rb", "c^");
    }


    public void testIssue155028() throws Exception {
        // test that renaming works with also other occurences than
        // assigments (for local variables)
        checkRenameSections("testfiles/rename_iz155028.rb", "acnst^rs.delete(String)");
    }

    public void testIssue155028_2() throws Exception {
        checkRenameSections("testfiles/rename_iz155028.rb", "#{acns^trs.join");
    }

    public void testIssue155028_3() throws Exception {
        checkRenameSections("testfiles/rename_iz155028.rb", "span patt^ern");
    }
}
