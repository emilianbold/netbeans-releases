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

import org.netbeans.modules.web.core.syntax.deprecated.JspJavaFakeTokenContext;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.html.HTMLTokenContext;
import org.netbeans.editor.ext.plain.PlainTokenContext;
import org.netbeans.modules.web.core.syntax.deprecated.ELTokenContext;
import org.openide.ErrorManager;

/**
* Token context for JSP.
*
* @author Miloslav Metelka
* @deprecated Use JSP Lexer instead
*/

public class JspMultiTokenContext extends TokenContext {

    // Jsp token numericIDs
    public static final int ERROR_ID              =  1;

    /** jsp-error token-id */
    public static final BaseTokenID ERROR = new BaseTokenID("error", ERROR_ID); // NOI18N

    // Context instance declaration
    public static final JspMultiTokenContext context = new JspMultiTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    /** Path for jsp-tags in jsp */
    public static final TokenContextPath jspTagContextPath
        = context.getContextPath(JspTagTokenContext.contextPath);

    /** Path for Expression Language tokens in jsp */
    public static final TokenContextPath elContextPath
        = context.getContextPath(ELTokenContext.contextPath);

    /** Path for java tokens in jsp */
    public static final TokenContextPath javaScriptletContextPath
        = context.getContextPath(JspJavaFakeTokenContext.JavaScriptletTokenContext.contextPath);

    public static final TokenContextPath javaExpressionContextPath
        = context.getContextPath(JspJavaFakeTokenContext.JavaExpressionTokenContext.contextPath);

    public static final TokenContextPath javaDeclarationContextPath
        = context.getContextPath(JspJavaFakeTokenContext.JavaDeclarationTokenContext.contextPath);

    
    /** Path for HTML tokens in jsp */
    public static final TokenContextPath htmlContextPath
        = context.getContextPath(HTMLTokenContext.contextPath);

    /** Path for plain tokens in jsp */
    public static final TokenContextPath plainContextPath
        = context.getContextPath(PlainTokenContext.contextPath);


    private JspMultiTokenContext() {
        super("jsp-", new TokenContext[] {  // NOI18N
                JspTagTokenContext.context,
                ELTokenContext.context,
                JspJavaFakeTokenContext.JavaScriptletTokenContext.context,
                JspJavaFakeTokenContext.JavaDeclarationTokenContext.context,
                JspJavaFakeTokenContext.JavaExpressionTokenContext.context,
                HTMLTokenContext.context,
                PlainTokenContext.context
            }
        );

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            ErrorManager.getDefault ().notify (ErrorManager.INFORMATIONAL, e);
        }

    }

}

