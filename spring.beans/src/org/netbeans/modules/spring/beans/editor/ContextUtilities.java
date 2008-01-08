/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.spring.beans.editor;

import javax.xml.XMLConstants;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;

/**
 *
 * @author Rohan Ranade
 */
public final class ContextUtilities {

    public static boolean isValueToken(TokenItem currentToken) {
        if(currentToken != null) {
            if (currentToken.getTokenID().getNumericID() == XMLDefaultTokenContext.VALUE_ID) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isTagToken(TokenItem currentToken) {
        if(currentToken != null) {
            if (currentToken.getTokenID().getNumericID() == XMLDefaultTokenContext.TAG_ID) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isAttributeToken(TokenItem currentToken) {
        if(currentToken != null) {
            if (currentToken.getTokenID().getNumericID() == XMLDefaultTokenContext.ARGUMENT_ID) {
                return true;
            }
        }
        
        return false;
    }

    private ContextUtilities() {
    }
  
    public static Tag getCurrentTagElement(DocumentContext context) {
        SyntaxElement element = context.getCurrentElement();
        if(element instanceof StartTag) {
            return (StartTag) element;
        } else if(element instanceof EmptyTag) {
            return (EmptyTag) element;
        }
        
        return null;
    }
    
    public static TokenItem getAttributeToken(DocumentContext context) {
        if(isValueToken(context.getCurrentToken())) {
            TokenItem equalsToken = context.getCurrentToken().getPrevious();
            while(equalsToken.getTokenID().getNumericID() != XMLDefaultTokenContext.OPERATOR_ID) {
                equalsToken = equalsToken.getPrevious();
            }
        
            TokenItem argumentToken = equalsToken.getPrevious();
            while(argumentToken.getTokenID().getNumericID() != XMLDefaultTokenContext.ARGUMENT_ID) {
                argumentToken = argumentToken.getPrevious();
            }
        
            return argumentToken;
        }
        
        return null;
    }
    
    public static String getAttributeTokenImage(DocumentContext context) {
        TokenItem tok = getAttributeToken(context);
        if(tok != null) {
            return tok.getImage();
        }
        
        return null;
    }
    
    /**
     * Returns the prefix from the element's tag.
     */
    public static String getPrefixFromTag(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? null :
            tagName.substring(0, tagName.indexOf(":"));
    }
    
    /**
     * Returns the local name from the element's tag.
     */
    public static String getLocalNameFromTag(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? tagName :
            tagName.substring(tagName.indexOf(":")+1, tagName.length());
    }
    
    /**
     * Returns any prefix declared with this namespace. For example, if
     * the namespace was declared as xmlns:po, the prefix 'po' will be returned.
     * Returns null for declaration that contains no prefix.
     */
    public static String getPrefixFromNamespaceDeclaration(String namespace) {
        if (!namespace.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) return null;
        int xmlnsLength = XMLConstants.XMLNS_ATTRIBUTE.length();
        if (namespace.length() == xmlnsLength) {
            return "";
        }
        if (namespace.charAt(xmlnsLength) == ':') {
            return namespace.substring(xmlnsLength + 1);
        }
        return null;
    }
    
    public static String getPrefixFromNodeName(String nodeName) {
        int colonIndex = nodeName.indexOf(':');
        if (colonIndex <= 0) {
            return null;
        }
        return nodeName.substring(0, colonIndex);
    }
    
    public static StartTag getRoot(SyntaxElement se) {
        StartTag root = null;
        while( se != null) {
            if(se instanceof StartTag) {
                root = (StartTag)se;
            }
            se = se.getPrevious();
        }
        
        return root;
    }
    
}

