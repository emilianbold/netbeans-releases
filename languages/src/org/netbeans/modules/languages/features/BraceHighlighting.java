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

package org.netbeans.modules.languages.features;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 *
 * @author Dan Prusa, Vita Stejskal
 */
public class BraceHighlighting implements BracesMatcher, BracesMatcherFactory {

    public BraceHighlighting(String topLevelMimeType) {
        this(topLevelMimeType, null);
    }

    public BraceHighlighting(String topLevelMimeType, MatcherContext context) {
        this.topLevelMimeType = topLevelMimeType;
        this.context = context;
    }

    // --------------------------------------------
    // BracesMatcher implementation
    // --------------------------------------------

    public int[] findOrigin() throws InterruptedException, BadLocationException {
        Language language = null;
        try {
            language = LanguagesManager.getDefault().getLanguage(topLevelMimeType);
        } catch (LanguageDefinitionNotFoundException e) {
            // ignore, handled later
        }
        TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
        
        if (language == null || th == null) {
            // ?? no lexer for the language, all Schliemann languages should have
            // a lexer
            return defaultFindOrigin(context);
        }
        
        int caretOffset = context.getSearchOffset();
        boolean searchBack = context.isSearchingBackward();
        List<TokenSequence<?>> sequences = th.embeddedTokenSequences(caretOffset, searchBack);

        for(int i = sequences.size() - 1; i >= 0; i--) {
            TokenSequence<?> ts = sequences.get(i);
            if (ts.language().equals(language)) {
                seq = ts;
                if (i > 0) {
                    TokenSequence<?> outerSeq = sequences.get(i - 1);
                    seqStart = outerSeq.offset();
                    seqEnd = outerSeq.offset() + outerSeq.token().length();
                } else {
                    // seq is the top level sequence, ie the whole document is just javadoc
                    seqStart = 0;
                    seqEnd = context.getDocument().getLength();
                }
                break;
            }
        }

        if (seq == null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("TokenSequence with wrong language " + language); //NOI18N
            }
            return null;
        }
        
        Map<String, String>[] pairsMap = getPairsMap(language);
        if (pairsMap == null) {
            return defaultFindOrigin(context);
        }

        seq.move(caretOffset);
        if (seq.moveNext()) {
            boolean [] bckwrd = new boolean[1];
            String [] mtchng = new String[1];
            
            String tokenText = seq.token().text().toString();
            if (isOrigin(pairsMap, tokenText, mtchng, bckwrd)) {
                if (seq.offset() < caretOffset || !searchBack) {
                    originText = tokenText;
                    matchingText = mtchng[0];
                    backwards = bckwrd[0];
                    return new int [] { seq.offset(), seq.offset() + seq.token().length() };
                }
            }

            while(moveTheSequence(seq, searchBack, context.getLimitOffset())) {
                if (isOrigin(pairsMap, tokenText, mtchng, bckwrd)) {
                    originText = tokenText;
                    matchingText = mtchng[0];
                    backwards = bckwrd[0];
                    return new int [] { seq.offset(), seq.offset() + seq.token().length() };
                }
            }
        }
        
        return null;
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        // Use the default matcher if no better was available
        if (defaultMatcher != null) {
            return defaultMatcher.findMatches();
        }
        
        // Proper matching using the pairs supplied by the language definition
        assert seq != null : "No token sequence"; //NOI18N
        
        int depth = 1;
        while(moveTheSequence(seq, backwards, -1)) {
            String text = seq.token().text().toString();
            if (matchingText.equals(text)) {
                depth--;
                if (depth == 0) {
                    return new int [] { seq.offset(), seq.offset() + seq.token().length() };
                }
            } else if (originText.equals(text)) {
                depth++;
            }
        }
        
        return null;
    }
    
    // --------------------------------------------
    // BracesMatcherFactory implementation
    // --------------------------------------------
    
    public BracesMatcher createMatcher(MatcherContext context) {
        return new BraceHighlighting(topLevelMimeType, context);
    }

    // --------------------------------------------
    // Private implementation
    // --------------------------------------------
    
    private static final Logger LOG = Logger.getLogger(BraceHighlighting.class.getName());
    private static final Map<Language, Map<String, String>[]> PAIRS = new WeakHashMap<Language, Map<String, String>[]>();

    private static Map<String, String>[] getPairsMap(Language l) {
        if (!PAIRS.containsKey(l)) {
            Map<String, String> startToEnd = new HashMap<String, String>();
            Map<String, String> endToStart = new HashMap<String, String>();

            List<Feature> indents = l.getFeatureList ().getFeatures("BRACE"); //NOI18N
            Iterator<Feature> it = indents.iterator();
            while (it.hasNext()) {
                Feature indent = it.next();
                String s = (String) indent.getValue ();
                int i = s.indexOf(':'); //NOI18N
                String start = s.substring(0, i);
                String end = s.substring(i + 1);
                startToEnd.put(start, end);
                endToStart.put(end, start);
            }
            @SuppressWarnings("unchecked")
            Map<String, String> [] arr = new Map [] { startToEnd, endToStart };
            PAIRS.put(l, arr);
        }
        return PAIRS.get(l);
    }
    
    private static boolean moveTheSequence(TokenSequence<?> seq, boolean backward, int offsetLimit) {
        if (backward) {
            if (seq.movePrevious()) {
                int e = seq.offset() + seq.token().length();
                return offsetLimit == -1 ? true : e > offsetLimit;
            }
        } else {
            if (seq.moveNext()) {
                int s = seq.offset();
                return offsetLimit == -1 ? true : s < offsetLimit;
            }
        }
        return false;
    }

    private static boolean isOrigin(Map<String, String>[] pairsMaps, String originText, String [] matchingText, boolean backwards[]) {
        String s = pairsMaps[0].get(originText);
        if (s != null) {
            matchingText[0] = s;
            backwards[0] = false;
            return true;
        } else {
            s = pairsMaps[1].get(originText);
            if (s != null) {
                matchingText[0] = s;
                backwards[0] = true;
                return true;
            }
        }
        return false;
    }
    
    private int [] defaultFindOrigin(MatcherContext context) throws InterruptedException, BadLocationException {
        defaultMatcher = BracesMatcherSupport.defaultMatcher(context, -1, -1);
        return defaultMatcher.findOrigin();
    }
    
    private final MatcherContext context;
    private final String topLevelMimeType;
    
    private TokenSequence<?> seq;
    private int seqStart;
    private int seqEnd;
    private String originText;
    private String matchingText;
    private boolean backwards;
    private BracesMatcher defaultMatcher;
    
}
