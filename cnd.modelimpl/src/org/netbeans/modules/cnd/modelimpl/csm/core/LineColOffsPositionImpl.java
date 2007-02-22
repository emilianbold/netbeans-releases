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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * (line, col, offset) based CsmOffsetable.Position implementation
 * @author Vladimir Voskresensky
 */
public class LineColOffsPositionImpl implements CsmOffsetable.Position {
    private final int line;
    private final int col;
    private final int offset;

    public LineColOffsPositionImpl() {
        this(0,0,0);
    }

    public LineColOffsPositionImpl(CsmOffsetable.Position pos) {
        if (pos != null) {
            this.line = pos.getLine();
            this.col = pos.getColumn();
            this.offset = pos.getOffset();            
        } else {
            this.line = 0;
            this.col = 0;
            this.offset = 0;
        }
    }
    
    public LineColOffsPositionImpl(int line, int col, int offset) {
        this.line = line;
        this.col = col;
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return col;
    }
    
    public String toString() {
        return "" + getLine() + ':' + getColumn() + '/' + getOffset();
    }

    /* package */ void toStream(DataOutput output) throws IOException {
        output.writeInt(line);
        output.writeInt(col);
        output.writeInt(offset);
    }

    /* package */ LineColOffsPositionImpl(DataInput input) throws IOException {
        line = input.readInt();
        col = input.readInt();
        offset = input.readInt();
    }
}       