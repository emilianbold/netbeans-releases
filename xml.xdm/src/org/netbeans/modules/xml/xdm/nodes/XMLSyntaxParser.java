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

import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;

public class XMLSyntaxParser {
    
    public Document parse(BaseDocument basedoc)
    throws IOException, BadLocationException {

        // get syntax and get token chain
        ExtSyntaxSupport sup = (ExtSyntaxSupport)basedoc.getSyntaxSupport();
        TokenItem token = sup.getTokenChain(0, basedoc.getLength());
        
        // create the core model
        Stack<NodeImpl> stack = new Stack<NodeImpl>();
        Document doc = new Document();
        stack.push(doc);
        NodeImpl currentNode = doc;
        List<Token> currentTokens = new ArrayList<Token>();
        // Add the text token, if any, before xml decalration to document node
        if(isValid(token) && token.getTokenID().getNumericID() == XMLTokenIDs.TEXT_ID) {
            currentTokens.add(Token.create(token.getImage(),TokenType.TOKEN_CHARACTER_DATA));
            token = token.getNext();
            // if the xml decalration is not there assign this token to document
            if(isValid(token) && token.getTokenID().getNumericID() != XMLTokenIDs.PI_START_ID) {
                currentNode.setTokens(new ArrayList<Token>(currentTokens));
                currentTokens.clear();
            }
        }
        
        while (token != null) {
            isValid(token);
            TokenID tokenId = token.getTokenID();
            int numericId = tokenId.getNumericID();
            String image = token.getImage();
            TokenType coreTokenId = TokenType.TOKEN_WHITESPACE;
            switch(numericId) {
                case XMLTokenIDs.PI_START_ID:
                {
                    coreTokenId = TokenType.TOKEN_PI_START_TAG;
                    currentTokens.add(Token.create(image,coreTokenId));
                    break;
                }
                case XMLTokenIDs.PI_END_ID:
                {
                    coreTokenId = TokenType.TOKEN_PI_END_TAG;
                    currentTokens.add(Token.create(image,coreTokenId));
                    if(currentNode instanceof Document) {
                        if(token.getNext().getTokenID().getNumericID() == XMLTokenIDs.TEXT_ID) {
                            token = token.getNext();
                            currentTokens.add(Token.create(token.getImage(),TokenType.TOKEN_CHARACTER_DATA));
                        }
                        stack.push(currentNode);
                    }
                    List<Token> list = new ArrayList<Token>(currentNode.getTokens());
                    list.addAll(currentTokens);
                    currentNode.setTokens(list);
                    currentTokens.clear();
                    break;
                }
                case XMLTokenIDs.TAG_ID:
                {
                    int len = image.length();
                    if (image.charAt(len-1) == '>') {
                        Token endToken =
                                Token.create(image,TokenType.TOKEN_ELEMENT_END_TAG);
                        if(len == 2) {
                            currentNode = stack.pop();
                            endToken =
                                    Token.create(image,TokenType.TOKEN_ELEMENT_END_TAG);
                        } else if(!(currentNode instanceof Element)) {
                            currentNode = stack.peek();
                        }
                        currentTokens.add(endToken);
                        currentNode.getTokensForWrite().addAll(currentTokens);
                        currentTokens.clear();
                    } else {
                        coreTokenId = TokenType.TOKEN_ELEMENT_START_TAG;
                        if(image.startsWith("</")) {
                            currentNode = stack.pop();
                            if(!currentNode.getTokens().get(0).getValue().substring(1).
                                    equals(image.substring(2))) {
                                throw new IOException("Invalid token '" + image +
                                        "' found in document: " +
                                        "Please use the text editor to resolve the issues...");
                            } else {//check for invalid endtag: <a></a
                                String saveTokenImage = image;
                                currentTokens.add(Token.create(image,coreTokenId));                                
                                token = token.getNext();
                                while(token != null) {
                                    int nextNumericId = token.getTokenID().getNumericID();
                                    if(nextNumericId != XMLTokenIDs.WS_ID)
                                        break;
                                    coreTokenId = TokenType.TOKEN_WHITESPACE;
                                    currentTokens.add(Token.create(token.getImage(), coreTokenId));                                    
                                    token = token.getNext();
                                }   
                                if(token == null || !token.getImage().equals(">"))
                                    throw new IOException("Invalid token '" + saveTokenImage +
                                            "' does not end with '>': Please use the " +
                                            "text editor to resolve the issues...");
                                continue;
                            }
                        } else {
                            currentNode = new Element();
                            Node parent = stack.peek();
                            parent.appendChild(currentNode);
                            stack.push(currentNode);
                            currentTokens.add(Token.create(image,coreTokenId));
                            currentNode.setTokens(new ArrayList<Token>(currentTokens));
                            currentTokens.clear();
                        }
                    }
                    break;
                }
                case XMLTokenIDs.ARGUMENT_ID:
                {
                    coreTokenId = TokenType.TOKEN_ATTR_NAME;
                    currentNode = new Attribute();
                    Element parent = (Element)stack.peek();
                    parent.appendAttribute((Attribute)currentNode);
                    currentTokens.add(Token.create(image,coreTokenId));
                    break;
                }
                case XMLTokenIDs.VALUE_ID:
                {
                    TokenItem nextToken = token.getNext();
                    isValid(nextToken);
                    int nextNumericId = nextToken.getTokenID().getNumericID();
                    while(nextNumericId == XMLTokenIDs.VALUE_ID || nextNumericId == XMLTokenIDs.CHARACTER_ID) {
                        token = token.getNext();
                        image = image.concat(token.getImage());
                        nextNumericId = token.getNext().getTokenID().getNumericID();
                    }
                    coreTokenId = TokenType.TOKEN_ATTR_VAL;
                    currentTokens.add(Token.create(image,coreTokenId));
                    currentNode.setTokens(new ArrayList<Token>(currentTokens));
                    currentTokens.clear();
                    break;
                }
                case XMLTokenIDs.BLOCK_COMMENT_ID:
                {
                    Node parent = stack.peek();
                    currentTokens.add(Token.create(image, coreTokenId));
                    if (image.endsWith(Token.COMMENT_END.getValue())) {
                        String combinedString = combineString(currentTokens);
                        Comment comment = new Comment(combinedString);
                        if (parent instanceof Element) {
                            ((Element)parent).appendChild(comment, true);
                        } else {//parent is Document
                            if(numericId != XMLTokenIDs.BLOCK_COMMENT_ID &&
                                    token.getImage().trim().length() > 0) {
                                throw new IOException("Invalid token '" + token.getImage() +
                                        "' found in document: " +
                                        "Please use the text editor to resolve the issues...");
                            }
                            parent.appendChild(comment);
                        }
                        currentTokens.clear();
                    }
                    break;
                }
                case XMLTokenIDs.TEXT_ID:
                case XMLTokenIDs.CHARACTER_ID:
                {
                    coreTokenId = TokenType.TOKEN_CHARACTER_DATA;
                    currentNode = new Text();
                    currentTokens.add(Token.create(image,coreTokenId));
                    if(numericId == XMLTokenIDs.TEXT_ID) {
                        while(token.getNext() != null) {
                            int nextNumericId = token.getNext().getTokenID().getNumericID();
                            if(nextNumericId != XMLTokenIDs.TEXT_ID && nextNumericId != XMLTokenIDs.CHARACTER_ID)
                                break;
                            token = token.getNext();
                            currentTokens.add(Token.create(token.getImage(),coreTokenId));
                        }
                    }
                    currentNode.setTokens(new ArrayList<Token>(currentTokens));
                    Node parent = stack.peek();
                    if (parent instanceof Element) {
                        ((Element)parent).appendChild(currentNode, true);
                    } else {//parent is Document
                        if(numericId != XMLTokenIDs.BLOCK_COMMENT_ID &&
                                token.getImage().trim().length() > 0) {
                            throw new IOException("Invalid token '" + token.getImage() +
                                    "' found in document: " +
                                    "Please use the text editor to resolve the issues...");
                        }
                        parent.appendChild(currentNode);
                    }
                    currentTokens.clear();
                    break;
                }
                case XMLTokenIDs.WS_ID:
                {
                    coreTokenId = TokenType.TOKEN_WHITESPACE;
                    currentTokens.add(Token.create(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.OPERATOR_ID:
                {
                    coreTokenId = TokenType.TOKEN_ATTR_EQUAL;
                    currentTokens.add(Token.create(image,coreTokenId));
                    break;
                }
                case XMLTokenIDs.DECLARATION_ID:
                {
                    coreTokenId = TokenType.TOKEN_DTD_VAL;
                    currentTokens.add(Token.create(image, coreTokenId));
                    while(token.getNext() != null) {
                        int nextNumericId = token.getNext().getTokenID().getNumericID();
                        if(nextNumericId != XMLTokenIDs.DECLARATION_ID && nextNumericId != XMLTokenIDs.VALUE_ID)
                            break;
                        token = token.getNext();
                        currentTokens.add(Token.create(token.getImage(),coreTokenId));
                    }
                    break;
                }
                case XMLTokenIDs.PI_CONTENT_ID:
                {
                    coreTokenId = TokenType.TOKEN_PI_VAL;
                    currentTokens.add(Token.create(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.PI_TARGET_ID:
                {
                    coreTokenId = TokenType.TOKEN_PI_NAME;
                    currentTokens.add(Token.create(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.CDATA_SECTION_ID:
                {
                    Node parent = stack.peek();
                    CData cdata = new CData(image);
                    if (parent instanceof Element) {
                        ((Element)parent).appendChild(cdata, true);
                    } else {//parent is Document
                        throw new IOException("CDATA is not valid as direct child of document" +
                                "Please use the text editor to resolve the issues...");
                    }
                    coreTokenId = TokenType.TOKEN_CDATA_VAL;
                    break;
                }
                case XMLTokenIDs.ERROR_ID:
                case XMLTokenIDs.EOL_ID:
                default:
                    //throw new IllegalArgumentException();
                    throw new IOException("Invalid token '" + token.getImage() + "' found in document: " +
                            "Please use the text editor to resolve the issues...");
            }
            token = token.getNext();
        }
        Node result = stack.pop();
        if(result instanceof Document) {           
            return (Document)result;
        }
        else
            //throw new IllegalArgumentException();
            throw new IOException("Document not well formed/Invalid: " +
                    "Please use the text editor to resolve the issues...");
    }
    
    private boolean isValid(TokenItem token) throws IOException {
        if(token!=null && token.getTokenID()!=null)
            return true;
        else
            throw new IOException("Document parsed is invalid: Please use the text " +
                    "editor to resolve the issues...");
    }
    
    private String combineString(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token t: tokens) {
            sb.append(t.getValue());
        }
        return sb.toString();
    } 
}
