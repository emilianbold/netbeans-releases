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

package org.netbeans.editor.ext.java;

import org.netbeans.editor.BaseTokenCategory;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.BaseImageTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.Utilities;

/**
* Various extensions to the displaying of the java tokens
* is defined here. The tokens defined here are used
* by the java-drawing-layer.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaLayerTokenContext extends TokenContext {

    // Token category-ids

    // Numeric-ids for token-ids
    public static final int METHOD_ID     = 1;

    // Token-categories
//    public static final BaseTokenCategory KEYWORDS
//    = new BaseTokenCategory("keywords", KEYWORDS_ID);


    // Token-ids
    public static final BaseTokenID METHOD
    = new BaseTokenID("method", METHOD_ID); // NOI18N

    // Context instance declaration
    public static final JavaLayerTokenContext context = new JavaLayerTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();


    private JavaLayerTokenContext() {
        super("java-layer-"); // NOI18N

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Utilities.annotateLoggable(e);
        }

    }

}

