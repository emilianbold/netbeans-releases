/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.db.sql.editor;

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;

/**
* SQL token-context defines token-ids and token-categories
* used in SQL language.
*
* @author Jesse Beaumont based on code by Miloslav Metelka
*/

public class SQLTokenContext extends TokenContext {

    // Numeric-ids for token categories
    public static final int ERRORS_ID = 0; // errors

    // Numeric-ids for token-ids
    public static final int WHITESPACE_ID = ERRORS_ID + 1; // inside white space
    public static final int LINE_COMMENT_ID = WHITESPACE_ID + 1; // inside line comment --
    public static final int BLOCK_COMMENT_ID = LINE_COMMENT_ID + 1; // inside block comment /* ... */
    public static final int STRING_ID = BLOCK_COMMENT_ID + 1; // inside string constant
    public static final int INCOMPLETE_STRING_ID = STRING_ID + 1; // inside string constant after '
    public static final int IDENTIFIER_ID = INCOMPLETE_STRING_ID + 1; // inside identifier
    public static final int OPERATOR_ID = IDENTIFIER_ID + 1; // slash char
    public static final int INVALID_COMMENT_END_ID = OPERATOR_ID + 1; // after '0'
    public static final int INT_LITERAL_ID = INVALID_COMMENT_END_ID + 1; // integer number
    public static final int DOUBLE_LITERAL_ID = INT_LITERAL_ID + 1; // double number
    public static final int DOT_ID = DOUBLE_LITERAL_ID + 1; // after '.'
    public static final int KEYWORD_ID = DOT_ID + 1;
    
    // Token categories
    public static final BaseTokenCategory ERRORS = 
            new BaseTokenCategory("errors", ERRORS_ID); // NOI18N
    
    // Token-ids
    public static final BaseTokenID WHITESPACE = 
            new BaseTokenID( "whitespace", WHITESPACE_ID ); // NOI18N
    public static final BaseTokenID LINE_COMMENT = 
            new BaseTokenID( "line-comment", LINE_COMMENT_ID ); // NOI18N
    public static final BaseTokenID BLOCK_COMMENT = 
            new BaseTokenID( "block-comment", BLOCK_COMMENT_ID ); // NOI18N
    public static final BaseTokenID STRING = 
            new BaseTokenID( "string-literal", STRING_ID ); // NOI18N
    public static final BaseTokenID INCOMPLETE_STRING = 
            new BaseTokenID( "incomplete-string-literal", INCOMPLETE_STRING_ID, ERRORS ); // NOI18N
    public static final BaseTokenID IDENTIFIER = 
            new BaseTokenID( "identifier", IDENTIFIER_ID ); // NOI18N
    public static final BaseTokenID OPERATOR = 
            new BaseTokenID( "operator", OPERATOR_ID ); // NOI18N
    public static final BaseTokenID INVALID_COMMENT_END = 
            new BaseTokenID( "invalid-comment-end", INVALID_COMMENT_END_ID, ERRORS ); // NOI18N
    public static final BaseTokenID INT_LITERAL = 
            new BaseTokenID( "int-literal", INT_LITERAL_ID ); // NOI18N
    public static final BaseTokenID DOUBLE_LITERAL = 
            new BaseTokenID( "double-literal", DOUBLE_LITERAL_ID ); // NOI18N
    public static final BaseTokenID DOT = 
            new BaseTokenID( "dot", DOT_ID ); // NOI18N
    public static final BaseTokenID KEYWORD = 
            new BaseTokenID( "keyword", KEYWORD_ID ); // NOI18N
        
    // Context instance declaration
    public static final SQLTokenContext context = new SQLTokenContext();
    public static final TokenContextPath contextPath = context.getContextPath();

    /**
     * Constructs a new SQLTokenContext
     */
    private SQLTokenContext() {
        super("sql-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Utilities.annotateLoggable(e);
        }

    }
}
