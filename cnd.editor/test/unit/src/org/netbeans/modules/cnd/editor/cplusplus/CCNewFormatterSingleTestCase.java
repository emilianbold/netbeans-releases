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

package org.netbeans.modules.cnd.editor.cplusplus;

import javax.swing.text.BadLocationException;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.options.EditorOptions;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;

/**
 *
 * @author as204739
 */
public class CCNewFormatterSingleTestCase extends CCFormatterBaseUnitTestCase {

    public CCNewFormatterSingleTestCase(String testMethodName) {
        super(testMethodName);
    }

    /**
     * Perform reformatting of the whole document's text.
     */
    @Override
    protected void reformat() {
        Reformatter f = new Reformatter(getDocument(), CodeStyle.getDefault(getDocument()));
        try {
            f.reformat();
        } catch (BadLocationException e) {
            e.printStackTrace(getLog());
            fail(e.getMessage());
	}
    }

    private void setDefaultsOptions(){
        EditorOptions.resetToDefault(CodeStyle.getDefault(CodeStyle.Language.CPP));
    }

//    public void testIdentMultyConstructor3() {
//        setDefaultsOptions();
//        setLoadDocumentText(
//            "class IndexReader : LUCENE_BASE\n" +
//            "{\n" +
//            "public:\n" +
//            "class IndexReaderCommitLockWith : \n" +
//            "public CL_NS(store)::LuceneLockWith\n" +
//            "{\n" +
//            "private:\n" +
//            "IndexReader* reader;\n" +
//            "};\n" +
//            "};\n"
//            );
//        reformat();
//        assertDocumentText("Incorrect identing multyline constructor",
//            "class IndexReader : LUCENE_BASE\n" +
//            "{\n" +
//            "public:\n" +
//            "    class IndexReaderCommitLockWith :\n" +
//            "    public CL_NS(store)::LuceneLockWith\n" +
//            "    {\n" +
//            "    private:\n" +
//            "        IndexReader* reader;\n" +
//            "    };\n" +
//            "};\n"
//        );
//    }

//    public void testCaseIndentAftePreprocessor() {
//        setDefaultsOptions();
//        setLoadDocumentText(
//            " C_MODE_START\n" +
//            "#    include <decimal.h>\n" +
//            "        C_MODE_END\n" +
//            "\n" +
//            "#    define DECIMAL_LONGLONG_DIGITS 22\n" +
//            "\n" +
//            "\n" +
//            "        /* maximum length of buffer in our big digits (uint32) */\n" +
//            "#    define DECIMAL_BUFF_LENGTH 9\n" +
//            "        /*\n" +
//            "        point on the border of our big digits))\n" +
//            "*/\n" +
//            "#    define DECIMAL_MAX_PRECISION ((DECIMAL_BUFF_LENGTH * 9) - 8*2)\n" +
//            "\n"
//            );
//        reformat();
//        assertDocumentText("Incorrect identing case after preprocessor",
//            "C_MODE_START\n" +
//            "#include <decimal.h>\n" +
//            "C_MODE_END\n" +
//            "\n" +
//            "#define DECIMAL_LONGLONG_DIGITS 22\n" +
//            "\n" +
//            "\n" +
//            "/* maximum length of buffer in our big digits (uint32) */\n" +
//            "#define DECIMAL_BUFF_LENGTH 9\n" +
//            "/*\n" +
//            "point on the border of our big digits))\n" +
//            "*/\n" +
//            "#define DECIMAL_MAX_PRECISION ((DECIMAL_BUFF_LENGTH * 9) - 8*2)\n" +
//            "\n"
//        );
//    }
//

    public void testReformatMultiLineClassDeclaration() {
        setDefaultsOptions();
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "}\n" +
                "/*\n" +
                "* Call this when vim starts up, whether or not the GUI is started\n" +
                " */\n" +
                "void\n" +
                "gui_prepare(argc)\n" +
                "    int *argc;\n" +
                "{\n" +
                "}\n"
                );
        reformat();
        assertDocumentText("Incorrect new-line indent",
                "int foo()\n" +
                "{\n" +
                "}\n" +
                "\n" +
                "/*\n" +
                " * Call this when vim starts up, whether or not the GUI is started\n" +
                " */\n" +
                "void\n" +
                "gui_prepare(argc)\n" +
                "int *argc;\n" +
                "{\n" +
                "}\n"
                );
    }
}
