/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.folding;

import antlr.Token;
import antlr.TokenStream;
import antlr.TokenStreamException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;

/**
 * filter to remove comments from token stream and generate code folds for:
 * - initial block comment (copyright)
 * - any block comment
 * - sequental line comments
 *
 * @author Vladimir Voskresensky
 */
/*package*/ class APTFoldingCommentFilter implements TokenStream {
    private static final int COMMENTS_FOLD = CppFoldRecord.COMMENTS_FOLD;
    private static final int INITIAL_COMMENT_FOLD = CppFoldRecord.INITIAL_COMMENT_FOLD;
    private static final int BLOCK_COMMENT_FOLD = CppFoldRecord.BLOCK_COMMENT_FOLD;
    
    // states
    private static final int BEFORE_FIRST_TOKEN_STATE = 0;
    private static final int INIT_STATE = 1;
    private static final int AFTER_LINE_COMMENT = 3;

    private final TokenStream orig;
    private int state = BEFORE_FIRST_TOKEN_STATE;
    
    private int lastTokenLine = -1;
    
    // folds
    private CppFoldRecord initialCommentFold = null;
    private List/*<CppFoldRecord>*/ blockCommentFolds = new ArrayList();
    private List/*<CppFoldRecord>*/ lineCommentFolds = new ArrayList();
        
    /** Creates a new instance of APTCommentsFilter */
    public APTFoldingCommentFilter(TokenStream orig) {
        this.orig = orig;
    }

    public Token nextToken() throws TokenStreamException {
        APTToken next = null;
        boolean needNext = true;
        while (needNext) {
            next = (APTToken) orig.nextToken();
            switch(next.getType()) {
                case APTTokenTypes.COMMENT:
                    createBlockCommentsFold(next);
                    break;
                case APTTokenTypes.CPP_COMMENT:
                    if (lastTokenLine != next.getLine()) {
                        if (firstLineComment == null) {
                            firstLineComment = next;
                            state = AFTER_LINE_COMMENT;
                        }
                        lastLineComment = next;
                    }
                    break;
                case APTTokenTypes.EOF:
                    needNext = false;
                    createLineCommentsFoldIfNeeded();
                    break;
                default:
                    needNext = false;
                    lastTokenLine = next.getEndLine();
                    createLineCommentsFoldIfNeeded();
            }    
        };
        return next;
    }    

    public String toString() {
        String retValue;
        
        retValue = orig.toString();
        return retValue;
    }   

    public void onPreprocNode(APT apt) {
        switch (state) {
            case BEFORE_FIRST_TOKEN_STATE:
                // first block comment wasn't found, switch to init state
                state = INIT_STATE;
                break;
            case AFTER_LINE_COMMENT:
                // met #-directive, flush line comments
                createLineCommentsFoldIfNeeded();
                break;
        }
    }

    public List/*<CppFoldRecord>*/ getFolders() {
        List out = new ArrayList(blockCommentFolds.size() + lineCommentFolds.size() + 1);
        if (initialCommentFold != null) {
            out.add(initialCommentFold);
        }
        out.addAll(blockCommentFolds);
        out.addAll(lineCommentFolds);
        return out;
    }

    private void createLineCommentsFoldIfNeeded() {
        if (state == AFTER_LINE_COMMENT) {        
            if (firstLineComment != lastLineComment) {
                assert (firstLineComment != null);
                assert (lastLineComment != null);
                lineCommentFolds.add(createFoldRecord(COMMENTS_FOLD, firstLineComment, lastLineComment));
            }
            firstLineComment = null;
            lastLineComment = null;        
            state = INIT_STATE;
        }
    }
    
    private void createBlockCommentsFold(APTToken token) {
        createLineCommentsFoldIfNeeded();
        if (state == BEFORE_FIRST_TOKEN_STATE) {
            // this is the copyright 
            assert (initialCommentFold == null) : "how there could be two copyrights?";
            initialCommentFold = createFoldRecord(INITIAL_COMMENT_FOLD, token, token);
        } else {
            blockCommentFolds.add(createFoldRecord(BLOCK_COMMENT_FOLD, token, token));
        }        
        state = INIT_STATE;
    }
    
    private APTToken firstLineComment = null;
    private APTToken lastLineComment = null;
    
    private CppFoldRecord createFoldRecord(int folderKind, APTToken begin, APTToken end) {
        if (APTFoldingUtils.isStandalone()) {
            return new CppFoldRecord(folderKind, begin.getLine(), begin.getColumn(), end.getEndLine(), end.getEndColumn());
        } else {
            return new CppFoldRecord(folderKind, begin.getOffset(), end.getEndOffset());            
        }        
    }
    
}
