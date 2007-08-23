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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.AttributesUtilities;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * The syntax coloring layer.
 * 
 * @author Vita Stejskal
 */
public final class SyntaxHighlighting extends AbstractHighlightsContainer implements TokenHierarchyListener {
    
    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting.level=FINEST
    private static final Logger LOG = Logger.getLogger(SyntaxHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting"; //NOI18N
    
    private final HashMap<String, WeakHashMap<TokenId, AttributeSet>> attribsCache = new HashMap<String, WeakHashMap<TokenId, AttributeSet>>();
    private final HashMap<String, FontColorSettings> fcsCache = new HashMap<String, FontColorSettings>();
    
    private final Document document;
    private final String mimeTypeForHack;
    private TokenHierarchy<? extends Document> hierarchy = null;
    private long version = 0;
    
    /** Creates a new instance of SyntaxHighlighting */
    public SyntaxHighlighting(Document document) {
        this.document = document;
        
        String mimeType = (String) document.getProperty("mimeType"); //NOI18N
        if (mimeType != null && mimeType.startsWith("test")) { //NOI18N
            this.mimeTypeForHack = mimeType;
        } else {
            this.mimeTypeForHack = null;
        }
    }

    public HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (hierarchy == null) {
                hierarchy = TokenHierarchy.get(document);
                if (hierarchy != null) {
                    hierarchy.addTokenHierarchyListener(WeakListeners.create(TokenHierarchyListener.class, this, hierarchy));
                }
            }

            if (hierarchy != null) {
                return new HSImpl(version, hierarchy, startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }

    // ----------------------------------------------------------------------
    //  TokenHierarchyListener implementation
    // ----------------------------------------------------------------------

    public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        synchronized (this) {
            version++;
        }

        if (LOG.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder();
            TokenSequence<? extends TokenId> ts = hierarchy.tokenSequence();
            
            sb.append("\n"); //NOI18N
            sb.append("Tokens after change: <" + evt.affectedStartOffset() + ", " + evt.affectedEndOffset() + ">\n"); //NOI18N
            dumpSequence(ts, sb);
            sb.append("--------------------------------------------\n\n"); //NOI18N
            
            LOG.finest(sb.toString());
        }
        
        fireHighlightsChange(evt.affectedStartOffset(), evt.affectedEndOffset());
//        fireHighlightsChange(0, Integer.MAX_VALUE);
    }

    // ----------------------------------------------------------------------
    //  Private implementation
    // ----------------------------------------------------------------------

    private static void dumpSequence(TokenSequence<? extends TokenId> seq, StringBuilder sb) {
        for(seq.moveStart(); seq.moveNext(); ) {
            TokenSequence<? extends TokenId> emSeq = seq.embedded();
            if (emSeq != null) {
                dumpSequence(emSeq, sb);
            } else {
                Token<? extends TokenId> token = seq.token();
                sb.append("<"); //NOI18N
                sb.append(String.format("%3s", seq.offset())).append(", "); //NOI18N
                sb.append(String.format("%3s", seq.offset() + token.length())).append(", "); //NOI18N
                sb.append(String.format("%+3d", token.length())).append("> : "); //NOI18N
                sb.append(tokenId(token.id(), true)).append(" : '"); //NOI18N
                sb.append(tokenText(token));
                sb.append("'\n"); //NOI18N
            }
        }
    }
    
    private static String tokenId(TokenId tokenId, boolean format) {
        if (format) {
            return String.format("%20s.%-15s", tokenId.getClass().getSimpleName(), tokenId.name()); //NOI18N
        } else {
            return tokenId.getClass().getSimpleName() + "." + tokenId.name(); //NOI18N
        }
    }
    
    private static String tokenText(Token<? extends TokenId> token) {
        CharSequence text = token.text();
        StringBuilder sb = new StringBuilder(text.length());
        
        for(int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.isISOControl(ch)) {
                switch (ch) {
                case '\n' : sb.append("\\n"); break; //NOI18N
                case '\t' : sb.append("\\t"); break; //NOI18N
                case '\r' : sb.append("\\r"); break; //NOI18N
                default : sb.append("\\").append(Integer.toOctalString(ch)); break; //NOI18N
                }
            } else {
                sb.append(ch);
            }
        }
        
        return sb.toString();
    }

    private static String attributeSet(AttributeSet as) {
        if (as == null) {
            return "AttributeSet is null"; //NOI18N
        }
        
        StringBuilder sb = new StringBuilder();
        
        for(Enumeration<? extends Object> keys = as.getAttributeNames(); keys.hasMoreElements(); ) {
            Object key = keys.nextElement();
            Object value = as.getAttribute(key);

            if (key == null) {
                sb.append("null"); //NOI18N
            } else {
                sb.append("'").append(key.toString()).append("'"); //NOI18N
            }

            sb.append(" = "); //NOI18N

            if (value == null) {
                sb.append("null"); //NOI18N
            } else {
                sb.append("'").append(value.toString()).append("'"); //NOI18N
            }

            if (keys.hasMoreElements()) {
                sb.append(", "); //NOI18N
            }
        }
        
        return sb.toString();
    }
    
    private final class HSImpl implements HighlightsSequence {
        
        private static final int S_NORMAL = 1;
        private static final int S_EMBEDDED_HEAD = 2;
        private static final int S_EMBEDDED_TAIL = 3;
        private static final int S_DONE = 4;

        private long version;
        private TokenHierarchy<? extends Document> scanner;
        private List<TokenSequence<? extends TokenId>> sequences;
        private int startOffset;
        private int endOffset;
        private int state = -1;
        
        public HSImpl(long version, TokenHierarchy<? extends Document> scanner, int startOffset, int endOffset) {
            this.version = version;
            this.scanner = scanner;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.sequences = null;
        }
        
        public boolean moveNext() {
            synchronized (SyntaxHighlighting.this) {
                if (checkVersion()) {
                    if (sequences == null) {
                        // initialize
                        TokenSequence<? extends TokenId> seq = scanner.tokenSequence();
                        seq.move(startOffset);
                        sequences = new ArrayList<TokenSequence<? extends TokenId>>();
                        sequences.add(seq);
                        state = S_NORMAL;
                    }
                } else {
                    state = S_DONE;
                }

                switch (state) {
                    case S_NORMAL:
                        // The current token is a normal one
                        state = moveTheSequence();
                        break;

                    case S_EMBEDDED_HEAD:
                        // The current token contains embedded language and we have processed it's head
                        TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
                        seq.moveStart();
                        if (seq.moveNext()) {
                            state = S_NORMAL;
                        } else {
                            throw new IllegalStateException("Invalid state"); //NOI18N
                        }
                        break;

                    case S_EMBEDDED_TAIL:
                        // The current token contains embedded language and we have processed it's tail
                        sequences.remove(sequences.size() - 1);
                        state = moveTheSequence();
                        break;

                    case S_DONE:
                        // We have gone through all the tokens in all sequences
                        break;

                    default:
                        throw new IllegalStateException("Invalid state: " + state); //NOI18N
                }

                if (state == S_NORMAL) {
                    // We have moved to the next normal token, so look what it is
                    TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
                    TokenSequence<? extends TokenId> embeddedSeq = seq.embedded();
                    while (embeddedSeq != null && embeddedSeq.moveNext()) {
                        sequences.add(sequences.size(), embeddedSeq);
                        if (embeddedSeq.offset() > seq.offset()) {
                            state = S_EMBEDDED_HEAD;
                            break;
                        } else {
                            seq = embeddedSeq;
                            embeddedSeq = seq.embedded();
                        }
                    }
                } else if (state == S_DONE) {
                    attribsCache.clear();
                }

                return state != S_DONE;
            }
        }

        public int getStartOffset() {
            synchronized (SyntaxHighlighting.this) {
                switch (state) {
                    case S_NORMAL: {
                        TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
                        return seq.offset();
                    }
                    case S_EMBEDDED_HEAD: {
                        TokenSequence<? extends TokenId> embeddingSeq = sequences.get(sequences.size() - 2);
                        return embeddingSeq.offset();
                    }
                    case S_EMBEDDED_TAIL: {
                        TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
                        seq.moveEnd();
                        if (seq.movePrevious()) {
                            return seq.offset() + seq.token().length();
                        } else {
                            throw new IllegalStateException("Invalid state"); //NOI18N
                        }
                    }
                    case S_DONE:
                        throw new NoSuchElementException();

                    default:
                        throw new IllegalStateException("Invalid state " + state + ", call moveNext() first."); //NOI18N
                }
            }
        }

        public int getEndOffset() {
            synchronized (SyntaxHighlighting.this) {
                switch (state) {
                    case S_NORMAL: {
                        TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
                        return seq.offset() + seq.token().length();
                    }
                    case S_EMBEDDED_HEAD: {
                        TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
                        seq.moveStart();
                        if (seq.moveNext()) {
                            return seq.offset();
                        } else {
                            TokenSequence<? extends TokenId> embeddingSeq = sequences.get(sequences.size() - 2);
                            return embeddingSeq.offset() + embeddingSeq.token().length();
                        }
                    }
                    case S_EMBEDDED_TAIL:
                        TokenSequence<? extends TokenId> embeddingSeq = sequences.get(sequences.size() - 2);
                        return embeddingSeq.offset() + embeddingSeq.token().length();

                    case S_DONE:
                        throw new NoSuchElementException();

                    default:
                        throw new IllegalStateException("Invalid state " + state + ", call moveNext() first."); //NOI18N
                }
            }
        }

        public AttributeSet getAttributes() {
            synchronized (SyntaxHighlighting.this) {
                switch (state) {
                    case S_NORMAL:
                        return findAttribs(sequences.size() - 1);

                    case S_EMBEDDED_HEAD:
                    case S_EMBEDDED_TAIL:
                        return findAttribs(sequences.size() - 2);

                    case S_DONE:
                        throw new NoSuchElementException();

                    default:
                        throw new IllegalStateException("Invalid state " + state + ", call moveNext() first."); //NOI18N
                }
            }
        }
        
        private AttributeSet findAttribs(int seqIdx) {
            TokenSequence<? extends TokenId> seq = sequences.get(seqIdx);
            TokenId tokenId = seq.token().id();
            String mimePath;
            
            if (mimeTypeForHack != null) {
                mimePath = languagePathToMimePathHack(seq.languagePath());
            } else {
                mimePath = seq.languagePath().mimePath();
            }

            WeakHashMap<TokenId, AttributeSet> token2attribs = attribsCache.get(mimePath);
            if (token2attribs == null) {
                token2attribs = new WeakHashMap<TokenId, AttributeSet>();
                attribsCache.put(mimePath, token2attribs);
            }
            
            AttributeSet tokenAttribs = token2attribs.get(tokenId);
            if (tokenAttribs == null) {
                tokenAttribs = findTokenAttribs(tokenId, mimePath, seq.languagePath().innerLanguage());

                if (seqIdx > 0) {
                    AttributeSet embeddingTokenAttribs = findAttribs(seqIdx - 1);
                    tokenAttribs = AttributesUtilities.createComposite(
                        tokenAttribs, embeddingTokenAttribs);
                }
                
                token2attribs.put(tokenId, tokenAttribs);
            }
            
            return tokenAttribs;
        }

        private AttributeSet findTokenAttribs(TokenId tokenId, String mimePath, Language<? extends TokenId> innerLanguage) {
            FontColorSettings fcs = fcsCache.get(mimePath);
            
            if (fcs == null) {
                Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
                fcs = lookup.lookup(FontColorSettings.class);
                fcsCache.put(mimePath, fcs);
                
                if (fcs == null && LOG.isLoggable(Level.WARNING)) {
                    // Should not normally happen; see #106337
                    LOG.warning("No FontColorSettings for '" + mimePath + "' mime path."); //NOI18N
                }
            }
            
            AttributeSet attribs = fcs == null ? null : findFontAndColors(fcs, tokenId, innerLanguage);
            
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(tokenId(tokenId, false) + " -> {" + attributeSet(attribs) + "}"); //NOI18N
            }
            
            return attribs != null ? attribs : SimpleAttributeSet.EMPTY;
        }

        // XXX: This hack is here to make sure that preview panels in Tools-Options
        // work. Currently there is no way how to force a particular JTextComponent
        // to use a particular MimeLookup. They all use MimeLookup common for all components
        // and for the mime path of things displayed in that component. The preview panels
        // however need special MimeLookup that loads colorings from a special profile
        // (i.e. not the currently active coloring profile, which is used normally by
        // all the other components).
        //
        // The hack is that Tools-Options modifies mime type of the document loaded
        // in the preview panel and prepend 'textXXXX_' at the beginning. The normal
        // MimeLookup for this mime type and any mime path derived from this mime type
        // is empty. The editor/settings/storage however provides a special handling
        // for these 'test' mime paths and bridge them to the MimeLookup that you would
        // normally get for the mime path without the 'testXXXX_' at the beginning, plus
        // they supply special colorings from the profile called 'testXXXX'. This way
        // the preview panels can have different colorings from the rest of the IDE.
        //
        // This is obviously very fragile and not fully transparent for clients as
        // you can see here. We need a better solution for that. Generally it should
        // be posible to ask somewhere for a component-specific MimeLookup. This would
        // normally be a standard MimeLookup as you know it, but in special cases it
        // could be modified by the client code that created the component - e.g. Tools-Options
        // panel.
        private String languagePathToMimePathHack(LanguagePath languagePath) {
            if (languagePath.size() == 1) {
                return mimeTypeForHack;
            } else if (languagePath.size() > 1) {
                return mimeTypeForHack + "/" + languagePath.subPath(1).mimePath(); //NOI18N
            } else {
                throw new IllegalStateException("LanguagePath should not be empty."); //NOI18N
            }
        }
        
        private AttributeSet findFontAndColors(FontColorSettings fcs, TokenId tokenId, Language lang) {
            // First try the token's name
            String name = tokenId.name();
            AttributeSet attribs = fcs.getTokenFontColors(name);
            
            // Then try the primary category
            if (attribs == null) {
                String primary = tokenId.primaryCategory();
                if (primary != null) {
                    attribs = fcs.getTokenFontColors(primary);
                }
            }
            
            // Then try all the other categories
            if (attribs == null) {
                @SuppressWarnings("unchecked")
                List<String> categories = ((Language<TokenId>)lang).nonPrimaryTokenCategories(tokenId);
                for(String c : categories) {
                    attribs = fcs.getTokenFontColors(c);
                    if (attribs != null) {
                        break;
                    }
                }
            }
            
            return attribs;
        }
        
        private int moveTheSequence() {
            TokenSequence<? extends TokenId> seq = sequences.get(sequences.size() - 1);
            
            if (seq.moveNext() && seq.offset() < endOffset) {
                return S_NORMAL;
            } else {
                if (sequences.size() > 1) {
                    TokenSequence<? extends TokenId> embeddingSeq = sequences.get(sequences.size() - 2);
                    seq.moveEnd();
                    if (seq.movePrevious()) {
                        if ((seq.offset() + seq.token().length()) < (embeddingSeq.offset() + embeddingSeq.token().length())) {
                            return S_EMBEDDED_TAIL;
                        } else {
                            sequences.remove(sequences.size() - 1);
                            return moveTheSequence();
                        }
                    } else {
                        throw new IllegalStateException("Invalid state"); //NOI18N
                    }
                } else {
                    sequences.clear();
                    return S_DONE;
                }
            }
        }
        
        private boolean checkVersion() {
            return this.version == SyntaxHighlighting.this.version;
        }
    } // End of HSImpl class
}
