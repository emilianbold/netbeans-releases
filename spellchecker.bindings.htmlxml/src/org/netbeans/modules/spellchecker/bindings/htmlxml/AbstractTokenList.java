/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007-2008 Sun Microsystems, Inc.
 * Portions Copyrighted 2007-2008 Stefan Riha, Roland Poppenreiter
 */
package org.netbeans.modules.spellchecker.bindings.htmlxml;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.spellchecker.spi.language.TokenList;
import org.openide.ErrorManager;

/**
 * Tokenize Html and Xml text for spell checking. Based on corresponding
 * JavaTokenList by Jan Lahoda.
 *
 * @author Riha, Poppenreiter
 */
public abstract class AbstractTokenList implements TokenList {

    protected BaseDocument doc;
    private CharSequence currentWord;
    private int currentStartOffset;
    private int nextSearchOffset;
    private int ignoreBefore;

    /** Creates a new instance of HtmlXmlTokenList */
    AbstractTokenList(BaseDocument doc) {
        this.doc = doc;
    }

    public void setStartOffset(int offset) {
        currentWord = null;
        currentStartOffset = (-1);
        this.ignoreBefore = offset;
        try {
            this.nextSearchOffset = Utilities.getRowStart(doc, offset);
        } catch (BadLocationException ex) {
            Logger.getLogger(AbstractTokenList.class.getName()).log(Level.FINE, null, ex);
            this.nextSearchOffset = offset;
        }
    }

    public int getCurrentWordStartOffset() {
        return currentStartOffset;
    }

    public CharSequence getCurrentWordText() {
        return currentWord;
    }

    private int[] findNextSpellSpan() throws BadLocationException {
        TokenHierarchy<Document> h = TokenHierarchy.get((Document) doc);
        TokenSequence<?> ts = h.tokenSequence();
        return findNextSpellSpan(ts, nextSearchOffset);
    }

    /** Given a sequence of Html/Xml tokens, return the next span of eligible comments */
    protected abstract int[] findNextSpellSpan(TokenSequence<? extends TokenId> ts, int offset) throws BadLocationException;

    public boolean nextWord() {
        boolean next = nextWordImpl();
        
        while (next && (currentStartOffset + currentWord.length()) < ignoreBefore)
            next = nextWordImpl();
        
        return next;
    }
    
    private boolean nextWordImpl() {
        try {
            int[] span = findNextSpellSpan();

            while (span[0] != -1) {
                int offset = (span[0] < nextSearchOffset) ? nextSearchOffset : span[0];

                int length = span[1];
                boolean searching = true;

					 /* find next word */
                while (offset < length) {
                    String t = doc.getText(offset, 1);
                    char c = t.charAt(0);

                    if (searching) {
                        if (Character.isLetter(c)) {
									 /* word beginn found */
                            searching = false;
                            currentStartOffset = offset;
                        }
                    } else {
                        if (!Character.isLetter(c)) {
                            /* word end found */
                            nextSearchOffset = offset;
                            currentWord = doc.getText(currentStartOffset, offset - currentStartOffset);
                            return true;
                        }
                    }

                    offset++;
                }

                nextSearchOffset = offset;

                if (!searching) {
                    currentWord = doc.getText(currentStartOffset, offset - currentStartOffset);
                    return true;
                }

                span = findNextSpellSpan();
            }

            return false;
        } catch (BadLocationException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }

    public void addChangeListener(ChangeListener l) {
    //ignored...
    }

    public void removeChangeListener(ChangeListener l) {
    //ignored...
    }
}
