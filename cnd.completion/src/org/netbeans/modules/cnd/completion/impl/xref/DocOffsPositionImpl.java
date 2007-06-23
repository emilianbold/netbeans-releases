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

package org.netbeans.modules.cnd.completion.impl.xref;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;

/**
 * (line, col, offset) based CsmOffsetable.Position implementation
 * @author Vladimir Voskresensky
 */
public class DocOffsPositionImpl implements CsmOffsetable.Position {
    private int line;
    private int col;
    private final int offset;
    private final BaseDocument doc;

    public DocOffsPositionImpl(BaseDocument doc, int offset) {
        this(-1,-1,offset, doc);
    }
    
    public DocOffsPositionImpl(CsmOffsetable.Position pos) {
        if (pos != null) {
            this.line = pos.getLine();
            this.col = pos.getColumn();
            this.offset = pos.getOffset();            
        } else {
            this.line = -1;
            this.col = -1;
            this.offset = 0;
        }
        this.doc = null;
    }
    
    public DocOffsPositionImpl(int line, int col, int offset, BaseDocument doc) {
        this.line = line;
        this.col = col;
        this.offset = offset;
        this.doc = doc;
    }

    public int getOffset() {
        return offset;
    }

    public int getLine() {
        return getLine(true);
    }

    public int getColumn() {
        return getColumn(true);
    }
    
    public int getLine(boolean create) {
        if (create && this.line == -1 && this.doc != null) {
            try {
                this.line = Utilities.getLineOffset(this.doc, this.offset);
            } catch (BadLocationException ex) {
                this.line = -1;
            }
        }
        return this.line;
    }

    public int getColumn(boolean create) {
        if (create && this.col == -1 && this.doc != null) {
            try {
                this.col = Utilities.getVisualColumn(this.doc, this.offset);
            } catch (BadLocationException ex) {
                this.col = -1;
            }
        }
        return this.col;
    }
    
    /*package*/BaseDocument getDocument() {
        return this.doc;
    }
    
    public String toString() {
        return "" + getLine(true) + ':' + getColumn(true) + '/' + getOffset();
    }
}       
