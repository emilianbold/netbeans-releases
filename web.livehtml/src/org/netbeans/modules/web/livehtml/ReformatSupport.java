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
package org.netbeans.modules.web.livehtml;

import org.netbeans.modules.web.domdiff.Change;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.lib.api.HtmlSource;
import org.netbeans.modules.html.editor.lib.api.elements.Element;
import org.netbeans.modules.html.editor.lib.api.elements.ElementType;
import org.netbeans.modules.html.editor.lib.api.elements.Node;
import org.netbeans.modules.html.editor.lib.api.elements.OpenTag;

/**
 *
 * @author petr-podzimek
 */
public class ReformatSupport {
    public static final String SCRIPT_TAG = "script";
    public static final String TEXT_JAVASCRIPT_MIME_TYPE = "text/javascript";
    public static final String SPACE = " ";
    
    public static StringBuilder reformat(HtmlSource htmlSource, OpenTag openTag, List<Change> changes) {
        StringBuilder sb = new StringBuilder(htmlSource.getSourceCode());
        Utilities.eraseNewLines(openTag, sb);
        ArrayList<ReformatSupport.IndentChange> updates = new ArrayList<ReformatSupport.IndentChange>();
        int indent = 0;
        collectIndents(openTag, updates, indent);
        applyIndents(updates, sb, changes);
        return sb;
    }
    
    private static void collectIndents(Element element, ArrayList<IndentChange> updates, int indent) {
        if (indent != 0 && element.from() != -1) {
            updates.add(new IndentChange(element.from(), indent));
        }
        if (!(element instanceof Node)) {
            return;
        }
        Node n = (Node)element;
        for (Element e : n.children()) {
            if (e.type() == ElementType.TEXT) {
                continue;
            }
            collectIndents(e, updates, indent+1);
        }
    }

    private static void applyIndents(ArrayList<ReformatSupport.IndentChange> indents, StringBuilder sb, List<Change> changes) {
        for (int i = indents.size()-1; i >= 0; i--) {
            ReformatSupport.IndentChange change = indents.get(i);
            StringBuilder indent = getIndent(change.indent);
            sb.insert(change.offset, indent);
            updateChanges(changes, change.offset, indent.length());
        }
    }

    private static StringBuilder getIndent(int indent) {
        StringBuilder s = new StringBuilder("\n");
        for (int i = 0; i < indent; i++) {
            s.append(' ');
        }
        return s;
    }

    private static void updateChanges(List<Change> changes, int offset, int length) {
        for (int i = changes.size()-1; i >= 0; i--) {
            Change ch = changes.get(i);
            int outerBoundary = ch.getOffset();
            if (ch.isAdd()) {
                outerBoundary = ch.getEndOffsetOfNewText();
            }
            if (ch.getOffset() >= offset) {
                ch.increment(length);
            } else if (outerBoundary != ch.getOffset() && ch.getOffset() < offset && outerBoundary > offset) {
                ch.incrementLength(length);
            }
            
            if (outerBoundary < offset) {
                break;
            }
        }
    }
    
    private static class IndentChange {
        int offset;
        int indent;

        public IndentChange(int offset, int indent) {
            this.offset = offset;
            this.indent = indent;
        }
        
    }
    
    /**
     * Replaces source text with spaces. Position and length of spaces is defined by {@link Map> of indexes and lengths.
     * @param indexesToReplace {@link Map} defines position and length of desired spaces. Key of map is begin index of space and value of map is length of space (number of spaces). Can be null.
     * @param text source of text used to replace. Can be null.
     * @return text replaced by spaces. Can be null when any parameter is null.
     */
    public static StringBuilder replaceBySpaces(Map<Integer, Integer> indexesToReplace, StringBuilder text) {
        if (indexesToReplace == null || text == null) {
            return null;
        }
        
        for (Map.Entry<Integer, Integer> entry : indexesToReplace.entrySet()) {
            final Integer start = entry.getKey();
            final Integer length = entry.getValue();

            String source = text.substring(start, start + length);
            String spaces = source.replaceAll(".", SPACE);

            text.replace(start, start + length, spaces);
        }

        return text;
    }
    
    /**
     * Gets indexes and lengths of JavaScript code in content of Document.<br>
     * Locate of "script" tags and JavaScript call in tag attributes is implemented.
     * @param document Document instance to process. Can be null.
     * @return content of Document without JavaScript. Returns null when input parameter is null or any error occurs.
     */
    public static Map<Integer, Integer> getIndexesOfJavaScript(CharSequence document) {
        if (document == null) {
            return null;
        }
        
        final TokenHierarchy<CharSequence> tokenHierarchy = TokenHierarchy.create(document, HTMLTokenId.language());
        final TokenSequence tokenSequence = tokenHierarchy.tokenSequence();
        
        Map<Integer, Integer> indexesToReplace = new HashMap<Integer, Integer>();
        
        while (tokenSequence.moveNext()) {
            final TokenSequence<?> embedded = tokenSequence.embedded();
            if (embedded == null) { // Processing NON-EMBEDED tokens
                final Token token = tokenSequence.token();
                if (token != null) {
                    final CharSequence text = token.text();
                    if (SCRIPT_TAG.equals(text)) {
                        int start = tokenSequence.offset() - 1; // tag begins 1 characer before "script"
                        if (searchFor("</", tokenSequence) != null) {
                            final Token script = searchFor(SCRIPT_TAG, tokenSequence);
                            if (script != null) {
                                int length = (tokenSequence.offset() + token.length() + 1) - start;
                                indexesToReplace.put(start, length);
                                continue;
                            }
                        }
                    }
                }
                
            } else { // Processing EMBEDED tokens
                final Language<?> innerLanguage = embedded.languagePath().innerLanguage();
                if (innerLanguage != null && TEXT_JAVASCRIPT_MIME_TYPE.equals(innerLanguage.mimeType())) {
                    embedded.moveNext();
                    int start = embedded.offset();
                    for (int i = 0; i < embedded.tokenCount(); i++) {
                        embedded.moveNext();
                    }
                    final Token<?> token = embedded.token();
                    if (token != null) {
                        int length = embedded.offset() + token.length() - start;
                        indexesToReplace.put(start, length);
                    }
                }
            }
        }
        
        return indexesToReplace;
    }
    
    private static Token searchFor(String tokenText, TokenSequence tokenSequence) {
        if (tokenText == null || tokenText.isEmpty() || tokenSequence == null) {
            return null;
        }
        
        int i = 0;
        
        while (tokenSequence.moveNext() && i < 100) {
            final Token token = tokenSequence.token();
            if (token != null) {
                final CharSequence text = token.text();
                if (tokenText.equals(text)) {
                    return token;
                }
            }
            
            i += 1;
        }
        
        return null;
    }
    
}
