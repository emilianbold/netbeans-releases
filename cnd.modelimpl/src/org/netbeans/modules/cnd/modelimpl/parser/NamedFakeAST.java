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

package org.netbeans.modules.cnd.modelimpl.parser;

import antlr.Token;
import antlr.collections.AST;
import java.io.Serializable;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.utils.APTStringManager;

/**
 * Fake AST managing text
 * @author Vladimir Voskresensky
 */
public class NamedFakeAST extends FakeAST implements Serializable {
    private static final long serialVersionUID = 3949611279758335361L;
    
    private String text;
    
    public NamedFakeAST() {
    }

    /** Set the token text for this node */
    public void setText(String text_) {
        if (APTTraceFlags.APT_SHARE_TEXT) {
            text_ = APTStringManager.instance().getString(text_);
        }
        text = text_;
    } 

    /** Get the token text for this node */
    public String getText() {
        return text;
    }

    public void initialize(int t, String txt) {
        setType(t);
        setText(txt);
    }

    public void initialize(AST t) {
        setText(t.getText());
        setType(t.getType());
    }

    public void initialize(Token tok) {
        setText(tok.getText());
        setType(tok.getType());
    }
}
