/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.editor.fortran;

import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;

/**
 *
 * @author Alexander Simon
 */
public class FortranFormatterTestCase extends FortranEditorBase {

    public FortranFormatterTestCase(String testMethodName) {
        super(testMethodName);
    }

    @Override
    protected void assertDocumentText(String msg, String expectedText) {
        super.assertDocumentText(msg, expectedText);
        reformat();
        super.assertDocumentText(msg+" (not stable)", expectedText);
    }

    public void testProgramFormat() {
        setLoadDocumentText(
                "  program   p\n"+
                "  i = 6\n"+
                " end  program\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect program reformat",
                "program p\n"+
                "    i = 6\n"+
                "end program\n"
                );
    }

    public void testIfFormat() {
        setLoadDocumentText(
                "subroutine  p\n"+
                "  if (i .eq. 6) then\n"+
                "  i =5\n"+
                "  else\n"+
                "  i=8\n"+
                "  endif\n"+
                " end  subroutine\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect program reformat",
                "subroutine p\n"+
                "    if (i .eq. 6) then\n"+
                "        i = 5\n"+
                "    else\n"+
                "        i = 8\n"+
                "    endif\n"+
                "end subroutine\n"
                );
    }

    public void testIfFixedFormat() {
        setLoadDocumentText(
                "subroutine  p\n"+
                "  if (i .eq. 6) then\n"+
                "  i =5\n"+
                "  else\n"+
                "  i=8\n"+
                "  endif\n"+
                " end  subroutine\n"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        reformat();
        assertDocumentText("Incorrect program reformat",
                "      subroutine p\n"+
                "          if (i .eq. 6) then\n"+
                "              i = 5\n"+
                "          else\n"+
                "              i = 8\n"+
                "          endif\n"+
                "      end subroutine\n"
                );
    }

    public void testEleIfFormat() {
        setLoadDocumentText(
                "subroutine  p\n"+
                "  if (i .eq. 6) then \n"+
                "  i =5\n"+
                "  elseif (i.eq.9) then \n"+
                "  i=8\n"+
                "  else\n"+
                "  i=18\n"+
                "  endif\n"+
                " end  subroutine\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect program reformat",
                "subroutine p\n"+
                "    if (i .eq. 6) then\n"+
                "        i = 5\n"+
                "    elseif (i .eq. 9) then\n"+
                "        i = 8\n"+
                "    else\n"+
                "        i = 18\n"+
                "    endif\n"+
                "end subroutine\n"
                );
    }

    public void testEleIfFormat2() {
        setLoadDocumentText(
                "subroutine  p\n"+
                "  if (i .eq. 6) then \n"+
                "  i =5\n"+
                "  else if (i.eq.9) then \n"+
                "  i=8\n"+
                "  else\n"+
                "  i=18\n"+
                "  endif\n"+
                " end  subroutine\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect program reformat",
                "subroutine p\n"+
                "    if (i .eq. 6) then\n"+
                "        i = 5\n"+
                "    else if (i .eq. 9) then\n"+
                "        i = 8\n"+
                "    else\n"+
                "        i = 18\n"+
                "    endif\n"+
                "end subroutine\n"
                );
    }

    public void testTypeFormat() {
        setLoadDocumentText(
                "  type   point\n"+
                "  real :: X,Y\n"+
                " end  type  point\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reformat",
                "type point\n"+
                "    real :: X, Y\n"+
                "end type point\n"
                );
    }

    public void testTypeFormat2() {
        setLoadDocumentText(
                "  type   point\n"+
                "  real :: X,Y\n"+
                " end  type  point\n"+
                "TYPE (point) aPoint\n"+
                "TYPE (point(4)) :: aPoints = point(1,2,3,4)\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reformat",
                "type point\n"+
                "    real :: X, Y\n"+
                "end type point\n"+
                "TYPE (point) aPoint\n"+
                "TYPE (point(4)) :: aPoints = point(1, 2, 3, 4)\n"
                );
    }

    public void testTypeFixedFormat() {
        setLoadDocumentText(
                "  type   point\n"+
                "  real :: X,Y\n"+
                " end  type  point\n"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        reformat();
        assertDocumentText("Incorrect type reformat",
                "      type point\n"+
                "          real :: X, Y\n"+
                "      end type point\n"
                );
    }

    public void testTypeFormat3() {
        setLoadDocumentText(
                "! definitions\n"+
                "Module DEFINITIONS\n"+
                "  type   point\n"+
                "  PRIVATE\n"+
                "  real :: X,Y\n"+
                "! public interface\n"+
                "  INTEGER, PUBLIC :: spin\n"+
                " CONTAINS\n"+
                "PROCEDURE, PASS :: LENGTH => POINT_LENGTH\n"+
                "  PROCEDURE (OPEN_FILE), DEPEND, PASS(HANDLE) :: OPEN\n"+
                " end  type  point\n"+
                "END Module DEFINITIONS\n"
                );
        setDefaultsOptions();
        reformat();
        assertDocumentText("Incorrect type reformat",
                "! definitions\n"+
                "Module DEFINITIONS\n"+
                "    type point\n"+
                "        PRIVATE\n"+
                "        real :: X, Y\n"+
                "        ! public interface\n"+
                "        INTEGER, PUBLIC :: spin\n"+
                "    CONTAINS\n"+
                "        PROCEDURE, PASS :: LENGTH => POINT_LENGTH\n"+
                "        PROCEDURE (OPEN_FILE), DEPEND, PASS(HANDLE) :: OPEN\n"+
                "    end type point\n"+
                "END Module DEFINITIONS\n"
                );
//nnnnnk fix lexer.
// First problem is wrong representation of T[35]: "CONTAINS"
// Second problem is =>
//This is part of wrong token sequence:
//T[31]: "::" F(2) DOUBLECOLON[155] FlyT, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@6e4ba6, IHC=10519538
//T[32]: " " <111,112> WHITESPACE[173] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@1289e48, IHC=28295413
//T[33]: "spin" <112,116> IDENTIFIER[0] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@1a517bd, IHC=28943672
//T[34]: "\n" <116,117> NEW_LINE[174] DefT, IHC=17704280
//T[35]: "CONTAINS" <117,125> LINE_COMMENT_FIXED[175] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@1b5d2b2, IHC=1815387
//T[36]: "\n" <125,126> NEW_LINE[174] DefT, IHC=10232210
//T[37]: "procedure" F(9) KW_PROCEDURE[93] FlyT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@6030f9, IHC=19878555
//T[38]: "," F(1) COMMA[153] FlyT, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@1d85e85, IHC=16062310
//T[39]: " " <136,137> WHITESPACE[173] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@258c74, IHC=19419730
//T[40]: "PASS" <137,141> IDENTIFIER[0] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@34a1c8, IHC=23686732
//T[41]: " " <141,142> WHITESPACE[173] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@c84361, IHC=29336323
//T[42]: "::" F(2) DOUBLECOLON[155] FlyT, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@6743e2, IHC=10519538
//T[43]: " " <144,145> WHITESPACE[173] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@199ea3c, IHC=31537708
//T[44]: "LENGTH" <145,151> IDENTIFIER[0] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@1b2591c, IHC=33061807
//T[45]: " " <151,152> WHITESPACE[173] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@8034b6, IHC=17087715
//T[46]: "=" F(1) EQ[151] FlyT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@ce0bb, IHC=24528974
//T[47]: ">" F(1) OP_GT[149] FlyT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@84fdbc, IHC=19815572
//T[48]: " " <154,155> WHITESPACE[173] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@bffc3a, IHC=4751287
//T[49]: "POINT_LENGTH" <155,167> IDENTIFIER[0] DefT, la=1, st=org.netbeans.modules.cnd.lexer.FortranLexer$State@1b8fcdd, IHC=31541880
//T[50]: "\n" <167,168> NEW_LINE[174] DefT, IHC=31472225
    }
}
