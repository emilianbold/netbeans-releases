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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.fortran;

import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;

/**
 * Class was taken from java
 * Links point to java IZ.
 * C/C++ specific tests begin from testReformatSimpleClass
 *
 * @author Alexander Simon
 */
public class FortranIndentUnitTestCase extends FortranFormatterBaseUnitTestCase {

    public FortranIndentUnitTestCase(String testMethodName) {
        super(testMethodName);
    }

    public void testProgramIndentFree() {
        setLoadDocumentText(
                "program p|"
                );
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line program indent (free form)",
                "program p\n" +
                "    |"
                );
    }

    public void testProgramIndentFixed() {
        setLoadDocumentText(
                "      program p|"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line program indent (fixed form)",
                "      program p\n" +
                "          |"
                );
    }
    
    public void testEndProgramIndentFree() {
        setLoadDocumentText(
                "program p\n"+
                "    end progra|"
                );
        setDefaultsOptions();        
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect new-line end program indent (free form)",
                "program p\n" +
                "end program|"
                );
    }

    public void testEndProgramIndentFixed() {
        setLoadDocumentText(
                "      program p\n"+
                "    end progra|"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect new-line end program indent (fixed form)",
                "      program p\n" +
                "      end program|"
                );
    }

    public void testEndProgramIndent2Free() {
        setLoadDocumentText(
                "program p\n"+
                "    endprogra|"
                );
        setDefaultsOptions();
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect new-line end program indent (free form)",
                "program p\n" +
                "endprogram|"
                );
    }

    public void testEndProgramIndent2Fixed() {
        setLoadDocumentText(
                "      program p\n"+
                "    endprogra|"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect new-line end program indent (fixed form)",
                "      program p\n" +
                "      endprogram|"
                );
    }

    public void testSubroutineIndentFree() {
        setLoadDocumentText(
                "subroutine p(c)|"
                );
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line subroutine indent (free form)",
                "subroutine p(c)\n"+
                "    |"
                );
    }

    public void testSubroutineIndentFixed() {
        setLoadDocumentText(
                "      subroutine p(c)|"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line subroutine indent (fixed form)",
                "      subroutine p(c)\n"+
                "          |"
                );
    }

    public void testEndSubroutineIndentFree() {
        setLoadDocumentText(
                "subroutine p(c)\n"+
                "    end subroutin|"
                );
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect new-line emd subroutine indent (free form)",
                "subroutine p(c)\n"+
                "end subroutine|"
                );
    }

    public void testEndSubroutineIndentFixed() {
        setLoadDocumentText(
                "      subroutine p(c)\n"+
                "    end subroutin|"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect new-line emd subroutine indent (fixed form)",
                "      subroutine p(c)\n"+
                "      end subroutine|"
                );
    }

    public void testEndSubroutineIndent2Free() {
        setLoadDocumentText(
                "subroutine p(c)\n"+
                "    endsubroutin|"
                );
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect new-line end subroutine indent (free form)",
                "subroutine p(c)\n"+
                "endsubroutine|"
                );
    }

    public void testEndSubroutineIndent2Fixed() {
        setLoadDocumentText(
                "      subroutine p(c)\n"+
                "    endsubroutin|"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect new-line end subroutine indent (fixed form)",
                "      subroutine p(c)\n"+
                "      endsubroutine|"
                );
    }

    public void testIfIndentFree() {
        setLoadDocumentText(
                "if (a .eq. 0) then|"
                );
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line if indent (free form)",
                "if (a .eq. 0) then\n"+
                "    |"
                );
    }

    public void testIfIndentFixed() {
        setLoadDocumentText(
                "      if (a .eq. 0) then|"
                );
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line if indent (fixed form)",
                "      if (a .eq. 0) then\n"+
                "          |"
                );
    }

    public void testCommentInFreeFormat() {
        setLoadDocumentText(
                "program test\n"+
                "\n"+
                "    write (*,*) 'Hello'\n"+
                "\n"+
                "    contains\n"+
                "\n"+
                "    subroutine p(c)\n"+
                "        integer :: c\n"+
                "        if (c > 0) then\n"+
                "             c = 0\n"+
                "             end i|"
                );
        setDefaultsOptions();
        typeChar('f', true);
        assertDocumentTextAndCaret("Infinite loop CR# 6749526",
                "program test\n"+
                "\n"+
                "    write (*,*) 'Hello'\n"+
                "\n"+
                "    contains\n"+
                "\n"+
                "    subroutine p(c)\n"+
                "        integer :: c\n"+
                "        if (c > 0) then\n"+
                "             c = 0\n"+
                "        end if|"
                );
    }
    
}
