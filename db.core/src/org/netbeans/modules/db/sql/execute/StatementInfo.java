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

package org.netbeans.modules.db.sql.execute;

/**
 *
 * @author Andrei Badea
 */
public class StatementInfo {

    private final String sql;
    private final int rawStartOffset;
    private final int startOffset;
    private final int startLine;
    private final int startColumn;
    private final int rawEndOffset;
    private final int endOffset;

    public StatementInfo(String sql, int rawStartOffset, int startOffset, int startLine, int startColumn, int endOffset, int rawEndOffset) {
        this.sql = sql;
        this.rawStartOffset = rawStartOffset;
        this.startOffset = startOffset;
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endOffset = endOffset;
        this.rawEndOffset = rawEndOffset;
    }

    public String getSQL() {
        return sql;
    }

    public int getRawStartOffset() {
        return rawStartOffset;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public int getRawEndOffset() {
        return rawEndOffset;
    }
}
