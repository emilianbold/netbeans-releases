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

package org.netbeans.modules.cnd.apt.impl.support;

import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenAbstact;

/**
 *
 * @author gorrus
 */
public class APTCommentToken extends APTTokenAbstact {
    /** Creates a new instance of APTCommentToken */
    public APTCommentToken() {
    }
    
    protected int type = INVALID_TYPE; // we have two kinds of comments :(
    protected int offset = 0;
    protected int length;
    protected int line;
    protected int column;
    protected int endLine;

    public String getText() {
        return "<comment text skipped>"; // NOI18N
    }
    
    public int getEndOffset() {
        return offset + length;
    }

    public void setOffset(int o) {
        offset = o;
    }

    public int getOffset() {
        return offset;
    }

    public void setType(int t) {
        type = t;
    }

    public int getType() {
        return type;
    }

    public void setColumn(int c) {
        column = c;
    }

    public void setLine(int l) {
        line = l;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public void setEndLine(int l) {
        this.endLine = l;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public int getEndColumn() {
        return getColumn() + length;
    }

    public void setText(String t) {
        length = t.length();
    }

}
