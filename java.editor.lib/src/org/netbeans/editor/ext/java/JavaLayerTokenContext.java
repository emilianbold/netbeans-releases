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

