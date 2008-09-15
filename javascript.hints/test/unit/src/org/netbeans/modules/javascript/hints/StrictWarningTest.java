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

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.mozilla.nb.javascript.Node;
import org.netbeans.modules.gsf.GsfTestCompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.HintSeverity;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.spi.DefaultError;

/**
 *
 * @author tor
 */
public class StrictWarningTest extends HintTestBase {
    private String goldenfileSuffix;
    enum ChangeOffsetType { NONE, OVERLAP, OUTSIDE };
    ChangeOffsetType changeOffsetType = ChangeOffsetType.NONE;

    
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

    public void testWrongOffsets() throws Exception {
        // Iterate over the various hints and set the offsets to invalid document positions to
        // check that the rules gracefully recover without throwing BadLocationExceptions etc
        // (This is necessary because during editing, if the user deletes a lot of text rapidly
        // such that when the rules are looking at AST offsets (containing offsets in the now
        // deleted text) and compares those to on-screen positions, the rules need to gracefully
        // handle nonexistent offsets)
        goldenfileSuffix = "";
        try {
            for (ChangeOffsetType type : new ChangeOffsetType[] { ChangeOffsetType.OUTSIDE, ChangeOffsetType.OVERLAP }) {
                changeOffsetType = type;
                getHints(this, new StrictWarning(StrictWarning.RESERVED_KEYWORD), "testfiles/reserved.js", null, null);
                getHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/sideeffects.js", null, null);
                getHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/functions-sideeffects.js", null, null);
                getHints(this, new StrictWarning(StrictWarning.NO_SIDE_EFFECTS), "testfiles/generated.js", null, null);
                getHints(this, new StrictWarning(StrictWarning.ANON_NO_RETURN_VALUE), "testfiles/returns.js", null, null);
                getHints(this, new StrictWarning(StrictWarning.TRAILING_COMMA), "testfiles/trailingcomma.js", null, null);

                for (String key : StrictWarning.KNOWN_STRICT_ERROR_KEYS) {
                    goldenfileSuffix = "." + key;
                    StrictWarning rule = new StrictWarning(key);
                    if (StrictWarning.RESERVED_KEYWORD.equals(key) || StrictWarning.TRAILING_COMMA.equals(key)) {
                        rule.setDefaultSeverity(HintSeverity.ERROR);
                    }
                    getHints(this, rule, "testfiles/prototype.js", null, null);
                }

                assertNull(StrictWarning.problem);
            }
        } finally {
            changeOffsetType = ChangeOffsetType.NONE;
        }
    }

    @Override
    protected void customizeHintInfo(GsfTestCompilationInfo info, ParserResult result) {
        if (changeOffsetType == ChangeOffsetType.NONE) {
            return;
        }
        if (info == null || result == null) {
            return;
        }
        // Test offset handling to make sure we can handle bogus node positions
        
        Document doc = info.getDocument();
        int docLength = doc.getLength();
        // Replace errors with offsets
        List<Error> errors = new ArrayList<Error>();
        List<Error> oldErrors = result.getDiagnostics();
        for (Error error : oldErrors) {
            int start = error.getStartPosition();
            int end = error.getEndPosition();

            // Modify document position to be off
            int length = end-start;
            if (changeOffsetType == ChangeOffsetType.OUTSIDE) {
                start = docLength+1;
            } else {
                start = docLength-1;
            }
            end = start+length;
            if (end <= docLength) {
                end = docLength+1;
            }

            Error newError = new DefaultError(error.getKey(), error.getDisplayName(), error.getDescription(), error.getFile(), start,
                    end, error.getSeverity());
            errors.add(newError);

            if (error.getParameters() != null && error.getParameters().length > 0 && error.getParameters()[0] instanceof Node) {
                Node node = (Node) error.getParameters()[0];
                // Tweak Node AST offsets as well
                int nodeLength = node.getSourceEnd()-node.getSourceStart();
                node.setSourceBounds(start, start+nodeLength);
            }
        }
        oldErrors.clear();
        oldErrors.addAll(errors);
    }
}
