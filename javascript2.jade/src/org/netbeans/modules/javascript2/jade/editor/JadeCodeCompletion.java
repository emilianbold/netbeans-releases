/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.jade.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler2;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModelFactory;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagType;
import static org.netbeans.modules.javascript2.jade.editor.JadeCompletionContext.ATTRIBUTE;
import static org.netbeans.modules.javascript2.jade.editor.JadeCompletionContext.NONE;
import static org.netbeans.modules.javascript2.jade.editor.JadeCompletionContext.TAG_AND_KEYWORD;
import org.netbeans.modules.javascript2.jade.editor.lexer.JadeTokenId;
import org.netbeans.modules.web.common.api.LexerUtils;

/**
 *
 * @author Petr Pisl
 */
public class JadeCodeCompletion implements CodeCompletionHandler2 {

    private static final Logger LOGGER = Logger.getLogger(JadeCodeCompletion.class.getName());
    
    private boolean caseSensitive;
    
    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        ParserResult info = context.getParserResult();
        int carretOffset = context.getParserResult().getSnapshot().getEmbeddedOffset(context.getCaretOffset());
        String prefix = context.getPrefix();
        this.caseSensitive = context.isCaseSensitive();
        
        JadeCompletionContext jadeContext = JadeCompletionContext.findCompletionContext(info, carretOffset);
        final List<CompletionProposal> resultList = new ArrayList<>();
        JadeCompletionItem.CompletionRequest request = new JadeCompletionItem.CompletionRequest( info, 
                prefix == null ? carretOffset : carretOffset - prefix.length(), prefix);
        LOGGER.log(Level.FINEST, "Jade Completion Context {0}, prefix: {1}, anchorOffset: {2}", new Object[] {jadeContext, request.prefix, request.anchor}); //NOI18N
        switch (jadeContext) {
            case TAG_AND_KEYWORD :
                completeKewords(request, resultList);
            case TAG:
                completeTags(request, resultList);
                break;
            case ATTRIBUTE:
                completeAttributes(request, resultList);
        }
        if (!resultList.isEmpty()) {
            return new DefaultCompletionResult(resultList, false);
        }
        return CodeCompletionResult.NONE;
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        return null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        String prefix = null;

        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return null;
        }

        //caretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        TokenSequence<? extends JadeTokenId> ts = info.getSnapshot().getTokenHierarchy().tokenSequence(JadeTokenId.jadeLanguage());
        if (ts == null) {
            return null;
        }

        int offset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        ts.move(offset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        
        if (ts.offset() == offset) {
            // We're looking at the offset to the RIGHT of the caret
            // and here I care about what's on the left
            ts.movePrevious();
        }

        Token<? extends JadeTokenId> token = ts.token();
        if (token.id() == JadeTokenId.TAG || token.id().isKeyword()) {
            prefix = token.text().toString();
            if (upToOffset) {
                if (offset - ts.offset() >= 0) {
                    prefix = prefix.substring(0, offset - ts.offset());
                }
            }
        }
        return prefix;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }
    
    @Override
    public Documentation documentElement(ParserResult info, ElementHandle element, Callable<Boolean> cancel) {
        if (element != null) {
            if (element instanceof JadeCompletionItem.SimpleElement) {
                return ((JadeCompletionItem.SimpleElement)element).getDocumentation();
            }
        }
        return null;
    }

    private void completeKewords(JadeCompletionItem.CompletionRequest request, List<CompletionProposal> resultList) {
        for (JadeTokenId id : JadeTokenId.values()) {
            if (id.isKeyword() && startsWith(id.getText(), request.prefix)) {
                resultList.add(new JadeCompletionItem.KeywordItem(id.getText(), request));
            }
        }
    }
    
    private void completeTags(JadeCompletionItem.CompletionRequest request, List<CompletionProposal> resultList) {
        Collection<HtmlTag> result;
        HtmlModel htmlModel = HtmlModelFactory.getModel(HtmlVersion.HTML5);
        Collection<HtmlTag> allTags = htmlModel.getAllTags();
        if (request.prefix == null || request.prefix.isEmpty()) {
            result = allTags;
        } else {
            result = new ArrayList<>();
            for (HtmlTag htmlTag : allTags) {
                if (startsWith(htmlTag.getName(), request.prefix)) {
                    result.add(htmlTag);
                }
            }
        }
        if (!result.isEmpty()) {
            for (HtmlTag tag : result) {
                resultList.add(JadeCompletionItem.create(request, tag));
            }
        }
    }
    
    private void completeAttributes(JadeCompletionItem.CompletionRequest request, List<CompletionProposal> resultList) {
        Collection<HtmlTagAttribute> resultAttrs = Collections.<HtmlTagAttribute>emptyList();
        HtmlModel htmlModel = HtmlModelFactory.getModel(HtmlVersion.HTML5);
        String tagName = findTag(request);
        String prefix = request.prefix == null ? "" : request.prefix;
        HtmlTag htmlTag = tagName == null ? null : htmlModel.getTag(tagName);
        if (tagName != null && !tagName.isEmpty() && htmlTag != null) {
            if (prefix.isEmpty()) {
                if (tagName.isEmpty()) {
                    resultAttrs = getAllAttributes(htmlModel);
                } else {
                    resultAttrs = htmlTag.getAttributes();
                }
            } else {
                Collection<HtmlTagAttribute> attributes = htmlTag.getAttributes();
                if (tagName.isEmpty() || htmlTag.getTagClass() == HtmlTagType.UNKNOWN) {
                    attributes = getAllAttributes(htmlModel);
                }
                resultAttrs = new ArrayList<HtmlTagAttribute>();
                for (HtmlTagAttribute htmlTagAttribute : attributes) {
                    if(htmlTagAttribute.getName().startsWith(prefix)) {
                        resultAttrs.add(htmlTagAttribute);
                    }
                }
            }
        } else {
            resultAttrs = getAllAttributes(htmlModel);
        }
        for (HtmlTagAttribute attribute: resultAttrs) {
            resultList.add(JadeCompletionItem.create(request, attribute));
        }
    }
    
    private boolean startsWith(String theString, String prefix) {
        if (prefix == null || prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private String findTag(JadeCompletionItem.CompletionRequest request) {
        TokenHierarchy<?> th = request.parserResult.getSnapshot().getTokenHierarchy();
        if (th == null) {
            return null;
        }
        TokenSequence<JadeTokenId> ts = th.tokenSequence(JadeTokenId.jadeLanguage());
        if (ts == null) {
            return null;
        }
        
        ts.move(request.anchor);
        
        if (!ts.movePrevious()) {
            return null;
        }
        
        Token<JadeTokenId> token = LexerUtils.followsToken(ts, JadeTokenId.TAG, true, false);
        if (token == null) {
            return null;
        }
        
        JadeTokenId id = token.id();
        
        String tagName = null;
        if (id == JadeTokenId.TAG) {
            tagName = token.text().toString();
        }
        return tagName;
    }

    private Collection<HtmlTagAttribute> getAllAttributes(HtmlModel htmlModel) {
        Map<String, HtmlTagAttribute> result = new HashMap<String, HtmlTagAttribute>();
        for (HtmlTag htmlTag : htmlModel.getAllTags()) {
            for (HtmlTagAttribute htmlTagAttribute : htmlTag.getAttributes()) {
                // attributes can probably differ per tag so we can just offer some of them,
                // at least for the CC purposes it should be complete list of attributes for unknown tag
                if (!result.containsKey(htmlTagAttribute.getName())) {
                    result.put(htmlTagAttribute.getName(), htmlTagAttribute);
                }
            }
        }
        return result.values();
    }
}
