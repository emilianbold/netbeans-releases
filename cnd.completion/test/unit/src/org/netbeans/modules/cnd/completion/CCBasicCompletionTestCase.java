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

package org.netbeans.modules.cnd.completion;

import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CCBasicCompletionTestCase extends CompletionBaseTestCase {

    /**
     * Creates a new instance of CCBasicCompletionTestCase
     */
    public CCBasicCompletionTestCase(String testName) {
        super(testName, true);
    }

    public void test142903_1() throws Exception {
        // IZ#142903: Code completion does not work immediately after "{" or "}"
        super.performTest("file.cc", 44, 35);
    }
    
    public void test142903_2() throws Exception {
        // IZ#142903: Code completion does not work immediately after "{" or "}"
        super.performTest("file.cc", 46, 6);
    }

    public void testIZ109010() throws Exception {
        // IZ#109010: Code completion listbox doesn't appear after "flag ? static_cast<int>(remainder) :" expression
        super.performTest("file.cc", 45, 54);
    }
    
    public void testIZ131568() throws Exception {
        // IZ#131568: Completion doubles some static functions
        super.performTest("iz131568.cc", 4, 5, "Re");
    }

    public void testIZ131283() throws Exception {
        // IZ#131283: Code Completion works wrongly in some casses
        super.performTest("file.h", 28, 9, "st");
    }

    public void testIZ119041() throws Exception {
        super.performTest("file.cc", 9, 5, "str[]", -1);
    }

    public void testNoAbbrevInCompletion() throws Exception {
        super.performTest("file.cc", 43, 5, "a.f");
    }

    public void testEmptyDerefCompletion() throws Exception {
        super.performTest("file.cc", 39, 5, " &");
        super.performTest("file.cc", 39, 5, "pointer = &");
        super.performTest("file.cc", 39, 5, "foo(&)", -1);
    }

    public void testEmptyPtrCompletion() throws Exception {
        super.performTest("file.cc", 39, 5, "*");
        super.performTest("file.cc", 39, 5, "pointer = *");
        super.performTest("file.cc", 39, 5, "foo(*)", -1);
    }

    public void testCompletionInEmptyFile() throws Exception {
        super.performTest("empty.cc", 1,1);
    }

    public void testRecoveryBeforeFoo() throws Exception {
        super.performTest("file.cc", 43, 5, "a.");
    }

    public void testExtraDeclarationOnTypeInsideFun() throws Exception {
        super.performTest("file.cc", 39, 5, "p");
    }

    public void testSwitchCaseVarsInCompound() throws Exception {
        super.performTest("file.cc", 24, 13);
    }

    public void testSwitchCaseVarsNotIncCompound() throws Exception {
        super.performTest("file.cc", 28, 13);
    }

    public void testSwitchCaseVarsAfterCompoundAndNotCompoundInDefault() throws Exception {
        super.performTest("file.cc", 32, 13);
    }

    public void testCompletionOnEmptyInGlobal() throws Exception {
        super.performTest("file.cc", 1, 1);
    }

    public void testCompletionOnEmptyInClassFunction() throws Exception {
        super.performTest("file.cc", 7, 1);
    }

    public void testCompletionOnEmptyInGlobFunction() throws Exception {
        super.performTest("file.cc", 19, 1);
    }

    public void testCompletionInsideInclude() throws Exception {
        // IZ#98530]  Completion list appears in #include directive
        super.performTest("file.cc", 2, 11); // completion inside #include "file.c"
    }

    public void testCompletionInsideString() throws Exception {
        // no completion inside string
        super.performTest("file.cc", 8, 18); // completion inside strings
    }

    public void testCompletionInsideChar() throws Exception {
        // no completion inside char literal
        super.performTest("file.cc", 14, 15); // completion inside chars
    }

    public void testGlobalCompletionInGlobal() throws Exception {
        super.performTest("file.cc", 47, 1, "::");
    }

    public void testGlobalCompletionInClassFunction() throws Exception {
        super.performTest("file.cc", 7, 1, "::");
    }

    public void testGlobalCompletionInGlobFunction() throws Exception {
        super.performTest("file.cc", 19, 1, "::");
    }

    public void testCompletionInConstructor() throws Exception {
        super.performTest("file.h", 20, 9);
    }

    public void testProtectedMethodByClassPrefix() throws Exception {
        super.performTest("file.h", 23, 9, "B::");
    }

    public void testGlobalMethodByScopePrefix() throws Exception {
        super.performTest("file.cc", 9, 26);
    }
    ////////////////////////////////////////////////////////////////////////////
    // tests for incomplete or incorrect constructions

    public void testErrorCompletion1() throws Exception {
        super.performTest("file.cc", 5, 1, "->");
    }

    public void testErrorCompletion2() throws Exception {
        super.performTest("file.cc", 5, 1, ".");
    }

    public void testErrorCompletion3() throws Exception {
        super.performTest("file.cc", 5, 1, ".->");
    }

    public void testErrorCompletion4() throws Exception {
        super.performTest("file.cc", 5, 1, "::.");
    }

    public void testErrorCompletion5() throws Exception {
        super.performTest("file.cc", 5, 1, "*:");
    }

    public void testErrorCompletion6() throws Exception {
        super.performTest("file.cc", 5, 1, ":");
    }

    public void testErrorCompletion7() throws Exception {
        super.performTest("file.cc", 5, 1, "->");
    }

    public void testErrorCompletion8() throws Exception {
        super.performTest("file.cc", 5, 1, "#inc");
    }

    public void testErrorCompletion9() throws Exception {
        super.performTest("file.cc", 5, 1, "#");
    }

    public void testErrorCompletion10() throws Exception {
        // IZ#77774: Code completion list appears, when include header file
        super.performTest("file.cc", 1, 1, "#include \"file.");
    }

    public void testErrorCompletionInFun1() throws Exception {
        super.performTest("file.cc", 7, 1, "->");
    }

    public void testErrorCompletionInFun2() throws Exception {
        super.performTest("file.cc", 7, 1, ".");
    }

    public void testErrorCompletionInFun3() throws Exception {
        super.performTest("file.cc", 7, 1, ".->");
    }

    public void testErrorCompletionInFun4() throws Exception {
        super.performTest("file.cc", 7, 1, "::.");
    }

    public void testErrorCompletionInFun5() throws Exception {
        super.performTest("file.cc", 7, 1, "*:");
    }

    public void testErrorCompletionInFun6() throws Exception {
        super.performTest("file.cc", 7, 1, ":");
    }

    public void testErrorCompletionInFun7() throws Exception {
        super.performTest("file.cc", 7, 1, "->");
    }

    public  void testCompletionAfterSpace() throws Exception {
        super.performTest("file.cc", 7, 5, "A::                 f");
    }

    public  void testCompletionAfterSpace2() throws Exception {
        super.performTest("file.cc", 43, 5, "a    .     ");
    }

    public void testCompletionInEmptyUsrInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include \"\"", -1);
    }

    public void testCompletionInEmptySysInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include <>", -1);
    }

    ////////////////////////////////////////////////////////////////////////////
    // tests for static function completion
    // IZ#126622 : Static function is missed in Code Completion listbox

    public void testCompletionForStaticFunctions1() throws Exception {
        super.performTest("static.cc", 18, 5);
    }

    public void testCompletionForStaticFunctions2() throws Exception {
        super.performTest("static.cc", 18, 5, "func");
    }

    public void testCompletionForStaticFunctions3() throws Exception {
        super.performTest("static.cc", 18, 5, "b");
    }

    public void testCompletionForStaticFunctions4() throws Exception {
        super.performTest("static.cc", 23, 1);
    }

    public void testRestrictPointers1() throws Exception {
        super.performTest("restrict.cc", 15, 5);
    }

    public void testRestrictPointers2() throws Exception {
        super.performTest("restrict.c", 15, 5);
    }

    public void testLocalEnumerators() throws Exception {
        super.performTest("local_enumerators.cc", 4, 14);
    }

    ////////////////////////////////////////////////////////////////////////////
    // tests for cast completion
    // IZ#92198 : Code completion works wrong with static_cast expression
    public void testCast1() throws Exception {
        super.performTest("cast.cc", 9, 23);
    }

    public void testCast2() throws Exception {
        super.performTest("cast.cc", 10, 34);
    }

    public void testCast3() throws Exception {
        super.performTest("cast.cc", 19, 21);
    }

    public void testCast4() throws Exception {
        super.performTest("cast.cc", 20, 33);
    }

    // IZ#148011 : Unable to resolve identifier when strings are involved
    public void testStringLiteral() throws Exception {
        super.performTest("iz148011.cc", 12, 25);
    }

    public void testIZ159423() throws Exception {
        // IZ#159423: Code completion doesn't display macros
        performTest("check_macro.cpp", 11, 16);
        performTest("check_macro.cpp", 11, 17);
    }

    public void testIZ159424() throws Exception {
        // IZ#159423: Code Completion works wrongly in macros
        performTest("check_macro.cpp", 13, 14);
        performTest("check_macro.cpp", 13, 18, ".v");
    }

    public void testIZ166620() throws Exception {
        // IZ#166620 : Code comlpetion does not show local variable inside for operator
        performTest("iz166620.cc", 6, 24);
    }
}
