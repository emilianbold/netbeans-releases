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

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

/**
* Various extensions to the displaying of the EL tokens
* is defined here. The tokens defined here are used
* by the el-drawing-layer.
*
* @author Petr Pisl
* @deprecated Will be replaced by Semantic Coloring
*/

public class ELLayerTokenContext extends TokenContext {

    // Token category-ids

    // Numeric-ids for token-ids
    public static final int METHOD_ID     = 1;



    // Token-ids
    public static final BaseTokenID METHOD = new BaseTokenID("method", METHOD_ID);

    // Context instance declaration
    public static final ELLayerTokenContext context = new ELLayerTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();



    private ELLayerTokenContext() {
        super("jsp-el-layer-");

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }

    }

}

