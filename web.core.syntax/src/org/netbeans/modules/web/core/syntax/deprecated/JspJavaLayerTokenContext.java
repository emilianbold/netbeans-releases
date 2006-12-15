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

import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.java.JavaLayerTokenContext;

/** Helper class for token context for Java methods (layer above the lexical layer provided by the JavaSyntax class.
* This is necessary to make java method coloring work properly in JSP
*
* @author Petr Jiricka
* @deprecated Will be replaced by Semantic Coloring
*/

public class JspJavaLayerTokenContext extends TokenContext {

    private JspJavaLayerTokenContext() {
        super("jsp-", new TokenContext[] { JavaLayerTokenContext.context } );   // NOI18N
    }

    public static final JspJavaLayerTokenContext context = new JspJavaLayerTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

}

