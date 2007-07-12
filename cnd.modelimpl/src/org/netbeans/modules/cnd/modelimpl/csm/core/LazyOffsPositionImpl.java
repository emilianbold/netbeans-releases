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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * offset based CsmOffsetable.Position implementation
 * do not keep reference to this object for a long time to prevent memory leaks
 * @author Vladimir Voskresensky
 */
public final class LazyOffsPositionImpl implements CsmOffsetable.Position {
    private int line = -1;
    private int col = -1;
    private final int offset;
    private final FileImpl file;
    
    public LazyOffsPositionImpl(FileImpl file, int offset) {
        this.offset = offset;
        this.file = file;
    }

    public int getOffset() {
        return offset;
    }

    public int getLine() {
        if (line == -1) {
            int[] res = file.getLineColumn(offset);
            line = res[0];
            col = res[1];
        }
        return line;
    }

    public int getColumn() {
        if (col == -1) {
            int[] res = file.getLineColumn(offset);
            line = res[0];
            col = res[1];
        }
        return col;
    }
    
    public String toString() {
        return "" + getLine() + ':' + getColumn() + '/' + getOffset();
    }
}       
