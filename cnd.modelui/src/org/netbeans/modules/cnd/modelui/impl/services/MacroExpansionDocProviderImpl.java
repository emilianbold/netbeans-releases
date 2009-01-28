/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * Service that provides macro expansions implementation
 *
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider.class)
public class MacroExpansionDocProviderImpl implements CsmMacroExpansionDocProvider {

    public final static String MACRO_EXPANSION_OFFSET_TRANSFORMER = "macro-expansion-offset-transformer"; // NOI18N

//    public String getExpandedText(CsmFile file, int startOffset, int endOffset) {
//        if (file instanceof FileImpl) {
//            FileImpl f = (FileImpl) file;
//            TokenStream ts = f.getTokenStream(startOffset, endOffset, false);
//            StringBuilder sb = new StringBuilder();
//            try {
//                antlr.Token token = ts.nextToken();
//                while (token != null && !APTUtils.isEOF(token)) {
//                    sb.append(token.getText());
//                    sb.append(' '); // NOI18N
//                    token = ts.nextToken();
//                }
//            } catch (TokenStreamException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//            f.releaseTokenStream(ts);
//            return sb.toString();
//        }
//        return null;
//    }

    public synchronized int expand(Document inDoc, int startOffset, int endOffset, Document outDoc) {
        int expansionsNumber = 0;
        if (inDoc == null || outDoc == null) {
            return 0;
        }
        CsmFile file = CsmUtilities.getCsmFile(inDoc, true);
        if (file == null) {
            return 0;
        }
        synchronized (inDoc) {
            TokenSequence<CppTokenId> docTS = CndLexerUtilities.getCppTokenSequence(inDoc, inDoc.getLength(), false, true);
            if (docTS == null) {
                return 0;
            }
            docTS.move(startOffset);
            TokenStream fileTS = null;
            FileImpl fileImpl = null;
            if (file instanceof FileImpl) {
                fileImpl = (FileImpl) file;
                fileTS = fileImpl.getTokenStream(startOffset, endOffset, false);
            }
            if (fileTS == null) {
                return 0;
            }

            try {

                TransformationTable tt = new TransformationTable();

                int shift = startOffset;
                int inIntervalStart = startOffset;
                boolean inMacroParams = false;
                boolean inDeadCode = true;

                StringBuffer expandedString = new StringBuffer(); // NOI18N

                APTToken fileToken = null;
                try {
                    fileToken = (APTToken) fileTS.nextToken();
                } catch (TokenStreamException ex) {
                    Exceptions.printStackTrace(ex);
                }
                while (docTS.moveNext()) {
                    Token<CppTokenId> docToken = docTS.token();

                    int docTokenStartOffset = docTS.offset();
                    int docTokenEndOffset = docTokenStartOffset + docToken.length();

                    if (isWhitespace(docToken)) {
                        continue;
                    }

                    fileToken = findRelatedTokenInExpandedStream(fileToken, docTokenStartOffset, fileTS);
                    if (fileToken == null) {
                        if (!(inMacroParams || inDeadCode)) {
                            copyInterval(inDoc, outDoc, new Interval(inIntervalStart, endOffset), shift, tt, expandedString);
                            inIntervalStart = endOffset;
                        }
                        int shiftShift = endOffset - inIntervalStart;
                        tt.intervals.add(new IntervalCorrespondence(new Interval(inIntervalStart, endOffset),
                                new Interval(inIntervalStart - shift, endOffset - (shift + shiftShift)), false));
                        inIntervalStart = endOffset;
                        shift += shiftShift;
                        break;
                    }
                    if (!APTUtils.isMacro(fileToken)) {
                        if (!isOnInclude(docTS) && docTokenEndOffset <= fileToken.getOffset()) {
                            if (inMacroParams || inDeadCode) {
                                int shiftShift = docTokenEndOffset - inIntervalStart;
                                tt.intervals.add(new IntervalCorrespondence(new Interval(inIntervalStart, docTokenEndOffset),
                                        new Interval(inIntervalStart - shift, docTokenEndOffset - (shift + shiftShift)), false));
                                inIntervalStart = docTokenEndOffset;
                                shift += shiftShift;
                                continue;
                            } else {
                                copyInterval(inDoc, outDoc, new Interval(inIntervalStart, docTokenStartOffset), shift, tt, expandedString);
                                inIntervalStart = docTokenStartOffset;

                                int shiftShift = docTokenEndOffset - inIntervalStart;
                                tt.intervals.add(new IntervalCorrespondence(new Interval(inIntervalStart, docTokenEndOffset),
                                        new Interval(inIntervalStart - shift, docTokenEndOffset - (shift + shiftShift)), false));
                                inIntervalStart = docTokenEndOffset;
                                shift += shiftShift;

                                inDeadCode = true;
                                continue;
                            }
                        }
                        inMacroParams = false;
                        inDeadCode = false;
                        continue;
                    }

                    copyInterval(inDoc, outDoc, new Interval(inIntervalStart, docTokenStartOffset), shift, tt, expandedString);
                    inIntervalStart = docTokenStartOffset;

                    StringBuffer expandedToken = new StringBuffer(""); // NOI18N
                    try {
                        if (fileToken.getOffset() < docTokenEndOffset) {
                            expandedToken.append(fileToken.getText());
                            APTToken prevFileToken = fileToken;
                            fileToken = (APTToken) fileTS.nextToken();
                            while (fileToken != null && !APTUtils.isEOF(fileToken) && fileToken.getOffset() < docTokenEndOffset) {
                                if (!APTUtils.areAdjacent(prevFileToken, fileToken)) {
                                    expandedToken.append(" "); // NOI18N
                                }
                                expandedToken.append(fileToken.getText());
                                prevFileToken = fileToken;
                                fileToken = (APTToken) fileTS.nextToken();
                            }
                        }
                    } catch (TokenStreamException ex) {
                        Exceptions.printStackTrace(ex);
                    }

                    int expandedTokenLength = indentAndAddString(outDoc, expandedToken.toString(), expandedString);
                    int shiftShift = docToken.length() - expandedTokenLength;
                    tt.intervals.add(new IntervalCorrespondence(new Interval(inIntervalStart, docTokenEndOffset),
                            new Interval(inIntervalStart - shift, docTokenEndOffset - (shift + shiftShift)), true));
                    inIntervalStart = docTokenEndOffset;
                    shift += shiftShift;

                    inMacroParams = true;
                }

                copyInterval(inDoc, outDoc, new Interval(inIntervalStart, endOffset), shift, tt, expandedString);

//        for (IntervalCorrespondence ic : tt.intervals) {
//            System.err.println("[" + ic.inInterval.start + " - " + ic.inInterval.end + "]" + " => " + "[" + ic.outInterval.start + " - " + ic.outInterval.end + "]");
//        }

                outDoc.putProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER, tt);
                try {
                    outDoc.insertString(0, expandedString.toString(), null);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (outDoc instanceof StyledDocument) {
                    for (IntervalCorrespondence ic : tt.intervals) {
                        if (ic.macro) {
                            NbDocument.markGuarded((StyledDocument) outDoc, ic.outInterval.start, ic.outInterval.length());
                            expansionsNumber++;
                        }
                    }
                }

            } finally {
                fileImpl.releaseTokenStream(fileTS);
            }
        }
        return expansionsNumber;
    }
//
//    public boolean isChanged(Document inDoc, int startOffset, int endOffset, Document outDoc) {
//        if (inDoc == null || outDoc == null) {
//            return false;
//        }
//        Object o = outDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
//        if (o != null && o instanceof TransformationTable) {
//            TransformationTable tt = (TransformationTable) o;
//            if (tt.intervals.get(0).inInterval.start == startOffset &&
//                    tt.intervals.get(tt.intervals.size() - 1).inInterval.end == endOffset) {
//                return false;
//            }
//        } else {
//            return false;
//        }
//        return true;
//    }

    private void copyInterval(Document inDoc, Document outDoc, Interval interval, int shift, TransformationTable tt, StringBuffer expandedString) {
        if (interval.length() != 0) {
            try {
                addString(outDoc, inDoc.getText(interval.start, interval.length()), expandedString);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
            tt.intervals.add(new IntervalCorrespondence(interval,
                    new Interval(interval, -shift), false));
        }
    }

    private boolean isWhitespace(Token<CppTokenId> docToken) {
        switch (docToken.id()) {
            case NEW_LINE:
            case WHITESPACE:
            case ESCAPED_WHITESPACE:
            case ESCAPED_LINE:
                return true;
            default:
                return false;
        }
    }

    private boolean isOnInclude(TokenSequence<CppTokenId> docTS) {
        Token<CppTokenId> docToken = docTS.token();
        switch (docToken.id()) {
            case PREPROCESSOR_DIRECTIVE:
                TokenSequence<?> embTS = docTS.embedded();
                if (embTS != null) {
                    embTS.moveStart();
                    embTS.moveNext();
                    Token embToken = embTS.token();
                    if (embToken == null || !(embToken.id() instanceof CppTokenId) || (embToken.id() != CppTokenId.PREPROCESSOR_START)) {
                        return false;
                    }
                    embTS.moveNext();
                    embToken = embTS.token();
                    if (embToken != null && (embToken.id() instanceof CppTokenId)) {
                        switch ((CppTokenId)embToken.id()) {
                            case PREPROCESSOR_INCLUDE:
                            case PREPROCESSOR_INCLUDE_NEXT:
                                return true;
                            default:
                                return false;
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return false;
    }

    private APTToken findRelatedTokenInExpandedStream(APTToken fileToken, int offset, TokenStream fileTS) {
        try {
            while (fileToken != null && !APTUtils.isEOF(fileToken) && fileToken.getOffset() < offset) {
                fileToken = (APTToken) fileTS.nextToken();
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

    public int getNextMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getNextMacroExpansionStartOffset(expandedOffset);
        }
        return expandedOffset;
    }

    public int getPrevMacroExpansionStartOffset(Document expandedDoc, int expandedOffset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt.getPrevMacroExpansionStartOffset(expandedOffset);
        }
        return expandedOffset;
    }

    private void addString(Document doc, String s, StringBuffer expandedString) {
        expandedString.append(s);

//        try {
//            doc.insertString(doc.getLength(), s, null);
//        } catch (BadLocationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
    }

    private int indentAndAddString(Document doc, String s, StringBuffer expandedString) {
        expandedString.append(s);
        return s.length();


//        int startLength = doc.getLength();
//        try {
//            doc.insertString(startLength, s, null);
//        } catch (BadLocationException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        int addedLendth = doc.getLength() - startLength;
//
//        if (doc instanceof StyledDocument) {
//            NbDocument.markGuarded((StyledDocument) doc, startLength, addedLendth);
//        }

//        format(doc, startLength);

//        return addedLendth;
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
    
    private static class Interval {

        public int start;
        public int end;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public Interval(Interval i, int shift) {
            this.start = i.start + shift;
            this.end = i.end + shift;
        }

        public int length() {
            return end - start;
        }

        public boolean contains(int offset) {
            return (start <= offset && end >= offset);
        }
    }

    private static class IntervalCorrespondence {

        public Interval inInterval;
        public Interval outInterval;
        boolean macro;

        public IntervalCorrespondence(Interval in, Interval out, boolean macro) {
            this.inInterval = in;
            this.outInterval = out;
            this.macro = macro;
        }
    }

    private static class TransformationTable {

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

        public int getNextMacroExpansionStartOffset(int outOffset) {
            if (intervals.isEmpty()) {
                return outOffset;
            }
            for (IntervalCorrespondence ic : intervals) {
                if (ic.outInterval.start <= outOffset) {
                    continue;
                }
                if (ic.macro) {
                    return ic.outInterval.start;
                }
            }
            return outOffset;
        }

        public int getPrevMacroExpansionStartOffset(int outOffset) {
            if (intervals.isEmpty()) {
                return outOffset;
            }
            int result = outOffset;
            for (IntervalCorrespondence ic : intervals) {
                if (ic.outInterval.end >= outOffset) {
                    return result;
                }
                if (ic.macro) {
                    result = ic.outInterval.start;
                }
            }
            return outOffset;
        }
    }
}
