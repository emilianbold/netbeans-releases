/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.editor.lib2.highlighting;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
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
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyEventType;
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

    public @Override HighlightsSequence getHighlights(int startOffset, int endOffset) {
        synchronized (this) {
            if (hierarchy == null) {
                hierarchy = TokenHierarchy.get(document);
                hierarchy.addTokenHierarchyListener(WeakListeners.create(TokenHierarchyListener.class, this, hierarchy));
            }

            if (hierarchy.isActive()) {
                return new HSImpl(version, hierarchy, startOffset, endOffset);
            } else {
                return HighlightsSequence.EMPTY;
            }
        }
    }

    // ----------------------------------------------------------------------
    //  TokenHierarchyListener implementation
    // ----------------------------------------------------------------------

    public @Override void tokenHierarchyChanged(TokenHierarchyEvent evt) {
        if (evt.type() == TokenHierarchyEventType.ACTIVITY || evt.type() == TokenHierarchyEventType.LANGUAGE_PATHS) {
            // ignore
            return;
        }
        
        synchronized (this) {
            version++;
        }

        if (LOG.isLoggable(Level.FINEST)) {
            StringBuilder sb = new StringBuilder();
            TokenSequence<?> ts = hierarchy.tokenSequence();
            
            sb.append("\n"); //NOI18N
            sb.append("Tokens after change: <").append(evt.affectedStartOffset()).append(", ").append(evt.affectedEndOffset()).append(">\n"); //NOI18N
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

    private static void dumpSequence(TokenSequence<?> seq, StringBuilder sb) {
        if (seq == null) {
            sb.append("Inactive TokenHierarchy"); //NOI18N
        } else {
            for(seq.moveStart(); seq.moveNext(); ) {
                TokenSequence<?> emSeq = seq.embedded();
                if (emSeq != null) {
                    dumpSequence(emSeq, sb);
                } else {
                    Token<?> token = seq.token();
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
    }
    
    private static String tokenId(TokenId tokenId, boolean format) {
        if (format) {
            return String.format("%20s.%-15s", tokenId.getClass().getSimpleName(), tokenId.name()); //NOI18N
        } else {
            return tokenId.getClass().getSimpleName() + "." + tokenId.name(); //NOI18N
        }
    }
    
    private static String tokenText(Token<?> token) {
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

        private final long version;
        private final TokenHierarchy<? extends Document> scanner;
        private final int startOffset;
        private final int endOffset;
        
        private List<TokenSequence<?>> sequences;
        private int state = -1;
        private LogHelper logHelper;
        
        public HSImpl(long version, TokenHierarchy<? extends Document> scanner, int startOffset, int endOffset) {
            this.version = version;
            this.scanner = scanner;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.sequences = null;
            if (LOG.isLoggable(Level.FINE)) {
                logHelper = new LogHelper();
                logHelper.startTime = System.currentTimeMillis();
            }
        }
        
        public @Override boolean moveNext() {
            synchronized (SyntaxHighlighting.this) {
                if (checkVersion()) {
                    if (sequences == null) {
                        sequences = new ArrayList<TokenSequence<?>>();
                        
                        // initialize
                        TokenSequence<?> seq = scanner.tokenSequence();
                        if (seq != null) {
                            try {
                                seq.move(startOffset);
                                sequences.add(seq);
                                state = S_NORMAL;
                            } catch (ConcurrentModificationException cme) {
                                state = S_DONE;
                            }
                        } else {
                            state = S_DONE;
                        }
                    }
                } else {
                    state = S_DONE;
                }

                try {
                    switch (state) {
                        case S_NORMAL:
                            // The current token is a normal one
                            state = moveTheSequence();
                            break;

                        case S_EMBEDDED_HEAD:
                            // The current token contains embedded language and we have processed it's head
                            TokenSequence<?> seq = sequences.get(sequences.size() - 1);
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
                } catch (ConcurrentModificationException cme) {
                    state = S_DONE;
                }

                if (state == S_NORMAL) {
                    try {
                        // We have moved to the next normal token, so look what it is
                        TokenSequence<?> seq = sequences.get(sequences.size() - 1);
                        TokenSequence<?> embeddedSeq = seq.embedded();
                        while (embeddedSeq != null && embeddedSeq.moveNext()) {
                            sequences.add(sequences.size(), embeddedSeq);

                            if (embeddedSeq.offset() + embeddedSeq.token().length() < startOffset) {
                                embeddedSeq.move(startOffset);
                                if (!embeddedSeq.moveNext()) {
                                    state = S_EMBEDDED_TAIL;
                                    break;
                                }
                            } else if (embeddedSeq.offset() > seq.offset()) {
                                state = S_EMBEDDED_HEAD;
                                break;
                            }

                            seq = embeddedSeq;
                            embeddedSeq = seq.embedded();
                        }
                    } catch (ConcurrentModificationException cme) {
                        state = S_DONE;
                    }
                }

                if (state == S_DONE) {
                    attribsCache.clear();
                }

                if (LOG.isLoggable(Level.FINE)) {
                    if (state != S_DONE) {
                        logHelper.tokenCount++;
                    } else {
                        LOG.fine("SyntaxHighlighting for " + scanner.inputSource() + //NOI18N
                                ":\n-> returned " + logHelper.tokenCount + " token highlights for <" + //NOI18N
                                startOffset + "," + endOffset + //NOI18N
                                "> in " + //NOI18N
                                (System.currentTimeMillis() - logHelper.startTime) + " ms.\n"); //NOI18N
                    }
                }

//                if (state != S_DONE) {
//                    TokenSequence<?> seq = sequences.get(sequences.size() - 1);
//                    LOG.fine("HSImpl@" + Integer.toHexString(System.identityHashCode(this)) + " <" + startOffset + ", " + endOffset + ">: " +
//                        "state=" + state + ", positioned at <" + getStartOffset() + ", " + getEndOffset() + ">");
//                } else {
//                    LOG.fine("HSImpl@" + Integer.toHexString(System.identityHashCode(this)) + " <" + startOffset + ", " + endOffset + ">: " +
//                        "S_DONE");
//                }

                return state != S_DONE;
            }
        }

        public @Override int getStartOffset() {
            synchronized (SyntaxHighlighting.this) {
                switch (state) {
                    case S_NORMAL: {
                        TokenSequence<?> seq = sequences.get(sequences.size() - 1);
                        return Math.max(seq.offset(), startOffset);
                    }
                    case S_EMBEDDED_HEAD: {
                        TokenSequence<?> embeddingSeq = sequences.get(sequences.size() - 2);
                        return Math.max(embeddingSeq.offset(), startOffset);
                    }
                    case S_EMBEDDED_TAIL: {
                        TokenSequence<?> seq = sequences.get(sequences.size() - 1);
                        seq.moveEnd();
                        if (seq.movePrevious()) {
                            return Math.max(seq.offset() + seq.token().length(), startOffset);
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

        public @Override int getEndOffset() {
            synchronized (SyntaxHighlighting.this) {
                switch (state) {
                    case S_NORMAL: {
                        TokenSequence<?> seq = sequences.get(sequences.size() - 1);
                        return Math.min(seq.offset() + seq.token().length(), endOffset);
                    }
                    case S_EMBEDDED_HEAD: {
                        TokenSequence<?> seq = sequences.get(sequences.size() - 1);
                        seq.moveStart();
                        if (seq.moveNext()) {
                            return Math.min(seq.offset(), endOffset);
                        } else {
                            TokenSequence<?> embeddingSeq = sequences.get(sequences.size() - 2);
                            return Math.min(embeddingSeq.offset() + embeddingSeq.token().length(), endOffset);
                        }
                    }
                    case S_EMBEDDED_TAIL:
                        TokenSequence<?> embeddingSeq = sequences.get(sequences.size() - 2);
                        return Math.min(embeddingSeq.offset() + embeddingSeq.token().length(), endOffset);

                    case S_DONE:
                        throw new NoSuchElementException();

                    default:
                        throw new IllegalStateException("Invalid state " + state + ", call moveNext() first."); //NOI18N
                }
            }
        }

        public @Override AttributeSet getAttributes() {
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
            TokenSequence<?> seq = sequences.get(seqIdx);
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
                token2attribs.put(tokenId, tokenAttribs);
            }

            if (LOG.isLoggable(Level.FINE)) {
                // Add token info to the tooltip
                tokenAttribs = AttributesUtilities.createComposite(
                    AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, 
                        "<html>" //NOI18N
                        + "<b>Token:</b> " + seq.token().text() //NOI18N
                        + "<br><b>Id:</b> " + tokenId.name() //NOI18N
                        + "<br><b>Category:</b> " + tokenId.primaryCategory() //NOI18N
                        + "<br><b>Ordinal:</b> " + tokenId.ordinal() //NOI18N
                        + "<br><b>Mimepath:</b> " + mimePath //NOI18N
                    ),
                    tokenAttribs 
                );
            }

            return tokenAttribs;
        }

        private AttributeSet findTokenAttribs(TokenId tokenId, String mimePath, Language<?> innerLanguage) {
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
            TokenSequence<?> seq = sequences.get(sequences.size() - 1);
            
            if (seq.moveNext()) {
                if (seq.offset() < endOffset) {
                    return S_NORMAL;
                } else {
                    return S_DONE;
                }
            } else {
                if (sequences.size() > 1) {
                    TokenSequence<?> embeddingSeq = sequences.get(sequences.size() - 2);
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

    private static final class LogHelper {

        int tokenCount;
        long startTime;
    }

}
