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
 * File       : VBClassStateHandler.java
 * Created on : Dec 12, 2003
 * Author     : Aztec
 */
package org.netbeans.modules.uml.core.reverseengineering.parsingfacilities.translation.statehandlers;

import org.dom4j.Node;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ITokenDescriptor;

/**
 * @author Aztec
 */
public class VBClassStateHandler extends ClassStateHandler
{

    public VBClassStateHandler(
        String language,
        String packageName,
        boolean isInner)
    {
        super(language, packageName, isInner);
    }
    
    public String cleanseComment(String origComment) 
    {
        String retVal = ""; 


        String test = origComment.substring(0, 3);
        if("rem".equalsIgnoreCase(test))
        {
            retVal += origComment.substring(3);
        }   
        else
        {
            // The only other comment style is to start the line with a '
            retVal += origComment.substring(1);
        }
        return retVal;   
    }
    
    public StateHandler createSubStateHandler(String stateName, String language)
    {
        StateHandler retVal = null;
        
        if("Variable Definition".equals(stateName))
        {
            retVal = new VBAttributeStateHandler(language);
        }
        else if(("Method Definition".equals(stateName)) ||           
                ("Method Declaration".equals(stateName)) )
        {
            retVal = new VBOperationStateHandler(language, 
                                                    stateName, 
                                                    OperationStateHandler.OPERATION, 
                                                    isForceAbstractMethods());
        } 
        else if("Structure Declaration".equals(stateName))
        {

            retVal = new StructureStateHandler(language, "", true);
        }
        else if("Enumeration Declaration".equals(stateName))
        {

            retVal= new EnumStateHandler(language, "", true);
        }
        else
        {
            retVal = super.createSubStateHandler(stateName, language);
        }

        if((retVal != null) && (retVal != this))
        {
            Node pClassNode = getDOMNode();

            if(pClassNode != null)
            {
                retVal.setDOMNode(pClassNode);
            }
        }
        return retVal;
    }
    
    public void processToken(ITokenDescriptor pToken, String language)
    {
        super.processToken(pToken, language);
    }
    
    protected void handleName(ITokenDescriptor pToken) 
    {
        if(pToken == null) return;
        
        long line = pToken.getLine();
        long col = pToken.getColumn();
        long pos = pToken.getPosition();
        long length = pToken.getLength();
        
        String nameString = pToken.getValue();

        if(nameString.indexOf("\"") != -1)
        {
           nameString = nameString.substring(1, (int)length - 2);         
        }

        setNodeAttribute("name", nameString);

        createTokenDescriptor("Name", 
                                line, 
                                col + 1, 
                                pos + 1, 
                                nameString, 
                                nameString.length());
    }

}
