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
package org.netbeans.modules.java.editor.semantic;

import java.util.Map;
import javax.swing.text.AttributeSet;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.editor.semantic.ColoringAttributes.Coloring;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;

/**
 *
 * @author Jan Lahoda
 */
public class LexerBasedHighlightSequence implements HighlightsSequence {
    
    private LexerBasedHighlightLayer layer;
    private Map<Token, Coloring> colorings;
    private TokenSequence ts;
    private boolean started;
    
    public LexerBasedHighlightSequence(LexerBasedHighlightLayer layer, TokenSequence ts, Map<Token, Coloring> colorings) {
        this.layer = layer;
        this.ts = ts;
        this.colorings = colorings;
    }
    
    private boolean moveNextImpl() {
        if (started) {
            return ts.moveNext();
        } else {
            started = true;
            
            return ts.moveNext();
        }
    }

    public boolean moveNext() {
        while (moveNextImpl()) {
            Token t = ts.token();
            if (t.id() == JavaTokenId.IDENTIFIER && colorings.containsKey(ts.token()))
                return true;
        }
        
        return false;
    }
    
    public int getStartOffset() {
        return ts.offset();
    }

    public int getEndOffset() {
        return ts.offset() + ts.token().length();
    }

    public AttributeSet getAttributes() {
        return layer.getColoring(colorings.get(ts.token()));
    }

}
