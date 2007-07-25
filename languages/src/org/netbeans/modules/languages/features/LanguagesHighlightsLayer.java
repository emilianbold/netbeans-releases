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

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.spi.editor.highlighting.HighlightsContainer;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.HighlightsSequence;
import org.netbeans.spi.editor.highlighting.support.AbstractHighlightsContainer;


/**
 *
 * @author Jan Jancura
 */
class LanguagesHighlightsLayer extends AbstractHighlightsContainer {

    private Document document;
    
    LanguagesHighlightsLayer (Document document) {
        this.document = document;
    }

    public HighlightsSequence getHighlights (int startOffset, int endOffset) {
        return new Highlights (document, startOffset, endOffset);
    }

    
    private static class Highlights implements HighlightsSequence {

        private Document            document;
        private int                 endOffset;
        private int                 startOffset1;
        private int                 endOffset1;
        private SimpleAttributeSet  attributeSet;
        private TokenSequence       tokenSequence;
        private String              mimeType;
        
        
        private Highlights (Document document, int startOffset, int endOffset) {
            this.document = document;
            this.endOffset = endOffset;
            startOffset1 = startOffset;
            endOffset1 = startOffset;
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (document);
            tokenSequence = tokenHierarchy.tokenSequence ();
            mimeType = (String) document.getProperty ("mimeType");
        }
        
        public boolean moveNext () {
            attributeSet = new SimpleAttributeSet ();
            do {
                startOffset1 = endOffset1;
                mark (tokenSequence);
                if (endOffset1 > startOffset1) return true;
                tokenSequence.move (startOffset1);
                if (!tokenSequence.moveNext ()) return false;
                Token token = tokenSequence.token ();
                endOffset1 = tokenSequence.offset () + token.length ();
            } while (endOffset1 < endOffset);
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

        private void mark (TokenSequence ts) {
            ts.move (startOffset1);
            if (!ts.moveNext ()) return;
            Token token = ts.token ();
            TokenSequence ts2 = ts.embedded ();
            if (ts2 == null) return;
            String mimeTypeOut = ts.language ().mimeType ();
            String mimeTypeIn = ts2.language ().mimeType ();
            if (token.id ().name ().equals ("PE")) {
                Color c = getPreprocessorImportsColor (mimeTypeIn);
                if (c != null) {
                    attributeSet.addAttribute (StyleConstants.Background, c);
                    attributeSet.addAttribute (HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                    endOffset1 = tokenSequence.offset () + token.length ();
                }
            } else
            if (!mimeTypeOut.equals (mimeTypeIn)) {
                Color c = getTokenImportsColor (mimeTypeOut, mimeTypeIn, token.id ().name ());
                if (c != null) {
                    attributeSet.addAttribute (StyleConstants.Background, c);
                    attributeSet.addAttribute (HighlightsContainer.ATTR_EXTENDS_EOL, Boolean.TRUE);
                    endOffset1 = tokenSequence.offset () + token.length ();
                }
            }
            mark (ts2);
        }

        private Map<String,Map<String,Color>> tokenImportColors = new HashMap<String,Map<String,Color>> ();

        private Color getPreprocessorImportsColor (String mimeTypeIn) {
            if (preprocessorImportColors == null) {
                preprocessorImportColors = new HashMap<String,Color> ();
                try {
                    Language l = LanguagesManager.getDefault ().
                        getLanguage (mimeType);
                    Feature properties = l.getPreprocessorImport ();
                    if (properties != null) {
                        String mimeType = (String) properties.getValue ("mimeType");
                        Color color = ColorsManager.readColor (
                            (String) properties.getValue ("background_color")
                        );
                        if (color != null)
                            preprocessorImportColors.put (mimeType, color);
                    }
                } catch (ParseException ex) {
                }
            }
            return preprocessorImportColors.get (mimeTypeIn);
        }

        private Map<String,Color> preprocessorImportColors;

        private Color getTokenImportsColor (String mimeTypeOut, String mimeTypeIn, String tokenTypeIn) {
            Map<String,Color> m = tokenImportColors.get (mimeTypeOut);
            if (m == null) {
                m = new HashMap<String,Color> ();
                tokenImportColors.put (mimeTypeOut, m);
                try {
                    Language l = LanguagesManager.getDefault ().
                        getLanguage (mimeTypeOut);
                    Map<String,Feature> m2 = l.getTokenImports ();
                    Iterator<String> it = m2.keySet ().iterator ();
                    while (it.hasNext ()) {
                        String tokenType = it.next ();
                        Feature properties = m2.get (tokenType);
                        Color color = ColorsManager.readColor (
                            (String) properties.getValue ("background_color")
                        );
                        if (color != null)
                            m.put (tokenType, color);
                    }
                } catch (LanguageDefinitionNotFoundException ex) {
                }
            }
            if (m.containsKey (tokenTypeIn))
                return m.get (tokenTypeIn);
            return m.get (mimeTypeIn);
        }
    }
}
