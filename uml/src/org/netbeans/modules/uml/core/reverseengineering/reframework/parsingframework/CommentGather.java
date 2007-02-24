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
 * File       : CommentGather.java
 * Created on : Oct 23, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import antlr.CommonHiddenStreamToken;
import antlr.collections.AST;
import antlr.*;

/**
 * @author Aztec
 */
public class CommentGather implements ICommentGather
{
    protected int m_SLCOMMENT;
    protected int m_MLCOMMENT;

    public CommentGather()
    {
    }

    public CommentGather(int slComment, int mlComment)
    {
        m_SLCOMMENT = slComment;
        m_MLCOMMENT = mlComment;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICommentGather#gather(antlr.collections.AST, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor)
     */
    public ITokenDescriptor gather(AST pAST, ITokenDescriptor pDesc)
    {
        if(pAST == null) return pDesc;

        if(pDesc == null) pDesc = new TokenDescriptor();

        CommonASTWithLocationsAndHidden pToken = null;

        try
        {
            pToken = (CommonASTWithLocationsAndHidden)pAST;
        }
        catch (ClassCastException e) {
            e.printStackTrace();
        }

        if(pToken == null) return null;

        CommonHiddenStreamToken pHiddenToken = (CommonHiddenStreamToken) pToken.getHiddenBefore();

        int startLine   = -1;
        int startColumn = -1;
        long startPos    = -1;
        int length      = -1;

        boolean processingComment = false;
        String comment = "";

        while(pHiddenToken != null)
        {
            int type = pHiddenToken.getType();

            if(type == getSingleLineType() || type == getMultiLineType())
            {
                comment = pHiddenToken.getText() + comment;
                startLine = pHiddenToken.getLine();
                startColumn = pHiddenToken.getColumn();
                startPos = pHiddenToken.getPosition();

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
        }
        //If found a comment I want to add it to the token descriptors property.  Since the
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

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICommentGather#getMultiLineType()
     */
    public int getMultiLineType()
    {
        // TODO Auto-generated method stub
        return m_MLCOMMENT;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ICommentGather#getSingleLineType()
     */
    public int getSingleLineType()
    {
        // TODO Auto-generated method stub
        return m_SLCOMMENT;
    }

}
