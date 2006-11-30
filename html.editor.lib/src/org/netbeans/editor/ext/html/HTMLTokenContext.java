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

package org.netbeans.editor.ext.html;

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;

/**
* HTML token-context defines token-ids and token-categories
* used in HTML language.
*
* @author Miloslav Metelka
* @version 1.00
*
* @deprecated Use Lexer API instead. See {@link HTMLTokenId}.
*/

public class HTMLTokenContext extends TokenContext {

    // Numeric-ids for token-ids
    public static final int TEXT_ID = 1;
    public static final int WS_ID = TEXT_ID + 1;
    public static final int ERROR_ID = WS_ID + 1;
    public static final int TAG_OPEN_ID = ERROR_ID + 1;
    public static final int TAG_CLOSE_ID = TAG_OPEN_ID + 1; 
    public static final int ARGUMENT_ID = TAG_CLOSE_ID + 1;
    public static final int OPERATOR_ID = ARGUMENT_ID + 1;
    public static final int VALUE_ID = OPERATOR_ID + 1;
    public static final int BLOCK_COMMENT_ID = VALUE_ID + 1;
    public static final int SGML_COMMENT_ID = BLOCK_COMMENT_ID + 1;
    public static final int DECLARATION_ID = SGML_COMMENT_ID + 1;
    public static final int CHARACTER_ID = DECLARATION_ID + 1;
    public static final int EOL_ID = CHARACTER_ID + 1;
    public static final int TAG_OPEN_SYMBOL_ID = EOL_ID + 1; // '<' or '</';
    public static final int TAG_CLOSE_SYMBOL_ID = TAG_OPEN_SYMBOL_ID + 1; // '>' or '/>';

    //token category id for tag tokens
    public static final int TAG_CATEGORY_ID = TAG_CLOSE_SYMBOL_ID + 1;

    /** Token category for all tag tokens. */
    public static final BaseTokenCategory TAG_CATEGORY
        = new BaseTokenCategory("tag", TAG_CATEGORY_ID); // NOI18N

    
    // Token-ids
    /** Plain text */
    public static final BaseTokenID TEXT = new BaseTokenID( "text", TEXT_ID ); // NOI18N
    /** Erroneous Text */
    public static final BaseTokenID WS = new BaseTokenID( "ws", WS_ID ); // NOI18N
    /** Plain Text*/
    public static final BaseTokenID ERROR = new BaseTokenID( "error", ERROR_ID ); // NOI18N
    /** Html Open Tag */
    public static final BaseTokenID TAG_OPEN = new BaseTokenID( "open-tag", TAG_OPEN_ID, TAG_CATEGORY ); // NOI18N
    /** Html Close Tag */
    public static final BaseTokenID TAG_CLOSE = new BaseTokenID( "close-tag", TAG_CLOSE_ID, TAG_CATEGORY ); // NOI18N
    /** Argument of a tag */
    public static final BaseTokenID ARGUMENT = new BaseTokenID( "argument", ARGUMENT_ID ); // NOI18N
    /** Operators - '=' between arg and value */
    public static final BaseTokenID OPERATOR = new BaseTokenID( "operator", OPERATOR_ID ); // NOI18N
    /** Value - value of an argument */
    public static final BaseTokenID VALUE = new BaseTokenID( "value", VALUE_ID ); // NOI18N
    /** Block comment */
    public static final BaseTokenID BLOCK_COMMENT = new BaseTokenID( "block-comment", BLOCK_COMMENT_ID ); // NOI18N
    /** SGML comment - e.g. in DOCTYPE */
    public static final BaseTokenID SGML_COMMENT = new BaseTokenID( "sgml-comment", SGML_COMMENT_ID ); // NOI18N
    /** SGML declaration in HTML document - e.g. <!DOCTYPE> */
    public static final BaseTokenID DECLARATION = new BaseTokenID( "sgml-declaration", DECLARATION_ID ); // NOI18N
    /** Character reference, e.g. &amp;lt; = &lt; */
    public static final BaseTokenID CHARACTER = new BaseTokenID( "character", CHARACTER_ID ); // NOI18N
    /** End of line */
    public static final BaseTokenID EOL = new BaseTokenID( "EOL", EOL_ID ); // NOI18N
    /** Html Tag open symbol: '<' or '</' */
    public static final BaseTokenID TAG_OPEN_SYMBOL = new BaseTokenID("tag-open-symbol", TAG_OPEN_SYMBOL_ID, TAG_CATEGORY); //NOI18N
    /** Html Tag close symbol: '>' or '/>' */
    public static final BaseTokenID TAG_CLOSE_SYMBOL = new BaseTokenID("tag-close-symbol", TAG_CLOSE_SYMBOL_ID, TAG_CATEGORY); //NOI18N

    // Context instance declaration
    public static final HTMLTokenContext context = new HTMLTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();


    private HTMLTokenContext() {
        super("html-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Utilities.annotateLoggable(e);
        }

    }

}
