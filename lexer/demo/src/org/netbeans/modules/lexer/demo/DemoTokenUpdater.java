/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.lexer.demo;

import javax.swing.text.Segment;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.LexerInput;
import org.netbeans.api.lexer.TokenTextMatcher;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Lexer;
import org.netbeans.spi.lexer.util.Compatibility;
import org.netbeans.spi.lexer.inc.TextTokenUpdater;

/**
 * Token updater working over a swing document.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class DemoTokenUpdater extends TextTokenUpdater {
    
    private Segment seg = new Segment(); // shared segment instance

    protected final Document doc;
    
    protected final Language language;

    protected final boolean maintainLookbacks;
    
    public DemoTokenUpdater(Document doc, Language language) {
        this(doc, language, true);
    }

    public DemoTokenUpdater(Document doc, Language language, boolean maintainLookbacks) {
        this.doc = doc;
        this.language = language;
        this.maintainLookbacks = maintainLookbacks;
    }

    public char textCharAt(int index) {
        synchronized (seg) {
            try {
                doc.getText(index, 1, seg);
                return seg.array[seg.offset];

            } catch (BadLocationException e) {
                throw new IllegalStateException(e.toString());
            }
        }
    }

    public int textLength() {
        return doc.getLength();
    }
    
    private String getDocumentText(int offset, int length) {
        try {
            return doc.getText(offset, length);
        } catch (BadLocationException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }
    
    protected Token createToken(TokenId id, int index, int length) {
        String fixedText = null;
        if (Compatibility.charSequenceExists()) {
            TokenTextMatcher matcher = id.getTokenTextMatcher();
            if (matcher != null) {
                /* The recognizedText would not be a string
                 * in the normal applications. It would be
                 * a custom char sequence reused for every
                 * recognized token. Here it's string
                 * to simplify the code.
                 */
                CharSequence recognizedText
                    = (CharSequence)(Object)getDocumentText(index, length); // 1.3 compilability 
                fixedText = matcher.match(recognizedText);
            }
        }

        Token token;
        if (fixedText != null) {
            token = new DemoFixedToken(id, fixedText);
        } else {
            token = new DemoToken(this, id, index, length);
        }

        return token;
    }
    
    protected Lexer createLexer() {
        return language.createLexer();
    }
    
    protected void add(Token token, int lookahead, Object state) {
        add(token);

        if (token instanceof DemoFixedToken) {
            DemoFixedToken dft = (DemoFixedToken)token;
            dft.setLookahead(lookahead);
            dft.setState(state);
        } else {
            DemoToken dt = (DemoToken)token;
            dt.setLookahead(lookahead);
            dt.setState(state);
        }
    }
    
    public int getLookahead() {
        Token token = getToken(getValidPreviousIndex());
        return (token instanceof DemoFixedToken)
            ? ((DemoFixedToken)token).getLookahead()
            : ((DemoToken)token).getLookahead();
    }

    public Object getState() {
        Token token = getToken(getValidPreviousIndex());
        return (token instanceof DemoFixedToken)
            ? ((DemoFixedToken)token).getState()
            : ((DemoToken)token).getState();
    }

    public int getLookback() {
        if (maintainLookbacks) {
            Token token = getToken(getValidPreviousIndex());
            return (token instanceof DemoFixedToken)
                ? ((DemoFixedToken)token).getLookback()
                : ((DemoToken)token).getLookback();
                
        } else { // do not maintain the lookbacks
            return -1;
        }
    }

    protected void setLookback(int lookback) {
        if (maintainLookbacks) {
            Token token = getToken(getValidPreviousIndex());
            if (token instanceof DemoFixedToken) {
                ((DemoFixedToken)token).setLookback(lookback);
            } else {
                ((DemoToken)token).setLookback(lookback);
            }
        }
    }
    
    public boolean hasNext() {
        return super.hasNext();
    }
    
    public Token next() {
        return super.next();
    }
    
    public int relocate(int index) {
        return super.relocate(index);
    }

    public String tokensToString() {
        StringBuffer sb = new StringBuffer();
        int cnt = getTokenCount();
        sb.append("Token count: " + cnt + "\n");
        int offset = 0;
        try {
            for (int i = 0; i < cnt; i++) {
                Token t = getToken(i);
                int length = org.netbeans.spi.lexer.util.Compatibility.getLength(t);
                String text = org.netbeans.spi.lexer.util.Compatibility.toString(t);
                sb.append("[" + i + "] \""
                    + org.netbeans.spi.lexer.util.LexerUtilities.toSource(text)
                    + "\", " + t.getId()
                    + ", off=" + offset
                );
                
                if (t instanceof DemoToken) {
                    DemoToken dt = (DemoToken)t;

                    if (getOffset(dt.getRawOffset()) != offset) {
                        throw new IllegalStateException("offsets differ");
                    }
                    
                    sb.append(", type=regular"
                        + ", la=" + dt.getLookahead()
                        + ", lb=" + dt.getLookback()
                        + ", st=" + dt.getState()
                        + "\n"
                    );
                    
                } else {
                    DemoFixedToken dft = (DemoFixedToken)t;
                    sb.append(", type=fixed"
                        + ", la=" + dft.getLookahead()
                        + ", lb=" + dft.getLookback()
                        + ", st=" + dft.getState()
                        + "\n"
                    );
                }
                

                offset += length;
            }
        } catch (RuntimeException e) {
            System.err.println(sb.toString());
            throw e;
        }
        
        return sb.toString();
    }

}
