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
 * File       : VBAttributeStateHandler.java
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
public class VBAttributeStateHandler extends AttributeStateHandler
{


    public VBAttributeStateHandler(String language)
    {
        super(language);
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        
        if("Object Creation".equals(stateName))
        {
            retVal = this;
            setTypeState(true);
        }
   
        if(retVal == null) 
        {      
            retVal = super.createSubStateHandler(stateName, language);
        }
        else
        {
            if(retVal != null && retVal != this)
            {
                Node pNode = getDOMNode();

                if(pNode != null)
                {
                    retVal.setDOMNode(pNode);
                }
            }
        }
        return retVal;
    }
    
    public void initialize()
    {
        super.initialize();
        setNodeAttribute("type", "Variant");
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        if(pToken == null) return;
        
        super.processToken(pToken, language);
        
        String tokenType = pToken.getType();
        if("Modifier".equals(tokenType))
        {
            String value = pToken.getValue();
            if(value != null)
            {
                if(isModifierSame("WithEvents", language, value))
                {
                    setNodeAttribute("isWithEvents", "true");
                }
            }
        }
    }
    
    public void stateComplete(String stateName)
    {
        if("Object Creation".equals(stateName))
        {
            setTypeState(false);
        }
        else
        {
            super.stateComplete(stateName);
        }
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
    
    protected void handleName(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        super.handleName(pToken);

        String value = pToken.getValue();

        updateByTypeSpecifier(value);

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
                setNodeAttribute("type", value);
            }
        }
    }

}
