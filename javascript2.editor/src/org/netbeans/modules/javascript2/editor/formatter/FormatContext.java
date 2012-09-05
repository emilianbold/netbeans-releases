/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.formatter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 *
 * @author Petr Hejl
 */
public final class FormatContext {

    private static final Logger LOGGER = Logger.getLogger(FormatContext.class.getName());

    private static final Pattern SAFE_DELETE_PATTERN = Pattern.compile("\\s*"); // NOI18N

    private final Context context;

    private final Snapshot snapshot;

    private final int initialStart;

    private final int initialEnd;

    private final List<Region> regions;

    private final boolean embedded;

    private LineWrap lastLineWrap;

    private int indentationLevel;

    private int offsetDiff;

    private int currentLineStart;

    public FormatContext(Context context, Snapshot snapshot) {
        this.context = context;
        this.snapshot = snapshot;
        this.initialStart = context.startOffset();
        this.initialEnd = context.endOffset();

        regions = new ArrayList<Region>(context.indentRegions().size());
        for (Context.Region region : context.indentRegions()) {
            regions.add(new Region(region.getStartOffset(), region.getEndOffset()));
        }

        dumpRegions();

        this.embedded = !JsTokenId.JAVASCRIPT_MIME_TYPE.equals(context.mimePath())
                && !JsTokenId.JSON_MIME_TYPE.equals(context.mimePath());

        /*
         * What we do here is fix for case like this:
         * <head>
         *     <script>[REGION_START]
         *         var x = 1;
         *         function test() {
         *             x ="";
         *         }
         *     [REGION_END]</script>
         * </head>
         *
         * The last line with REGION_END would be considered empty line and
         * truncated. So we could either avoid that or shift the REGION_END to
         * the line start offset. We do the latter.
         */
        if (embedded) {
            for (Region region : regions) {
                int endOffset = region.getOriginalEnd();
                try {
                    int lineOffset = context.lineStartOffset(endOffset);
                    TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(
                        snapshot, region.getOriginalStart());
                    if (ts != null) {
                        int embeddedOffset = snapshot.getEmbeddedOffset(lineOffset);
                        if (embeddedOffset >= 0) {
                            ts.move(embeddedOffset);
                            if (ts.moveNext()) {
                                Token<? extends JsTokenId> token = ts.token();
                                // BEWARE whitespace must span across the whole line
                                if (token.id() == JsTokenId.WHITESPACE
                                        && (lineOffset + token.length()) == endOffset) {
                                    region.setOriginalEnd(lineOffset);
                                }
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                }
            }

            LOGGER.log(Level.FINE, "Tuned regions");
            dumpRegions();
        }
    }


    public void setLastLineWrap(LineWrap lineWrap) {
        this.lastLineWrap = lineWrap;
    }

    public LineWrap getLastLineWrap() {
        return lastLineWrap;
    }

    public int getCurrentLineStart() {
        return currentLineStart;
    }

    public void setCurrentLineStart(int currentLineStart) {
        this.currentLineStart = currentLineStart;
    }

    public int getIndentationLevel() {
        return indentationLevel;
    }

    public void incIndentationLevel() {
        this.indentationLevel++;
    }

    public void decIndentationLevel() {
        this.indentationLevel--;
    }

    public int getOffsetDiff() {
        return offsetDiff;
    }

    private void setOffsetDiff(int offsetDiff) {
        this.offsetDiff = offsetDiff;
    }

    private void dumpRegions() {
        if (!LOGGER.isLoggable(Level.FINE)) {
            return;
        }

        for (Region region : regions) {
            try {
                LOGGER.log(Level.FINE, region.getOriginalStart() + ":" + region.getOriginalEnd()
                        + ":" + getDocument().getText(region.getOriginalStart(), region.getOriginalEnd() - region.getOriginalStart()));
            } catch (BadLocationException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
    }
    
    public int getDocumentOffset(int offset) {
        return getDocumentOffset(offset, true);
    }

    private int getDocumentOffset(int offset, boolean check) {
        if (!embedded) {
            if (!check || (offset >= initialStart && offset < initialEnd)) {
                return offset;
            }
            return -1;
        }

        int docOffset = snapshot.getOriginalOffset(offset);
        if (docOffset < 0) {
            return -1;
        }

        for (Region region : regions) {
            if (docOffset >= region.getOriginalStart() && docOffset < region.getOriginalEnd()) {
                return docOffset;
            }
        }
        return -1;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public int getEmbeddingIndent(int offset) {
        if (!embedded) {
            return 0;
        }

        int docOffset = snapshot.getOriginalOffset(offset);
        if (docOffset < 0) {
            return 0;
        }

        Region start = null;
        for (Region region : regions) {
            if (docOffset >= region.getOriginalStart() && docOffset < region.getOriginalEnd()) {
                start = region;
                break;
            }
        }
        if (start != null) {
            try {
                /*
                 * If the lineStart is going to be in different region (this
                 * might happen when another embedding in JS) we move to that
                 * region and we're getting the indent from the start of that
                 * region.
                 */
                int lineStart = context.lineStartOffset(start.getOriginalStart());
                while (start != null && lineStart < start.getOriginalStart()) {

                    Region previousStart = null;
                    for (Region region : regions) {
                        if (lineStart >= region.getOriginalStart() && lineStart < region.getOriginalEnd()) {
                            previousStart = region;
                            break;
                        }
                    }
                    if (previousStart != null) {
                        lineStart = context.lineStartOffset(previousStart.getOriginalStart());
                    }
                    start = previousStart;
                }
                return context.lineIndent(lineStart)
                        + IndentUtils.indentLevelSize(getDocument());
            } catch (BadLocationException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }
        try {
            return context.lineIndent(context.lineStartOffset(docOffset));
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return 0;
    }

    public BaseDocument getDocument() {
        return (BaseDocument) context.document();
    }

    public void indentLine(int voffset, int indentationSize,
            JsFormatter.Indentation indentationCheck) {

        indentLineWithOffsetDiff(voffset, indentationSize, indentationCheck, offsetDiff);
    }

    public void indentLineWithOffsetDiff(int voffset, int indentationSize,
            JsFormatter.Indentation indentationCheck, int realOffsetDiff) {

        if (!indentationCheck.isAllowed()) {
            return;
        }

        int offset = getDocumentOffset(voffset, !indentationCheck.isExceedLimits());
        if (offset < 0) {
            return;
        }

        try {
            int diff = GsfUtilities.setLineIndentation(getDocument(),
                    offset + realOffsetDiff, indentationSize);
            setOffsetDiff(offsetDiff + diff);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void insert(int voffset, String newString) {
        insertWithOffsetDiff(voffset, newString, offsetDiff);
    }

    public void insertWithOffsetDiff(int voffset, String newString, int realOffsetDiff) {
        int offset = getDocumentOffset(voffset);
        if (offset < 0) {
            return;
        }

        BaseDocument doc = getDocument();
        try {
            doc.insertString(offset + realOffsetDiff, newString, null);
            setOffsetDiff(offsetDiff + newString.length());
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void replace(int voffset, String oldString, String newString) {
        if (oldString.equals(newString)) {
            return;
        }

        replace(voffset, oldString.length(), newString);
    }

    public void replace(int voffset, int length, String newString) {
        int offset = getDocumentOffset(voffset);
        if (offset < 0) {
            return;
        }

        BaseDocument doc = getDocument();
        try {
            String oldText = doc.getText(offset + offsetDiff, length);
            if (newString.equals(oldText)) {
                return;
            }
            if (SAFE_DELETE_PATTERN.matcher(oldText).matches()) {
                doc.remove(offset + offsetDiff, length);
                doc.insertString(offset + offsetDiff, newString, null);
                setOffsetDiff(offsetDiff + (newString.length() - length));
            } else {
                LOGGER.log(Level.WARNING, "Tried to remove non empty text: {0}",
                        doc.getText(offset + offsetDiff, length));
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public void remove(int voffset, int length) {
        int offset = getDocumentOffset(voffset);
        if (offset < 0) {
            return;
        }

        BaseDocument doc = getDocument();
        try {
            if (SAFE_DELETE_PATTERN.matcher(doc.getText(offset + offsetDiff, length)).matches()) {
                doc.remove(offset + offsetDiff, length);
                setOffsetDiff(offsetDiff - length);
            } else {
                LOGGER.log(Level.WARNING, "Tried to remove non empty text: {0}",
                        doc.getText(offset + offsetDiff, length));
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    public static class LineWrap {

        private final FormatToken token;

        private final int offsetDiff;

        private final int indentationLevel;

        public LineWrap(FormatToken token, int offsetDiff, int indentationLevel) {
            this.token = token;
            this.offsetDiff = offsetDiff;
            this.indentationLevel = indentationLevel;
        }

        public FormatToken getToken() {
            return token;
        }

        public int getOffsetDiff() {
            return offsetDiff;
        }

        public int getIndentationLevel() {
            return indentationLevel;
        }
    }

    private static class Region {

        private final int originalStart;

        private int originalEnd;

        public Region(int originalStart, int originalEnd) {
            this.originalStart = originalStart;
            this.originalEnd = originalEnd;
        }

        public int getOriginalStart() {
            return originalStart;
        }

        public int getOriginalEnd() {
            return originalEnd;
        }

        public void setOriginalEnd(int originalEnd) {
            this.originalEnd = originalEnd;
        }
    }
}
