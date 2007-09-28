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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rhtml;

import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.spi.editor.indent.Context;
import org.netbeans.spi.editor.indent.ExtraLock;
import org.netbeans.spi.editor.indent.IndentTask;

/**
 * Indent task for RHTML.
 * The important work is realy done by the HTML and Ruby indenters, but
 * the HTML indenter no longer indents non-tag lines, so the RHTML indenter
 * (which will be run first, given that it's the outermost language)
 * goes and moves everything to column 0 first, such that the HTML indenter
 * can adjust it afterwards.
 *
 * @author Tor Norbye
 */
public class RhtmlIndentTask implements IndentTask {

    private Context context;

    RhtmlIndentTask(Context context) {
        this.context = context;
    }

    public void reindent() throws BadLocationException {
        BaseDocument doc = (BaseDocument) context.document();
        int start = context.startOffset();
        int end = Math.min(context.endOffset(), doc.getLength());

        reindent(doc, start, end);
    }

    public static void reindent(BaseDocument doc, int start, int end) throws BadLocationException {
//        doc.putProperty(HTMLLexerFormatter.HTML_FORMATTER_ACTS_ON_TOP_LEVEL, Boolean.TRUE);
        doc.putProperty("HTML_FORMATTER_ACTS_ON_TOP_LEVEL", Boolean.TRUE);
        int offset = Utilities.getRowStart(doc, end);
        org.netbeans.editor.Formatter editorFormatter = doc.getFormatter();
        while (offset >= start) {
            editorFormatter.changeRowIndent(doc, offset, 0);

            if (offset > 0) {
                // XXX >= ? What about empty first line?
                offset--;
                offset = Utilities.getRowStart(doc, offset);
            } else {
                break;
            }
        }
    }

    public ExtraLock indentLock() {
        return null;
    }
}
