/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
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
import org.netbeans.lib.editor.util.ListenerList;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * The syntax coloring layer.
 * 
 * @author Vita Stejskal
 * @author Miloslav Metelka
 */
public final class SyntaxHighlighting extends AbstractHighlightsContainer
implements TokenHierarchyListener, ChangeListener {
    
    // -J-Dorg.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting.level=FINEST
    private static final Logger LOG = Logger.getLogger(SyntaxHighlighting.class.getName());
    
    public static final String LAYER_TYPE_ID = "org.netbeans.modules.editor.lib2.highlighting.SyntaxHighlighting"; //NOI18N
    
    /**
     * Static cache for colorings mapping mime-path to coloring info.
     */
    private static final HashMap<String, FCSInfo<?>> globalFCSCache = new HashMap<String, FCSInfo<?>>();

    /**
     * Local cache of items from globalFCSCache.
     */
    private final HashMap<String, FCSInfo<?>> fcsCache = new HashMap<String, FCSInfo<?>>();
    
    private final Document document;

    /**
     * Either null or a mime-type that starts with "test" and it's used
     * for preview in Tools/Options/Fonts-and-Colors.
     */
    private final String mimeTypeForOptions;

    private TokenHierarchy<? extends Document> hierarchy = null;

    private long version = 0;
    
    private AttributeSet cachedAttrs;
    
    /** Creates a new instance of SyntaxHighlighting */
    public SyntaxHighlighting(Document document) {
        this.document = document;
        String mimeType = (String) document.getProperty("mimeType"); //NOI18N
        if (mimeType != null && mimeType.startsWith("test")) { //NOI18N
            this.mimeTypeForOptions = mimeType;
        } else {
            this.mimeTypeForOptions = null;
        }
        
        // Start listening on changes in global colorings since they may affect colorings for target language
        findFCSInfo("", null);
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
        if (evt.type() == TokenHierarchyEventType.LANGUAGE_PATHS) {
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
    private String languagePathToMimePathOptions(LanguagePath languagePath) {
        if (languagePath.size() == 1) {
            return mimeTypeForOptions;
        } else if (languagePath.size() > 1) {
            return mimeTypeForOptions + "/" + languagePath.subPath(1).mimePath(); //NOI18N
        } else {
            throw new IllegalStateException("LanguagePath should not be empty."); //NOI18N
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireHighlightsChange(0, Integer.MAX_VALUE); // Recompute highlights for whole document
    }

    private <T extends TokenId> FCSInfo<T> findFCSInfo(String mimePath, Language<T> language) {
        @SuppressWarnings("unchecked")
        FCSInfo<T> fcsInfo = (FCSInfo<T>) fcsCache.get(mimePath); // Search local cache
        if (fcsInfo == null) { // Search in global cache
            synchronized (globalFCSCache) {
                @SuppressWarnings("unchecked")
                FCSInfo<T> fcsI = (FCSInfo<T>) globalFCSCache.get(mimePath);
                fcsInfo = fcsI;
                if (fcsInfo == null) {
                    fcsInfo = new FCSInfo<T>(mimePath, language);
                    if (mimeTypeForOptions == null) { // Only cache non-test ones globally
                        globalFCSCache.put(mimePath, fcsInfo);
                    }
                }
            }
            fcsInfo.addChangeListener(WeakListeners.change(this, fcsInfo));
            fcsCache.put(mimePath, fcsInfo);
        }
        return fcsInfo;
    }

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
        
        private List<TSInfo<?>> sequences;
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
                        sequences = new ArrayList<TSInfo<?>>();
                        
                        // initialize
                        @SuppressWarnings("unchecked")
                        TokenSequence<TokenId> seq = (TokenSequence<TokenId>) scanner.tokenSequence();
                        if (seq != null) {
                            seq.move(startOffset);
                            TSInfo<TokenId> tsInfo = new TSInfo<TokenId>(seq);
                            sequences.add(tsInfo);
                            state = S_NORMAL;
                        } else {
                            state = S_DONE;
                        }
                    }
                } else {
                    state = S_DONE;
                }

                while (state != S_DONE) {
                    switch (state) {
                        case S_NORMAL:
                            // The current token is a normal one
                            state = moveTheSequence();
                            break;

                        case S_EMBEDDED_HEAD:
                            // The current token contains embedded language and we have processed it's head
                            TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
                            tsInfo.ts.moveStart();
                            if (tsInfo.ts.moveNext()) {
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
                        TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
                        TokenSequence seq = tsInfo.ts;
                        TokenSequence<?> embeddedSeq = seq.embedded();
                        while (embeddedSeq != null && embeddedSeq.moveNext()) {
                            @SuppressWarnings("unchecked")
                            TokenSequence<TokenId> lts = (TokenSequence<TokenId>) embeddedSeq;
                            sequences.add(new TSInfo<TokenId>(lts));

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
                    }
                    
                    cachedAttrs = null;
                    if (state != S_DONE && getAttributes() != null) { // getAttributes() fills cachedAttrs
                        break;
                    }
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
                        TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
                        return Math.max(tsInfo.ts.offset(), startOffset);
                    }
                    case S_EMBEDDED_HEAD: {
                        TSInfo<?> embeddedTSInfo = sequences.get(sequences.size() - 2);
                        return Math.max(embeddedTSInfo.ts.offset(), startOffset);
                    }
                    case S_EMBEDDED_TAIL: {
                        TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
                        tsInfo.ts.moveEnd();
                        if (tsInfo.ts.movePrevious()) {
                            return Math.max(tsInfo.ts.offset() + tsInfo.ts.token().length(), startOffset);
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
                        TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
                        return Math.min(tsInfo.ts.offset() + tsInfo.ts.token().length(), endOffset);
                    }
                    case S_EMBEDDED_HEAD: {
                        TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
                        tsInfo.ts.moveStart();
                        if (tsInfo.ts.moveNext()) {
                            return Math.min(tsInfo.ts.offset(), endOffset);
                        } else {
                            TSInfo<?> embeddedTSInfo = sequences.get(sequences.size() - 2);
                            return Math.min(embeddedTSInfo.ts.offset() + embeddedTSInfo.ts.token().length(), endOffset);
                        }
                    }
                    case S_EMBEDDED_TAIL:
                        TSInfo<?> embeddedTSInfo = sequences.get(sequences.size() - 2);
                        return Math.min(embeddedTSInfo.ts.offset() + embeddedTSInfo.ts.token().length(), endOffset);

                    case S_DONE:
                        throw new NoSuchElementException();

                    default:
                        throw new IllegalStateException("Invalid state " + state + ", call moveNext() first."); //NOI18N
                }
            }
        }

        public @Override AttributeSet getAttributes() {
            if (cachedAttrs != null) {
                return cachedAttrs;
            }
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
            TSInfo<?> tsInfo = sequences.get(seqIdx);
            AttributeSet attrs = tsInfo.findCurrentAttrs();
            if (LOG.isLoggable(Level.FINE)) {
                // Add token info to the tooltip
                Token<?> token = tsInfo.ts.token();
                TokenId tokenId = token.id();
                attrs = AttributesUtilities.createComposite(
                    AttributesUtilities.createImmutable(EditorStyleConstants.Tooltip, 
                        "<html>" //NOI18N
                        + "<b>Token:</b> " + token.text() //NOI18N
                        + "<br><b>Id:</b> " + tokenId.name() //NOI18N
                        + "<br><b>Category:</b> " + tokenId.primaryCategory() //NOI18N
                        + "<br><b>Ordinal:</b> " + tokenId.ordinal() //NOI18N
                        + "<br><b>Mimepath:</b> " + tsInfo.ts.languagePath().mimePath() //NOI18N
                    ),
                    attrs 
                );
            }

            return attrs;
        }

        private int moveTheSequence() {
            TSInfo<?> tsInfo = sequences.get(sequences.size() - 1);
            
            if (tsInfo.ts.moveNext()) {
                if (tsInfo.ts.offset() < endOffset) {
                    return S_NORMAL;
                } else {
                    return S_DONE;
                }
            } else {
                if (sequences.size() > 1) {
                    TSInfo<?> embeddedTSInfo = sequences.get(sequences.size() - 2);
                    tsInfo.ts.moveEnd();
                    if (tsInfo.ts.movePrevious()) {
                        if ((tsInfo.ts.offset() + tsInfo.ts.token().length()) < (embeddedTSInfo.ts.offset() + embeddedTSInfo.ts.token().length())) {
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
    
    private static final class FCSInfo<T extends TokenId> implements LookupListener {
        
        static final ChangeEvent staticChangeEvent = new ChangeEvent(FCSInfo.class);
        
        private final Language<T> innerLanguage;
        
        private final String mimePath; // Can start with mimeTypeForOptions
    
        private final ListenerList<ChangeListener> listeners;

        private final Lookup.Result<FontColorSettings> result;
        
        private AttributeSet[] tokenId2attrs;
        
        FontColorSettings fcs;
        
        /**
         * @param innerLanguage
         * @param mimePath note it may start with mimeTypeForOptions
         */
        public FCSInfo(String mimePath, Language<T> innerLanguage) {
            this.innerLanguage = innerLanguage;
            this.mimePath = mimePath;
            this.listeners = new ListenerList<ChangeListener>();
            Lookup lookup = MimeLookup.getLookup(MimePath.parse(mimePath));
            result = lookup.lookupResult(FontColorSettings.class);
            result.addLookupListener(this);
            updateFCS();
        }
        
        /**
         * @param tokenId non-null token id.
         * @return attributes for tokenId or null if none found.
         */
        synchronized AttributeSet findAttrs(T tokenId) {
            AttributeSet attrs = tokenId2attrs[tokenId.ordinal()];
            if (attrs == null && fcs != null) {
                // First try the token's name
                String name = tokenId.name();
                attrs = fcs.getTokenFontColors(name);

                // Then try the primary category
                if (attrs == null) {
                    String primary = tokenId.primaryCategory();
                    if (primary != null) {
                        attrs = fcs.getTokenFontColors(primary);
                    }
                }

                // Then try all the other categories
                if (attrs == null) {
                    List<String> categories = innerLanguage.nonPrimaryTokenCategories(tokenId);
                    for (String c : categories) {
                        attrs = fcs.getTokenFontColors(c);
                        if (attrs != null) {
                            break;
                        }
                    }
                }
                
                tokenId2attrs[tokenId.ordinal()] = attrs;
            }

            return attrs;
        }

        public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void updateFCS() {
            FontColorSettings newFCS = result.allInstances().iterator().next();
            if (newFCS == null && LOG.isLoggable(Level.WARNING)) {
                // Should not normally happen; see #106337
                LOG.warning("No FontColorSettings for '" + mimePath + "' mime path."); //NOI18N
            }
            synchronized (this) {
                fcs = newFCS;
                if (innerLanguage != null) {
                    tokenId2attrs = new AttributeSet[innerLanguage.maxOrdinal() + 1];
                }
            }
        }

        @Override
        public void resultChanged(LookupEvent ev) {
            updateFCS();
            for (ChangeListener l : listeners.getListeners()) {
                l.stateChanged(staticChangeEvent);
            }
        }
   
    }
    
    private final class TSInfo<T extends TokenId> {
        
        final TokenSequence<T> ts;
        
        final FCSInfo<T> fcsInfo;

        /**
         * @param ts
         */
        public TSInfo(TokenSequence<T> ts) {
            this.ts = ts;
            LanguagePath languagePath = ts.languagePath();
            @SuppressWarnings("unchecked")
            Language<T> innerLanguage = (Language<T>)languagePath.innerLanguage();
            String mimePathExt;
            if (mimeTypeForOptions != null) {
                // First mime-type in mimePath starts with "test"
                mimePathExt = languagePathToMimePathOptions(languagePath);
            } else {
                mimePathExt = languagePath.mimePath();
            }

            fcsInfo = findFCSInfo(mimePathExt, innerLanguage);
        }
        
        AttributeSet findCurrentAttrs() {
            return findAttrs(ts.token().id());
        }
        
        AttributeSet findAttrs(T tokenId) {
            return fcsInfo.findAttrs(tokenId);
        }

    }

}
