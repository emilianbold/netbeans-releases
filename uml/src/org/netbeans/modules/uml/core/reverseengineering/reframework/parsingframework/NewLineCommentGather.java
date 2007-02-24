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

/*
 * File       : NewLineCommentGather.java
 * Created on : Oct 27, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import antlr.CommonASTWithHiddenTokens;
import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;

/**
 * @author Aztec
 */
public class NewLineCommentGather extends CommentGather implements INewLineCommentGather
{
    private int m_NewLineType;

    public NewLineCommentGather()
    {
    }

    public NewLineCommentGather(int slComment, int mlComment, int newline)
    {
        super(slComment, mlComment);
        m_NewLineType = newline;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.INewLineCommentGather#gather(antlr.collections.AST, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor)
     */
    public ITokenDescriptor gather(AST pAST, ITokenDescriptor pDesc)
    {
        if(pAST == null) return pDesc;

        if(pDesc == null) pDesc = new TokenDescriptor();

        CommonASTWithHiddenTokens pToken = null;

        try
        {
            pToken = (CommonASTWithHiddenTokens)pAST;
        }
        catch (ClassCastException ignored) {}

        if(pToken == null) return null;

        CommonHiddenStreamToken pHiddenToken = pToken.getHiddenBefore();

        if(pHiddenToken != null && pHiddenToken.getType() == getNewLineType())
        {
            return gatherFromNode(pHiddenToken, pDesc);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.INewLineCommentGather#getNewLineType()
     */
    public int getNewLineType()
    {
        return m_NewLineType;
    }

    private ITokenDescriptor gatherFromNode(CommonHiddenStreamToken pHiddenToken ,
                                            ITokenDescriptor pDesc)
    {
        int startLine   = -1;
        int startColumn = -1;
        int startPos    = -1;
        int length      = -1;

        boolean processingComment = false;
        String comment = "";
        int lastType = -1;

        while(pHiddenToken != null)
        {
            int type = pHiddenToken.getType();

            if(type == getSingleLineType() || type == getMultiLineType())
            {
                // The problem is two fold.
                // One: since the new line token is not included
                // in the comment we must manually add the line characters to the return string.
                // Two: Since there can be NEWLINE tokens that are not associated with comments
                // then we do not want to manually add the new line characters to the return string.
                if(lastType == getNewLineType())
                {
                    comment = "\r\n" + comment;
                }
                comment = pHiddenToken.getText() + comment;

                startLine = pHiddenToken.getLine();
                startColumn = pHiddenToken.getColumn();
                // TODO: Aztec - Need to modify source if we want this
                //startPos = pHiddenToken.getPosition();

                String value = pHiddenToken.getText();
                length += value.length();

                processingComment = true;
            }

            if( pHiddenToken != null && type != getMultiLineType())
            {
                pHiddenToken = pHiddenToken.getHiddenBefore();
            }
            else if(type == getMultiLineType())
            {
                    // If we have found a multiline comment there is no reason to continue.
                    // basically we only want to continue if the comment is a single line
                    // comment.
                    pHiddenToken = null;
            }
            else if(pHiddenToken != null && pHiddenToken.getType() != getNewLineType())
            {
                // When we are handling comments that do not process the new line comment (because
                // the newline character is used as the line terminator) a newline token type must
                // be present to continue.  REASON: The comment will always be a hidden comment of a
                // new line comment.
                pHiddenToken = null;

            }
            lastType = type;
        }
        // If found a comment I want to add it to the token descriptors property.  Since the
        // comment is optional for a token I am making it a property.  The idea is that only
        // required elements are fields on token descriptors.  Also a comment is not really a
        // entity of the specific token.  I is really property of a state (class, operation, or attribute).
        // However it is only able to be retrieved by a token.
        if(comment != null && comment.trim().length() > 0)
        {
            // If we fail I do not want to worry about it.
            pDesc.addProperty("Comment", comment);
            pDesc.addProperty("CommentStartLine", String.valueOf(startLine));
            pDesc.addProperty("CommentStartColumn", String.valueOf(startColumn));
            pDesc.addProperty("CommentStartPos", String.valueOf(startPos));
            pDesc.addProperty("CommentLength", String.valueOf(length + 1));
        }

        return pDesc;
    }

}
