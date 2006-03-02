/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * 
 * $Id$
 */

package org.netbeans.modules.xml.xdm.nodes;
import java.io.IOException;
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
    
    private BaseDocument basedoc;
    /** Creates a new instance of XMLParser */
    public XMLSyntaxParser(BaseDocument basedoc) {
        this.basedoc = basedoc;
    }
    
    public Document parse() throws IOException, BadLocationException {
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
            currentTokens.add(new Token(token.getImage(),TokenType.TOKEN_CHARACTER_DATA));
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
                    currentTokens.add(new Token(image,coreTokenId));
                    break;
                }
                case XMLTokenIDs.PI_END_ID:
                {
                    coreTokenId = TokenType.TOKEN_PI_END_TAG;
                    currentTokens.add(new Token(image,coreTokenId));
                    if(currentNode instanceof Document) {
                        if(token.getNext().getTokenID().getNumericID() == XMLTokenIDs.TEXT_ID) {
                            token = token.getNext();
                            currentTokens.add(new Token(token.getImage(),TokenType.TOKEN_CHARACTER_DATA));
                        }
                        stack.push(currentNode);
                    } else {
                        // create new PI
//                        currentNode = new ProcessingInstruction();
                    }
                    currentNode.setTokens(new ArrayList<Token>(currentTokens));
                    currentTokens.clear();
                    break;
                }
                case XMLTokenIDs.TAG_ID:
                {
                    int len = image.length();
                    if (image.charAt(len-1) == '>') {
                        if(len==2)
                            currentNode = stack.pop();
                        else if(!(currentNode instanceof Element))
                            currentNode = stack.peek();
                        coreTokenId = TokenType.TOKEN_ELEMENT_END_TAG;
                        currentTokens.add(new Token(image,coreTokenId));
                        currentNode.getTokensForWrite().addAll(currentTokens);
                        currentTokens.clear();
                    } else {
                        coreTokenId = TokenType.TOKEN_ELEMENT_START_TAG;
                        if(image.startsWith("</")) {
                            currentNode = stack.pop();
                            currentTokens.add(new Token(image,coreTokenId));
                        } else {
                            currentNode = new Element();
                            Node parent = stack.peek();
                            parent.appendChild(currentNode);
                            stack.push(currentNode);
                            currentTokens.add(new Token(image,coreTokenId));
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
                    currentTokens.add(new Token(image,coreTokenId));
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
                    currentTokens.add(new Token(image,coreTokenId));
                    currentNode.setTokens(new ArrayList<Token>(currentTokens));
                    currentTokens.clear();
                    break;
                }
                case XMLTokenIDs.TEXT_ID:
                case XMLTokenIDs.BLOCK_COMMENT_ID:
                {
                    coreTokenId = TokenType.TOKEN_CHARACTER_DATA;
                    currentNode = new Text();
                    currentTokens.add(new Token(image,coreTokenId));
                    if(numericId == XMLTokenIDs.TEXT_ID) {
                        while(token.getNext() != null) {
                            int nextNumericId = token.getNext().getTokenID().getNumericID();
                            if(nextNumericId != XMLTokenIDs.TEXT_ID && nextNumericId != XMLTokenIDs.CHARACTER_ID)
                                break;
                            token = token.getNext();
                            currentTokens.add(new Token(token.getImage(),coreTokenId));
                        }
                    }
                    currentNode.setTokens(new ArrayList<Token>(currentTokens));
                    Node parent = stack.peek();
                    parent.appendChild(currentNode);
                    currentTokens.clear();
                    break;
                }
                case XMLTokenIDs.WS_ID:
                {
                    coreTokenId = TokenType.TOKEN_WHITESPACE;
                    currentTokens.add(new Token(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.OPERATOR_ID:
                {
                    coreTokenId = TokenType.TOKEN_ATTR_EQUAL;
                    currentTokens.add(new Token(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.DECLARATION_ID:
                {
                    coreTokenId = TokenType.TOKEN_DTD_VAL;
                    currentTokens.add(new Token(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.PI_CONTENT_ID:
                {
                    coreTokenId = TokenType.TOKEN_PI_VAL;
                    currentTokens.add(new Token(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.PI_TARGET_ID:
                {
                    coreTokenId = TokenType.TOKEN_PI_NAME;
                    currentTokens.add(new Token(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.CDATA_SECTION_ID:
                {
                    coreTokenId = TokenType.TOKEN_CDATA_VAL;
                    currentTokens.add(new Token(image, coreTokenId));
                    break;
                }
                case XMLTokenIDs.CHARACTER_ID:
                case XMLTokenIDs.ERROR_ID:
                case XMLTokenIDs.EOL_ID:
                default:
                    //throw new IllegalArgumentException();
					throw new IOException("Invalid token found in document: " +
						"Please use the text editor to resolve the issues...");
            }
            token = token.getNext();
        }
        Node result = stack.pop();
        if(result instanceof Document)
            return (Document)result;
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
    
}
