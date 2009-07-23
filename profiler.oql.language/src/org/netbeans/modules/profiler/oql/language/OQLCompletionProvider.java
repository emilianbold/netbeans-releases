/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.profiler.oql.language;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 * @author Jaroslav Bachorik
 */
public class OQLCompletionProvider implements CompletionProvider {
    final private Set<String> keywords = new HashSet<String>();
    final private Set<String> functions = new HashSet<String>();
    final private Set<String> heapMethods = new HashSet<String>();
    
    public OQLCompletionProvider() {
        keywords.add("select");
        keywords.add("from");
        keywords.add("instanceof");
        keywords.add("where");

        functions.add("map");
        functions.add("filter");
        functions.add("sort");
        functions.add("top");
        functions.add("classof");
        functions.add("forEachReferrer");
        functions.add("identical");
        functions.add("objectid");
        functions.add("reachables");
        functions.add("referrers");
        functions.add("referees");
        functions.add("refers");
        functions.add("root");
        functions.add("sizeof");
        functions.add("rsizeof");
        functions.add("toHtml");
        functions.add("concat");
        functions.add("contains");
        functions.add("count");
        functions.add("filter");
        functions.add("length");
        functions.add("map");
        functions.add("max");
        functions.add("min");
        functions.add("sort");
        functions.add("top");
        functions.add("sum");
        functions.add("toArray");
        functions.add("unique");

        heapMethods.add("objects");
        heapMethods.add("classes");
        heapMethods.add("forEachClass");
        heapMethods.add("forEachObject");
        heapMethods.add("findClass");
        heapMethods.add("findObject");
        heapMethods.add("finalizables");
        heapMethods.add("livepaths");
        heapMethods.add("roots");
    }

    public CompletionTask createTask(int queryType, final JTextComponent component) {
        if (queryType != CompletionProvider.COMPLETION_QUERY_TYPE) return null;
        final Document document = component.getDocument();
        final TokenHierarchy<Document> th = TokenHierarchy.get(document);

        AsyncCompletionQuery query = new AsyncCompletionQuery() {

            @Override
            protected void query(final CompletionResultSet resultSet, Document doc, int caretOffset) {
                final TokenSequence ts = th.tokenSequence();
                final Token<OQLTokenId> currentToken = findCurrentToken(component, ts);

                // sanity test
                if (currentToken == null) {
                    resultSet.finish();
                    return;
                }

                switch (currentToken.id()) {
                    case UNKNOWN: {
                        String tokentext = currentToken.toString();
                        if ("instanceof".startsWith(tokentext.trim())) {
                            resultSet.addItem(new KeywordCompletionItem("00", "instanceof", ts.offset() + tokentext.length(), tokentext.length()));
                        }
                        break;
                    }
                    case WHITESPACE: {
                        resultSet.addItem(new KeywordCompletionItem("00", "instanceof", ts.offset() + 1));
                        break;
                    }
                    case KEYWORD: {
                        String tokentext = currentToken.toString();
                        for(String keyword : keywords) {
                            if (tokentext.trim().length() == 0 || keyword.startsWith(tokentext.trim())) {
                                resultSet.addItem(new KeywordCompletionItem("00", keyword, ts.offset() + tokentext.length(), tokentext.length()));
                            }
                        }
                        break;
                    }
                    case JSBLOCK: {
                        boolean isHeap = false;
                        int backout = 0;
                        if (ts.movePrevious()) backout++;
                        if (ts.movePrevious()) backout++; // check for "heap.somet[...]"
                        isHeap = ts.token().toString().trim().toLowerCase().equals("heap");
                        // get to the current token
                        for(int i=backout;i>0;i--) {
                            ts.moveNext();
                        }
                        String tokentext = currentToken.toString();
                        for(String function : functions) {
                            if (tokentext.trim().length() == 0 || function.startsWith(tokentext.trim())) {
                                resultSet.addItem(new FunctionCompletionItem("00", function, ts.offset() + tokentext.length(), tokentext.trim().length()));
                            }
                        }
                        if ("heap".startsWith(tokentext.trim())) {
                            resultSet.addItem(new KeywordCompletionItem("00", "heap", ts.offset() + tokentext.length(), tokentext.trim().length()));
                        }

                        if (isHeap) {
                            tokentext = currentToken.toString().trim();
                            for(String method : heapMethods) {
                                if (tokentext.length() == 0 || method.startsWith(tokentext)) {
                                    resultSet.addItem(new FunctionCompletionItem("00", method, ts.offset() + tokentext.length(), tokentext.trim().length()));
                                }
                            }

                        }

                        // special hack for "from" keyword
                        int pos = tokentext.lastIndexOf(" ");
                        if (pos > -1) {
                            tokentext = tokentext.substring(pos);
                        }
                        if (tokentext.trim().length() == 0 || "from".startsWith(tokentext.trim())) {
                            resultSet.addItem(new KeywordCompletionItem("01", "from", ts.offset() + (pos > -1 ? pos : 0) + tokentext.length(), tokentext.trim().length()));
                        }

                        break;
                    }
                    case DOT: {
                        ts.movePrevious();
                        if (ts.token().toString().trim().toLowerCase().equals("heap")) {
                            ts.moveNext();

                            for(String method : heapMethods) {
                                resultSet.addItem(new FunctionCompletionItem("00", method, ts.offset() + 1));
                            }
                        }
                        break;
                    }
                    case CLAZZ_E:
                    case CLAZZ: {
                        OQLEngine e = (OQLEngine)document.getProperty(OQLEngine.class);
                        final String tokentext = currentToken.toString().replace("\n", " ").trim();

                        String regex = ".*?" + tokentext.replace("[", "\\[").replace("]", "\\]").replace("$", "\\$") + ".*";
                        String camel = null;
                        if (tokentext.trim().equals(tokentext.trim().toUpperCase())) {
                            String trimmed = tokentext.trim();
                            StringBuilder sb = new StringBuilder(".*?");
                            for(int i=0;i<trimmed.length();i++) {
                                if (trimmed.charAt(i) >= 'A' && trimmed.charAt(i) <= 'Z') {
                                    sb.append(trimmed.charAt(i));
                                    sb.append("[a-z]*?");
                                } else {
                                    sb = null;
                                    break;
                                }
                            }
                            if (sb != null) {
                                sb.append(".*");
                                camel = sb.toString();
                            }
                            
                        }
                        String prefix = "^" + tokentext.replace("[", "\\[").replace("]", "\\]").replace("$", "\\$") + ".*";

                        Set<String> completions = new HashSet<String>();

                        Iterator clzs = e.getHeap().getJavaClassesByRegExp(prefix).iterator();
                        while(clzs.hasNext()) {
                            String className = (String)((JavaClass)clzs.next()).getName();
                            completions.add("00 " + className);
                        }

                        if (camel != null) {
                            clzs = e.getHeap().getJavaClassesByRegExp(camel).iterator();;
                            while(clzs.hasNext()) {
                                String className = (String)((JavaClass)clzs.next()).getName();
                                completions.add("01 " + className);
                            }
                        }

                        clzs = e.getHeap().getJavaClassesByRegExp(regex).iterator();;
                        while(clzs.hasNext()) {
                            String className = (String)((JavaClass)clzs.next()).getName();
                            completions.add("02 " + className);
                        }

                        for(String completion : completions) {
                            StringTokenizer tok = new StringTokenizer(completion);
                            resultSet.addItem(new ClassnameCompletionItem(tok.nextToken(), tok.nextToken(), ts.offset(), tokentext.length()));
                        }
                        break;
                    }
                }
                resultSet.finish();
            }
        };


        return query != null ? new AsyncCompletionTask(query) : null;
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        if (typedText.endsWith(".")) return CompletionProvider.COMPLETION_QUERY_TYPE;
        return 0;
    }

    private Token<OQLTokenId> findCurrentToken(JTextComponent component, TokenSequence<OQLTokenId> ts) {
        Token<OQLTokenId> currentToken = null;
        ts.moveStart();
        int forPosition = component.getCaretPosition();
        int position = 0;
        while(ts.moveNext()) {
            position = ts.offset();
            if (position >= forPosition) {
                ts.movePrevious();
                break;
            }
            currentToken = ts.token();
        }
        return currentToken;
    }
}
