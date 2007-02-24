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
 * File       : VBOperationStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;
import org.netbeans.modules.uml.core.support.umlsupport.StringUtilities;
import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 * @author Aztec
 */
public class VBOperationStateHandler extends OperationStateHandler
{

    public VBOperationStateHandler(
        String language,
        String stateName,
        int kind,
        boolean forceAbstract)
    {
        super(language, stateName, kind, forceAbstract);
    }
    
    protected void handleName(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        super.handleName(pToken);

        String value = pToken.getValue();

        updateByTypeSpecifier(value);
    }
    
    public String cleanseComment(String origComment) 
    {
        String retVal = ""; 

        ETList< String > lines = StringUtilities.splitOnDelimiter(origComment
                                                                ,"\n");
        int count = 0;
        if(lines != null && (count = lines.size()) > 0)
        {
            for(int i = 0; i < count; ++i)
            {
                String curLine = lines.get(i);
                String test = curLine.substring(0, 3);
                if("rem".equalsIgnoreCase(test))
                {
                    retVal += curLine.substring(3);
                }   
                else if("'".equals(curLine.substring(1)))
                {
                    // The only other comment style is to start the line with a '
                    retVal += curLine.substring(1);
                }
                else 
                {
                   // The only other comment style is to start the line with a '
                   retVal += curLine;
                }
            }
        }
        return retVal;   
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        
        if("Parameter".equals(stateName))
        {
            retVal = new VBParameterStateHandler(null);
        }
        else if("Type".equals(stateName))
        {
            retVal = new VBOpReturnStateHandler( stateName );
        }

        if(retVal == null) 
        {      
            retVal = super.createSubStateHandler(stateName, language);
        }
        else
        {
            if(retVal != null && retVal != this)
            {
                Node pOperationNode = getDOMNode();

                if(pOperationNode != null)
                {
                    retVal.setDOMNode(pOperationNode);
                }
            }
        }
        return retVal;
    }
    
    
    public void handleKeyword(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        String value = pToken.getValue();

        if(value != null)
        {
            if("Sub".equals(value))
            {
                setNodeAttribute("isSub", true) ; 
            }
            else if("Function".equals(value))
            {
                setNodeAttribute("isSub", false) ; 
            }
        }

        super.handleKeyword( pToken );
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;
        
        String tokenType = pToken.getType();
        
        // If the token type is a keyword I just want to
        // record the start token.  I still want the default
        // processing to occur.  So, I will always call 
        // OperationStateHandler::ProcessToken
        if("Keyword".equals(tokenType))
        {
            String tokenValue = pToken.getValue();
            if(("Function".equals(tokenValue)) ||
              ("Sub".equals(tokenValue)) ||
              ("Property".equals(tokenValue)) )
            {
                recordStartToken(pToken);
                handleComment(pToken);
            }

            handleKeyword(pToken);
        }
        super.processToken(pToken, language);
    }

    protected void updateByTypeSpecifier(String typeName) 
    {
        if(typeName != null)
        {
            String value = null;
            String lastChar = typeName.substring(typeName.length() - 1);
            if("%".equals(lastChar))
            {
                value = "Integer";
            }
            else if("!".equals(lastChar))
            {
                value = "Single";
            }
            else if("$".equals(lastChar))
            {
                value = "String";
            }
            else if("@".equals(lastChar))
            {
               value = "Decimal";
            }
            else if("#".equals(lastChar))
            {
               value = "Double";
            }
            else if("&".equals(lastChar))
            {
               value = "Long";
            }

            if(value != null)
            {
                createReturnType(value);
            }
        }
    }

    protected void createReturnType(String typeName) 
    {
        Node pCurNode = getDOMNode();

        // The generalization token descriptor is really a 
        // sub node of the UML:Class XML node.  Therefore, I 
        // will create the TGeneralization under the passed
        // in node.  The TGeneralization node will then be
        // passed to StateHandler to use in the helper methods.
        Node pClassifierFeature = ensureElementExists(pCurNode, 
                                      "UML:Element.ownedElement", 
                                      "UML:Element.ownedElement");

        if(pClassifierFeature != null)
        {
            Node pFeature = createNode(pClassifierFeature, 
                                        "UML:Parameter");

            if(pFeature != null)
            {               
                setNodeAttribute(pFeature, "visibility", "package");
                setNodeAttribute(pFeature, "direction", "result");
                setNodeAttribute(pFeature, "isQuery", "false");
                setNodeAttribute(pFeature, "type", typeName) ;
            }
        }
    }
}
