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

import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;

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

    public void testProgramIndent() {
        setLoadDocumentText(
                "program p|"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line program indent",
                "program p\n" +
                "    |"
                );
    }

    public void testEndProgramIndent() {
        setLoadDocumentText(
                "program p\n"+
                "    end progra|"
                );
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect new-line end program indent",
                "program p\n" +
                "end program|"
                );
    }

    public void testEndProgramIndent2() {
        setLoadDocumentText(
                "program p\n"+
                "    endprogra|"
                );
        typeChar('m', true);
        assertDocumentTextAndCaret("Incorrect new-line end program indent",
                "program p\n" +
                "endprogram|"
                );
    }

    public void testSubroutineIndent() {
        setLoadDocumentText(
                "subroutine p(c)|"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line subroutine indent",
                "subroutine p(c)\n"+
                "    |"
                );
    }

    public void testEndSubroutineIndent() {
        setLoadDocumentText(
                "subroutine p(c)\n"+
                "    end subroutin|"
                );
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect new-line emd subroutine indent",
                "subroutine p(c)\n"+
                "end subroutine|"
                );
    }

    public void testEndSubroutineIndent2() {
        setLoadDocumentText(
                "subroutine p(c)\n"+
                "    endsubroutin|"
                );
        typeChar('e', true);
        assertDocumentTextAndCaret("Incorrect new-line end subroutine indent",
                "subroutine p(c)\n"+
                "endsubroutine|"
                );
    }

    public void testIfIndent() {
        setLoadDocumentText(
                "if (a .eq. 0) then|"
                );
        indentNewLine();
        assertDocumentTextAndCaret("Incorrect new-line if indent",
                "if (a .eq. 0) then\n"+
                "    |"
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
