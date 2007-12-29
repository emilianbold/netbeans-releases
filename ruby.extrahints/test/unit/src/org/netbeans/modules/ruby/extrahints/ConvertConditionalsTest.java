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

package org.netbeans.modules.ruby.extrahints;

import org.netbeans.modules.ruby.hints.HintTestBase;

public class ConvertConditionalsTest extends HintTestBase {
    
    public ConvertConditionalsTest(String testName) {
        super(testName);
    }            

    public void testNoHint1() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", null);
    }

    public void testNoHint2() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals2.rb", "uri = env_pr^oxy ? URI.parse(env_proxy) : nil");
    }

    public void testNoHint3() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals2.rb", "(uri =~ /.(https?|ftp|file):/) ? u^ri : \"http://#{uri}\"");
    }
    
    public void testNoHint4() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/emptycondition.rb", "if (^)");
    }
    
    public void testNoHint5() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals3.rb", "if (^x > 5)");
    }
    
    public void testNoHint6() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals3.rb", "i^f jd >= commercial_to_jd");
    }
    
    public void testNoHint7() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/elsif.rb", 
                "elsi^f !str.blank?");
    }

    public void testNoHint8() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/elsif.rb", 
                "elsif !str.bla^nk?");
    }

    public void testNoHint9() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/elsif.rb", 
                "if st^r != 'something'");
    }

    public void testNoHint10() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/emptybody.rb", 
                "if !^x");
    }

    public void testNoHint11() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/emptybody.rb", 
                "if !^y");
    }

    public void testHint2() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "if tr^ue");
    }

    public void testHint3() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "i^f x < 5");
    }
    
    public void testHint4() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "i^f (x < 6)");
    }

    public void testHint5() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "i^f !x");
    }

    public void testHint6() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "i^f x < 8");
    }

    public void testHint7() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "i^f x < 9");
    }
    
    public void testHint8() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "^x < 11");
    }
    
    public void testHint9() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "unl^ess x < 5");
    }
    
    public void testHint10() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "un^less (x < 6)");
    }
    
    public void testHint11() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "^x > 12");
    }
    
    public void testHint12() throws Exception {
        findHints(this, new ConvertConditionals(), "testfiles/conditionals.rb", "i^f x < 12");
    }
    
    public void testFix1() throws Exception {
        applyHint(this, new ConvertConditionals(), "testfiles/conditionals.rb", 
                "if tr^ue", "Change statement into a modifier");
    }
    
    public void testFix2() throws Exception {
        applyHint(this, new ConvertConditionals(), "testfiles/conditionals.rb", 
                "i^f x < 5", "Change statement into a modifier");
    }

    public void testFix3() throws Exception {
        applyHint(this, new ConvertConditionals(), "testfiles/conditionals.rb", 
                "i^f (x < 6)", "Change statement into a modifier");
    }

    public void testFix4() throws Exception {
        applyHint(this, new ConvertConditionals(), "testfiles/conditionals.rb", 
                "i^f !x", "Change statement into a modifier");
    }

    public void testFix5() throws Exception {
        applyHint(this, new ConvertConditionals(), "testfiles/conditionals.rb", 
                "unl^ess x < 5", "Change statement into a modifier");
    }

    public void testFix6() throws Exception {
        applyHint(this, new ConvertConditionals(), "testfiles/conditionals.rb", 
                "i^f x < 12", "Change statement into a modifier");
    }

    public void testFixPosError() throws Exception {
        applyHint(this, new ConvertConditionals(), "testfiles/position_errors.rb", 
                "unl^ess !(x < 5)", "Change statement into a modifier");
    }
}
