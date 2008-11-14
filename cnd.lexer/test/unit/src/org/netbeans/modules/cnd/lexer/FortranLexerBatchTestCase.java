/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.cnd.lexer;

import junit.framework.TestCase;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.lib.lexer.test.LexerTestUtilities;

/**
 * Test several lexer inputs.
 *
 * @author Nick Krasilnikov
 */
public class FortranLexerBatchTestCase extends TestCase {

    public FortranLexerBatchTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws java.lang.Exception {
        // Set-up testing environment
        LexerTestUtilities.setTesting(true);
    }

    @Override
    protected void tearDown() throws java.lang.Exception {
    }

    protected InputAttributes getLexerAttributes() {
        InputAttributes lexerAttrs = new InputAttributes();
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_MAXIMUM_TEXT_WIDTH, 132, true);
        lexerAttrs.setValue(FortranTokenId.languageFortran(), CndLexerUtilities.FORTRAN_FREE_FORMAT, false, true);
        return lexerAttrs;
    }

    public void testAloneBackSlash() {
        String text = "\n";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        assertFalse("No more tokens", ts.moveNext());
    }

    public void testComments() {
        String text = "!abc\ncabc";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.LINE_COMMENT_FREE, "!abc");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.LINE_COMMENT_FIXED, "cabc");
    }

    public void testIdentifiers() {
        String text = "a ab aB2 2a x\nyZ\r\nz";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "ab");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "aB2");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "2");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "a");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "x");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "yZ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, "\r");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "z");
    }

    public void testApostropheChar() {
        String text = "id'";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "id");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.APOSTROPHE_CHAR, "'");
    }

    public void testStringLiterals() {
        String text = "\"\" \"a\"\"\" \"\\\"\" \"\\\\\" \"\\\\\\\"\" \"\\n\" \"a";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"a\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\\\\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\\\\\\"\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"\\n\"");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.STRING_LITERAL, "\"a");
    }

    public void testNumberLiterals() {
        String text = "0 00 09 1 12" +
                " o'7' b'101' z'A1' 1.1 1e1 1d1";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "0");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "00");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "09");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_INT, "12");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_OCTAL, "o'7'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_BINARY, "b'101'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_HEX, "z'A1'");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1.1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1e1");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NUM_LITERAL_REAL, "1d1");
    }

    public void testOperators() {
        String text = "** * / + - // == /= < <= > >=";
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_POWER, "**");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_MUL, "*");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_DIV, "/");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_PLUS, "+");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_MINUS, "-");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_CONCAT, "//");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LOG_EQ, "==");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_NOT_EQ, "/=");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LT, "<");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_LT_EQ, "<=");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_GT, ">");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.OP_GT_EQ, ">=");
        assertFalse("No more tokens", ts.moveNext());
    }

    private String getAllKeywords() {
        return "access action advance allocatable allocate apostrophe assignment backspace\n" +
                "blank block blockdata call case character close common complex contains\n" +
                " continue cycle data deallocate default delim dimension direct do double\n" +
                "doubleprecision elemental else elseif elsewhere end endassociate endblock\n" +
                "endblockdata enddo endenum end endfile endforall endfunction endif\n" +
                "endinterface endmap endmodule endprogram endselect endstructure endsubroutine\n" +
                "endtype endunion endwhere entry eor equivalance err exist exit external\n" +
                "file file forall form format formatted function go goto if implicit in\n" +
                "include inout inquire integer intent interface intrinsic iostat kind len\n" +
                "logical map module name named namelist nextrec nml none nullify number\n" +
                "only open opened operator optional out pad parameter pointer position precision\n" +
                "print private procedure program public pure quote read read\n" +
                "readwrite real rec recl recursive result return rewind save select selectcase\n" +
                "selecttype sequence sequential size size stat status stop structure\n" +
                "subroutine target then to type unformatted union use where while write write";
    }

    public void testKeywords() {
        String text = getAllKeywords();
        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ACCESS_EQ, "access");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ACTION_EQ, "action");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ADVANCE_EQ, "advance");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ALLOCATABLE, "allocatable");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ALLOCATE, "allocate");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_APOSTROPHE, "apostrophe");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ASSIGNMENT, "assignment");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_BACKSPACE, "backspace");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_BLANK_EQ, "blank");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_BLOCK, "block");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_BLOCKDATA, "blockdata");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_CALL, "call");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_CASE, "case");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_CHARACTER, "character");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_CLOSE, "close");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_COMMON, "common");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_COMPLEX, "complex");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_CONTAINS, "contains");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_CONTINUE, "continue");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_CYCLE, "cycle");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DATA, "data");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DEALLOCATE, "deallocate");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DEFAULT, "default");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DELIM_EQ, "delim");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DIMENSION, "dimension");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DIRECT_EQ, "direct");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DO, "do");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DOUBLE, "double");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_DOUBLEPRECISION, "doubleprecision");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ELEMENTAL, "elemental");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ELSE, "else");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ELSEIF, "elseif");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ELSEWHERE, "elsewhere");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_END_EQ, "end");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDASSOCIATE, "endassociate");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDBLOCK, "endblock");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDBLOCKDATA, "endblockdata");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDDO, "enddo");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDENUM, "endenum");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_END_EQ, "end");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDFILE, "endfile");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDFORALL, "endforall");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDFUNCTION, "endfunction");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDIF, "endif");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDINTERFACE, "endinterface");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDMAP, "endmap");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDMODULE, "endmodule");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDPROGRAM, "endprogram");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDSELECT, "endselect");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDSTRUCTURE, "endstructure");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDSUBROUTINE, "endsubroutine");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDTYPE, "endtype");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDUNION, "endunion");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENDWHERE, "endwhere");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ENTRY, "entry");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_EOR_EQ, "eor");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_EQUIVALENCE, "equivalance");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ERR_EQ, "err");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_EXIST_EQ, "exist");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_EXIT, "exit");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_EXTERNAL, "external");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_FILE_EQ, "file");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_FILE_EQ, "file");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_FORALL, "forall");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_FORM_EQ, "form");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_FORMAT, "format");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_FORMATTED, "formatted");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_FUNCTION, "function");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_GO, "go");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_GOTO, "goto");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_IF, "if");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_IMPLICIT, "implicit");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_IN, "in");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_INCLUDE, "include");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_INOUT, "inout");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_INQUIRE, "inquire");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_INTEGER, "integer");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_INTENT, "intent");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_INTERFACE, "interface");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_INTRINSIC, "intrinsic");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_IOSTAT_EQ, "iostat");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_KIND, "kind");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_LEN, "len");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_LOGICAL, "logical");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_MAP, "map");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_MODULE, "module");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NAME_EQ, "name");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NAMED_EQ, "named");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NAMELIST, "namelist");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NEXTREC, "nextrec");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NML_EQ, "nml");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NONE, "none");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NULLIFY, "nullify");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_NUMBER_EQ, "number");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_ONLY, "only");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_OPEN, "open");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_OPENED_EQ, "opened");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_OPERATOR, "operator");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_OPTIONAL, "optional");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_OUT, "out");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PAD_EQ, "pad");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PARAMETER, "parameter");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_POINTER, "pointer");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_POSITION_EQ, "position");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PRECISION, "precision");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PRINT, "print");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PRIVATE, "private");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PROCEDURE, "procedure");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PROGRAM, "program");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PUBLIC, "public");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_PURE, "pure");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_QUOTE, "quote");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_READ_EQ, "read");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_READ_EQ, "read");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_READWRITE_EQ, "readwrite");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_REAL, "real");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_REC_EQ, "rec");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_RECL_EQ, "recl");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_RECURSIVE, "recursive");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_RESULT, "result");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_RETURN, "return");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_REWIND, "rewind");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SAVE, "save");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SELECT, "select");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SELECTCASE, "selectcase");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SELECTTYPE, "selecttype");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SEQUENCE, "sequence");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SEQUENTIAL_EQ, "sequential");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SIZE_EQ, "size");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SIZE_EQ, "size");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_STAT_EQ, "stat");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_STATUS_EQ, "status");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_STOP, "stop");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_STRUCTURE, "structure");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.NEW_LINE, "\n");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_SUBROUTINE, "subroutine");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_TARGET, "target");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_THEN, "then");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_TO, "to");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_TYPE, "type");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_UNFORMATTED_EQ, "unformatted");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_UNION, "union");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_USE, "use");

        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_WHERE, "where");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_WHILE, "while");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_WRITE_EQ, "write");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.KW_WRITE_EQ, "write");
    }

    public void testNonKeywords() {
        String text = "asma autos b br car dou doubl finall im i ifa inti throwx ";

        TokenHierarchy<?> hi = TokenHierarchy.create(text, false, FortranTokenId.languageFortran(), null, getLexerAttributes());
        TokenSequence<?> ts = hi.tokenSequence();
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "asma");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "autos");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "b");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "br");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "car");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "dou");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "doubl");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "finall");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "im");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "i");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "ifa");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "inti");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.WHITESPACE, " ");
        LexerTestUtilities.assertNextTokenEquals(ts, FortranTokenId.IDENTIFIER, "throwx");
    }
}
