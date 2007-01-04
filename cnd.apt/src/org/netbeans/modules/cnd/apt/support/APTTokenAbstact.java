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

package org.netbeans.modules.cnd.apt.support;

/**
 *
 * @author gorrus
 */
public abstract class APTTokenAbstact implements APTToken {
    public int getOffset() {return -1;};
    public void setOffset(int o) {};
    
    public int getEndOffset() {return -1;};
    public void setEndOffset(int o) {};
    
    public int getEndColumn() {return -1;};
    public void setEndColumn(int c) {};
    
    public int getEndLine() {return -1;};
    public void setEndLine(int l) {};
    
    public int getTextID() {return -1;};
    public void setTextID(int id) {};
    
    public int getColumn() {return -1;};
    public void setColumn(int c) {};

    public int getLine() {return -1;};
    public void setLine(int l) {};

    public String getFilename() {return null;};
    public void setFilename(String name) {};
    
    public String getText() {return "<empty>";};
    public void setText(String t) {};

    public int getType() {return INVALID_TYPE;};
    public void setType(int t) {};
    
    public String toString() {
        return "[\"" + getText() + "\",<" + getType() + ">,line=" + getLine() + ",col=" + getColumn() + "]" + ",offset="+getOffset()+",file="+getFilename();
    }
}
