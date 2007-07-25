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
package org.netbeans.modules.languages.features;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;


/**
 *
 * @author Jan Jancura
 */
class TokenHighlightsLayer extends AbstractHighlightsContainer {

    private Highlighting            highlighting;
    private TokenHierarchy          hierarchy;
    private Document                document;

    
    TokenHighlightsLayer (final Document document) {
        highlighting = Highlighting.getHighlighting (document);
        hierarchy = TokenHierarchy.get (document);
        this.document = document;
        highlighting.addPropertyChangeListener (new PropertyChangeListener () {
            public void propertyChange (final PropertyChangeEvent evt) {
                fireHighlightsChange ((Integer) evt.getOldValue (), (Integer) evt.getNewValue ());
            }
        });
    }
    
    public HighlightsSequence getHighlights (int startOffset, int endOffset) {
        return new Highlights (document, highlighting, hierarchy, startOffset, endOffset);
    }
    
    
    private static class Highlights implements HighlightsSequence {

        private int                 endOffset;
        private int                 startOffset1;
        private int                 endOffset1;
        private SimpleAttributeSet  attributeSet;
        private Highlighting        highlighting;
        private TokenHierarchy      hierarchy;
        private Document            document;
        
        private Highlights (
            Document document,
            Highlighting highlighting, 
            TokenHierarchy hierarchy, 
            int startOffset, 
            int endOffset
        ) {
            this.document = document;
            this.endOffset = endOffset;
            this.highlighting = highlighting;
            this.hierarchy = hierarchy;
            endOffset1 = startOffset;
        }
        
        public boolean moveNext () {
            attributeSet = new SimpleAttributeSet ();
            startOffset1 = endOffset1;
            TokenSequence ts = hierarchy.tokenSequence ();
            AttributeSet as = null;
            do {
                do {
                    ts.move (startOffset1);
                    if (!ts.moveNext ())
                        return endOffset1 > startOffset1;
                    Token t = ts.token ();
                    ASTToken stoken = ASTToken.create (
                        ts.language ().mimeType (),
                        t.id ().name (),
                        t.text ().toString (),
                        ts.offset ()
                    );
                    as = highlighting.get (stoken);
                    if (as != null) {
                        attributeSet.addAttributes (as);
                        endOffset1 = ts.offset () + t.length ();
                    }
                    TokenSequence ts1 = ts.embedded ();
                    if (ts1 == null) break;
                    ts = ts1;
                } while (true);
                if (endOffset1 > startOffset1)
                    return true;
                startOffset1 = ts.offset () + ts.token ().length ();
                endOffset1 = startOffset1;
            } while (startOffset1 < endOffset);
            return false;
        }

        public int getStartOffset () {
            return startOffset1;
        }

        public int getEndOffset () {
            return endOffset1;
        }

        public AttributeSet getAttributes () {
            return attributeSet;
        }
    }
}
