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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.java;

import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.BracesMatcherFactory;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 *
 * @author Vita Stejskal
 */
public final class JavaBracesMatcher implements BracesMatcher, BracesMatcherFactory {

    private static final char [] PAIRS = new char [] { '(', ')', '[', ']', '{', '}' }; //NOI18N
    private static final JavaTokenId [] PAIR_TOKEN_IDS = new JavaTokenId [] { 
        JavaTokenId.LPAREN, JavaTokenId.RPAREN, 
        JavaTokenId.LBRACKET, JavaTokenId.RBRACKET, 
        JavaTokenId.LBRACE, JavaTokenId.RBRACE
    };
    
    private final MatcherContext context;
    
    private int originOffset;
    private char originChar;
    private char matchingChar;
    private boolean backward;
    
    public JavaBracesMatcher() {
        this(null);
    }

    private JavaBracesMatcher(MatcherContext context) {
        this.context = context;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int[] findOrigin() throws BadLocationException, InterruptedException {
        int [] origin = BracesMatcherSupport.findChar(
            context.getDocument(), 
            context.getSearchOffset(), 
            context.getLimitOffset(), 
            PAIRS
        );

        if (origin != null) {
            originOffset = origin[0];
            originChar = PAIRS[origin[1]];
            matchingChar = PAIRS[origin[1] + origin[2]];
            backward = origin[2] < 0;
            return new int [] { originOffset, originOffset + 1 };
        } else {
            return null;
        }
    }

    public int[] findMatches() throws InterruptedException, BadLocationException {
        TokenHierarchy<Document> th = TokenHierarchy.get(context.getDocument());
        List<TokenSequence<? extends TokenId>> sequences = getEmbeddedTokenSequences(
            th, originOffset, backward, JavaTokenId.language());

        if (!sequences.isEmpty()) {
            // Check special tokens
            TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
            seq.move(originOffset);
            if (seq.moveNext()) {
                if (seq.token().id() == JavaTokenId.STRING_LITERAL ||
                    seq.token().id() == JavaTokenId.BLOCK_COMMENT ||
                    seq.token().id() == JavaTokenId.LINE_COMMENT
                ) {
                    int offset = BracesMatcherSupport.matchChar(
                        context.getDocument(), 
                        backward ? originOffset : originOffset + 1, 
                        backward ? seq.offset() : seq.offset() + seq.token().length(), 
                        originChar,
                        matchingChar);
                    if (offset != -1) {
                        return new int [] { offset, offset + 1 };
                    } else {
                        return null;
                    }
                }
            }
            
            // We are in plain java
            
            List<TokenSequence<? extends TokenId>> list;
            if (backward) {
                list = th.tokenSequenceList(seq.languagePath(), 0, originOffset);
            } else {
                list = th.tokenSequenceList(seq.languagePath(), originOffset + 1, context.getDocument().getLength());
            }
            
            JavaTokenId originId = getTokenId(originChar);
            JavaTokenId lookingForId = getTokenId(matchingChar);
            int counter = 0;
            
            for(TokenSequenceIterator tsi = new TokenSequenceIterator(list, backward); tsi.hasMore(); ) {
                TokenSequence<? extends TokenId> sq = tsi.getSequence();
                
                if (originId == sq.token().id()) {
                    counter++;
                } else if (lookingForId == sq.token().id()) {
                    if (counter == 0) {
                        return new int [] { sq.offset(), sq.offset() + sq.token().length() };
                    } else {
                        counter--;
                    }
                }
            }
        }
        
        return null;
    }

    // -----------------------------------------------------
    // private implementation
    // -----------------------------------------------------
    
    private JavaTokenId getTokenId(char ch) {
        for(int i = 0; i < PAIRS.length; i++) {
            if (PAIRS[i] == ch) {
                return PAIR_TOKEN_IDS[i];
            }
        }
        return null;
    }
    
    public static List<TokenSequence<? extends TokenId>> getEmbeddedTokenSequences(
        TokenHierarchy<?> th, int offset, boolean backwardBias, Language<? extends TokenId> language
    ) {
        List<TokenSequence<? extends TokenId>> sequences = th.embeddedTokenSequences(offset, backwardBias);

        for(int i = sequences.size() - 1; i >= 0; i--) {
            TokenSequence<? extends TokenId> seq = sequences.get(i);
            if (seq.language() == language) {
                break;
            } else {
                sequences.remove(i);
            }
        }
        
        return sequences;
    }
    
    private static final class TokenSequenceIterator {
        
        private final List<TokenSequence<? extends TokenId>> list;
        private final boolean backward;
        
        private int index;
        
        public TokenSequenceIterator(List<TokenSequence<? extends TokenId>> list, boolean backward) {
            this.list = list;
            this.backward = backward;
            this.index = -1;
        }
        
        public boolean hasMore() {
            return backward ? hasPrevious() : hasNext();
        }

        public TokenSequence<? extends TokenId> getSequence() {
            assert index >= 0 && index < list.size() : "No sequence available, call hasMore() first."; //NOI18N
            return list.get(index);
        }
        
        private boolean hasPrevious() {
            boolean anotherSeq = false;
            
            if (index == -1) {
                index = list.size() - 1;
                anotherSeq = true;
            }
            
            for( ; index >= 0; index--) {
                TokenSequence<? extends TokenId> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveEnd();
                }
                
                if (seq.movePrevious()) {
                    return true;
                }
                
                anotherSeq = true;
            }
            
            return false;
        }
        
        private boolean hasNext() {
            boolean anotherSeq = false;
            
            if (index == -1) {
                index = 0;
                anotherSeq = true;
            }
            
            for( ; index < list.size(); index++) {
                TokenSequence<? extends TokenId> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveStart();
                }
                
                if (seq.moveNext()) {
                    return true;
                }
                
                anotherSeq = true;
            }
            
            return false;
        }
    } // End of TokenSequenceIterator class
    
    // -----------------------------------------------------
    // BracesMatcherFactory implementation
    // -----------------------------------------------------
    
    /** */
    public BracesMatcher createMatcher(MatcherContext context) {
        return new JavaBracesMatcher(context);
    }

}
