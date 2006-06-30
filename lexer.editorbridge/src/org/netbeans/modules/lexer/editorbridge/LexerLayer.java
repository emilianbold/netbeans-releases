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

import java.util.Iterator;
import java.util.List;
import javax.swing.text.Document;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.editor.DrawLayer;
import org.netbeans.editor.DrawContext;
import org.netbeans.editor.MarkFactory;
import org.netbeans.editor.Coloring;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.swing.TokenElement;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class LexerLayer extends DrawLayer.AbstractLayer implements DocumentListener {
    
    public static final String NAME = "lexer-layer";
    
    public static final int VISIBILITY = 1050;
    
    private final LanguageDescriptor languageDescriptor;
    
    private int tokenEndOffset;
    
    private int tokenIndex;
    
    private Coloring coloring;
    
    private boolean active;
    
    private boolean documentModified;
    
    public static TokenRootElement getTokenRootElement(Document doc) {
        return (TokenRootElement)doc.getProperty(TokenRootElement.class);
    }
    
    public LexerLayer(LanguageDescriptor languageDescriptor, JTextComponent target) {
        super(NAME);

        this.languageDescriptor = languageDescriptor;
        
        Document doc = target.getDocument();
        target.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("document".equals(evt.getPropertyName())) {
                        Document old = (Document)evt.getOldValue();
                        if (old != null) {
                            old.removeDocumentListener(LexerLayer.this);
                        }
                        Document cur = (Document)evt.getNewValue();
                        if (cur != null) {
                            checkDocument(cur);
                        }
                    }
                }
            }
        );
        
        checkDocument(doc);
    }
    
    private void checkDocument(Document doc) {
        doc.addDocumentListener(this);
        if (TokenRootElement.get(doc) == null) {
            createTokenRootElement(doc);
        }
    }
    
    protected TokenRootElement createTokenRootElement(Document doc) {
        return new TokenRootElement(doc, languageDescriptor.getLanguage());
    }
    
    private TokenRootElement getTokenRootElement(DrawContext ctx) {
        Document doc = ctx.getEditorUI().getDocument();
        return TokenRootElement.get(doc);
    }
    
    private Coloring findColoring(DrawContext ctx, Token token) {
        TokenId id = token.getId();
        String coloringName = languageDescriptor.getColoringPrefix()
            + id.getName();
        Coloring c = ctx.getEditorUI().getColoring(coloringName);

        if (c == null) { // no direct name for the token -> try categories
            List catNames = id.getCategoryNames();
            for (Iterator it = catNames.iterator(); c == null && it.hasNext();) {
                coloringName = languageDescriptor.getColoringPrefix()
                    + (String)it.next();
                c = ctx.getEditorUI().getColoring(coloringName);
            }
        }
        
        return c;
    }
    
    public boolean extendsEOL() {
        return true;
    }
    
    public void init(final DrawContext ctx) {
        tokenIndex = 0;
        tokenEndOffset = 0;

        int startOffset = ctx.getStartOffset();
        int endOffset = ctx.getEndOffset();

        final TokenRootElement tre = getTokenRootElement(ctx);
        active = (tre != null);

        // Check whether larger repaint is necessary
        if (active && documentModified) {
            documentModified = false;
//            System.out.println("startOffset=" + startOffset + ", endOffset=" + endOffset + ", tre.getLastUpdateStartOffset()=" + tre.getLastUpdateStartOffset() + ", tre.getLastUpdateEndOffset()=" + tre.getLastUpdateEndOffset());
            if (startOffset > tre.getLastUpdateStartOffset()
                || endOffset < tre.getLastUpdateEndOffset()
            ) {
                javax.swing.SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            org.netbeans.editor.EditorUI editorUI = ctx.getEditorUI();
                            javax.swing.text.JTextComponent c = editorUI.getComponent();
                            if (c != null) {
                                javax.swing.plaf.TextUI ui = (javax.swing.plaf.TextUI)c.getUI();
                                ui.damageRange(c, tre.getLastUpdateStartOffset(), tre.getLastUpdateEndOffset());
                            }
                        }
                    }
                );
            }
        }
                
        if (active) {
            tokenIndex = tre.getElementIndex(startOffset);
            if (tokenIndex >= 0) {
                int tokenOffset = tre.getElementOffset(tokenIndex);
                int tokenCount = tre.getElementCount();
                TokenElement tokenElement;
                if (tokenIndex + 1 == tokenCount) { // could be past the end
                    tokenElement = (TokenElement)tre.getElement(tokenIndex);
                    if (tokenOffset + (tokenElement.getEndOffset()
                        - tokenElement.getStartOffset()) <= startOffset
                    ) { // past the last token
                        active = false;
                    }

                } else { // not the last token
                    tokenElement = (TokenElement)tre.getElement(tokenIndex);
                }

                if (active) {
                    tokenEndOffset = tokenOffset + getTokenElementLength(tokenElement);
                    setNextActivityChangeOffset(tokenEndOffset);
                    coloring = findColoring(ctx, tokenElement);
                }

            } else { // no tokens
                active = false;
            }
        }
        
        if (coloring != null) {
            coloring.apply(ctx);
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
            TokenRootElement tre = getTokenRootElement(ctx);
            active = (tre != null);
            if (active) {
                int tokenCount = tre.getElementCount();
                tokenIndex++;
                if (tokenIndex >= tokenCount) {
                    active = false;
                }
                if (active) {
                    TokenElement tokenElement = (TokenElement)tre.getElement(tokenIndex);
                    int len = getTokenElementLength(tokenElement);
                    tokenEndOffset += len;
                    
                    coloring = findColoring(ctx, tokenElement);
                    setNextActivityChangeOffset(tokenEndOffset);
                }
            }
        }
    }
    
    private static int getTokenElementLength(TokenElement tokenElement) {
        return tokenElement.getEndOffset() - tokenElement.getStartOffset();
    }
    
    public void changedUpdate(DocumentEvent e) {
    }
    
    public void insertUpdate(DocumentEvent e) {
        documentModified = true;
    }
    
    public void removeUpdate(DocumentEvent e) {
        documentModified = true;
    }
    
}

