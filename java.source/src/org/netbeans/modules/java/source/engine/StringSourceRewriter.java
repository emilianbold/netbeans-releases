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

package org.netbeans.modules.java.source.engine;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.text.BadLocationException;

/**
 * Manages writing source file changes to a buffer, which toString() returns.
 */
public class StringSourceRewriter implements SourceRewriter {
    PrintWriter out;
    StringWriter sout;

    public StringSourceRewriter() {
        sout = new StringWriter();
        out = new PrintWriter(sout);
    }

    public void writeTo(String s) throws IOException, BadLocationException {
        out.print(s);
    }

    public void skipThrough(SourceReader in, int offset) throws IOException, BadLocationException {
        in.seek(offset);
    }

    public void copyTo(SourceReader in, int offset) throws IOException {
        char[] buf = in.getCharsTo(offset);
        out.write(buf);
    }

    public void copyRest(SourceReader in) throws IOException {
        char[] buf = new char[4096];
        int i;
        while ((i = in.read(buf)) > 0)
            out.write(buf, 0, i);
    }

    public void close(boolean save) {
        out.flush();
    }

    /**
     * Returns the output written to this SourceRewriter.
     */
    public String toString() {
        return sout.toString();
    }
}
