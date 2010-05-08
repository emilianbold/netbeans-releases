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
        keywords.add("select"); // NOI18N
        keywords.add("from"); // NOI18N
        
        functions.add("map"); // NOI18N
        functions.add("filter"); // NOI18N
        functions.add("sort"); // NOI18N
        functions.add("top"); // NOI18N
        functions.add("classof"); // NOI18N
        functions.add("forEachReferrer"); // NOI18N
        functions.add("identical"); // NOI18N
        functions.add("objectid"); // NOI18N
        functions.add("reachables"); // NOI18N
        functions.add("referrers"); // NOI18N
        functions.add("referees"); // NOI18N
        functions.add("refers"); // NOI18N
        functions.add("root"); // NOI18N
        functions.add("sizeof"); // NOI18N
        functions.add("rsizeof"); // NOI18N
        functions.add("toHtml"); // NOI18N
        functions.add("concat"); // NOI18N
        functions.add("contains"); // NOI18N
        functions.add("count"); // NOI18N
        functions.add("filter"); // NOI18N
        functions.add("length"); // NOI18N
        functions.add("map"); // NOI18N
        functions.add("max"); // NOI18N
        functions.add("min"); // NOI18N
        functions.add("sort"); // NOI18N
        functions.add("top"); // NOI18N
        functions.add("sum"); // NOI18N
        functions.add("toArray"); // NOI18N
        functions.add("unique"); // NOI18N

        heapMethods.add("objects"); // NOI18N
        heapMethods.add("classes"); // NOI18N
        heapMethods.add("forEachClass"); // NOI18N
        heapMethods.add("forEachObject"); // NOI18N
        heapMethods.add("findClass"); // NOI18N
        heapMethods.add("findObject"); // NOI18N
        heapMethods.add("finalizables"); // NOI18N
        heapMethods.add("livepaths"); // NOI18N
        heapMethods.add("roots"); // NOI18N
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

                String tokentext = currentToken.toString();
                switch (currentToken.id()) {
                    case UNKNOWN: {
                        if ("instanceof".startsWith(tokentext.trim())) { // NOI18N
                            resultSet.addItem(new KeywordCompletionItem("00", "instanceof", ts.offset() + tokentext.trim().length(), tokentext.length())); // NOI18N
                        }
                        break;
                    }
                    case SELECT: {
                        resultSet.addItem(new KeywordCompletionItem("00", "select", ts.offset() + tokentext.length(), tokentext.trim().length())); // NOI18N
                        break;
                    }
                    case FROM: {
                        resultSet.addItem(new KeywordCompletionItem("00", "from", ts.offset() + tokentext.length(), tokentext.trim().length())); // NOI18N
                        break;
                    }
                    case INSTANCEOF: {
                        resultSet.addItem(new KeywordCompletionItem("00", "instanceof", ts.offset() + tokentext.length())); // NOI18N
                        break;
                    }
                    case WHERE: {
                        resultSet.addItem(new KeywordCompletionItem("00", "where", ts.offset() + tokentext.length(), tokentext.trim().length())); // NOI18N
                        break;
                    }
                    case ERROR: {
                        for(String keyword : keywords) {
                            if (tokentext.trim().length() == 0 || keyword.startsWith(tokentext.trim())) {
                                KeywordCompletionItem kci = new KeywordCompletionItem("00", keyword, ts.offset() + tokentext.trim().length(), tokentext.trim().length());  // NOI18N
                                resultSet.addItem(kci);
                            }
                        }
                        break;
                    }
                    case JSBLOCK: {
                        boolean isHeap = false;
                        int backout = 0;
                        if (ts.movePrevious()) backout++;
                        if (ts.movePrevious()) backout++; // check for "heap.somet[...]"
                        isHeap = ts.token().toString().trim().toLowerCase().equals("heap"); // NOI18N
                        // get to the current token
                        for(int i=backout;i>0;i--) {
                            ts.moveNext();
                        }

                        int wsPosDiff = tokentext.indexOf(tokentext.trim());
                        for(String function : functions) {
                            if (tokentext.trim().length() == 0 || function.startsWith(tokentext.trim())) {
                                resultSet.addItem(new FunctionCompletionItem("00", function, ts.offset() + tokentext.trim().length() + wsPosDiff, tokentext.trim().length())); // NOI18N
                            }
                        }
                        if ("heap".startsWith(tokentext.trim())) { // NOI18N
                            resultSet.addItem(new KeywordCompletionItem("00", "heap", ts.offset() + tokentext.trim().length(), tokentext.trim().length())); // NOI18N
                        }

                        if (isHeap) {
                            tokentext = currentToken.toString().trim();
                            for(String method : heapMethods) {
                                if (tokentext.length() == 0 || method.startsWith(tokentext)) {
                                    resultSet.addItem(new FunctionCompletionItem("00", method, ts.offset() + tokentext.trim().length(), tokentext.trim().length())); // NOI18N
                                }
                            }

                        }

                        // special hack for "from" keyword
                        // kind of space-magick; in the same place as "from" keyword there may be a valid javascript
                        // not exactly the best designed language but, hey, it's just a script ...
                        if (tokentext.trim().isEmpty()) {
                            resultSet.addItem(new KeywordCompletionItem("01", "from", ts.offset() + tokentext.length(), tokentext.trim().length())); // NOI18N
                        } else {
                            StringTokenizer t = new StringTokenizer(tokentext, " ");
                            while (t.hasMoreTokens()) {
                                String tt = t.nextToken();
                                if ("FROM".startsWith(tt.trim().toUpperCase())) {
                                    int pos = tokentext.indexOf(tt);
                                    int wsPos = tokentext.indexOf(" ", pos);
                                    if (tt.trim().length() == 3) {
                                        pos++;
                                    }
                                    resultSet.addItem(new KeywordCompletionItem("01", "from", ts.offset() + pos + (wsPos > -1 ? 1 : 2), tt.trim().length())); // NOI18N
                                    break;
                                }
                            }
                        }

                        break;
                    }
                    case DOT: {
                        ts.movePrevious();
                        if (ts.token().toString().trim().toLowerCase().equals("heap")) { // NOI18N
                            ts.moveNext();

                            for(String method : heapMethods) {
                                resultSet.addItem(new FunctionCompletionItem("00", method, ts.offset() + 1)); // NOI18N
                            }
                        }
                        break;
                    }
                    case CLAZZ_E:
                    case CLAZZ: {
                        OQLEngine e = (OQLEngine)document.getProperty(OQLEngine.class);

                        String regex = ".*?" + tokentext.replace("[", "\\[").replace("]", "\\]").replace("$", "\\$") + ".*"; // NOI18N
                        String camel = null;
                        if (tokentext.trim().equals(tokentext.trim().toUpperCase())) {
                            // prepare camel-case completion
                            String trimmed = tokentext.trim();
                            StringBuilder sb = new StringBuilder(".*?"); // NOI18N
                            for(int i=0;i<trimmed.length();i++) {
                                if (trimmed.charAt(i) >= 'A' && trimmed.charAt(i) <= 'Z') { // NOI18N
                                    sb.append(trimmed.charAt(i));
                                    sb.append("[a-z]*?"); // NOI18N
                                } else {
                                    sb = null;
                                    break;
                                }
                            }
                            if (sb != null) {
                                sb.append(".*"); // NOI18N
                                camel = sb.toString();
                            }
                            
                        }
                        String regexBody = tokentext.replace("[", "\\[").replace("]", "\\]").replace("$", "\\$"); // NOI18N
                        String prefix = "^" + regexBody + ".*"; // NOI18N
                        Set<String> pkgCompletions = new HashSet<String>();
                        Set<String> completions = new HashSet<String>();

                        Iterator clzs = e.getHeap().getJavaClassesByRegExp(regex).iterator();
                        while(clzs.hasNext()) {
                            String className = (String)((JavaClass)clzs.next()).getName();
                            String[] sig = splitClassName(className);
                            if (sig[1].startsWith(tokentext)) {
                                completions.add("00 " + className); // NOI18N
                            } else if (sig[1].contains(tokentext)) {
                                completions.add("01 " + className); // NOI18N
                            }
                        }

                        clzs = e.getHeap().getJavaClassesByRegExp(prefix).iterator();
                        while(clzs.hasNext()) {
                            String className = (String)((JavaClass)clzs.next()).getName();

                            String[] sig = splitClassName(className);

                            if (sig[0].length() > tokentext.trim().length() && sig[0].startsWith(tokentext.trim())) {
                                int pkgSepPos = sig[0].indexOf('.', tokentext.trim().length() + 1); // NOI18N
                                if (pkgSepPos == -1) {
                                    pkgCompletions.add(sig[0]);
                                } else {
                                    pkgCompletions.add(sig[0].substring(0, pkgSepPos));
                                }
                            }
                            if (sig[0].indexOf(".", tokentext.trim().length() - 1) == -1) { // NOI18N
                                completions.add("01 " + className); // NOI18N
                            }
                        }

                        if (camel != null) {
                            clzs = e.getHeap().getJavaClassesByRegExp(camel).iterator();;
                            while(clzs.hasNext()) {
                                String className = (String)((JavaClass)clzs.next()).getName();
                                completions.add("02 " + className); // NOI18N
                            }
                        }

                        Set<String> usedTypeNames = new HashSet<String>();
                        for(String completion : completions) {
                            StringTokenizer tok = new StringTokenizer(completion);
                            String sortPre = tok.nextToken();
                            String clzName = tok.nextToken();
                            if (!usedTypeNames.contains(clzName)) {
                                resultSet.addItem(new ClassnameCompletionItem(sortPre, clzName, ts.offset(), tokentext.length()));
                                usedTypeNames.add(clzName);
                            }
                        }
                        for(String completion : pkgCompletions) {
                            if (!usedTypeNames.contains(completion)) {
                                resultSet.addItem(new PackageCompletionItem(completion, ts.offset(), tokentext.length()));
                            }
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
        return CompletionProvider.COMPLETION_ALL_QUERY_TYPE;
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

    private static String[] splitClassName(String className) {
        String pkgName, typeName;
        int pkgPos = className.lastIndexOf('.'); // NOI18N
        if (pkgPos > -1) {
            pkgName = className.substring(0, pkgPos);
            typeName = className.substring(pkgPos + 1);
        } else {
            pkgName = ""; // NOI18N
            typeName = className;
        }
        return new String[]{pkgName, typeName};
    }
}
