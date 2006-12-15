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

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.BaseTokenID;
import org.openide.ErrorManager;

/**
* Syntax Token Context class for JSP directives. Tokens from this token context
* are used for jsp directives. The only difference between them is that directive
* tokens has a special token context category.
*
* @author Marek Fukala
* @deprecated Use Jsp Lexer instead.
*/

public class JspDirectiveTokenContext extends JspTagTokenContext {

    //token ids - are inherited from superclass
    
    //token category ids
    public static final int TAG_CATEGORY_ID = WHITESPACE_ID + 1;

    //token category for jsp directives
    public static final TokenCategory tokenCategory = new BaseTokenCategory("directive", TAG_CATEGORY_ID); // NOI18N

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
    public static final JspDirectiveTokenContext context = new JspDirectiveTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    JspDirectiveTokenContext() {
        super();   // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

    }
    
}

