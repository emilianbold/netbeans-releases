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
public class FortranIndentTestCase extends FortranEditorBase {

    public FortranIndentTestCase(String testMethodName) {
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

    public void testElseIfIndentFree() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "        elsei|"
                );
        setDefaultsOptions();
        typeChar('f', true);
        assertDocumentTextAndCaret("Incorrect new-line else if indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    elseif|"
                );
    }

    public void testWhileIndentFree() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    while (j(3) == i + 2)\n" +
                "        i = j(5)\n" +
                "        end whil|"
                );
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect new-line while indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    while (j(3) == i + 2)\n" +
                "        i = j(5)\n" +
                "    end while|"
                );
    }

    public void testSubroutine2Free() {
        setLoadDocumentText(
                "recursive subroutine p(c)|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line subroutine indent (free form)",
                "recursive subroutine p(c)\n" +
                "    |");
    }

    public void testBlockDataFree() {
        setLoadDocumentText(
                "BLoCKdatA Unit|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect block data indent (free form)",
                "BLoCKdatA Unit\n" +
                "    |");
    }

    public void testBlockData2Free() {
        setLoadDocumentText(
                "BLoCK datA Unit|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect block data indent (free form)",
                "BLoCK datA Unit\n" +
                "    |");
    }

    public void testEndBlockDataFree() {
        setLoadDocumentText(
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "   eNDBLOCKdat|");
        setDefaultsOptions();
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (free form)",
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "eNDBLOCKdata|");
    }

    public void testEndBlockData2Free() {
        setLoadDocumentText(
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "   eND BLOCK dat|");
        setDefaultsOptions();
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (free form)",
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "eND BLOCK data|");
    }

    public void testEndBlockData3Free() {
        setLoadDocumentText(
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "   eND BLOCKdat|");
        setDefaultsOptions();
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (free form)",
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "eND BLOCKdata|");
    }

    public void testEndBlockData4Free() {
        setLoadDocumentText(
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "   eNDBLOCK dat|");
        setDefaultsOptions();
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (free form)",
                "BLoCKdatA Unit\n" +
                "    DoublePrecision A\n" +
                "    datA a/1d0/\n" +
                "    COMMOn /a/ a\n" +
                "eNDBLOCK data|");
    }

    public void testElseIfFree() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    elseif |");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    elseif \n" +
                "        |");
    }

    public void testElseIf2Free() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    else if |");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    else if \n" +
                "        |");
    }

    public void testElseIf3Free() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "        elsei|");
        setDefaultsOptions();
        typeChar('f', true);
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    elseif|");
    }

    public void testElseIf4Free() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "        else i|");
        setDefaultsOptions();
        typeChar('f', true);
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    else if|");
    }

    public void testEndIfFree() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    elseif (i < 3) then\n" +
                "        i = 2\n" +
                "        endi|");
        setDefaultsOptions();
        typeChar('f', true);
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    if (j(3) == i + 2) then\n" +
                "        i = j(5)\n" +
                "    elseif (i < 3) then\n" +
                "        i = 2\n" +
                "    endif|");
    }

    public void testWhereFree() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        |");
    }

    public void testElseWhereFree() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "        else wher|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where|");
    }

    public void testElseWhere2Free() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "        elsewher|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    elsewhere|");
    }

    public void testElseWhere3Free() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where\n" +
                "        |");
    }

    public void testEndWhereFree() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where(j == 1)\n" +
                "        j = 0\n" +
                "        end wher|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where(j == 1)\n" +
                "        j = 0\n" +
                "    end where|");
    }

    public void testEndWhere2Free() {
        setLoadDocumentText(
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where(j == 1)\n" +
                "        j = 0\n" +
                "        endwher|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (free form)",
                "program A\n" +
                "    integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "    i = 1\n" +
                "    where(j > 2)\n" +
                "        j = 10\n" +
                "    else where(j == 1)\n" +
                "        j = 0\n" +
                "    endwhere|");
    }

    public void testTypeFree() {
        setLoadDocumentText(
                "Type|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect type indent (free form)",
                "Type\n" +
                "    |");
    }

    public void testEndTypeFree() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "    endTyp|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect type indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType|");
    }

    public void testEndType2Free() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "    end Typ|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect type indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "end Type|");
    }

    public void testEnumFree() {
        setLoadDocumentText(
                "Enum |");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect enum indent (free form)",
                "Enum \n" +
                "    |");
    }

    public void testEndEnumFree() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnu|");
        setDefaultsOptions();
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect enum indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum|");
    }

    public void testEndEnum2Free() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "end Enu|");
        setDefaultsOptions();
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect enum indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "end Enum|");
    }

    public void testSelectFree() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect select indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase\n" +
                "    |");
    }

    public void testSelect2Free() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "select Case|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect select indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "select Case\n" +
                "    |");
    }

    public void testSelect3Free() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect select indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        |");
    }

    public void testSelect4Free() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        print *, \" zero \", Enum3.Enum\n" +
                "        cas|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect select indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        print *, \" zero \", Enum3.Enum\n" +
                "    case|");
    }

    public void testSelect5Free() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        print *, \" zero \", Enum3.Enum\n" +
                "    case(one)\n" +
                "        print *, \" one \", Enum3.Enum\n" +
                "    case(two)\n" +
                "        print *, \" two \", Enum3.Enum\n" +
                "        endSelec|");
        setDefaultsOptions();
        typeChar('t', true);
        assertDocumentTextAndCaret("Incorrect select indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        print *, \" zero \", Enum3.Enum\n" +
                "    case(one)\n" +
                "        print *, \" one \", Enum3.Enum\n" +
                "    case(two)\n" +
                "        print *, \" two \", Enum3.Enum\n" +
                "endSelect|");
    }

    public void testSelect6Free() {
        setLoadDocumentText(
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        print *, \" zero \", Enum3.Enum\n" +
                "    case(one)\n" +
                "        print *, \" one \", Enum3.Enum\n" +
                "    case(two)\n" +
                "        print *, \" two \", Enum3.Enum\n" +
                "        end Selec|");
        setDefaultsOptions();
        typeChar('t', true);
        assertDocumentTextAndCaret("Incorrect select indent (free form)",
                "Enum Enum\n" +
                "    Enumerator zero, one, two\n" +
                "endEnum Enum\n" +
                "Type Enum1\n" +
                "    Type(Enum) Enum\n" +
                "endType Enum1\n" +
                "Type(Enum),parameter::Enum2/zero/\n" +
                "Type(Enum1) Enum,Enum3\n" +
                "Enum3.Enum=two\n" +
                "selectCase(Enum3.Enum)\n" +
                "    case(zero)\n" +
                "        print *, \" zero \", Enum3.Enum\n" +
                "    case(one)\n" +
                "        print *, \" one \", Enum3.Enum\n" +
                "    case(two)\n" +
                "        print *, \" two \", Enum3.Enum\n" +
                "end Select|");
    }

    public void testModuleFree() {
        setLoadDocumentText(
                "Module|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect module indent (free form)",
                "Module\n" +
                "    |");
    }

    public void testEndModuleFree() {
        setLoadDocumentText(
                "Module A\n" +
                "    EndModul|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect module indent (free form)",
                "Module A\n" +
                "EndModule|");
    }

    public void testEndModule2Free() {
        setLoadDocumentText(
                "Module A\n" +
                "    End Modul|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect module indent (free form)",
                "Module A\n" +
                "End Module|");
    }

    public void testInrefaceFree() {
        setLoadDocumentText(
                "Module A\n" +
                "    INTERFACE|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect inreface indent (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        |");
    }

    public void testEndInterfaceFree() {
        setLoadDocumentText(
                "Module A\n" +
                "    INTERFACE\n" +
                "        SUBROUTINE EXT1 (X, Y, Z)\n" +
                "            REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "        ENDSUBROUTINE EXT1\n" +
                "        SUBROUTINE EXT2 (X, Z)\n" +
                "            REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "        ENDSUBROUTINE EXT2\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "        ENDFUNCTION EXT3\n" +
                "        ENDINTERFAC|");
        setDefaultsOptions();
        typeChar('E', true);
        assertDocumentTextAndCaret("Incorrect end interface indent (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        SUBROUTINE EXT1 (X, Y, Z)\n" +
                "            REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "        ENDSUBROUTINE EXT1\n" +
                "        SUBROUTINE EXT2 (X, Z)\n" +
                "            REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "        ENDSUBROUTINE EXT2\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "        ENDFUNCTION EXT3\n" +
                "    ENDINTERFACE|");
    }

    public void testEndInterface2Free() {
        setLoadDocumentText(
                "Module A\n" +
                "    INTERFACE\n" +
                "        SUBROUTINE EXT1 (X, Y, Z)\n" +
                "            REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "        ENDSUBROUTINE EXT1\n" +
                "        SUBROUTINE EXT2 (X, Z)\n" +
                "            REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "        ENDSUBROUTINE EXT2\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "        ENDFUNCTION EXT3\n" +
                "        END INTERFAC|");
        setDefaultsOptions();
        typeChar('E', true);
        assertDocumentTextAndCaret("Incorrect end interface indent (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        SUBROUTINE EXT1 (X, Y, Z)\n" +
                "            REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "        ENDSUBROUTINE EXT1\n" +
                "        SUBROUTINE EXT2 (X, Z)\n" +
                "            REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "        ENDSUBROUTINE EXT2\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "        ENDFUNCTION EXT3\n" +
                "    END INTERFACE|");
    }

    public void testFunctionFree() {
        setLoadDocumentText(
                "Module A\n" +
                "    INTERFACE\n" +
                "        FUNCTION EXT3 (P, Q)|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect function indent (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            |");
    }

    public void testFunction2Free() {
        setLoadDocumentText(
                "Module A\n" +
                "    INTERFACE\n" +
                "        DOUBLEPRECISION FUNCTION EXT3 (P, Q)|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect function indent (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        DOUBLEPRECISION FUNCTION EXT3 (P, Q)\n" +
                "            |");
    }

    public void testEndFunctionFree() {
        setLoadDocumentText(
                "Module A\n" +
                "    INTERFACE\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "            ENDFUNCTIO|");
        setDefaultsOptions();
        typeChar('N', true);
        assertDocumentTextAndCaret("Incorrect end function indent (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "        ENDFUNCTION|");
    }

    public void testEndFunction2Free() {
        setLoadDocumentText(
                "Module A\n" +
                "    INTERFACE\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "            END FUNCTIO|");
        setDefaultsOptions();
        typeChar('N', true);
        assertDocumentTextAndCaret("Incorrect end function indent (free form)",
                "Module A\n" +
                "    INTERFACE\n" +
                "        FUNCTION EXT3 (P, Q)\n" +
                "            LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "        END FUNCTION|");
    }

    public void testForallFree() {
        setLoadDocumentText(
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i=1,10\n" +
                "    call a(i);j(10-i)=i\n" +
                "enddo\n" +
                "forall (i = 2:7, J(I) <> 3)|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect forall indent (free form)",
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i=1,10\n" +
                "    call a(i);j(10-i)=i\n" +
                "enddo\n" +
                "forall (i = 2:7, J(I) <> 3)\n" +
                "    |");
    }

    public void testEndForallFree() {
        setLoadDocumentText(
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i=1,10\n" +
                "    call a(i);j(10-i)=i\n" +
                "enddo\n" +
                "forall (i=2:7,J(I)<>3)\n" +
                "    j(i)=100\n" +
                "    endforal|");
        setDefaultsOptions();
        typeChar('l', true);
        assertDocumentTextAndCaret("Incorrect forall indent (free form)",
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i=1,10\n" +
                "    call a(i);j(10-i)=i\n" +
                "enddo\n" +
                "forall (i=2:7,J(I)<>3)\n" +
                "    j(i)=100\n" +
                "endforall|");
    }

    public void testEndForall2Free() {
        setLoadDocumentText(
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i=1,10\n" +
                "    call a(i);j(10-i)=i\n" +
                "enddo\n" +
                "forall (i=2:7,J(I)<>3)\n" +
                "    j(i)=100\n" +
                "    end foral|");
        setDefaultsOptions();
        typeChar('l', true);
        assertDocumentTextAndCaret("Incorrect forall indent (free form)",
                "implicit none\n" +
                "integer(4) i, j(0:9)\n" +
                "do i=1,10\n" +
                "    call a(i);j(10-i)=i\n" +
                "enddo\n" +
                "forall (i=2:7,J(I)<>3)\n" +
                "    j(i)=100\n" +
                "end forall|");
    }

    public void testDoFree() {
        setLoadDocumentText(
                "PROGRAM test\n" +
                "    do i = 1, 7|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect do indent (free form)",
                "PROGRAM test\n" +
                "    do i = 1, 7\n" +
                "        |");
    }

    public void testEndDoFree() {
        setLoadDocumentText(
                "PROGRAM test\n" +
                "    do i = 1, 7\n" +
                "        do j = 1, 7\n" +
                "            i1ad2(i, j) = CHAR(k + 40)\n" +
                "            k = k + 1\n" +
                "        enddo\n" +
                "        endd|");
        setDefaultsOptions();
        typeChar('o', true);
        assertDocumentTextAndCaret("Incorrect do indent (free form)",
                "PROGRAM test\n" +
                "    do i = 1, 7\n" +
                "        do j = 1, 7\n" +
                "            i1ad2(i, j) = CHAR(k + 40)\n" +
                "            k = k + 1\n" +
                "        enddo\n" +
                "    enddo|");
    }

    public void testEndDo2Free() {
        setLoadDocumentText(
                "PROGRAM test\n" +
                "    do i = 1, 7\n" +
                "        do j = 1, 7\n" +
                "            i1ad2(i, j) = CHAR(k + 40)\n" +
                "            k = k + 1\n" +
                "        enddo\n" +
                "        end d|");
        setDefaultsOptions();
        typeChar('o', true);
        assertDocumentTextAndCaret("Incorrect do indent (free form)",
                "PROGRAM test\n" +
                "    do i = 1, 7\n" +
                "        do j = 1, 7\n" +
                "            i1ad2(i, j) = CHAR(k + 40)\n" +
                "            k = k + 1\n" +
                "        enddo\n" +
                "    end do|");
    }

    public void testMapFree() {
        setLoadDocumentText(
                "program\n" +
                "    structure /explorer2/\n" +
                "        union\n" +
                "            map|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect map indent (free form)",
                "program\n" +
                "    structure /explorer2/\n" +
                "        union\n" +
                "            map\n" +
                "                |");
    }

    public void testEndMapFree() {
        setLoadDocumentText(
                "program\n" +
                "    structure /explorer2/\n" +
                "        union\n" +
                "            map\n" +
                "                logical*1 :: var\n" +
                "                end ma|");
        setDefaultsOptions();
        typeChar('p', true);
        assertDocumentTextAndCaret("Incorrect map indent (free form)",
                "program\n" +
                "    structure /explorer2/\n" +
                "        union\n" +
                "            map\n" +
                "                logical*1 :: var\n" +
                "            end map|");
    }

    public void testUnionFree() {
        setLoadDocumentText(
                "program\n" +
                "    structure /explorer2/\n" +
                "        union|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect union indent (free form)",
                "program\n" +
                "    structure /explorer2/\n" +
                "        union\n" +
                "            |");
    }

    public void testStructureFree() {
        setLoadDocumentText(
                "program\n" +
                "    structure /OUTSTR/|");
        setDefaultsOptions();
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect structure indent (free form)",
                "program\n" +
                "    structure /OUTSTR/\n" +
                "        |");
    }

    public void testEndStructureFree() {
        setLoadDocumentText(
                "program\n" +
                "    structure /OUTSTR/\n" +
                "        real*4 zxc\n" +
                "        record /STR1/ inex\n" +
                "        end structur|");
        setDefaultsOptions();
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect structure indent (free form)",
                "program\n" +
                "    structure /OUTSTR/\n" +
                "        real*4 zxc\n" +
                "        record /STR1/ inex\n" +
                "    end structure|");
    }

    public void testSubroutine2Fixed() {
        setLoadDocumentText(
                "      recursive subroutine p(c)|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line subroutine indent (fixed form)",
                "      recursive subroutine p(c)\n" +
                "          |");
    }

    public void testBlockDataFixed() {
        setLoadDocumentText(
                "      BLoCKdatA Unit|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect block data indent (fixed form)",
                "      BLoCKdatA Unit\n" +
                "          |");
    }

    public void testBlockData2Fixed() {
        setLoadDocumentText(
                "      BLoCK datA Unit|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect block data indent (fixed form)",
                "      BLoCK datA Unit\n" +
                "          |");
    }

    public void testEndBlockDataFixed() {
        setLoadDocumentText(
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "         eNDBLOCKdat|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (fixed form)",
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "      eNDBLOCKdata|");
    }

    public void testEndBlockData2Fixed() {
        setLoadDocumentText(
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "         eND BLOCK dat|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (fixed form)",
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "      eND BLOCK data|");
    }

    public void testEndBlockData3Fixed() {
        setLoadDocumentText(
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "         eND BLOCKdat|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (fixed form)",
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "      eND BLOCKdata|");
    }

    public void testEndBlockData4Fixed() {
        setLoadDocumentText(
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "         eNDBLOCK dat|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('a', true);
        assertDocumentTextAndCaret("Incorrect block data indent (fixed form)",
                "      BLoCKdatA Unit\n" +
                "          DoublePrecision A\n" +
                "          datA a/1d0/\n" +
                "          COMMOn /a/ a\n" +
                "      eNDBLOCK data|");
    }

    public void testElseIfFixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          elseif |");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          elseif \n" +
                "              |");
    }

    public void testElseIf2Fixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          else if |");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          else if \n" +
                "              |");
    }

    public void testElseIf3Fixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "              elsei|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('f', true);
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          elseif|");
    }

    public void testElseIf4Fixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "              else i|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('f', true);
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          else if|");
    }

    public void testEndIfFixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          elseif (i < 3) then\n" +
                "              i = 2\n" +
                "              endi|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('f', true);
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          if (j(3) == i + 2) then\n" +
                "              i = j(5)\n" +
                "          elseif (i < 3) then\n" +
                "              i = 2\n" +
                "          endif|");
    }

    public void testWhereFixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              |");
    }

    public void testElseWhereFixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "              else wher|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where|");
    }

    public void testElseWhere2Fixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "              elsewher|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          elsewhere|");
    }

    public void testElseWhere3Fixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where\n" +
                "              |");
    }

    public void testEndWhereFixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where(j == 1)\n" +
                "              j = 0\n" +
                "              end wher|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where(j == 1)\n" +
                "              j = 0\n" +
                "          end where|");
    }

    public void testEndWhere2Fixed() {
        setLoadDocumentText(
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where(j == 1)\n" +
                "              j = 0\n" +
                "              endwher|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect statements indent (fixed form)",
                "      program A\n" +
                "          integer :: j(5)/1, 2, 3, 4, 5/, i\n" +
                "          i = 1\n" +
                "          where(j > 2)\n" +
                "              j = 10\n" +
                "          else where(j == 1)\n" +
                "              j = 0\n" +
                "          endwhere|");
    }

    public void testTypeFixed() {
        setLoadDocumentText(
                "      Type|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect type indent (fixed form)",
                "      Type\n" +
                "          |");
    }

    public void testEndTypeFixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "          endTyp|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect type indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType|");
    }

    public void testEndType2Fixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "          end Typ|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect type indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      end Type|");
    }

    public void testEnumFixed() {
        setLoadDocumentText(
                "      Enum |");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect enum indent (fixed form)",
                "      Enum \n" +
                "          |");
    }

    public void testEndEnumFixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnu|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect enum indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum|");
    }

    public void testEndEnum2Fixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      end Enu|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect enum indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      end Enum|");
    }

    public void testSelectFixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect select indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase\n" +
                "          |");
    }

    public void testSelect2Fixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      select Case|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect select indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      select Case\n" +
                "          |");
    }

    public void testSelect3Fixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect select indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              |");
    }

    public void testSelect4Fixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              print *, \" zero \", Enum3.Enum\n" +
                "              cas|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect select indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              print *, \" zero \", Enum3.Enum\n" +
                "          case|");
    }

    public void testSelect5Fixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              print *, \" zero \", Enum3.Enum\n" +
                "          case(one)\n" +
                "              print *, \" one \", Enum3.Enum\n" +
                "          case(two)\n" +
                "              print *, \" two \", Enum3.Enum\n" +
                "              endSelec|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('t', true);
        assertDocumentTextAndCaret("Incorrect select indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              print *, \" zero \", Enum3.Enum\n" +
                "          case(one)\n" +
                "              print *, \" one \", Enum3.Enum\n" +
                "          case(two)\n" +
                "              print *, \" two \", Enum3.Enum\n" +
                "      endSelect|");
    }

    public void testSelect6Fixed() {
        setLoadDocumentText(
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              print *, \" zero \", Enum3.Enum\n" +
                "          case(one)\n" +
                "              print *, \" one \", Enum3.Enum\n" +
                "          case(two)\n" +
                "              print *, \" two \", Enum3.Enum\n" +
                "              end Selec|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('t', true);
        assertDocumentTextAndCaret("Incorrect select indent (fixed form)",
                "      Enum Enum\n" +
                "          Enumerator zero, one, two\n" +
                "      endEnum Enum\n" +
                "      Type Enum1\n" +
                "          Type(Enum) Enum\n" +
                "      endType Enum1\n" +
                "      Type(Enum),parameter::Enum2/zero/\n" +
                "      Type(Enum1) Enum,Enum3\n" +
                "      Enum3.Enum=two\n" +
                "      selectCase(Enum3.Enum)\n" +
                "          case(zero)\n" +
                "              print *, \" zero \", Enum3.Enum\n" +
                "          case(one)\n" +
                "              print *, \" one \", Enum3.Enum\n" +
                "          case(two)\n" +
                "              print *, \" two \", Enum3.Enum\n" +
                "      end Select|");
    }

    public void testModuleFixed() {
        setLoadDocumentText(
                "      Module|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect module indent (fixed form)",
                "      Module\n" +
                "          |");
    }

    public void testEndModuleFixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          EndModul|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect module indent (fixed form)",
                "      Module A\n" +
                "      EndModule|");
    }

    public void testEndModule2Fixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          End Modul|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect module indent (fixed form)",
                "      Module A\n" +
                "      End Module|");
    }

    public void testInrefaceFixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          INTERFACE|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect inreface indent (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              |");
    }

    public void testEndInterfaceFixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          INTERFACE\n" +
                "              SUBROUTINE EXT1 (X, Y, Z)\n" +
                "                  REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "              ENDSUBROUTINE EXT1\n" +
                "              SUBROUTINE EXT2 (X, Z)\n" +
                "                  REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "              ENDSUBROUTINE EXT2\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "              ENDFUNCTION EXT3\n" +
                "              ENDINTERFAC|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('E', true);
        assertDocumentTextAndCaret("Incorrect end interface indent (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              SUBROUTINE EXT1 (X, Y, Z)\n" +
                "                  REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "              ENDSUBROUTINE EXT1\n" +
                "              SUBROUTINE EXT2 (X, Z)\n" +
                "                  REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "              ENDSUBROUTINE EXT2\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "              ENDFUNCTION EXT3\n" +
                "          ENDINTERFACE|");
    }

    public void testEndInterface2Fixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          INTERFACE\n" +
                "              SUBROUTINE EXT1 (X, Y, Z)\n" +
                "                  REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "              ENDSUBROUTINE EXT1\n" +
                "              SUBROUTINE EXT2 (X, Z)\n" +
                "                  REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "              ENDSUBROUTINE EXT2\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "              ENDFUNCTION EXT3\n" +
                "              END INTERFAC|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('E', true);
        assertDocumentTextAndCaret("Incorrect end interface indent (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              SUBROUTINE EXT1 (X, Y, Z)\n" +
                "                  REAL, DIMENSION (100, 100) :: X, Y, Z\n" +
                "              ENDSUBROUTINE EXT1\n" +
                "              SUBROUTINE EXT2 (X, Z)\n" +
                "                  REAL X;COMPLEX (KIND = 4) Z (2000)\n" +
                "              ENDSUBROUTINE EXT2\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "              ENDFUNCTION EXT3\n" +
                "          END INTERFACE|");
    }

    public void testFunctionFixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          INTERFACE\n" +
                "              FUNCTION EXT3 (P, Q)|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect function indent (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  |");
    }

    public void testFunction2Fixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          INTERFACE\n" +
                "              DOUBLEPRECISION FUNCTION EXT3 (P, Q)|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect function indent (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              DOUBLEPRECISION FUNCTION EXT3 (P, Q)\n" +
                "                  |");
    }

    public void testEndFunctionFixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          INTERFACE\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "                  ENDFUNCTIO|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('N', true);
        assertDocumentTextAndCaret("Incorrect end function indent (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "              ENDFUNCTION|");
    }

    public void testEndFunction2Fixed() {
        setLoadDocumentText(
                "      Module A\n" +
                "          INTERFACE\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "                  END FUNCTIO|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('N', true);
        assertDocumentTextAndCaret("Incorrect end function indent (fixed form)",
                "      Module A\n" +
                "          INTERFACE\n" +
                "              FUNCTION EXT3 (P, Q)\n" +
                "                  LOGICAL EXT3 ; INTEGER P (1000) ; LOGICAL Q (1000)\n" +
                "              END FUNCTION|");
    }

    public void testForallFixed() {
        setLoadDocumentText(
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i=1,10\n" +
                "          call a(i);j(10-i)=i\n" +
                "      enddo\n" +
                "      forall (i = 2:7, J(I) <> 3)|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect forall indent (fixed form)",
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i=1,10\n" +
                "          call a(i);j(10-i)=i\n" +
                "      enddo\n" +
                "      forall (i = 2:7, J(I) <> 3)\n" +
                "          |");
    }

    public void testEndForallFixed() {
        setLoadDocumentText(
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i=1,10\n" +
                "          call a(i);j(10-i)=i\n" +
                "      enddo\n" +
                "      forall (i=2:7,J(I)<>3)\n" +
                "          j(i)=100\n" +
                "          endforal|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('l', true);
        assertDocumentTextAndCaret("Incorrect forall indent (fixed form)",
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i=1,10\n" +
                "          call a(i);j(10-i)=i\n" +
                "      enddo\n" +
                "      forall (i=2:7,J(I)<>3)\n" +
                "          j(i)=100\n" +
                "      endforall|");
    }

    public void testEndForall2Fixed() {
        setLoadDocumentText(
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i=1,10\n" +
                "          call a(i);j(10-i)=i\n" +
                "      enddo\n" +
                "      forall (i=2:7,J(I)<>3)\n" +
                "          j(i)=100\n" +
                "          end foral|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('l', true);
        assertDocumentTextAndCaret("Incorrect forall indent (fixed form)",
                "      implicit none\n" +
                "      integer(4) i, j(0:9)\n" +
                "      do i=1,10\n" +
                "          call a(i);j(10-i)=i\n" +
                "      enddo\n" +
                "      forall (i=2:7,J(I)<>3)\n" +
                "          j(i)=100\n" +
                "      end forall|");
    }

    public void testDoFixed() {
        setLoadDocumentText(
                "      PROGRAM test\n" +
                "          do i = 1, 7|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect do indent (fixed form)",
                "      PROGRAM test\n" +
                "          do i = 1, 7\n" +
                "              |");
    }

    public void testEndDoFixed() {
        setLoadDocumentText(
                "      PROGRAM test\n" +
                "          do i = 1, 7\n" +
                "              do j = 1, 7\n" +
                "                  i1ad2(i, j) = CHAR(k + 40)\n" +
                "                  k = k + 1\n" +
                "              enddo\n" +
                "              endd|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('o', true);
        assertDocumentTextAndCaret("Incorrect do indent (fixed form)",
                "      PROGRAM test\n" +
                "          do i = 1, 7\n" +
                "              do j = 1, 7\n" +
                "                  i1ad2(i, j) = CHAR(k + 40)\n" +
                "                  k = k + 1\n" +
                "              enddo\n" +
                "          enddo|");
    }

    public void testEndDo2Fixed() {
        setLoadDocumentText(
                "      PROGRAM test\n" +
                "          do i = 1, 7\n" +
                "              do j = 1, 7\n" +
                "                  i1ad2(i, j) = CHAR(k + 40)\n" +
                "                  k = k + 1\n" +
                "              enddo\n" +
                "              end d|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('o', true);
        assertDocumentTextAndCaret("Incorrect do indent (fixed form)",
                "      PROGRAM test\n" +
                "          do i = 1, 7\n" +
                "              do j = 1, 7\n" +
                "                  i1ad2(i, j) = CHAR(k + 40)\n" +
                "                  k = k + 1\n" +
                "              enddo\n" +
                "          end do|");
    }

    public void testMapFixed() {
        setLoadDocumentText(
                "      program\n" +
                "          structure /explorer2/\n" +
                "              union\n" +
                "                  map|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect map indent (fixed form)",
                "      program\n" +
                "          structure /explorer2/\n" +
                "              union\n" +
                "                  map\n" +
                "                      |");
    }

    public void testEndMapFixed() {
        setLoadDocumentText(
                "      program\n" +
                "          structure /explorer2/\n" +
                "              union\n" +
                "                  map\n" +
                "                      logical*1 :: var\n" +
                "                      end ma|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('p', true);
        assertDocumentTextAndCaret("Incorrect map indent (fixed form)",
                "      program\n" +
                "          structure /explorer2/\n" +
                "              union\n" +
                "                  map\n" +
                "                      logical*1 :: var\n" +
                "                  end map|");
    }

    public void testUnionFixed() {
        setLoadDocumentText(
                "      program\n" +
                "          structure /explorer2/\n" +
                "              union|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect union indent (fixed form)",
                "      program\n" +
                "          structure /explorer2/\n" +
                "              union\n" +
                "                  |");
    }

    public void testStructureFixed() {
        setLoadDocumentText(
                "      program\n" +
                "          structure /OUTSTR/|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect structure indent (fixed form)",
                "      program\n" +
                "          structure /OUTSTR/\n" +
                "              |");
    }

    public void testEndStructureFixed() {
        setLoadDocumentText(
                "      program\n" +
                "          structure /OUTSTR/\n" +
                "              real*4 zxc\n" +
                "              record /STR1/ inex\n" +
                "              end structur|");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(false);
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect structure indent (fixed form)",
                "      program\n" +
                "          structure /OUTSTR/\n" +
                "              real*4 zxc\n" +
                "              record /STR1/ inex\n" +
                "          end structure|");
    }

    public void testTypeIndentFree() {
        setLoadDocumentText(
                "  function QC_LOG(X) result(Z)\n" +
                "    type (QC), intent(in) :: X|\n" +
                "    type (QC) :: Z\n" +
                "\n" +
                "    Z % QR = log(abs(X))\n" +
                "    Z % QI = atan2(X % QI, X % QR)\n" +
                "\n" +
                "    return\n" +
                "  end function QC_LOG");
        setDefaultsOptions();
        FortranCodeStyle.get(getDocument()).setFreeFormatFortran(true);
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect structure indent (fixed form)",
                "  function QC_LOG(X) result(Z)\n" +
                "    type (QC), intent(in) :: X\n" +
                "    |\n" +
                "    type (QC) :: Z\n" +
                "\n" +
                "    Z % QR = log(abs(X))\n" +
                "    Z % QI = atan2(X % QI, X % QR)\n" +
                "\n" +
                "    return\n" +
                "  end function QC_LOG");
    }
}
