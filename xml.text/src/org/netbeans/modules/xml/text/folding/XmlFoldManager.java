/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.xml.text.folding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.NbBundle;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.Utilities;
import org.openide.util.RequestProcessor;

/**
 * This class is an implementation of @see org.netbeans.spi.editor.fold.FoldManager
 * responsible for creating, deleting and updating code folds.
 *
 * @author  Ayub Khan
 * @author  Samaresh Panda
 */
public class XmlFoldManager implements FoldManager {

    private FoldOperation operation;
    private FoldHierarchyTransaction transaction;
    private List<Fold> myFolds;
    private long dirtyTimeMillis = 0;
    private RequestProcessor.Task SYNCHRONIZER = null;
    
    public static final int DELAY_SYNCER = 2000;  // milisecs.
    public static final int DELAY_DIRTY = 1000;  // milisecs.

    protected FoldOperation getOperation() {
        return operation;
    }

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    //fold hiearchy has been released
    public void release() {
    }

    public void initFolds(FoldHierarchyTransaction transaction) {
        Document doc = getOperation().getHierarchy().getComponent().getDocument();
        //filtering of the PlainDocument set during the JEditorPane initialization
        if (!(doc instanceof BaseDocument)) {
            return;
        }
        updateFolds(transaction);
        this.transaction = transaction;
            
        SYNCHRONIZER = RequestProcessor.getDefault().post(
                new Runnable() {
                    public void run() {
                        if (dirtyIntervalMillis() > DELAY_DIRTY) {
                            unsetDirty();
                            updateFolds(null);
                        }
                        SYNCHRONIZER.schedule(DELAY_SYNCER);
                    }
                }, DELAY_SYNCER);
    }
    
    public void setDirty() {
        dirtyTimeMillis = System.currentTimeMillis();
    }
    
    public void unsetDirty() {
        dirtyTimeMillis = 0;
    }
    
    private long dirtyIntervalMillis() {
        if (dirtyTimeMillis == 0) return 0;
        return System.currentTimeMillis() - dirtyTimeMillis;
    }

    /**
     * This method parses the XML document using Lexer and creates/recreates
     * the fold hierarchy.
     */
    private synchronized void updateFolds(FoldHierarchyTransaction transaction) {
        FoldHierarchy foldHierarchy = getOperation().getHierarchy();
        //lock the document for changes
        getDocument().readLock();
        try {
            //lock the hierarchy
            foldHierarchy.lock();
            try {
                //open new transaction
                FoldHierarchyTransaction fhTran = (transaction == null) ? getOperation().openTransaction() : transaction;
                try {
                    BaseDocument basedoc = getDocument();
                    myFolds = new ArrayList<Fold>();
                    parseDocument(basedoc, fhTran);
                } catch (BadLocationException ble) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
                            NbBundle.getMessage(XmlFoldManager.class, "MSG_FOLDS_DISABLED"));
                    removeAllFolds(fhTran);
                } catch (IOException iox) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, 
                            NbBundle.getMessage(XmlFoldManager.class, "MSG_FOLDS_DISABLED"));
                    removeAllFolds(fhTran);
                } finally {
                    if (transaction == null) {
                        fhTran.commit(); //the given transaction from initFolds() will be commited by the infr.
                    }
                    myFolds.clear();
                    myFolds = null;//allow garbage collection
                }
            } finally {
                foldHierarchy.unlock();
            }
        } finally {
            getDocument().readUnlock();
        }
    }
    
    private void removeAllFolds(FoldHierarchyTransaction transaction) {
        for (Fold f : myFolds) {
            getOperation().removeFromHierarchy(f, transaction);
        }
    }
    
    /**
     * Creates a new fold and adds to the fold hierarchy.
     */
    private Fold createFold(FoldType type, String description, boolean collapsed,
            int startOffset, int endOffset, FoldHierarchyTransaction transaction) 
                throws BadLocationException {
        Fold fold = null;
        if (startOffset >= 0 && endOffset >= 0 && startOffset < endOffset && endOffset <= getDocument().getLength()) {
            fold = getOperation().addToHierarchy(
                    type,
                    description.intern(), //save some memory
                    collapsed,
                    startOffset,
                    endOffset,
                    description.length(),
                    0,
                    null,
                    transaction);
        }
        return fold;
    }

    /**
     * This is the core of the fold creation algorithm.
     * This method parses the document using lexer and creates folds and adds
     * them to the fold hierarchy.
     */
    private void parseDocument(BaseDocument basedoc, FoldHierarchyTransaction fhTran) 
            throws BadLocationException, IOException {
        TokenHierarchy tokenHierarchy = TokenHierarchy.get(basedoc);
        TokenSequence<XMLTokenId> tokenSequence = tokenHierarchy.tokenSequence();
        org.netbeans.api.lexer.Token<XMLTokenId> token = tokenSequence.token();
        // Add the text token, if any, before xml decalration to document node
        if(token != null && token.id() == XMLTokenId.TEXT) {
            if(tokenSequence.moveNext()) {
                token = tokenSequence.token();
            }
        }
        int currentTokensSize = 0;
        Stack<TokenElement> stack = new Stack<TokenElement>();
        String currentNode = null;
        while (tokenSequence.moveNext()) {
            token = tokenSequence.token();
            XMLTokenId tokenId = token.id();
            String image = token.text().toString();
            TokenType tokenType = TokenType.TOKEN_WHITESPACE;
            switch(tokenId) {
                case TAG:
                {
                    int len = image.length();
                    if (image.charAt(len-1) == '>') {
                        TokenElement tokenElem = null;
                        if(len == 2) {
                            if(!stack.empty())
                                stack.pop();
                        } else {
                            if(!stack.empty()) { 
                                if(stack.peek().getName().equals(currentNode))
                                    tokenElem = stack.pop();
                            }
                        }
                        if(tokenElem != null) {
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            //do not create fold if start and end tags are
                            //in the same line
                            if(isOneLiner(so, eo))
                                break;
                            String foldName = "<" + currentNode + ">";
                            Fold f = createFold(XmlFoldTypes.TAG, foldName, false, so, eo, fhTran);
                            myFolds.add(f);
                        }
                    } else {
                        tokenType = TokenType.TOKEN_ELEMENT_START_TAG;
                        if(image.startsWith("</")) {
                            String tagName = image.substring(2);
                            currentNode = tagName;
                        } else {
                            String tagName = image.substring(1);
                            stack.push(new TokenElement(tokenType, tagName, currentTokensSize, currentTokensSize+image.length()));
                        }
                    }
                    break;
                }
                case BLOCK_COMMENT:
                {
                    tokenType = TokenType.TOKEN_COMMENT;
                    if (!(image.startsWith(Token.COMMENT_START.getValue()) && 
                            image.endsWith(Token.COMMENT_END.getValue()))) {
                        if (image.startsWith(Token.COMMENT_START.getValue())) {
                            String foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_COMMENT"); //NOI18N
                            stack.push(new TokenElement(tokenType, foldName, currentTokensSize, currentTokensSize+image.length()));
                        } else if(image.endsWith(Token.COMMENT_END.getValue())) {
                            TokenElement tokenElem = stack.pop();
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            Fold f = createFold(XmlFoldTypes.COMMENT, tokenElem.getName(), false, so, eo, fhTran);
                            myFolds.add(f);
                        }
                    }
                    break;
                }
                case CDATA_SECTION:
                {
                    tokenType = TokenType.TOKEN_CDATA_VAL;
                    if (!(image.startsWith(Token.CDATA_START.getValue()) && 
                            image.endsWith(Token.CDATA_END.getValue()))) {
                        if (image.startsWith(Token.CDATA_START.getValue())) {
                            String foldName = NbBundle.getMessage(XmlFoldManager.class, "LBL_CDATA"); //NOI18N
                            stack.push(new TokenElement(tokenType, foldName, currentTokensSize, currentTokensSize+image.length()));
                        } else if(image.endsWith(Token.CDATA_END.getValue())) {
                            TokenElement tokenElem = stack.pop();
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            Fold f = createFold(XmlFoldTypes.CDATA, tokenElem.getName(), false, so, eo, fhTran);
                            myFolds.add(f);
                        }
                    }
                    break;
                }
                    
                case PI_START:
                case PI_TARGET:
                case PI_CONTENT:
                case PI_END:
                case ARGUMENT: //attribute of an element
                case VALUE:
                case TEXT:                    
                case CHARACTER:
                case WS:
                case OPERATOR:
                case DECLARATION:
                    break; //Do nothing for above case's
                
                case ERROR:
                case EOL:
                default:
                    throw new IOException("Invalid token found in document: " +
                            "Please use the text editor to resolve the issues...");
            }
            currentTokensSize += image.length();
        }
    }

    private BaseDocument getDocument() {
        return (BaseDocument) getOperation().getHierarchy().getComponent().getDocument();
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        setDirty();
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        setDirty();
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        setDirty();
    }

    public void removeEmptyNotify(Fold epmtyFold) {
    }

    public void removeDamagedNotify(Fold damagedFold) {
    }

    public void expandNotify(Fold expandedFold) {
    }
    
    public boolean isOneLiner(int start, int end) {
        try {
            BaseDocument doc = getDocument();
            return Utilities.getLineOffset(doc, start) ==
                   Utilities.getLineOffset(doc, end);
        } catch (BadLocationException ex) {
            //Exceptions.printStackTrace(ex);
            return false;
        }
    }
       
    public class TokenElement {
        private TokenType type;
        private String name;
        private int startOffset;
        private int endOffset;
        
        public TokenElement(TokenType type, String name, int startOffset, int endOffset) {
            this.type = type;
            this.name = name;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
        }
        
        private TokenType getType() {
            return type;
        }
        
        private String getName() {
            return name;
        }
        
        private int getStartOffset() {
            return startOffset;
        }
        
        private int getEndOffset() {
            return endOffset;
        }
        
        public String toString() {
            return type + ", " + name + ", " + startOffset + ", " + endOffset;
        }
    }
    
    public enum Token {

        EQUALS_TOKEN("=", TokenType.TOKEN_ATTR_EQUAL), WHITESPACE_TOKEN(" ", TokenType.TOKEN_WHITESPACE),

        CLOSE_ELEMENT(">", TokenType.TOKEN_ELEMENT_END_TAG), //NOI18N

        SELF_CLOSE_ELEMENT("/>", TokenType.TOKEN_ELEMENT_END_TAG), //NOI18N

        CDATA_START("<![CDATA[", TokenType.TOKEN_CDATA_VAL), //NOI18N

        CDATA_END("]]>", TokenType.TOKEN_CDATA_VAL), //NOI18N

        COMMENT_START("<!--", TokenType.TOKEN_COMMENT), //NOI18N

        COMMENT_END("-->", TokenType.TOKEN_COMMENT); //NOI18N

        Token(String val, TokenType type) {
            value = val;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public TokenType getType() {
            return type;
        }

        @Override
        public String toString() {
            return getType() + " '" + value + "'";
        }

        private final String value;
        private final TokenType type;
    }
    
    public enum TokenType {

        TOKEN_ELEMENT_NAME,
        TOKEN_ELEMENT_START_TAG,
        TOKEN_ELEMENT_END_TAG,
        TOKEN_ATTR_NAME,
        TOKEN_ATTR_NS,
        TOKEN_ATTR_VAL,
        TOKEN_ATTR_QUOTATION,
        TOKEN_ATTR_EQUAL,
        TOKEN_CHARACTER_DATA,
        TOKEN_WHITESPACE,
        TOKEN_COMMENT,
        TOKEN_COMMENT_TAG,
        TOKEN_PI_START_TAG,
        TOKEN_PI_NAME,
        TOKEN_PI_VAL,
        TOKEN_PI_END_TAG,
        TOKEN_DEC_ATTR_NAME,
        TOKEN_DEC_ATTR_VAL,
        TOKEN_CDATA_VAL,
        TOKEN_DTD_VAL,
        TOKEN_DOC_VAL,
        TOKEN_NS,
        TOKEN_NS_SEPARATOR,
    }
}
