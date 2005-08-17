/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
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

    // Token categories

    // Numeric-ids for token-ids
    public static final int WHITESPACE_ID = 0; // inside white space
    public static final int LINE_COMMENT_ID = 1; // inside line comment --
    public static final int BLOCK_COMMENT_ID = 2; // inside block comment /* ... */
    public static final int STRING_ID = 3; // inside string constant
    public static final int INCOMPLETE_STRING_ID = 4; // inside string constant after '
    public static final int IDENTIFIER_ID = 5; // inside identifier
    public static final int OPERATOR_ID = 6; // slash char
    public static final int INVALID_CHARACTER_ID = 7; // after '='
    public static final int INVALID_COMMENT_END_ID = 8; // after '0'
    public static final int INT_LITERAL_ID = 9; // integer number
    public static final int DOUBLE_LITERAL_ID = 10; // double number
    public static final int DOT_ID = 11; // after '.'
    public static final int KEYWORD_ID = 12;

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
            new BaseTokenID( "incomplete-string-literal", INCOMPLETE_STRING_ID ); // NOI18N
    public static final BaseTokenID IDENTIFIER = 
            new BaseTokenID( "identifier", IDENTIFIER_ID ); // NOI18N
    public static final BaseTokenID OPERATOR = 
            new BaseTokenID( "operator", OPERATOR_ID ); // NOI18N
    public static final BaseTokenID INVALID_CHARACTER = 
            new BaseTokenID( "invalid-character", INVALID_CHARACTER_ID ); // NOI18N
    public static final BaseTokenID INVALID_COMMENT_END = 
            new BaseTokenID( "invalid-comment-end", INVALID_COMMENT_END_ID ); // NOI18N
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
