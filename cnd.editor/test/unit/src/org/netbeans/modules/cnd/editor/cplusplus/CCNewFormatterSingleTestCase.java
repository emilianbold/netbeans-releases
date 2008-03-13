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

//    public void testIdentMultyConstructor5() {
//        setDefaultsOptions();
//        setLoadDocumentText(
//            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" +
//            "        ulong query_length, bool using_trans,\n" +
//            "        bool suppress_use)\n" +
//            ":Log_event(thd_arg,\n" +
//            "        ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
//            "        & (suppress_use          ? LOG_EVENT_SUPPRESS_USE_F    : 0)),\n" +
//            "                using_trans),\n" +
//            "                data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
//            "                db(thd_arg->db), q_len((uint32) query_length),\n" +
//            "                error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
//            "                    ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
//            "                        0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
//            "                                thread_id(thd_arg->thread_id),\n" +
//            "                                /* save the original thread id; we already know the server id */\n" +
//            "                                slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
//            "                                flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
//            "                                sql_mode(thd_arg->variables.sql_mode),\n" +
//            "                                auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
//            "                                auto_increment_offset(thd_arg->variables.auto_increment_offset)\n" +
//            "                        {\n" +
//            "                            time_t end_time;\n" +
//            "                        }\n"
//            );
//        reformat();
//        assertDocumentText("Incorrect identing multyline constructor",
//            "Query_log_event::Query_log_event(THD* thd_arg, const char* query_arg,\n" +
//            "        ulong query_length, bool using_trans,\n" +
//            "        bool suppress_use)\n" +
//            ": Log_event(thd_arg,\n" +
//            "        ((thd_arg->tmp_table_used ? LOG_EVENT_THREAD_SPECIFIC_F : 0)\n" +
//            "        & (suppress_use ? LOG_EVENT_SUPPRESS_USE_F : 0)),\n" +
//            "        using_trans),\n" +
//            "        data_buf(0), query(query_arg), catalog(thd_arg->catalog),\n" +
//            "        db(thd_arg->db), q_len((uint32) query_length),\n" +
//            "        error_code((thd_arg->killed != THD::NOT_KILLED) ?\n" +
//            "            ((thd_arg->system_thread & SYSTEM_THREAD_DELAYED_INSERT) ?\n" +
//            "                 0 : thd->killed_errno()) : thd_arg->net.last_errno),\n" +
//            "        thread_id(thd_arg->thread_id),\n" +
//            "        /* save the original thread id; we already know the server id */\n" +
//            "        slave_proxy_id(thd_arg->variables.pseudo_thread_id),\n" +
//            "        flags2_inited(1), sql_mode_inited(1), charset_inited(1),\n" +
//            "        sql_mode(thd_arg->variables.sql_mode),\n" +
//            "        auto_increment_increment(thd_arg->variables.auto_increment_increment),\n" +
//            "        auto_increment_offset(thd_arg->variables.auto_increment_offset) {\n" +
//            "    time_t end_time;\n" +
//            "}\n"
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


//What about []:
//        if (lens[sym] != 0) work[offs[lens[sym]]++] = (unsigned short)sym;
//

    
    public void testTryCatchHalf() {
        setDefaultsOptions();
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                putBoolean(EditorOptions.newLineCatch, true);
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBrace, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        EditorOptions.getPreferences(CodeStyle.getDefault(CodeStyle.Language.CPP)).
                put(EditorOptions.newLineBeforeBraceDeclaration, 
                CodeStyle.BracePlacement.NEW_LINE_HALF_INDENTED.name());
        setLoadDocumentText(
                "int foo()\n" +
                "{\n" +
                "  if (strcmp (TREE_STRING_POINTER (id), \"default\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_DEFAULT;  // comment\n" +
                "  else if (strcmp (TREE_STRING_POINTER (id), \"hidden\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_HIDDEN;  \n" +
                "  else if (strcmp (TREE_STRING_POINTER (id), \"protected\") == 0)\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_PROTECTED;   /* comment */   \n" +
                "  else\n" +
                "    DECL_VISIBILITY (decl) = VISIBILITY_PROTECTED;\n" +
                "}\n");
        reformat();
        assertDocumentText("Incorrect formatting try-catch half indent",
                "int foo()\n" +
                "{\n" +
                "  if (strcmp(TREE_STRING_POINTER(id), \"default\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_DEFAULT; // comment\n" +
                "  else if (strcmp(TREE_STRING_POINTER(id), \"hidden\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_HIDDEN;\n" +
                "  else if (strcmp(TREE_STRING_POINTER(id), \"protected\") == 0)\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_PROTECTED; /* comment */\n" +
                "  else\n" +
                "    DECL_VISIBILITY(decl) = VISIBILITY_PROTECTED;\n" +
                "}\n");
    }
}