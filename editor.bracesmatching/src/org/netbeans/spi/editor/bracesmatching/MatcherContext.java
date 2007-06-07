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
package org.netbeans.spi.editor.bracesmatching;

import javax.swing.text.Document;
import org.netbeans.modules.editor.bracesmatching.SpiAccessor;

/**
 * An immutable context for a {@link BracesMatcher}. This context is created by the
 * editor infrastructure when it needs to create a matcher by using a registered
 * {@link BracesMatcherFactory}. The context provides a matcher with 
 * information essential to perform the search.
 * 
 * @author Vita Stejskal
 */
public final class MatcherContext {

    static {
        SpiAccessor.register(new SpiAccessorImpl());
    }
    
    private final Document document;
    private final int offset;
    private final boolean backward;
    private final int lookahead;
    
    private MatcherContext(Document document, int offset, boolean backward, int lookahead) {
        this.document = document;
        this.offset = offset;
        this.backward = backward;
        this.lookahead = lookahead;
    }

    /**
     * Gets a document that should be searched for matching areas.
     * 
     * @return The document to search in.
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * Gets an offset in a document where searching should start. It's usually
     * a position of a caret.
     * 
     * @return The caret's position.
     */
    public int getSearchOffset() {
        return offset;
    }

    /**
     * Gets the direction to search in for an original area. The search always
     * begins at the caret offset.
     * 
     * @return <code>true</code> to search backward towards the beginning of 
     *   a document or <code>false</code> to search forward towards the end of 
     *   a document.
     */
    public boolean isSearchingBackward() {
        return backward;
    }
    
    /**
     * Gets an offset in a document towards which the search should go. This
     * is basically <code>searchOffset ± searchLookahead</code> depending on
     * the search direction.
     * 
     * @return The offset limiting the search, <code>searchOffset ± searchLookahead</code>.
     */
    public int getLimitOffset() {
        return backward ? offset - lookahead : offset + lookahead;
    }
    
    /**
     * Gets the number of characters to search through when looking for an original
     * area. When searching for an original area matchers should not look
     * further from the caret offset then the number of characters returned from
     * this method.
     * 
     * @return A small positive number to limit the search for an original
     *   area.
     */
    public int getSearchLookahead() {
        return lookahead;
    }

    private static final class SpiAccessorImpl extends SpiAccessor {
        
        public MatcherContext createCaretContext(Document document, int offset, boolean backward, int lookahead) {
            return new MatcherContext(document, offset, backward, lookahead);
        }
        
    } // End of SpiAccessorImpl class
}
