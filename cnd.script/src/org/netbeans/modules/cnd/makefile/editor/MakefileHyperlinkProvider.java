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

import java.util.Collections;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkProvider;
import org.netbeans.modules.cnd.makefile.lexer.MakefileTokenId;
import org.netbeans.modules.cnd.makefile.model.AbstractMakefileElement;
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
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.util.RequestProcessor;

/**
 * @author Alexey Vladykin
 */
public class MakefileHyperlinkProvider implements HyperlinkProvider {

    @Override
    public boolean isHyperlinkPoint(Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        return tokenHierarchy != null && findBareTokenAtOffset(tokenHierarchy, offset) != null;
    }

    @Override
    public int[] getHyperlinkSpan(Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        if (tokenHierarchy != null) {
            Token<MakefileTokenId> token = findBareTokenAtOffset(tokenHierarchy, offset);
            if (token != null) {
                int tokenStart = token.offset(tokenHierarchy);
                int tokenEnd = tokenStart + token.length();
                return new int[] {tokenStart, tokenEnd};
            }
        }
        return null;
    }

    @Override
    public void performClickAction(final Document doc, int offset) {
        TokenHierarchy<?> tokenHierarchy = TokenHierarchy.get(doc);
        if (tokenHierarchy != null) {
            Token<MakefileTokenId> token = findBareTokenAtOffset(tokenHierarchy, offset);
            if (token != null) {
                final String targetName = token.text().toString();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ParserManager.parse(
                                    Collections.singleton(Source.create(doc)),
                                    new HyperlinkTask(targetName));
                        } catch (ParseException ex) {
                            // tsss, don't tell anybody
                        }
                    }
                });
            }
        }
    }

    private static Token<MakefileTokenId> findBareTokenAtOffset(TokenHierarchy<?> tokenHierarchy, int offset) {
         TokenSequence<MakefileTokenId> tokenSequence = tokenHierarchy.tokenSequence(MakefileTokenId.language());
         if (tokenSequence != null) {
             tokenSequence.move(offset);
             if (tokenSequence.moveNext()) {
                Token<MakefileTokenId> token = tokenSequence.token();
                if (token != null && token.id() == MakefileTokenId.BARE) {
                    return token;
                }
             }
         }
         return null;
    }

    private static class HyperlinkTask extends UserTask {

        private final String targetName;

        private HyperlinkTask(String targetName) {
            this.targetName = targetName;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            Result result = resultIterator.getParserResult();
            if (result instanceof MakefileParseResult) {
                MakefileParseResult makefileResult = (MakefileParseResult) result;
                for (AbstractMakefileElement element : makefileResult.getElements()) {
                    if (element.getKind() == ElementKind.RULE) {
                        MakefileRule rule = (MakefileRule) element;
                        if (rule.getTargets().contains(targetName)) {
                            openInEditor(result.getSnapshot().getSource().getDocument(true), rule.getOffsetRange(makefileResult).getStart());
                            break;
                        }
                    }
                }
            }
        }

        private static void openInEditor(final Document doc, final int offset) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    DataObject dataObject = NbEditorUtilities.getDataObject(doc);
                    if (dataObject != null) {
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
                }
            });
        }
    }
}
