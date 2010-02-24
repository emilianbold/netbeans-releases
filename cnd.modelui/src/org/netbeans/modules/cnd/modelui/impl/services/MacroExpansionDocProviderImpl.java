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

import org.netbeans.modules.cnd.antlr.TokenStream;
import org.netbeans.modules.cnd.antlr.TokenStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.support.APTFileCacheEntry;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.parser.apt.APTParseFileWalker;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;

/**
 * Service that provides macro expansions implementation.
 *
 * @author Nick Krasilnikov
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.cnd.spi.model.services.CsmMacroExpansionDocProvider.class)
public class MacroExpansionDocProviderImpl implements CsmMacroExpansionDocProvider {

    public final static String MACRO_EXPANSION_OFFSET_TRANSFORMER = "macro-expansion-offset-transformer"; // NOI18N
    public final static String MACRO_EXPANSION_MACRO_TABLE = "macro-expansion-macro-table"; // NOI18N

    public final static String MACRO_EXPANSION_STOP_ON_OFFSET_PARSE_FILE_WALKER_CACHE = "macro-expansion-stop-on-offset-parse-file-walker-cache"; // NOI18N

    public synchronized int expand(final Document inDoc, final int startOffset, final int endOffset, final Document outDoc) {
        if (inDoc == null || outDoc == null) {
            return 0;
        }
        final CsmFile file = CsmUtilities.getCsmFile(inDoc, true, false);
        if (file == null) {
            return 0;
        }


        final MyTokenSequence fileTS = getFileTokenSequence(file, startOffset, endOffset);
        if (fileTS == null) {
            return 0;
        }

        final StringBuilder expandedData = new StringBuilder();
        final TransformationTable tt = new TransformationTable(DocumentUtilities.getDocumentVersion(inDoc), CsmFileInfoQuery.getDefault().getFileVersion(file));

        Runnable r = new Runnable() {

            public void run() {
                // Init token sequences
                TokenSequence<CppTokenId> docTS = CndLexerUtilities.getCppTokenSequence(inDoc, inDoc.getLength(), false, true);
                if (docTS == null) {
                    return;
                }
                docTS.move(startOffset);

                // process tokens
                tt.setInStart(startOffset);
                tt.setOutStart(0);

                boolean inMacroParams = false;
                boolean inDeadCode = true;

                while (docTS.moveNext()) {
                    Token<CppTokenId> docToken = docTS.token();

                    int docTokenStartOffset = docTS.offset();
                    int docTokenEndOffset = docTokenStartOffset + docToken.length();

                    if (isWhitespace(docToken)) {
                        continue;
                    }

                    APTToken fileToken = findToken(fileTS, docTokenStartOffset);
                    if (fileToken == null) {
                        // expanded stream ended
                        if (!(inMacroParams || inDeadCode)) {
                            copyInterval(inDoc, ((endOffset > docTokenStartOffset) ? docTokenStartOffset : endOffset) - tt.currentIn.start, tt, expandedData);
                        }
                        tt.appendInterval(endOffset - tt.currentIn.start, 0, false);
                        break;
                    }
                    if (docTokenEndOffset <= fileToken.getOffset() || !APTUtils.isMacroExpandedToken(fileToken)) {
                        if (isOnInclude(docTS)) {
                            if (!(inMacroParams || inDeadCode)) {
                                copyInterval(inDoc, docTokenStartOffset - tt.currentIn.start, tt, expandedData);
                            } else {
                                tt.appendInterval(docTokenStartOffset - tt.currentIn.start, 0, false);
                            }
                            expandIcludeToken(docTS, inDoc, file, tt, expandedData);
                        } else if (docTokenEndOffset <= fileToken.getOffset()) {
                            if (inMacroParams || inDeadCode) {
                                // skip token in dead code
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0, false);
                                continue;
                            } else {
                                // copy tokens befor dead token and skip this token
                                copyInterval(inDoc, docTokenStartOffset - tt.currentIn.start, tt, expandedData);
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0, false);
                                inDeadCode = true;
                                continue;
                            }
                        }
                        inMacroParams = false;
                        inDeadCode = false;
                        continue;
                    }
                    // process macro
                    copyInterval(inDoc, docTokenStartOffset - tt.currentIn.start, tt, expandedData);
                expandMacroToken(docTS, fileTS, tt, expandedData);
                inMacroParams = true;
                }
                // copy the tail of the code
                copyInterval(inDoc, endOffset - tt.currentIn.start, tt, expandedData);

                tt.cleanUp();
            }
        };

        inDoc.render(r);

        // apply transformation to result document
        outDoc.putProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER, tt);
        try {
            outDoc.insertString(0, expandedData.toString(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        initGuardedBlocks(outDoc, tt);

        return calcExpansionNumber(tt);
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

    private void fillParamsToExpansionMap(APTToken fileToken, TransformationTable tt, int expandedOffsetShift, Map<Interval, List<Interval>> paramsToExpansion) {
        APTToken to = APTUtils.getExpandedToken(fileToken);
        if (to != null) {
            Interval paramInterval = new Interval(to.getOffset(), to.getEndOffset());
            Interval paramExpansionInterval = new Interval(tt.currentOut.start + expandedOffsetShift, tt.currentOut.start + expandedOffsetShift + fileToken.getText().length());
            List<Interval> paramExpansions = paramsToExpansion.get(paramInterval);
            if (paramExpansions != null) {
                paramExpansions.add(paramExpansionInterval);
            } else {
                paramExpansions = new ArrayList<Interval>();
                paramExpansions.add(paramExpansionInterval);
                paramsToExpansion.put(paramInterval, paramExpansions);
            }
        }
    }

    private APTToken findToken(MyTokenSequence fileTS, int offset) {
        while (fileTS.token() != null && !APTUtils.isEOF(fileTS.token()) && fileTS.token().getOffset() < offset) {
            fileTS.moveNext();
        }
        if (fileTS.token() == null || APTUtils.isEOF(fileTS.token())) {
            return null;
        }
        return fileTS.token();
    }

    private TransformationTable getMacroTable(Document doc) {
        Object o = doc.getProperty(MACRO_EXPANSION_MACRO_TABLE);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt;
        }
        return null;
    }

    private TransformationTable getTransformationTable(Document doc) {
        Object o = doc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;
            return tt;
        }
        return null;
    }

    public String[] getMacroExpansion(Document doc, int offset) {
        // returns empty expansion
        return new String[]{"", ""}; // NOI18N
    }

    public String expand(Document doc, int startOffset, int endOffset) {
        if(doc == null) {
            return null;
        }
        return expand(doc, CsmUtilities.getCsmFile(doc, true, false), startOffset, endOffset);
    }

    public String expand(Document doc, CsmFile file, int startOffset, int endOffset) {
        TransformationTable tt = updateMacroTableIfNeeded(doc, file);
        return tt == null ? null : expandInterval(doc, tt, startOffset, endOffset);
    }

    public int[] getMacroExpansionSpan(Document doc, int offset, boolean wait) {
        int[] span = new int[]{offset, offset};
        TransformationTable tt;
        if (wait) {
            CsmFile file = CsmUtilities.getCsmFile(doc, true, false);
            tt = updateMacroTableIfNeeded(doc, file);
        } else {
            synchronized (doc) {
                tt = getMacroTable(doc);
            }
        }
        if (tt != null) {
            int startIndex = tt.findInIntervalIndex(offset);
            if (0 <= startIndex && startIndex < tt.intervals.size()) {
                if (tt.intervals.get(startIndex).inInterval.end == offset && startIndex < tt.intervals.size() - 1) {
                    // use next
                    startIndex++;
                }
                boolean foundMacroExpansion = false;
                int macroIndex = tt.intervals.size();
                // back to start of macro expansion
                for (int i = startIndex; i >= 0; i--) {
                    IntervalCorrespondence ic = tt.intervals.get(i);
                    if (ic.macro) {
                        span[0] = ic.inInterval.start;
                        span[1] = ic.inInterval.end;
                        foundMacroExpansion = true;
                        macroIndex = i;
                        break;
                    } else if (ic.outInterval.length() != 0) {
                        // we are out of macro expansion
                        return span;
                    }
                }
                if (foundMacroExpansion) {
                    // forward to the end of macro expansion
                    for (int i = macroIndex + 1; i < tt.intervals.size(); i++) {
                        IntervalCorrespondence ic = tt.intervals.get(i);
                        if (ic.outInterval.length() == 0) {
                            // we are in macro expansion
                            span[1] = ic.inInterval.end;
                        } else {
                            return span;
                        }
                    }
                }
            }
        }
        return span;
    }

    public int[][] getUsages(Document expandedDoc, int offset) {
        Object o = expandedDoc.getProperty(MACRO_EXPANSION_OFFSET_TRANSFORMER);
        if (o != null && o instanceof TransformationTable) {
            TransformationTable tt = (TransformationTable) o;

            int startIndex = tt.findInIntervalIndex(offset);
            if (0 <= startIndex && startIndex < tt.intervals.size()) {
                if (tt.intervals.get(startIndex).inInterval.end == offset && startIndex < tt.intervals.size() - 1) {
                    // use next
                    startIndex++;
                }
                // back to start of macro expansion
                for (int i = startIndex; i >= 0; i--) {
                    IntervalCorrespondence ic = tt.intervals.get(i);
                    if (ic.macro) {
                        if (ic.paramsToExpansion != null) {
                            for (Interval in : ic.paramsToExpansion.keySet()) {
                                if (in.contains(offset)) {
                                    List<Interval> intervals = ic.paramsToExpansion.get(in);
                                    int usages[][] = new int[intervals.size()][2];
                                    for (int j = 0; j < usages.length; j++) {
                                        usages[j][0] = intervals.get(j).start;
                                        usages[j][1] = intervals.get(j).end;
                                    }
                                    return usages;
                                }
                            }
                        }
                        break;
                    } else if (ic.outInterval.length() != 0) {
                        // we are out of macro expansion
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public String expand(Document doc, int offset, String code) {
        if (doc == null) {
            return code;
        }
        CsmFile file = CsmUtilities.getCsmFile(doc, true, false);
        if (!(file instanceof FileImpl)) {
            return code;
        }
        FileImpl fileImpl = (FileImpl) file;
        APTPreprocHandler handler = ((FileImpl) file).getPreprocHandler(offset);
        if (handler == null) {
            return code;
        }
        APTFile aptLight = null;
        try {
            aptLight = APTDriver.getInstance().findAPTLight((fileImpl).getBuffer());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (aptLight == null) {
            return code;
        }
        CsmProject project = file.getProject();
        if(!(project instanceof ProjectBase)) {
            return code;
        }
        ProjectBase base = (ProjectBase) project;

        // create concurrent entry if absent
        APTFileCacheEntry cacheEntry = fileImpl.getAPTCacheEntry(handler, Boolean.FALSE);
        StopOnOffsetParseFileWalker walker = new StopOnOffsetParseFileWalker(base, aptLight, fileImpl, offset, handler,cacheEntry);
        walker.visit();
        // we do not remember cache entry because it is stopped before end of file
        // fileImpl.setAPTCacheEntry(handler, cacheEntry, false);
        TokenStream ts = APTTokenStreamBuilder.buildTokenStream(code);
        if (ts != null) {
            ts = new APTMacroExpandedStream(ts, handler.getMacroMap());
            StringBuilder sb = new StringBuilder(""); // NOI18N
            try {
                APTToken t = (APTToken) ts.nextToken();
                while (t != null && !APTUtils.isEOF(t)) {
                    sb.append(t.getText());
                    t = (APTToken) ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }
            return sb.toString();
        }
        return code;
    }

    private String expandInterval(Document doc, TransformationTable tt, int startOffset, int endOffset) {
        if (tt.intervals.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(""); // NOI18N
        int size = tt.intervals.size();
        int startIndex = tt.findInIntervalIndex(startOffset);
        if (startIndex < 0) {
            return ""; // NOI18N
        }
        for(int i = startIndex; i < size; i++) {
            IntervalCorrespondence ic = tt.intervals.get(i);
            if (ic.inInterval.start >= endOffset) {
                break;
            }
            if (ic.inInterval.end <= startOffset) {
                continue;
            }
            int startShift = startOffset - ic.inInterval.start;
            if (startShift < 0) {
                startShift = 0;
            }
            if (startShift >= ic.outInterval.length()) {
                continue;
            }
            int endShift = startShift + (endOffset - startOffset);
            if(endOffset >= ic.inInterval.end) {
                endShift = ic.outInterval.length();
            }
            if (endShift > ic.outInterval.length()) {
                endShift = ic.outInterval.length();
            }
            if (endShift - startShift != 0) {
                if (ic.macro) {
                    if(startShift == 0 && endShift == ic.outInterval.length()) {
                        sb.append(ic.getMacroExpansion());
                    } else {
                        sb.append(ic.getMacroExpansion().toString().substring(startShift, endShift));
                    }
                } else if (ic.outInterval.length() != 0) {
                    sb.append(getDocumentText(doc, ic.inInterval.start + startShift, endShift - startShift));
                }
            } 
        }
        return sb.toString();
    }

    private void expand(final Document doc, final CsmFile file, final TransformationTable tt) {
        if (doc == null) {
            return;
        }
        if (file == null) {
            return;
        }
        // Init file token sequence
        // why only one token stream is analyzed and not all preprocessor branches?
        final MyTokenSequence fileTS = getFileTokenSequence(file, 0, doc.getLength());
        if (fileTS == null) {
            return;
        }

        Runnable r = new Runnable() {

            public void run() {
                // Init document token sequence
                TokenSequence<CppTokenId> docTS = CndLexerUtilities.getCppTokenSequence(doc, doc.getLength(), false, true);
                if (docTS == null) {
                    return;
                }
                docTS.moveStart();

                int startOffset = 0;
                int endOffset = doc.getLength();

                // process tokens
                tt.setInStart(startOffset);
                tt.setOutStart(0);

                boolean inMacroParams = false;
                boolean inDeadCode = true;

                while (docTS.moveNext()) {
                    Token<CppTokenId> docToken = docTS.token();

                    int docTokenStartOffset = docTS.offset();
                    int docTokenEndOffset = docTokenStartOffset + docToken.length();

                    if (isWhitespace(docToken)) {
                        continue;
                    }

                    APTToken fileToken = findToken(fileTS, docTokenStartOffset);
                    if (fileToken == null) {
                        // expanded stream ended
                        if (!(inMacroParams || inDeadCode)) {
                            copyInterval(doc, ((endOffset > docTokenStartOffset) ? docTokenStartOffset : endOffset) - tt.currentIn.start, tt, null);
                        }
                        tt.appendInterval(endOffset - tt.currentIn.start, 0, false);
                        break;
                    }
                    if (docTokenEndOffset <= fileToken.getOffset() || !APTUtils.isMacroExpandedToken(fileToken)) {
                        if (isOnInclude(docTS)) {
                            if (!(inMacroParams || inDeadCode)) {
                                copyInterval(doc, docTokenStartOffset - tt.currentIn.start, tt, null);
                            } else {
                                tt.appendInterval(docTokenStartOffset - tt.currentIn.start, 0, false);
                            }
                            expandIcludeToken(docTS, doc, file, tt, null);
                        } else if (docTokenEndOffset <= fileToken.getOffset()) {
                            if (inMacroParams || inDeadCode) {
                                // skip token in dead code
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0, false);
                                continue;
                            } else {
                                // copy tokens befor dead token and skip this token
                                copyInterval(doc, docTokenStartOffset - tt.currentIn.start, tt, null);
                                tt.appendInterval(docTokenEndOffset - tt.currentIn.start, 0, false);
                                inDeadCode = true;
                                continue;
                            }
                        }
                        inMacroParams = false;
                        inDeadCode = false;
                        continue;
                    }
                    // process macro
                    copyInterval(doc, docTokenStartOffset - tt.currentIn.start, tt, null);
                    expandMacroToken(docTS, fileTS, tt, null);
                    inMacroParams = true;
                }
                // copy the tail of the code
                copyInterval(doc, endOffset - tt.currentIn.start, tt, null);
            }
        };
        doc.render(r);

//        System.out.println("MACRO_EXPANSION_MACRO_TABLE");
//        System.out.println(tt);
    }

    private String expandMacroToken(MyTokenSequence fileTS, int docTokenStartOffset, int docTokenEndOffset, TransformationTable tt) {
        APTToken fileToken = fileTS.token();
        StringBuilder expandedToken = new StringBuilder(""); // NOI18N
        int expandedOffsetShift = 0;

        Map<Interval, List<Interval>> paramsToExpansion = new HashMap<Interval, List<Interval>>();
                
        boolean skipIndent = true;
        if (fileToken.getOffset() < docTokenEndOffset) {
            // empty comment - expansion of empty macro
            if (!APTUtils.isCommentToken(fileToken)) {
                expandedToken.append(fileToken.getText());
                if (APTUtils.isMacroParamExpandedToken(fileToken)) {
                    fillParamsToExpansionMap(fileToken, tt, expandedOffsetShift, paramsToExpansion);
                }
                expandedOffsetShift += fileToken.getText().length();
                skipIndent = false;
            }
            APTToken prevFileToken = fileToken;
            fileTS.moveNext();
            fileToken = fileTS.token();
            while (fileToken != null && !APTUtils.isEOF(fileToken) && fileToken.getOffset() < docTokenEndOffset) {
                if (!APTUtils.isCommentToken(fileToken)) {
                    if (!skipIndent) {
                        if (!APTUtils.areAdjacent(prevFileToken, fileToken)) {
                            expandedToken.append(" "); // NOI18N
                            expandedOffsetShift++;
                        }
                    }
                    skipIndent = false;
                    expandedToken.append(fileToken.getText());
                    if (APTUtils.isMacroParamExpandedToken(fileToken)) {
                        fillParamsToExpansionMap(fileToken, tt, expandedOffsetShift, paramsToExpansion);
                    }
                    expandedOffsetShift += fileToken.getText().length();
                }
                prevFileToken = fileToken;
                fileTS.moveNext();
                fileToken = fileTS.token();
            }
        }
        tt.appendInterval(docTokenEndOffset - docTokenStartOffset, expandedToken.length(), true, expandedToken.toString(), paramsToExpansion);
        return expandedToken.toString();
    }

    private void expandMacroToken(TokenSequence docTS, MyTokenSequence fileTS, TransformationTable tt, StringBuilder expandedData) {
        expandMacroToken(docTS.token(), docTS.offset(), fileTS, tt, expandedData);
    }

    private void expandMacroToken(Token docToken, int docTokenStartOffset, MyTokenSequence fileTS, TransformationTable tt, StringBuilder expandedData) {
        String expandedToken = expandMacroToken(fileTS, docTokenStartOffset, docTokenStartOffset + docToken.length(), tt);
        addString(expandedToken, expandedData);
    }

    private void expandIcludeToken(TokenSequence<CppTokenId> docTS, Document inDoc, CsmFile file, TransformationTable tt, StringBuilder expandedData) {
        int incStartOffset = docTS.offset();
        String includeName = getIncludeName(file, incStartOffset);
        if (includeName == null) {
            return;
        }
        int incNameStartOffset = incStartOffset;
        int incNameEndOffset = incStartOffset;
        Token<CppTokenId> docToken = docTS.token();
        switch (docToken.id()) {
            case PREPROCESSOR_DIRECTIVE:
                TokenSequence<?> embTS = docTS.embedded();
                if (embTS != null) {
                    embTS.moveStart();
                    if (!embTS.moveNext()) {
                        return;
                    }
                    Token embToken = embTS.token();
                    if (embToken == null || !(embToken.id() instanceof CppTokenId) || (embToken.id() != CppTokenId.PREPROCESSOR_START)) {
                        return;
                    }
                    if (!embTS.moveNext()) {
                        return;
                    }
                    skipWhitespacesAndComments(embTS);
                    embToken = embTS.token();
                    if (embToken != null && (embToken.id() instanceof CppTokenId)) {
                        switch ((CppTokenId) embToken.id()) {
                            case PREPROCESSOR_INCLUDE:
                                if (!embTS.moveNext()) {
                                    return;
                                }
                                skipWhitespacesAndComments(embTS);
                                incNameStartOffset = embTS.offset();
                                embToken = embTS.token();
                                while (embToken != null && (embToken.id() instanceof CppTokenId) && (embToken.id() != CppTokenId.NEW_LINE)) {
                                    if (!embTS.moveNext()) {
                                        return;
                                    }
                                    incNameEndOffset = embTS.offset();
                                    skipWhitespacesAndComments(embTS);
                                    embToken = embTS.token();
                                }
                                break;
                            default:
                                return;
                        }
                    }
                }
                break;
            default:
                return;
        }
        copyInterval(inDoc, incNameStartOffset - incStartOffset, tt, expandedData);
        int expandedLength = addString(includeName, expandedData);
        tt.appendInterval(incNameEndOffset - incNameStartOffset, expandedLength, false);
    }

    private String getIncludeName(CsmFile file, int offset) {
        for (CsmInclude inc : file.getIncludes()) {
            if (inc.getStartOffset() == offset) {
                if(inc.isSystem()) {
                    StringBuilder sb = new StringBuilder("<"); // NOI18N
                    sb.append(inc.getIncludeName().toString());
                    sb.append(">"); // NOI18N
                    return sb.toString();
                } else {
                    StringBuilder sb = new StringBuilder("\""); // NOI18N
                    sb.append(inc.getIncludeName().toString());
                    sb.append("\""); // NOI18N
                    return sb.toString();
                }
            }
        }
        return null;
    }

    private void skipWhitespacesAndComments(TokenSequence ts) {
        if (ts != null) {
            Token token = ts.token();
            while (token != null && (token.id() instanceof CppTokenId)) {
                switch ((CppTokenId) token.id()) {
                    case LINE_COMMENT:
                    case DOXYGEN_LINE_COMMENT:
                    case BLOCK_COMMENT:
                    case DOXYGEN_COMMENT:
                    case WHITESPACE:
                    case ESCAPED_WHITESPACE:
                    case ESCAPED_LINE:
                        ts.moveNext();
                        token = ts.token();
                        continue;
                    default:
                        return;
                }
            }
        }
    }

    private void initGuardedBlocks(Document doc, TransformationTable tt) {
        if (doc instanceof StyledDocument) {
            for (IntervalCorrespondence ic : tt.intervals) {
                if (ic.macro) {
                    NbDocument.markGuarded((StyledDocument) doc, ic.outInterval.start, ic.outInterval.length());
                }
            }
        }
    }

    private int calcExpansionNumber(TransformationTable tt) {
        int expansionsNumber = 0;
        for (IntervalCorrespondence ic : tt.intervals) {
            if (ic.macro) {
                expansionsNumber++;
            }
        }
        return expansionsNumber;
    }

    private MyTokenSequence getFileTokenSequence(CsmFile file, int startOffset, int endOffset) {
        FileImpl fileImpl = null;
        if (file instanceof FileImpl) {
            fileImpl = (FileImpl) file;
            // why only one token stream is analyzed and not all preprocessor branches?
            TokenStream ts = fileImpl.getTokenStream(startOffset, endOffset, 0, false);
            if (ts != null) {
                return new MyTokenSequence(ts, fileImpl);
            }
        }
        return null;
    }

    private void copyInterval(Document inDoc, int length, TransformationTable tt, StringBuilder expandedString) {
        if (length != 0) {
            addString(getDocumentText(inDoc, tt.currentIn.start, length), expandedString);
            tt.appendInterval(length, length, false);
        }
    }

    static private String getDocumentText(Document doc, int startOffset, int length) {
        try {
            int docLength = doc.getLength();
            startOffset = startOffset > 0 ? startOffset : 0;
            startOffset = startOffset < docLength ? startOffset : docLength;
            length = length > 0 ? length : 0;
            length = startOffset + length <= docLength ? length : docLength - startOffset;
            if (length > 0) {
                return doc.getText(startOffset, length);
            }
        } catch (BadLocationException ex) {
            //
        }
        return ""; // NOI18N
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
                    if (embTS.moveNext()) {
                        Token embToken = embTS.token();
                        if (embToken == null || !(embToken.id() instanceof CppTokenId) || (embToken.id() != CppTokenId.PREPROCESSOR_START)) {
                            return false;
                        }
                        if (embTS.moveNext()) {
                            skipWhitespacesAndComments(embTS);
                            embToken = embTS.token();
                            if (embToken != null && (embToken.id() instanceof CppTokenId)) {
                                switch ((CppTokenId) embToken.id()) {
                                    case PREPROCESSOR_INCLUDE:
                                    case PREPROCESSOR_INCLUDE_NEXT:
                                        return true;
                                    default:
                                        return false;
                                }
                            }
                        }
                    }
                }
                break;
            default:
                return false;
        }
        return false;
    }

    private int addString(String s, StringBuilder expandedString) {
        if(expandedString != null) {
            expandedString.append(s);
        }
        return s.length();
    }

    /* package local */ String dumpTables(Document doc) {
        StringBuilder sb = new StringBuilder();
        TransformationTable tt = getMacroTable(doc);
        if(tt != null) {
            sb.append("MacroTable: "); // NOI18N
            sb.append(tt.toString());
        }
        tt = getTransformationTable(doc);
        if(tt != null) {
            sb.append("TransformationTable: "); // NOI18N
            sb.append(tt.toString());
        }
        return sb.toString();
    }
    
    private static class MyTokenSequence {

        private final TokenStream ts;
        private final FileImpl file;
        private APTToken currentToken = null;

        public MyTokenSequence(TokenStream ts, FileImpl file) {
            this.ts = ts;
            this.file = file;
            moveNext();
        }

        public APTToken token() {
            return currentToken;
        }

        public void moveNext() {
            try {
                currentToken = (APTToken) ts.nextToken();
            } catch (TokenStreamException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static class Interval {

        public int start;
        public int end;

        public Interval(int start) {
            this.start = start;
            this.end = start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public void setLength(int length) {
            this.end = start + length;
        }

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

        @Override
        public boolean equals(Object o) {
            if(o instanceof Interval) {
                Interval i = (Interval) o;
                return start == i.start && end == i.end;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + this.start;
            hash = 79 * hash + this.end;
            return hash;
        }

        @Override
        public String toString() {
            return "["+start+"-"+end+"]"; // NOI18N
        }
    }

    private static class IntervalCorrespondence {

        public Interval inInterval;
        public Interval outInterval;
        boolean macro;
        private CharSequence macroExpansion;
        private Map<Interval, List<Interval>> paramsToExpansion;

        public IntervalCorrespondence(Interval in, Interval out, boolean macro) {
            this(in, out, macro, null, null);
        }

        public IntervalCorrespondence(Interval in, Interval out, boolean macro, CharSequence macroExpansion, Map<Interval, List<Interval>> paramsToExpansion) {
            this.inInterval = in;
            this.outInterval = out;
            this.macro = macro;
            this.macroExpansion = macroExpansion;
            this.paramsToExpansion = paramsToExpansion;
        }

        /**
         * @return the macroExpansion
         */
        public CharSequence getMacroExpansion() {
            return macroExpansion;
        }

        @Override
        public String toString() {
            return "IN:"+inInterval + " OUT:"+outInterval; // NOI18N
        }
    }

    private static class TransformationTable {

        private ArrayList<IntervalCorrespondence> intervals = new ArrayList<IntervalCorrespondence>();
        private Map<CharSequence, CharSequence> cache = new HashMap<CharSequence, CharSequence>();
        private Interval currentIn;
        private Interval currentOut;
        private final long documentVersion;
        private final long fileVersion;

        public TransformationTable(long documentVersion, long fileVersion) {
            this.documentVersion = documentVersion;
            this.fileVersion = fileVersion;
        }

        public void cleanUp() {
            cache = null;
        }

        public boolean isInited() {
            return cache == null;
        }

        public void setInStart(int start) {
            currentIn = new Interval(start);
        }

        public void setOutStart(int start) {
            currentOut = new Interval(start);
        }

        public void appendInterval(int inLength, int outLength, boolean macro) {
            appendInterval(inLength, outLength, macro, null, null);
        }

        public void appendInterval(int inLength, int outLength, boolean macro, String macroExpansion, Map<Interval, List<Interval>> paramsToExpansion) {
            assert(cache != null);
            CharSequence cs = CharSequenceKey.create(macroExpansion);
            CharSequence cachedCS = cache.get(cs);
            if(cachedCS != null) {
                cs = cachedCS;
            } else {
                cache.put(cs, cs);
            }
            currentIn.setLength(inLength);
            currentOut.setLength(outLength);
            intervals.add(new IntervalCorrespondence(currentIn, currentOut, macro, cs, paramsToExpansion));
            setInStart(currentIn.end);
            setOutStart(currentOut.end);
        }

        public int getOutOffset(int inOffset) {
            if (intervals.isEmpty()) {
                return inOffset;
            }
            if (intervals.get(0).inInterval.start > inOffset) {
                int shift = intervals.get(0).inInterval.start - inOffset;
                return intervals.get(0).outInterval.start - shift;
            }

            IntervalCorrespondence lastMacro = null;
            for (IntervalCorrespondence ic : intervals) {
                if (ic.outInterval.length() != 0) {
                    lastMacro = null;
                }
                if (ic.macro) {
                    lastMacro = ic;
                }
                if (ic.inInterval.contains(inOffset)) {
                    if(ic.outInterval.length() == 0 && lastMacro != null) {
                        for (Interval i : lastMacro.paramsToExpansion.keySet()) {
                            if(i.contains(inOffset)) {
                                int shift = inOffset - i.start;
                                Interval j = lastMacro.paramsToExpansion.get(i).get(0);
                                if (shift >= j.length() || shift >= j.length()) {
                                    return j.end;
                                } else {
                                    return j.start + shift;
                                }

                            }
                        }
                    }
                    int shift = inOffset - ic.inInterval.start;
                    if (shift >= ic.inInterval.length() || shift >= ic.outInterval.length()) {
                        return ic.outInterval.end;
                    } else {
                        if(ic.macro) {
                            return ic.outInterval.start;
                        } else {
                            return ic.outInterval.start + shift;
                        }
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
                    if(ic.macro) {
                        for (Interval i : ic.paramsToExpansion.keySet()) {
                            for (Interval j : ic.paramsToExpansion.get(i)) {
                                if(j.contains(outOffset)) {
                                    int shift = outOffset - j.start;
                                    if (shift >= i.length() || shift >= j.length()) {
                                        return i.end;
                                    } else {
                                        return i.start + shift;
                                    }
                                }
                            }
                        }
                    }
                    int shift = outOffset - ic.outInterval.start;
                    if (shift >= ic.outInterval.length() || shift >= ic.inInterval.length()) {
                        return ic.inInterval.end;
                    } else {
                        if(ic.macro) {
                            return ic.inInterval.start;
                        } else {
                            return ic.inInterval.start + shift;
                        }
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

        public int findInIntervalIndex(int offset) {
            return Collections.binarySearch(intervals, new IntervalCorrespondence(new Interval(offset, offset), new Interval(offset, offset), false),
                    new Comparator<IntervalCorrespondence>() {

                        public int compare(IntervalCorrespondence o1, IntervalCorrespondence o2) {
                            if (o1.inInterval.end < o2.inInterval.start) {
                                return -1;
                            }
                            if (o1.inInterval.start > o2.inInterval.end) {
                                return 1;
                            }
                            return 0;
                        }
                    });
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(""); // NOI18N
            for (IntervalCorrespondence ic : intervals) {
                sb.append("[" + ic.inInterval.start + "," +  ic.inInterval.end + "] => [" + ic.outInterval.start + "," + ic.outInterval.end + "]\n"); // NOI18N
            }
            return sb.toString();
        }
    }

    private TransformationTable updateMacroTableIfNeeded(Document doc, CsmFile file) {
        if (file == null || doc == null) {
            return null;
        }
        TransformationTable tt = null;
        synchronized (doc) {
            tt = getMacroTable(doc);
            if (tt == null) {
                tt = new TransformationTable(DocumentUtilities.getDocumentVersion(doc), CsmFileInfoQuery.getDefault().getFileVersion(file));
                doc.putProperty(MACRO_EXPANSION_MACRO_TABLE, tt);
            }
        }
        synchronized (tt) {
            synchronized (doc) {
                tt = getMacroTable(doc);
                if (tt.documentVersion != DocumentUtilities.getDocumentVersion(doc) || tt.fileVersion != CsmFileInfoQuery.getDefault().getFileVersion(file)) {
                    tt = new TransformationTable(DocumentUtilities.getDocumentVersion(doc), CsmFileInfoQuery.getDefault().getFileVersion(file));
                }
            }
            if (!tt.isInited()) {
                expand(doc, file, tt);
                tt.cleanUp();
                synchronized (doc) {
                    doc.putProperty(MACRO_EXPANSION_MACRO_TABLE, tt);
                }
            }
        }
        return tt;
    }

    private static class StopOnOffsetParseFileWalker extends APTParseFileWalker {

        private final int stopOffset;
        public StopOnOffsetParseFileWalker(ProjectBase base, APTFile apt, FileImpl file, int offset, APTPreprocHandler preprocHandler, APTFileCacheEntry cacheEntry) {
            super(base, apt, file, preprocHandler, false, null, cacheEntry);
            stopOffset = offset;
        }

        @Override
        protected boolean onAPT(APT node, boolean wasInBranch) {
            if(node.getEndOffset() >= stopOffset) {
                stop();
                return false;
            }
            return super.onAPT(node, wasInBranch);
        }
    }
}
