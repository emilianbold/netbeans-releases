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

package org.netbeans.modules.lexer.editorbridge;

import java.awt.Color;
import java.awt.Font;
import java.util.List;
import java.util.Stack;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchyEvent;
import org.netbeans.api.lexer.TokenHierarchyListener;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.MarkFactory;
import org.netbeans.editor.Coloring;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.text.EditorKit;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;

class LexerLayer extends DrawLayer.AbstractLayer {

    private static final boolean debug = Boolean.getBoolean("netbeans.debug.lexer.layer");
    
    public static final String NAME = "lexer-layer";
    
    public static final int VISIBILITY = 1050;

    private final JTextComponent component;

    private FontColorSettings fontColorSettings;

    private final Map<LanguagePath, Map<TokenId, Coloring>> token2Coloring= new WeakHashMap<LanguagePath, Map<TokenId, Coloring>>();

    private Listener listener;

    private TokenHierarchy listenerHierarchy;

    private Stack<TokenSequence> pastSequences;
    private TokenSequence tokenSequence;
    private boolean moveNext = true;
    private boolean goToEmbed = true;

    private int tokenEndOffset;
    
    private Coloring coloring;
    
    private boolean active;
    
    
    public LexerLayer(JTextComponent component) {
        super(NAME);

        assert (component != null);
        this.component = component;

        Document doc = component.getDocument();
        component.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("document".equals(evt.getPropertyName())) {
                        // Remove old listening
                        if (listenerHierarchy != null) {
                            listenerHierarchy.removeTokenHierarchyListener(listener);
                        }
                    }
                }
            }
        );
    }
    
    public boolean extendsEOL() {
        return true;
    }
    
    public void init(final DrawContext ctx) {
        coloring = null;
        tokenEndOffset = 0;
        int startOffset = ctx.getStartOffset();
        String mimeType = component.getUI().getEditorKit(component).getContentType();
        fontColorSettings = (FontColorSettings)MimeLookup.getMimeLookup(mimeType).lookup(FontColorSettings.class);

        TokenHierarchy hi = tokenHierarchy();
        active = (hi != null) && (fontColorSettings != null);

        if (active) {
            pastSequences = new Stack<TokenSequence>();
            tokenSequence = hi.tokenSequence();
            int relOffset = tokenSequence.move(startOffset);
            if (relOffset != Integer.MAX_VALUE) {
                updateTokenEndOffsetAndColoring(startOffset);
            } else { // no tokens
                active = false;
            }
        }
    }
    
    public boolean isActive(DrawContext ctx, MarkFactory.DrawMark mark) {
        return active;
    }
    
    public void updateContext(DrawContext ctx) {
        if (coloring != null) {
            coloring.apply(ctx);
        }

        int fragEndOffset = ctx.getFragmentOffset() + ctx.getFragmentLength();
        while (active && fragEndOffset >= tokenEndOffset) {
            if (!moveNext || tokenSequence.moveNext()) {
                updateTokenEndOffsetAndColoring(-1);
            } else {
                if (pastSequences.isEmpty()) {
                    active = false;
                } else {
                    tokenSequence = pastSequences.pop();
                    if ((tokenSequence.offset() + tokenSequence.token().length()) > tokenEndOffset) {
                        //highlight the rest of the popped token:
                        goToEmbed = false;
                        moveNext = false;
                    }
                }
            }
        }
    }

    private TokenHierarchy tokenHierarchy() {
        TokenHierarchy hi = TokenHierarchy.get(component.getDocument());
        // Possibly start listening on the token changes in the hierarchy
        if (hi != null && listenerHierarchy == null) {
            if (listener == null) {
                listener = new Listener();
            }
            listenerHierarchy = hi;
            listenerHierarchy.addTokenHierarchyListener(listener);
        }
        return hi;
    }
    
    private void updateTokenEndOffsetAndColoring(int offset) {
        int origOffset = tokenSequence.offset();
        boolean isInside = tokenSequence.offset() < offset;
        Token origToken = tokenSequence.token();
        LanguagePath origPath = tokenSequence.languagePath();
        boolean wasEmbedd = false;
        
        while (origOffset == tokenSequence.offset() && goToEmbed) {
            TokenSequence embed = tokenSequence.embedded();
            
            if (embed != null) {
                wasEmbedd = true;
                if (offset == (-1)) {
                    if (embed.moveNext()) {
                        pastSequences.push(tokenSequence);
                        tokenSequence = embed;
                    } else {
                        break;
                    }
                } else {
                    if (embed.move(offset) != Integer.MAX_VALUE) {
                        pastSequences.push(tokenSequence);
                        tokenSequence = embed;
                    } else {
                        break;
                    }
                }
            } else {
                break;
            }
        }
        
        goToEmbed = true;
        
        Token token;
        LanguagePath path;
        
        if (origOffset == tokenSequence.offset() || isInside) {
            token = tokenSequence.token();
            tokenEndOffset = tokenSequence.offset() + token.length();
            path = tokenSequence.languagePath();
            moveNext = true;
        } else {
            token = origToken;
            tokenEndOffset = tokenSequence.offset();
            path = origPath;
            moveNext = !wasEmbedd;
        }
        
        setNextActivityChangeOffset(tokenEndOffset);
        coloring = findColoring(token, path, fontColorSettings);
    }

    private static final Coloring NULL_COLORING = new Coloring();
    
    private Coloring findColoring(Token token,
    LanguagePath languagePath, FontColorSettings fcs) {
        TokenId id = token.id();
        Map<TokenId, Coloring> id2Coloring = token2Coloring.get(languagePath);
        
        if (id2Coloring == null) {
            token2Coloring.put(languagePath, id2Coloring = new WeakHashMap<TokenId, Coloring>());
        }
        
        Coloring c = id2Coloring.get(id);
        
        if (c == null) {
            AttributeSet as = findColoringImpl(id, languagePath, fcs);
            
            if (as == null) {
                c = NULL_COLORING;
            } else {
                c = toColoring(as);
            }
            
            id2Coloring.put(id, c);
        }
        
        return c;
    }
    
    private AttributeSet findColoringImpl(TokenId id,
    LanguagePath languagePath, FontColorSettings fcs) {
        AttributeSet as = fcs.getTokenFontColors(updateColoringName(id.name()));

        if (as == null) { // no direct name for the token -> try primary category
            String primaryCategory = id.primaryCategory();
            if (primaryCategory == null || ((as = fcs.getTokenFontColors(
                    updateColoringName(primaryCategory))) == null)
            ) {
                @SuppressWarnings("unchecked") List<String> cats
                        = ((Language<TokenId>)languagePath.innerLanguage()).nonPrimaryTokenCategories(id);
                for (int i = 0; i < cats.size(); i++) {
                    String cat = (String)cats.get(i);
                    as = fcs.getTokenFontColors(updateColoringName(cat));
                    if (as != null) {
                        if (debug) {
                            /*DEBUG*/System.err.println("Coloring found for category " + cat + ": " + as);
                        }
                        break;
                    }
                }
            } else if (debug) { // valid coloring for primary category and in debugging mode
                /*DEBUG*/System.err.println("Coloring found for primary category " + primaryCategory + ": " + as);
            }
        } else if (debug) { // valid coloring found and in debug mode
            /*DEBUG*/System.err.println("Coloring found for id=" + id + ": " + as);
        }
        
        return as;
    }

    private String updateColoringName(String coloringName) {
        EditorKit kit = component.getUI().getEditorKit(component);
        if (kit instanceof LexerEditorKit) {
            coloringName = ((LexerEditorKit)kit).updateColoringName(coloringName);
        }
        return coloringName;
    }
    
    private Coloring toColoring(AttributeSet as) {
        int fontApplyMode = 0;
        int fontStyle = 0;
        int fontSize;
        String fontFamily = (String)as.getAttribute(StyleConstants.FontFamily);
        Integer sz = (Integer)as.getAttribute(StyleConstants.FontSize);
        boolean bold = Boolean.TRUE.equals(as.getAttribute(StyleConstants.Bold));
        boolean italic = Boolean.TRUE.equals(as.getAttribute(StyleConstants.Italic));
        if (fontFamily != null) {
            fontApplyMode |= Coloring.FONT_MODE_APPLY_NAME;
        } else {
            fontFamily = "Monospaced";
        }
        if (sz != null) {
            fontSize = sz.intValue();
            fontApplyMode |= Coloring.FONT_MODE_APPLY_SIZE;
        } else {
            fontSize = 10;
        }
        if (bold) {
            fontStyle |= Font.BOLD;
            fontApplyMode |= Coloring.FONT_MODE_APPLY_STYLE;
        }
        if (italic) {
            fontStyle |= Font.ITALIC;
            fontApplyMode |= Coloring.FONT_MODE_APPLY_STYLE;
        }
        
        Font font = new Font(
                fontFamily,
                fontStyle,
                fontSize
                );
        
        return new Coloring(
                font,
                fontApplyMode,
                (Color) as.getAttribute(StyleConstants.Foreground),
                (Color) as.getAttribute(StyleConstants.Background),
                (Color) as.getAttribute(StyleConstants.Underline),
                (Color) as.getAttribute(StyleConstants.StrikeThrough),
                (Color) as.getAttribute(EditorStyleConstants.WaveUnderlineColor)
                );
        
    }

    private final class Listener implements TokenHierarchyListener {

        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            javax.swing.plaf.TextUI ui = (javax.swing.plaf.TextUI)component.getUI();
            int startRepaintOffset = evt.affectedStartOffset();
            int endRepaintOffset = Math.max(evt.affectedEndOffset(), startRepaintOffset + 1);
            ui.damageRange(component, startRepaintOffset, endRepaintOffset);
        }

    }

}

