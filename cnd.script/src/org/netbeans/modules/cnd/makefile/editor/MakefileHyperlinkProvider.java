/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.makefile.editor;

import java.io.IOException;
import java.util.Collections;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.cnd.makefile.lexer.MakefileTokenId;
import org.netbeans.modules.cnd.makefile.model.AbstractMakefileElement;
import org.netbeans.modules.cnd.makefile.model.MakefileAssignment;
import org.netbeans.modules.cnd.makefile.model.MakefileRule;
import org.netbeans.modules.cnd.makefile.parser.MakefileParseResult;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * @author Alexey Vladykin
 */
public class MakefileHyperlinkProvider implements HyperlinkProvider {

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        return tokenHierarchy != null && findHyperlinkToken(tokenHierarchy, offset) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        if (tokenHierarchy != null) {
            HyperlinkToken token = findHyperlinkToken(tokenHierarchy, offset);
            if (token != null) {
                return new int[] {token.offset, token.offset + token.text.length()};
            }
        }
        return null;
    }

    @Override
    public void performClickAction(final Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        if (tokenHierarchy != null) {
            final HyperlinkToken token = findHyperlinkToken(tokenHierarchy, offset);
            if (token != null) {
                switch (token.targetKind) {
                    case RULE:
                    case VARIABLE:
                        findAndOpenElement(doc, token);
                        break;

                    case FILE:
                        FileObject fileObject = NbEditorUtilities.getFileObject(doc);
                        if (fileObject != null) {
                            findAndOpenFile(fileObject.getParent(), token.text);
                        }
                        break;
                }
            }
        }
    }

    private static HyperlinkToken findHyperlinkToken(TokenHierarchy<?> tokenHierarchy, int offset) {
         TokenSequence<MakefileTokenId> tokenSequence = tokenHierarchy.tokenSequence(MakefileTokenId.language());
         if (tokenSequence != null) {
             tokenSequence.move(offset);
             if (tokenSequence.moveNext()) {
                Token<MakefileTokenId> token = tokenSequence.token();
                if (token != null && token.id() == MakefileTokenId.BARE) {
                    String text = token.text().toString();
                    int tokenOffset = tokenSequence.offset();
                    ElementKind targetKind = ElementKind.RULE;

                    // check for "include" keyword behind current token
                    PREV_LOOP: while (tokenSequence.movePrevious()) {
                        Token<MakefileTokenId> prevToken = tokenSequence.token();
                        switch (prevToken.id()) {
                            case BARE:
                            case WHITESPACE:
                            case MACRO:
                                // ok, just skip them
                                break;

                            case KEYWORD:
                                if (TokenUtilities.equals(prevToken.text(), "include")) { // NOI18N
                                    targetKind = ElementKind.FILE;
                                }
                                break PREV_LOOP;

                            default:
                                break PREV_LOOP;
                        }
                    }

                    tokenSequence.move(offset);

                    // check for some assignment token ahead of current token
                    NEXT_LOOP: while (tokenSequence.moveNext()) {
                        Token<MakefileTokenId> nextToken = tokenSequence.token();
                        switch (nextToken.id()) {
                            case BARE:
                            case WHITESPACE:
                            case MACRO:
                                // ok, just skip them
                                break;

                            case EQUALS:
                            case COLON_EQUALS:
                            case PLUS_EQUALS:
                                targetKind = ElementKind.VARIABLE;
                                break NEXT_LOOP;

                            default:
                                break NEXT_LOOP;
                        }
                    }

                    return new HyperlinkToken(text, tokenOffset, targetKind);
                }
             }
         }
         return null;
    }

    private static void findAndOpenFile(final FileObject baseDir, final String targetPath) {
        FileObject fileObject = baseDir.getFileObject(targetPath);
        if (fileObject != null) {
            asyncOpenInEditor(fileObject, 0);
        }
    }

    private static void findAndOpenElement(final Document doc, final HyperlinkToken token) {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    ParserManager.parse(
                            Collections.singleton(Source.create(doc)),
                            new HyperlinkTask(token.text, token.targetKind));
                } catch (ParseException ex) {
                    // tsss, don't tell anybody
                }
            }
        });
    }

    private static final class HyperlinkToken {

        private final String text;
        private final int offset;
        private final ElementKind targetKind;

        private HyperlinkToken(String text, int offset, ElementKind targetKind) {
            this.text = text;
            this.offset = offset;
            this.targetKind = targetKind;
        }
    }

    private static final class HyperlinkTask extends UserTask {

        private final String elementName;
        private final ElementKind elementKind;

        private HyperlinkTask(String elementName, ElementKind elementKind) {
            this.elementName = elementName;
            this.elementKind = elementKind;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Result result = resultIterator.getParserResult();
            if (result instanceof MakefileParseResult) {
                MakefileParseResult makefileResult = (MakefileParseResult) result;
                for (AbstractMakefileElement element : makefileResult.getElements()) {
                    if (element.getKind() == ElementKind.RULE && elementKind == ElementKind.RULE) {
                        MakefileRule rule = (MakefileRule) element;
                        if (rule.getTargets().contains(elementName)) {
                            asyncOpenInEditor(result.getSnapshot().getSource(), rule.getOffsetRange(makefileResult).getStart());
                            break;
                        }
                    } else if (element.getKind() == ElementKind.VARIABLE && elementKind == ElementKind.VARIABLE) {
                        MakefileAssignment assign = (MakefileAssignment) element;
                        if (assign.getName().equals(elementName)) {
                            asyncOpenInEditor(result.getSnapshot().getSource(), assign.getOffsetRange(makefileResult).getStart());
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void asyncOpenInEditor(final Source source, final int offset) {
        Document doc = source.getDocument(true);
        if (doc != null) {
            DataObject dataObject = NbEditorUtilities.getDataObject(doc);
            if (dataObject != null) {
                asyncOpenIdEditor(dataObject, doc, offset);
            }
        }
    }

    private static void asyncOpenInEditor(final FileObject fileObject, final int offset) {
        try {
            DataObject dataObject = DataObject.find(fileObject);
            asyncOpenInEditor(dataObject, offset);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void asyncOpenInEditor(final DataObject dataObject, final int offset) {
        EditorCookie editorCookie = dataObject.getLookup().lookup(EditorCookie.class);
        if (editorCookie != null) {
            try {
                Document doc = editorCookie.openDocument();
                asyncOpenIdEditor(dataObject, doc, offset);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private static void asyncOpenIdEditor(final DataObject dataObject, final Document doc, final int offset) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LineCookie lineCookie = dataObject.getLookup().lookup(LineCookie.class);
                if (lineCookie != null) {
                    try {
                        int lineIdx = Utilities.getLineOffset((BaseDocument) doc, offset);
                        Line line = lineCookie.getLineSet().getCurrent(lineIdx);
                        line.show(Line.ShowOpenType.OPEN, Line.ShowVisibilityType.FOCUS);
                    } catch (BadLocationException ex) {
                    }
                }
            }
        });
    }
}
