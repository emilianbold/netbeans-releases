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

package org.netbeans.modules.properties.syntax;

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Token-ids and token-categories defined
* for the properties syntax.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class PropertiesTokenContext extends TokenContext {

    // Token numeric-IDs
    public static final int TEXT_ID         = 1; // plain text
    public static final int LINE_COMMENT_ID = 2; // line comment
    public static final int KEY_ID          = 3; // key
    public static final int EQ_ID           = 4; // equal-sign
    public static final int VALUE_ID        = 5; // value
    public static final int EOL_ID          = 6; // EOL

    // TokenIDs
    public static final BaseTokenID TEXT
    = new BaseTokenID("text", TEXT_ID);
    public static final BaseTokenID LINE_COMMENT
    = new BaseTokenID("line-comment", LINE_COMMENT_ID);
    public static final BaseTokenID KEY
    = new BaseTokenID("key", KEY_ID);
    public static final BaseTokenID EQ
    = new BaseTokenID("equal-sign", EQ_ID);
    public static final BaseTokenID VALUE
    = new BaseTokenID("value", VALUE_ID);
    public static final BaseTokenID EOL
    = new BaseTokenID("EOL", EOL_ID);


    // Context instance declaration
    public static final PropertiesTokenContext context = new PropertiesTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    private PropertiesTokenContext() {
        super("properties-");

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

    }

}

