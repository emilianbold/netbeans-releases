/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            return null;
        }
        
        int caretOffset = context.getSearchOffset();
        boolean searchBack = context.isSearchingBackward();
        List<TokenSequence<? extends TokenId>> sequences = th.embeddedTokenSequences(caretOffset, searchBack);

        for(int i = sequences.size() - 1; i >= 0; i--) {
            TokenSequence<? extends TokenId> ts = sequences.get(i);
            if (ts.language().equals(language)) {
                seq = ts;
                if (i > 0) {
                    TokenSequence<? extends TokenId> outerSeq = sequences.get(i - 1);
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
            return null;
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

            List<Feature> indents = l.getFeatures("BRACE"); //NOI18N
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
    
    private static boolean moveTheSequence(TokenSequence<? extends TokenId> seq, boolean backward, int offsetLimit) {
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
    
    private final MatcherContext context;
    private final String topLevelMimeType;
    
    private TokenSequence<? extends TokenId> seq;
    private int seqStart;
    private int seqEnd;
    private String originText;
    private String matchingText;
    private boolean backwards;
    
}
