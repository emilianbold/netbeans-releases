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

package org.netbeans.modules.ruby.hints;

/**
 * @author Tor Norbye
 */
public class ConvertBlockTypeTest extends HintTestBase {

    public ConvertBlockTypeTest(String testName) {
        super(testName);
    }

    public void testHint1() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks.rb", null);
    }

    public void testHint2() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks.rb", "x.each { ^|foo|");
    }

    public void testHint3() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks.rb", "x.each do ^|foo|");
    }

    public void testHint4() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks.rb", "for i in 1..^10");
    }

    public void testHint5() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks.rb", "pu^ts foo1");
    }
    
    public void testHint7() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks4.rb", "@scanner.set_prompt d^o");
    }

    public void testHint8() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks4.rb", "@context1.instance_eval d^o");
    }

    public void testHint9() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks4.rb", "@context4.instance_eval d^o");
    }

    public void testHint10() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks4.rb", "@context2.instance_eval {^");
    }
    
    public void testHint11() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks4.rb", "@context3.instance_eval {^");
    }
    
    public void testFix1() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks.rb", "x.each {^ |foo|", "Convert {}-block to a do/end-block\n");
    }

    public void testFix2() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks.rb", "x.each do^ |foo|", "Convert do/end-block to a {}-block\n");
    }

    public void testHint6() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks2.rb", "x.each {^ |foo| ");
    }

    public void testFix3() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks2.rb", "x.each {^ |foo| ", "Convert {}-block to a do/end-block\n");
    }

    public void testFix4() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks.rb", "x.each do^ |foo|", "Convert do/end-block to a {}-block, and collapse to a single line");
    }

    public void testOneLineHint1() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", null);
    }
    public void testOneLineHint2() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "self.gsub!(pattern) do^ |c| h[c] end");
    }
    public void testOneLineHint3() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "self.gsub!(pattern) {^ |c| h[c] }");
    }
    public void testOneLineHint4() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "sort{^|a1, a2| a1[0].id2name <=> a2[0].id2name}");
    }
    public void testOneLineHint5() throws Exception {
        findHints(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "sort do^|a1, a2| a1[0].id2name <=> a2[0].id2name end");
    }

    public void testOneLineHintFix2() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "self.gsub!(pattern) do^ |c| h[c] end", "Convert do/end-block to");
    }
    public void testOneLineHintFix3() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "self.gsub!(pattern) {^ |c| h[c] }", "Convert {}-block to a do/end-block, and expand to multiple lines");
    }
    public void testOneLineHintFix4() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "sort{^|a1, a2| a1[0].id2name <=> a2[0].id2name}", "Convert {}-block to a do/end-block, and expand to multiple lines");
    }
    public void testOneLineHintFix5() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks3.rb", "sort do^|a1, a2| a1[0].id2name <=> a2[0].id2name end", "Convert do/end-block to");
    }

    public void testHintFix1() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context1.instance_eval d^o",
                "Convert do/end-block to a {}-block, and collapse to a single line");
    }

    public void testHintFix2() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context1.instance_eval d^o",
                "Convert do/end-block to a {}-block\n");
    }

    public void testHintFix3() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context1.instance_eval d^o",
                "Collapse multi-line block to a single line");
    }
    
    public void testHintFix4() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context4.instance_eval d^o",
                "Convert do/end-block to a {}-block\n");
    }

    public void testHintFix5() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context4.instance_eval d^o",
                "Expand single-line block to multiple lines");
    }
    
    public void testHintFix6() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context2.instance_eval {^",
                "Convert {}-block to a do/end-block\n");
    }

    public void testHintFix7() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context2.instance_eval {^",
                "Collapse multi-line block to a single line");
    }
    
    public void testHintFix8() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context3.instance_eval {^",
                "Convert {}-block to a do/end-block, and expand to multiple lines");
    }

    public void testHintFix9() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context3.instance_eval {^",
                "Convert {}-block to a do/end-block\n");
    }

    public void testHintFix10() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks4.rb",
                "@context3.instance_eval {^",
                "Expand single-line block to multiple lines");
    }

    public void testHintFix11() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks5.rb",
                "[\"a\",\"b\",\"c\"].each do^ |word|",
                "Convert do/end-block to a {}-block, and collapse to a single line");
    }

    public void testHintFix12() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks5.rb",
                "[1,2,4].each {^ |number| double = 2*number; tripple = 3*number; puts double,tripple }",
                "Convert {}-block to a do/end-block, and expand to multiple lines");
    }

    public void testHintFix13() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks6.rb",
                "foo do^",
                "Convert do/end-block to a {}-block, and collapse to a single line");
    }

    public void testHintFix14() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks6.rb",
                "foo do^",
                "Collapse multi-line block to a single line");
    }

    public void testHintFix15() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks7.rb",
                "b.create_menu :name => 'default_menu' do ^|d|",
                //"b.create_menu :name => 'default_menu' do |d|^",
                "Convert do/end-block to a {}-block\n");
    }

    public void testHintFix16() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks8.rb",
                "args.each {^ |a| puts \"#{a}\\n\" }",
                "Convert {}-block to a do/end-block, and expand to multiple lines");
    }

    public void testHintFix17() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks8.rb",
                "ss = vv.collect{^|kkk, vvv| \":#{kkk.id2name}=>#{vvv.inspect}\"}",
                "Convert {}-block to a do/end-block, and expand to multiple lines");
    }

    public void testHintFix18() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks9.rb",
                "create_table :posts d^o |t|",
                "Convert do/end-block to a {}-block, and collapse to a single line");
    }

    public void testHintFix19() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks10.rb",
                "{^|x| sleep 0.5; yield x}",
                "Expand single-line block to multiple lines");
    }

    public void testHintFix20() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks5.rb",
                "en^d",
                "Convert do/end-block to a {}-block, and collapse to a single line");
    }

    public void testHintFix21() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks6.rb",
                "en^d",
                "Convert do/end-block to a {}-block, and collapse to a single line");
    }

    public void testHintFix22() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks.rb",
                "^}",
                "Convert {}-block to a do/end-block\n");
    }

    public void testHintFix23() throws Exception {
        applyHint(this, new ConvertBlockType(), "testfiles/convertblocks11.rb",
                "x.each do^ |foo|",
                "Convert do/end-block to a {}-block, and collapse to a single line");
    }
}
