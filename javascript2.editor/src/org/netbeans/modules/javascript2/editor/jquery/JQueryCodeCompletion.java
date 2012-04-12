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
package org.netbeans.modules.javascript2.editor.jquery;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.css.indexing.api.CssIndex;
import org.netbeans.modules.html.editor.lib.api.HtmlParser;
import org.netbeans.modules.html.editor.lib.api.HtmlParserFactory;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.html.editor.lib.api.model.HtmlModel;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTag;
import org.netbeans.modules.html.editor.lib.api.model.HtmlTagAttribute;
import org.netbeans.modules.javascript2.editor.CompletionContextFinder;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class JQueryCodeCompletion {

    private static final Logger LOGGER = Logger.getLogger(JQueryCodeCompletion.class.getName());

    public List<CompletionProposal> complete(CodeCompletionContext ccContext, CompletionContextFinder.CompletionContext jsCompletionContext, String prefix) {
        long start = System.currentTimeMillis();
        List<CompletionProposal> result = new ArrayList<CompletionProposal>();
        ParserResult parserResult = ccContext.getParserResult();
        int offset = ccContext.getCaretOffset();
        int tsOffset = ccContext.getParserResult().getSnapshot().getEmbeddedOffset(offset);
        switch (jsCompletionContext) {
            case GLOBAL:
            case EXPRESSION:
                if (isJQuery(parserResult, tsOffset)) {
                    addGlobalContext(result, parserResult, prefix, tsOffset);
                }
                break;
        }
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Counting jQuery CC took {0}ms ", (end - start));
        return result;
    }

    private boolean isJQuery(ParserResult parserResult, int offset) {
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
        if (ts == null) {
            return false;
        }
        ts.move(offset);
        if (!(ts.moveNext() && ts.movePrevious())) {
            return false;
        }
        Token<? extends JsTokenId> lastToken = ts.token();
        Token<? extends JsTokenId> token = lastToken;
        JsTokenId tokenId = token.id();
        while (tokenId != JsTokenId.EOL
                && tokenId != JsTokenId.WHITESPACE
                && ts.movePrevious()) {
            lastToken = token;
            token = ts.token();
            tokenId = token.id();
        }
        return (lastToken.id() == JsTokenId.IDENTIFIER && "$".equals(lastToken.text().toString()))
                || (!ts.movePrevious() && "$".equals(token.text().toString()));
    }

    private void addGlobalContext(final List<CompletionProposal> result, final ParserResult parserResult, final String prefix, final int offset) {
        /*
         * basic selectors: 
         * $(document); // Activate jQuery for object
         * $('#mydiv') // Element with ID "mydiv" 
         * $('p.first') // P tags with class first. 
         * $('p[title="Hello"]') // P tags with title "Hello"
         * $('p[title^="H"]') // P tags title starting with H
         */
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(parserResult.getSnapshot().getTokenHierarchy(), offset);
        if (ts == null) {
            return;
        }
        ts.move(offset);
        if (!(ts.moveNext() && ts.movePrevious())) {
            return;
        }
        String wrapup = "";
        if (!(ts.token().id() == JsTokenId.STRING || ts.token().id() == JsTokenId.STRING_END)) {
            wrapup = "'";
        }
        int docOffset = parserResult.getSnapshot().getOriginalOffset(offset) - prefix.length();
        String hash = "#";  // NOI18N
        if (prefix.isEmpty()) {
            // offer all tags
            Collection<HtmlTag> tags = getHtmlTags("");
            for (HtmlTag htmlTag : tags) {
                result.add(JQueryCompletionItem.create(htmlTag, docOffset, wrapup));
            }
            // and all tag ids
            Collection<String> tagIds = getTagIds("", parserResult);
            for (String tagId : tagIds) {
                result.add(JQueryCompletionItem.createCSSItem(hash + tagId, docOffset, wrapup));
            }
        } else {
            // find what is already there
            if (prefix.startsWith(hash)) {
                // provide tag ids
                String tagIdPrefix = prefix.substring(1);
                Collection<String> tagIds = getTagIds(tagIdPrefix, parserResult);
                for(String tagId : tagIds) {
                    result.add(JQueryCompletionItem.createCSSItem(hash + tagId, docOffset, wrapup));
                }
            } else {
                int index = prefix.indexOf('.');
                if (index > -1) {
                    // provide classes for the tag like p.first
                    String classPrefix = prefix.substring(index + 1);
                    Collection<String> classes = getCSSClasses(classPrefix, parserResult);
                    int anchorOffset = docOffset + prefix.length() - classPrefix.length();
                    for(String cl : classes) {
                        result.add(JQueryCompletionItem.createCSSItem(cl, anchorOffset, wrapup));
                    }
                } else {
                    index = prefix.indexOf('[');
                    if (index > -1) {
                        if (prefix.indexOf('=') == -1) {
                            // provide attributes
                            String tagName = prefix.substring(0, index);
                            String attributePrefix = prefix.substring(index + 1);
                            int anchorOffset = docOffset + prefix.length() - attributePrefix.length();
                            Collection<HtmlTagAttribute> attributes = getHtmlAttributes(tagName, attributePrefix);
                            for (HtmlTagAttribute htmlTagAttribute : attributes) {
                                result.add(JQueryCompletionItem.create(htmlTagAttribute, anchorOffset, ""));
                            }
                        }
                    } else {
                        // there is a prefix, we will expect that this is a tag name prefix
                        Collection<HtmlTag> tags = getHtmlTags(prefix);
                        for (HtmlTag htmlTag : tags) {
                            result.add(JQueryCompletionItem.create(htmlTag, docOffset, wrapup));
                        }
                        Collection<String> tagIds = getTagIds(prefix, parserResult);
                        for (String tagId : tagIds) {
                            result.add(JQueryCompletionItem.createCSSItem(hash + tagId, docOffset, wrapup));
                        }
                    }
                }
            }
        }
    }

    private Collection<String> getTagIds(String tagIdPrefix, ParserResult parserResult) {
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        HashSet<String> unigue = new HashSet<String>();
        try {
            CssIndex cssIndex = CssIndex.create(project);
            Map<FileObject, Collection<String>> findIdsByPrefix = cssIndex.findIdsByPrefix(tagIdPrefix);

            for (FileObject fObject : findIdsByPrefix.keySet()) {
                Collection<String> ids = findIdsByPrefix.get(fObject);
                for (String id : ids) {
                    unigue.add(id);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return unigue;
    }

    private Collection<String> getCSSClasses(String classPrefix, ParserResult parserResult) {
        FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
        Project project = FileOwnerQuery.getOwner(fo);
        HashSet<String> unigue = new HashSet<String>();
        try {
            CssIndex cssIndex = CssIndex.create(project);
            Map<FileObject, Collection<String>> findIdsByPrefix = cssIndex.findClassesByPrefix(classPrefix);

            for (FileObject fObject : findIdsByPrefix.keySet()) {
                Collection<String> ids = findIdsByPrefix.get(fObject);
                for (String id : ids) {
                    unigue.add(id);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return unigue;

    }

    private Collection<HtmlTagAttribute> getHtmlAttributes(final String tagName, final String prefix) {
        Collection<HtmlTagAttribute> result = Collections.emptyList();
        HtmlParser htmlParser = HtmlParserFactory.findParser(HtmlVersion.HTML5);
        HtmlModel htmlModel = htmlParser.getModel(HtmlVersion.HTML5);
        HtmlTag htmlTag = htmlModel.getTag(tagName);
        if (htmlTag != null) {
            if (prefix.isEmpty()) {
                result = htmlTag.getAttributes();
            } else {
                Collection<HtmlTagAttribute> attributes = htmlTag.getAttributes();
                result = new ArrayList<HtmlTagAttribute>();
                for (HtmlTagAttribute htmlTagAttribute : attributes) {
                    if(htmlTagAttribute.getName().startsWith(prefix)) {
                        result.add(htmlTagAttribute);
                    }
                }
            }
        }
        return result;
    }

    private Collection<HtmlTag> getHtmlTags(String prefix) {
        Collection<HtmlTag> result = Collections.emptyList();
        HtmlParser htmlParser = HtmlParserFactory.findParser(HtmlVersion.HTML5);
        HtmlModel htmlModel = htmlParser.getModel(HtmlVersion.HTML5);
        Collection<HtmlTag> allTags = htmlModel.getAllTags();
        if (prefix.isEmpty()) {
            result = allTags;
        } else {
            result = new ArrayList<HtmlTag>();
            for (HtmlTag htmlTag : allTags) {
                if (htmlTag.getName().startsWith(prefix)) {
                    result.add(htmlTag);
                }
            }
        }
        return result;
    }
}
