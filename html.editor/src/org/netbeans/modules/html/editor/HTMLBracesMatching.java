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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.html.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.ext.html.parser.AstNode;
import org.netbeans.editor.ext.html.parser.AstNodeUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.editor.html.HTMLKit;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.gsf.api.TranslatedSource;
import org.netbeans.modules.html.editor.gsf.HtmlParserResult;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * A HTML parser based implementation of BracesMatcher. 
 *
 * @author Marek Fukala
 */
public class HTMLBracesMatching implements BracesMatcher, BracesMatcherFactory {

    private MatcherContext context;
    private LanguagePath htmlLanguagePath;
    private FileObject fileObject;
    private static final String BLOCK_COMMENT_START = "<!--"; //NOI18N
    private static final String BLOCK_COMMENT_END = "-->"; //NOI18N

    public HTMLBracesMatching() {
        this(null, null, null);
    }

    private HTMLBracesMatching(MatcherContext context, FileObject fileObject, LanguagePath htmlLanguagePath) {
        this.context = context;
        this.fileObject = fileObject;
        this.htmlLanguagePath = htmlLanguagePath;
    }

    public int[] findOrigin() throws InterruptedException, BadLocationException {
        if (MatcherContext.isTaskCanceled()) {
            return null;
        }
        TokenHierarchy th = TokenHierarchy.get(context.getDocument());
        List<TokenSequence> tsl = th.embeddedTokenSequences(context.getSearchOffset(), false);
        for (TokenSequence ts : tsl) {
            if (ts.language() == HTMLTokenId.language()) {
                ts.move(context.getSearchOffset());
                if (context.isSearchingBackward() ? ts.movePrevious() : ts.moveNext()) {
                    if (context.isSearchingBackward() && ts.offset() + ts.token().length() < context.getSearchOffset()) {
                        //check whether the searched position doesn't overlap the token boundaries 
                        return null;
                    }
                    Token t = ts.token();
                    if (tokenInTag(t)) {
                        //find the tag beginning
                        do {
                            Token t2 = ts.token();
                            if (!tokenInTag(t2)) {
                                return null;
                            } else if (t2.id() == HTMLTokenId.TAG_OPEN_SYMBOL) {
                                //find end
                                do {
                                    Token t3 = ts.token();
                                    if (!tokenInTag(t3)) {
                                        return null;
                                    } else if (t3.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                                        if("/>".equals(t3.text().toString())) {
                                            //do no match empty tags
                                            return null;
                                        } else {
                                            return new int[]{t2.offset(th), t3.offset(th) + t3.length()};
                                        }
                                    }
                                } while (ts.moveNext());
                                break;
                            }
                        } while (ts.movePrevious());
                    } else if (t.id() == HTMLTokenId.BLOCK_COMMENT) {
                        String tokenImage = t.text().toString();
                        if (tokenImage.startsWith(BLOCK_COMMENT_START) && context.getSearchOffset() < (t.offset(th)) + BLOCK_COMMENT_START.length()) {
                            return new int[]{t.offset(th), t.offset(th) + BLOCK_COMMENT_START.length()};
                        } else if (tokenImage.endsWith(BLOCK_COMMENT_END) && (context.getSearchOffset() >= (t.offset(th)) + tokenImage.length() - BLOCK_COMMENT_END.length())) {
                            return new int[]{t.offset(th) + t.length() - BLOCK_COMMENT_END.length(), t.offset(th) + t.length()};
                        }
                    }
                }
            }
        }

        return null;

    }

    private boolean tokenInTag(Token t) {
        return t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL || t.id() == HTMLTokenId.TAG_OPEN_SYMBOL || t.id() == HTMLTokenId.TAG_OPEN || t.id() == HTMLTokenId.TAG_CLOSE || t.id() == HTMLTokenId.WS || t.id() == HTMLTokenId.ARGUMENT || t.id() == HTMLTokenId.VALUE || t.id() == HTMLTokenId.VALUE_JAVASCRIPT || t.id() == HTMLTokenId.OPERATOR || t.id() == HTMLTokenId.EOL;
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        if (MatcherContext.isTaskCanceled()) {
            return null;
        }
        try {
            SourceModel sourceModel = SourceModelFactory.getInstance().getModel(fileObject);
            if (sourceModel == null) {
                return null;
            }

            final List<HtmlParserResult> l = new ArrayList<HtmlParserResult>(1);
            sourceModel.runUserActionTask(new CancellableTask<CompilationInfo>() {

                public void cancel() {
                }

                public void run(CompilationInfo parameter) throws Exception {
                    if (MatcherContext.isTaskCanceled()) {
                        return;
                    }
                    HtmlParserResult result = (HtmlParserResult) parameter.getEmbeddedResult(HTMLKit.HTML_MIME_TYPE, context.getSearchOffset());
                    l.add(0, result);
                }
            }, true);

            if (l.size() > 0) {
                HtmlParserResult result = l.get(0);
                if(result == null) {
                    return null;
                }
                AstNode root = result.root();
                int searched =  result.getTranslatedSource() == null 
                        ? context.getSearchOffset() 
                        : result.getTranslatedSource().getAstOffset(context.getSearchOffset());
                AstNode origin = AstNodeUtils.findDescendant(root, searched);
                if (origin != null) {
                    if (origin.type() == AstNode.NodeType.OPEN_TAG) {
                        AstNode parent = origin.parent();
                        if(parent.type() == AstNode.NodeType.UNMATCHED_TAG) {
                            return null;
                        } else {
                            //last element must be the matching tag
                            AstNode endTag = parent.children().get(parent.children().size() - 1);
                            return translate(new int[]{endTag.startOffset(), endTag.endOffset()}, result.getTranslatedSource());
                        }
                    } else if (origin.type() == AstNode.NodeType.ENDTAG) {
                        AstNode parent = origin.parent();
                        if(parent.type() == AstNode.NodeType.UNMATCHED_TAG) {
                            return null;
                        } else {
                            //first element must be the matching tag
                            AstNode openTag = parent.children().get(0);
                            return translate(new int[]{openTag.startOffset(), openTag.endOffset()}, result.getTranslatedSource());
                        }
                    } else if (origin.type() == AstNode.NodeType.COMMENT) {
                        int so = origin.startOffset();
                        if (searched >= origin.startOffset() && searched <= origin.startOffset() + BLOCK_COMMENT_START.length()) {
                            //complete end of comment
                            return translate(new int[]{origin.endOffset() - BLOCK_COMMENT_END.length(), origin.endOffset()}, result.getTranslatedSource());
                        } else if (searched >= origin.endOffset() - BLOCK_COMMENT_END.length() && searched <= origin.endOffset()) {
                            //complete start of comment
                            return translate(new int[]{origin.startOffset(), origin.startOffset() + BLOCK_COMMENT_START.length()}, result.getTranslatedSource());
                        }

                    }
                }
            }

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private int[] translate(int[] match, TranslatedSource source) {
        if(source == null) {
            return match;
        } else {
            return new int[]{source.getLexicalOffset(match[0]), source.getLexicalOffset(match[1])};
        }
    }
            

    
    //BracesMatcherFactory implementation
    public BracesMatcher createMatcher(MatcherContext context) {
        TokenHierarchy<Document> hierarchy = TokenHierarchy.get(context.getDocument());
        List<TokenSequence<?>> ets = hierarchy.embeddedTokenSequences(context.getSearchOffset(), context.isSearchingBackward());
        for (TokenSequence ts : ets) {
            Language language = ts.language();
            if (language == HTMLTokenId.language()) {
                DataObject od = NbEditorUtilities.getDataObject(context.getDocument());
                if (od != null) {
                    return new HTMLBracesMatching(context, od.getPrimaryFile(), ts.languagePath());
                } else {
                    break;
                }
            }
        }
        return null;
// We might be trying to search at the end or beginning of a document. In which
// case there is nothing to find and/or search through, so don't create a matcher.
//        throw new IllegalStateException("No text/html language found on the MatcherContext's search offset! This should never happen!");
    }
}
