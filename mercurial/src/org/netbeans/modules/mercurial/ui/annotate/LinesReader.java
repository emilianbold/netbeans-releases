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
 * "Portions Copyrighted [year] [name of copyright owner]" // NOI18N
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mercurial.ui.annotate;

import java.io.Reader;
import java.io.IOException;
import java.util.List;

/**
 * Reader over annotation lines. It uses '\n' as
 * line separator to match Document line separator.
 *
 * @author Petr Kuzel
 */
public final class LinesReader extends  Reader {

    private List lines;
    private int lineIndex;
    private int columnIndex;
    private boolean closed;

    /**
     * Creates reader from list of AnnotateLine objects.
     */
    LinesReader(List lines) {
        this.lines = lines;
    }

    public void close() throws IOException {
        if (closed) throw new IOException("Closed"); // NOI18N
        closed = true;
    }

    public int read(char cbuf[], int off, int len) throws IOException {
        if (closed) throw new IOException("Closed"); // NOI18N

        if (lineIndex >= lines.size()) return -1;

        AnnotateLine aline = (AnnotateLine) lines.get(lineIndex);
        String line = aline.getContent() + "\n"; // NOI18N
        int lineLen = line.length();
        int unread =  lineLen - columnIndex;
        int toRead = Math.min(unread, len);
        line.getChars(columnIndex, columnIndex + toRead, cbuf, off);
        columnIndex += toRead;
        if (columnIndex >= lineLen) {
            columnIndex = 0;
            lineIndex++;
        }
        return toRead;
    }
}
