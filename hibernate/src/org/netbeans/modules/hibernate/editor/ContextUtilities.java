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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.hibernate.editor;

import org.netbeans.modules.hibernate.completion.*;
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

    private ContextUtilities() {
    }
    
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
    
    public static TokenItem getAttributeToken(TokenItem currentToken) {
        if(currentToken == null )
            return null;
        
        if(isValueToken(currentToken)) {
            TokenItem equalsToken = currentToken.getPrevious();
            if(equalsToken == null)
                return null;
            
            while(equalsToken != null && equalsToken.getTokenID().getNumericID() != XMLDefaultTokenContext.OPERATOR_ID) {
                equalsToken = equalsToken.getPrevious();
            }
        
            TokenItem argumentToken = equalsToken.getPrevious();
            if(argumentToken == null)
                return null;
            
            while(argumentToken != null && argumentToken.getTokenID().getNumericID() != XMLDefaultTokenContext.ARGUMENT_ID) {
                argumentToken = argumentToken.getPrevious();
            }
        
            return argumentToken;
        }
        
        return null;
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
        if(context.getCurrentToken() == null )
            return null;
        
        if(isValueToken(context.getCurrentToken())) {
            TokenItem equalsToken = context.getCurrentToken().getPrevious();
            if(equalsToken == null)
                return null;
            
            //getTokenId() should not return null by JavaDoc. But in reality, it does reutrn null sometimes
            // see issue 67661
            if(equalsToken.getTokenID() == null) {
                return null;
            }
            while(equalsToken != null && equalsToken.getTokenID().getNumericID() != XMLDefaultTokenContext.OPERATOR_ID) {
                equalsToken = equalsToken.getPrevious();
            }
        
            TokenItem argumentToken = equalsToken.getPrevious();
            if(argumentToken == null)
                return null;
            
            while(argumentToken != null && argumentToken.getTokenID().getNumericID() != XMLDefaultTokenContext.ARGUMENT_ID) {
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
        return (tagName.indexOf(":") == -1) ? null : // NOI18N
            tagName.substring(0, tagName.indexOf(":")); // NOI18N
    }
    
    /**
     * Returns the local name from the element's tag.
     */
    public static String getLocalNameFromTag(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? tagName : // NOI18N
            tagName.substring(tagName.indexOf(":")+1, tagName.length()); // NOI18N
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
            return ""; // NOI18N
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

