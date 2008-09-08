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
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.NbBundle;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.xml.text.folding.TokenElement.Token;
import org.netbeans.modules.xml.text.folding.TokenElement.TokenType;

/**
 * This class is an implementation of @see org.netbeans.spi.editor.fold.FoldManager
 * responsible for creating, deleting and updating code folds in XML documents.
 *
 * @author  Ayub Khan
 * @author  Samaresh Panda
 */
public class XmlFoldManager implements FoldManager {

    private FoldOperation operation;
    private long dirtyTimeMillis = 0;
    private Timer timer;
    private TimerTask timerTask;
   
    public static final int DELAY_SYNCER = 2000;  // milisecs.
    public static final int DELAY_DIRTY = 1000;  // milisecs.

    public void init(FoldOperation operation) {
        this.operation = operation;
    }

    /**
     * Ideally release should get called, but god knows why it doesn't.
     */
    public void release() {
        releaseTimerTask();
        timer = null;
    }

    protected FoldOperation getOperation() {
        return operation;
    }
      
    
    /**
     * Do NOT update folds here. For some reason, three fold managers get
     * instantiated by the infrastructure and three initFolds() get called.
     * 
     * Schedule fold updates in insertUpdate removeUpdate and changeUpdate.
     * First fold will be created by changeUpdate.
     * @param transaction
     */
    public void initFolds(FoldHierarchyTransaction transaction) {
    }
    
    private BaseDocument getDocument() {
        return (BaseDocument) getOperation().getHierarchy().getComponent().getDocument();
    }

    public void insertUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        scheduleFoldUpdate();
    }

    public void removeUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        scheduleFoldUpdate();
    }

    public void changedUpdate(DocumentEvent evt, FoldHierarchyTransaction transaction) {
        scheduleFoldUpdate();
    }

    public void removeEmptyNotify(Fold epmtyFold) {
    }

    public void removeDamagedNotify(Fold damagedFold) {
    }

    public void expandNotify(Fold expandedFold) {
    }
   
    private void scheduleFoldUpdate() {
        if (timer == null) {
            timer = new Timer();
        }
        dirtyTimeMillis = System.currentTimeMillis();
        
        //dump the old timerTask
        releaseTimerTask();
        
        timerTask = new TimerTask() {
            public void run() {
                if (dirtyIntervalMillis() > DELAY_DIRTY) {
                    updateFolds();
                    unsetDirty();
                }
            }
        };
        timer.schedule(timerTask, DELAY_SYNCER);
    }
    
    private void releaseTimerTask() {
        if(timerTask == null)
            return;
        
        timerTask.cancel();
        timerTask = null;
    }
   
    public void unsetDirty() {
        dirtyTimeMillis = 0;
        timer.cancel();
        timer = null;
    }
   
    private long dirtyIntervalMillis() {
        if (dirtyTimeMillis == 0) return 0;
        return System.currentTimeMillis() - dirtyTimeMillis;
    }

    /**
     * This method parses the XML document using Lexer and creates/recreates
     * the fold hierarchy.
     */
    private synchronized void updateFolds() {
        FoldHierarchy foldHierarchy = getOperation().getHierarchy();
        //lock the document for changes
        getDocument().readLock();
        try {
            //lock the hierarchy
            foldHierarchy.lock();
            try {
                //open new transaction
                FoldHierarchyTransaction fht = getOperation().openTransaction();
                try {
                    ArrayList<Fold> existingFolds = new ArrayList<Fold>();
                    collectExistingFolds(foldHierarchy.getRootFold(), existingFolds);
                    for(Fold f : existingFolds) {
                        getOperation().removeFromHierarchy(f, fht);
                    }
                    createFolds(fht);
                } catch (Exception ex) {
//                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
//                            NbBundle.getMessage(XmlFoldManager.class, "MSG_FOLDS_DISABLED"));
                } finally {
                    fht.commit();
                }
            } finally {
                //printFoldHierarchy(foldHierarchy.getRootFold(),"");
                foldHierarchy.unlock();
            }
        } finally {
            getDocument().readUnlock();
        }
    }
   
    /**
     * Prints the fold hierarchy.
     * @param fold
     * @param tab
     */
    private void printFoldHierarchy(Fold fold, String tab) {
        //System.out.println(tab + fold.getDescription());
        for(int i=0; i<fold.getFoldCount(); i++) {
            printFoldHierarchy(fold.getFold(i), tab+"==");
        }
    }
   
    /**
     * Collects all folds from the hierarchy that were created by this manager
     * and are not the root fold.
     *
     * @param fold
     * @param list 
     */
    private void collectExistingFolds(Fold fold, List<Fold> list) {
        for(int i=0; i<fold.getFoldCount(); i++) {
            collectExistingFolds(fold.getFold(i), list);
        }
        if(!FoldUtilities.isRootFold(fold) && getOperation().owns(fold)) {
            list.add(fold);
        }
    }
   
    /**
     * Creates a new fold and adds to the fold hierarchy.
     */
    private Fold createFold(FoldType type, String description, boolean collapsed,
            int startOffset, int endOffset, FoldHierarchyTransaction transaction)
                throws BadLocationException {
        Fold fold = null;
        if ( startOffset >= 0 &&
             endOffset >= 0 &&
             startOffset < endOffset &&
             endOffset <= getDocument().getLength() ) {
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
    private void createFolds(FoldHierarchyTransaction fhTran)
            throws BadLocationException, IOException {
        BaseDocument basedoc = getDocument();
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
                            currentNode = null;
                        }
                    } else {
                        tokenType = TokenType.TOKEN_ELEMENT_START_TAG;
                        if(image.startsWith("</")) {
                            String tagName = image.substring(2);
                            currentNode = tagName;
                        } else {
                            String tagName = image.substring(1);
                            stack.push(new TokenElement(tokenType, tagName,
                                    currentTokensSize, currentTokensSize+image.length(), -1));
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
                            stack.push(new TokenElement(tokenType, foldName,
                                    currentTokensSize, currentTokensSize+image.length(), -1));
                        } else if(image.endsWith(Token.COMMENT_END.getValue())) {
                            TokenElement tokenElem = stack.pop();
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            Fold f = createFold(XmlFoldTypes.COMMENT, tokenElem.getName(), false, so, eo, fhTran);
                            //myFolds.add(f);
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
                            stack.push(new TokenElement(tokenType, foldName,
                                    currentTokensSize, currentTokensSize+image.length(), -1));
                        } else if(image.endsWith(Token.CDATA_END.getValue())) {
                            TokenElement tokenElem = stack.pop();
                            int so = tokenElem.getStartOffset();
                            int eo = currentTokensSize+image.length();
                            Fold f = createFold(XmlFoldTypes.CDATA, tokenElem.getName(), false, so, eo, fhTran);
                            //myFolds.add(f);
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
                    break;
            }
            currentTokensSize += image.length();
        }
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
}
