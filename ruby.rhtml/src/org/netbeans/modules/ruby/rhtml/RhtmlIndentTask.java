/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby.rhtml;

import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;

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
        //doc.putProperty(HTMLLexerFormatter.HTML_FORMATTER_ACTS_ON_TOP_LEVEL, Boolean.TRUE);
        doc.putProperty("HTML_FORMATTER_ACTS_ON_TOP_LEVEL", Boolean.TRUE);
        
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        TokenSequence<?extends RhtmlTokenId> ts = th.tokenSequence(RhtmlTokenId.language());
        if (ts == null) {
            return;
        }
        
        int offset = Utilities.getRowStart(doc, end);
        org.netbeans.editor.Formatter editorFormatter = doc.getFormatter();
        List<Integer> offsets = new ArrayList<Integer>();
        while (offset >= start) {
            int lineStart = Utilities.getRowFirstNonWhite(doc, offset);
            if (lineStart != -1) {
                ts.move(lineStart);
                if (ts.moveNext()) {
                    TokenId id = ts.token().id();
                    if (id != RhtmlTokenId.HTML) {
                        offsets.add(offset);
                    }
                }
            }
            
            if (offset > 0) {
                // XXX >= ? What about empty first line?
                offset--;
                offset = Utilities.getRowStart(doc, offset);
            } else {
                break;
            }
        }

        // Process offsets to be reformatted
        if (offsets.size() > 0) {
            for (Integer lineOffset : offsets) {
                editorFormatter.changeRowIndent(doc, lineOffset, 0);
            }
        }
    }

    public ExtraLock indentLock() {
        return null;
    }
}
