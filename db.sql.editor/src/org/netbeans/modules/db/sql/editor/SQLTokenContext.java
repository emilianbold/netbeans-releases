/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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

package org.netbeans.modules.db.sql.editor;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

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
            Logger.getLogger (SQLTokenContext.class.getName ()).log (Level.INFO, e.getLocalizedMessage (), e);
        }

    }
}
