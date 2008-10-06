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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.IndentTask;
import org.netbeans.modules.gsf.spi.GsfUtilities;
import org.netbeans.modules.ruby.RubyFormatter;
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

        //doc.putProperty(HTMLLexerFormatter.HTML_FORMATTER_ACTS_ON_TOP_LEVEL, Boolean.TRUE);
        doc.putProperty("HTML_FORMATTER_ACTS_ON_TOP_LEVEL", Boolean.TRUE);
        
        TokenHierarchy<Document> th = TokenHierarchy.get((Document)doc);
        TokenSequence<?extends RhtmlTokenId> ts = th.tokenSequence(RhtmlTokenId.language());
        if (ts == null) {
            return;
        }
        
        int offset = Utilities.getRowStart(doc, end);
        List<Integer> offsets = new ArrayList<Integer>();
        boolean prevWasNonHtml = false;
        while (offset >= start) {
            int lineStart = Utilities.getRowFirstNonWhite(doc, offset);
            if (lineStart != -1) {
                prevWasNonHtml = false;
                ts.move(lineStart);
                if (ts.moveNext()) {
                    TokenId id = ts.token().id();
                    if (id != RhtmlTokenId.HTML) {
                        prevWasNonHtml = true;
                        offsets.add(offset);
                    }
                }
            } else if (prevWasNonHtml) {
                // Include blank lines leading up to a non-html block since HTML
                // will treat these as part of the block to be indented
                offsets.add(offset);
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
                assert lineOffset == Utilities.getRowStart(doc, lineOffset);
                context.modifyIndent(lineOffset, 0);
            }
        }

        if (context.isIndent() && start > 0) { // inserting a newline
            int rowEnd = Utilities.getRowLastNonWhite(doc, start-1);
            if (rowEnd != -1) {
                String s = doc.getText(start, end-start);
                if (s.indexOf('\n') != -1) {
                    // We're not just splitting a line
                    return;
                }
                int delta = ts.move(rowEnd+1); // +1: getRowLastNonWhite returns START of last char
                if (delta > 0) {
                    if (!ts.moveNext()) {
                        return;
                    }
                } else {
                    if (!ts.movePrevious()) {
                        return;
                    }
                }
                Token<? extends RhtmlTokenId> token = ts.token();
                if (token.id() == RhtmlTokenId.DELIMITER) {
                    int rowStart = Utilities.getRowFirstNonWhite(doc, rowEnd);
                    int balance = RubyFormatter.getTokenBalance(doc, rowStart, rowEnd+1, true, true);
                    int indent = GsfUtilities.getLineIndent(doc, start-1);
                    if (balance > 0) {
                        indent += IndentUtils.indentLevelSize(doc);
                    }
                    context.modifyIndent(Utilities.getRowStart(doc, start), indent);
                }
            }
        }
    }

    public ExtraLock indentLock() {
        return null;
    }
}
