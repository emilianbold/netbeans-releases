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
package org.netbeans.spi.editor.bracesmatching.support;

import org.netbeans.modules.editor.bracesmatching.*;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 *
 * @author Vita Stejskal
 */
/* package */ final class CharacterMatcher implements BracesMatcher {

    private static final Logger LOG = Logger.getLogger(CharacterMatcher.class.getName());
    
    private final MatcherContext context;
    private final char [] matchingPairs;
    private final int lowerBound;
    private final int upperBound;
    
    private int originOffset;
    private char originalChar;
    private char matchingChar;
    private boolean backward;
    
    public CharacterMatcher(MatcherContext context, int lowerBound, int upperBound, char... matchingPairs) {
        this.context = context;
        this.lowerBound = lowerBound == -1 ? Integer.MIN_VALUE : lowerBound;
        this.upperBound = upperBound == -1 ? Integer.MAX_VALUE : upperBound;
        
        assert matchingPairs.length % 2 == 0 : "The matchingPairs parameter must contain even number of characters."; //NOI18N
        this.matchingPairs = matchingPairs;
    }
    
    // -----------------------------------------------------
    // BracesMatcher implementation
    // -----------------------------------------------------
    
    public int [] findOrigin() throws BadLocationException {
        int result [] = BracesMatcherSupport.findChar(
            context.getDocument(), 
            context.getSearchOffset(),
            context.isSearchingBackward() ? 
                Math.max(context.getLimitOffset(), lowerBound) :
                Math.min(context.getLimitOffset(), upperBound),
            matchingPairs
        );
        
        if (result != null) {
            originOffset = result[0];
            originalChar = matchingPairs[result[1]];
            matchingChar = matchingPairs[result[1] + result[2]];
            backward = result[2] < 0;
            return new int [] { originOffset, originOffset + 1 };
        } else {
            return null;
        }
    }

    public int [] findMatches() throws BadLocationException {
        int offset = BracesMatcherSupport.matchChar(
            context.getDocument(),
            backward ? originOffset : originOffset + 1,
            backward ? 
                Math.max(0, lowerBound) :
                Math.min(context.getDocument().getLength(), upperBound),
            originalChar,
            matchingChar
        );
        
        return offset != -1 ? new int [] { offset, offset + 1 } : null;
    }
}
