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

package org.netbeans.modules.web.core.syntax.formatting;

import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.editor.ext.java.JavaFormatSupport;
import org.netbeans.editor.ext.java.JavaTokenContext;

/**
 * FormatSupport for scripting in  jsp and tag files.
 * @author Petr Pisl
 *
 */

//marek: Is this class really used now?

public class JspJavaFormatSupport extends JavaFormatSupport{

    /** Creates a new instance of JspJavaFormatSupport */
    public JspJavaFormatSupport(FormatWriter formatWriter, TokenContextPath tokenContextPath) {
        super(formatWriter, tokenContextPath);
    }
    
    public boolean canModifyWhitespace(TokenItem inToken) {
        if (inToken.getTokenContextPath() == getTokenContextPath()) {
            switch (inToken.getTokenID().getNumericID()) {
                case JavaTokenContext.BLOCK_COMMENT_ID:
                case JavaTokenContext.WHITESPACE_ID:
                    return true;
            }
        }

        return false;
    }
}
