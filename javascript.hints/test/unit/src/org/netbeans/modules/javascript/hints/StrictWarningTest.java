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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.hints;

import org.netbeans.modules.gsf.api.HintSeverity;

/**
 *
 * @author tor
 */
public class StrictWarningTest extends HintTestBase {
    private String goldenfileSuffix;

    
    public StrictWarningTest(String testName) {
        super(testName);
    }            
    
    @Override
    protected String getGoldenFileSuffix() {
        return goldenfileSuffix;
    }

    public void testStrict() throws Exception {
        // Add builtin wrappers for strict warnings
        for (String key : StrictWarning.KNOWN_STRICT_ERROR_KEYS) {
            goldenfileSuffix = "." + key;
            StrictWarning rule = new StrictWarning(key);
            if (StrictWarning.RESERVED_KEYWORD.equals(key) || StrictWarning.TRAILING_COMMA.equals(key)) {
                rule.setDefaultSeverity(HintSeverity.ERROR);
            }
            checkHints(this, rule, "testfiles/prototype.js", null);
        }
    }
    
    //Uncomment to generate all the golden files over again
    //@Override
    //protected boolean failOnMissingGoldenFile() {
    //    return false;
    //}
    
    public void testReservedKeyword() throws Exception {
        goldenfileSuffix = "";
        checkHints(this, new StrictWarning(StrictWarning.RESERVED_KEYWORD), "testfiles/reserved.js", null);
    }

    public void testDebuggerKeyword() throws Exception {
        goldenfileSuffix = "";
        checkHints(this, new StrictWarning(StrictWarning.RESERVED_KEYWORD), "testfiles/debugger.js", null);
    }

    public void testNoFunctionSideEffects() throws Exception {
        goldenfileSuffix = "";
        checkHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/functions-sideeffects.js", null);
    }
    
    public void testSideEffects() throws Exception {
        goldenfileSuffix = "";
        // See 135144
        checkHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/sideeffects.js", null);
    }

    public void testSideEffects2() throws Exception {
        goldenfileSuffix = "";
        checkHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/generated.js", null);
    }

    public void testSideEffects3() throws Exception {
        goldenfileSuffix = "";
        // See 135144
        checkHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/effects_error.js", null);
    }

    public void testSideEffects4() throws Exception {
        goldenfileSuffix = "";
        // See 135144
        checkHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/assign.js", null);
    }

    // Test no false return warnings
    public void testReturnAnalysis() throws Exception {
        goldenfileSuffix = "";
        checkHints(this, new StrictWarning(StrictWarning.ANON_NO_RETURN_VALUE), "testfiles/returns.js", null);
    }
    
    public void testTrailingComma() throws Exception {
        goldenfileSuffix = "";
        checkHints(this, new StrictWarning(StrictWarning.TRAILING_COMMA), "testfiles/trailingcomma.js", null);
    }
    
    public void testFixTrailingComma() throws Exception {
        goldenfileSuffix = "";
        applyHint(this, new StrictWarning(StrictWarning.TRAILING_COMMA), "testfiles/trailingcomma.js", "600px\"^,", "Remove");
    }

    public void testFixSideEffects1() throws Exception {
        goldenfileSuffix = "";
        applyHint(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/assign.js", "ba^r1", "Assign");
    }

    public void testFixSideEffects2() throws Exception {
        goldenfileSuffix = "";
        applyHint(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/assign.js", "ba^r2", "Assign");
    }

    public void testFixSideEffects3() throws Exception {
        goldenfileSuffix = "";
        applyHint(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/assign.js", "ba^r3", "Assign");
    }

    public void testFixSideEffects4() throws Exception {
        goldenfileSuffix = "";
        applyHint(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/assign.js", "ba^r4", "Assign");
    }

    public void testFixSideEffects5() throws Exception {
        goldenfileSuffix = "";
        applyHint(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/assign.js", "^3+4", "Assign");
    }

    public void testFixSideEffects6() throws Exception {
        goldenfileSuffix = "";
        applyHint(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/assign.js", "ba^r2", "Return");
    }

//
//    @Override
//    protected void customizeHintError(Error error, int start) {
//        if (error.getParameters() != null && error.getParameters().length > 0 && error.getParameters()[0] instanceof Node) {
//            Node node = (Node) error.getParameters()[0];
//            // Tweak Node AST offsets as well
//            int nodeLength = node.getSourceEnd()-node.getSourceStart();
//            node.setSourceBounds(start, start+nodeLength);
//        }
//    }
}
