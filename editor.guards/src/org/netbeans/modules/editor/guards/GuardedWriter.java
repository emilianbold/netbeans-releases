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

package org.netbeans.modules.editor.guards;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;
import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.spi.editor.guards.support.AbstractGuardedSectionsProvider;

/** This stream is able to insert special guarded comments.
*/
final class GuardedWriter extends Writer {
    /** Encapsulated writer. */
    private Writer writer;

    private CharArrayWriter buffer;

    private final AbstractGuardedSectionsProvider gw;

    private boolean isClosed = false;

    private final List<GuardedSection> sections;

    
    /** Creates new GuardedWriter.
    * @param os Encapsulated output stream.
    * @param list The list of the guarded sections.
    */
    public GuardedWriter(AbstractGuardedSectionsProvider gw, OutputStream os, List<GuardedSection> list, String encoding) throws UnsupportedEncodingException {
        if (encoding == null)
            writer = new OutputStreamWriter(os);
        else
            writer = new OutputStreamWriter(os, encoding);
        this.gw = gw;
        sections = list;
    }

    /** Writes chars to underlying writer */
    public void write(char[] cbuf, int off, int len) throws IOException {
        
        if (buffer == null) {
            buffer = new CharArrayWriter(10240);
        }
        
        buffer.write(cbuf, off, len);
    }

    /** Calls underlying writer flush */
    public void close() throws IOException {
        if (isClosed) {
            return;
        }
        isClosed = true;
        if (buffer != null) {
            char[] content = this.gw.writeSections(sections, buffer.toCharArray());
            writer.write(content);
        }
        writer.close();
    }

    /** Calls underlying writer flush */
    public void flush() throws IOException {
    }
    
}
