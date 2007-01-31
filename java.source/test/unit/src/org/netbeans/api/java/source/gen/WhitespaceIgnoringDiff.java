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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source.gen;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.EnumSet;
import java.util.Set;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.junit.diff.Diff;
import org.netbeans.junit.diff.LineDiff;

/**
 *
 * @author Jan Lahoda
 */
public class WhitespaceIgnoringDiff implements Diff {
    
    /** Creates a new instance of WhitespaceIgnoringDiff */
    public WhitespaceIgnoringDiff() {
    }

    private Set<JavaTokenId> IGNORED_TOKENS = EnumSet.of(JavaTokenId.WHITESPACE, JavaTokenId.LINE_COMMENT, JavaTokenId.BLOCK_COMMENT, JavaTokenId.BLOCK_COMMENT_INCOMPLETE, JavaTokenId.JAVADOC_COMMENT, JavaTokenId.JAVADOC_COMMENT_INCOMPLETE);
    
    public boolean diff(File first, File second, File diff) throws IOException {
        boolean result = diffImpl(first, second);
        
        if (result) {
            new LineDiff().diff(first, second, diff);
        }
        
        return result;
    }
    
    private boolean diffImpl(File first, File second) throws IOException {
        Reader firstReader = new FileReader(first);
        Reader secondReader = new FileReader(second);
        try {
            TokenHierarchy firstH = TokenHierarchy.create(firstReader, JavaTokenId.language(), /*IGNORED_TOKENS*/null, null);
            TokenHierarchy secondH = TokenHierarchy.create(secondReader, JavaTokenId.language(), /*IGNORED_TOKENS*/null, null);
            TokenSequence<JavaTokenId> firstTS = firstH.tokenSequence(JavaTokenId.language());
            TokenSequence<JavaTokenId> secondTS = secondH.tokenSequence(JavaTokenId.language());
            
//            if (firstTS.tokenCount() != secondTS.tokenCount()) {
//                return true;
//            }
            
            firstTS.moveNext();
            secondTS.moveNext();
            
            boolean firstHasNext = true;
            boolean secondHasNext = true;
            
            do {
                Token<JavaTokenId> firstToken = firstTS.token();
                Token<JavaTokenId> secondToken = secondTS.token();
                
                while (IGNORED_TOKENS.contains(firstToken.id()) && firstHasNext) {
                    firstHasNext = firstTS.moveNext();
                    firstToken = firstTS.token();
                }
                
                while (IGNORED_TOKENS.contains(secondToken.id()) && secondHasNext) {
                    secondHasNext = secondTS.moveNext();
                    secondToken = secondTS.token();
                }
                
                if (!firstHasNext || !secondHasNext)
                    break;
                
                if (firstToken.id() != secondToken.id() || !TokenUtilities.equals(firstToken.text(), secondToken.text()))
                    return true;
                
                firstHasNext = firstTS.moveNext();
                secondHasNext = secondTS.moveNext();
            } while (firstHasNext && secondHasNext);
            
            if (firstHasNext || secondHasNext)
                return true;
        } finally {
            firstReader.close();
            secondReader.close();
        }
        
        return false;
    }

    public boolean diff(String first, String second, String diff) throws IOException {
        File fFirst = new File(first);
        File fSecond = new File(second);
        File fDiff = null != diff ? new File(diff) : null;
        return diff(fFirst, fSecond, fDiff);
    }
    
}
