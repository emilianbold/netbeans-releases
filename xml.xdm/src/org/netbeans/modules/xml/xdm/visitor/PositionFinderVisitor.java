/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * PositionFinderVisitor.java
 *
 * Created on October 26, 2005, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.nodes.TokenType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
/**
 *
 * @author rico
 */
public class PositionFinderVisitor implements XMLNodeVisitor{
    
    int position = 0;
    Node rootNode;
    Node node;
    boolean found;
    
    public int findPosition(Node rootNode, Node node){
        reset();
        this.rootNode = rootNode;
        this.node = node;
        rootNode.accept(this);

        return position;
    }
    
    public void reset(){
        position = 0;
        found = false;
    }
    
    public void visit(Document doc) {
        //xml processing instruction
        position += getLengthOfTokens(doc);        
        NodeList nodes = doc.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++){
            Node n = (Node)nodes.item(i);
            n.accept(this);            
            if(found) return;
        }
    }
    
    public void visit(Element e) {
        if(e.getId() == node.getId()){
            found = true;
        } else{
            position += getTokenLength(e, TokenType.TOKEN_WHITESPACE); //all whitespaces
            position += getElementStartTokenLength(e, true); //open start tag
            NamedNodeMap attrs = e.getAttributes();
            for(int i = 0; i < attrs.getLength(); i++){
                Node attr = (Node)attrs.item(i);
                attr.accept(this);
                if(found) return;
            }
            position++; //close of start tag
            NodeList children = e.getChildNodes();
            for(int i = 0; i < children.getLength(); i++){
                Node n = (Node)children.item(i);
                n.accept(this);
                if(found) return;
            }
            position += getElementStartTokenLength(e, false); //open end tag
            position++; //close of end tag
        }
    }
    
    public void visit(Text txt) {
        if(txt.getId() == node.getId()){
            found = true;
        } else{
			int txtLen = 1; //length 1 for line break
			if(txt.getText().length()>0)
				txtLen = txt.getText().length();
			position += txtLen;
        }
    }
    
    public void visit(Attribute attr) {
        if(attr.getId() == node.getId()){
            //add preceding white spaces
            position += getLeadingWhiteSpaces(attr);
            found = true;
        } else{
            position += getLengthOfTokens(attr); 
        }
    }
    
    
    /**
     * Obtains the length of a start element, e.g., "<", or "<elementname",
     * "</", or "</elementname".
     * @param node The element being queried
     * @param beginTag Is this for the start tag (<) or end tag (</)?
     * @return length of start element
     */
    private int getElementStartTokenLength(Element element, boolean beginTag){
        String value = "";
        List<Token> tokens = element.getTokens();
        for(Token token : tokens){
            if(token.getType() != TokenType.TOKEN_ELEMENT_START_TAG){
                continue;
            }
            String tokenValue = token.getValue();
            if(beginTag){
                if(!tokenValue.startsWith("</")){
                    value = tokenValue;
                }
            } else{ //end tag
                if(tokenValue.startsWith("</")){
                    value = tokenValue;
                }
            }
        }
        return value.length();
    }
    
    private int getTokenLength(NodeImpl node, TokenType type){
        StringBuffer buf = new StringBuffer("");
        List<Token> tokens = node.getTokens();
        for(Token token : tokens){
            if(token.getType() == type){
                buf.append(token.getValue());
            }
        }
        return buf.toString().length();
    }
    
    private int getLeadingWhiteSpaces(Attribute attr){
        Token firstToken = attr.getTokens().get(0); //get the first token
        if(firstToken.getType() == TokenType.TOKEN_WHITESPACE){
            return firstToken.getValue().length();
        }
        return 0;
    }
    
    private int getLengthOfTokens(NodeImpl node){
        StringBuffer buf = new StringBuffer();
        List<Token> tokens = node.getTokens();
        for(Token token : tokens){
            buf.append(token.getValue());
        }
        return buf.length();
    }
    
}
