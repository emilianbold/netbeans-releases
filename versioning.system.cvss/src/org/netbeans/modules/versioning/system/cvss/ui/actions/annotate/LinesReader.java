/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss.ui.actions.annotate;

import org.netbeans.lib.cvsclient.command.annotate.AnnotateLine;

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
