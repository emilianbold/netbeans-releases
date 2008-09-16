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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.hints.introduce;

import org.netbeans.modules.ruby.hints.HintTestBase;


/**
 *
 * @todo Test that a method with NO outputs doens't have a "return" statement
 * @todo Test having a block prior to the region with vars that name-alias!
 * @todo Perform some kind of automated text which applies this stuff to all kinds
 *  of code blocks in a large source file and checks that the result is safe
 * @todo Make sure that I can't have enpoints in different blocks
 * @todo Test that I skip past the comments in rdoc correctly!
 * @todo Test RHTML
 * @todo Test that if (x > 50) introduce constant doesn't move inside parenthesis
 * 
 * @author Tor Norbye
 */
public class IntroduceHintTest extends HintTestBase {
    
    public IntroduceHintTest(String testName) {
        super(testName);
    }            

    public void testHint1() throws Exception {
        checkHints(this, new IntroduceHint(), "testfiles/introduce1.rb", null);
    }

    public void testHintNoPartialExps() throws Exception {
        checkHints(new IntroduceHint(), "testfiles/introduce4.rb", "^ x = 51", "puts y^");
    }
    
    public void testIntroduceConstant() throws Exception {
        checkHints(this, new IntroduceHint(), "testfiles/introduce1.rb", "50+30^");
    }

    public void testIntroduceField() throws Exception {
        checkHints(this, new IntroduceHint(), "testfiles/introduce1.rb", "x+30^");
    }
    
    public void testExtractMethod() throws Exception {
        checkHints(new IntroduceHint(), "testfiles/introduce1.rb", "^good_symbol = 50", "x = 50^");
    }

    public void testExtractMethod8() throws Exception {
        // No extract method for loops involving returns, etc.
        checkHints(new IntroduceHint(), "testfiles/introduce8.rb", "^ny, = clfloor(y + 1, 1)", "return unless [ny, 1] == jd_to_ordinal(jd - d, ns)^");
    }
    
    public void testExtractMethod9() throws Exception {
        // No extract method for loops involving returns, etc.
        checkHints(new IntroduceHint(), "testfiles/introduce8.rb", "^loop do", "name = newname^");
    }
    
    public void testImbalancedExpr() throws Exception {
        checkHints(new IntroduceHint(), "testfiles/introduce4.rb", "^[10,11,12].each do |bar|", "end #block^");
    }

    public void testApplyExtractMethod() throws Exception {
        IntroduceHint hint = new IntroduceHint();
//        applyHint(this, hint, "testfiles/introduce1.rb",
//                "good_symbol = 50\n    x = 50^", "Extract Method...");
        applyHint(this, hint, "testfiles/introduce1.rb",
                "^good_symbol = 50", "x = 50^", "Extract Method...");
    }

    public void testApplyIntroduceConstant() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce1.rb",
                "50+30^", "Introduce Constant...");
    }

    public void testApplyIntroduceField() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce1.rb",
                "x+30^", "Introduce Field...");
    }
    
    public void testApplyIntroduceVariable() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce1.rb",
                "50+30^", "Introduce Variable...");
    }

    public void testApplyIntroduceVariable2() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce1.rb",
                "50+30^", "Introduce Variable...");
    }

    public void testApplyIntroduceVariableHash() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce9.rb",
                "^:foo => :bar", ":foo => :bar^", "Introduce Variable...");
    }

    public void testApplyExtractMethod2() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce2.rb", "^newvar = 50", "usedlater = 30^", 
                "Extract Method...");
    }

    public void testApplyExtractMethod3() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce3.rb", "^newvar = 50", "usedlater = 30^", 
                "Extract Method...");
    }

    public void testApplyExtractMethod4() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce4.rb", "^ x = 51", "puts y+q^", 
                "Extract Method...");
    }

    public void testApplyExtractMethod4b() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce4.rb", "^[1,2,3].each do |foo|", "end #block^", 
                "Extract Method...");
    }

    public void testApplyExtractMethod4d() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce4.rb", "^x = x+y", "x = x+y^", 
                "Extract Method...");
    }

    public void testApplyExtractMethod5() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce5.rb", "^[1,2,3].each do |foo|", "end^ #block", 
                "Extract Method...");
    }

    public void testApplyExtractMethod5range() throws Exception {
        // This is the same as testApplyExtractMethod5 but I've extended the range to the next line
        // to test that it collapses whitespace correctly
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce5.rb", "^[1,2,3].each do |foo|", "^puts faen", 
                "Extract Method...");
    }

    public void testApplyExtractMethod6() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce6.rb", "^[1,2,3].each do |foo|", "end^ #block", 
                "Extract Method...");
    }

    public void testApplyExtractMethod7() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce7.rb", "^a = (y / 100.0).floor", "^ if os?(jd, sg)", 
                "Extract Method...");
    }

    public void testApplyExtractMethodMultiAssign() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce10.rb", "^y = 10", "z = 20^", 
                "Extract Method...");
    }

    public void testApplyExtractMethodConditionals1() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce11.rb", "^if (b > 4)", "c += h^", 
                "Extract Method...");
    }

    public void testApplyExtractMethodConditionals2() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce11.rb", "^if (a < 5)", "c += a^", 
                "Extract Method...");
    }
    
//    public void testApplyIntroduceVariableNewlines1() throws Exception {
//        IntroduceHint hint = new IntroduceHint();
//        applyHint(this, hint, "testfiles/introduce12.rb",
//                "case (^x)", "case (x^)", "Introduce Variable...");
//    }

    public void testApplyIntroduceVariableNewlines2() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce12.rb",
                "call( (^x<y) && true", "call( (x<y^) && true", "Introduce Variable...");
    }

    public void testApplyIntroduceVariableNewlines3() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce12.rb",
                "if (^x < y)", "if (x < y^)", "Introduce Variable...");
    }

    public void testApplyIntroduceVariableNewlines4() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce12.rb",
                "^y+1", "y+1^", "Introduce Variable...");
    }

    public void testApplyIntroduceVariableNewlines5() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce12.rb",
                "(^1+y)", "(1+y^)", "Introduce Variable...");
    }

    public void testApplyIntroduceHintNoMethod1() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce13.rb",
                "^a = 1", "b = 2^", "Extract Method...");
    }

    public void testApplyIntroduceHintNoMethod2() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce13.rb",
                "^some_call1 do", "end # some_call1^", "Extract Method...");
    }

    public void testApplyIntroduceHintNoMethod3() throws Exception {
        IntroduceHint hint = new IntroduceHint();
        applyHint(this, hint, "testfiles/introduce13.rb",
                "^c = 1", "d = 2^", "Extract Method...");
    }

// This test case doesn't work right.  Check it and fix it!    
//    public void testWrongIntroduce() throws Exception {
//        IntroduceHint hint = new IntroduceHint();
//        applyHint(this, hint, "testfiles/wrong-extract-method.rb",
//                "^attr_accessor :x", "helper :all^", "Extract Method...");
//    }
}
