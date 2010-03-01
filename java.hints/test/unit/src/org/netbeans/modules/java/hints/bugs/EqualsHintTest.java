/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.bugs;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.jackpot.code.spi.TestBase;
import org.netbeans.spi.editor.hints.Fix;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsHintTest extends TestBase {
    
    public EqualsHintTest(String testName) {
        super(testName, EqualsHint.class);
    }

    public void testSimpleAnalysis1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}",
                            "0:83-0:89:verifier:AE");
    }
    
    public void testSimpleAnalysis2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test{ public void test() {Class c = null; String s = null; boolean b = c.equals(s);}}",
                            "0:100-0:106:verifier:IE");
    }
    
    public void testSimpleAnalysis3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test{ public void test() {Class c = null; String s = null; boolean b = s.equals(c);}}",
                            "0:100-0:106:verifier:IE");
    }
    
    public void testSimpleAnalysis4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = o.equals(c);}}");
    }
    
    public void testSimpleAnalysis5() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = c.equals(o);}}");
    }
    
    public void testFix1() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}",
                       "0:83-0:89:verifier:AE",
                       "FIX_ReplaceWithArraysEquals",
                       "package test; import java.util.Arrays; public class Test{ public void test() {int[] a = null; boolean b = Arrays.equals(a, a);}}");
    }
    
    public void testFix2() throws Exception {
        performFixTest("test/Test.java",
                       "package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}",
                       "0:83-0:89:verifier:AE",
                       "FIX_ReplaceWithInstanceEquals",
                       "package test; public class Test{ public void test() {int[] a = null; boolean b = a == a;}}");
    }
    
    public void testAnalysis132853() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = this.equals(c, o);} private boolean equals(Object o1, Object o2) { return false; } }");
    }
    
    public void testUnresolved1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(aa);}}");
    }

    public void testNoThis() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; public class Test { public boolean test(Integer o) {return equals(o);}}",
                            "0:73-0:79:verifier:IE");
    }

    @Override
    protected String toDebugString(CompilationInfo info, Fix f) {
        return f.getText();
    }

}
