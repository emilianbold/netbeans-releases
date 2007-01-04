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
import java.util.Enumeration;
import java.util.WeakHashMap;
import javax.swing.text.EditorKit;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.EditorStyleConstants;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.openide.util.Lookup;

final class LexerLayer extends DrawLayer.AbstractLayer {

    public static final String NAME = "lexer-layer"; //NOI18N
    
    public static final int VISIBILITY = 1050;

    private static final Coloring NULL_COLORING = new Coloring();
    
    private final JTextComponent component;

    private final WeakHashMap<AttributeSet, Coloring> colorings = new WeakHashMap<AttributeSet, Coloring>();
    
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
                    if ("document".equals(evt.getPropertyName())) { //NOI18N
                        // Remove old listening
                        if (listenerHierarchy != null) {
                            listenerHierarchy.removeTokenHierarchyListener(listener);
                            listenerHierarchy = null;
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

        TokenHierarchy hi = tokenHierarchy();
        active = (hi != null);

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
        coloring = findColoring(token.id(), path);
    }

    private Coloring findColoring(TokenId tokenId, LanguagePath languagePath) {
        MimePath mimePath = languagePathToMimePathHack(languagePath);
        Lookup lookup = MimeLookup.getLookup(mimePath);
        FontColorSettings fcs = lookup.lookup(FontColorSettings.class);
        AttributeSet attribs = findFontAndColors(fcs, tokenId, languagePath.innerLanguage());
     
//        dumpAttribs(attribs, tokenId.name(), languagePath.mimePath());
        
        return attribs == null ? NULL_COLORING : toColoring(attribs);
    }

    private void dumpAttribs(AttributeSet attribs, String token, String lang) {
//        if (!lang.contains("xml")) {
//            return;
//        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Attribs for token '"); //NOI18N
        sb.append(token);
        sb.append("', language '"); //NOI18N
        sb.append(lang);
        sb.append("' = {"); //NOI18N
        
        if (attribs != null) {
            Enumeration<?> keys = attribs.getAttributeNames();
            while (keys.hasMoreElements()) {
                Object key = keys.nextElement();
                Object value = attribs.getAttribute(key);

                sb.append("'" + key + "' = '" + value + "'"); //NOI18N
                if (keys.hasMoreElements()) {
                    sb.append(", "); //NOI18N
                }
            }
        }
        
        sb.append("} LexerLayer.this = "); //NOI18N
        sb.append(this.toString());
        
        System.out.println(sb.toString());
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
    private MimePath languagePathToMimePathHack(LanguagePath languagePath) {
        String mimeType = getMimeType(component);
        if (languagePath.size() == 1) {
            return MimePath.parse(mimeType);
        } else if (languagePath.size() > 1) {
            return MimePath.parse(mimeType + "/" + languagePath.subPath(1).mimePath()); //NOI18N
        } else {
            throw new IllegalStateException("LanguagePath should not be empty."); //NOI18N
        }
    }

    private String getMimeType(JTextComponent c) {
        Object mimeTypeProp = c.getDocument().getProperty("mimeType"); //NOI18N
        if (mimeTypeProp instanceof String) {
            return (String) mimeTypeProp;
        } else {
            return c.getUI().getEditorKit(c).getContentType();
        }
    }
    
    private String updateColoringName(String coloringName) {
        EditorKit kit = component.getUI().getEditorKit(component);
        if (kit instanceof LexerEditorKit) {
            String updatedName = ((LexerEditorKit)kit).updateColoringName(coloringName);
            if (updatedName != null) {
                coloringName = updatedName;
            }
        }
        return coloringName;
    }
    
    private AttributeSet findFontAndColors(FontColorSettings fcs, TokenId tokenId, Language lang) {
        // First try the token's name
        String name = tokenId.name();
        AttributeSet attribs = fcs.getTokenFontColors(updateColoringName(name));

        // Then try the primary category
        if (attribs == null) {
            String primary = tokenId.primaryCategory();
            if (primary != null) {
                attribs = fcs.getTokenFontColors(updateColoringName(primary));
            }
        }

        // Then try all the other categories
        if (attribs == null) {
            @SuppressWarnings("unchecked") //NOI18N
            List<String> categories = ((Language<TokenId>)lang).nonPrimaryTokenCategories(tokenId);
            for(String c : categories) {
                attribs = fcs.getTokenFontColors(updateColoringName(c));
                if (attribs != null) {
                    break;
                }
            }
        }

        return attribs;
    }

    private Coloring toColoring(AttributeSet as) {
        synchronized (colorings) {
            Coloring coloring = colorings.get(as);

            if (coloring == null) {
                Object [] fontObj = toFont(as);

                coloring = new Coloring(
                    (Font) fontObj[0],
                    ((Integer) fontObj[1]).intValue(),
                    (Color) as.getAttribute(StyleConstants.Foreground),
                    (Color) as.getAttribute(StyleConstants.Background),
                    (Color) as.getAttribute(StyleConstants.Underline),
                    (Color) as.getAttribute(StyleConstants.StrikeThrough),
                    (Color) as.getAttribute(EditorStyleConstants.WaveUnderlineColor)
                );

                colorings.put(as, coloring);
            }

            return coloring;
        }
    }

    private Object [] toFont(AttributeSet as) {
        int applyMode = 0;

        // Determine font family
        String fontFamily = null;
        {
            Object fontFamilyObj = as.getAttribute(StyleConstants.FontFamily);
            if (fontFamilyObj instanceof String) {
                fontFamily = (String) fontFamilyObj;
                applyMode += Coloring.FONT_MODE_APPLY_NAME;
            }
        }

        // Determine font size
        int fontSize = 0;
        {
            Object fontSizeObj = as.getAttribute(StyleConstants.FontSize);
            if (fontSizeObj instanceof Integer) {
                fontSize = ((Integer) fontSizeObj).intValue();
                applyMode += Coloring.FONT_MODE_APPLY_SIZE;
            }
        }

        // Determine font style
        int style = 0;
        {
            Object boldStyleObj = as.getAttribute(StyleConstants.Bold);
            Object italicStyleObj = as.getAttribute(StyleConstants.Italic);

            if (boldStyleObj != null || italicStyleObj != null) {
                if (Boolean.TRUE.equals(boldStyleObj)){
                    style += Font.BOLD;
                }

                if (Boolean.TRUE.equals(italicStyleObj)){
                    style += Font.ITALIC;
                }

                applyMode += Coloring.FONT_MODE_APPLY_STYLE;
            }
        }

        // TODO: cache the Font objects somehow
        return new Object [] {
            new Font(fontFamily, style, fontSize),
            new Integer(applyMode)
        };
    }
    
    private final class Listener implements TokenHierarchyListener {

        public void tokenHierarchyChanged(TokenHierarchyEvent evt) {
            javax.swing.plaf.TextUI ui = (javax.swing.plaf.TextUI)component.getUI();
            int startRepaintOffset = evt.affectedStartOffset();
            int endRepaintOffset = Math.max(evt.affectedEndOffset(), startRepaintOffset + 1);
            ui.damageRange(component, startRepaintOffset, endRepaintOffset);
        }

    } // End of Listener class

}

