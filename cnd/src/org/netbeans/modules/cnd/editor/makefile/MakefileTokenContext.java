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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.makefile;

import java.util.HashMap;
import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Makefile token-context defines token-ids and token-categories
* used in Makefiles.
*
*/

public class MakefileTokenContext extends TokenContext {

    private static int id = 1;

    // Token category
    public static final BaseTokenCategory TC_MACROS =
	new BaseTokenCategory("macros", id++); // NOI18N
    public static final BaseTokenCategory TC_MACRO_OPERATORS =
	new BaseTokenCategory("macro-operators", id++); // NOI18N
    public static final BaseTokenCategory TC_RULES =
	new BaseTokenCategory("rules", id++); // NOI18N
    public static final BaseTokenCategory TC_GLOBAL =
	new BaseTokenCategory("global-special-characters", id++); // NOI18N
    public static final BaseTokenCategory TC_TARGET =
	new BaseTokenCategory("targets", id++); // NOI18N
    public static final BaseTokenCategory TC_ERRORS =
	new BaseTokenCategory("errors", id++); // NOI18N

    // Token IDs
    public static final BaseTokenID IDENTIFIER =
	new BaseTokenID("identifier", id++); // NOI18N
    public static final BaseTokenID WHITESPACE =
	new BaseTokenID("whitespace", id++); // NOI18N
    public static final BaseTokenID TAB =
	new BaseTokenID("tab", id++); // NOI18N
    public static final BaseTokenID LINE_COMMENT =
	new BaseTokenID("line-comment", id++); // NOI18N
    public static final BaseTokenID STRING_LITERAL =
	new BaseTokenID("string-literal", id++); // NOI18N

    // Macros
    public static final BaseTokenID MACRO_LITERAL =
	new BaseTokenID("macro-literal", id++, TC_MACROS); // NOI18N
    public static final BaseImageTokenID MACRO_DOLLAR =
	new BaseImageTokenID("dollar", id++, TC_MACROS, "$"); // NOI18N
    public static final BaseImageTokenID MACRO_DOLAR_REFERENCE =
	new BaseImageTokenID("dollar-reference", id++, TC_MACROS, "$$"); // NOI18N
    public static final BaseImageTokenID MACRO_ESCAPED_DOLLAR =
	new BaseImageTokenID("escaped-currency", id++, TC_MACROS, "\\$"); // NOI18N
    public static final BaseImageTokenID MACRO_LPAREN =
	new BaseImageTokenID("left-paran", id++, TC_MACROS, "("); // NOI18N
    public static final BaseImageTokenID MACRO_RPAREN =
	new BaseImageTokenID("right-paran", id++, TC_MACROS, ")"); // NOI18N
    public static final BaseImageTokenID MACRO_LBRACE =
	new BaseImageTokenID("left-brace", id++, TC_MACROS, "{"); // NOI18N
    public static final BaseImageTokenID MACRO_RBRACE =
	new BaseImageTokenID("right-brace", id++, TC_MACROS, "}"); // NOI18N
    public static final BaseImageTokenID MACRO_COMMAND_SUBSTITUTE =
	new BaseImageTokenID("command substitute", id++, TC_MACROS, ":sh"); // NOI18N
    public static final BaseImageTokenID MACRO_DYN_TARGET_BASENAME =
	new BaseImageTokenID("target-basename", id++, TC_MACROS, "$*"); // NOI18N
    public static final BaseImageTokenID MACRO_DYN_DEPENDENCY_FILENAME =
	new BaseImageTokenID("dependency-filename", id++, TC_MACROS, "$<"); // NOI18N
    public static final BaseImageTokenID MACRO_DYN_CURRENTTARGET =
	new BaseImageTokenID("current-target", id++, TC_MACROS, "$@"); // NOI18N
    public static final BaseImageTokenID MACRO_DYN_DEPENDENCY_LIST =
	new BaseImageTokenID("dependency-list", id++, TC_MACROS, "$?"); // NOI18N
    public static final BaseImageTokenID MACRO_DYN_LIBRARYNAME =
	new BaseImageTokenID("library-name", id++, TC_MACROS, "$%"); // NOI18N

    // Macro Operators
    public static final BaseImageTokenID MACRO_OP_EQUALS =
	new BaseImageTokenID("equals", id++, TC_MACRO_OPERATORS, "="); // NOI18N
    public static final BaseImageTokenID MACRO_OP_APPEND =
	new BaseImageTokenID("append", id++, TC_MACRO_OPERATORS, "+="); // NOI18N
    public static final BaseImageTokenID MACRO_OP_CONDITIONAL =
	new BaseImageTokenID("conditional", id++, TC_MACRO_OPERATORS, ":="); // NOI18N

    // Rules
    public static final BaseImageTokenID RULES_PLUS =
	new BaseImageTokenID("plus", id++, TC_RULES, "+"); // NOI18N
    public static final BaseImageTokenID RULES_MINUS =
	new BaseImageTokenID("minus", id++, TC_RULES, "-"); // NOI18N
    public static final BaseImageTokenID RULES_AT =
	new BaseImageTokenID("at", id++, TC_RULES, "@"); // NOI18N
    public static final BaseImageTokenID RULES_QUESTION_MARK =
	new BaseImageTokenID("question-mark", id++, TC_RULES, "?"); // NOI18N
    public static final BaseImageTokenID RULES_EXCLAMATION =
	new BaseImageTokenID("exclamation", id++, TC_RULES, "!"); // NOI18N

    
    /**
     * SC_POUND in here is just a place holder to show that it is a special character
     * the syntax highlighting for exclamations are handled in LINE_COMMENT
     */
    public static final BaseImageTokenID GLOBAL_POUND =
	new BaseImageTokenID("pound", id++, TC_GLOBAL, "#"); // NOI18N
    public static final BaseImageTokenID GLOBAL_INCLUDE =
	new BaseImageTokenID("include", id++, TC_GLOBAL); // NOI18N

    // Targets and Dependencies
    public static final BaseImageTokenID TARGET_COLON =
	new BaseImageTokenID("colon", id++, TC_TARGET, ":"); // NOI18N
    public static final BaseImageTokenID TARGET_DOUBLE_COLON =
	new BaseImageTokenID("double-colon", id++, TC_TARGET, "::"); // NOI18N
    public static final BaseImageTokenID TARGET_PERCENT =
	new BaseImageTokenID("percent", id++, TC_TARGET, "%"); // NOI18N

    public static final BaseImageTokenID TARGET_DEFAULT =
	new BaseImageTokenID(".DEFAULT", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_DONE =
	new BaseImageTokenID(".DONE", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_FAILED =
	new BaseImageTokenID(".FAILED", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_GETPOSIX =
	new BaseImageTokenID(".GET_POSIX", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_IGNORE =
	new BaseImageTokenID(".IGNORE", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_INIT =
	new BaseImageTokenID(".INIT", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_KEEPSTATE =
	new BaseImageTokenID(".KEEP_STATE", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_KEEPSTATEFILE =
	new BaseImageTokenID(".KEEP_STATE_FILE", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_MAKEVERSION =
	new BaseImageTokenID(".MAKE_VERSION", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_NOPARALLEL =
	new BaseImageTokenID(".NO_PARALLEL", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_PARALLEL =
	new BaseImageTokenID(".PARALLEL", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_POSIX =
	new BaseImageTokenID(".POSIX", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_PRECIOUS =
	new BaseImageTokenID(".PRECIOUS", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_SCCSGET =
	new BaseImageTokenID(".SCCS_GET", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_SCCSGETPOSIX =
	new BaseImageTokenID(".SCCS_GET_POSIX", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_SILENT =
	new BaseImageTokenID(".SILENT", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_SUFFIXES =
	new BaseImageTokenID(".SUFFIXES", id++, TC_TARGET); // NOI18N
    public static final BaseImageTokenID TARGET_WAIT =
	new BaseImageTokenID(".WAIT", id++, TC_TARGET); // NOI18N


    // Errors
    public static final BaseTokenID ERR_INCOMPLETE_MACRO_LITERAL =
	new BaseTokenID("incomplete-macro-literal", id++, TC_ERRORS); // NOI18N
                        
    // Context instance declaration
    public static final MakefileTokenContext context = new MakefileTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private MakefileTokenContext() {
        super("makefile-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { //NOI18N
                e.printStackTrace();
            }
        }

    }

}

