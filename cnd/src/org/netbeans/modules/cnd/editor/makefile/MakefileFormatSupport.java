/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.makefile;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtFormatSupport;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.FormatWriter;

/**
 *
 * @author gordonp
 */
public class MakefileFormatSupport extends ExtFormatSupport {
    
    private TokenContextPath tokenContextPath;
    
    /** Creates a new instance of MakefileFormatSupport */
    public MakefileFormatSupport(FormatWriter fw) {
        this(fw, MakefileTokenContext.contextPath);
    }
    
    public MakefileFormatSupport(FormatWriter fw, TokenContextPath tokenContextPath) {
        super(fw);
        this.tokenContextPath = tokenContextPath;
    }
    
    public FormatTokenPosition indentLine(FormatTokenPosition pos) {
        TokenItem token = pos.getToken();
        
        if (isRuleOrActionLine(token)) {
            return changeLineIndent(pos, 8);
        } else {
            return pos;
        }
    }
    
    private boolean isRuleOrActionLine(TokenItem token) {
        
        if (token == null) {    // an empty line...
            JTextComponent tc = Utilities.getFocusedComponent();
            BaseDocument doc = Utilities.getDocument(tc);
            int dot = tc.getCaret().getDot();
            try {
                int start = Utilities.getRowStart(doc, dot - 1);
                String line = doc.getText(start, dot - start);
                int colon = line.indexOf(':');
                int pound = line.indexOf('#');
                if (line.charAt(0) == '\t' ||
                        (colon > 0 && pound == -1) || (colon > 0 && colon < pound)) {
                    return true;
                }
            } catch (BadLocationException ex) {
            }
        }
        return false;
    }
    
    public TokenID getWhitespaceTokenID() {
        return MakefileTokenContext.TAB;
    }
    
    public TokenContextPath getWhitespaceTokenContextPath() {
        return tokenContextPath;
    }
}
