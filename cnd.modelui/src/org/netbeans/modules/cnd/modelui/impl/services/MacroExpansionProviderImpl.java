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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.cnd.modelui.impl.services;

import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.ArrayList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionProvider;
import org.openide.util.Exceptions;

/**
 * Service that provides macro expansions implementation
 *
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionProvider.class)
public class MacroExpansionProviderImpl implements CsmMacroExpansionProvider {

    public final static String MACRO_EXPANSION_OFFSET_TRANSFORMER = "macro-expansion-offset-transformer"; // NOI18N

    public String getExpandedText(CsmFile file, int startOffset, int endOffset) {
        if (file instanceof FileImpl) {
            FileImpl f = (FileImpl) file;
            TokenStream ts = f.getTokenStream(startOffset, endOffset);
            StringBuilder sb = new StringBuilder();
            try {
                antlr.Token token = ts.nextToken();
                while (token != null && !APTUtils.isEOF(token)) {
                    sb.append(token.getText());
                    sb.append(' '); // NOI18N
                    token = ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }
            f.releaseTokenStream(ts);
            return sb.toString();
        }
        return null;
    }

    public void expand(Document inDoc, int startOffset, int endOffset, Document outDoc) {
        if (inDoc == null || outDoc == null) {
            return;
        }
        CsmFile file = CsmUtilities.getCsmFile(inDoc, true);
        if (file == null) {
            return;
        }
        TokenSequence<CppTokenId> docTS = CndLexerUtilities.getCppTokenSequence(inDoc, inDoc.getLength(), false, true);
        if (docTS == null) {
            return;
        }
        docTS.move(startOffset);
        TokenStream fileTS = null;
        FileImpl fileImpl = null;
        if (file instanceof FileImpl) {
            fileImpl = (FileImpl) file;
            fileTS = fileImpl.getTokenStream(startOffset, endOffset);
        }
        if (fileTS == null) {
            return;
        }

        TransformationTable tt = new TransformationTable();

        int shift = startOffset;
        int inIntervalStart = startOffset;

        org.netbeans.modules.cnd.apt.support.APTToken fileToken = null;
        try {
            fileToken = (org.netbeans.modules.cnd.apt.support.APTToken) fileTS.nextToken();
        } catch (TokenStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        while (docTS.moveNext()) {
            org.netbeans.api.lexer.Token<CppTokenId> docToken = docTS.token();

            int docTokenStartOffset = docTS.offset();
            int docTokenEndOffset = docTokenStartOffset + docToken.length();

            org.netbeans.modules.cnd.apt.support.APTToken token = findRelatedTokenInExpandedStream(fileToken, docToken, docTokenStartOffset, fileTS);
            if (token == null || !isMacro(token, docToken, docTokenStartOffset, fileTS)) {
                continue;
            }
            fileToken = token;

            copyInterval(inDoc, outDoc, new Interval(inIntervalStart, docTokenStartOffset), shift, tt);
            inIntervalStart = docTokenStartOffset;

            String expandedToken = "";
            try {
                if (fileToken.getOffset() < docTokenEndOffset) {
                    expandedToken = fileToken.getText();
                    fileToken = (org.netbeans.modules.cnd.apt.support.APTToken) fileTS.nextToken();
                    while (fileToken != null && !APTUtils.isEOF(fileToken) && fileToken.getOffset() < docTokenEndOffset) {
                        expandedToken += " " + fileToken.getText();
                        fileToken = (org.netbeans.modules.cnd.apt.support.APTToken) fileTS.nextToken();
                    }
                }
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }

            int expandedTokenLength = indentAndAddString(outDoc, expandedToken);
            int shiftShift = docToken.length() - expandedTokenLength;
            tt.intervals.add(new IntervalCorrespondence(new Interval(inIntervalStart, docTokenEndOffset),
                    new Interval(inIntervalStart - shift, docTokenEndOffset - (shift + shiftShift))));
            inIntervalStart = docTokenEndOffset;
            shift += shiftShift;
        }

        copyInterval(inDoc, outDoc, new Interval(inIntervalStart, endOffset), shift, tt);

//        for (IntervalCorrespondence ic : tt.intervals) {
//            System.out.println("[" + ic.inInterval.start + " - " + ic.inInterval.end + "]" + " => " + "[" + ic.outInterval.start + " - " + ic.outInterval.end + "]");
//        }

        outDoc.putProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER, tt);

        fileImpl.releaseTokenStream(fileTS);
    }

    private void copyInterval(Document inDoc, Document outDoc, Interval interval, int shift, TransformationTable tt) {
        if (interval.length() != 0) {
            try {
                addString(outDoc, inDoc.getText(interval.start, interval.length()));
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            tt.intervals.add(new IntervalCorrespondence(interval,
                    new Interval(interval.start - shift, interval.end - shift)));
        }
    }


    private org.netbeans.modules.cnd.apt.support.APTToken findRelatedTokenInExpandedStream(org.netbeans.modules.cnd.apt.support.APTToken fileToken, org.netbeans.api.lexer.Token<CppTokenId> docToken, int docTokenStartOffset, TokenStream fileTS) {
        if (docToken.id().equals(CppTokenId.PREPROCESSOR_DIRECTIVE) ||
                docToken.id().equals(CppTokenId.LINE_COMMENT) ||
                docToken.id().equals(CppTokenId.BLOCK_COMMENT) ||
                docToken.id().equals(CppTokenId.DOXYGEN_COMMENT) ||
                docToken.id().equals(CppTokenId.NEW_LINE) ||
                docToken.id().equals(CppTokenId.WHITESPACE) ||
                docToken.id().equals(CppTokenId.ESCAPED_WHITESPACE) ||
                docToken.id().equals(CppTokenId.ESCAPED_LINE)) {
            return null;
        }
        try {
            while (fileToken != null && !APTUtils.isEOF(fileToken) && fileToken.getOffset() < docTokenStartOffset) {
                fileToken = (org.netbeans.modules.cnd.apt.support.APTToken) fileTS.nextToken();
            }
            if (fileToken == null || APTUtils.isEOF(fileToken)) {
                return null;
            }
            return fileToken;
        } catch (TokenStreamException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private boolean isMacro(org.netbeans.modules.cnd.apt.support.APTToken fileToken, org.netbeans.api.lexer.Token<CppTokenId> docToken, int docTokenStartOffset, TokenStream fileTS) {
        if (docToken.toString().equals(fileToken.getText())) {
            return false;
        }

        return true;
    }

    public int getOffsetInExpandedText(Document expandedDoc, int originalOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getOutOffset(originalOffset);
        }
        return originalOffset;
    }

    public int getOffsetInOriginalText(Document expandedDoc, int expandedOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getInOffset(expandedOffset);
        }
        return expandedOffset;
    }

    private void addString(Document doc, String s) {
        try {
            doc.insertString(doc.getLength(), s, null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private int indentAndAddString(Document doc, String s) {
        int startLength = doc.getLength();
        try {
            doc.insertString(startLength, s, null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
//        format(doc, startLength);
        return doc.getLength() - startLength;
    }

//    private void indent(Document doc, int startOffset) {
//        Indent indent = Indent.get(doc);
//        indent.lock();
//        try {
//            try {
//                indent.reindent(startOffset, doc.getLength());
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        } finally {
//            indent.unlock();
//        }
//    }
//
//    private void format(Document doc, int startOffset) {
//        Reformat format = Reformat.get(doc);
//        format.lock();
//        try {
//            try {
//                format.reformat(startOffset, doc.getLength());
//            } catch (BadLocationException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        } finally {
//            format.unlock();
//        }
//    }
    
    private class Interval {

        public int start;
        public int end;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int length() {
            return end - start;
        }

        public boolean contains(int offset) {
            return (start <= offset && end >= offset);
        }
    }

    private class IntervalCorrespondence {

        public Interval inInterval;
        public Interval outInterval;

        public IntervalCorrespondence(Interval in, Interval out) {
            inInterval = in;
            outInterval = out;
        }
    }

    private class TransformationTable {

        private ArrayList<IntervalCorrespondence> intervals = new ArrayList<IntervalCorrespondence>();

        public int getOutOffset(int inOffset) {
            if (intervals.isEmpty()) {
                return inOffset;
            }
            if (intervals.get(0).inInterval.start > inOffset) {
                int shift = intervals.get(0).inInterval.start - inOffset;
                return intervals.get(0).outInterval.start - shift;
            }
            for (IntervalCorrespondence ic : intervals) {
                if (ic.inInterval.contains(inOffset)) {
                    int shift = inOffset - ic.inInterval.start;
                    if (shift >= ic.inInterval.length() || shift >= ic.outInterval.length()) {
                        return ic.outInterval.end;
                    } else {
                        return ic.outInterval.start + shift;
                    }
                }
            }
            int shift = inOffset - intervals.get(intervals.size() - 1).inInterval.end;
            return intervals.get(intervals.size() - 1).outInterval.end + shift;
        }

        public int getInOffset(int outOffset) {
            if (intervals.isEmpty()) {
                return outOffset;
            }
            if (intervals.get(0).outInterval.start > outOffset) {
                int shift = intervals.get(0).outInterval.start - outOffset;
                return intervals.get(0).inInterval.start - shift;
            }
            for (IntervalCorrespondence ic : intervals) {
                if (ic.outInterval.contains(outOffset)) {
                    int shift = outOffset - ic.outInterval.start;
                    if (shift >= ic.outInterval.length() || shift >= ic.inInterval.length()) {
                        return ic.inInterval.end;
                    } else {
                        return ic.inInterval.start + shift;
                    }
                }
            }
            int shift = outOffset - intervals.get(intervals.size() - 1).outInterval.end;
            return intervals.get(intervals.size() - 1).inInterval.end + shift;
        }
    }
}
