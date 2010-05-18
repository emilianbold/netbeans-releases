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

package org.netbeans.modules.xml.text.dom;

import java.util.WeakHashMap;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.openide.util.WeakListeners;

/**
 * Creates higher level syntax elements (DOM nodes) above token chain.
 *
 * @author  Samaresh Panda
 */
public class XMLSyntaxSupport {
    
    /** Holds last character user have typed. */
    private char lastInsertedChar = 'X';  // NOI18N
    private final DocumentMonitor documentMonitor;
    private BaseDocument document;
    private static WeakHashMap<BaseDocument, XMLSyntaxSupport> supportMap =
            new WeakHashMap<BaseDocument, XMLSyntaxSupport>();
    
    /** Creates new XMLSyntaxSupport */
    private XMLSyntaxSupport(BaseDocument doc) {
        this.document = doc;
        documentMonitor = new DocumentMonitor();
        DocumentListener l = WeakListeners.document(documentMonitor, doc);
        doc.addDocumentListener(l);
    }

    public static XMLSyntaxSupport getSyntaxSupport(BaseDocument doc) {
        XMLSyntaxSupport support = supportMap.get(doc);
        if(support != null)
            return support;

        support = new XMLSyntaxSupport(doc);
        supportMap.put(doc, support);
        return support;
    }

    public BaseDocument getDocument() {
        return document;
    }
    
    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document.
     * @param offset valid position in document
     * @return TokenItem or <code>null</code> at the document beginning.
     */
    public Token getPreviousToken( int offset) throws BadLocationException {
        if (offset == 0) return null;
        if (offset < 0) throw new BadLocationException("Offset " +
                offset + " cannot be less than 0.", offset);  //NOI18N
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(((AbstractDocument)document));
            TokenSequence ts = th.tokenSequence();
            return getToken(ts, offset, false);
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }

    /**
     * Get token at given offet or previous one if at token boundary.
     * It does not lock the document.
     * @param offset valid position in document
     * @return TokenItem or <code>null</code> at the document beginning.
     */
    public Token getNextToken( int offset) throws BadLocationException {
        if (offset == 0) return null;
        if (offset < 0) throw new BadLocationException("Offset " +
                offset + " cannot be less than 0.", offset);  //NOI18N
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(((AbstractDocument)document));
            TokenSequence ts = th.tokenSequence();
            return getToken(ts, offset, true);
        } finally {
            ((AbstractDocument)document).readUnlock();
        }
    }
    
    private Token getToken(TokenSequence ts, int offset, boolean next) {
        ts.move(offset);
        Token token = ts.token();
        //there are cases when this could be null
        //in which case use the next one.
        if(token == null)
            ts.moveNext();

        if(next) {
            ts.moveNext();
        } else {
            ts.movePrevious();
        }
        token = ts.token();
        return token;
    }

    
    /**
     * Returns SyntaxElement instance for block of tokens, which is either
     * surrounding given offset, or is just before the offset.
     * @param offset Offset in document where to search for SyntaxElement.
     * @return SyntaxElement Element surrounding or laying BEFORE the offset
     * or <code>null</code> at document begining.
     */
    public SyntaxElement getElementChain(final int offset ) throws BadLocationException {

        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(((AbstractDocument)document));
            TokenSequence<XMLTokenId> ts = th.tokenSequence();
            Token<XMLTokenId> token = initialize(ts, offset);
            if(token == null)
                return null;
            switch(token.id()) {
                case PI_START:
                case PI_END:
                case PI_CONTENT:
                case PI_TARGET: {
                    while(token.id() != XMLTokenId.PI_START) {
                        ts.movePrevious();
                        token = ts.token();
                    }
                    return createElement(ts, token);
                }

                case TEXT:
                case DECLARATION:
                case CDATA_SECTION:
                case BLOCK_COMMENT:
                case TAG:
                case ERROR: {
                    return createElement(ts, token);
                }
            }

        } finally {
            ((AbstractDocument)document).readUnlock();
        }

        return null;
    }

    private Token<XMLTokenId> initialize(TokenSequence ts, int offset) {
        ts.move(offset);
        Token<XMLTokenId> token = ts.token();
        if(token == null) {
            if(!ts.moveNext())
                return null;
            token = ts.token();
        }
        XMLTokenId id = token.id();
        String image = token.text().toString();
        if ( id == XMLTokenId.WS ||
                id == XMLTokenId.ARGUMENT ||
                id == XMLTokenId.OPERATOR ||
                id == XMLTokenId.VALUE ||
                (id == XMLTokenId.TAG &&
                (">".equals(image) || "/>".equals(image)))) { //NOI18N
            while (true) {
                ts.movePrevious();
                token = ts.token();
                id = token.id();
                if (id == XMLTokenId.TAG || id == XMLTokenId.PI_START)
                    break;
            } //while
        } //if
        return token;
    }

    /**
     * Create elements starting with given item.
     *
     * @param  item or null if EOD
     * @return SyntaxElement startting at offset, or null, if EoD
     */
    private SyntaxElement createElement(final TokenSequence ts,
            final Token<XMLTokenId> token) throws BadLocationException {
        //default start and end.
        int start = ts.offset();
        int end = start + token.length();
        switch(token.id()) {
            
            case PI_START: {
                String target = null;
                String content = null;
                Token<XMLTokenId> t = token;
                while(t.id() != XMLTokenId.PI_END) {
                    if(t.id() == XMLTokenId.PI_TARGET)
                        target = t.text().toString();
                    if(t.id() == XMLTokenId.PI_CONTENT)
                        content = t.text().toString();
                    ts.moveNext();
                    t = ts.token();
                }
                end = ts.offset() + t.length();
                return new ProcessingInstruction(this, token, start,
                        end, target, content);
            }

            case DECLARATION: {
                return new DocumentType(this, token, start, end);
            }

            case CDATA_SECTION: {
                return new CDATASection(this, token, start, end);
            }
            
            case BLOCK_COMMENT: {
                return new Comment(this, token, start, end);
            }
            
            case TEXT: {
                return new Text(this, token, start, end);
            }

            case TAG: {
                Token<XMLTokenId> t = token;
                do {
                    ts.moveNext();
                    t = ts.token();
                } while(t.id() != XMLTokenId.TAG);
                end = ts.offset() + t.length();
                //empty tag
                if(t.text().toString().equals("/>")) {
                    return new EmptyTag(this, token, start, end);
                }
                //end tag
                if(token.text().toString().startsWith("</")) {//NOI18N
                    return new EndTag(this, token, start, end);
                }
                return new StartTag(this, token, start, end);
            }

            case ERROR: {
                return new SyntaxElement.Error(this, token, start, end );
            }
        }

        return null;
    }

    /**
     * No completion inside PI, CDATA, comment section.
     * True only inside PI or CDATA section, false otherwise.
     * @param target
     */
    public static boolean noCompletion(JTextComponent target) {
        if(target == null || target.getCaret() == null)
            return false;
        int offset = target.getCaret().getDot();
        if(offset < 0)
            return false;
        //no completion inside CDATA or comment section
        BaseDocument document = (BaseDocument)target.getDocument();
        ((AbstractDocument)document).readLock();
        try {
            TokenHierarchy th = TokenHierarchy.get(document);
            TokenSequence ts = th.tokenSequence();
            if(ts == null)
                return false;
            ts.move(offset);
            Token token = ts.token();
            if(token == null) {
                ts.moveNext();
                token = ts.token();
                if(token == null)
                    return false;
            }
            if( token.id() == XMLTokenId.CDATA_SECTION ||
               token.id() == XMLTokenId.BLOCK_COMMENT ||
               token.id() == XMLTokenId.PI_START ||
               token.id() == XMLTokenId.PI_END ||
               token.id() == XMLTokenId.PI_CONTENT ||
               token.id() == XMLTokenId.PI_TARGET ) {
               return true;
            }
        } finally {
            ((AbstractDocument)document).readUnlock();
        }

        return false;
    }

    /** Returns last inserted character. It's most likely one recently typed by user. */
    public final char lastTypedChar() {
        return lastInsertedChar;
    }
        
    /** Keep track of last typed character */
    private class DocumentMonitor implements DocumentListener {
        
        public void changedUpdate(DocumentEvent e) {
        }
        
        public void insertUpdate(DocumentEvent e) {
            int start = e.getOffset();
            int len = e.getLength();
            try {
                String s = e.getDocument().getText(start + len - 1, 1);
                lastInsertedChar = s.charAt(0);
            } catch (BadLocationException e1) {
            }
        }
        
        public void removeUpdate(DocumentEvent e) {
        }
    }
}

