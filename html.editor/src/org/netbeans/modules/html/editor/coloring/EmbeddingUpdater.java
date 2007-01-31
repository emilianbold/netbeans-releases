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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.html.editor.coloring;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.ext.html.parser.SyntaxElement;
import org.netbeans.editor.ext.html.parser.SyntaxParserListener;

/**
 * Listens on HTML parser changes and updates embedding of nested languages accordingly.
 *
 * @author Marek.Fukala@Sun.com
 */
public class EmbeddingUpdater implements SyntaxParserListener {
    
    private static final String JAVASCRIPT_MIMETYPE = "text/javascript";//NOI18N
    
    private static final String HTML_SCRIPT_TAG_NAME = "script"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(EmbeddingUpdater.class.getName());
    
    private final Document doc;

    private int scriptStart;
    
    public EmbeddingUpdater(Document doc) {
        this.doc = doc;
    }
    
    public void parsingFinished(List<SyntaxElement> elements) {
        scriptStart = -1;
        for(SyntaxElement sel : elements) {
            if(sel.getType() == SyntaxElement.TYPE_TAG) {
                startTag((SyntaxElement.Tag)sel);
            } else if(sel.getType() == SyntaxElement.TYPE_ENDTAG) {
                endTag((SyntaxElement.Named)sel);
            }
        }
    }
    
    private void startTag(SyntaxElement.Named sel) {
        if(HTML_SCRIPT_TAG_NAME.equals(sel.getName())) {
            scriptStart = sel.getElementOffset() + sel.getElementLength();
        }
    }
    
    private void endTag(SyntaxElement.Named sel) {
        if(HTML_SCRIPT_TAG_NAME.equals(sel.getName())) {
            if(scriptStart != -1) {
                createJavaScriptEmbedding(scriptStart, sel.getElementOffset() - 1);
            }
        }
    }
    
    private void createJavaScriptEmbedding(final int startOffset, final int endOffset) {
        ((BaseDocument)doc).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(doc);
            TokenSequence ts = tokenSequence(th, scriptStart);
            
            int diff = ts.move(startOffset);
            if(diff == Integer.MAX_VALUE) return; //no token found
            
            ts.moveNext();
            Language lang = Language.find(JAVASCRIPT_MIMETYPE);
            if(lang == null) {
                LOGGER.log(Level.WARNING, "No " + JAVASCRIPT_MIMETYPE + " language found!");
                return ; //no javascript language found
            }
            
            do {
                Token item = ts.token();
                if(!ts.createEmbedding(lang, 0, 0)) {
                    LOGGER.log(Level.WARNING, "Cannot create embedding for " + JAVASCRIPT_MIMETYPE + ".");
                } else {
                    LOGGER.log(Level.INFO, "Embedding for " + JAVASCRIPT_MIMETYPE + " created [" + startOffset + " - "  + endOffset + "].");
                }
                
                //                        System.out.println("embedding of the script content token:");
                //                        TokenSequence ts2 = ts.embedded();
                //                        if(ts2 != null) {
                //                            System.out.println(ts2.toString().substring(1));
                //                        } else {
                //                            System.out.println("NO embedding in script content token");
                //                        }
                
            } while(ts.moveNext() && ts.offset() <= endOffset);
        }finally {
            ((BaseDocument)doc).readUnlock();
        }
    }
    
    private static TokenSequence tokenSequence(TokenHierarchy hi, int offset) {
        TokenSequence ts = hi.tokenSequence(HTMLTokenId.language());
        if(ts == null) {
            //HTML language is not top level one
            ts = hi.tokenSequence();
            int diff = ts.move(offset);
            if(diff == Integer.MAX_VALUE) {
                return null; //no token found
            } else {
                ts.moveNext();
                ts = ts.embedded(HTMLTokenId.language());
            }
        }
        return ts;
    }
    
    
    
}
