/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * File       : StateHandler.java
 * Created on : Dec 5, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

import org.netbeans.modules.uml.core.coreapplication.ICoreProduct;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParser;
import org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.IUMLParserEventDispatcher;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacility;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.IFacilityManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguageManager;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ISyntaxToken;
import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.ProductRetriever;
import org.netbeans.modules.uml.core.support.umlsupport.XMLManip;

/**
 * @author Aztec
 */
public class StateHandler
{
    Node                m_pNode;
    Node                m_TokenDescriptors;

    // Members used to record the start token.  I may want to move this
    // functionallity to a helper class.
    long                m_StartLine;
    long                m_StartColumn;
    long                m_StartPosition;
    ITokenDescriptor    m_StartToken;

    public StateHandler()
    {
        m_pNode = null;
        m_StartToken = null;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.StateHandler#createSubStateHandler(java.lang.String, java.lang.String)
     */
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        // No valid implementation in the C++ code base.
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.StateHandler#getDOMNode()
     */
    public Node getDOMNode()
    {
        return m_pNode;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.StateHandler#getEventDispatcher()
     */
    public IUMLParserEventDispatcher getEventDispatcher()
    {
        IUMLParserEventDispatcher pVal = null;

        // In order to retrieve the correct dispatcher I must first retrieve the faciltiy manager
        // from the core product.  There will only be one facility factory on the core product.
        // Therefore, every one will be using the same dispatcher.

        ICoreProduct pProduct = ProductRetriever.retrieveProduct();

        if(pProduct != null)
        {
            IFacilityManager pManager = pProduct.getFacilityManager();

            if(pManager != null)
            {
                IFacility pFacility = pManager
                                        .retrieveFacility("Parsing.UMLParser");
                IUMLParser pParser = (pFacility instanceof IUMLParser)?
                                        (IUMLParser)pFacility : null;
                if(pParser != null)
                {
                    pVal = pParser.getUMLParserDispatcher();
                }
            }
        }
        return pVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.StateHandler#initialize()
     */
    public void initialize()
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.StateHandler#processToken(org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor, org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ILanguage)
     */
    public void processToken(ITokenDescriptor desc, String lang)
    {
        // No valid implementation in the C++ code base.
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.StateHandler#setDOMNode(org.dom4j.Node)
     */
    public void setDOMNode(Node pNewVal)
    {
        m_pNode = pNewVal;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.StateHandler#stateComplete()
     */
    public void stateComplete(String val)
    {
        // No valid implementation in the C++ code base.
    }


    /**
     * Creates a new Token Descriptor tag for the specified XML DOM node.  The
     * assumption is that the specified not is the node that must contain the
     * TokenDescriptor node.  If the TokenDescriptor must reside under the
     * TokenDescriptors tag then use one of the CreateTokenDescriptor methods.
     *
     * @param type [in] The name of the token descriptor.
     * @param line [in] The token's line number.
     * @param col [in] The token's column number.
     * @param position [in] The token's file position number.
     * @param value [in] The token's value.
     * @param lenght [in] The token's value length.
     * @see #CreateTokenDescriptor(CComBSTR type, long line, long col, long pos, CComBSTR value, long length)
     */
    protected void createDescriptor(String type,
                                           long line,
                                           long col,
                                           long pos,
                                           String value,
                                           long length)
    {
       createDescriptor(m_pNode, type, line, col, pos, value, length);
    }

    /**
     * Creates a new Token Descriptor tag for the specified XML DOM node.  The
     * assumption is that the specified not is the node that must contain the
     * TokenDescriptor node.  If the TokenDescriptor must reside under the
     * TokenDescriptors tag then use one of the CreateTokenDescriptor methods.
     *
     * @param pNode [in] The node to recieve the token descriptor.
     * @param type [in] The name of the token descriptor.
     * @param line [in] The token's line number.
     * @param col [in] The token's column number.
     * @param position [in] The token's file position number.
     * @param value [in] The token's value.
     * @param lenght [in] The token's value length.
     * @see #CreateTokenDescriptor(IXMLDOMNode* pNode, CComBSTR type, long line, long col, long pos, CComBSTR value, long length)
     */
    protected void createDescriptor(Node pNode,
                                           String type,
                                           long line,
                                           long col,
                                           long pos,
                                           String value,
                                           long length)
    {

        if(pNode == null) return;

        Node pDesc = XMLManip.createElement((Element)pNode, "TDescriptor");
        if(pDesc != null)
        {
            XMLManip.setAttributeValue(pDesc,"line", Long.toString(line));
            XMLManip.setAttributeValue(pDesc,"column",Long.toString(col));
            XMLManip.setAttributeValue(pDesc,"position",Long.toString(pos));
            XMLManip.setAttributeValue(pDesc,"type",type);
            XMLManip.setAttributeValue(pDesc,"value",(value == null)?"":value);
            XMLManip.setAttributeValue(pDesc,"length",Long.toString(length));
        }
    }

    /**
     * Create a new XML node and added to the document.  The node is created in the
     * namespace of the document.  CreateElement will throw
     * _com_error exceptions will an invalid HRESULT is received.
     *
     * @param pDoc [in] The document that will recieve the XML node.
     * @param pOwnerNode [in] The XML node that will be the owner of the new XML node.
     * @param nodeName [in] The name of the new XML node.
     * @param pNewNode [out] The new XML node.
     */
    protected Node createNamespaceElement(Node pOwnerNode,
                                          String nodeName)
    {
        if(pOwnerNode == null || nodeName == null || nodeName.trim().length()==0)
            return null;

        return XMLManip.createElement((Element) pOwnerNode, nodeName);
    }

    /**
     * Create a new XML node and added to the document.  The node is created in the
     * namespace of the document.  CreateNamespaceElement will throw
     * _com_error exceptions will an invalid HRESULT is received.
     *
     * @param nodeName [in] The name of the new XML node.
     * @param pNewNode [out] The new XML node.
     */
    protected Node createNamespaceElement(String nodeName)
    {
        return createNamespaceElement(m_pNode, nodeName);
    }

    /**
     * Creates a node add adds it to the current node. The new node is will <B>NOT</B>
     * become the current node.
     *
     * @param nodeName [in] The name of node.
     * @param pNode [out] The new node.
     */
    protected Node createNode(String nodeName)
    {
        return createNode(m_pNode, nodeName);
    }


    /**
     * Create a new XML node and added to the document.  CreateElement will throw
     * _com_error exceptions will an invalid HRESULT is received.
     * @param pDoc [in] The document that will recieve the XML node.
     * @param nodeName [in] The name of the new XML node.
     * @param pNewNode [out] The new XML node.
     */
    protected Node createNode(Node pOwner, String nodeName)
    {
        if(pOwner == null || nodeName == null || nodeName.trim().length()==0)
             return null;

        return (pOwner instanceof Document)?
                 XMLManip.createElement((Document) pOwner, nodeName)
               : XMLManip.createElement((Element) pOwner, nodeName);
    }

//  ********************************************************************
//  ** Helper Methods
//  ********************************************************************

   /**
    * Creates a new Token Descriptor tag for the current XML DOM node.
    *
    * @param type [in] The name of the token descriptor.
    * @param pToken [in] The token data.
    */
    protected void createTokenDescriptor(String type, ITokenDescriptor pToken)
    {
        createTokenDescriptor(m_pNode, type, pToken);
    }

   /**
    * Creates a new Token Descriptor tag for the specified XML DOM node.
    *
    * @param pNode [in] The node to recieve the token descriptor.
    * @param type [in] The name of the token descriptor.
    * @param pToken [in] The token data.
    */
    protected void createTokenDescriptor(Node pNode, String type, ITokenDescriptor pToken)
    {

        if(pToken == null) return;

        long line = pToken.getLine();
        long column = pToken.getColumn();
        long position = pToken.getPosition();
        long length = pToken.getLength();
        String value = pToken.getValue();

        createTokenDescriptor(pNode, type, line, column, position, value, length);
  }

    /**
    * Creates a new Token Descriptor tag for the current XML DOM node.  If the TokenDescritors
    * tag does not exist it will be created.
    *
    * @param type [in] The name of the token descriptor.
    * @param line [in] The token's line number.
    * @param col [in] The token's column number.
    * @param position [in] The token's file position number.
    * @param value [in] The token's value.
    * @param lenght [in] The token's value length.
    */
    protected void createTokenDescriptor(String type,
                                              long line,
                                              long col,
                                              long position,
                                              String value,
                                              long length)
    {
        createTokenDescriptor(m_pNode, type, line, col, position, value, length);
    }


    /**
    * Creates a new Token Descriptor tag for the specified XML DOM node.  If the TokenDescritors
    * tag does not exist it will be created.
    *
    * @param pNode [in] The node to recieve the token descriptor.
    * @param type [in] The name of the token descriptor.
    * @param line [in] The token's line number.
    * @param col [in] The token's column number.
    * @param position [in] The token's file position number.
    * @param value [in] The token's value.
    * @param lenght [in] The token's value length.
    */
    protected void createTokenDescriptor(Node pNode,
                                              String type,
                                              long line,
                                              long col,
                                              long pos,
                                              String value,
                                              long length)
    {
        // If the TokenDescritpors tag has not already been created then it must
        // first be created.  EnsureElementExists will first make sure that
        // someone else did not create it already.
        Node pDescriptors = ensureElementExists(pNode,
                                "TokenDescriptors",
                                XMLManip.getCreateCachedXPath("TokenDescriptors"));

        if(pDescriptors != null)
        {
            createDescriptor(pDescriptors, type, line, col, pos, value, length);
        }
    }

    /**
     * Makes sure that the node with the passed in name is present
     * under curNode. If it isn't, one is created.  XMLManip has a
     * method that does the exact same thing.  The only problem is that
     * XMLMainp wants to create a node with a namespace.  In this
     * case we do not want the namespace.
     *
     * @param name    [in] Name of the node to check for existence for.
     * @parma query   [in] The query string to used to check for existence.
     * @param node    [out] the node representing the element
     */
    protected Node ensureElementExists(String name, String query)
    {
       return ensureElementExists(m_pNode, name, query);
    }

    /**
     * Makes sure that the node with the passed in name is present
     * under curNode. If it isn't, one is created.  XMLManip has a
     * method that does the exact same thing.  The only problem is that
     * XMLMainp wants to create a node with a namespace.  In this
     * case we do not want the namespace.
     *
     * @param curNode [in] The node to append to.
     * @param name    [in] Name of the node to check for existence for.
     * @parma query   [in] The query string to used to check for existence.
     * @param node    [out] the node representing the element
     */
    protected Node ensureElementExists(Node curNode,String name,
                                        String query)

    {
        if(curNode == null) return null;

        Node node = XMLManip.selectSingleNode(curNode, query);

        // If not able to find the node then create it.
        if( node == null )
        {
            // Node doesn't exist, so we need to create it.

            Document doc = curNode.getDocument();

            if(doc != null)
            {
                node = XMLManip.createElement((Element)curNode, name);
            }
        }
        return node;
    }

    /** 
     *  pre-compiled XPath instead of String query version
     */ 
    protected Node ensureElementExists(Node curNode,String name,
                                        XPath query)

    {
        if(curNode == null) return null;
	
	if (query == null) return null;

        Node node = query.selectSingleNode(curNode);

        // If not able to find the node then create it.
        if( node == null )
        {
            // Node doesn't exist, so we need to create it.

            Document doc = curNode.getDocument();

            if(doc != null)
            {
                node = XMLManip.createElement((Element)curNode, name);
            }
        }
        return node;
    }


    protected ILanguage getLanguageDef(String name)
    {

        if(name == null)return null;
        ILanguage pVal = null;
        // Retrieve the LanguageManager from the CoreProduct
        ICoreProduct prod = ProductRetriever.retrieveProduct();
        if(prod != null)
        {
            ILanguageManager pLangManager = prod.getLanguageManager();
            if(pLangManager != null)
            {
                pVal = pLangManager.getLanguage(name);
            }
        }
        return pVal;
    }

    /**
     * Adds the <code>StartPosition</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleBodyStart(ITokenDescriptor pToken)
    {
        createTokenDescriptor("Body Start", pToken);
    }

    /**
     * Adds the tokens coment information to the current XML DOM Node.
     *
     * @param pToken [in] The token information.
     */
    protected void handleComment(ITokenDescriptor pToken)
    {
        handleComment(m_pNode, pToken);
    }

    /**
     * Adds the tokens coment information to the current XML DOM Node.
     *
     * @param pNode [in] The node to update.
     * @param pToken [in] The token information.
     */
    protected void handleComment(Node pNode, ITokenDescriptor pToken)
    {
        if(pToken != null)
        {
            String comment = pToken.getProperty("Comment");

            if(comment != null && comment.trim().length() > 0)
            {
                if(comment != null)
                {
                    String line = pToken.getProperty("CommentStartLine");
                    String col = pToken.getProperty("CommentStartColumn");
                    String pos = pToken.getProperty("CommentStartPos");
                    String length = pToken.getProperty("CommentLength");

		                       try {
		    createTokenDescriptor(pNode,
                                            "Comment",
                                            Long.parseLong(line),
                                            Long.parseLong(col),
                                            Long.parseLong(pos),
                                            comment,
                                            Long.parseLong(length));
					}
					catch (NumberFormatException e)
					{
						e.printStackTrace();
					}
             }
	  }
	  handleMarkerComment(pNode, pToken);
       }
    }

    protected void handleMarkerComment(Node pNode, ITokenDescriptor pToken)
    {
        if(pToken != null)
        {
            String comment = pToken.getProperty("Marker-Comment");

            if(comment != null && comment.trim().length() > 0)
            {
                if(comment != null)
                {
                    String line = pToken.getProperty("Marker-CommentStartLine");
                    String col = pToken.getProperty("Marker-CommentStartColumn");
                    String pos = pToken.getProperty("Marker-CommentStartPos");
                    String length = pToken.getProperty("Marker-CommentLength");

					try
					{
                    createTokenDescriptor(pNode,
                                            "Marker-Comment",
                                            Long.parseLong(line),
                                            Long.parseLong(col),
                                            Long.parseLong(pos),
                                            comment,
                                            Long.parseLong(length));
					}
					catch (NumberFormatException e)
					{
						e.printStackTrace();
					}
		}
		String[] markerValueNames 
		    = new String[]{"Marker-regen", "Marker-regenbody", "Marker-id"};
		for(String key : markerValueNames) 
		{
		    String value = pToken.getProperty(key);
                    String posStr = pToken.getProperty(key+"StartPos");
                    String lengthStr = pToken.getProperty(key+"Length");
                    long pos = -1;
                    long length = -1;
                    if (posStr != null) 
                    {
                        try
                        {
                            pos = Long.parseLong(posStr);
                        }
                        catch (NumberFormatException e) {}
                    }
                    if (lengthStr != null) 
                    {
                        try
                        {
                            length = Long.parseLong(lengthStr);
                        }
                        catch (NumberFormatException e) {}
                    }
		    if(value != null && value.trim().length() > 0)
		    {
			createTokenDescriptor(pNode,
					      key,
					      -1,
					      -1,
					      pos,
					      value,
					      length);
		    }
		}		    	       
	    }
	}
    }

    /**
     * Adds the <code>EndPosition</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleEndPostion(ITokenDescriptor pToken)
    {
        handleEndPostion(m_pNode, pToken);
    }

    /**
     * Adds the <code>EndPosition</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleEndPostion(Node pNode, ITokenDescriptor pToken)
    {
        if(m_pNode != null)
        {
            long line = pToken.getLine();
            long col  = pToken.getColumn();
            long pos  = pToken.getPosition();
            long len  = pToken.getLength();
            String value = pToken.getValue();

            createTokenDescriptor(pNode, "EndPosition", line, col, pos, value, len);
        }
    }

    /**
     * Adds the <code>Filename</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleFilename(ITokenDescriptor pToken)
    {

        if(m_pNode != null)
        {
            String value = pToken.getProperty("Filename");

            if(value != null)
            {
                createTokenDescriptor("Filename",
                                        -1,
                                        -1,
                                        -1,
                                        value,
                                        value.length());
            }
        }
    }

    /**
     * Adds the <code>Keyword</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleKeyword(ITokenDescriptor pToken)
    {
        if(m_pNode != null)
        {
            createTokenDescriptor("Keyword", pToken);
        }
    }

    /**
     * Generate the XMI data for a modifier.
     *
     * @param pToken [in] The modifier token.
     */
    protected void handleModifier(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;

        long line = pToken.getLine();
        long col =  pToken.getColumn();
        long pos =  pToken.getPosition();
        long length = pToken.getLength();
        String value = pToken.getValue();

        createTokenDescriptor("Modifier", line, col, pos, value, length);

        // In some langauges certian modifiers specify that a class is abstract or
        // constant.  So, I will query if the modifier should be handlec as  abstract
        // or leaf modifiers.  The query methods are virtual so they can be overriden
        // when specifing a new langauge.
        if(isAbstractModifier(value, language))
        {
            setNodeAttribute("isAbstract", true);
        }
        else if(isLeafModifier(value, language))
        {
            setNodeAttribute("isFinal", true);
        }
        else if(isOwnerScopeModifier(value, language))
        {
            setNodeAttribute("isStatic", "true");
        }
        else if(isTransientModifier(value, language))
        {
            setNodeAttribute("isTransient", "true");
        }
        else if(isNativeModifier(value, language))
        {
            setNodeAttribute("isNative", "true");
        }
        else if(isSynchronizedModifier(value, language))
        {
            //_VH(SetNodeAttribute(_T("isSynchronized"), xstring(_T("true"))) );
            setNodeAttribute("concurrency", "guarded");
        }
        else if(isVolatileModifier(value, language))
        {
            setNodeAttribute("isVolatile", "true");
        }
        else if(isStrictfpModifier(value, language))
        {
            setNodeAttribute("isStrictFP", "true");
        }
        else if(isModifierSame("Public Visibility", language, value))
        {
            setNodeAttribute("visibility", "public");
        }
        else if(isModifierSame("Private Visibility", language, value))
        {
            setNodeAttribute("visibility", "private");
        }
        else if(isModifierSame("Protected Visibility", language, value))
        {
            setNodeAttribute("visibility", "protected");
        }
        else if(isModifierSame("Friend Visibility", language, value))
        {
            setNodeAttribute("isFriend", "true");
            setNodeAttribute("visibility", "public");
        }
        else if(isModifierSame("Virtual Method", language, value))
        {
            setNodeAttribute("isVirtual", "true");
        }
    }

    /**
     * Adds the <code>EndPosition</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleStartPosition(ITokenDescriptor pToken)
    {
        handleStartPosition(m_pNode, pToken);
    }

    /**
     * Adds the <code>EndPosition</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleStartPosition(Node pNode, ITokenDescriptor pToken)
    {
        if(m_pNode != null)
        {
            long line = pToken.getLine();
            long col  = pToken.getColumn();
            long pos  = pToken.getPosition();

            createTokenDescriptor(pNode, "StartPosition", line, col, pos, null, 0L);
        }
    }

    /**
     * Adds the <code>StatementTerminator</code> token descriptor.
     *
     * @param pToken [in] The token information.
     */
    protected void handleTerminator(ITokenDescriptor pToken)
    {
        if(m_pNode != null)
        {
            createTokenDescriptor("StatementTerminator", pToken);
        }
    }

    /**
     * Deterimies if the modifier is a abstract modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an abstract modifier.
     */
    protected boolean isAbstractModifier(String value, String language)
    {
       //return value == CComBSTR("abstract");
       return isModifierSame("Abstract", language, value);
    }

    /**
     * Deterimies if the modifier is a Leaf modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Leaf modifier.
     */
    protected boolean isLeafModifier(String value, String languageName)
    {
       //return value == CComBSTR("final");
       return isModifierSame("Leaf", languageName, value);
    }

    /**
     * Deterimies if the modifier is a Owner Scope modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Owner Scope modifier.
     */
    protected boolean isOwnerScopeModifier(String value, String languageName)
    {
        //return value == CComBSTR("static");
        return isModifierSame("OwnerScope", languageName, value);
    }

    /**
     * Deterimies if the modifier is a Transient modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Transient modifier.
     */
    protected boolean isTransientModifier(String value, String languageName)
    {
        //return value == CComBSTR("transient");
        return isModifierSame("Transient", languageName, value);
    }

    /**
     * Deterimies if the modifier is a Native modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Native modifier.
     */
    protected boolean isNativeModifier(String value, String languageName)
    {
       //return value == CComBSTR("native");
        return isModifierSame("Native", languageName, value);
    }

    /**
     * Deterimies if the modifier is a Synchronized modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Synchronized modifier.
     */
    protected boolean isSynchronizedModifier(String value, String languageName)
    {
       //return value == CComBSTR("synchronized");
        return isModifierSame("Guarded", languageName, value);
    }

    /**
     * Deterimies if the modifier is a Volatile modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Volatile modifier.
     */
    protected boolean isVolatileModifier(String value, String languageName)
    {
       //return value == CComBSTR("volatile");
        return isModifierSame("Volatile", languageName, value);
    }

    /**
     * Deterimies if the modifier is a Strictfp modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Strictfp modifier.
     */
    protected boolean isStrictfpModifier(String value, String languageName)
    {
       //return value == CComBSTR("strictfp");
        return isModifierSame("StrictFP", languageName, value);
    }

    /**
     * Deterimies if the modifier is a Visibiltiy modifier.
     *
     * @param value [out] The modifier name.
     *
     * @return true if the modifier is an Visibiltiy modifier.
     */
    protected boolean isVisibiltiyModifier(String value, String languageName)
    {
       return isModifierSame("Visibility", languageName, value);
    }

    protected boolean isModifierSame(String modifierType,
                                        String langName,
                                        String value)
    {
        boolean retVal = false;

        ILanguage pLang = getLanguageDef(langName);

        if(pLang != null)
        {
            ISyntaxToken pToken = pLang.getSyntaxToken(value);

            if(pToken != null)
            {
                String type = pToken.getType();

                if(type != null)
                {
                   retVal = (type.equals(modifierType));
                }
            }
        }
        return retVal;
    }


    /**
     * Sets the node attribute value.
     *
     * @param attrName [in] The name of the attribute.
     * @param value [int] The attributes value.
     */
    protected void setNodeAttribute(String attrName, boolean value)
    {
        setNodeAttribute(m_pNode, attrName, value);
    }

    /**
     * Sets the node attribute value.
     *
     * @param attrName [in] The name of the attribute.
     * @param value [int] The attributes value.
     */
    protected void setNodeAttribute(String attrName, String value)
    {
    	if(value != null && value.length() > 0)
        setNodeAttribute(m_pNode, attrName, value);
    }

    /**
     * Sets the node attribute value.
     *
     * @param pNode [in] The node that ownes the attribute.
     * @param attrName [in] The name of the attribute.
     * @param value [int] The attributes value.
     */
    protected void setNodeAttribute(Node pNode, String attrName, boolean value)
    {
        String boolValue = (value) ? "true" : "false";
        setNodeAttribute(pNode, attrName, boolValue);
    }



    /**
     * Sets the node attribute value.
     *
     * @param pNode [in] The node that ownes the attribute.
     * @param attrName [in] The name of the attribute.
     * @param value [int] The attributes value.
     */
    protected void setNodeAttribute(Node pNode, String attrName, String value)
    {
        if(pNode == null) return;

        if(attrName != null)
        {
            Element element = (pNode instanceof Element)?(Element)pNode : null;
            if(element != null)
            {
                XMLManip.setAttributeValue(element, attrName, value);
            }
        }
    }

    /**
     * Test if the specified token is the first token of the source element.
     * if it is then the location of the token will be record.  When done the
     * token information can be written to the XML stream by calling WriteStartToken.
     *
     * @param pToken [in] The token to test.
     * @see #WriteStartToken()
     */
    protected void recordStartToken(ITokenDescriptor pToken)
    {
        if(m_StartToken == null)
        {
            m_StartToken = pToken;
        }
        else
        {
            long curPos = m_StartToken.getPosition();
            long newPos = pToken.getPosition();

            if(newPos < curPos)
            {
                m_StartToken = pToken;
            }
        }
    }

    /**
     * If a token descriptor of type @a type exists under @a pNode, its
     * line, column, position, value, and length attributes are set to the
     * values specified in this operation's parameters.
     *
     * If a token descriptor of type @a type does NOT exist under @a pNode,
     * one is created with the attribute values specified in this operation's
     * parameters.
     *
     * This operation is a good way of making sure you only have one of a
     * particular type of token descriptor.
     *
     * @param pNode[in] the parent XML node of the TokenDescriptor you're trying to set
     * @param type[in] the TokenDescriptor's type
     * @param line[in] the line that the TokenDescriptor starts on
     * @param col[in] the column that the TokenDescriptor starts in
     * @param position[in] the position that the TokenDescriptor starts at
     * @param value[in] the value of the TokenDescriptor
     * @param length[in] the length of the TokenDescriptor
     */
    protected void setTokenDescriptor(Node pNode,
                                        String type,
                                        long line,
                                        long col,
                                        long position,
                                        String value,
                                        long length)
    {
        if( pNode == null) return;

        String query = "TokenDescriptors/TDescriptor[@type=\"";
        query += type;
        query += "\"]";

        // Try to find a TokenDescriptor of type @a type
        Node pTokenDescriptorNode =
            XMLManip.selectSingleNode(pNode, query);

        if( pTokenDescriptorNode != null)
        {
            // A TokenDescriptor of type @a type already exists.  Just
            // change its attributes.
            XMLManip.setAttributeValue( pTokenDescriptorNode, "line", Long.toString(line));
            XMLManip.setAttributeValue( pTokenDescriptorNode, "column", Long.toString(col));
            XMLManip.setAttributeValue( pTokenDescriptorNode, "position", Long.toString(position));
            XMLManip.setAttributeValue( pTokenDescriptorNode, "value", value);
            XMLManip.setAttributeValue( pTokenDescriptorNode, "length", Long.toString(length));
        }
        else
        {
            // No TokenDescriptor of type @a type exists.  Create one.
            createTokenDescriptor( pNode, type, line, col, position, value, length );
        }
    }

    /**
     * Write the start token to the XML stream.  The start token must be initialized
     * by calling the RecordStartToken method.
     *
     * @see #RecordStartToken(ITokenDescriptor * pToken)
     */
    protected void writeStartToken()
    {
        writeStartToken(m_pNode);
    }

    protected void writeStartToken(Node pNode)
    {
        if(m_StartToken != null)
        {
            String query = "TokenDescriptors/TDescriptor[@type = \"StartPosition\"]";

            Node pStartNode = XMLManip.selectSingleNode(pNode, query);

            // Make sure that no modifiers have set the start position attribute.
            if(pStartNode == null)
            {
                handleStartPosition(pNode, m_StartToken);
            }
            else if(isTokenFirst(pStartNode, m_StartToken))
            {

                Element pElement = (pStartNode instanceof Element)?
                                (Element)pStartNode : null;
                if(pElement != null)
                {
                    long line = m_StartToken.getLine();
                    long column = m_StartToken.getColumn();
                    long position = m_StartToken.getPosition();
                    long length = m_StartToken.getLength();
                    String value = m_StartToken.getValue();

                    XMLManip.setAttributeValue( pElement, "line", Long.toString(line));
                    XMLManip.setAttributeValue( pElement, "column", Long.toString(column));
                    XMLManip.setAttributeValue( pElement, "position", Long.toString(position));
                    XMLManip.setAttributeValue( pElement, "type", "StartPosition");
                    XMLManip.setAttributeValue( pElement, "value", value);
                    XMLManip.setAttributeValue( pElement, "length", Long.toString(length));
                }
            }
        }
    }


    /**
     * Determines if the a token file position is befoer the a speified token.
     *
     * @param pNode [in] The token to check.
     * @param pToken [in] The token to check against.
     *
     * @return true if the test token is befor the specified token.
     */
    protected boolean isTokenFirst(Node pNode, ITokenDescriptor pToken)
    {
        boolean retVal = false;

        if( (pNode != null) && (pToken != null))
        {
            int curPos = -1;
            try
            {
                 curPos = XMLManip.getAttributeIntValue(pNode, "position");
            }
            catch(Exception e)
            {
                e.printStackTrace();
                curPos = -1;
            }


            long tokenPos = -1;
            try
            {
                tokenPos = pToken.getPosition();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                tokenPos = -1;
            }


            if(tokenPos > 0)
            {
                if((curPos >= 0) && (tokenPos < curPos))
                {
                    retVal = true;
                }
            }
        }
        else if(pNode != null)
        {
            retVal = true;
        }

        return retVal;
    }

}
