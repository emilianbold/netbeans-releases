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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
    private List<CppFoldRecord> blockCommentFolds = new ArrayList<CppFoldRecord>();
    private List<CppFoldRecord> lineCommentFolds = new ArrayList<CppFoldRecord>();
        
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
        }
        return next;
    }    

    @Override
    public String toString() {
        String retValue = orig.toString();
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

    public List<CppFoldRecord> getFolders() {
        List<CppFoldRecord> out = new ArrayList<CppFoldRecord>(blockCommentFolds.size() + lineCommentFolds.size() + 1);
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
