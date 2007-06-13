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

package org.netbeans.modules.web.core.xmlsyntax;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;


/**
* Token context for JSP pages with XML content.
*
* @author Petr Jiricka
*/
@Deprecated()
public class JspXMLTokenContext extends TokenContext {

    // Jsp token numericIDs
    public static final int ERROR_ID              =  1;

    /** jsp-error token-id */
    public static final BaseTokenID ERROR = new BaseTokenID("error", ERROR_ID);    // NOI18N

    // Context instance declaration
    public static final JspXMLTokenContext context = new JspXMLTokenContext();

    public static final TokenContextPath contextPath = context.getContextPath();

    /** Path for java tokens in jsp */
    public static final TokenContextPath xmlContextPath
        = context.getContextPath(XMLDefaultTokenContext.contextPath);

    private JspXMLTokenContext() {
        super("jsp-", new TokenContext[] {    // NOI18N
                XMLDefaultTokenContext.context
            }
        );

        try {
            addDeclaredTokenIDs();
        } catch (Exception e) {
            Logger.getLogger("global").log(Level.INFO, null, e);
        }

    }

}

