/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
        super.performTest("file.cc", 5, 1, "::");
    } 
    
    public void testGlobalCompletionInClassFunction() throws Exception {
        super.performTest("file.cc", 7, 1, "::");
    }  

    public void testGlobalCompletionInGlobFunction() throws Exception {
        super.performTest("file.cc", 19, 1, "::");
    }  
    
//    public void testCompletionInConstructor() throws Exception {
//        super.performTest("file.h", 20, 9);
//    }
    
    public void testProtectedMethodByClassPrefix() throws Exception {
        super.performTest("file.h", 23, 9, "B::");
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

    public void testCompletionInEmptyUsrInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include \"\"", -1);
    }

    public void testCompletionInEmptySysInclude() throws Exception {
        super.performTest("file.cc", 1, 1, "#include <>", -1);
    }
}
