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

package org.netbeans.modules.xml.xdm.nodes;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;

/**
 *
 * @author Ajit
 */
public class Comment extends Text implements org.w3c.dom.Comment {
    
    public void accept(XMLNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    Comment() {
        super();
    }

    Comment(String text) {
        this();
        stripCommentMarkers(text);
    }

    private void stripCommentMarkers(String data) {
	// remove start and end markers
	String normalizedData = ""; //NOI18N
	assert data.startsWith(Token.COMMENT_START.getValue()):data;
	if (data.length() > Token.COMMENT_START.getValue().length() +
	    Token.COMMENT_END.getValue().length()) {
	    normalizedData = 
		data.substring(Token.COMMENT_START.getValue().length(),
		data.length() - Token.COMMENT_END.getValue().length());
	}
	setData(normalizedData);
    }
    
    private void addCommentTokens() {
	List<Token> tokens = getTokensForWrite();
	tokens.add(0,Token.COMMENT_START);
	tokens.add(Token.COMMENT_END);
	setTokens(tokens);
    }

    @Override
    public String getNodeValue() {
        return getData();
    }

    @Override
    public void setData(String data) {
	super.setData(data);
	addCommentTokens();
    }
    
    @Override
    public short getNodeType() {
        return Node.COMMENT_NODE;
    }

    @Override
    public String getNodeName() {
        return "#comment"; //NOI18N
    }

}
