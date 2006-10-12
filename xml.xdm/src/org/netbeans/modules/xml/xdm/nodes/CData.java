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
import java.util.List;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;
import org.w3c.dom.CDATASection;

/**
 *
 * @author Ajit
 */
public class CData extends Text implements CDATASection {
    
    public void accept(XMLNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    CData() {
        super();
    }

    CData(String text) {
        this();
        stripCDataMarkers(text);
    }

    private void stripCDataMarkers(String data) {
	// remove start and end CDATA
	String normalizedData = ""; //NOI18N
	assert data.startsWith(Token.CDATA_START.getValue());
	if (data.length() > Token.CDATA_START.getValue().length() +
	    Token.CDATA_END.getValue().length()) {
	    normalizedData = 
		data.substring(Token.CDATA_START.getValue().length(),
		data.length() - Token.CDATA_END.getValue().length());
	}
	setData(normalizedData);
    }
    
    private void addCDataTokens() {
	List<Token> tokens = getTokensForWrite();
	tokens.add(0,Token.CDATA_START);
	tokens.add(Token.CDATA_END);
	setTokens(tokens);
    }
    
    @Override
    public String getNodeValue() {
        return getData();
    }
    
    @Override
    public void setData(String data) {
	super.setData(data);
	addCDataTokens();
    }
    
    @Override
    public short getNodeType() {
        return Node.CDATA_SECTION_NODE;
    }

    @Override
    public String getNodeName() {
        return "#cdata-section"; //NOI18N
    }

}
