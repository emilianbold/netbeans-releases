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

/*
 * File       : CommentGather.java
 * Created on : Oct 23, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;

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

	boolean markerFound = false;
	boolean processAsUsually = false;
        boolean processCached = false;
	int state = 0;
	CommonHiddenStreamToken cached1 = null;
	CommonHiddenStreamToken cached2 = null;
	Hashtable<String, MarkerKeyTokenDescriptor> parsedValues = null;

        while(pHiddenToken != null)
        {
            int type = pHiddenToken.getType();

            if(type == getSingleLineType() || type == getMultiLineType())
            {
		processAsUsually = markerFound;		
		// parsing id marker possibly enclosed into folding comment tags
		if (! markerFound ) {
		    processCached = false;
		    if (type == getSingleLineType()) {
			if (state == 0 || state == 1) {
			    parsedValues = new Hashtable<String, MarkerKeyTokenDescriptor>();
			}
			if (state == 0) {
			    cached1 = null;
			    cached2 = null;			    
			}
			if (state == 0) {
			    if (parseEditorFoldComment(pHiddenToken.getText(), false)) {
				if (parseMarkerComment(pHiddenToken.getText(), 
                                                       parsedValues,
                                                       pHiddenToken.getPosition())) 
                                {
				    cached2 = pHiddenToken;
				    state = 2;
				} else {
				    cached1 = pHiddenToken;
				    state = 1;
				}			    
			    } else if (parseMarkerComment(pHiddenToken.getText(), 
                                                          parsedValues,
                                                          pHiddenToken.getPosition())) 
                            {
				storeMarkerComment(new CommonHiddenStreamToken[] {pHiddenToken}, 
						   parsedValues, pDesc);
				markerFound = true;
			    } else {
				processAsUsually = true;
			    }
			} else if (state == 1) {
			    if (parseMarkerComment(pHiddenToken.getText(), 
                                                   parsedValues,
                                                   pHiddenToken.getPosition())) 
                            {
				cached2 = pHiddenToken;
				state = 2;
			    } else {
				processCached = true;
				processAsUsually = true;
				state = 0;
			    }
			} else if (state == 2) {
			    if (parseEditorFoldComment(pHiddenToken.getText(), true)) {
				storeMarkerComment
				    (new CommonHiddenStreamToken[]{cached1, cached2, pHiddenToken}, 
				     parsedValues, pDesc);
			    } else {
				storeMarkerComment
				    (new CommonHiddenStreamToken[]{cached2}, 
				     parsedValues, pDesc);
				processCached = true;
				processAsUsually = true;			    
			    } 			
			    markerFound = true;
			}
		    } else {
			if (cached2 != null) {
			    storeMarkerComment
				(new CommonHiddenStreamToken[]{cached2}, 
				 parsedValues, pDesc);
			    markerFound = true;
			}
			processCached = true;
			processAsUsually = true;			    			
		    }
		}   
		if (cached1 != null && processCached ) {
		    comment = cleanseComment(cached1.getText(), cached1.getType()) + comment;
		    startLine = cached1.getLine();
		    startColumn = cached1.getColumn();
		    startPos = cached1.getPosition();
		    
		    String value = cached1.getText();
		    length += value.length();
		    
		    processingComment = true;
		}

		if (processAsUsually) {
		    comment = cleanseComment(pHiddenToken.getText(), pHiddenToken.getType()) + comment;
		    startLine = pHiddenToken.getLine();
		    startColumn = pHiddenToken.getColumn();
		    startPos = pHiddenToken.getPosition();
		    
		    String value = pHiddenToken.getText();
		    length += value.length();
		    
		    processingComment = true;
		}
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

    protected String cleanseComment(String comment, int type)
    {
        if(comment == null || comment.trim().length() == 0) 
            return comment;

        
        if (type == getSingleLineType()) 
        {
            return comment.replaceAll("^\\s*//\\s?", "");
        } 
        else 
        {
            return comment.replaceAll("/\\*\\s*|\\*/|\\*\\s?", "");            
        }

    }

    /**
     *  parses (in a very ad-hoc style) the line of the following format
     *  #[regen=yes,id=C2FEEEAC-CFCD-11D1-8B05-00600806D9B6]
     */
    public static boolean parseMarkerComment(String comment, 
					     Hashtable<String, MarkerKeyTokenDescriptor> result,
                                             long linePos) 
    {
	if (comment == null || result == null) {
	    return false;
	}
        long initPos = linePos;
        int ws = 0;
        while(Character.isWhitespace(comment.charAt(ws))) 
        {
            ws++;
        }
        initPos += ws;
 	String ln = comment.trim();
	int cs = ln.indexOf("//");
	if (cs == 0 && ln.length() > 5) {
	    ln = ln.substring(cs + 2).trim();
            initPos += cs + 2;
	}
	if (ln.length() < 3) {
	    return false;
	}
	if ( ! ( ln.charAt(0) == '#' && ln.charAt(1) == '[' ) ) {
	    return false;
	}  
	int start = 2;
	int end = ln.indexOf("]", start);
	if (end < 0) {
	    return false;
	}
	String values = ln.substring(start, end);
        initPos += start;
        int pnt = 0;
        int len = end - start;
        while(pnt < len) {
            int commaAt = values.indexOf(",", pnt);
	    String pair = null;
            if (commaAt < 0) {                
                commaAt = values.length();
            }
            pair = values.substring(pnt, commaAt);            
	    if (pair != null) {
		pair = pair.trim();
		int ind = pair.indexOf("=");
		if (ind > 0) {
		    String key = pair.substring(0, ind).trim();
		    String value = pair.substring(ind + 1, pair.length()).trim();
                    MarkerKeyTokenDescriptor desc = new MarkerKeyTokenDescriptor();
                    desc.value = value;
                    desc.length = commaAt - pnt;
                    desc.startPos = initPos + pnt;
		    result.put(key, desc);
		}
	    }
            pnt = commaAt + 1;
	}
	return true;
    }


    /**
     *  parses (in a very ad-hoc style - don't want to call full-fledge 
     *  XML parsing, thus user isn't supposed to alterate these lines too much) 
     *  the <editor-fold> start and end line
     */
    public static boolean parseEditorFoldComment(String comment, 
						 boolean open) 
    {
	if (comment == null) {
	    return false;
	}
	String ln = comment.trim();
	int cs = ln.indexOf("//");
	if (cs == 0) {
	    ln = ln.substring(cs + 2).trim();
	} else {
	    return false;
	}

	if (open) {
	    if (ln.startsWith("<editor-fold")) {
		return true;
	    } 
	} else {
	    if (ln.indexOf("</editor-fold") > -1) {
		return true;
	    } 	    
	}
	return false;
    }


    private void storeMarkerComment(CommonHiddenStreamToken[] pHiddenTokens, 
				    Hashtable<String, MarkerKeyTokenDescriptor> parsedValues, 
				    ITokenDescriptor pDesc) 
    {
	Set<String> keys = parsedValues.keySet();
	for(String key: keys) {
            MarkerKeyTokenDescriptor desc = parsedValues.get(key);
            pDesc.addProperty("Marker-"+key.toLowerCase(), desc.value);       
            pDesc.addProperty("Marker-"+key.toLowerCase()+"StartPos", new Long(desc.startPos).toString());       
            pDesc.addProperty("Marker-"+key.toLowerCase()+"Length", new Integer(desc.length).toString());       
	}

        String commentMarker = "";
        int startLineMarker   = -1;
        int startColumnMarker = -1;
        long startPosMarker    = -1;
        int lengthMarker      = -1;

	for (int i = 0; i < pHiddenTokens.length; i++) {
	    commentMarker = cleanseComment(pHiddenTokens[i].getText(), pHiddenTokens[i].getType()) + commentMarker;
	    startLineMarker   = pHiddenTokens[i].getLine();
	    startColumnMarker = pHiddenTokens[i].getColumn();
	    startPosMarker    = pHiddenTokens[i].getPosition();
	    lengthMarker      += pHiddenTokens[i].getText().length();
	}

	pDesc.addProperty("Marker-Comment", commentMarker);
	pDesc.addProperty("Marker-CommentStartLine", String.valueOf(startLineMarker));
	pDesc.addProperty("Marker-CommentStartColumn", String.valueOf(startColumnMarker));
	pDesc.addProperty("Marker-CommentStartPos", String.valueOf(startPosMarker));
	pDesc.addProperty("Marker-CommentLength", String.valueOf(lengthMarker + 1));
    }

    static class MarkerKeyTokenDescriptor {
        String value;
        long startPos = -1;
        int length = -1;
    }

}
