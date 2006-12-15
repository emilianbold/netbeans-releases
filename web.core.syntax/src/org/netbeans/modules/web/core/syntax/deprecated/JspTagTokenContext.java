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

package org.netbeans.modules.web.core.syntax.deprecated;

import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.BaseTokenID;
import org.openide.ErrorManager;

/**
* Syntax class for JSP tags. It is not meant to be used by itself, but as one of syntaxes with
* MultiSyntax. Recognizes JSP tags, comments and directives. Does not recognize scriptlets,
* expressions and declarations, which should be rocognized by the master syntax, as expressions
* can appear embedded in a JSP tag. Moreover, they all share Java syntax.
*
* @author Petr Jiricka
* @deprecated Use JSP Lexer instead
*/

public class JspTagTokenContext extends TokenContext {

    //there is not any token category for jsp tags
    //TODO - consider whether there is a need to create a category for jsp tags
    public static final TokenCategory tokenCategory = null;
    
    // Numeric-ids
    public static final int TEXT_ID = 1;
    public static final int ERROR_ID = 2;
    public static final int TAG_ID = 3;
    public static final int SYMBOL_ID = 4;
    public static final int SYMBOL2_ID = 5;
    public static final int COMMENT_ID = 6;
    public static final int ATTRIBUTE_ID = 7;
    public static final int ATTR_VALUE_ID = 8;
    public static final int EOL_ID = 9;
    public static final int AFTER_UNEXPECTED_LT_ID = 10;
    public static final int WHITESPACE_ID = 11;
    
    

    // TokenIDs
    public static final BaseTokenID TEXT = new BaseTokenID("text", TEXT_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID ERROR = new BaseTokenID("error", ERROR_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID TAG = new BaseTokenID("tag-directive", TAG_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID SYMBOL = new BaseTokenID("symbol", SYMBOL_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID SYMBOL2 = new BaseTokenID("scriptlet-delimiter", SYMBOL2_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID COMMENT = new BaseTokenID("comment", COMMENT_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID ATTRIBUTE = new BaseTokenID("attribute-name", ATTRIBUTE_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID ATTR_VALUE = new BaseTokenID("attribute-value", ATTR_VALUE_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID EOL = new BaseTokenID("EOL", EOL_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID AFTER_UNEXPECTED_LT = new BaseTokenID("AFTER_UNEXPECTED_LT", AFTER_UNEXPECTED_LT_ID, tokenCategory);   // NOI18N
    public static final BaseTokenID WHITESPACE = new BaseTokenID("whitespace", WHITESPACE_ID, tokenCategory);   // NOI18N
    

    // Context instance declaration
    public static final JspTagTokenContext context = new JspTagTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();


    JspTagTokenContext() {
        super("jsptag-");   // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

    }

}

