/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.xmlsyntax;

import org.netbeans.editor.BaseTokenID;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;

import org.netbeans.modules.xml.text.syntax.XMLDefaultTokenContext;

import org.openide.ErrorManager;

/** 
* Token context for JSP pages with XML content.
*
* @author Petr Jiricka
*/

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
            ErrorManager.getDefault ().notify(ErrorManager.INFORMATIONAL, e);
        }

    }

}

